# 💼 Full Stack Job Portal App

A complete **full-stack job portal web application** built with **Java Spring Boot (Backend)** and **React + Vite (Frontend)**.  
It supports **secure authentication**, job browsing, filtering, saving, applying, and **role-based access** for job seekers and employers.  

This project is designed to provide a **real-world job marketplace experience** — from a job seeker discovering, applying, and tracking jobs,  
to an employer posting jobs, managing applicants, and making hiring decisions.  

Every feature was **planned, built, and tested end-to-end**, covering frontend, backend, database, and API integration.

---

## 📖 About the Project

This application simulates the core workflows of modern job platforms like LinkedIn or Indeed but built entirely from scratch.  
It includes **secure JWT authentication**, dynamic job listing, saving and applying features, applicant management, and a fully interactive dashboard for employers.  

The project required:  
- Building a **REST API** with authentication, file upload handling, and relational data modeling in the backend.  
- Designing and implementing a **responsive React UI** from scratch without pre-made templates.  
- Managing **real-time-like updates** (e.g., notifications, application status changes).  
- Writing clean, maintainable code with modular architecture.

---

## 🛠️ Built With

### 🧠 Backend
- Java 17
- Spring Boot
- Spring Security with JWT
- Hibernate (JPA)
- PostgreSQL
- Lombok

For the full backend documentation and implementation details,  
please navigate to the [Backend README](./backend/README.md).

### 🎨 Frontend
- React.js
- Vite
- Axios
- Formik
- Context API
- Plain CSS / Inline styling

---

## ✨ Features

### 👤 Job Seeker
- **Authentication:** Secure sign up, sign in, and logout flows.
- **Job Discovery:** Browse, filter, and search jobs by title, location, and type.
- **Job Details:** View full job descriptions, responsibilities, and required skills.
- **Job Saving:** Save and unsave jobs with instant visual feedback.
- **Applications:** Apply to jobs, answer optional screening questions, and upload a resume.
- **Tracking:** View saved jobs, applied jobs, and detailed application status.
- **Management:** Withdraw applications at any time.
- **Profile Editing:** Update profile picture, resume, username, email, etc.
- **Notifications:** Get notified when application status changes, and mark notifications as read.

### 🧑‍💼 Employer
- **Authentication:** Sign up, sign in, and logout.
- **Job Posting:** Create and publish jobs with full details, responsibilities, skills, and optional screening questions.
- **Job Management:** Edit and delete posted jobs.
- **Applicant Management:** View applicants per job or across all jobs.
- **Decision Making:** Accept or reject applicants and notify them automatically.
- **Resume Review:** View resumes directly in-browser.
- **Dashboard:** View statistics for total jobs, applicants, accepted, rejected, and pending applications.

---

## 🌟 Why This Project Stands Out

- **60+ unique UI screens** designed and implemented manually.
- **Fully functional backend** with clean service-layer architecture.
- **Real database integration** (PostgreSQL) with optimized queries and entity relationships.
- **File uploads** for resumes and profile pictures with secure handling.
- **Notification system** that mimics real-time updates using efficient state management.
- **Role-based access control** ensuring separate flows for job seekers and employers.
- **Responsive design** that adapts to various screen sizes without relying on heavy UI frameworks.

---

## 🖼️ Screenshots (General Overview)

### 🌐 Common Pages

#### 1. Home Page
![HomePage](./ScreenShots/HomePage.png)  
The landing page for both job seekers and employers, with clear options to sign up or log in.

#### 2. Selection Options
![SelectionOptions](./ScreenShots/SelectionOptions.png)  
Role selection menu ensuring users choose the correct account type before signing up or logging in.

---

### 👤 Job Seeker Flow (15 Screenshots)

#### 1. Registering as a Job Seeker
![RegisteringJobSeeker](./ScreenShots/RegisteringJobSeeker.png)  
Signup form with required details (name, username, password, DOB, email) and optional profile picture & resume.

#### 2. Landing Job Page
![LandingJobPage](./ScreenShots/LandingJobPage.png)  
Main job listing interface with job cards on the left and job details on the right.

#### 3. Viewing More Jobs (Pagination)
![ViewingJobs](./ScreenShots/ViewingJobs.png)  
Pagination controls allow browsing through more job postings.

#### 4. Selected Job (Google Example)
![SelectedJobGoogle](./ScreenShots/SelectedJobGoogle.png)  
Detailed job view with responsibilities, skills, and application options.

#### 5. Search by Type
![SearchTypes](./ScreenShots/SearchTypes.png)  
Filter jobs by type (Full-time, Part-time, Internship, etc.).

#### 6. Searching for a Job
![SearchingForJob](./ScreenShots/SearchingForJob.png)  
Example of searching for internships — results show only relevant positions.

#### 7. Job Card Design
![JobCard](./ScreenShots/JobCard.png)  
Compact card layout displaying job title, company logo, location, and type.

#### 8. Saving a Job
![SavingGameDevJob](./ScreenShots/SavingGameDevJob.png)  
Saving a job triggers a toast notification and fills the save icon.

#### 9. My Jobs Page
![MyJobs](./ScreenShots/MyJobs.png)  
Tabs for Saved, Applied, and Interviews, with job counts in each category.

#### 10. Removing a Saved Job
![RemovingSavedJob](./ScreenShots/RemovingSavedJob.png)  
Removing a saved job instantly updates the list and count.

#### 11. Applying for a Saved Job
![ApplyingforGameDevJob](./ScreenShots/ApplyingforGameDevJob.png)  
Start of application process, beginning with screening questions.

#### 12. Application Details
![ApplicationDetails](./ScreenShots/ApplicationDeatils.png)  
Submitted application overview showing job info, resume link, and status.

#### 13. Notification Pop-Up
![NotificationPopUp](./ScreenShots/NotificationPopUp.png)  
Pop-up showing status changes for multiple job applications.

#### 14. Notification Closer Look
![NotificationCloserLook](./ScreenShots/NotificationCloserLook.png)  
Detailed pop-up view with company logos, job titles, and status updates.

#### 15. Notification from Bell
![NotificationFromBell](./ScreenShots/NotificationFromBell.png)  
Dropdown list of notifications accessible from the navbar bell icon.

---

### 🧑‍💼 Employer Flow (10 Screenshots)

#### 1. Employer Sign Up
![EmployerSignUp](./ScreenShots/EmployerSiginUp.png)  
Signup form for employers with account info, company name, industry, email, and logo.

#### 2. Employer Dashboard (Empty)
![DashboardGoogle](./ScreenShots/DashboardGoogle.png)  
Empty dashboard shown after signing in before any jobs are posted.

#### 3. Creating a Job (Step 1)
![CreatingJob](./ScreenShots/CreatingJob.png)  
Starting job creation by adding the title and description.

#### 4. Adding Required Skills
![CreatingJob4](./ScreenShots/CreatingJob4.png)  
Step for entering skills required for the position.

#### 5. Job Created Successfully
![CreatingJobSuccessfully](./ScreenShots/CreatingJobSucessfully.png)  
Confirmation screen with options to view the job or add another.

#### 6. Created Job Details
![CreatedJobDetails](./ScreenShots/CreatedJobDetails.png)  
Job details page with edit and delete controls.

#### 7. Dashboard After Adding Jobs
![DashboardGoogleAfterAddingJobs](./ScreenShots/DashboardGoogleAfteraddingJobs.png)  
Dashboard with counts for jobs and applicants after posting multiple jobs.

#### 8. Viewing Applicants
![ViewingApplicants](./ScreenShots/ViewingApplicants.png)  
List of applicants for a specific job.

#### 9. Viewing Application Details
![ViewingApplicationDetails](./ScreenShots/ViewingApplicationDetails.png)  
Detailed applicant view with resume link and application info.

#### 10. Status Changed to Accepted
![StatusChangedToAccepted](./ScreenShots/Statuschangedtoaccepted.png)  
Applicant status updated to "Accepted" with automatic notification.

---

## 📄 Full Walkthrough
To see **all 60+ screenshots** with detailed explanations for every step of both Job Seeker and Employer workflows:  
➡️ [**View Full Screenshot Walkthrough**](./Full_Walkthrough.md)

---

## 📂 Project Structure
project-root/
│ README.md
│ Full_Walkthrough.md
│ backend/
│ frontend/
└── ScreenShots/
