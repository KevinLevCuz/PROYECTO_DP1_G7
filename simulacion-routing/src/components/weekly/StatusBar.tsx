/*"use client";

import { useState, useEffect } from "react";
import { useSimTime } from "@/components/weekly/TimeContext";
import { useTransport } from "@/components/weekly/TransportContext";

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
*/
"use client";

import { useEffect, useState } from "react";
import { useSimTime } from "@/components/weekly/TimeContext";
import { useTransport } from "@/components/weekly/TransportContext";

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
