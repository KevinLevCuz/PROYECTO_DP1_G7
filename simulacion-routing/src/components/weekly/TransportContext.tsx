"use client";

import { createContext, useContext, ReactNode, useState, useRef, useEffect } from "react";
import type { Pedido, Camion, Planta, Bloqueo } from '../../lib/api';

interface TransportContextType {
  activeOrders: Pedido[];
  activeTrucks: Camion[];
  selectedOrder: Pedido | null;
  selectedTruck: Camion | null;
  pedidosTotales: number;
  pedidosEntregados: number;
  trucks: Camion[];
  setActiveOrders: (orders: Pedido[] | ((prev: Pedido[]) => Pedido[])) => void;
  setActiveTrucks: (trucks: Camion[]) => void;
  setSelectedOrder: (order: Pedido | null) => void;
  setSelectedTruck: (truck: Camion | null) => void;
  setPedidosTotales: (pedidos: number) => void;
  setPedidosEntregados: (pedidos: number) => void;
  setTrucks: (trucks: Camion[]) => void
  selectedPlanta: Planta | null;
  setSelectedPlanta: (planta: Planta | null) => void;
  setSelectedBloqueo: (bloqueo: Bloqueo | null) => void;
  
}

const TransportContext = createContext<TransportContextType | undefined>(undefined);

export function TransportProvider({ children }: { children: ReactNode }) {
  const [activeOrders, setActiveOrders] = useState<Pedido[]>([]);
  const [activeTrucks, setActiveTrucks] = useState<Camion[]>([]);
  const [selectedOrder, setSelectedOrder] = useState<Pedido | null>(null);
  const [selectedTruck, setSelectedTruck] = useState<Camion | null>(null);
  const [pedidosTotales, setPedidosTotales] = useState<number>(0);
  const [pedidosEntregados, setPedidosEntregados] = useState<number>(0);
  const [trucks, setTrucks] = useState<Camion[]>([]);
  const [selectedPlanta, setSelectedPlanta] = useState<Planta | null>(null);
  const [selectedBloqueo, setSelectedBloqueo] = useState<Bloqueo | null>(null);




  return (
    <TransportContext.Provider value={{
  activeOrders, 
  activeTrucks, 
  selectedOrder, 
  selectedTruck,
  pedidosTotales, 
  pedidosEntregados, 
  trucks, 
  selectedPlanta,
  setSelectedPlanta,
  setActiveOrders, 
  setActiveTrucks, 
  setSelectedOrder,
  setSelectedTruck,
  setPedidosTotales, 
  setPedidosEntregados, 
  setTrucks,
  setSelectedBloqueo
}}>
  {children}
</TransportContext.Provider>

  );
}

export function useTransport() {
  const context = useContext(TransportContext);
  if (!context) {
    throw new Error('useTransport must be used within a TransportProvider');
  }
  return context;
}