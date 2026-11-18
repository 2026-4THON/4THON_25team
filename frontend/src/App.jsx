import { Routes, Route, Navigate,Outlet} from "react-router-dom";
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
import Explore from "./pages/home/explore.jsx";
import Search from "./pages/home/search.jsx";
import Start from "./pages/start/start.jsx";
import { isLoggedIn } from "./auth.js";

function RequireAuth() {
  return isLoggedIn() ? <Outlet /> : <Navigate to="/start" replace />;
}
import AnalysisSurvey from "./pages/analysis/survey/AnalysisSurvey.jsx";

function App() {
  return (
    <Routes>
      <Route element={<Layout nav={false} />}>
        <Route
          path="/start"
          element={isLoggedIn() ? <Navigate to="/" replace /> : <Start />}
        />
      </Route>

      {/* 네브바 o */}
      <Route element={<Layout />}>
        <Route path="/" element={<Home />} />
        <Route path="/my" element={<My />} />
        <Route path="/map" element={<Map />} />
        <Route path="/record" element={<Record />} />
        <Route path="/analysis" element={<Analysis />} />
      </Route>

      {/* 네브바 x */}
      <Route element={<Layout nav={false} />}>
        <Route path="/search" element={<Search />} />
        <Route path="/explore" element={<Explore />} />
        <Route path="/detail/:id" element={<Detail />} />
        <Route path="/detail/:id/actor/:name" element={<ActorReview />} />
      </Route>

      <Route path="/record/new" element={<TicketRegister />} />
      <Route path="/record/new/complete" element={<RegisterComplete />} />
      <Route path="/record/:id/edit" element={<TicketRegister mode="edit" />} />
      <Route path="/record/:id" element={<RecordDetail />} />
      <Route path="/analysis/survey" element={<AnalysisSurvey />} />
    </Routes>
  );
}

export default App;