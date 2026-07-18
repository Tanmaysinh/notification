// /** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        vasy: {
          navy: "#2c4a63",
          dark: "#0f1420",
          panel: "#171d2b",
        },
      },
    },
  },
  plugins: [],
};