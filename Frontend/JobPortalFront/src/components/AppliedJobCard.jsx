import { useNavigate } from "react-router-dom";
import axios from "axios";
import { toast } from "react-toastify";

export default function AppliedJobCard({ application, onWithdraw, onRefresh }) {
  const navigate = useNavigate();
  const token = localStorage.getItem("token");

  const formatDate = (isoDate) => {
    const date = new Date(isoDate);
    return date.toLocaleDateString("en-US", {
      weekday: "short",
      month: "short",
      day: "numeric",
    });
  };

  const handleWithdraw = async () => {
    try {
      await axios.delete(
        `http://localhost:8080/applications/${application.applicationId}`,
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      toast.success("Application withdrawn successfully");
      if (onWithdraw) onWithdraw(application.applicationId);
      if (onRefresh) onRefresh();
    } catch (err) {
      toast.error("Failed to withdraw application");
      console.error("Error withdrawing application:", err);
    }
  };

  return (
    <div
      key={application.applicationId}
      className="flex justify-between items-center px-4 py-4 mb-4 rounded-lg border border-gray-200 bg-white hover:shadow-sm transition"
    >
      {/* Left: Logo + Info */}
      <div className="flex items-center gap-4">
        <div className="w-12 h-12 bg-gray-100 border rounded-full overflow-hidden flex items-center justify-center">
          <img
            src={`http://localhost:8080/files/profile-picture/${application.companyLogoUrl}`}
            alt="Company Logo"
            className="w-full h-full object-contain"
          />
        </div>
        <div>
          <h2 className="text-lg font-semibold text-[#000000]">{application.jobTitle}</h2>
          <p className="text-sm text-gray-700">{application.companyName}</p>
          <p className="text-sm text-gray-600">{application.location}</p>
          <p className="text-xs text-gray-500">
            Applied on {formatDate(application.appliedAt)}
          </p>
        </div>
      </div>

      {/* Right: Status + Actions */}
      <div className="flex flex-col md:flex-row gap-2 items-center">
        <span className="text-sm border border-gray-400 text-gray-800 px-4 py-1 rounded-full font-medium">
          {application.status}
        </span>

        <button
          className="border border-black text-black px-4 py-1 rounded hover:bg-black hover:text-white transition text-sm"
          onClick={() =>
            navigate(`/applications/${application.applicationId}`)
          }
        >
          View Application
        </button>

        <button
          onClick={handleWithdraw}
          className="bg-[#6B3F27] hover:bg-[#5C3421] text-white px-4 py-1 rounded text-sm transition"
        >
          Withdraw
        </button>
      </div>
    </div>
  );
}
