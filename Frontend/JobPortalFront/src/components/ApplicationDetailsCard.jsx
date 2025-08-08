import PropTypes from "prop-types";

export default function ApplicationDetailsCard({ application }) {
  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleString(undefined, {
      dateStyle: "medium",
      timeStyle: "short",
    });
  };

  const handleResumeClick = async (e) => {
    e.preventDefault();
    const token = localStorage.getItem("token");
    const resumeUrl = `http://localhost:8080/files/resume/${application.resumeUrl}`;

    try {
      const response = await fetch(resumeUrl, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!response.ok) throw new Error("Failed to fetch resume.");
      const blob = await response.blob();
      const fileUrl = window.URL.createObjectURL(blob);
      window.open(fileUrl, "_blank");
    } catch (error) {
      console.error("Resume fetch error:", error);
      alert("Failed to open resume. You may not be authorized.");
    }
  };

  const getStatusStyle = (status) => {
    switch (status.toUpperCase()) {
      case "ACCEPTED":
        return "bg-green-100 text-green-700 border-green-300";
      case "REJECTED":
        return "bg-red-100 text-red-700 border-red-300";
      default:
        return "bg-blue-100 text-blue-700 border-blue-300";
    }
  };

  return (
    <div className="max-w-3xl mx-auto mt-10 bg-white p-8 rounded-lg shadow-lg border border-gray-200">
      {/* Header */}
      <div className="flex items-center gap-5 mb-6">
        <div className="w-16 h-16 rounded border bg-white flex items-center justify-center overflow-hidden">
          <img
            src={`http://localhost:8080/files/profile-picture/${application.companyLogoUrl}`}
            alt="Company Logo"
            className="object-contain w-full h-full"
          />
        </div>
        <div>
          <h1 className="text-2xl font-bold text-gray-800">{application.jobTitle}</h1>
          <p className="text-gray-600 text-sm">{application.companyName} • {application.location}</p>
        </div>
      </div>

      {/* Status Badges */}
      <div className="flex items-center gap-3 mb-6">
        <span className="px-3 py-1 text-xs font-medium rounded-full bg-gray-100 text-gray-500 border border-gray-300">
          Applied
        </span>
        <span
          className={`px-3 py-1 text-xs font-semibold uppercase rounded-full border ${getStatusStyle(application.status)}`}
        >
          {application.status}
        </span>
      </div>

      <hr className="my-4" />

      {/* Job Details */}
      <div className="space-y-2 text-sm text-gray-800 leading-relaxed">
        <div><strong>🕓 Applied At:</strong> {formatDate(application.appliedAt)}</div>
        <div><strong>📋 Job Type:</strong> {application.jobType.replace("_", " ")}</div>
        <div><strong>💼 Work Mode:</strong> {application.workMode.replace("_", " ")}</div>
        <div><strong>📍 Location:</strong> {application.location}</div>
        <div><strong>📝 Description:</strong> {application.jobDescription}</div>
      </div>

      <hr className="my-4" />

      {/* Applicant Details */}
      <div className="space-y-2 text-sm text-gray-800 leading-relaxed">
        <div><strong>🙍 Applicant:</strong> {application.username}</div>
        <div>
          <strong>📄 Resume:</strong>{" "}
          {application.resumeUrl ? (
            <button
              onClick={handleResumeClick}
              className="text-blue-600 underline hover:text-blue-800"
            >
              View Resume
            </button>
          ) : (
            <span className="text-gray-500">Not available</span>
          )}
        </div>
        <div><strong>🆔 Application ID:</strong> {application.applicationId}</div>
      </div>
    </div>
  );
}

ApplicationDetailsCard.propTypes = {
  application: PropTypes.shape({
    applicationId: PropTypes.number,
    username: PropTypes.string,
    resumeUrl: PropTypes.string,
    status: PropTypes.string,
    appliedAt: PropTypes.string,
    jobTitle: PropTypes.string,
    jobDescription: PropTypes.string,
    jobType: PropTypes.string,
    workMode: PropTypes.string,
    location: PropTypes.string,
    companyName: PropTypes.string,
    companyLogoUrl: PropTypes.string,
  }),
};
