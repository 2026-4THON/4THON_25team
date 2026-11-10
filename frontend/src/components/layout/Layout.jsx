import { Outlet } from "react-router-dom";
import Nav from "../nav/nav.jsx";
import "./Layout.scss";

const Layout = () => {
  return (
    <div className="layout">
      <main className="layout__screen">
        <Outlet />
      </main>
      <Nav />
    </div>
  );
};

export default Layout;