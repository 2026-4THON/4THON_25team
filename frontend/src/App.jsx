import { Routes, Route } from "react-router-dom";
import Layout from "./components/layout/Layout.jsx";
import My from "./pages/my/my.jsx";
import Map from "./pages/map/Map.jsx";
import Record from "./pages/record/Record.jsx";
import RecordDetail from "./pages/record/RecordDetail.jsx";
import Analysis from "./pages/analysis/Analysis.jsx";
import Detail from "./pages/home/detail.jsx";


function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<div>홈</div>} />
        <Route path="/my" element={<My />} />
        <Route path="/map" element={<Map />} />
        <Route path="/record" element={<Record />} />
        <Route path="/analysis" element={<Analysis />} />
        <Route path="/detail/:id" element={<Detail />} />
      </Route>
      <Route path="/record/:id" element={<RecordDetail />} />
    </Routes>
  );
}

export default App;