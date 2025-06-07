// app/daily/page.tsx
import SimulationMapDaily from "@/components/daily/SimulationMapDaily";
import Sidebar from "@/components/common/Sidebar";
import StatusBarDaily from "@/components/daily/StatusBarDaily";
import LegendDaily from "@/components/common/Legend";
import TransportPanel from "@/components/daily/TransportPanelDaily";        
import NewOrderPanel from "@/components/daily/NewOrderPanel";      

export default function DailyView() {
  return (
    <div className="flex h-screen bg-gray-100">
      <Sidebar />
      
      <div className="flex-1 flex flex-col ml-12">
        <StatusBarDaily />
        
        <div className="flex-1 relative overflow-hidden">
          <SimulationMapDaily />
          <NewOrderPanel />
          <TransportPanel />
          <LegendDaily />
        </div>
      </div>
    </div>
  );
}