/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,jsx}",
  ],
  theme: {
    extend: {
      colors: {
        odoo: {
          50: '#fbf8fa',
          100: '#f4edf2',
          200: '#eadbe5',
          300: '#dbbed3',
          400: '#c59ab9',
          500: '#a77599',
          600: '#8c5d7f',
          700: '#714B67', // Odoo Signature Purple
          800: '#5e3e56',
          900: '#50374a',
          950: '#331f2f',
        },
        primary: {
          DEFAULT: '#714B67',
          hover: '#5d3d54',
          light: '#fbf9fa',
        }
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
      },
    },
  },
  plugins: [],
};
