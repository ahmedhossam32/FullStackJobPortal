import { useEffect, useContext } from "react";
import axios from "axios";
import { toast } from "react-toastify";
import { AuthContext } from "../context/AuthContext";
import CustomNotificationToast from "./CustomNotificationToast";

export default function NotificationToastManager() {
  const { user } = useContext(AuthContext);

  useEffect(() => {
    if (!user) {
      console.log("🚫 No user yet, skipping toast logic.");
      return;
    }

    if (user.role !== "JOB_SEEKER") {
      console.log("🚫 User is not a job seeker. Skipping notifications.");
      return;
    }

    const token = localStorage.getItem("token");
    if (!token) {
      console.log("🚫 No token found, skipping toast logic.");
      return;
    }

    const showToasts = async () => {
      try {
        console.log("📡 Fetching notifications...");
        const res = await axios.get("http://localhost:8080/notifications", {
          headers: { Authorization: `Bearer ${token}` },
        });

        const unread = res.data.filter((n) => !n.seen);
        console.log("📌 Unread notifications:", unread);

        if (unread.length === 0) {
          console.log("✅ No unread notifications.");
          return;
        }

        localStorage.removeItem("shownToasts");

        const alreadyShown = localStorage.getItem("shownToasts");
        if (alreadyShown === "true") {
          console.log("⚠️ Toasts already shown, skipping.");
          return;
        }

        let index = 0;
        localStorage.setItem("shownToasts", "true");

        const showNext = () => {
          if (index >= unread.length) return;

          const n = unread[index++];
          console.log("🔊 Showing toast for:", n.message);

          toast(<CustomNotificationToast notification={n} token={token} />, {
            autoClose: 5000,
            closeButton: false,
            position:"top-right",
            hideProgressBar: true,
            style: { background: "transparent", boxShadow: "none", padding: 0 },
          });

          setTimeout(showNext, 5500);
        };

        showNext();
      } catch (err) {
        console.error("❌ Failed to fetch toast notifications:", err);
      }
    };

    setTimeout(showToasts, 500);
  }, [user]);

  return null;
}
