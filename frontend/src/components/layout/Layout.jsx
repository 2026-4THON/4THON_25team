import { Outlet } from 'react-router-dom'

import Nav from "../nav/nav.jsx";

import './Layout.scss'

const Layout = () => {
  return (
    <div className="layout">
      <Outlet />
      <Nav />
    </div>
  )
}

export default Layout