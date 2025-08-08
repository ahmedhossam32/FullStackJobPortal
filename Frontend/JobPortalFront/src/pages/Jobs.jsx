import { useEffect, useState } from "react";
import JobDetails from "../components/JobDetails";
import JobSearchBar from "../components/JobSearchBar";
import PaginatedJobList from "../components/PaginatedJobList";

export default function Jobs() {
  const [jobs, setJobs] = useState([]);
  const [selectedJob, setSelectedJob] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (token) {
      fetchAllJobs(token);
    } else {
      setError("You must be signed in to view jobs.");
    }
  }, []);

  const fetchAllJobs = async (token) => {
    try {
      const res = await fetch("http://localhost:8080/jobs", {
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });

      if (!res.ok) {
        const errorText = await res.text();
        console.error("❌ Failed to fetch jobs:", res.status, errorText);
        setError("Failed to fetch jobs.");
        return;
      }

      const data = await res.json();
      setJobs(data);
      setSelectedJob(data[0] || null);
    } catch (err) {
      console.error("🔥 Error:", err.message);
      setError("Something went wrong.");
    }
  };

  const handleSearch = async ({ title, type }) => {
    const token = localStorage.getItem("token");
    if (!token) {
      setError("You must be signed in to search.");
      return;
    }

    try {
      let titleResults = [];
      let typeResults = [];

      if (title.trim()) {
        const res = await fetch(`http://localhost:8080/jobs/search/title?keyword=${encodeURIComponent(title)}`, {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
        });
        if (res.ok) {
          titleResults = await res.json();
        }
      }

      if (type.trim()) {
        const res = await fetch(`http://localhost:8080/jobs/search/type?type=${encodeURIComponent(type)}`, {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
        });
        if (res.ok) {
          typeResults = await res.json();
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
        fetchAllJobs(token); // fallback if no input
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
    <div className="p-6">
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
        </div>

        <div className="md:col-span-2">
          <div className="sticky top-24">
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
