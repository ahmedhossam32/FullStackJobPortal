# 💼 Full Stack Job Portal App

A complete full-stack job portal web application built with **Java Spring Boot (Backend)** and **React + Vite (Frontend)**.  
It supports secure authentication, job browsing, filtering, saving, applying, and role-based access for job seekers and employers.

---

## 🛠️ Built With

### 🧠 Backend
- Java 17
- Spring Boot
- Spring Security with JWT
- Hibernate (JPA)
- PostgreSQL
- Lombok

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
- Sign up, sign in, and logout
- Browse & filter jobs by title, location, and type
- View job details
- Save & unsave jobs
- Apply to jobs (with optional screening questions)
- View saved jobs, applied jobs, and application details
- Withdraw applications
- Edit profile (picture, resume, username, email)
- Receive & mark notifications

### 🧑‍💼 Employer
- Sign up, sign in, and logout
- Create, update, and delete jobs
- View all posted jobs
- View applicants per job or across jobs
- Accept or reject applicants
- View resumes
- Dashboard with statistics

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
![EmployerSignUp](./ScreenShots/EmployerSignUp.png)  
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
![DashboardGoogleAfterAddingJobs](./ScreenShots/DashboardGoogleAfterAddingJobs.png)  
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
