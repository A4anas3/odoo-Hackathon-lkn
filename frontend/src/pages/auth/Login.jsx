import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Lock, Mail, ArrowRight, ShieldCheck } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

export const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const ok = await login(email, password);
      if (ok) {
        navigate('/');
      }
    } catch {
      // Error handled by AuthContext via Toast
    } finally {
      setLoading(false);
    }
  };

  const handlePreFill = (userEmail, userPass) => {
    setEmail(userEmail);
    setPassword(userPass);
  };

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
      <div className="w-full max-w-md bg-white rounded-2xl border border-slate-200 shadow-xl p-8">
        {/* Brand header */}
        <div className="text-center mb-8">
          <div className="w-14 h-14 rounded-2xl bg-[#714B67] flex items-center justify-center text-white font-bold text-2xl mx-auto shadow-md mb-3">
            B
          </div>
          <h1 className="text-2xl font-bold text-black tracking-tight">Brightloop ERP</h1>
          <p className="text-xs text-slate-500 mt-1 uppercase tracking-wider font-semibold">
            Subscription & Billing Engine
          </p>
        </div>

        {/* Demo Credentials Quick-Fill Pills */}
        <div className="mb-6 p-3 bg-purple-50 rounded-xl border border-purple-200">
          <div className="flex items-center space-x-1.5 text-xs font-bold text-[#714B67] mb-2">
            <ShieldCheck className="w-4 h-4" />
            <span>Demo Test Credentials (Click to prefill):</span>
          </div>
          <div className="flex flex-wrap gap-2">
            <button
              type="button"
              onClick={() => handlePreFill('admin@brightloop.local', 'Admin@1234')}
              className="text-xs font-semibold px-2.5 py-1 rounded bg-white text-[#714B67] border border-purple-300 hover:bg-purple-100 transition-colors shadow-2xs"
            >
              Admin (Admin@1234)
            </button>
            <button
              type="button"
              onClick={() => handlePreFill('staff@brightloop.local', 'Staff@1234')}
              className="text-xs font-semibold px-2.5 py-1 rounded bg-white text-slate-700 border border-slate-300 hover:bg-slate-100 transition-colors shadow-2xs"
            >
              Staff (Staff@1234)
            </button>
          </div>
        </div>

        {/* Login form */}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-black uppercase tracking-wider mb-1">
              Email Address
            </label>
            <div className="relative">
              <Mail className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="name@company.com"
                className="w-full pl-9 pr-3 py-2 bg-white border border-slate-300 rounded-lg text-sm text-black placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#714B67] focus:border-transparent transition-all"
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
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full pl-9 pr-3 py-2 bg-white border border-slate-300 rounded-lg text-sm text-black placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#714B67] focus:border-transparent transition-all"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-2.5 px-4 rounded-lg bg-[#714B67] hover:bg-[#5d3d54] text-white font-semibold text-sm shadow-md transition-all flex items-center justify-center space-x-2 disabled:opacity-50 mt-2"
          >
            <span>{loading ? 'Authenticating...' : 'Sign In to Workspace'}</span>
            {!loading && <ArrowRight className="w-4 h-4" />}
          </button>
        </form>

        <div className="text-center mt-6 pt-4 border-t border-slate-200">
          <p className="text-xs text-slate-600">
            Don't have an enterprise account?{' '}
            <Link to="/register" className="font-bold text-[#714B67] hover:underline">
              Create one now
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;
