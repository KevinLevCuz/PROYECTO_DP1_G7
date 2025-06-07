import SimulationMap from "@/components/weekly/SimulationMap";
import Sidebar from "@/components/common/Sidebar";
import StatusBar from "@/components/weekly/StatusBar";
import Legend from "@/components/common/Legend";

import TransportPanel from "@/components/weekly/TransportPanel";

export default function Home() {
  return (
    <div className="flex h-screen bg-gray-100">
      <Sidebar />
      
      <div className="flex-1 flex flex-col ml-12">
        <StatusBar />
        
        <div className="flex-1 relative overflow-hidden">
          <SimulationMap />
          <TransportPanel />
          <Legend />
        </div>
      </div>
    </div>
  );
}