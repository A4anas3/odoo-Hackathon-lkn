import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { User, Mail, Lock, Phone, ArrowRight } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

export const Register = () => {
  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    password: '',
    phone: '',
    roleName: 'ROLE_STAFF',
  });
  const [loading, setLoading] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const ok = await register(formData);
      if (ok) {
        navigate('/');
      }
    } catch {
      // Handled by Toast
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
      <div className="w-full max-w-md bg-white rounded-2xl border border-slate-200 shadow-xl p-8">
        <div className="text-center mb-6">
          <div className="w-12 h-12 rounded-2xl bg-[#714B67] flex items-center justify-center text-white font-bold text-xl mx-auto shadow-md mb-2">
            B
          </div>
          <h1 className="text-2xl font-bold text-black tracking-tight">Create Brightloop Account</h1>
          <p className="text-xs text-slate-500 mt-1 uppercase tracking-wider font-semibold">
            Enterprise Subscription Access
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-3.5">
          <div>
            <label className="block text-xs font-bold text-black uppercase tracking-wider mb-1">
              Full Name
            </label>
            <div className="relative">
              <User className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
              <input
                type="text"
                required
                value={formData.fullName}
                onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                placeholder="John Doe"
                className="w-full pl-9 pr-3 py-2 bg-white border border-slate-300 rounded-lg text-sm text-black placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#714B67] transition-all"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-black uppercase tracking-wider mb-1">
              Work Email
            </label>
            <div className="relative">
              <Mail className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
              <input
                type="email"
                required
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                placeholder="john@company.com"
                className="w-full pl-9 pr-3 py-2 bg-white border border-slate-300 rounded-lg text-sm text-black placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#714B67] transition-all"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-black uppercase tracking-wider mb-1">
              Phone Number
            </label>
            <div className="relative">
              <Phone className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
              <input
                type="tel"
                value={formData.phone}
                onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                placeholder="+91 98765 43210"
                className="w-full pl-9 pr-3 py-2 bg-white border border-slate-300 rounded-lg text-sm text-black placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#714B67] transition-all"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-black uppercase tracking-wider mb-1">
              Password
            </label>
            <div className="relative">
              <Lock className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
              <input
                type="password"
                required
                value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                placeholder="••••••••"
                className="w-full pl-9 pr-3 py-2 bg-white border border-slate-300 rounded-lg text-sm text-black placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#714B67] transition-all"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-black uppercase tracking-wider mb-1">
              Account Role
            </label>
            <select
              value={formData.roleName}
              onChange={(e) => setFormData({ ...formData, roleName: e.target.value })}
              className="w-full px-3 py-2 bg-white border border-slate-300 rounded-lg text-sm text-black focus:outline-none focus:ring-2 focus:ring-[#714B67] transition-all"
            >
              <option value="ROLE_STAFF">Staff Member</option>
              <option value="ROLE_ADMIN">Administrator</option>
              <option value="ROLE_CUSTOMER">Portal Customer</option>
            </select>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-2.5 px-4 rounded-lg bg-[#714B67] hover:bg-[#5d3d54] text-white font-semibold text-sm shadow-md transition-all flex items-center justify-center space-x-2 disabled:opacity-50 mt-4"
          >
            <span>{loading ? 'Creating Account...' : 'Complete Registration'}</span>
            {!loading && <ArrowRight className="w-4 h-4" />}
          </button>
        </form>

        <div className="text-center mt-6 pt-4 border-t border-slate-200">
          <p className="text-xs text-slate-600">
            Already registered?{' '}
            <Link to="/login" className="font-bold text-[#714B67] hover:underline">
              Sign in here
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Register;
