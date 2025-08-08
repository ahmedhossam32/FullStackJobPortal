// src/components/InterviewCard.jsx
import { FaBuilding, FaCalendarAlt, FaMapMarkerAlt, FaUserTie } from "react-icons/fa";

export default function InterviewCard({ interview }) {
  return (
    <div className="border-b py-4 px-2 hover:bg-gray-50 transition flex justify-between items-center">
      {/* Left */}
      <div className="flex items-start gap-4">
        <div className="w-12 h-12 bg-gray-200 rounded flex items-center justify-center text-gray-500 text-xl">
          🏢
        </div>
        <div>
          <h2 className="text-lg font-semibold">{interview.jobTitle}</h2>
          <p className="text-sm text-gray-600">{interview.company}</p>
          <div className="text-sm text-gray-600 flex items-center gap-2 mt-1">
            <FaCalendarAlt /> {interview.date} at {interview.time}
          </div>
          <div className="text-sm text-gray-600 flex items-center gap-2 mt-1">
            <FaMapMarkerAlt /> {interview.mode}
          </div>
          {interview.contact && (
            <div className="text-sm text-gray-600 flex items-center gap-2 mt-1">
              <FaUserTie /> {interview.contact}
            </div>
          )}
        </div>
      </div>

      {/* Right */}
      <div className="flex gap-3 items-center">
        {interview.link && (
          <a
            href={interview.link}
            target="_blank"
            rel="noopener noreferrer"
            className="text-sm bg-green-600 text-white px-4 py-1 rounded hover:bg-green-700"
          >
            Join Meeting
          </a>
        )}
        <button className="text-sm text-red-600 hover:underline">Cancel</button>
      </div>
    </div>
  );
}
