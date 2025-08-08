import { useContext } from "react";
import { AuthContext } from "../../context/AuthContext";
import NavbarEmployer from "./NavbarEmployer";
import NavbarJobSeeker from "./NavbarJobSeeker";
import NavbarGuest from "./NavbarGuest";

export default function Navbar({ onOpenJobSeeker, onOpenEmployer }) {
  const { user } = useContext(AuthContext);

  if (!user) {
    return <NavbarGuest onOpenJobSeeker={onOpenJobSeeker} onOpenEmployer={onOpenEmployer} />;
  }

  if (user.role === "EMPLOYER") {
    return <NavbarEmployer />;
  }

  if (user.role === "JOB_SEEKER") {
    return <NavbarJobSeeker />;
  }

  return null;
}
