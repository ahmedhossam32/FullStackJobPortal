import { useEffect, useState, useRef } from "react";
import { FaBell } from "react-icons/fa";
import axios from "axios";
import { formatDistanceToNow } from "date-fns";

export default function NotificationBell() {
  const [notifications, setNotifications] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);
  const bellRef = useRef(null);
  const token = localStorage.getItem("token");

  useEffect(() => {
    const user = JSON.parse(localStorage.getItem("user"));
    if (!user || user.role !== "JOB_SEEKER") return;

    fetchNotifications();

    const handleClickOutside = (event) => {
      if (bellRef.current && !bellRef.current.contains(event.target)) {
        setShowDropdown(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const fetchNotifications = async () => {
    try {
      const response = await axios.get("http://localhost:8080/notifications", {
        headers: { Authorization: `Bearer ${token}` },
      });
      const unseen = response.data.filter((n) => !n.seen);
      setNotifications(unseen);
    } catch (error) {
      console.error("Failed to fetch notifications:", error);
    }
  };

  const toggleDropdown = () => setShowDropdown(!showDropdown);

  const markAsRead = async (id) => {
    try {
      await axios.put(
        `http://localhost:8080/notifications/${id}/read`,
        {},
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setNotifications((prev) => prev.filter((n) => n.id !== id));
    } catch (error) {
      console.error("Failed to mark notification as read:", error);
    }
  };

  return (
    <div className="relative" ref={bellRef}>
      <div className="relative cursor-pointer" onClick={toggleDropdown}>
        <FaBell className="text-lg hover:text-[#6F4E37] transition" title="Notifications" />
        {notifications.length > 0 && (
          <span className="absolute -top-2 -right-2 bg-red-600 text-white text-xs font-bold px-1.5 py-0.5 rounded-full animate-pulse">
            {notifications.length}
          </span>
        )}
      </div>

      {showDropdown && (
        <div className="absolute right-0 mt-2 w-[400px] bg-white shadow-xl border rounded-xl z-50 max-h-[450px] overflow-y-auto">
          <div className="p-4 font-semibold text-gray-800 border-b text-base">
            Notifications
          </div>

          {notifications.length === 0 ? (
            <div className="p-4 text-sm text-gray-500 text-center">
              No new notifications
            </div>
          ) : (
            <div className="p-2 space-y-2">
              {notifications.map((n) => (
                <div
                  key={n.id}
                  className="flex gap-3 p-3 bg-white rounded-lg shadow-sm hover:shadow-md transition"
                >
                  <img
                    src={n.companyLogoUrl}
                    alt="Company Logo"
        className="w-10 h-10 rounded-full object-contain border"
                  />
                  <div className="flex-1">
                    <p className="text-sm text-gray-800 leading-snug">{n.message}</p>
                    <p className="text-xs text-gray-400 mt-1">
                      {formatDistanceToNow(new Date(n.createdAt), {
                        addSuffix: true,
                      })}
                    </p>
                  </div>
                  <button
                    className="text-xs text-[#6F4E37] hover:underline self-start"
                    onClick={() => markAsRead(n.id)}
                  >
                    Mark read
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
