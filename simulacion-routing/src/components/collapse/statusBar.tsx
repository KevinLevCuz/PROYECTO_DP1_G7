"use client";

import { useState, useEffect } from "react";
import { useSimTime } from "@/components/collapse/TimeContext";
import { useTransport } from "@/components/collapse/TransportContext";

export default function StatusBar() {
  const [tiempoReal, setTiempoReal] = useState<Date | null>(null);
  const [tiempoSimulacion, setTiempoSimulacion] = useState<Date | null>(null);
  const [vehiculos, setVehiculos] = useState(20);
  const { activeOrders, activeTrucks, pedidosTotales, pedidosEntregados, trucks} = useTransport();
  const [pedidos, setPedidos] = useState({ entregados: 0, total: 3 });
  const { simTime } = useSimTime();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

const fetchLoop = async () => {
  setInterval(() => {
    setPedidos({ entregados: activeOrders.length, total: 3 });
  }, 1000)
}

  useEffect(() => {
    const now = new Date();
    const fechaBase = new Date();
    fechaBase.setHours(0, 0, 0, 0);
    setTiempoReal(now);
    setTiempoSimulacion(fechaBase);

    const intervalReal = setInterval(() => {
      setTiempoReal(new Date());
    }, 1000);

    const intervalSim = setInterval(() => {
      setTiempoSimulacion((prev) =>
        prev ? new Date(prev.getTime() + 1000) : new Date()
      );
    }, 1000);

    return () => {
      clearInterval(intervalReal);
      clearInterval(intervalSim);
    };
  }, []);

  const formatFechaHora1 = (dateObj: Date | null) => {
    if (!dateObj) return "--/--/---- --:--:--";
    const partesFecha = dateObj.toLocaleDateString("es-ES");
    const partesHora = dateObj.toLocaleTimeString("es-ES", { hour12: false });
    return `${partesFecha} - ${partesHora}`;
  };

  const formatFechaHora = (date: Date) =>
    `${date.toLocaleDateString("es-PE")} - ${date.toLocaleTimeString("es-PE", { hour12: false })}`;

  if (!mounted) return null;

  return (
    <div className="bg-red-500 text-white p-2 text-sm grid grid-cols-4 gap-4 justify-center">
      <div className="text-center">
        <span className="font-semibold">Tiempo Real:</span> {formatFechaHora1(tiempoReal)}
      </div>
      <div className="text-center">
        <div>
          ⏱️ Tiempo Simulación:{" "}
          {simTime.toLocaleDateString()} - {simTime.toLocaleTimeString("es-PE", { hour12: false })}
        </div>
      </div>
      <div className="text-center">
        <span className="font-semibold">Vehículos:</span>{ `${trucks.filter((truck) => truck.enRuta).length}/20`}
      </div>
      <div className="text-center">
        <span className="font-semibold">Ped. Entregados:</span>{" "}
        {`${pedidosEntregados}/${pedidosTotales}`}
      </div>
    </div>
  );
}