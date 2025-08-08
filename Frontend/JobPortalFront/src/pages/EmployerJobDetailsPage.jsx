import { useParams, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { FaMapMarkerAlt, FaClock } from "react-icons/fa";
import Swal from 'sweetalert2';

export default function EmployerJobDetailsPage() {
  const { jobId } = useParams();
  const navigate = useNavigate();
  const [job, setJob] = useState(null);
  const token = localStorage.getItem("token");

  useEffect(() => {
    async function fetchJobDetails() {
      try {
        const res = await fetch(`http://localhost:8080/jobs/${jobId}`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        if (!res.ok) throw new Error("Failed to fetch job details");

        const data = await res.json();
        setJob(data);
      } catch (error) {
        console.error("❌ Error fetching job:", error.message);
      }
    }

    fetchJobDetails();
  }, [jobId, token]);

  const handleDelete = () => {
    Swal.fire({
      title: `Delete job "${job.title}"?`,
      text: "This will permanently remove the job and all its applicants.",
      icon: "warning",
      showCancelButton: true,
      confirmButtonColor: "#6B3F27", // coffee brown
      cancelButtonColor: "#aaa",
      confirmButtonText: "Yes, delete it",
      cancelButtonText: "Cancel",
      reverseButtons: true,
    }).then(async (result) => {
      if (result.isConfirmed) {
        try {
          const res = await fetch(`http://localhost:8080/jobs/${jobId}`, {
            method: "DELETE",
            headers: {
              Authorization: `Bearer ${token}`,
            },
          });

          if (!res.ok) throw new Error("Failed to delete job");

          Swal.fire({
            title: "Deleted!",
            text: "Job and its applications were removed.",
            icon: "success",
            confirmButtonColor: "#6B3F27",
          });

          navigate("/employer/jobs");
        } catch (err) {
          Swal.fire("Error", "Something went wrong during deletion.", "error");
        }
      }
    });
  };

  const handleUpdate = () => {
    navigate(`/employer/jobs/update/${job.id}`);
  };

  const handleViewApplicants = () => {
    navigate(`/employer/jobs/${job.id}/applicants`);
  };

  if (!job) return <div className="p-6 text-center">Loading...</div>;

  return (
    <div className="max-w-3xl mx-auto px-4 py-6">
      <div className="relative bg-white shadow-xl rounded-2xl p-8 border border-gray-200">
        {/* View Applicants */}
        <button
          onClick={handleViewApplicants}
          className="absolute top-6 right-6 bg-[#6B3F27] text-white px-4 py-2 rounded hover:bg-[#5C3421] text-sm shadow-sm"
        >
          View Applicants for Job
        </button>

        {/* Header */}
        <div className="flex items-center gap-4 mb-4">
          {job.profilePicture && (
            <img
              src={`http://localhost:8080/files/profile-picture/${job.profilePicture}`}
              alt="Company Logo"
              className="w-12 h-12 object-contain rounded border"
            />
          )}
          <h1 className="text-2xl font-bold text-gray-900">{job.title}</h1>
        </div>

        {/* Metadata */}
        <div className="text-sm text-gray-600 flex flex-wrap items-center gap-4 mb-3">
          <span className="flex items-center">
            <FaMapMarkerAlt className="mr-1" /> {job.location}
          </span>
          <span className="flex items-center">
            <FaClock className="mr-1" /> {new Date(job.postedAt).toLocaleString()}
          </span>
          <span className="px-2 py-0.5 text-xs bg-green-100 text-green-700 rounded-full">
            {job.status || "Open"}
          </span>
          <span className="text-xs bg-blue-100 text-blue-700 px-2 py-0.5 rounded-full uppercase">
            {job.type?.replace("_", " ")}
          </span>
          <span className="text-xs bg-purple-100 text-purple-700 px-2 py-0.5 rounded-full uppercase">
            {job.workMode}
          </span>
        </div>

        {/* Company */}
        <div className="mb-2 text-gray-800 text-sm">
          <strong>Company:</strong> {job.companyName}
        </div>

        {/* Description */}
        <div className="mb-4 text-gray-700 text-sm">
          <strong>Description:</strong>
          <p className="mt-1 whitespace-pre-line">{job.description}</p>
        </div>

        {/* Responsibilities */}
        {job.responsibilities?.length > 0 && (
          <div className="mb-4">
            <strong className="text-sm text-gray-800">Responsibilities:</strong>
            <ul className="list-disc ml-6 mt-1 text-sm text-gray-700">
              {job.responsibilities.map((r, i) => (
                <li key={i}>{r}</li>
              ))}
            </ul>
          </div>
        )}

        {/* Required Skills */}
        {job.requiredSkills?.length > 0 && (
          <div className="mb-4">
            <strong className="text-sm text-gray-800">Required Skills:</strong>
            <ul className="list-disc ml-6 mt-1 text-sm text-gray-700">
              {job.requiredSkills.map((s, i) => (
                <li key={i}>{s}</li>
              ))}
            </ul>
          </div>
        )}

        {/* Screening Questions */}
        {job.screeningQuestions?.length > 0 && (
          <div className="mb-6">
            <strong className="text-sm text-gray-800">Screening Questions:</strong>
            <ul className="list-disc ml-6 mt-1 text-sm text-gray-700">
              {job.screeningQuestions.map((q, i) => (
                <li key={i}>{q}</li>
              ))}
            </ul>
          </div>
        )}

        {/* Action Buttons */}
        <div className="flex gap-4 mt-4">
          <button
            onClick={handleUpdate}
            className="bg-[#6B3F27] text-white px-5 py-2 rounded hover:bg-[#5C3421] transition"
          >
            Update
          </button>
          <button
            onClick={handleDelete}
            className="bg-red-600 text-white px-5 py-2 rounded hover:bg-red-700 transition"
          >
            Delete
          </button>
        </div>
      </div>
    </div>
  );
}
