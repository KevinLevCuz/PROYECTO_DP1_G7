import SimulationMap from "@/components/weekly/SimulationMap";
import Sidebar from "@/components/common/Sidebar";
import StatusBar from "@/components/weekly/StatusBar";
import Legend from "@/components/common/Legend";
import { TimeProvider } from "@/components/weekly/TimeContext";
import TransportPanel from "@/components/weekly/TransportPanel";
import { TransportProvider } from "@/components/weekly/TransportContext"; // Asegúrate de que la ruta es correcta

export default function Home() {
  return (
    <TimeProvider>
      <TransportProvider> {/* El orden de los providers es importante */}
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
      </TransportProvider>
    </TimeProvider>
  );
}