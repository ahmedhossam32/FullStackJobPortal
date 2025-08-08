import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import axios from "axios";
import ApplicationDetailsCard from "../components/ApplicationDetailsCard";
import { toast } from "react-toastify";

export default function ApplicationDetailsPage() {
  const { id } = useParams();
  const [application, setApplication] = useState(null);

  useEffect(() => {
    const token = localStorage.getItem("token");

    // ✅ Dismiss any lingering toast on page load
    toast.dismiss();

    axios
      .get(`http://localhost:8080/applications/${id}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })
      .then((res) => setApplication(res.data))
      .catch((err) => {
        console.error("Error fetching application:", err);
        toast.error("Failed to load application details");
      });
  }, [id]);

  if (!application) {
    return (
      <div className="p-6 text-center text-gray-600">
        Loading application details...
      </div>
    );
  }

  return <ApplicationDetailsCard application={application} />;
}
