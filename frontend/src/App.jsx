import { Routes, Route } from "react-router-dom";
<<<<<<< HEAD
import Layout from "./components/layout/Layout.jsx";
import My from "./pages/my/my.jsx";
=======
import Layout from "../src/components/layout/Layout.jsx"; 
>>>>>>> cf50b8b3b589d092cbfd948ee95f07ca5d4757f9
import Map from "./pages/map/Map.jsx";
import Record from "./pages/record/Record.jsx";
import RecordDetail from "./pages/record/RecordDetail.jsx";
import Analysis from "./pages/analysis/Analysis.jsx";

function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<div>홈</div>} />
<<<<<<< HEAD
        <Route path="/my" element={<My />} />
=======
>>>>>>> cf50b8b3b589d092cbfd948ee95f07ca5d4757f9
        <Route path="/map" element={<Map />} />
        <Route path="/record" element={<Record />} />
        <Route path="/analysis" element={<Analysis />} />
      </Route>
      <Route path="/record/:id" element={<RecordDetail />} />
    </Routes>
  );
}

export default App;