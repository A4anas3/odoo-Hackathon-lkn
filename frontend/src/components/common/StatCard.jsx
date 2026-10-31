import React from 'react';

export const StatCard = ({ title, value, subtitle, icon: Icon, color = 'odoo', trend, onClick }) => {
  const colorMap = {
    odoo: 'bg-purple-50 text-[#714B67] border-purple-200',
    indigo: 'bg-indigo-50 text-indigo-700 border-indigo-200',
    emerald: 'bg-emerald-50 text-emerald-700 border-emerald-200',
    amber: 'bg-amber-50 text-amber-700 border-amber-200',
    rose: 'bg-rose-50 text-rose-700 border-rose-200',
    sky: 'bg-sky-50 text-sky-700 border-sky-200',
  };

  const iconStyle = colorMap[color] || colorMap.odoo;

  return (
    <div
      onClick={onClick}
      className={`bg-white rounded-xl border border-slate-200 p-5 shadow-xs hover:shadow-md transition-all ${
        onClick ? 'cursor-pointer hover:border-slate-300' : ''
      }`}
    >
      <div className="flex items-center justify-between">
        <div>
          <p className="text-xs font-medium uppercase tracking-wider text-slate-500">{title}</p>
          <h3 className="text-2xl font-bold text-black mt-1 tracking-tight">{value}</h3>
          {subtitle && <p className="text-xs text-slate-600 mt-1">{subtitle}</p>}
        </div>
        {Icon && (
          <div className={`w-12 h-12 rounded-xl flex items-center justify-center border ${iconStyle}`}>
            <Icon className="w-6 h-6" />
          </div>
        )}
      </div>
      {trend && (
        <div className="mt-3 pt-3 border-t border-slate-100 flex items-center text-xs">
          <span
            className={`font-semibold mr-1.5 ${
              trend.positive ? 'text-emerald-700' : 'text-rose-700'
            }`}
          >
            {trend.positive ? '↑' : '↓'} {trend.value}
          </span>
          <span className="text-slate-500">{trend.label || 'vs last month'}</span>
        </div>
      )}
    </div>
  );
};

export default StatCard;
