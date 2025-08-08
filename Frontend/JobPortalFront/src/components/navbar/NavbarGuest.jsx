import { useNavigate } from "react-router-dom";

export default function NavbarGuest({ onOpenJobSeeker, onOpenEmployer }) {
  const navigate = useNavigate();

  return (
    <nav className="w-full px-6 py-4 flex justify-between items-center bg-white shadow-sm border-b sticky top-0 z-50">
      {/* LEFT */}
      <div className="flex items-center gap-6 text-sm font-medium text-gray-800">
        <span className="text-xl font-bold text-black cursor-pointer" onClick={() => navigate("/")}>
          JobPort
        </span>
        <span className="hover:text-black cursor-pointer" onClick={() => alert("Jobs page not yet implemented")}>Jobs</span>
        <span className="hover:text-black cursor-pointer" onClick={() => alert("Companies page not yet implemented")}>Companies</span>
        <span className="hover:text-black cursor-pointer" onClick={onOpenEmployer}>For Employers</span>
        <span className="hover:text-black cursor-pointer" onClick={onOpenJobSeeker}>For Professionals</span>
      </div>

      {/* RIGHT - Dropdown buttons */}
      <div className="flex items-center gap-3 text-gray-700 relative">
        {/* Login Dropdown */}
        <div className="relative group">
          <button className="px-4 py-1.5 border rounded hover:bg-gray-100 text-sm">
            Login
          </button>
          <div className="absolute hidden group-hover:flex flex-col bg-white border shadow-md right-0 top-[110%] z-20 min-w-[140px] rounded">
            <button
              onClick={onOpenJobSeeker}
              className="px-4 py-2 hover:bg-gray-100 text-left text-sm"
            >
              As Job Seeker
            </button>
            <button
              onClick={onOpenEmployer}
              className="px-4 py-2 hover:bg-gray-100 text-left text-sm"
            >
              As Employer
            </button>
          </div>
        </div>

        {/* Sign Up Dropdown */}
        <div className="relative group">
          <button className="px-4 py-1.5 bg-black text-white rounded hover:opacity-90 text-sm">
            Sign Up
          </button>
          <div className="absolute hidden group-hover:flex flex-col bg-white border shadow-md right-0 top-[110%] z-20 min-w-[140px] rounded">
            <button
              onClick={onOpenJobSeeker}
              className="px-4 py-2 hover:bg-gray-100 text-left text-sm"
            >
              As Job Seeker
            </button>
            <button
              onClick={onOpenEmployer}
              className="px-4 py-2 hover:bg-gray-100 text-left text-sm"
            >
              As Employer
            </button>
          </div>
        </div>
      </div>
    </nav>
  );
}
