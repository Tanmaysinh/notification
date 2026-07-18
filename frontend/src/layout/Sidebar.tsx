import { useState } from "react";
import { NavLink } from "react-router-dom";
import { NAV_ITEMS } from "./navConfig";

  import { useSecureSession } from "@/crypto/SecureSessionContext";

export default function Sidebar() {
  const [openMenu, setOpenMenu] = useState<string | null>("Template Management");

const { clearSession } = useSecureSession();



  return (
    <aside className="w-64 min-h-screen bg-[#171d2b] border-r border-white/10 flex flex-col">
      <div className="px-6 py-5 border-b border-white/10">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-lg bg-[#2c4a63] flex items-center justify-center">
            <span className="text-white font-bold text-sm">V</span>
          </div>
          <span className="text-white font-semibold text-sm">VasyERP</span>
        </div>
      </div>

      <nav className="flex-1 px-3 py-4 space-y-1">
        {NAV_ITEMS.map((item) => {
          if (item.children) {
            const isOpen = openMenu === item.label;
            return (
              <div key={item.label}>
                <button
                  type="button"
                  onClick={() => setOpenMenu(isOpen ? null : item.label)}
                  className="w-full flex items-center justify-between px-3 py-2.5 rounded-lg text-sm text-gray-300 hover:bg-white/5 hover:text-white transition"
                >
                  <span className="flex items-center gap-2.5">
                    <span>{item.icon}</span>
                    {item.label}
                  </span>
                  <span className={`text-xs transition-transform ${isOpen ? "rotate-90" : ""}`}>
                    ▸
                  </span>
                </button>

                {isOpen && (
                  <div className="ml-8 mt-1 space-y-0.5 border-l border-white/10 pl-3">
                    {item.children.map((child) => (
                      <NavLink
                        key={child.path}
                        to={child.path}
                        className={({ isActive }) =>
                          `block px-3 py-2 rounded-md text-sm transition ${
                            isActive
                              ? "bg-[#2c4a63] text-white"
                              : "text-gray-400 hover:bg-white/5 hover:text-white"
                          }`
                        }
                      >
                        {child.label}
                      </NavLink>
                    ))}
                  </div>
                )}
              </div>
            );
          }

          return (
            <NavLink
              key={item.path}
              to={item.path!}
              end={item.path === "/dashboard"}
              className={({ isActive }) =>
                `flex items-center gap-2.5 px-3 py-2.5 rounded-lg text-sm transition ${
                  isActive
                    ? "bg-[#2c4a63] text-white"
                    : "text-gray-300 hover:bg-white/5 hover:text-white"
                }`
              }
            >
              <span>{item.icon}</span>
              {item.label}
            </NavLink>
          );
        })}
      </nav>

      <div className="px-3 py-4 border-t border-white/10">
        <button
          type="button"
          onClick={() => {
            localStorage.removeItem("token");
            localStorage.removeItem("user");
            clearSession();
            window.location.href = "/login";
            }}
          className="w-full flex items-center gap-2.5 px-3 py-2.5 rounded-lg text-sm text-gray-400 hover:bg-white/5 hover:text-white transition"
        >
          <span>🚪</span>
          Log out
        </button>
      </div>
    </aside>
  );
}