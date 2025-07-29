
"use client";

import { useState, useEffect } from "react";
import { useSimTime } from "@/components/daily/TimeContextDaily";
import { useTransport } from "@/components/daily/TransportContextDaily";

export default function StatusBar() {
  const [tiempoReal, setTiempoReal] = useState<Date | null>(null);
    const [tiempoSimulacion, setTiempoSimulacion] = useState<Date | null>(null);
    const [vehiculos, setVehiculos] = useState(20);
    const { activeOrders, activeTrucks, pedidosTotales, pedidosEntregados, trucks} = useTransport();
    const [pedidos, setPedidos] = useState({ entregados: 0, total: 3 });
    const { simTime } = useSimTime();
    const [mounted, setMounted] = useState(false);
    

  useEffect(() => {
    const now = new Date();
    setTiempoReal(now);
    setTiempoSimulacion(now);

    const intervalReal = setInterval(() => {
      setTiempoReal(new Date());
    }, 1000);

    return () => {
      clearInterval(intervalReal);
    };
  }, []);

  const formatFechaHora = (dateObj: Date | null) => {
    if (!dateObj) return "--/--/---- --:--:--";
    const partesFecha = dateObj.toLocaleDateString("es-ES");
    const partesHora = dateObj.toLocaleTimeString("es-ES", { hour12: false });
    return `${partesFecha} - ${partesHora}`;
  };

  return (
    <div className="bg-red-500 text-white p-2 text-sm grid grid-cols-3 gap-4 justify-center">
      <div className="text-center">
        <span className="font-semibold">Tiempo Real:</span> {formatFechaHora(tiempoReal)}
      </div>
      <div className="text-center">
        <span className="font-semibold">Vehículos:</span> {vehiculos}
      </div>
      <div className="text-center">
        <span className="font-semibold">Ped. Entregados:</span>{" "}
        {`${pedidosEntregados}/${pedidosTotales}`}
      </div>
    </div>
  );
}
