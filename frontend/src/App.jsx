import { Routes, Route } from "react-router-dom";
import Layout from "./components/layout/Layout.jsx";
import My from "./pages/my/my.jsx";
import Map from "./pages/map/Map.jsx";
import Record from "./pages/record/Record.jsx";
import RecordDetail from "./pages/record/RecordDetail.jsx";
import TicketRegister from "./pages/record/register/TicketRegister.jsx";
import RegisterComplete from "./pages/record/register/RegisterComplete.jsx";
import Analysis from "./pages/analysis/Analysis.jsx";
import Home from "./pages/home/home.jsx";
import Detail from "./pages/home/detail.jsx";
import ActorReview from "./pages/home/actorReview.jsx";

function App() {
  return (
    <Routes>
      //네브바 o 
      <Route element={<Layout />}>
        <Route path="/" element={<Home />} />
        <Route path="/my" element={<My />} />
        <Route path="/map" element={<Map />} />
        <Route path="/record" element={<Record />} />
        <Route path="/analysis" element={<Analysis />} />
      </Route>

      //네브바 x
      <Route element={<Layout nav={false} />}>
        <Route path="/detail/:id" element={<Detail />} />
        <Route path="/detail/:id/actor/:name" element={<ActorReview />} />
      </Route>

      <Route path="/record/:id" element={<RecordDetail />} />
    </Routes>
  );
}

export default App;