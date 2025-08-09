# 💼 Full Stack Job Portal App

This is a complete full-stack job portal web application built with **Java Spring Boot (Backend)** and **React + Vite (Frontend)**. It supports secure authentication, job browsing, filtering, saving, applying, and role-based access for job seekers and employers.

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

### 🔗 Other Tools
- Postman (API Testing)
- GitHub
- IntelliJ & VS Code

---

## ✨ Features

### 👤 Job Seeker
- ✅ Sign up, sign in, and logout
- ✅ Browse all job listings
- ✅ Filter/search jobs by title, location, job type (Full-time, Part-time, Internship)
- ✅ View detailed job information: responsibilities, skills, location, and company info
- ✅ Apply to jobs by answering optional screening questions and submitting contact details + resume
- ✅ Save/unsave jobs for later
- ✅ View all saved jobs
- ✅ View applied jobs and application details
- ✅ Withdraw applications if needed
- ✅ Edit profile: update picture, resume, username, email, etc.
- ✅ Receive notifications when application status changes (accepted/rejected)
- ✅ Mark notifications as read

### 🧑‍💼 Employer
- ✅ Sign up, sign in, and logout
- ✅ Create/post new jobs
- ✅ Edit and delete posted jobs
- ✅ View all posted jobs
- ✅ View applicants for a specific job or across all jobs
- ✅ Accept or reject applicants
- ✅ Close jobs (no longer accepting applications)
- ✅ View applicant resumes
- ✅ Dashboard with statistics (total jobs, applicants, accepted, rejected, pending)

---

## 🎥 Demo Video

🚧 **Coming soon...**

---

## 🖼️ Screenshots
### 🌐 Common Pages

#### 1. Home Page
![HomePage](./screenshots/HomePage.png)  
This is the landing page for both employers and job seekers when they access the website.  
From here, users can choose to register either as an **Employer** (to post jobs) or as a **Job Seeker** (to find jobs), depending on their needs.

---

#### 2. Selection Options
![SelectionOptions](./screenshots/SelectionOptions.png)  
When a user clicks **Sign Up** or **Login**, a dropdown menu appears allowing them to choose their correct role — **Job Seeker** or **Employer**.  
Selecting the right role ensures proper registration and prevents login or feature access errors.

---

### 👤 Job Seeker Screens

#### 1. Registering as a Job Seeker
![RegisteringJobSeeker](./screenshots/RegisteringJobSeeker.png)  
When registering as a job seeker, users must provide their name, username, password, date of birth, and email.  
They also have the option to upload a **profile picture** and **resume**. These two fields are optional during registration, but without a resume, users cannot apply for jobs. However, they can upload a resume later while applying.  
Uploading it now links it directly to their account for future applications.

---

#### 2. Landing Job Page
![LandingJobPage](./screenshots/LandingJobPage.png)  
After signing in, job seekers land on the "Jobs For You" page.  
The left side displays a list of available job cards, and the right side shows the details of the currently selected job.  
Users can filter and search for jobs by **title**, **location**, and **type** (Full-time, Part-time, Internship).

---

#### 3. Selected Job (Example: Google)
![SelectedJobGoogle](./screenshots/SelectedJobGoogle.png)  
When a job card is selected, full details appear on the right — including job description, responsibilities, and required skills.  
The user can choose to **save** the job for later or **apply** directly.

---

#### 4. Viewing More Jobs with Pagination
![ViewingJobs](./screenshots/ViewingJobs.png)  
Users can browse additional job listings using pagination.  
Each page displays a maximum of **5 job cards**, with "Previous" and "Next" buttons for navigation.

---

#### 5. Job Card Design
![JobCard](./screenshots/JobCard.png)  
Each job card displays:
- Job title
- Company logo
- Job location
- Job type (Full-time, Part-time, Internship, etc.)  
The design ensures consistency and readability across the job listings.

---

#### 6. Saving a Job
![SavingGameDevJob](./screenshots/SavingGameDevJob.png)  
When a user saves a job (example: Game Developer at Netflix), a toast notification appears saying **"Job saved successfully!"**.  
The save icon changes to a filled brown color, indicating the job is saved.  
Users can save as many jobs as they want to review later.

---

#### 7. Searching for a Job
![SearchingForJob](./screenshots/SearchingForJob.png)  
Here, the user searches for jobs with the **Internship** filter applied.  
The search result returns a **Software Engineering Intern** position at Meta.

---

#### 8. Search by Type
![SearchTypes](./screenshots/SearchTypes.png)  
The search bar allows filtering jobs by type, including:
- All Types
- Full Time
- Part Time
- Contract
- Internship  

Users can combine search types with keywords or locations for precise results.

---

## 📂 My Jobs Module

### MyJobs
The **My Jobs** page contains three tabs:
1. **Saved** – Shows jobs the user has saved for later.
2. **Applied** – Shows jobs the user has already applied for.
3. **Interviews** – Shows jobs where the user has an interview scheduled.

Each tab displays a count of jobs.  
Example:  
- Saved: **7**  
- Applied: **0**  
- Interviews: **0**  

---

### RemovingSavedJob
When the user removes a saved job (e.g., **Data Privacy Engineer** at Google):
- The job is instantly removed from the **Saved** tab list.
- The saved count updates (e.g., from **7** to **6**).
- A success notification appears at the top:  
  _"Job removed from saved list."_

---

### ApplyingforGameDevJob
When the user decides to apply for a job from the Saved list (e.g., **Game Developer** at Netflix):
- The page splits into **two sections**:
  - **Right Panel** – Job details:
    - Company info, location, job type, and posting date.
    - Full job description, responsibilities, and required skills.
  - **Left Panel** – Application process starting with **Screening Questions**.
    - The user must answer all questions before proceeding.
- Clicking **Next** continues the application process.

---

## 🖼️ Application Process

#### 1. Application Process – Part 1
![Application Process 1](path/to/ApplicationProcess1.png)  
Before you can proceed to the next step, you must answer all screening questions. The **Next** button remains disabled until all questions are answered.

#### 2. Application Process – Part 2
![Application Process 2](path/to/ApplicationProcess2.png)  
In this step, you provide your **Name** and **Email**, and optionally a **Phone Number**.  
You also choose a **Resume**:
- Use your already uploaded resume.
- Or upload a new one.

#### 3. Submitted Application
![Submitted Application](path/to/SubmittedApplication.png)  
Once submitted:
- A success pop-up confirms it.
- You can view details.
- You cannot reapply for the same job.

#### 4. Application Details
![Application Details](path/to/ApplicationDetails.png)  
Shows:
- Job details
- Resume link
- Application status (**Pending**)

#### 5. Applying for a Meta Job
![Applying for Meta Job](path/to/ApplyingForMetaJob.png)  

---

## 📌 Saved & Applied Jobs

#### Saved Jobs Tab (After Applying)
![Saved Jobs Tab](screenshots/savedJobsTabAfterApplying.png)  

#### Applied Jobs Tab
![Applied Jobs Tab](screenshots/myJobsAppliedTab.png)  

#### Withdrawing an Application
![Withdrawing Application](screenshots/withdrawingApplication.png)  

#### Jobs Page After Applying
![Jobs Page After Applying](screenshots/jobsPageAfterApplying.png)  

#### Viewing Applications
![View Applications](screenshots/viewApplications.png)  

---

## 🔔 Notifications

#### NotificationPopUp & NotificationCloserLook
Shows updates when your application status changes.

#### NotificationFromBell
Access notifications from the bell icon.

#### MarkAsRead
Removes the notification after marking it as read.

---

### MyJobsAppliedTabAfterStatusChanged
![MyJobsAppliedTabAfterStatusChanged](screenshots/myJobsAppliedTabAfterStatusChanged.png)  

### RejectedApplication
![RejectedApplication](screenshots/rejectedApplication.png)  

### AcceptedApplication
![AcceptedApplication](screenshots/acceptedApplication.png)  

---

## 👤 JobSeekerProfilePage
![JobSeekerProfilePage](screenshots/jobSeekerProfilePage.png)  
Manage:
- Profile picture
- Personal info
- Resume  
Save changes to keep your profile updated.

---

## Summary of Job Seeker Functionalities
- Sign up, sign in, and log out.
- Browse, filter, and search jobs.
- View job details.
- Apply for jobs with/without screening questions.
- Save & unsave jobs.
- View & withdraw applications.
- Manage profile.
- Receive and manage notifications.

---

## 📂 Project Structure

