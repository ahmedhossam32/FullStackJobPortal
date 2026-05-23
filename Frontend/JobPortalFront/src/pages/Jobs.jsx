import { useEffect, useState } from "react";
import JobDetails from "../components/JobDetails";
import JobSearchBar from "../components/JobSearchBar";
import PaginatedJobList from "../components/PaginatedJobList";

export default function Jobs() {
  const [jobs, setJobs] = useState([]);
  const [selectedJob, setSelectedJob] = useState(null);
  const [error, setError] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  useEffect(() => {
    fetchAllJobs();
  }, []);

  const fetchAllJobs = async (page = 0, size = 10) => {
    try {
      const res = await fetch(
        `http://localhost:8080/jobs?page=${page}&size=${size}`,
        { headers: { "Content-Type": "application/json" } }
      );

      if (!res.ok) {
        const errorText = await res.text();
        console.error("❌ Failed to fetch jobs:", res.status, errorText);
        setError("Failed to fetch jobs.");
        return;
      }

      const data = await res.json();
      setJobs(data.content);
      setSelectedJob(data.content[0] || null);
      setCurrentPage(data.currentPage);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
    } catch (err) {
      console.error("🔥 Error:", err.message);
      setError("Something went wrong.");
    }
  };

  const handleSearch = async ({ title, type }) => {
    try {
      let titleResults = [];
      let typeResults = [];

      if (title.trim()) {
        const res = await fetch(
          `http://localhost:8080/jobs/search/title?keyword=${encodeURIComponent(title)}&page=0&size=10`,
          { headers: { "Content-Type": "application/json" } }
        );
        if (res.ok) {
          const data = await res.json();
          titleResults = data.content;
        }
      }

      if (type.trim()) {
        const res = await fetch(
          `http://localhost:8080/jobs/search/type?type=${encodeURIComponent(type)}&page=0&size=10`,
          { headers: { "Content-Type": "application/json" } }
        );
        if (res.ok) {
          const data = await res.json();
          typeResults = data.content;
        }
      }

      let finalResults = [];

      if (title.trim() && type.trim()) {
        const titleIds = new Set(titleResults.map((job) => job.id));
        finalResults = typeResults.filter((job) => titleIds.has(job.id));
      } else if (title.trim()) {
        finalResults = titleResults;
      } else if (type.trim()) {
        finalResults = typeResults;
      } else {
        fetchAllJobs();
        return;
      }

      setJobs(finalResults);
      setSelectedJob(finalResults[0] || null);
    } catch (err) {
      console.error("🔴 Search error:", err.message);
      setError("Search failed.");
    }
  };

  return (
    <div className="px-4 py-4 md:p-6">
      <JobSearchBar onSearch={handleSearch} />

      {error && <div className="text-red-600 font-semibold mb-4">{error}</div>}

      <h2 className="text-2xl font-bold mb-4">Jobs For You</h2>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-4">
        <div className="md:col-span-1">
          <PaginatedJobList
            jobs={jobs}
            selectedJob={selectedJob}
            onSelect={setSelectedJob}
          />

          {totalPages > 1 && (
            <div className="flex items-center justify-between mt-4">
              <button
                onClick={() => fetchAllJobs(currentPage - 1)}
                disabled={currentPage === 0}
                className="px-4 py-2 min-h-[44px] rounded text-sm font-medium bg-[#5D3A00] text-white disabled:opacity-40 disabled:cursor-not-allowed"
              >
                Previous
              </button>
              <span className="text-sm text-gray-600">
                Page {currentPage + 1} of {totalPages}
              </span>
              <button
                onClick={() => fetchAllJobs(currentPage + 1)}
                disabled={currentPage === totalPages - 1}
                className="px-4 py-2 min-h-[44px] rounded text-sm font-medium bg-[#5D3A00] text-white disabled:opacity-40 disabled:cursor-not-allowed"
              >
                Next
              </button>
            </div>
          )}
        </div>

        <div className="md:col-span-2">
          <div className="md:sticky md:top-24">
            {selectedJob ? (
              <JobDetails job={selectedJob} />
            ) : (
              <p className="text-gray-500">Select a job to see details</p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
