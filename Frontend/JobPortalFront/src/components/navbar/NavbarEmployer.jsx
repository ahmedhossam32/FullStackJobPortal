import { useContext, useEffect } from "react";
import { AuthContext } from "../../context/AuthContext";
import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import defaultAvatar from "../../pics/deafult-avatar.png";
import { FaBell } from "react-icons/fa";

export default function NavbarEmployer() {
  const { user, logout } = useContext(AuthContext);
  const navigate = useNavigate();

  useEffect(() => {
    console.log("👔 Employer user:", user);
  }, [user]);

  const handleLogout = () => {
    logout();
    toast.success("Logged out successfully!");
    navigate("/");
  };

  const profileImageUrl = user?.profilePicture
    ? `http://localhost:8080/files/profile-picture/${user.profilePicture}`
    : defaultAvatar;

  return (
    <nav className="w-full px-6 py-4 flex justify-between items-center bg-white shadow-sm border-b sticky top-0 z-50">
      {/* LEFT - Logo */}
      <div className="flex items-center gap-6 text-sm font-medium text-gray-800">
        <span className="text-xl font-bold text-black cursor-pointer" onClick={() => navigate("/dashboard")}>
          JobPort
        </span>
      </div>

      {/* CENTER - Greeting + Navigation */}
      <div className="hidden md:flex gap-6 text-sm font-medium text-gray-800 items-center">
        <span>Welcome back, <strong>{user.companyName}</strong></span>
        <span className="cursor-pointer hover:text-black" onClick={() => navigate("/dashboard")}>Dashboard</span>
        <span className="cursor-pointer hover:text-black" onClick={() => navigate("/employer/jobs")}>My Jobs</span>
        <span className="cursor-pointer hover:text-black" onClick={() => navigate("/create-job")}>Post Job</span>
      </div>

      {/* RIGHT - Bell, Avatar, Logout */}
      <div className="flex items-center gap-3 text-gray-700">
        <FaBell className="cursor-pointer text-lg hover:text-black" title="Notifications" />
       <img
  src={profileImageUrl}
  alt="Profile"
  className="w-10 h-8 rounded-full border object-contain bg-white p-0.5"
  title="Company Profile"
  onClick={() => navigate("/employer/profile")}
  onError={(e) => {
    e.target.onerror = null;
    e.target.src = defaultAvatar;
  }}
/>
        <button onClick={handleLogout} className="px-4 py-1.5 border rounded hover:bg-gray-100 text-sm">
          Logout
        </button>
      </div>
    </nav>
  );
}
