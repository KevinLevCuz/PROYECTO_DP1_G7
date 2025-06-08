import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  eslint: {
    // ⛔ Evita que ESLint bloquee la build
    ignoreDuringBuilds: true,
  },
};

export default nextConfig;
