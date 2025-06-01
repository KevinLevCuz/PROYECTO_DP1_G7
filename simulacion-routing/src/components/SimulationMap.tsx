"use client";

import Legend from "./Legend";
import { BsPlayFill, BsStopFill } from "react-icons/bs";
import { FiMaximize2, FiMinimize2 } from "react-icons/fi";
import { useState } from "react";

export default function SimulationMap() {
  const [isLegendExpanded, setIsLegendExpanded] = useState(true);

  return (
    <div className="min-h-screen bg-gray-200 relative">
      {/* Controles de simulación */}
      <div className="absolute top-4 left-16 z-10 flex gap-2">
        <div className="flex items-end gap-2">
          <div>
            <label className="block text-sm font-medium mb-1">Fecha y Hora de Inicio</label>
            <div className="flex items-center border rounded px-2 py-1 bg-white">
              <input
                type="datetime-local"
                className="text-sm outline-none border-none bg-transparent"
                placeholder="dd:mm:aaaa hh:mm"
              />
              <span className="text-gray-400 ml-2">📅</span>
            </div>
          </div>
          
          <button className="w-8 h-8 rounded-full bg-teal-500 text-white flex items-center justify-center">
            <BsPlayFill className="w-4 h-4" />
          </button>

          <button className="w-8 h-8 rounded-full border-2 border-red-500 text-red-500 flex items-center justify-center">
            <BsStopFill className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Mapa */}
      <div className="absolute inset-0 flex items-center justify-center">
        <p className="text-gray-500">Mapa de simulación aparecerá aquí</p>
      </div>
    </div>
  );
}