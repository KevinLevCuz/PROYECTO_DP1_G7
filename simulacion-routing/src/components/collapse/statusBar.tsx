"use client";

import { useEffect, useState } from "react";
import { useSimTime } from "@/components/collapse/TimeContext";
import { useTransport } from "@/components/collapse/TransportContext";

export default function StatusBar() {
  const { simTime, realTimeFromStart, elapsedSimulatedTime, elapsedRealTime } = useSimTime();

  const { pedidosTotales, pedidosEntregados, trucks } = useTransport();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  const formatDateTime = (date: Date) => {
    if (!date) return "--/--/---- --:--:--";
    return `${date.toLocaleDateString("es-PE")} - ${date.toLocaleTimeString("es-PE", {
      hour12: false,
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })}`;
  };

  const formatDuration = (date: Date) => {
    const days = Math.floor(date.getTime() / (1000 * 60 * 60 * 24));
    const hours = date.getUTCHours().toString().padStart(2, '0');
    const minutes = date.getUTCMinutes().toString().padStart(2, '0');
    const seconds = date.getUTCSeconds().toString().padStart(2, '0');
    return `${days}d ${hours}:${minutes}:${seconds}`;
  };

  if (!mounted) return null;

  return (
    <div className="bg-red-500 text-white p-2 text-sm flex items-center justify-between gap-4 overflow-x-auto pl-9">
      {/* Tiempo Real */}
      <div className="whitespace-nowrap">
        <span className="font-semibold">Tiempo Real:</span> {formatDateTime(realTimeFromStart)}
      </div>

      {/* Tiempo Simulación */}
      <div className="whitespace-nowrap">
        <span className="font-semibold">⏱️ Tiempo Simulación:</span> {formatDateTime(simTime)}
      </div>

      {/* Avance */}
      <div className="whitespace-nowrap">
        <span className="font-semibold">Avance:</span> {formatDuration(elapsedSimulatedTime)}
      </div>

      {/* Transcurrido */}
      <div className="whitespace-nowrap">
        <span className="font-semibold">Transcurrido:</span> {formatDuration(elapsedRealTime)}
      </div>

      {/* Vehículos y Pedidos (en línea) */}
      <div className="whitespace-nowrap">
        <span className="font-semibold">🚚 Vehículos:</span> {trucks.filter(t => t.enRuta).length}/20
        <span className="mx-2">|</span>
        <span className="font-semibold">📦 Ped. Entregados:</span> {pedidosEntregados}/{pedidosTotales}
      </div>
    </div>
  );
}
