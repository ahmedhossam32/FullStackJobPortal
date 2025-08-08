import { useEffect, useState } from "react";
import axios from "axios";
import { toast } from "react-toastify";

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

  const [profilePicFile, setProfilePicFile] = useState(null);
  const [resumeFile, setResumeFile] = useState(null);
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
    if (type === "profilePic") setProfilePicFile(file);
    else setResumeFile(file);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
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

      if (profilePicFile) {
        const picForm = new FormData();
        picForm.append("file", profilePicFile);
        await axios.post("http://localhost:8080/user/upload-profile-picture", picForm, {
          headers: { Authorization: `Bearer ${token}` },
        });
      }

      if (resumeFile) {
        const resumeForm = new FormData();
        resumeForm.append("file", resumeFile);
        await axios.post("http://localhost:8080/user/jobseeker/upload-resume", resumeForm, {
          headers: { Authorization: `Bearer ${token}` },
        });
      }

      toast.success("Profile updated successfully");
    } catch (err) {
      console.error("Error updating profile:", err);
      toast.error("Failed to update profile.");
    }
  };

  const handleResumePreview = async () => {
    try {
      const res = await axios.get(
        `http://localhost:8080/files/resume/${formData.resume}`,
        {
          headers: { Authorization: `Bearer ${token}` },
          responseType: "blob",
        }
      );
      const fileURL = URL.createObjectURL(new Blob([res.data], { type: "application/pdf" }));
      window.open(fileURL);
    } catch (error) {
      toast.error("Failed to preview resume");
      console.error("Resume preview error:", error);
    }
  };

  return (
    <div className="max-w-4xl mx-auto mt-10 px-4">
      <h1 className="text-3xl font-bold mb-8">My Profile</h1>

      <form onSubmit={handleSubmit} className="flex flex-col md:flex-row gap-10 items-start">
        {/* Profile Picture */}
        <div className="relative w-40 h-40 rounded-full overflow-hidden border border-gray-300 shadow-sm">
          {formData.profilePicture ? (
            <img
              src={`http://localhost:8080/files/profile-picture/${formData.profilePicture}`}
              alt="Profile"
              className="w-full h-full object-cover"
            />
          ) : (
            <div className="w-full h-full bg-gray-100 flex items-center justify-center text-gray-500 text-sm">
              No Image
            </div>
          )}
          <label className="absolute bottom-0 left-0 right-0 bg-black bg-opacity-70 text-white text-center text-sm cursor-pointer py-1">
            Edit
            <input
              type="file"
              accept="image/*"
              onChange={(e) => handleFileChange(e, "profilePic")}
              className="hidden"
            />
          </label>
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
              className="w-full border border-gray-300 px-3 py-2 rounded-md"
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
              className="w-full border border-gray-300 px-3 py-2 rounded-md"
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
              className="w-full border border-gray-300 px-3 py-2 rounded-md"
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
              className="w-full border border-gray-300 px-3 py-2 rounded-md"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">Resume (PDF)</label>
            <input
              type="file"
              accept=".pdf"
              onChange={(e) => handleFileChange(e, "resume")}
              className="text-sm"
            />
            {formData.resumeOriginalName && (
              <div className="text-xs text-gray-600 mt-1">
                <p className="mb-1">
                  <span className="font-medium text-gray-800">Current:</span> {formData.resumeOriginalName}
                </p>
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
            className="bg-[#6B3F27] hover:bg-[#5c3421] text-white px-6 py-2 rounded-md font-semibold mt-4"
          >
            Save Changes
          </button>
        </div>
      </form>
    </div>
  );
}
