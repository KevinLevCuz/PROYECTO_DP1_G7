"use client";

import { TimeProvider } from "@/components/daily/TimeContextDaily";
import { TransportProvider } from "@/components/daily/TransportContextDaily";

import SimulationMapDaily from "@/components/daily/SimulationMapDaily";
import Sidebar from "@/components/common/Sidebar";
import StatusBarDaily from "@/components/daily/StatusBarDaily";
import LegendDaily from "@/components/common/Legend";
import TransportPanelDaily from "@/components/daily/TransportPanelDaily";
import NewOrderPanel from "@/components/daily/NewOrderPanel";

export default function DailyView() {
  return (
    <TimeProvider>
      <TransportProvider> {/* <---- Aquí el envoltorio necesario */}
        <div className="flex h-screen bg-gray-100">
          <Sidebar />

          <div className="flex-1 flex flex-col ml-12">
            <StatusBarDaily />
            <div className="flex-1 relative overflow-hidden">
              <SimulationMapDaily />
              <NewOrderPanel />
              <TransportPanelDaily />
              <LegendDaily />
            </div>
          </div>
        </div>
      </TransportProvider>
    </TimeProvider>
  );
}
