import { useState, useContext } from 'react';
import { toast } from 'react-toastify';
import { AuthContext } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

export default function JobSeekerModal({ isOpen, onClose }) {
  const [activeTab, setActiveTab] = useState('signin');
  const [profilePic, setProfilePic] = useState(null);
  const [resume, setResume] = useState(null);
  const { login } = useContext(AuthContext);
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    name: '',
    username: '',
    password: '',
    dob: '',
    email: ''
  });

  const [signinData, setSigninData] = useState({
    username: '',
    password: ''
  });

  if (!isOpen) return null;

  const handleChange = (e) => {
    setFormData(prev => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSigninChange = (e) => {
    setSigninData(prev => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleProfileChange = (e) => {
    const file = e.target.files[0];
    if (file) setProfilePic(file);
  };

  const handleResumeChange = (e) => {
    const file = e.target.files[0];
    if (file) setResume(file);
  };

  const handleSignUp = async (e) => {
    e.preventDefault();
    try {
      const signupRes = await fetch('http://localhost:8080/auth/signup/jobseeker', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      });

      if (!signupRes.ok) throw new Error('Signup failed');

      toast.success("Account created. Now sign in to complete your registration.");
      setSigninData({
        username: formData.username,
        password: formData.password
      });
      setActiveTab("signin");
    } catch (err) {
      console.error(err);
      toast.error("Signup failed: " + err.message);
    }
  };

  const handleSignIn = async (e) => {
    e.preventDefault();
    try {
      const loginRes = await fetch('http://localhost:8080/auth/signin', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(signinData)
      });

      if (!loginRes.ok) throw new Error("Invalid credentials");

      const data = await loginRes.json();
      const token = data.token.replace(/\n/g, '');

      if (data.role !== "JOB_SEEKER") {
        toast.error("This account is not a job seeker!");
        return;
      }

      if (profilePic) {
        const picForm = new FormData();
        picForm.append('file', profilePic);
        await fetch("http://localhost:8080/user/upload-profile-picture", {
          method: "POST",
          headers: { Authorization: `Bearer ${token}` },
          body: picForm
        });
      }

      if (resume) {
        const resumeForm = new FormData();
        resumeForm.append('file', resume);
        await fetch('http://localhost:8080/user/jobseeker/upload-resume', {
          method: 'POST',
          headers: { Authorization: `Bearer ${token}` },
          body: resumeForm
        });
      }

      const meRes = await fetch('http://localhost:8080/user/me', {
        method: 'GET',
        headers: { Authorization: `Bearer ${token}` }
      });

      if (!meRes.ok) throw new Error("Failed to fetch updated user");

      const updatedUser = await meRes.json();
      login(updatedUser, token);

      toast.success("Signed in!");
      onClose();
      navigate("/jobs");
    } catch (err) {
      toast.error("Sign in failed: " + err.message);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/30 backdrop-blur-[3px] flex items-center justify-center z-50">
      <div className="bg-white p-6 w-full max-w-md rounded shadow-lg relative">
        <button className="absolute top-2 right-2 text-xl" onClick={onClose}>&times;</button>
        <div className="text-center text-2xl font-bold mb-4">JobPort</div>

        <div className="flex justify-around mb-4">
          <button className={`px-4 py-2 ${activeTab === 'signin' ? 'border-b-2 border-black font-semibold' : ''}`} onClick={() => setActiveTab('signin')}>Sign In</button>
          <button className={`px-4 py-2 ${activeTab === 'signup' ? 'border-b-2 border-black font-semibold' : ''}`} onClick={() => setActiveTab('signup')}>Sign Up</button>
        </div>

        {activeTab === 'signin' ? (
          <form className="space-y-4" onSubmit={handleSignIn}>
            <input name="username" onChange={handleSigninChange} value={signinData.username} type="text" placeholder="Username" className="w-full border px-3 py-2 rounded" />
            <input name="password" onChange={handleSigninChange} value={signinData.password} type="password" placeholder="Password" className="w-full border px-3 py-2 rounded" />
            <button type="submit" className="w-full bg-black text-white py-2 rounded">Sign In</button>
          </form>
        ) : (
          <form className="space-y-4" onSubmit={handleSignUp}>
            <input name="name" value={formData.name} onChange={handleChange} type="text" placeholder="Name" className="w-full border px-3 py-2 rounded" />
            <input name="username" value={formData.username} onChange={handleChange} type="text" placeholder="Username" className="w-full border px-3 py-2 rounded" />
            <input name="password" value={formData.password} onChange={handleChange} type="password" placeholder="Password" className="w-full border px-3 py-2 rounded" />
            <input name="dob" value={formData.dob} onChange={handleChange} type="date" className="w-full border px-3 py-2 rounded" />
            <input name="email" value={formData.email} onChange={handleChange} type="email" placeholder="Email" className="w-full border px-3 py-2 rounded" />

            <div>
              <label className="block mb-1 font-medium">Upload Profile Picture:</label>
              {!profilePic ? (
                <input type="file" name="profilePicture" accept="image/*" onChange={handleProfileChange} className="w-full border px-3 py-[6px] rounded bg-gray-50" />
              ) : (
                <p className="text-sm text-green-600">✅ {profilePic.name} uploaded</p>
              )}
            </div>

            <div>
              <label className="block mb-1 font-medium">Upload Resume (PDF):</label>
              {!resume ? (
                <input type="file" name="resume" accept=".pdf" onChange={handleResumeChange} className="w-full border px-3 py-[6px] rounded bg-gray-50" />
              ) : (
                <p className="text-sm text-green-600">✅ {resume.name} uploaded</p>
              )}
            </div>

            <button type="submit" className="w-full bg-black text-white py-2 rounded">Sign Up</button>
          </form>
        )}
      </div>
    </div>
  );
}
