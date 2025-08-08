import {
  FaBuilding,
  FaMapMarkerAlt,
  FaRegBookmark,
  FaBookmark,
} from "react-icons/fa";
import { useNavigate } from "react-router-dom";
import { useState, useEffect } from "react";
import { successToast, infoToast } from "../utils/toastUtils";

export default function JobDetails({ job }) {
  const navigate = useNavigate();
  const [saved, setSaved] = useState(false);
  const [hasApplied, setHasApplied] = useState(false);

  useEffect(() => {
    const fetchStatuses = async () => {
      const token = localStorage.getItem("token");
      if (!token || !job) return;

      try {
        const res = await fetch("http://localhost:8080/user/saved-jobs", {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        if (res.ok) {
          const savedJobs = await res.json();
          const isSaved = savedJobs.some((savedJob) => savedJob.id === job.id);
          setSaved(isSaved);
        }

        const appliedRes = await fetch(
          `http://localhost:8080/applications/has-applied/${job.id}`,
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );

        if (appliedRes.ok) {
          const applied = await appliedRes.json();
          setHasApplied(applied);
        }
      } catch (err) {
        console.error("Error fetching status:", err);
      }
    };

    fetchStatuses();
  }, [job]);

  const formatDate = (dateStr) => {
    const options = { year: "numeric", month: "long", day: "numeric" };
    return new Date(dateStr).toLocaleDateString(undefined, options);
  };

  const handleApply = () => {
    navigate(`/apply/${job.id}`);
  };

  const toggleSave = async () => {
    const token = localStorage.getItem("token");

    if (!token) {
      infoToast("You need to be logged in to save jobs.");
      return;
    }

    const endpoint = saved
      ? `http://localhost:8080/user/unsave-job/${job.id}`
      : `http://localhost:8080/user/save-job/${job.id}`;
    const method = saved ? "DELETE" : "POST";

    try {
      const response = await fetch(endpoint, {
        method,
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (response.ok) {
        setSaved(!saved);
        successToast(saved ? "Job removed from saved list." : "Job saved successfully!");
      } else {
        const errorMsg = await response.text();
        successToast(`Failed to ${saved ? "unsave" : "save"} job: ${errorMsg}`);
      }
    } catch (err) {
      console.error(err);
      successToast("An error occurred while saving the job.");
    }
  };

  if (!job)
    return <div className="text-gray-500">Select a job to view details</div>;

  return (
    <div className="relative bg-white p-6 rounded-lg shadow-lg border border-gray-200 w-full">
      {/* Action Buttons */}
      <div className="absolute top-4 right-4 flex items-center gap-3">
        <button
          onClick={toggleSave}
          className="text-[#6B3F27] hover:text-[#5C3421] transition-colors duration-200"
          title={saved ? "Unsave job" : "Save job"}
        >
          {saved ? <FaBookmark size={20} /> : <FaRegBookmark size={20} />}
        </button>

        <button
          onClick={
            hasApplied ? () => navigate("/myjobs?tab=applied") : handleApply
          }
          className={`px-4 py-2 rounded font-medium transition-all duration-200 ${
            hasApplied
              ? "bg-gray-500 hover:bg-gray-600 text-white"
              : "bg-[#6B3F27] hover:bg-[#5C3421] text-white"
          }`}
        >
          {hasApplied ? "Applied – View Application" : "Apply"}
        </button>
      </div>

      {/* Job Title & Logo */}
      <div className="flex items-center gap-4 mb-4">
        <div className="w-16 h-16 rounded border overflow-hidden bg-gray-100">
          <img
            src={
              job.profilePicture
                ? `http://localhost:8080/files/profile-picture/${job.profilePicture}`
                : "/default-logo.png"
            }
            alt={job.companyName || "Company Logo"}
            className="w-full h-full object-contain"
          />
        </div>
        <h1 className="text-2xl font-bold text-[#000000]">{job.title}</h1>
      </div>

      {/* Company Info */}
      <p className="text-sm text-gray-700 mb-1 flex items-center">
        <FaBuilding className="mr-2 text-[#6B3F27]" /> {job.companyName}
      </p>
      <p className="text-sm text-gray-600 mb-4 flex items-center">
        <FaMapMarkerAlt className="mr-2 text-[#6B3F27]" /> {job.location}
      </p>

      {/* Job Meta Tags */}
      <div className="flex flex-wrap gap-2 mb-6">
        <span className="bg-[#6B3F27] text-white text-xs font-semibold px-3 py-1 rounded-full flex items-center gap-1">
          <FaBuilding size={12} /> {job.type}
        </span>
        <span className="bg-[#6B3F27] text-white text-xs font-semibold px-3 py-1 rounded-full flex items-center gap-1">
          <FaMapMarkerAlt size={12} /> {job.workMode}
        </span>
        <span className="bg-gray-100 text-gray-700 text-xs font-semibold px-3 py-1 rounded-full">
          Posted: {formatDate(job.postedAt)}
        </span>
      </div>

      {/* Description */}
      {job.description?.trim() && (
        <div className="mb-6">
          <h2 className="text-black font-semibold text-base mb-1">
            Job Description
          </h2>
          <p className="text-sm text-gray-800 leading-relaxed">
            {job.description}
          </p>
        </div>
      )}

      {/* Responsibilities */}
      {Array.isArray(job.responsibilities) && job.responsibilities.length > 0 && (
        <div className="mb-6">
          <h2 className="text-black  font-semibold text-base mb-1">
            Responsibilities
          </h2>
          <ul className="list-disc list-inside text-sm text-gray-800 leading-relaxed">
            {job.responsibilities.map((item, idx) => (
              <li key={idx}>{item}</li>
            ))}
          </ul>
        </div>
      )}

      {/* Skills */}
      {Array.isArray(job.requiredSkills) && job.requiredSkills.length > 0 && (
        <div>
          <h2 className="text-black  font-semibold text-base mb-1">
            Required Skills
          </h2>
          <ul className="list-disc list-inside text-sm text-gray-800 leading-relaxed">
            {job.requiredSkills.map((item, idx) => (
              <li key={idx}>{item}</li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}
