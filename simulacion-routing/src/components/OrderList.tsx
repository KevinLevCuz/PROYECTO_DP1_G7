"use client";
import { FiChevronLeft, FiChevronRight, FiX } from "react-icons/fi";
import { useEffect, useRef, useState, useCallback } from "react";
import type { Pedido } from '../lib/api';

import { obtenerPedidos } from "../lib/api";

export default function OrderList() {
  const [isOpen, setIsOpen] = useState(true);
  const [filter, setFilter] = useState({
    entregado: true,
    ruta: true,
    pendiente: true
  });

  const [pedidos, setPedidos] = useState<Pedido[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  useEffect(() => {
    const fetchData = async () => {
      try {
        const apiData = await obtenerPedidos();
        setPedidos(apiData);
      } catch (err) {
        console.error(err);
      }
    };
    fetchData();
  }, []);

  const filteredPedidos = pedidos.filter(pedido => {
    if (pedido.estado === 'Entregado' && !filter.entregado) return false;
    if (pedido.estado === 'Ruteando' && !filter.ruta) return false;
    if (pedido.estado === 'Pendiente' && !filter.pendiente) return false;
    return true;
  });

  // Calcular los pedidos para la página actual
  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentPedidos = filteredPedidos.slice(indexOfFirstItem, indexOfLastItem);
  const totalPages = Math.ceil(filteredPedidos.length / itemsPerPage);

  const toggleFilter = (key: keyof typeof filter) => {
    setFilter(prev => ({ ...prev, [key]: !prev[key] }));
    // Resetear a la primera página cuando cambian los filtros
    setCurrentPage(1);
  };

  const goToNextPage = () => {
    if (currentPage < totalPages) {
      setCurrentPage(currentPage + 1);
    }
  };

  const goToPrevPage = () => {
    if (currentPage > 1) {
      setCurrentPage(currentPage - 1);
    }
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
                  {currentPedidos.map((pedido) => (
                    <tr key={pedido.id} className="border-b hover:bg-gray-50">
                      <td className="p-2">{pedido.id}</td>
                      <td className="p-2">{pedido.idCliente}</td>
                      <td className="p-2">{pedido.cantidadGlp}</td>
                      <td className="p-2">({pedido.destino.posX} , {pedido.destino.posY})</td>
                      <td className="p-2">{pedido.plazoMaximoEntrega}</td>
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
              <div>
                {indexOfFirstItem + 1} - {Math.min(indexOfLastItem, filteredPedidos.length)} de {filteredPedidos.length}
              </div>
              <div className="flex space-x-2">
                <button 
                  onClick={goToPrevPage}
                  disabled={currentPage === 1}
                  className={`px-2 py-1 border rounded ${currentPage === 1 ? 'opacity-50 cursor-not-allowed' : ''}`}
                >
                  Anterior
                </button>
                <button 
                  onClick={goToNextPage}
                  disabled={currentPage === totalPages}
                  className={`px-2 py-1 border rounded ${currentPage === totalPages ? 'opacity-50 cursor-not-allowed' : 'bg-gray-200'}`}
                >
                  Siguiente
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}