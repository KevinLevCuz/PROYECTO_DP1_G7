"use client";
import { useState } from "react";
import { FiChevronLeft, FiChevronRight, FiX } from "react-icons/fi";

export default function OrderList() {
  const [isOpen, setIsOpen] = useState(true);
  const [filter, setFilter] = useState({
    entregado: true,
    ruta: true,
    pendiente: true
  });

  const pedidos = [
    { id: 1, cliente: 'K. Levano', paquete: '5m³', lugar: '(12,5)', fecha: '06/05 11:58', estado: 'Ruteando' },
    { id: 2, cliente: 'J. Gutierrez', paquete: '15m³', lugar: '(10,45)', fecha: '06/05 11:58', estado: 'Ruteando' },
    { id: 3, cliente: 'B. Morales', paquete: '5m³', lugar: '(6,9)', fecha: '06/05 11:58', estado: 'Entregado' },
    { id: 4, cliente: 'K. Levano', paquete: '15m³', lugar: '(12,6)', fecha: '06/05 11:58', estado: 'Pendiente' },
    { id: 5, cliente: 'J. Gutierrez', paquete: '5m³', lugar: '(10,26)', fecha: '06/05 11:58', estado: 'Ruteando' },
    { id: 6, cliente: 'B. Morales', paquete: '15m³', lugar: '(6,17)', fecha: '06/05 11:58', estado: 'Ruteando' },
    { id: 7, cliente: 'K. Levano', paquete: '5m³', lugar: '(12,15)', fecha: '06/05 11:58', estado: 'Entregado' },
    { id: 8, cliente: 'J. Gutierrez', paquete: '5m³', lugar: '(10,26)', fecha: '06/05 11:58', estado: 'Pendiente' },
    { id: 9, cliente: 'B. Morales', paquete: '15m³', lugar: '(6,12)', fecha: '06/05 11:58', estado: 'Entregado' },
    { id: 10, cliente: 'K. Levano', paquete: '5m³', lugar: '(12,15)', fecha: '06/05 11:58', estado: 'Pendiente' },
  ];

  const filteredPedidos = pedidos.filter(pedido => {
    if (pedido.estado === 'Entregado' && !filter.entregado) return false;
    if (pedido.estado === 'Ruteando' && !filter.ruta) return false;
    if (pedido.estado === 'Pendiente' && !filter.pendiente) return false;
    return true;
  });

  const toggleFilter = (key: keyof typeof filter) => {
    setFilter(prev => ({ ...prev, [key]: !prev[key] }));
  };

  return (
    <>
      {/* Botón para abrir/cerrar */}
      {!isOpen && (
        <button 
          onClick={() => setIsOpen(true)}
          className="fixed right-0 top-1/2 transform -translate-y-1/2 bg-red-500 text-white p-2 rounded-l-lg shadow-lg z-30"
        >
          <FiChevronLeft size={20} />
        </button>
      )}

      {/* Panel principal */}
      <div className={`fixed right-0 top-12 h-150 bg-white border-l shadow-lg transition-transform duration-300 z-20 ${isOpen ? 'translate-x-0' : 'translate-x-full'}`}
           style={{ width: '550px' }}>
        <div className="h-full flex flex-col">
          {/* Header */}
          <div className="bg-red-500 text-white p-2 flex justify-between items-center">
            <h3 className="font-semibold">Lista de Pedidos</h3>
            <button 
              onClick={() => setIsOpen(false)}
              className="text-white hover:text-gray-200"
            >
              <FiX size={20} />
            </button>
          </div>

          {/* Contenido */}
          <div className="p-4 flex-1 overflow-y-auto">
            <div className="flex flex-col items-center mb-4">
              <div className="flex mb-2">
                <button className="bg-red-500 text-white px-4 py-1 w-40 text-sm">Pedidos</button>
                <button className="bg-red-300 text-white px-4 py-1 w-40 text-sm">Vehículos</button>
              </div>
              <div className="flex space-x-3 text-xs">
                <label className="flex items-center">
                  <input 
                    type="checkbox" 
                    checked={filter.entregado} 
                    onChange={() => toggleFilter('entregado')} 
                    className="mr-1"
                  /> Entregado
                </label>
                <label className="flex items-center">
                  <input 
                    type="checkbox" 
                    checked={filter.ruta} 
                    onChange={() => toggleFilter('ruta')} 
                    className="mr-1"
                  /> Ruteando
                </label>
                <label className="flex items-center">
                  <input 
                    type="checkbox" 
                    checked={filter.pendiente} 
                    onChange={() => toggleFilter('pendiente')} 
                    className="mr-1"
                  /> Pendiente
                </label>
              </div>
            </div>

            <input
              type="text"
              placeholder="Buscar por Cliente"
              className="border p-2 rounded w-full mb-4 text-sm"
            />

            <div className="overflow-x-auto">
              <table className="w-full text-xs">
                <thead>
                  <tr className="text-left border-b text-gray-700">
                    <th className="p-2">ID</th>
                    <th className="p-2">Cliente</th>
                    <th className="p-2">Paquete</th>
                    <th className="p-2">L. Entrega</th>
                    <th className="p-2">F.H. Entrega</th>
                    <th className="p-2">Estado</th>
                    <th className="p-2">Ubicar</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredPedidos.map((pedido) => (
                    <tr key={pedido.id} className="border-b hover:bg-gray-50">
                      <td className="p-2">{pedido.id}</td>
                      <td className="p-2">{pedido.cliente}</td>
                      <td className="p-2">{pedido.paquete}</td>
                      <td className="p-2">{pedido.lugar}</td>
                      <td className="p-2">{pedido.fecha}</td>
                      <td className="p-2">
                        <span className={`px-2 py-1 rounded-full text-xs ${
                          pedido.estado === 'Entregado' ? 'bg-green-100 text-green-800' :
                          pedido.estado === 'Ruteando' ? 'bg-blue-100 text-blue-800' :
                          'bg-yellow-100 text-yellow-800'
                        }`}>
                          {pedido.estado}
                        </span>
                      </td>
                      <td className="p-2">
                        <button className="text-gray-500 hover:text-gray-700">📍</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="flex justify-between items-center mt-2 text-xs text-gray-500">
              <div>1 - 10 de 35</div>
              <div className="flex space-x-2">
                <button className="px-2 py-1 border rounded">Anterior</button>
                <button className="px-2 py-1 border rounded bg-gray-200">Siguiente</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}