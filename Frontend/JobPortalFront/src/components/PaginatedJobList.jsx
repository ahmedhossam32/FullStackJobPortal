import { useState, useEffect } from "react";
import JobCard from "./JobCard";

export default function PaginatedJobList({ jobs, selectedJob, onSelect }) {
  const [currentPage, setCurrentPage] = useState(1);
  const jobsPerPage = 5;

  useEffect(() => {
    setCurrentPage(1);
  }, [jobs]);

  const indexOfLastJob = currentPage * jobsPerPage;
  const indexOfFirstJob = indexOfLastJob - jobsPerPage;
  const currentJobs = jobs.slice(indexOfFirstJob, indexOfLastJob);
  const totalPages = Math.ceil(jobs.length / jobsPerPage);

  return (
    <>
      {currentJobs.length === 0 ? (
        <p className="text-gray-600">No jobs found.</p>
      ) : (
        currentJobs.map((job) => (
          <JobCard
            key={job.id}
            job={job}
            isSelected={selectedJob?.id === job.id}
            onSelect={onSelect}
          />
        ))
      )}

      {jobs.length > jobsPerPage && (
        <div className="flex justify-between items-center mt-6">
          <button
            onClick={() => setCurrentPage((prev) => prev - 1)}
            disabled={currentPage === 1}
            className={`px-4 py-2 text-sm rounded transition-colors duration-200 ${
              currentPage === 1
                ? "bg-gray-300 text-gray-500 cursor-not-allowed"
                : "bg-[#5D3A00] text-white hover:bg-[#4b2f00]"
            }`}
          >
            Previous
          </button>

          <span className="text-gray-700 text-sm font-medium">
            Page {currentPage} of {totalPages}
          </span>

          <button
            onClick={() => setCurrentPage((prev) => prev + 1)}
            disabled={currentPage === totalPages}
            className={`px-4 py-2 text-sm rounded transition-colors duration-200 ${
              currentPage === totalPages
                ? "bg-gray-300 text-gray-500 cursor-not-allowed"
                : "bg-[#5D3A00] text-white hover:bg-[#4b2f00]"
            }`}
          >
            Next
          </button>
        </div>
      )}
    </>
  );
}
