"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  FiHome,
  FiTruck,
  FiSettings,
  FiBarChart2,
  FiMap,
} from "react-icons/fi";

export default function Sidebar() {
  const pathname = usePathname();

  const navItems = [
    { href: "/", icon: <FiHome size={20} />, label: "Semanal" },
    { href: "/daily", icon: <FiMap size={20} />, label: "Diario" }
  ];

  return (
    <div className="bg-white text-red w-16 h-full fixed left-0 top-0 flex flex-col items-center py-6 z-40 shadow-lg">
      <div className="space-y-6 flex flex-col items-center">
        {navItems.map((item) => (
          <Link
            key={item.href}
            href={item.href}
            className={`p-3 rounded-full transition-colors duration-300 ${
              pathname === item.href
                ? "bg-white text-red-700 shadow-md"
                : "hover:bg-red-600 hover:text-white"
            }`}
            title={item.label}
          >
            {item.icon}
          </Link>
        ))}
      </div>
    </div>
  );
}
