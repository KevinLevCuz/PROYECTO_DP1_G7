"use client";

import { createContext, useContext, ReactNode, useState } from "react";
import type { Pedido, Camion } from '../../lib/api';

interface TransportContextType {
  activeOrders: Pedido[];
  activeTrucks: Camion[];
  selectedOrder: Pedido | null;
  pedidosTotales: number;
  pedidosEntregados: number;
  trucks: Camion[];
  setActiveOrders: (orders: Pedido[] | ((prev: Pedido[]) => Pedido[])) => void; // Añade soporte para función
  setActiveTrucks: (trucks: Camion[]) => void;
  setSelectedOrder: (order: Pedido | null) => void;
  setPedidosTotales: (pedidos: number) => void;
  setPedidosEntregados: (pedidos: number) => void;
  setTrucks: (trucks: Camion[]) => void;
}

const TransportContext = createContext<TransportContextType | undefined>(undefined);

export function TransportProvider({ children }: { children: ReactNode }) {
  const [activeOrders, setActiveOrders] = useState<Pedido[]>([]);
  const [activeTrucks, setActiveTrucks] = useState<Camion[]>([]);
  const [selectedOrder, setSelectedOrder] = useState<Pedido | null>(null);
  const [pedidosTotales, setPedidosTotales] = useState<number>(0);
  const [pedidosEntregados, setPedidosEntregados] = useState<number>(0);
  const [trucks, setTrucks] = useState<Camion[]>([]);
  return (
    <TransportContext.Provider value={{ activeOrders, activeTrucks, selectedOrder, pedidosTotales, pedidosEntregados, trucks, setActiveOrders, setActiveTrucks, setSelectedOrder, setPedidosTotales, setPedidosEntregados, setTrucks}}>
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