import { Routes, Route } from "react-router-dom";
import Layout from "../src/components/layout/Layout.jsx"; 
import Map from "./pages/map/Map.jsx";
import Record from "./pages/record/Record.jsx";
import Analysis from "./pages/analysis/Analysis.jsx";

function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<div>홈</div>} />
        <Route path="/map" element={<Map />} />
        <Route path="/record" element={<Record />} />
        <Route path="/analysis" element={<Analysis />} />
      </Route>
    </Routes>
  );
}

export default App;