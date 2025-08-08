import axios from "axios";
import { IoMdClose } from "react-icons/io";

export default function CustomNotificationToast({ notification, token }) {
  const handleMarkAsRead = async () => {
    try {
      await axios.put(
        `http://localhost:8080/notifications/${notification.id}/read`,
        {},
        { headers: { Authorization: `Bearer ${token}` } }
      );
      console.log(`✅ Notification ${notification.id} marked as read`);
    } catch (err) {
      console.error(`❌ Failed to mark as read:`, err);
    }
  };

  const getBoldedMessage = (message) => {
    const match = message.match(
      /application for '(.+)' at (.+?) has been (.+)\./i
    );
    if (!match) return message;

    const [, jobTitle, companyName, status] = match;

    return (
      <p className="text-sm text-gray-800 text-left leading-snug">
        Update: Your application for{" "}
        <span className="font-semibold">'{jobTitle}'</span> at{" "}
        <span className="font-semibold">{companyName}</span> has been{" "}
        <span className="font-semibold">{status}</span>.
      </p>
    );
  };

  return (
    <div className="relative rounded-xl bg-white p-4 shadow-lg flex items-start gap-3 max-w-sm border border-gray-100">
      {/* Close button */}
      <button
        onClick={handleMarkAsRead}
        className="absolute top-2 right-2 text-gray-400 hover:text-gray-600"
      >
        <IoMdClose size={18} />
      </button>

      {/* Company logo */}
      <img
        src={notification.companyLogoUrl || "/default-logo.png"}
        alt="Company Logo"
        className="w-10 h-10 rounded-full object-contain border"
      />

      {/* Message + Button */}
      <div className="flex-1">
        {getBoldedMessage(notification.message)}

        <button
          onClick={handleMarkAsRead}
          className="mt-2 bg-[#6b4027] text-white text-xs font-medium px-4 py-1.5 rounded hover:bg-[#59331f] transition"
        >
          Mark as Read
        </button>
      </div>
    </div>
  );
}
