import { useState } from 'react';
import { FiSearch } from 'react-icons/fi';
import { GoLocation } from 'react-icons/go';
import { FaTimes } from 'react-icons/fa';
import Select from 'react-select';

const jobTypeOptions = [
  { value: '', label: 'All types' },
  { value: 'FULL_TIME', label: 'Full Time' },
  { value: 'PART_TIME', label: 'Part Time' },
  { value: 'CONTRACT', label: 'Contract' },
  { value: 'INTERNSHIP', label: 'Internship' },
];

export default function JobSearchBar({ onSearch }) {
  const [title, setTitle] = useState('');
  const [location, setLocation] = useState('');
  const [type, setType] = useState(jobTypeOptions[0]);

  const handleClick = () => {
    onSearch({ title, location, type: type.value });
  };

  const handleClear = () => {
    setTitle('');
    setLocation('');
    setType(jobTypeOptions[0]);
    onSearch({ title: '', location: '', type: '' });
  };

  const hasValue = title || location || type.value;

  return (
    <div className="flex justify-center my-8 z-10 relative">
      <div className="flex flex-col md:flex-row md:items-center bg-white/70 backdrop-blur-md rounded-xl overflow-visible w-[90%] max-w-5xl shadow-lg border border-gray-200">

        {/* Job title input */}
        <div className="flex items-center px-4 py-3 w-full md:w-1/3 border-b md:border-b-0 border-gray-200">
          <FiSearch className="text-gray-500 mr-2 flex-shrink-0" />
          <input
            type="text"
            placeholder="Find your perfect job"
            className="bg-transparent outline-none w-full text-sm placeholder:text-gray-400"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
          />
        </div>

        {/* Location input */}
        <div className="flex items-center px-4 py-3 w-full md:w-1/3 border-b md:border-b-0 md:border-l border-gray-200">
          <GoLocation className="text-gray-500 mr-2 flex-shrink-0" />
          <input
            type="text"
            placeholder="Enter city or country"
            className="bg-transparent outline-none w-full text-sm placeholder:text-gray-400"
            value={location}
            onChange={(e) => setLocation(e.target.value)}
          />
        </div>

        {/* Job type dropdown using react-select */}
        <div className="px-4 py-2 w-full md:w-1/4 border-b md:border-b-0 md:border-l border-gray-200">
          <Select
            value={type}
            onChange={setType}
            options={jobTypeOptions}
            className="text-sm z-50"
            classNamePrefix="react-select"
            menuPortalTarget={typeof window !== 'undefined' ? document.body : null}
            styles={{
              control: (base) => ({
                ...base,
                border: 'none',
                boxShadow: 'none',
                background: 'transparent',
                fontSize: '0.875rem',
              }),
              menu: (base) => ({
                ...base,
                zIndex: 9999,
              }),
              menuPortal: (base) => ({
                ...base,
                zIndex: 9999,
              }),
            }}
          />
        </div>

        {/* Clear button */}
        {hasValue && (
          <button
            onClick={handleClear}
            className="p-2 text-gray-400 hover:text-gray-600 transition self-center"
            title="Clear search"
          >
            <FaTimes />
          </button>
        )}

        {/* Search button */}
        <button
          onClick={handleClick}
          className="bg-[#6F4E37] text-white px-6 py-3 md:py-2 m-2 min-h-[44px] w-[calc(100%-1rem)] md:w-auto text-sm font-medium rounded-xl hover:bg-[#3E2723] transition"
        >
          Search
        </button>
      </div>
    </div>
  );
}
