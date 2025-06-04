// components/TransportPanel.tsx
"use client";
import { FiChevronLeft, FiChevronRight, FiX } from "react-icons/fi";
import { useEffect, useState } from "react";
import type { Pedido, Camion, RutaCamion } from '../lib/api';
import { obtenerPedidos, obtenerRutasOptimizadas } from "../lib/api";


export default function TransportPanel() {
  const [isOpen, setIsOpen] = useState(false);
  const [showVehicles, setShowVehicles] = useState(false);
  
  // Estado para pedidos
  const [pedidos, setPedidos] = useState<Pedido[]>([]);
  const [pedidoFilter, setPedidoFilter] = useState({
    entregado: true,
    ruta: true,
    pendiente: true
  });
  const [clienteSearch, setClienteSearch] = useState('');
  
  // Estado para vehículos
  const [rutasCamiones, setRutasCamiones] = useState<RutaCamion[]>([]);
  const [vehiculoFilter, setVehiculoFilter] = useState({
    enRuta: true,
    disponible: true
  });
  const [codigoSearch, setCodigoSearch] = useState('');
  
  // Paginación común
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  // Obtener datos
  useEffect(() => {
    const fetchData = async () => {
      try {
        const [pedidosData, rutasData] = await Promise.all([
          obtenerPedidos(),
          obtenerRutasOptimizadas()
        ]);
        setPedidos(pedidosData);
        setRutasCamiones(rutasData);
      } catch (err) {
        console.error(err);
      }
    };
    fetchData();
  }, []);

  // Filtrado de pedidos
  const filteredPedidos = pedidos.filter(pedido => {
    if (pedido.estado === 'Entregado' && !pedidoFilter.entregado) return false;
    if (pedido.estado === 'Ruteando' && !pedidoFilter.ruta) return false;
    if (pedido.estado === 'Pendiente' && !pedidoFilter.pendiente) return false;

    if (clienteSearch.trim() !== '' && !pedido.idCliente.toString().toLowerCase().includes(clienteSearch.toLowerCase())) {
      return false;
    }

    return true;
  });

  // Filtrado de vehículos
  const filteredCamiones = rutasCamiones
    .map(ruta => ruta.camion)
    .filter(camion => {
      if (camion.enRuta && !vehiculoFilter.enRuta) return false;
      if (!camion.enRuta && !vehiculoFilter.disponible) return false;

      if (codigoSearch.trim() !== '' && !camion.codigo.toLowerCase().includes(codigoSearch.toLowerCase())) {
        return false;
      }

      return true;
    });

  // Calcular items para la página actual
  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  
  const currentItems = showVehicles 
    ? filteredCamiones.slice(indexOfFirstItem, indexOfLastItem)
    : filteredPedidos.slice(indexOfFirstItem, indexOfLastItem);
    
  const totalItems = showVehicles ? filteredCamiones.length : filteredPedidos.length;
  const totalPages = Math.ceil(totalItems / itemsPerPage);

  // Funciones comunes
  const resetPagination = () => setCurrentPage(1);
  
  const goToNextPage = () => {
    if (currentPage < totalPages) setCurrentPage(currentPage + 1);
  };

  const goToPrevPage = () => {
    if (currentPage > 1) setCurrentPage(currentPage - 1);
  };

  return (
    <>
      {/* Botón para abrir/cerrar */}
      {!isOpen && (
        <button 
          onClick={() => setIsOpen(true)}
          className={`fixed right-0 top-1/2 transform -translate-y-1/2 ${showVehicles ? 'bg-red-500' : 'bg-red-500'} text-white p-2 rounded-l-lg shadow-lg z-30`}
        >
          <FiChevronLeft size={20} />
        </button>
      )}

      {/* Panel principal */}
      <div className={`fixed right-0 top-12 h-150 bg-white border-l shadow-lg transition-transform duration-300 z-20 ${isOpen ? 'translate-x-0' : 'translate-x-full'}`}
           style={{ width: '550px' }}>
        <div className="h-full flex flex-col">
          {/* Header */}
          <div className={`${showVehicles ? 'bg-red-500' : 'bg-red-500'} text-white p-2 flex justify-between items-center`}>
            <h3 className="font-semibold">{showVehicles ? 'Lista de Vehículos' : 'Lista de Pedidos'}</h3>
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
                <button 
                  className={`px-4 py-1 w-40 text-sm ${!showVehicles ? 'bg-red-500' : 'bg-red-300'} text-white`}
                  onClick={() => {
                    setShowVehicles(false);
                    resetPagination();
                  }}
                >
                  Pedidos
                </button>
                <button 
                  className={`px-4 py-1 w-40 text-sm ${showVehicles ? 'bg-red-500' : 'bg-red-300'} text-white`}
                  onClick={() => {
                    setShowVehicles(true);
                    resetPagination();
                  }}
                >
                  Vehículos
                </button>
              </div>

              {/* Filtros según la vista */}
              {showVehicles ? (
                <div className="flex space-x-3 text-xs">
                  <label className="flex items-center">
                    <input 
                      type="checkbox" 
                      checked={vehiculoFilter.enRuta} 
                      onChange={() => {
                        setVehiculoFilter(prev => ({ ...prev, enRuta: !prev.enRuta }));
                        resetPagination();
                      }} 
                      className="mr-1"
                    /> En Ruta
                  </label>
                  <label className="flex items-center">
                    <input 
                      type="checkbox" 
                      checked={vehiculoFilter.disponible} 
                      onChange={() => {
                        setVehiculoFilter(prev => ({ ...prev, disponible: !prev.disponible }));
                        resetPagination();
                      }} 
                      className="mr-1"
                    /> Disponible
                  </label>
                </div>
              ) : (
                <div className="flex space-x-3 text-xs">
                  <label className="flex items-center">
                    <input 
                      type="checkbox" 
                      checked={pedidoFilter.entregado} 
                      onChange={() => {
                        setPedidoFilter(prev => ({ ...prev, entregado: !prev.entregado }));
                        resetPagination();
                      }} 
                      className="mr-1"
                    /> Entregado
                  </label>
                  <label className="flex items-center">
                    <input 
                      type="checkbox" 
                      checked={pedidoFilter.ruta} 
                      onChange={() => {
                        setPedidoFilter(prev => ({ ...prev, ruta: !prev.ruta }));
                        resetPagination();
                      }} 
                      className="mr-1"
                    /> Ruteando
                  </label>
                  <label className="flex items-center">
                    <input 
                      type="checkbox" 
                      checked={pedidoFilter.pendiente} 
                      onChange={() => {
                        setPedidoFilter(prev => ({ ...prev, pendiente: !prev.pendiente }));
                        resetPagination();
                      }} 
                      className="mr-1"
                    /> Pendiente
                  </label>
                </div>
              )}
            </div>

            {/* Barra de búsqueda */}
            <input
              type="text"
              placeholder={showVehicles ? "Buscar por Código" : "Buscar por Cliente"}
              className="border p-2 rounded w-full mb-4 text-sm"
              value={showVehicles ? codigoSearch : clienteSearch}
              onChange={(e) => {
                showVehicles 
                  ? setCodigoSearch(e.target.value)
                  : setClienteSearch(e.target.value);
                resetPagination();
              }}
            />

            {/* Tabla de contenido */}
            <div className="overflow-x-auto">
              <table className="w-full text-xs">
                <thead>
                  <tr className="text-left border-b text-gray-700">
                    {showVehicles ? (
                      <>
                        <th className="p-2">Código</th>
                        <th className="p-2">Ubicación</th>
                        <th className="p-2">Capacidad</th>
                        <th className="p-2">GLP Actual</th>
                        <th className="p-2">Estado</th>
                        <th className="p-2">Ubicar</th>
                      </>
                    ) : (
                      <>
                        <th className="p-2">ID</th>
                        <th className="p-2">Cliente</th>
                        <th className="p-2">Paquete</th>
                        <th className="p-2">L. Entrega</th>
                        <th className="p-2">F.H. Entrega</th>
                        <th className="p-2">Estado</th>
                        <th className="p-2">Ubicar</th>
                      </>
                    )}
                  </tr>
                </thead>
                <tbody>
                  {currentItems.map((item) => (
                    <tr key={showVehicles ? (item as Camion).codigo : (item as Pedido).id} className="border-b hover:bg-gray-50">
                      {showVehicles ? (
                        <>
                          <td className="p-2">{(item as Camion).codigo}</td>
                          <td className="p-2">({(item as Camion).ubicacionActual.posX}, {(item as Camion).ubicacionActual.posY})</td>
                          <td className="p-2">{(item as Camion).capacidadMaxima}</td>
                          <td className="p-2">{(item as Camion).glpActual}</td>
                          <td className="p-2">
                            <span className={`px-2 py-1 rounded-full text-xs ${
                              (item as Camion).enRuta ? 'bg-red-100 text-red-800' : 'bg-green-100 text-green-800'
                            }`}>
                              {(item as Camion).enRuta ? 'En Ruta' : 'Disponible'}
                            </span>
                          </td>
                          <td className="p-2">
                            <button className="text-gray-500 hover:text-gray-700">📍</button>
                          </td>
                        </>
                      ) : (
                        <>
                          <td className="p-2">{(item as Pedido).id}</td>
                          <td className="p-2">{(item as Pedido).idCliente}</td>
                          <td className="p-2">{(item as Pedido).cantidadGlp}</td>
                          <td className="p-2">({(item as Pedido).destino.posX} , {(item as Pedido).destino.posY})</td>
                          <td className="p-2">{(item as Pedido).plazoMaximoEntrega}</td>
                          <td className="p-2">
                            <span className={`px-2 py-1 rounded-full text-xs ${
                              (item as Pedido).estado === 'Entregado' ? 'bg-green-100 text-green-800' :
                              (item as Pedido).estado === 'Ruteando' ? 'bg-blue-100 text-blue-800' :
                              'bg-yellow-100 text-yellow-800'
                            }`}>
                              {(item as Pedido).estado}
                            </span>
                          </td>
                          <td className="p-2">
                            <button className="text-gray-500 hover:text-gray-700">📍</button>
                          </td>
                        </>
                      )}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Paginación */}
            <div className="flex justify-between items-center mt-2 text-xs text-gray-500">
              <div>
                {indexOfFirstItem + 1} - {Math.min(indexOfLastItem, totalItems)} de {totalItems}
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