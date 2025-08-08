import { useState, useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import { toast } from 'react-toastify';
import { useNavigate } from 'react-router-dom';

export default function EmployerModal({ isOpen, onClose }) {
  const [activeTab, setActiveTab] = useState('signin');
  const [profilePic, setProfilePic] = useState(null);
  const { login } = useContext(AuthContext);
  const navigate = useNavigate();

  const [signupData, setSignupData] = useState({
    name: '',
    username: '',
    password: '',
    companyName: '',
    industry: '',
    email: ''
  });

  const [signinData, setSigninData] = useState({
    username: '',
    password: ''
  });

  if (!isOpen) return null;

  const handleSignupChange = (e) => {
    setSignupData(prev => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSigninChange = (e) => {
    setSigninData(prev => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleProfileChange = (e) => {
    const file = e.target.files[0];
    if (file) setProfilePic(file);
  };

  const handleSignUp = async (e) => {
    e.preventDefault();
    try {
      const res = await fetch("http://localhost:8080/auth/signup/employer", {
        method: "POST",
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(signupData)
      });

      if (!res.ok) throw new Error("Signup failed");
      toast.success("Account created! Now sign in.");

      setSigninData({
        username: signupData.username,
        password: signupData.password
      });
      setActiveTab("signin");
    } catch (err) {
      toast.error("Signup failed: " + err.message);
    }
  };

  const handleSignIn = async (e) => {
    e.preventDefault();
    try {
      const loginRes = await fetch("http://localhost:8080/auth/signin", {
        method: "POST",
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(signinData)
      });

      if (!loginRes.ok) throw new Error("Invalid credentials");

      const data = await loginRes.json();
      const token = data.token.replace(/\n/g, '');

      // ❌ Prevent job seekers from logging in via employer modal
      if (data.role !== "EMPLOYER") {
        toast.error("This account is not an employer!");
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

      const meRes = await fetch("http://localhost:8080/user/me", {
        method: "GET",
        headers: { Authorization: `Bearer ${token}` }
      });

      const updatedUser = await meRes.json();
      login(updatedUser, token);

      toast.success("Signed in!");
      onClose();
      navigate("/dashboard");
    } catch (err) {
      toast.error("Sign in failed: " + err.message);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/30 backdrop-blur-[3px] flex items-center justify-center z-50">
      <div className="bg-white p-6 w-full max-w-md rounded shadow-lg relative">
        <button className="absolute top-2 right-2 text-xl" onClick={onClose}>
          &times;
        </button>

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
            <input name="name" value={signupData.name} onChange={handleSignupChange} type="text" placeholder="Name" className="w-full border px-3 py-2 rounded" />
            <input name="username" value={signupData.username} onChange={handleSignupChange} type="text" placeholder="Username" className="w-full border px-3 py-2 rounded" />
            <input name="password" value={signupData.password} onChange={handleSignupChange} type="password" placeholder="Password" className="w-full border px-3 py-2 rounded" />
            <input name="companyName" value={signupData.companyName} onChange={handleSignupChange} type="text" placeholder="Company Name" className="w-full border px-3 py-2 rounded" />
            <input name="industry" value={signupData.industry} onChange={handleSignupChange} type="text" placeholder="Industry" className="w-full border px-3 py-2 rounded" />
            <input name="email" value={signupData.email} onChange={handleSignupChange} type="email" placeholder="Email" className="w-full border px-3 py-2 rounded" />

            <div>
              <label className="block mb-1 font-medium">Upload Profile Picture:</label>
              {!profilePic ? (
                <input type="file" name="profilePicture" accept="image/*" onChange={handleProfileChange} className="w-full border px-3 py-[6px] rounded bg-gray-50" />
              ) : (
                <p className="text-sm text-green-600">✅ {profilePic.name} uploaded</p>
              )}
            </div>

            <button type="submit" className="w-full bg-black text-white py-2 rounded">Sign Up</button>
          </form>
        )}
      </div>
    </div>
  );
}
