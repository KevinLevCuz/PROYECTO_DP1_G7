"use client";

import { useState, useEffect } from "react";

export default function StatusBar() {
  const [tiempoReal, setTiempoReal] = useState<Date | null>(null);
  const [tiempoSimulacion, setTiempoSimulacion] = useState<Date | null>(null);
  const [vehiculos, setVehiculos] = useState(20);
  const [pedidosEntregados, setPedidosEntregados] = useState({ entregados: 20, total: 722 });

  useEffect(() => {
    const now = new Date();
    setTiempoReal(now);
    setTiempoSimulacion(now);

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

  const formatFechaHora = (dateObj: Date | null) => {
    if (!dateObj) return "--/--/---- --:--:--";
    const partesFecha = dateObj.toLocaleDateString("es-ES");
    const partesHora = dateObj.toLocaleTimeString("es-ES", { hour12: false });
    return `${partesFecha} - ${partesHora}`;
  };

  return (
    <div className="bg-red-500 text-white p-2 text-sm grid grid-cols-4 gap-4 justify-center">
      <div className="text-center">
        <span className="font-semibold">Tiempo Real:</span> {formatFechaHora(tiempoReal)}
      </div>
      <div className="text-center">
        <span className="font-semibold">Tiempo Simulación:</span> {formatFechaHora(tiempoSimulacion)}
      </div>
      <div className="text-center">
        <span className="font-semibold">Vehículos:</span> {vehiculos}
      </div>
      <div className="text-center">
        <span className="font-semibold">Ped. Entregados:</span>{" "}
        {`${pedidosEntregados.entregados}/${pedidosEntregados.total}`}
      </div>
    </div>
  );
}
