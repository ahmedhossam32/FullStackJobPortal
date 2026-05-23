import { useContext, useEffect, useState } from "react";
import axios from "axios";
import { toast } from "react-toastify";
import { AuthContext } from "../context/AuthContext";

export default function JobSeekerProfilePage() {
  const [formData, setFormData] = useState({
    name: "",
    username: "",
    email: "",
    dob: "",
    profilePicture: "",
    resume: "",
    resumeOriginalName: "",
  });

  const { updateUser } = useContext(AuthContext);
  const [profilePicFile, setProfilePicFile] = useState(null);
  const [resumeFile, setResumeFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [loading, setLoading] = useState(false);
  const token = localStorage.getItem("token");

  useEffect(() => {
    axios
      .get("http://localhost:8080/user/me", {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((res) => {
        const data = res.data;
        setFormData({
          name: data.name || "",
          username: data.username || "",
          email: data.email || "",
          dob: data.dob || "",
          profilePicture: data.profilePicture || "",
          resume: data.resume || "",
          resumeOriginalName: data.resumeOriginalName || "",
        });
      })
      .catch((err) => {
        console.error("Failed to fetch profile:", err);
        toast.error("Failed to load profile info.");
      });
  }, [token]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleFileChange = (e, type) => {
    const file = e.target.files[0];
    if (type === "profilePic") {
      setProfilePicFile(file);
      const objectUrl = URL.createObjectURL(file);
      setPreviewUrl(objectUrl);
    } else {
      setResumeFile(file);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    let hasError = false;
    setLoading(true);

    try {
      await axios.put(
        "http://localhost:8080/user/jobseeker/update-profile",
        {
          name: formData.name,
          username: formData.username,
          email: formData.email,
          dob: formData.dob,
        },
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
    } catch (err) {
      console.error("Error updating profile info:", err);
      toast.error(err.response?.data?.message || "Failed to update profile.");
      hasError = true;
    }

    if (profilePicFile) {
      try {
        const picForm = new FormData();
        picForm.append("file", profilePicFile);
        await axios.post("http://localhost:8080/user/upload-profile-picture", picForm, {
          headers: { Authorization: `Bearer ${token}` },
        });
        const meRes = await axios.get("http://localhost:8080/user/me", {
          headers: { Authorization: `Bearer ${token}` },
        });
        updateUser(meRes.data);
      } catch (err) {
        console.error("Error uploading profile picture:", err);
        toast.error(err.response?.data?.message || "Failed to upload profile picture.");
        hasError = true;
      }
    }

    if (resumeFile) {
      try {
        const resumeForm = new FormData();
        resumeForm.append("file", resumeFile);
        await axios.post("http://localhost:8080/user/jobseeker/upload-resume", resumeForm, {
          headers: { Authorization: `Bearer ${token}` },
        });
      } catch (err) {
        console.error("Error uploading resume:", err);
        toast.error(err.response?.data?.message || "Failed to upload resume.");
        hasError = true;
      }
    }

    if (!hasError) toast.success("Profile updated successfully");
    setLoading(false);
  };

  const handleResumePreview = () => {
    window.open(formData.resume, "_blank");
  };

  return (
    <div className="max-w-4xl mx-auto mt-8 px-4 pb-10">
      <h1 className="text-2xl md:text-3xl font-bold mb-6 md:mb-8">My Profile</h1>

      <form onSubmit={handleSubmit} className="flex flex-col md:flex-row gap-8 md:gap-10 md:items-start">
        {/* Profile Picture */}
        <div className="flex justify-center md:justify-start md:flex-shrink-0">
          <div className="relative w-36 h-36 md:w-40 md:h-40 rounded-full overflow-hidden border border-gray-300 shadow-sm">
            {(previewUrl || formData.profilePicture) ? (
              <img
                src={previewUrl || formData.profilePicture || "/default-avatar.png"}
                alt="Profile"
                className="w-full h-full object-cover"
              />
            ) : (
              <div className="w-full h-full bg-gray-100 flex items-center justify-center text-gray-500 text-sm">
                No Image
              </div>
            )}
            <label className="absolute bottom-0 left-0 right-0 bg-black bg-opacity-70 text-white text-center text-sm cursor-pointer py-1.5">
              Edit
              <input
                type="file"
                accept="image/*"
                onChange={(e) => handleFileChange(e, "profilePic")}
                className="hidden"
              />
            </label>
          </div>
        </div>

        {/* Form Fields */}
        <div className="flex-1 space-y-5">
          <div>
            <label className="block text-sm font-medium mb-1">Full Name</label>
            <input
              type="text"
              name="name"
              value={formData.name}
              onChange={handleChange}
              className="w-full border border-gray-300 px-3 py-2.5 rounded-md text-base"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">Username</label>
            <input
              type="text"
              name="username"
              value={formData.username}
              onChange={handleChange}
              className="w-full border border-gray-300 px-3 py-2.5 rounded-md text-base"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">Email</label>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              className="w-full border border-gray-300 px-3 py-2.5 rounded-md text-base"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">Date of Birth</label>
            <input
              type="date"
              name="dob"
              value={formData.dob}
              onChange={handleChange}
              className="w-full border border-gray-300 px-3 py-2.5 rounded-md text-base"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">Resume (PDF)</label>
            <input
              type="file"
              accept=".pdf"
              onChange={(e) => handleFileChange(e, "resume")}
              className="w-full border border-gray-300 px-3 py-2.5 rounded-md bg-gray-50 text-sm"
            />
            {formData.resume && (
              <div className="text-xs text-gray-600 mt-2">
                {formData.resumeOriginalName && (
                  <p className="mb-1">
                    <span className="font-medium text-gray-800">Current:</span> {formData.resumeOriginalName}
                  </p>
                )}
                <button
                  type="button"
                  onClick={handleResumePreview}
                  className="text-green-600 hover:underline"
                >
                  Preview Resume
                </button>
              </div>
            )}
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full md:w-auto bg-[#6B3F27] hover:bg-[#5c3421] text-white px-6 py-3 min-h-[44px] rounded-md font-semibold mt-2 disabled:opacity-60 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            {loading && (
              <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
            )}
            {loading ? "Saving..." : "Save Changes"}
          </button>
        </div>
      </form>
    </div>
  );
}
