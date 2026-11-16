import { Outlet } from "react-router-dom";
import Nav from "../nav/nav.jsx";
import "./Layout.scss";

const Layout = ({ nav = true }) => {
  return (
    <div className={nav ? "layout" : "layout layout--no-nav"}>
      <main className="layout__screen">
        <Outlet />
      </main>
      {nav && <Nav />}
    </div>
  );
};

export default Layout;