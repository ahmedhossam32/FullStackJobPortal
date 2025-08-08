import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { toast } from "react-toastify";

export default function ApplicationDetailsEmployerPage() {
  const { id } = useParams();
  const [application, setApplication] = useState(null);
  const [selectedStatus, setSelectedStatus] = useState("");
  const [isUpdating, setIsUpdating] = useState(false);
  const token = localStorage.getItem("token");

  useEffect(() => {
    async function fetchApplication() {
      try {
        const res = await fetch(`http://localhost:8080/applications/employer/${id}`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        if (!res.ok) throw new Error("Failed to fetch application");

        const data = await res.json();
        setApplication(data);
        setSelectedStatus(data.status);
      } catch (err) {
        console.error("Error fetching application:", err);
        toast.error("Failed to load application");
      }
    }

    fetchApplication();
  }, [id, token]);

  const handleViewResume = async () => {
    try {
      const res = await fetch(`http://localhost:8080/files/resume/${application.resumeUrl}`, {
        headers: { Authorization: `Bearer ${token}` },
      });

      if (!res.ok) throw new Error("Failed to fetch resume");

      const blob = await res.blob();
      const url = window.URL.createObjectURL(blob);
      window.open(url, "_blank");
    } catch (err) {
      console.error(err);
      toast.error("Unable to view resume");
    }
  };

  const handleStatusUpdate = async () => {
    try {
      setIsUpdating(true);
      const res = await fetch(`http://localhost:8080/applications/${id}/status`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ status: selectedStatus }),
      });

      if (!res.ok) throw new Error("Failed to update status");

      toast.success("Application status updated successfully");
      setApplication({ ...application, status: selectedStatus });
    } catch (err) {
      console.error(err);
      toast.error("Failed to update application status");
    } finally {
      setIsUpdating(false);
    }
  };

  if (!application) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-10 text-center text-gray-500 text-sm">
        Loading application...
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-4 py-10">
      <div className="bg-white shadow-lg border border-gray-200 rounded-2xl p-8 flex flex-col sm:flex-row gap-6">
        {/* Left: Profile Picture */}
        <div className="flex-shrink-0">
          <img
            src={`http://localhost:8080/files/profile-picture/${application.applicantProfilePicture}`}
            alt="Profile"
            className="w-32 h-32 object-cover rounded-full border"
          />
        </div>

        {/* Right: Info */}
        <div className="flex-grow text-gray-800">
          <h2 className="text-2xl font-bold mb-1">{application.applicantName}</h2>
          <p className="text-gray-600 mb-4">@{application.applicantUsername}</p>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-sm">
            <p><strong>Email:</strong> {application.applicantEmail}</p>
            <p><strong>Date of Birth:</strong> {application.applicantDOB}</p>
            <p><strong>Applied on:</strong> {new Date(application.appliedAt).toLocaleDateString()}</p>
            <p><strong>Job Title:</strong> {application.jobTitle}</p>
            <p className="col-span-2">
              <strong>Status:</strong>{" "}
              <span
                className={`inline-block px-2 py-1 rounded-full text-xs font-semibold ${
                  application.status === "PENDING"
                    ? "bg-yellow-100 text-yellow-800"
                    : application.status === "ACCEPTED"
                    ? "bg-green-100 text-green-800"
                    : "bg-red-100 text-red-700"
                }`}
              >
                {application.status}
              </span>
            </p>
          </div>

          <div className="mt-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            {/* View Resume */}
            <button
              onClick={handleViewResume}
              className="text-sm text-[#6B3F27] font-medium hover:underline"
            >
              📎 View Resume
            </button>

            {/* Status Selector + Save */}
            <div className="flex items-center gap-3">
              <select
                value={selectedStatus}
                onChange={(e) => setSelectedStatus(e.target.value)}
                className="border border-gray-300 rounded px-3 py-1 text-sm"
              >
                <option value="PENDING">PENDING</option>
                <option value="ACCEPTED">ACCEPTED</option>
                <option value="REJECTED">REJECTED</option>
              </select>
              <button
                onClick={handleStatusUpdate}
                disabled={isUpdating || selectedStatus === application.status}
                className={`text-sm px-4 py-1.5 rounded transition ${
                  selectedStatus === application.status || isUpdating
                    ? "bg-gray-300 text-gray-600 cursor-not-allowed"
                    : "bg-[#6B3F27] text-white hover:bg-[#5c3421]"
                }`}
              >
                {isUpdating ? "Saving..." : "Save"}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
