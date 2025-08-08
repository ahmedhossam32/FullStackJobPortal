// src/components/RecentApplicantCard.jsx
export default function RecentApplicantCard({ applicant }) {
  const statusStyles = {
    Pending: "bg-yellow-100 text-yellow-800",
    Accepted: "bg-green-100 text-green-800",
    Rejected: "bg-red-100 text-red-700",
  };

  return (
    <div className="p-4 border-b last:border-none flex justify-between items-center">
      <div>
        <div className="font-medium">{applicant.name}</div>
        <div className="text-sm text-gray-500">Applied for {applicant.job}</div>
      </div>
      <div
        className={`text-sm px-2 py-1 rounded font-medium ${
          statusStyles[applicant.status] || "bg-gray-200 text-gray-700"
        }`}
      >
        {applicant.status}
      </div>
    </div>
  );
}
