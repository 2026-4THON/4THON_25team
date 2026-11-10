import { Routes, Route } from "react-router-dom";
import Layout from "../src/components/layout/Layout.jsx"; 
import My from "../src/pages/my/my.jsx";


function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<div>홈</div>} />
        <Route path="/my" element={<My />} />
      </Route>
    </Routes>
  );
}

export default App;