import { useContext, useEffect } from 'react';
import { AuthContext } from '../../context/AuthContext';
import { FaBell, FaEnvelope, FaBookmark } from 'react-icons/fa';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import defaultAvatar from '../../pics/deafult-avatar.png';
import NotificationBell from "../NotificationBell";

export default function Navbar({ onOpenJobSeeker, onOpenEmployer }) {
  const { user, logout } = useContext(AuthContext);
  const navigate = useNavigate();

  useEffect(() => {
    console.log("🧠 Logged-in user from context:", user);
    if (user && !user.profilePicture) {
      console.warn("⚠️ User has no profilePicture — using default avatar");
    }
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

      {/* LEFT - Logo + Links */}
      <div className="flex items-center gap-6 text-sm font-medium text-gray-800">
        <span
          className="text-xl font-bold text-black cursor-pointer"
          onClick={() => navigate('/')}
        >
          JobPort
        </span>
        {user ? (
          <>
            <span className="cursor-pointer hover:text-[#6F4E37] transition" onClick={() => navigate('/')}>Home</span>
            <span className="cursor-pointer hover:text-[#6F4E37] transition" onClick={() => navigate('/jobs')}>Jobs</span>
          </>
        ) : (
          <>
            <span className="cursor-pointer hover:text-[#6F4E37] transition" onClick={() => alert("Jobs page not yet implemented")}>Jobs</span>
            <span className="cursor-pointer hover:text-[#6F4E37] transition" onClick={() => alert("Companies page not yet implemented")}>Companies</span>
            <span className="cursor-pointer hover:text-[#6F4E37] transition" onClick={onOpenEmployer}>For Employers</span>
            <span className="cursor-pointer hover:text-[#6F4E37] transition" onClick={onOpenJobSeeker}>For Professionals</span>
          </>
        )}
      </div>

      {/* CENTER - Welcome Message */}
      {user && (
        <div className="hidden md:flex text-sm font-medium text-gray-700">
          Welcome back, <span className="ml-1 font-semibold">{user.name}</span>
        </div>
      )}

      {/* RIGHT - Icons, Profile, Logout */}
      <div className="flex items-center gap-3 text-gray-700">
        {user ? (
          <>
            <FaBookmark
              className="cursor-pointer text-lg hover:text-[#6F4E37] transition"
              title="My Jobs"
              onClick={() => navigate("/myjobs")}
            />
            <FaEnvelope
              className="cursor-pointer text-lg hover:text-[#6F4E37] transition"
              title="Messages"
            />

            <NotificationBell />

            <img
              src={profileImageUrl}
              alt="Profile"
              className="w-8 h-8 rounded-full border object-cover cursor-pointer"
              title="Profile"
              onClick={() => navigate("/profile")}
              onError={(e) => {
                console.warn("❌ Failed to load profile image, falling back to default.");
                e.target.onerror = null;
                e.target.src = defaultAvatar;
              }}
            />

            <button
              onClick={handleLogout}
              className="px-4 py-1.5 border border-[#6F4E37] text-[#6F4E37] rounded hover:bg-[#6F4E37] hover:text-white transition text-sm"
            >
              Logout
            </button>
          </>
        ) : (
          <>
            <button
              onClick={onOpenJobSeeker}
              className="px-4 py-1.5 border rounded hover:bg-gray-100 text-sm"
            >
              Login
            </button>
            <button
              onClick={onOpenJobSeeker}
              className="px-4 py-1.5 bg-black text-white rounded hover:opacity-90 text-sm"
            >
              Sign Up
            </button>
          </>
        )}
      </div>
    </nav>
  );
}
