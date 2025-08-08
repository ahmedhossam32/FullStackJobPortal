// src/App.jsx
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { useState } from 'react';
import Home from './pages/Home';
import Navbar from './components/navbar/Navbar';
import JobSeekerModal from './modals/JobSeekerModal';
import EmployerModal from './modals/EmployerModal';
import { ToastContainer } from 'react-toastify';
import Jobs from './pages/Jobs';
import ApplyPage from './pages/ApplyPage'; 
import 'react-toastify/dist/ReactToastify.css';
import MyJobsPage from './pages/MyJobsPage';
import JobSeekerProfilePage from './pages/JobSeekerProfilePage';
import ApplicationDetailsPage from "./pages/ApplicationDetailsPage";
import NotificationToastManager from './components/NotificationToastManager';
import RequireRole from './components/RequireRole';
import EmployerDashboard from './pages/EmployerDashboard';
import EmployerJobsPage from './pages/EmployerJobsPage';
import EmployerJobDetailsPage from './pages/EmployerJobDetailsPage';
import CreateJobPage from "./pages/CreateJobPage";
import UpdateJobPage from "./pages/UpdateJobPage";
import EmployerApplicantsPage from "./pages/EmployerApplicantsPage";
import JobApplicantsPage from "./pages/JobApplicantsPage";
import ApplicationDetailsEmployerPage from "./pages/ApplicationDetailsEmployerPage";
function App() {
  const [showJobSeekerModal, setShowJobSeekerModal] = useState(false);
  const [showEmployerModal, setShowEmployerModal] = useState(false);

  return (
    <Router>
      <Navbar
        onOpenJobSeeker={() => setShowJobSeekerModal(true)}
        onOpenEmployer={() => setShowEmployerModal(true)}
      />

      <ToastContainer position="top-center" autoClose={2000} pauseOnHover />
      <NotificationToastManager />

      <Routes>
        {/* Public Routes */}
        <Route path="/" element={<Home />} />
        <Route path="/jobs" element={<Jobs />} />

        {/* Job Seeker Protected Routes */}
        <Route path="/myjobs" element={
          <RequireRole role="JOB_SEEKER">
            <MyJobsPage />
          </RequireRole>
        } />
        <Route path="/apply/:jobId" element={
          <RequireRole role="JOB_SEEKER">
            <ApplyPage />
          </RequireRole>
        } />
        <Route path="/profile" element={
          <RequireRole role="JOB_SEEKER">
            <JobSeekerProfilePage />
          </RequireRole>
        } />
        <Route path="/applications/:id" element={
          <RequireRole role="JOB_SEEKER">
            <ApplicationDetailsPage />
          </RequireRole>
        } />
        <Route path="/dashboard" element={<EmployerDashboard />} />
        <Route path="/employer/jobs" element={<EmployerJobsPage />} />
        <Route path="/employer/jobs/:jobId" element={<EmployerJobDetailsPage />} /> 
       <Route path="/create-job" element={<CreateJobPage />} />
       <Route path="/employer/jobs/update/:jobId" element={<UpdateJobPage />} />
       <Route path="/employer/applicants" element={<EmployerApplicantsPage />} />
       <Route path="/employer/jobs/:jobId/applicants" element={<JobApplicantsPage />} />
       <Route path="/employer/applications/:id" element={<ApplicationDetailsEmployerPage />} />

      </Routes>

      {/* Modals */}
      <JobSeekerModal
        isOpen={showJobSeekerModal}
        onClose={() => setShowJobSeekerModal(false)}
      />
      <EmployerModal
        isOpen={showEmployerModal}
        onClose={() => setShowEmployerModal(false)}
      />
    </Router>
  );
}

export default App;
