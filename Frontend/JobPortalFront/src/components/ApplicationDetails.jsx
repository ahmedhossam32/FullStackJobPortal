import { useState } from "react";
import { toast } from "react-toastify";
import { useNavigate } from "react-router-dom";

export default function ApplicationDetails({ jobId, screeningAnswers = [] }) {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem("user"));
  const token = localStorage.getItem("token");

  const [fullName] = useState(user?.name || "");
  const [email] = useState(user?.email || "");
  const [phone, setPhone] = useState("");
  const [existingResume] = useState(user?.resume || null);
  const [selectedResume, setSelectedResume] = useState(existingResume);
  const [uploadedFile, setUploadedFile] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const uploadResume = async () => {
    const formData = new FormData();
    formData.append("file", uploadedFile);

    const res = await fetch("http://localhost:8080/user/jobseeker/upload-resume", {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
      },
      body: formData,
    });

    if (!res.ok) throw new Error("Resume upload failed");
    return await res.text(); // return uploaded filename
  };

  const handleSubmit = async () => {
    try {
      setSubmitting(true);
      let resumeUrl = selectedResume;

      if (uploadedFile) {
        resumeUrl = await uploadResume();
      }

      const response = await fetch("http://localhost:8080/applications", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          jobId,
          resumeUrl,
          screeningAnswers,
        }),
      });

      if (!response.ok) throw new Error("Application failed");

      const data = await response.json();
      const applicationId = data.applicationId;

      toast.success(
  <div>
    <p className="text-[#256029] font-semibold">✅ Application submitted!</p>
    <button
      onClick={() => navigate(`/applications/${applicationId}`)}
      className="mt-1 text-blue-600 underline hover:text-blue-800 text-sm"
    >
      Show Application Details
    </button>
  </div>,
  {
    autoClose: false,
    closeOnClick: false,
    pauseOnHover: true,
  }
);

    } catch (err) {
      console.error("❌ Application Error:", err);
      toast.error("Failed to submit application.");
    } finally {
      setSubmitting(false);
    }
  };

  const previewResume = async () => {
    try {
      const res = await fetch(`http://localhost:8080/files/resume/${existingResume}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!res.ok) throw new Error("Failed to fetch resume");

      const blob = await res.blob();
      const url = window.URL.createObjectURL(blob);
      window.open(url, "_blank");
    } catch (err) {
      console.error("Preview failed:", err);
      toast.error("⚠️ Failed to preview resume.");
    }
  };

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <label className="block text-sm font-medium text-gray-800">Full Name</label>
        <input
          type="text"
          className="w-full border border-gray-300 px-3 py-2 rounded-md outline-none focus:ring-2 focus:ring-[#6B3F27]/30 focus:border-[#6B3F27] transition"
          value={fullName}
          disabled
        />
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-800">Email</label>
        <input
          type="email"
          className="w-full border border-gray-300 px-3 py-2 rounded-md outline-none focus:ring-2 focus:ring-[#6B3F27]/30 focus:border-[#6B3F27] transition"
          value={email}
          disabled
        />
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-800">Phone Number</label>
        <input
          type="tel"
          value={phone}
          onChange={(e) => setPhone(e.target.value)}
          placeholder="e.g. +20 123456789"
          required
          className="w-full border border-gray-300 px-3 py-2 rounded-md outline-none focus:ring-2 focus:ring-[#6B3F27]/30 focus:border-[#6B3F27] transition"
        />
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-800 mb-2">Resume</label>

        {existingResume ? (
          <div className="space-y-2">
            <label className="flex items-center gap-2 cursor-pointer text-sm">
              <input
                type="radio"
                name="resume"
                checked={selectedResume === existingResume}
                onChange={() => {
                  setUploadedFile(null);
                  setSelectedResume(existingResume);
                }}
              />
              <span className="text-gray-700">{existingResume.split("_").slice(1).join("_")}</span>
              <button
                type="button"
                onClick={previewResume}
                className="text-blue-600 underline hover:text-blue-800"
              >
                Preview
              </button>
            </label>

            <div className="border-t pt-2 text-sm text-gray-600">
              <p className="mb-1">Or upload a new resume:</p>
              <label
                htmlFor="resume-upload"
                className="inline-block bg-white border border-gray-300 px-4 py-1 rounded cursor-pointer text-[#6B3F27] hover:bg-[#f8f4f2] font-medium"
              >
                Choose File
                <input
                  id="resume-upload"
                  type="file"
                  accept=".pdf"
                  className="hidden"
                  onChange={(e) => {
                    setUploadedFile(e.target.files[0]);
                    setSelectedResume(null);
                  }}
                />
              </label>

              {uploadedFile && (
                <div className="mt-1 text-green-700 text-sm font-medium">
                  ✅ Selected: {uploadedFile.name}
                </div>
              )}
            </div>
          </div>
        ) : (
          <input
            type="file"
            accept=".pdf"
            onChange={(e) => setUploadedFile(e.target.files[0])}
            className="w-full text-sm"
          />
        )}
      </div>

      <button
        onClick={handleSubmit}
        disabled={submitting}
        className={`w-full sm:w-fit px-6 py-2 rounded-md text-white font-medium transition-all ${
          submitting
            ? "bg-[#6B3F27]/60 cursor-not-allowed"
            : "bg-[#6B3F27] hover:bg-[#5C3421]"
        }`}
      >
        {submitting ? "Submitting..." : "Submit Application"}
      </button>
    </div>
  );
}
