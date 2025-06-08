import SimulationMap from "@/components/SimulationMap";
import Sidebar from "@/components/Sidebar";
import StatusBar from "@/components/StatusBar";
import Legend from "@/components/Legend";
import { TimeProvider } from "@/components/TimeContext";

import TransportPanel from "@/components/TransportPanel";

export default function Home() {
  return (
    <TimeProvider>
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
    </TimeProvider>
  );
}