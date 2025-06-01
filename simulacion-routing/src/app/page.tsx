import SimulationMap from "@/components/SimulationMap";
import OrderList from "@/components/OrderList";
import Sidebar from "@/components/Sidebar";
import StatusBar from "@/components/StatusBar";
import Legend from "@/components/Legend";

export default function Home() {
  return (
    <div className="flex h-screen bg-gray-100">
      <Sidebar />
      
      <div className="flex-1 flex flex-col ml-12">
        <StatusBar />
        
        <div className="flex-1 relative overflow-hidden">
          <SimulationMap />
          <OrderList />
          <Legend />
        </div>
      </div>
    </div>
  );
}