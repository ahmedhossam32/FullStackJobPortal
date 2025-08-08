import React from 'react';
import { FaMapMarkerAlt, FaBriefcase } from 'react-icons/fa';
import { MdWork } from 'react-icons/md';

export default function JobCard({ job, isSelected, onSelect }) {
  return (
    <div
      onClick={() => onSelect(job)}
      className={`flex items-center gap-4 p-4 mb-4 cursor-pointer rounded-xl border transition duration-200 transform ${
        isSelected ? 'border-black shadow-md' : 'border-gray-300'
      } hover:shadow-lg hover:scale-[1.01] bg-white backdrop-blur-sm`}
    >
      {/* Logo */}
      <div className="w-16 h-16 flex-shrink-0 rounded-full overflow-hidden border border-gray-200 bg-gray-50 shadow-sm flex items-center justify-center">
        <img
          src={
            job.profilePicture
              ? `http://localhost:8080/files/profile-picture/${job.profilePicture}`
              : '/default-logo.png'
          }
          alt={job.companyName}
          className="w-12 h-12 object-contain"
        />
      </div>

      {/* Job Info */}
      <div className="flex-1">
        <h2 className="text-lg font-bold text-gray-800">{job.title}</h2>

        <div className="text-sm text-gray-600 flex items-center mt-1">
          <FaBriefcase className="mr-2 text-brown-700" />
          <span>{job.companyName}</span>
        </div>

        <div className="text-sm text-gray-600 flex items-center mt-1">
          <FaMapMarkerAlt className="mr-2 text-brown-700" />
          <span>{job.location}</span>
        </div>

        <div className="text-xs text-gray-700 mt-2 flex items-center">
          <MdWork className="mr-2 text-brown-700" />
          <span className="uppercase tracking-wide">{job.type} | {job.workMode}</span>
        </div>
      </div>
    </div>
  );
}
