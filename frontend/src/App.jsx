import { Routes, Route } from "react-router-dom";
import Layout from "../src/components/layout/Layout.jsx"; 
import Map from "./pages/map/Map.jsx";
import Record from "./pages/record/Record.jsx";
import RecordDetail from "./pages/record/RecordDetail.jsx";
import TicketRegister from "./pages/record/register/TicketRegister.jsx";
import RegisterComplete from "./pages/record/register/RegisterComplete.jsx";
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
      <Route path="/record/new" element={<TicketRegister />} />
      <Route path="/record/new/complete" element={<RegisterComplete />} />
      <Route path="/record/:id/edit" element={<TicketRegister mode="edit" />} />
      <Route path="/record/:id" element={<RecordDetail />} />
    </Routes>
  );
}

export default App;