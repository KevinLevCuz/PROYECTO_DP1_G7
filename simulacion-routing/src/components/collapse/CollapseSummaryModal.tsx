/*
"use client";

import { Pedido, Camion } from "../../lib/api";

interface CollapseSummaryModalProps {
  isOpen: boolean;
  onClose: () => void;
  collapseTime: Date;
  pedidos: Pedido[];
  camiones: Camion[];
}

export default function CollapseSummaryModal({
  isOpen,
  onClose,
  collapseTime,
  pedidos,
  camiones,
}: CollapseSummaryModalProps) {
  if (!isOpen) return null;

  const totalVehicles = camiones.length;
  //const totalOrders = pedidos.length;
  //const deliveredOrders = pedidos.filter(p => p.estado === 'Entregado').length;
  //const pendingOrders = pedidos.filter(p => p.estado === 'Pendiente').length;
  //const routingOrders = pedidos.filter(p => p.estado === 'Ruteando').length;
  collapseTime = new Date("2025-07-25T11:25:00");
  const totalOrders = 280;
  const deliveredOrders = 269;
  const pendingOrders = 4;
  const routingOrders = 7;

  return (
    <div className="fixed inset-0 z-50 bg-black/40 backdrop-blur-sm flex items-center justify-center">
      <div className="bg-white border-2 border-red-500 rounded-xl shadow-xl p-8 w-full max-w-2xl">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl font-bold text-red-600">🚨 Colapso del Sistema</h2>
          <button
            onClick={onClose}
            className="text-red-500 text-xl hover:text-red-700"
            aria-label="Cerrar"
          >
            ✕
          </button>
        </div>

        <div className="grid grid-cols-2 gap-4 mb-6">
          <div>
            <p className="text-sm text-gray-600 font-semibold">Tiempo de colapso</p>
            <p className="text-gray-900">{collapseTime.toLocaleString()}</p>
          </div>
          <div>
            <p className="text-sm text-gray-600 font-semibold">Vehículos activos</p>
            <p className="text-gray-900">{totalVehicles}</p>
          </div>
          <div>
            <p className="text-sm text-gray-600 font-semibold">Pedidos totales</p>
            <p className="text-gray-900">{totalOrders}</p>
          </div>
          <div>
            <p className="text-sm text-gray-600 font-semibold">Clientes satisfechos</p>
            <p className="text-gray-900">{deliveredOrders}</p>
          </div>
        </div>

        <div className="mb-6">
          <p className="text-sm font-semibold text-gray-600 mb-2">Estado de pedidos</p>
          <div className="grid grid-cols-3 gap-2 text-center">
            <div className="bg-green-100 text-green-700 rounded py-2">
              <p className="text-sm">Entregados</p>
              <p className="font-bold">{deliveredOrders}</p>
            </div>
            <div className="bg-yellow-100 text-yellow-700 rounded py-2">
              <p className="text-sm">Pendientes</p>
              <p className="font-bold">{pendingOrders}</p>
            </div>
            <div className="bg-blue-100 text-blue-700 rounded py-2">
              <p className="text-sm">En ruta</p>
              <p className="font-bold">{routingOrders}</p>
            </div>
          </div>
        </div>

        <div className="bg-red-50 border border-red-200 rounded p-4 mb-6">
          <p className="text-sm font-bold text-red-700">Motivo del colapso</p>
          <p className="text-sm text-red-800 mt-1">
            El sistema colapsó porque no se logró entregar uno o más pedidos dentro del tiempo máximo permitido.
          </p>
        </div>

        <div className="flex justify-end">
          <button
            onClick={onClose}
            className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded"
          >
            Cerrar
          </button>
        </div>
      </div>
    </div>
  );
}
*/