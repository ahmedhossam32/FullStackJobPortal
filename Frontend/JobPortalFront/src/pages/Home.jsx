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
      <header className="px-4 sm:px-6 md:px-6 py-10 md:py-6 flex flex-col md:flex-row md:items-center justify-between gap-6 md:gap-12 overflow-hidden">
        {/* Left Image — hidden on mobile */}
        <img
          src={leftImage}
          alt="Left Illustration"
          className="hidden md:block md:w-[380px] max-w-full object-contain"
        />

        {/* Center Text */}
        <div className="text-center w-full md:w-1/2 lg:w-1/3 md:mt-10 mx-auto py-4 md:py-0">
          <h1 className="text-2xl sm:text-3xl md:text-4xl font-bold mb-3">Your Career Starts Here</h1>
          <p className="text-sm sm:text-base md:text-lg text-gray-600 mb-6">
            Explore thousands of job opportunities and company reviews
          </p>
          <div className="flex flex-col sm:flex-row flex-wrap justify-center gap-3">
            <button className="w-full sm:w-auto px-5 py-3 min-h-[44px] bg-black text-white rounded">Find Jobs</button>
            <button className="w-full sm:w-auto px-5 py-3 min-h-[44px] border rounded">Post a Job</button>
          </div>
        </div>

        {/* Right Image — hidden on mobile */}
        <img
          src={rightImage}
          alt="Right Illustration"
          className="hidden md:block md:w-[380px] max-w-full object-contain"
        />
      </header>

      {/* Feature Icons Section */}
      <section className="py-10 px-4 sm:px-6 md:px-6 max-w-screen-xl mx-auto grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-6 text-center">
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
