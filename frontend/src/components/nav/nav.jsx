import { useNavigate, useLocation } from "react-router-dom";

import mapIcon from "../../assets/nav/map.svg";
import recordIcon from "../../assets/nav/record.svg";
import homeIcon from "../../assets/nav/home.svg";
import analysisIcon from "../../assets/nav/analysis.svg";
import myIcon from "../../assets/nav/my.svg";

import "./nav.scss";

const ITEMS = [
  { path: "/map", label: "지도", icon: mapIcon },
  { path: "/record", label: "기록", icon: recordIcon },
  { path: "/", label: "홈", icon: homeIcon },
  { path: "/analysis", label: "분석", icon: analysisIcon },
  { path: "/my", label: "마이", icon: myIcon },
];

const Nav = () => {
  const navigate = useNavigate();
  const { pathname } = useLocation();

  const isActive = (path) =>
    path === "/" ? pathname === "/" : pathname.startsWith(path);

  return (
    <nav className="bottom-nav">
      <div className="bottom-nav-menu">
        {ITEMS.map(({ path, label, icon }) => (
          <button
            key={path}
            type="button"
            className={isActive(path) ? "bottom-nav-item active" : "bottom-nav-item"}
            onClick={() => navigate(path)}
          >
            <span
              className="bottom-nav-icon"
              style={{ "--icon": `url("${icon}")` }}
              aria-hidden="true"
            />
            <span className="bottom-nav-label">{label}</span>
          </button>
        ))}
      </div>
    </nav>
  );
};

export default Nav;