import { useParams, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { FaClock, FaEnvelope } from "react-icons/fa";
import { toast } from "react-toastify";

export default function JobApplicantsPage() {
  const { jobId } = useParams();
  const navigate = useNavigate();
  const [applicants, setApplicants] = useState([]);
  const [jobTitle, setJobTitle] = useState("");
  const user = JSON.parse(localStorage.getItem("user"));
  const token = localStorage.getItem("token");

  const logoUrl = user?.profilePicture
    ? `http://localhost:8080/files/profile-picture/${user.profilePicture}`
    : "/default-logo.png";
  const companyName = user?.companyName || user?.name || "Your Company";

  useEffect(() => {
    async function fetchApplicantsAndTitle() {
      try {
        const res = await fetch(`http://localhost:8080/applications/job/${jobId}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        const data = await res.json();
        setApplicants(data);

        if (data.length > 0) {
          setJobTitle(data[0].jobTitle);
        } else {
          const jobRes = await fetch(`http://localhost:8080/jobs/${jobId}`, {
            headers: { Authorization: `Bearer ${token}` },
          });
          const jobData = await jobRes.json();
          setJobTitle(jobData.title || "");
        }
      } catch (err) {
        console.error("Error fetching applicants or job title:", err);
      }
    }

    fetchApplicantsAndTitle();
  }, [jobId, token]);

  const handleViewResume = async (filename) => {
    try {
      const res = await fetch(`http://localhost:8080/files/resume/${filename}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!res.ok) throw new Error("Failed to fetch resume");

      const blob = await res.blob();
      const url = window.URL.createObjectURL(blob);
      window.open(url, "_blank");
    } catch (error) {
      console.error("Error opening resume:", error);
      toast.error("Unable to view resume");
    }
  };

  return (
    <div className="max-w-6xl mx-auto px-4 py-6 bg-white min-h-screen">
      {/* Header */}
      <div className="flex items-center gap-4 mb-8 border-b pb-4">
        <img
          src={logoUrl}
          alt="Company Logo"
          className="w-12 h-12 object-contain rounded border"
        />
        <div>
          <h1 className="text-2xl font-bold">{companyName}</h1>
          <p className="text-gray-700 text-sm mt-1">
            Applications for "{jobTitle}"
          </p>
        </div>
      </div>

      {/* No applicants message */}
      {applicants.length === 0 ? (
        <p className="text-gray-500 text-center text-md italic">
          Currently there are no applicants for "{jobTitle}"
        </p>
      ) : (
        <div className="grid gap-4">
          {applicants.map((app) => (
            <div
              key={app.id}
                onClick={() => navigate(`/employer/applications/${app.id}`)}
              className="bg-gray-100 border border-gray-200 rounded-xl p-5 shadow-md flex justify-between items-center hover:bg-gray-50 transition"
              style={{ borderLeft: "6px solid #6B3F27", paddingLeft: "0.5rem" }}
            >
              {/* Left: profile and info */}
              <div className="flex items-start gap-4">
                <img
                  src={`http://localhost:8080/files/profile-picture/${app.applicantProfilePicture}`}
                  alt="Applicant"
                  className="w-12 h-12 rounded-full object-cover border"
                />
                <div className="leading-snug">
                  <p
                    onClick={() => navigate(`/employer/applications/${app.id}`)}
                    className="font-semibold text-blue-700 hover:underline cursor-pointer"
                  >
                    {app.applicantUsername}
                  </p>
                  <p className="text-sm text-gray-600 flex items-center gap-2">
                    <FaEnvelope className="text-gray-500" /> {app.applicantEmail}
                  </p>
                  <p className="text-sm text-gray-600">
                    <strong>DOB:</strong> {app.applicantDOB}
                  </p>
                  <p className="text-sm text-gray-600 flex items-center gap-2">
                    <FaClock className="text-gray-500" /> Applied on:{" "}
                    {new Date(app.appliedAt).toLocaleDateString()}
                  </p>
                  <button
                    onClick={() => handleViewResume(app.resumeUrl)}
                    className="text-blue-600 hover:underline text-sm mt-2"
                  >
                    📎 View Resume
                  </button>
                </div>
              </div>

              {/* Right: status badge */}
              <span
                className={`text-xs px-3 py-1 rounded-full font-semibold border shadow-sm ${
                  app.status === "PENDING"
                    ? "bg-yellow-100 text-yellow-800 border-yellow-300"
                    : app.status === "ACCEPTED"
                    ? "bg-green-100 text-green-800 border-green-300"
                    : "bg-red-100 text-red-700 border-red-300"
                }`}
              >
                {app.status}
              </span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
