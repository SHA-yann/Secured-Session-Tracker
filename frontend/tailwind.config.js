/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.{html,ts}"],
  darkMode: 'class', // Pour un mode sombre moderne
  theme: {
    extend: {
      colors: {
        'auth-primary': '#1e293b', // Couleur pro pour la sécurité
      }
    },
  },
  plugins: [],
}