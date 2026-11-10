import { Routes, Route } from "react-router-dom";
import Layout from "../src/components/layout/Layout.jsx"; 

function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<div>홈</div>} />
      </Route>
    </Routes>
  );
}

export default App;