// src/pages/ApplyPage.jsx
import { useParams } from 'react-router-dom';
import { useState, useEffect } from 'react';
import ScreeningQuestions from '../components/ScreeningQuestions';
import ApplicationDetails from '../components/ApplicationDetails';
import JobDetails from '../components/JobDetails';

export default function ApplyPage() {
  const { jobId } = useParams();
  const [job, setJob] = useState(null);
  const [step, setStep] = useState(1);
  const [screeningAnswers, setScreeningAnswers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchJob = async () => {
      try {
        const token = localStorage.getItem("token");
        if (!token) throw new Error("No token found");

        const res = await fetch(`http://localhost:8080/jobs/${jobId}`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        if (!res.ok) throw new Error("Failed to fetch job");
        const data = await res.json();
        setJob(data);

        if (!data.screeningQuestions || data.screeningQuestions.length === 0) {
          setStep(2);
        }
      } catch (err) {
        console.error("Error loading job:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchJob();
  }, [jobId]);

  const handleNextStep = (answers) => {
    setScreeningAnswers(answers);
    setStep(2);
  };

  if (loading) return <div className="p-8 text-gray-500">Loading job...</div>;
  if (!job) return <div className="p-8 text-red-500">Job not found.</div>;

  return (
    <div className="min-h-screen bg-white px-6 md:px-10 py-10 flex flex-col md:flex-row gap-12 animate-fade-in">
      <div className="md:w-[42%] w-full">
        <h2 className="text-2xl font-semibold mb-6 leading-relaxed">
          Apply for{' '}
          <span className="text-[#6B3F27]">{job.title}</span> at{' '}
          <span className="text-[#6B3F27]">{job.companyName}</span>
        </h2>

        {step === 1 && (
          <ScreeningQuestions
            questions={job.screeningQuestions || []}
            onNext={handleNextStep}
          />
        )}

        {step === 2 && (
          <ApplicationDetails
            jobId={job.id}
            screeningAnswers={screeningAnswers}
          />
        )}
      </div>

      <div className="md:w-[58%] w-full">
        <JobDetails job={job} />
      </div>
    </div>
  );
}
