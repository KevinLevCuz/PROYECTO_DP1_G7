// components/TransportPanel.tsx
"use client";
import { FiChevronLeft, FiChevronRight, FiX } from "react-icons/fi";
import { useEffect, useState } from "react";
import type { Pedido, Camion, Planta, Bloqueo } from '../../lib/api';
import { obtenerPedidos, obtenerCamiones, obtenerPlantas, obtenerBloqueos } from "../../lib/api";
import { useSimTime } from "@/components/weekly/TimeContext";
import { useTransport } from "@/components/weekly/TransportContext";

export default function TransportPanel() {
  const [isOpen, setIsOpen] = useState(false);
  const [activeTab, setActiveTab] = useState<'pedidos' | 'vehiculos' | 'plantas' | 'bloqueos'>('pedidos');
  const { simTime } = useSimTime();
  const {
    activeOrders,
    activeTrucks,
    pedidosEntregados,
    setSelectedOrder,
    setPedidosTotales,
    setPedidosEntregados,
    selectedTruck,
    setSelectedTruck,
    setSelectedPlanta,
    setSelectedBloqueo
  } = useTransport();
  const [todosLosPedidos, setTodosLosPedidos] = useState<Pedido[]>([]);

  // Estado para pedidos
  const [pedidos, setPedidos] = useState<Pedido[]>([]);
  const [pedidoFilter, setPedidoFilter] = useState({
    entregado: true,
    pendiente: true
  });
  const [clienteSearch, setClienteSearch] = useState('');

  // Estado para vehículos
  const [camiones, setCamiones] = useState<Camion[]>([]);
  const [vehiculoFilter, setVehiculoFilter] = useState({
    enRuta: true,
    disponible: true
  });
  const [codigoSearch, setCodigoSearch] = useState('');

  // Estado para plantas
  const [plantas, setPlantas] = useState<Planta[]>([]);
  const [plantaFilter, setPlantaFilter] = useState({
    disponible: true,
    bajaCapacidad: true
  });
  const [plantaSearch, setPlantaSearch] = useState('');

  // Estado para bloqueos
  const [bloqueos, setBloqueos] = useState<Bloqueo[]>([]);
  const [bloqueoFilter, setBloqueoFilter] = useState({
    activo: true,
    futuro: true,
    finalizado:true
  });
  const [bloqueoSearch, setBloqueoSearch] = useState('');

  // Paginación común
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  useEffect(() => {
    const actualizarPedidos = () => {
      setTodosLosPedidos(prev => {
        const actualizados = prev.map(pedidoExistente => {
          const actualizado = activeOrders.find(p => p.id === pedidoExistente.id);
          return actualizado ? actualizado : pedidoExistente;
        });

        const nuevosPedidos = activeOrders.filter(nuevo =>
          !prev.some(existente => existente.id === nuevo.id)
        );

        return [...actualizados, ...nuevosPedidos];
      });
    };

    actualizarPedidos();
  }, [activeOrders]);

  // Obtener datos
  useEffect(() => {
    const fetchData = async () => {
      try {
        setPedidos(activeOrders);
        setCamiones(activeTrucks);
        const plantasData = await obtenerPlantas();
        setPlantas(plantasData);
        const bloqueosData = await obtenerBloqueos();
        setBloqueos(bloqueosData);
      } catch (err) {
        console.error(err);
      }
    };
    fetchData();
  }, [activeOrders, activeTrucks]);

  const filteredPedidos = todosLosPedidos.filter(pedido => {
    const pedidoTime = new Date(pedido.horaPedido).getTime();
    const currentSimTime = simTime.getTime();
    if (pedidoTime > currentSimTime) {
      return false;
    }

    if (pedido.entregado === true && !pedidoFilter.entregado) return false;
    if (pedido.entregado === false && !pedidoFilter.pendiente) return false;

    if (clienteSearch.trim() !== '' && !pedido.idCliente.toString().toLowerCase().includes(clienteSearch.toLowerCase()) && !pedido.id?.toString().toLowerCase().includes(clienteSearch.toLowerCase())) {
      return false;
    }

    return true;
  });

  const filteredCamiones = camiones.filter(camion => {
    if (camion.enRuta && !vehiculoFilter.enRuta) return false;
    if (!camion.enRuta && !vehiculoFilter.disponible) return false;

    if (codigoSearch.trim() !== '' && !camion.codigo.toLowerCase().includes(codigoSearch.toLowerCase())) {
      return false;
    }

    return true;
  });

  const filteredPlantas = plantas.filter(planta => {
    // Filtro por capacidad (baja capacidad = menos del 20%)
    if (planta.glpDisponible / planta.capacidadMaxima < 0.2 && !plantaFilter.bajaCapacidad) return false;
    if (planta.glpDisponible / planta.capacidadMaxima >= 0.2 && !plantaFilter.disponible) return false;

    if (plantaSearch.trim() !== '' && !planta.id.toLowerCase().includes(plantaSearch.toLowerCase())) {
      return false;
    }

    return true;
  });

  const filteredBloqueos = bloqueos.filter(bloqueo => {
  // Verifica que tenga fechas válidas
  if (!bloqueo.inicio || !bloqueo.fin) return false;
  
  const ahora = simTime.getTime();
  const inicio = new Date(bloqueo.inicio).getTime();
  const fin = new Date(bloqueo.fin).getTime();

  // Si las fechas no son válidas, descartar
  if (isNaN(inicio) || isNaN(fin)) return false;

  const esActivo = ahora >= inicio && ahora <= fin;
  const esFuturo = ahora < inicio;
  const esFinalizado = ahora > fin;

  // Aplicar filtros
  if (esActivo && !bloqueoFilter.activo) return false;
  if (esFuturo && !bloqueoFilter.futuro) return false;
  if (esFinalizado && !bloqueoFilter.finalizado) return false;
  
  // Mostrar finalizados solo si no hay otros filtros activos
  if (esFinalizado && (bloqueoFilter.activo || bloqueoFilter.futuro)) return false;

  // Filtro de búsqueda
  if (bloqueoSearch.trim() !== '' && 
      !bloqueo.nodos.some(nodo => 
        `(${nodo.posX},${nodo.posY})`.includes(bloqueoSearch.toLowerCase())
      )) {
    return false;
  }

  return true;
});

  const getCurrentItems = () => {
    const indexOfLastItem = currentPage * itemsPerPage;
    const indexOfFirstItem = indexOfLastItem - itemsPerPage;
    
    switch(activeTab) {
      case 'pedidos':
        return filteredPedidos.slice(indexOfFirstItem, indexOfLastItem);
      case 'vehiculos':
        return filteredCamiones.slice(indexOfFirstItem, indexOfLastItem);
      case 'plantas':
        return filteredPlantas.slice(indexOfFirstItem, indexOfLastItem);
      case 'bloqueos':
        return filteredBloqueos.slice(indexOfFirstItem, indexOfLastItem);
      default:
        return [];
    }
  };

  const getTotalItems = () => {
    switch(activeTab) {
      case 'pedidos':
        return filteredPedidos.length;
      case 'vehiculos':
        return filteredCamiones.length;
      case 'plantas':
        return filteredPlantas.length;
      case 'bloqueos':
        return filteredBloqueos.length;
      default:
        return 0;
    }
  };

  const currentItems = getCurrentItems();
  const totalItems = getTotalItems();

  useEffect(() => {
    setPedidosTotales(filteredPedidos.length);
    setPedidosEntregados(todosLosPedidos.filter(pedido => pedido.entregado).length);
  }, [todosLosPedidos, filteredPedidos]);

  const totalPages = Math.ceil(totalItems / itemsPerPage);

  const resetPagination = () => setCurrentPage(1);

  const goToNextPage = () => {
    if (currentPage < totalPages) setCurrentPage(currentPage + 1);
  };

  const goToPrevPage = () => {
    if (currentPage > 1) setCurrentPage(currentPage - 1);
  };

  const renderTabButtons = () => (
    <div className="flex mb-2">
      <button
        className={`px-4 py-1 w-32 text-sm ${activeTab === 'pedidos' ? 'bg-red-500' : 'bg-red-300'} text-white`}
        onClick={() => {
          setActiveTab('pedidos');
          resetPagination();
        }}
      >
        Pedidos
      </button>
      <button
        className={`px-4 py-1 w-32 text-sm ${activeTab === 'vehiculos' ? 'bg-red-500' : 'bg-red-300'} text-white`}
        onClick={() => {
          setActiveTab('vehiculos');
          resetPagination();
        }}
      >
        Vehículos
      </button>
      <button
        className={`px-4 py-1 w-32 text-sm ${activeTab === 'plantas' ? 'bg-red-500' : 'bg-red-300'} text-white`}
        onClick={() => {
          setActiveTab('plantas');
          resetPagination();
        }}
      >
        Plantas
      </button>
      <button
        className={`px-4 py-1 w-32 text-sm ${activeTab === 'bloqueos' ? 'bg-red-500' : 'bg-red-300'} text-white`}
        onClick={() => {
          setActiveTab('bloqueos');
          resetPagination();
        }}
      >
        Bloqueos
      </button>
    </div>
  );

  const renderStatusBar = () => {
    switch(activeTab) {
      case 'pedidos':
        return (
          <div className="flex items-center gap-2">
            <div className="flex-1 bg-gray-200 rounded-full h-2.5">
              <div
                className="bg-blue-500 h-2.5 rounded-full"
                style={{
                  width: `${todosLosPedidos.length > 0
                      ? (todosLosPedidos.filter(p => p.entregado).length / todosLosPedidos.length) * 100
                      : 0
                    }%`,
                  maxWidth: '100%',
                }}
              ></div>
            </div>
            <span className="text-xs font-medium whitespace-nowrap">
              {todosLosPedidos.filter(p => p.entregado).length}/{todosLosPedidos.length} pedidos entregados (
              {todosLosPedidos.length > 0
                ? Math.round(
                  (todosLosPedidos.filter(p => p.entregado).length / todosLosPedidos.length) * 100
                )
                : 0}
              %)
            </span>
          </div>
        );
      case 'vehiculos':
        return (
          <div className="flex items-center gap-2">
            <div className="flex-1 bg-gray-200 rounded-full h-2.5">
              <div
                className="bg-green-500 h-2.5 rounded-full"
                style={{
                  width: `${camiones.length > 0
                      ? (camiones.filter(t => !t.enRuta).length / camiones.length) * 100
                      : 0
                    }%`,
                  maxWidth: '100%',
                }}
              ></div>
            </div>
            <span className="text-xs font-medium whitespace-nowrap">
              {camiones.filter(t => !t.enRuta).length}/{camiones.length} camiones disponibles (
              {camiones.length > 0
                ? Math.round(
                  (camiones.filter(t => !t.enRuta).length / camiones.length) * 100
                )
                : 0}
              %)
            </span>
          </div>
        );
      case 'plantas':
        return (
          <div className="flex items-center gap-2">
            <div className="flex-1 bg-gray-200 rounded-full h-2.5">
              <div
                className="bg-yellow-500 h-2.5 rounded-full"
                style={{
                  width: `${plantas.length > 0
                      ? (plantas.filter(p => p.glpDisponible / p.capacidadMaxima >= 0.2).length / plantas.length) * 100
                      : 0
                    }%`,
                  maxWidth: '100%',
                }}
              ></div>
            </div>
            <span className="text-xs font-medium whitespace-nowrap">
              {plantas.filter(p => p.glpDisponible / p.capacidadMaxima >= 0.2).length}/{plantas.length} plantas con capacidad (
              {plantas.length > 0
                ? Math.round(
                  (plantas.filter(p => p.glpDisponible / p.capacidadMaxima >= 0.2).length / plantas.length) * 100
                )
                : 0}
              %)
            </span>
          </div>
        );
      case 'bloqueos':
        const ahora = simTime.getTime();
        const bloqueosActivos = bloqueos.filter(b => {
          const inicio = new Date(b.inicio).getTime();
          const fin = new Date(b.fin).getTime();
          if(ahora >= inicio && ahora <= fin){
            b.estado = 'Activo';
            return true;
          } else if(ahora>fin){
            b.estado = 'Finalizado';
          } else if (ahora < fin){
            b.estado = 'Futuro';
          }
          return false;
        });
        
        return (
          <div className="flex items-center gap-2">
            <div className="flex-1 bg-gray-200 rounded-full h-2.5">
              <div
                className="bg-purple-500 h-2.5 rounded-full"
                style={{
                  width: `${bloqueos.length > 0
                      ? (bloqueosActivos.length / bloqueos.length) * 100
                      : 0
                    }%`,
                  maxWidth: '100%',
                }}
              ></div>
            </div>
            <span className="text-xs font-medium whitespace-nowrap">
              {bloqueosActivos.length}/{bloqueos.length} bloqueos activos (
              {bloqueos.length > 0
                ? Math.round(
                  (bloqueosActivos.length / bloqueos.length) * 100
                )
                : 0}
              %)
            </span>
          </div>
        );
      default:
        return null;
    }
  };

  const renderFilters = () => {
    switch(activeTab) {
      case 'pedidos':
        return (
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
                checked={pedidoFilter.pendiente}
                onChange={() => {
                  setPedidoFilter(prev => ({ ...prev, pendiente: !prev.pendiente }));
                  resetPagination();
                }}
                className="mr-1"
              /> Pendiente
            </label>
          </div>
        );
      case 'vehiculos':
        return (
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
        );
      case 'plantas':
        return (
          <div className="flex space-x-3 text-xs">
            <label className="flex items-center">
              <input
                type="checkbox"
                checked={plantaFilter.disponible}
                onChange={() => {
                  setPlantaFilter(prev => ({ ...prev, disponible: !prev.disponible }));
                  resetPagination();
                }}
                className="mr-1"
              /> Capacidad OK
            </label>
            <label className="flex items-center">
              <input
                type="checkbox"
                checked={plantaFilter.bajaCapacidad}
                onChange={() => {
                  setPlantaFilter(prev => ({ ...prev, bajaCapacidad: !prev.bajaCapacidad }));
                  resetPagination();
                }}
                className="mr-1"
              /> Baja Capacidad
            </label>
          </div>
        );
      case 'bloqueos':
        return (
          <div className="flex space-x-3 text-xs">
            <label className="flex items-center">
              <input
                type="checkbox"
                checked={bloqueoFilter.activo}
                onChange={() => {
                  setBloqueoFilter(prev => ({ ...prev, activo: !prev.activo }));
                  resetPagination();
                }}
                className="mr-1"
              /> Activos
            </label>
            <label className="flex items-center">
              <input
                type="checkbox"
                checked={bloqueoFilter.futuro}
                onChange={() => {
                  setBloqueoFilter(prev => ({ ...prev, futuro: !prev.futuro }));
                  resetPagination();
                }}
                className="mr-1"
              /> Futuros
            </label>
            <label className="flex items-center">
              <input
                type="checkbox"
                checked={bloqueoFilter.finalizado}
                onChange={() => {
                  setBloqueoFilter(prev => ({ ...prev, finalizado: !prev.finalizado }));
                  resetPagination();
                }}
                className="mr-1"
              /> Finalizados
            </label>
          </div>
        );
      default:
        return null;
    }
  };

  const renderSearchPlaceholder = () => {
    switch(activeTab) {
      case 'pedidos':
        return "Buscar por ID Pedido o ID Cliente";
      case 'vehiculos':
        return "Buscar por Código";
      case 'plantas':
        return "Buscar por ID Planta";
      case 'bloqueos':
        return "Buscar por coordenadas (x,y)";
      default:
        return "";
    }
  };

  const renderSearchValue = () => {
    switch(activeTab) {
      case 'pedidos':
        return clienteSearch;
      case 'vehiculos':
        return codigoSearch;
      case 'plantas':
        return plantaSearch;
      case 'bloqueos':
        return bloqueoSearch;
      default:
        return "";
    }
  };

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    switch(activeTab) {
      case 'pedidos':
        setClienteSearch(value);
        break;
      case 'vehiculos':
        setCodigoSearch(value);
        break;
      case 'plantas':
        setPlantaSearch(value);
        break;
      case 'bloqueos':
        setBloqueoSearch(value);
        break;
    }
    resetPagination();
  };

  const renderTableHeaders = () => {
    switch(activeTab) {
      case 'pedidos':
        return (
          <>
            <th className="p-2">ID</th>
            <th className="p-2">Cliente</th>
            <th className="p-2">Paquete</th>
            <th className="p-2">L. Entrega</th>
            <th className="p-2">F.H. Pedido</th>
            <th className="p-2">F.H.Maximo</th>
            <th className="p-2">Estado</th>
            <th className="p-2">Ubicar</th>
          </>
        );
      case 'vehiculos':
        return (
          <>
            <th className="p-2">Código</th>
            <th className="p-2">Ubicación</th>
            <th className="p-2">Capacidad</th>
            <th className="p-2">GLP Actual</th>
            <th className="p-2">Estado</th>
            <th className="p-2">Ubicar</th>
          </>
        );
      case 'plantas':
        return (
          <>
            <th className="p-2">ID</th>
            <th className="p-2">Ubicación</th>
            <th className="p-2">Cap. Máxima</th>
            <th className="p-2">GLP Disponible</th>
            <th className="p-2">% Capacidad</th>
            <th className="p-2">Siguiente Recarga</th>
            <th className="p-2">Ubicar</th>
          </>
        );
      case 'bloqueos':
        return (
          <>
            <th className="p-2">Nodos</th>
            <th className="p-2">Inicio</th>
            <th className="p-2">Fin</th>
            <th className="p-2">Estado</th>
            <th className="p-2">Ubicar</th>
          </>
        );
      default:
        return null;
    }
  };

  const renderTableRows = () => {
    return currentItems.map((item) => {
      switch(activeTab) {
        case 'pedidos':
          const pedido = item as Pedido;
          return (
            <tr key={pedido.id} className="border-b hover:bg-gray-50">
              <td className="p-2">{pedido.id}</td>
              <td className="p-2">{pedido.idCliente}</td>
              <td className="p-2">{pedido.cantidadGlp}</td>
              <td className="p-2">({pedido.destino.posX} , {pedido.destino.posY})</td>
              <td className="p-2">{pedido.horaPedido}</td>
              <td className="p-2">{pedido.plazoMaximoEntrega}</td>
              <td className="p-2">
                <span className={`px-2 py-1 rounded-full text-xs ${pedido.entregado === false ? 'bg-green-100 text-green-800' :
                  pedido.entregado === true ? 'bg-blue-100 text-blue-800' :
                    'bg-yellow-100 text-yellow-800'
                  }`}>
                  {pedido.entregado === false ? 'Pendiente' : 'Entregado'}
                </span>
              </td>
              <td className="p-2">
                <button
                  className="text-gray-500 hover:text-gray-700"
                  onClick={() => {
                    setSelectedOrder(pedido);
                    setTimeout(() => setSelectedOrder(null), 3000);
                  }}
                >
                  📍
                </button>
              </td>
            </tr>
          );
        case 'vehiculos':
          const camion = item as Camion;
          return (
            <tr key={camion.codigo} className="border-b hover:bg-gray-50">
              <td className="p-2">{camion.codigo}</td>
              <td className="p-2">({camion.ubicacionActual.posX}, {camion.ubicacionActual.posY})</td>
              <td className="p-2">{camion.capacidadMaxima}</td>
              <td className="p-2">{camion.glpActual}</td>
              <td className="p-2">
                <span className={`px-2 py-1 rounded-full text-xs ${camion.enRuta ? 'bg-red-100 text-red-800' : camion.estado=='ND'? 'bg-yellow-100 text-yellow-800' : 'bg-green-100 text-green-800'
                  }`}>
                  {camion.enRuta ? 'En Ruta' : camion.estado=='ND' ? 'Mantenimiento' : 'Disponible'}
                </span>
              </td>
              <td className="p-2">
                <button
                  className="text-gray-500 hover:text-gray-700"
                  onClick={() => {
                    setSelectedTruck(camion);
                    setTimeout(() => setSelectedTruck(null), 3000);
                  }}
                >
                  📍
                </button>
              </td>
            </tr>
          );
        case 'plantas':
          const planta = item as Planta;
          const porcentajeCapacidad = (planta.glpDisponible / planta.capacidadMaxima) * 100;
          return (
            <tr key={planta.id} className="border-b hover:bg-gray-50">
              <td className="p-2">{planta.id}</td>
              <td className="p-2">({planta.ubicacion.posX}, {planta.ubicacion.posY})</td>
              <td className="p-2">{planta.capacidadMaxima}</td>
              <td className="p-2">{planta.glpDisponible}</td>
              <td className="p-2">
                <div className="w-full bg-gray-200 rounded-full h-2.5">
                  <div 
                    className={`h-2.5 rounded-full ${porcentajeCapacidad < 20 ? 'bg-red-600' : 'bg-green-600'}`} 
                    style={{width: `${porcentajeCapacidad}%`}}
                  ></div>
                </div>
                <span className="text-xs">{Math.round(porcentajeCapacidad)}%</span>
              </td>
              <td className="p-2">{planta.siguienteRecarga}</td>
              <td className="p-2">
                <button
                  className="text-gray-500 hover:text-gray-700"
                  onClick={() => {
                    setSelectedPlanta(planta);
                    setTimeout(() => setSelectedPlanta(null), 3000);
                  }}
                >
                  📍
                </button>
              </td>
            </tr>
          );
        case 'bloqueos':
          const bloqueo = item as Bloqueo;
          const ahora = simTime.getTime();
          const inicio = new Date(bloqueo.inicio).getTime();
          const fin = new Date(bloqueo.fin).getTime();
          let estado = '';
          let estadoClass = '';
          
          if (ahora >= inicio && ahora <= fin) {
            bloqueo.estado = 'Activo';
            estado = 'Activo';
            estadoClass = 'bg-red-100 text-red-800';

          } else if (inicio > ahora) {
            estado = 'Futuro';
            bloqueo.estado = 'Futuro';
            estadoClass = 'bg-yellow-100 text-yellow-800';
          } else {
            bloqueo.estado = 'Finalizado';
            estado = 'Finalizado';
            estadoClass = 'bg-gray-100 text-gray-800';
          }
          
          return (
            <tr key={`${bloqueo.inicio}-${bloqueo.fin}`} className="border-b hover:bg-gray-50">
              <td className="p-2">
                {bloqueo.nodos.map((nodo, i) => (
                  <div key={i}>({nodo.posX}, {nodo.posY})</div>
                ))}
              </td>
              <td className="p-2">{bloqueo.inicio}</td>
              <td className="p-2">{bloqueo.fin}</td>
              <td className="p-2">
                <span className={`px-2 py-1 rounded-full text-xs ${estadoClass}`}>
                  {estado}
                </span>
              </td>
              <td className="p-2">
                <button
                  className="text-gray-500 hover:text-gray-700"
                  onClick={() => {
                    setSelectedBloqueo(bloqueo);
                    setTimeout(() => setSelectedBloqueo(null), 3000);
                  }}
                >
                  📍
                </button>
              </td>
            </tr>
          );
        default:
          return null;
      }
    });
  };

  return (
    <>
      {/* Botón para abrir/cerrar */}
      {!isOpen && (
        <button
          onClick={() => setIsOpen(true)}
          className={`fixed right-0 top-1/2 transform -translate-y-1/2 bg-red-500 text-white p-2 rounded-l-lg shadow-lg z-30`}
        >
          <FiChevronLeft size={20} />
        </button>
      )}

      {/* Panel principal */}
      <div className={`fixed right-0 top-12 h-190 bg-white border-l shadow-lg transition-transform duration-300 z-20 ${isOpen ? 'translate-x-0' : 'translate-x-full'}`}
        style={{ width: '650px' }}>
        <div className="h-full flex flex-col">
          {/* Header */}
          <div className={`bg-red-500 text-white p-2 flex justify-between items-center`}>
            <h3 className="font-semibold">
              {activeTab === 'pedidos' && 'Lista de Pedidos'}
              {activeTab === 'vehiculos' && 'Lista de Vehículos'}
              {activeTab === 'plantas' && 'Lista de Plantas'}
              {activeTab === 'bloqueos' && 'Lista de Bloqueos'}
            </h3>
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
              {renderTabButtons()}
              <div className="w-full mb-2">
                {renderStatusBar()}
              </div>
              {renderFilters()}
            </div>

            {/* Barra de búsqueda */}
            <input
              type="text"
              placeholder={renderSearchPlaceholder()}
              className="border p-2 rounded w-full mb-4 text-sm"
              value={renderSearchValue()}
              onChange={handleSearchChange}
            />

            {/* Tabla de contenido */}
            <div className="overflow-x-auto">
              <table className="w-full text-xs">
                <thead>
                  <tr className="text-left border-b text-gray-700">
                    {renderTableHeaders()}
                  </tr>
                </thead>
                <tbody>
                  {renderTableRows()}
                </tbody>
              </table>
            </div>

            {/* Paginación */}
            <div className="flex justify-between items-center mt-2 text-xs text-gray-500">
              <div>
                {Math.min((currentPage - 1) * itemsPerPage + 1, totalItems)} - {Math.min(currentPage * itemsPerPage, totalItems)} de {totalItems}
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
