"use client";
import { FiChevronLeft, FiChevronRight, FiX } from "react-icons/fi";
import { useEffect, useRef, useState, useCallback } from "react";
import type { Camion, RutaCamion } from '../lib/api';
import { obtenerRutasOptimizadas } from "../lib/api";

export default function VehicleList() {
  const [isOpen, setIsOpen] = useState(true);
  const [filter, setFilter] = useState({
    enRuta: true,
    disponible: true
  });

  const [codigoSearch, setCodigoSearch] = useState('');
  useEffect(() => {
    setCurrentPage(1);
  }, [filter, codigoSearch]);

  const [rutasCamiones, setRutasCamiones] = useState<RutaCamion[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  useEffect(() => {
    const fetchData = async () => {
      try {
        const apiData = await obtenerRutasOptimizadas();
        setRutasCamiones(apiData);
      } catch (err) {
        console.error(err);
      }
    };
    fetchData();
  }, []);

  const filteredCamiones = rutasCamiones
    .map(ruta => ruta.camion)
    .filter(camion => {
      if (camion.enRuta && !filter.enRuta) return false;
      if (!camion.enRuta && !filter.disponible) return false;

      if (codigoSearch.trim() !== '' && !camion.codigo.toLowerCase().includes(codigoSearch.toLowerCase())) {
        return false;
      }

      return true;
    });

  // Calcular los camiones para la página actual
  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentCamiones = filteredCamiones.slice(indexOfFirstItem, indexOfLastItem);
  const totalPages = Math.ceil(filteredCamiones.length / itemsPerPage);

  const toggleFilter = (key: keyof typeof filter) => {
    setFilter(prev => ({ ...prev, [key]: !prev[key] }));
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
          className="fixed right-0 top-1/2 transform -translate-y-1/2 bg-blue-500 text-white p-2 rounded-l-lg shadow-lg z-30"
        >
          <FiChevronLeft size={20} />
        </button>
      )}

      {/* Panel principal */}
      <div className={`fixed right-0 top-12 h-150 bg-white border-l shadow-lg transition-transform duration-300 z-20 ${isOpen ? 'translate-x-0' : 'translate-x-full'}`}
           style={{ width: '550px' }}>
        <div className="h-full flex flex-col">
          {/* Header */}
          <div className="bg-blue-500 text-white p-2 flex justify-between items-center">
            <h3 className="font-semibold">Lista de Vehículos</h3>
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
                <button className="bg-blue-300 text-white px-4 py-1 w-40 text-sm">Pedidos</button>
                <button className="bg-blue-500 text-white px-4 py-1 w-40 text-sm">Vehículos</button>
              </div>
              <div className="flex space-x-3 text-xs">
                <label className="flex items-center">
                  <input 
                    type="checkbox" 
                    checked={filter.enRuta} 
                    onChange={() => toggleFilter('enRuta')} 
                    className="mr-1"
                  /> En Ruta
                </label>
                <label className="flex items-center">
                  <input 
                    type="checkbox" 
                    checked={filter.disponible} 
                    onChange={() => toggleFilter('disponible')} 
                    className="mr-1"
                  /> Disponible
                </label>
              </div>
            </div>

            <input
              type="text"
              placeholder="Buscar por Código"
              className="border p-2 rounded w-full mb-4 text-sm"
              value={codigoSearch}
              onChange={(e) => setCodigoSearch(e.target.value)}
            />

            <div className="overflow-x-auto">
              <table className="w-full text-xs">
                <thead>
                  <tr className="text-left border-b text-gray-700">
                    <th className="p-2">Código</th>
                    <th className="p-2">Ubicación</th>
                    <th className="p-2">Capacidad</th>
                    <th className="p-2">GLP Actual</th>
                    <th className="p-2">Estado</th>
                    <th className="p-2">Disponible desde</th>
                    <th className="p-2">Ubicar</th>
                  </tr>
                </thead>
                <tbody>
                  {currentCamiones.map((camion) => (
                    <tr key={camion.codigo} className="border-b hover:bg-gray-50">
                      <td className="p-2">{camion.codigo}</td>
                      <td className="p-2">({camion.ubicacionActual.posX}, {camion.ubicacionActual.posY})</td>
                      <td className="p-2">{camion.capacidadMaxima}</td>
                      <td className="p-2">{camion.glpActual}</td>
                      <td className="p-2">
                        <span className={`px-2 py-1 rounded-full text-xs ${
                          camion.enRuta ? 'bg-blue-100 text-blue-800' : 'bg-green-100 text-green-800'
                        }`}>
                          {camion.enRuta ? 'En Ruta' : 'Disponible'}
                        </span>
                      </td>
                      <td className="p-2">{camion.disponibleDesde}</td>
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
                {indexOfFirstItem + 1} - {Math.min(indexOfLastItem, filteredCamiones.length)} de {filteredCamiones.length}
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