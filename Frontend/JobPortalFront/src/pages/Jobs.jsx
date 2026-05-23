import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import JobDetails from "../components/JobDetails";
import JobSearchBar from "../components/JobSearchBar";
import PaginatedJobList from "../components/PaginatedJobList";
import API_URL from "../api/config";

export default function Jobs() {
  const [searchParams] = useSearchParams();
  const [jobs, setJobs] = useState([]);
  const [selectedJob, setSelectedJob] = useState(null);
  const [error, setError] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const jobId = searchParams.get("jobId");
    fetchAllJobs(0, 10, jobId);
  }, []);

  const fetchAllJobs = async (page = 0, size = 10, targetJobId = null) => {
    setLoading(true);
    try {
      const res = await fetch(
        `${API_URL}/jobs?page=${page}&size=${size}`,
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
      setCurrentPage(data.currentPage);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);

      if (targetJobId) {
        const found = data.content.find((j) => String(j.id) === String(targetJobId));
        if (found) {
          setSelectedJob(found);
        } else {
          const jobRes = await fetch(`${API_URL}/jobs/${targetJobId}`, {
            headers: { "Content-Type": "application/json" },
          });
          if (jobRes.ok) {
            setSelectedJob(await jobRes.json());
          } else {
            setSelectedJob(data.content[0] || null);
          }
        }
      } else {
        setSelectedJob(data.content[0] || null);
      }
    } catch (err) {
      console.error("🔥 Error:", err.message);
      setError("Something went wrong.");
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async ({ title, location, type }) => {
    if (!title.trim() && !location.trim() && !type.trim()) {
      fetchAllJobs();
      return;
    }

    setLoading(true);
    try {
      let titleResults = null;
      let locationResults = null;
      let typeResults = null;

      if (title.trim()) {
        const res = await fetch(
          `${API_URL}/jobs/search/title?keyword=${encodeURIComponent(title)}&page=0&size=10`,
          { headers: { "Content-Type": "application/json" } }
        );
        if (res.ok) {
          const data = await res.json();
          titleResults = data.content;
        }
      }

      if (location.trim()) {
        const res = await fetch(
          `${API_URL}/jobs/search/location?location=${encodeURIComponent(location)}&page=0&size=10`,
          { headers: { "Content-Type": "application/json" } }
        );
        if (res.ok) {
          const data = await res.json();
          locationResults = data.content;
        }
      }

      if (type.trim()) {
        const res = await fetch(
          `${API_URL}/jobs/search/type?type=${encodeURIComponent(type)}&page=0&size=10`,
          { headers: { "Content-Type": "application/json" } }
        );
        if (res.ok) {
          const data = await res.json();
          typeResults = data.content;
        }
      }

      const allResultSets = [titleResults, locationResults, typeResults].filter((r) => r !== null);
      const idSets = allResultSets.map((r) => new Set(r.map((j) => j.id)));
      const finalResults = allResultSets[0].filter((j) => idSets.every((s) => s.has(j.id)));

      setJobs(finalResults);
      setSelectedJob(finalResults[0] || null);
    } catch (err) {
      console.error("🔴 Search error:", err.message);
      setError("Search failed.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="px-4 py-4 md:p-6">
      <JobSearchBar onSearch={handleSearch} />

      {error && <div className="text-red-600 font-semibold mb-4">{error}</div>}

      <h2 className="text-2xl font-bold mb-4">Jobs For You</h2>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-4">
        <div className="md:col-span-1">
          {loading ? (
            <div className="flex justify-center items-center py-4">
              <div className="w-6 h-6 border-2 border-[#6B3F27] border-t-transparent rounded-full animate-spin"></div>
            </div>
          ) : (
            <>
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
            </>
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
