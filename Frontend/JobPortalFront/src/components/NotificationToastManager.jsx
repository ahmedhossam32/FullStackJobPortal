import { useEffect, useContext } from "react";
import { toast } from "react-toastify";
import { AuthContext } from "../context/AuthContext";
import CustomNotificationToast from "./CustomNotificationToast";
import apiClient from "../api/client";

export default function NotificationToastManager() {
  const { user } = useContext(AuthContext);

  useEffect(() => {
    if (!user || user.role !== "JOB_SEEKER") return;

    const showToasts = async () => {
      try {
        const res = await apiClient.get("/notifications");
        const unread = res.data.filter((n) => !n.seen);

        if (unread.length === 0) return;

        localStorage.removeItem("shownToasts");

        const alreadyShown = localStorage.getItem("shownToasts");
        if (alreadyShown === "true") return;

        let index = 0;
        localStorage.setItem("shownToasts", "true");

        const showNext = () => {
          if (index >= unread.length) return;
          const n = unread[index++];
          const toastId = `notif-${n.id}`;

          toast(
            <CustomNotificationToast notification={n} toastId={toastId} />,
            {
              toastId,
              autoClose: 5000,
              closeButton: false,
              position: "top-right",
              hideProgressBar: true,
              style: {
                background: "transparent",
                boxShadow: "none",
                padding: 0,
                maxWidth: "100vw",
                width: "auto",
              },
            }
          );

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