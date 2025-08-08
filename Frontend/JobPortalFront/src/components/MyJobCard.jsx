import { useEffect, useState } from "react";
import { FaBookmark, FaTrash } from "react-icons/fa";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { successToast, errorToast } from "../utils/toastUtils"; // ✅ updated import

export default function MyJobCard({ job, tab, onUnsave, onWithdraw, onRefresh }) {
  const navigate = useNavigate();
  const [isApplied, setIsApplied] = useState(false);
  const [isSaved, setIsSaved] = useState(true);

  const token = localStorage.getItem("token");

  const formatDate = (isoDate) => {
    const date = new Date(isoDate);
    return date.toLocaleDateString("en-US", {
      weekday: "short",
      month: "short",
      day: "numeric",
    });
  };

  useEffect(() => {
    if (tab === "saved") {
      axios
        .get(`http://localhost:8080/applications/has-applied/${job.id}`, {
          headers: { Authorization: `Bearer ${token}` },
        })
        .then((res) => setIsApplied(res.data))
        .catch((err) => console.error("❌ Error checking if applied:", err));
    }
  }, [job.id, tab]);

  const handleUnsave = async () => {
    try {
      const response = await fetch(
        `http://localhost:8080/user/unsave-job/${job.id}`,
        {
          method: "DELETE",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (response.ok) {
        successToast("Job removed from saved list."); // ✅ using your custom toast
        setIsSaved(false);
        if (onUnsave) onUnsave(job.id);
        if (onRefresh) onRefresh();
      } else {
        errorToast("Failed to unsave job."); // ✅
      }
    } catch (error) {
      console.error("Error unsaving job:", error);
      errorToast("An error occurred while unsaving."); // ✅
    }
  };

  const handleApply = () => {
    navigate(`/apply/${job.id}`);
    if (onRefresh) onRefresh();
  };

  return (
    isSaved && (
      <div
        key={job.id}
        className="flex justify-between items-center px-4 py-4 mb-4 rounded-lg border border-gray-200 bg-white hover:shadow-md transition-all"
      >
        {/* Left - Logo and Info */}
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 rounded-full bg-gray-100 border flex items-center justify-center overflow-hidden">
            <img
              src={`http://localhost:8080/files/profile-picture/${job.profilePicture}`}
              alt="Company Logo"
              className="w-full h-full object-contain"
            />
          </div>

          <div>
            <h2 className="text-lg font-semibold text-[#000000]">{job.title}</h2>
            <p className="text-sm text-gray-700 font-medium">{job.companyName}</p>
            <p className="text-sm text-gray-600">{job.location}</p>
            <p className="text-xs text-gray-500">
              {tab === "saved"
                ? `Saved on ${formatDate(job.postedAt)}`
                : `Applied on ${formatDate(job.postedAt)}`}
            </p>
          </div>
        </div>

        {/* Right - Actions */}
        <div className="flex items-center gap-4">
          {tab === "saved" && (
            <>
              {!isApplied ? (
                <button
                  className="bg-[#6B3F27] hover:bg-[#5C3421] text-white px-4 py-1 rounded transition-all"
                  onClick={handleApply}
                >
                  Apply now
                </button>
              ) : (
                <span className="text-sm px-3 py-1 rounded border border-[#6B3F27] text-[#6B3F27] bg-gray-100 font-medium">
                  Applied
                </span>
              )}

              <FaBookmark
                className="text-[#6B3F27] hover:text-red-500 cursor-pointer text-lg"
                title="Unsave"
                onClick={handleUnsave}
              />
            </>
          )}

          {tab === "applied" && (
            <>
              <span className="text-sm bg-[#6B3F27] text-white px-4 py-1 rounded">
                Applied
              </span>
              <FaTrash
                className="text-gray-500 hover:text-red-600 cursor-pointer"
                title="Withdraw application"
                onClick={() => {
                  if (onWithdraw) onWithdraw(job.id);
                  if (onRefresh) onRefresh();
                }}
              />
            </>
          )}

          <span className="text-xl cursor-pointer" title="More options">⋮</span>
        </div>
      </div>
    )
  );
}
