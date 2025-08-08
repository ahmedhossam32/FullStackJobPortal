import Navbar from '../components/navbar/Navbar';
import Footer from '../components/Footer';
import leftImage from '../pics/background3.png';
import rightImage from '../pics/background2.png';

// New icon imports
import jobIcon from '../pics/icons8-job-application-50.png';
import companyIcon from '../pics/icons8-company-30.png';
import communityIcon from '../pics/icons8-community-30.png';
import incomeIcon from '../pics/icons8-money-24.png';

export default function Home() {
  return (
    <div className="bg-white text-black">
     
      {/* Hero Section */}
      <header className="px-6 py-10 md:py-6 flex flex-col md:flex-row items-start justify-between gap-4 md:gap-12">
        {/* Left Image */}
        <img
          src={leftImage}
          alt="Left Illustration"
          className="w-[300px] md:w-[380px] max-w-full object-contain mt-6 md:mt-0"
        />

        {/* Center Text */}
        <div className="text-center md:w-1/3 md:mt-10">
          <h1 className="text-4xl font-bold mb-3">Your Career Starts Here</h1>
          <p className="text-lg text-gray-600 mb-5">
            Explore thousands of job opportunities and company reviews
          </p>
          <div className="space-x-4">
            <button className="px-6 py-2 bg-black text-white rounded">Find Jobs</button>
            <button className="px-6 py-2 border rounded">Post a Job</button>
          </div>
        </div>

        {/* Right Image */}
        <img
          src={rightImage}
          alt="Right Illustration"
          className="w-[300px] md:w-[380px] max-w-full object-contain mt-6 md:mt-0"
        />
      </header>

      {/* Feature Icons Section */}
      <section className="py-10 px-6 grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-6 text-center">
        <div>
          <img src={jobIcon} alt="Find Jobs" className="mx-auto w-10 h-10 mb-2" />
          <p className="font-medium">Find and apply to jobs</p>
        </div>
        <div>
          <img src={companyIcon} alt="Company Profiles" className="mx-auto w-10 h-10 mb-2" />
          <p className="font-medium">Search company profiles</p>
        </div>
        <div>
          <img src={communityIcon} alt="Community" className="mx-auto w-10 h-10 mb-2" />
          <p className="font-medium">Join your work community</p>
        </div>
        <div>
          <img src={incomeIcon} alt="Compare Salaries" className="mx-auto w-10 h-10 mb-2" />
          <p className="font-medium">Compare salaries</p>
        </div>
      </section>

      <Footer />
    </div>
  );
}
