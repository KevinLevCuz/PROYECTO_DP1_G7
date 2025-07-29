import { conectarSimulacionSemanal } from "./websocketSimulacion";
import { desconectarSimulacion } from "./websocketSimulacion";
import { conectarSimulacionDiaDia } from "./websocketSimulacion";
// lib/api.ts
export interface Ubicacion {
  posX: number;
  posY: number;
  bloqueado: boolean;
}

export interface Planta {
  id: string;
  ubicacion: Ubicacion;
  capacidadMaxima: number;
  glpDisponible: number;
  siguienteRecarga: string;
  intervaloRecarga: string;
  canvasPosition?: { x: number; y: number; size: number };
}

export interface Pedido {
  id: string | null;
  destino: Ubicacion;
  cantidadGlp: number;
  horaPedido: string;
  plazoMaximoEntrega: string;
  tiempoDescarga: number | null;
  entregado: boolean;
  idCliente: string;
  estado?: string;  // Opcional si lo agregas después
  canvasPosition?: { x: number; y: number; size: number };
  horaSiguientePedido: string;
  sigId: string | null;
};


export interface SubRuta {
  inicio: Ubicacion;
  fin: Ubicacion;
  pedido: Pedido | null;
  trayectoria: Ubicacion[];
  horaInicio: string;
  horaFin: string;
  tiemposNodo: String[];
}

export interface Bloqueo{
  nodos: Ubicacion[];
  inicio: string;
  fin: string;
  estado: String;
}


export interface Camion {
  codigo: string;
  ubicacionActual: Ubicacion;
  capacidadMaxima: number;
  glpActual: number;
  enRuta: boolean;
  disponibleDesde: string;
  horaLibre?: String;
  SubRutasExistentes?: String;
  canvasPosition?: { x: number; y: number};
  estado: String;
}

export interface RutaCamion {
  camion: Camion;
  subRutas: SubRuta[];
}

export interface Bloqueo {
  nodos: Ubicacion[];
  inicio: string;
  fin: string;
}
export interface Solucion {
  planesCamion: RutaCamion[];
  costo: number;
}

export interface CamionStats {
  codigo: string;
  totalEntregados: number;
  porcentajeGlobal: number;
}

export interface PedidoStats {
  id: number;
  horaPedido: string;
  plazoMaximoEntrega: string;
  horaEntrega: string;
  tiempoDisponible: number;
  tiempoReal: number;
  porcentajeUtilizado: number;
}

export interface CamionResumen {
  codigo: string;
  pedidosAtendidos: number;
  glpConsumido: number;
  promedioGlpPorPedido: number;
}

export interface PedidoResumen {
  totalPedidos: number;
  entregados: number;
  pendientes: number;
  promedioGlpPorPedido: number;
  porcentajeCumplimiento: number;
  promedioTiempoEntregaMin: number;
  totalGlpEntregado: number;
}



export async function obtenerSimulacionSemanal(fechaInicio: string, fechaVariable: string): Promise<Solucion | null> {
  try {
    const response = await fetch('/api/routing/simulacionSemanal', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ahora: fechaInicio, fechaVariable }),
    });

    if (!response.ok) {
      throw new Error(`Error HTTP: ${response.status}`);
    }

    // Leer como texto primero
    const text = await response.text();

    // Si está vacío, retornamos null
    if (!text) {
      console.warn('⚠️ Respuesta vacía del backend');
      return null;
    }

    // Si tiene contenido, lo parseamos a JSON
    const data = JSON.parse(text);
    return data;
  } catch (error) {
    console.error('Error al obtener simulación:', error);
    return null; // O puedes relanzarlo con `throw error` si quieres detener el loop
  }
}


export async function monitoreoDiario(
  fechaInicio: string,
  fechaVariable: string,
  cont: number
): Promise<Solucion | null> {
  try {
    const response = await fetch('/api/routing/monitoreoDiario', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ahora: fechaInicio, fechaVariable, cont }),
    });

    if (!response.ok) {
      throw new Error(`Error HTTP: ${response.status}`);
    }

    const text = await response.text();

    if (!text) {
      console.warn('⚠ Respuesta vacía del backend (monitoreoDiario)');
      return null;
    }

    const data = JSON.parse(text);
    return data;
  } catch (error) {
    console.error('Error al obtener monitoreo diario:', error);
    return null;
  }
}



export async function obtenerRutasOptimizadas(
  ahora: string,
  pedidos: Pedido[],
  camiones: Camion[]
): Promise<RutaCamion[]> {
  try {
    console.log("Payload enviado:", { ahora, pedidos, camiones });

    const response = await fetch('/api/routing/optimize', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ahora, pedidos, camiones }),
    });

    if (!response.ok) {
      throw new Error(`Error: ${response.status}`);
    }

    const data = await response.json();
    return data.planesCamion;
  } catch (error) {
    console.error('Error fetching optimized routes:', error);
    throw error;
  }
}

export async function obtenerPedidos(): Promise<Pedido[]> {
  try {
    const response = await fetch('/api/routing/obtenerPedidos', { method: 'POST' });

    if (!response.ok) {
      throw new Error(`Error: ${response.status}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Error fetching optimized routes:', error);
    throw error;
  }
}

export async function obtenerPlantas(): Promise<Planta[]> {
  try {
    const response = await fetch('/api/routing/obtenerPlantas', { method: 'POST' });

    if (!response.ok) {
      throw new Error(`Error: ${response.status}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Error fetching optimized routes:', error);
    throw error;
  }
}

export async function obtenerCamiones(): Promise<Camion[]> {
  try {
    const response = await fetch('/api/routing/obtenerCamiones', { method: 'POST' });

    if (!response.ok) {
      throw new Error(`Error: ${response.status}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Error fetching optimized routes:', error);
    throw error;
  }
}

export async function obtenerBloqueos(): Promise<Bloqueo[]> {
  console.log('Iniciando obtención de bloqueos...');
  try {

    const response = await fetch('/api/routing/obtenerBloqueos', { method: 'POST' });

    if (!response.ok) {
      throw new Error(`Error: ${response.status}`);
    }

    const data = await response.json();
    console.log(data)
    return data;
  } catch (error) {
    console.error('Error fetching optimized routes:', error);
    throw error;
  }


}

export async function obtenerEstadisticasCamiones(): Promise<CamionStats[]> {
  try {
    const response = await fetch('/api/estadisticas/camiones');

    if (!response.ok) {
      throw new Error(`Error al obtener camiones: ${response.status}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Error al obtener estadísticas de camiones:', error);
    throw error;
  }
}

export async function obtenerEstadisticasCamionesDiaDia(): Promise<CamionStats[]> {
  try {
    const response = await fetch('/api/estadisticas/camionesDiaDia');

    if (!response.ok) {
      throw new Error(`Error al obtener camiones: ${response.status}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Error al obtener estadísticas de camiones:', error);
    throw error;
  }
}


export async function obtenerEstadisticasPedidos(): Promise<PedidoStats[]> {
  try {
    const response = await fetch('/api/estadisticas/pedidos');

    if (!response.ok) {
      throw new Error(`Error al obtener pedidos: ${response.status}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Error al obtener estadísticas de pedidos:', error);
    throw error;
  }
}
export async function obtenerEstadisticasPedidosDiaDia(): Promise<PedidoStats[]> {
  try {
    const response = await fetch('/api/estadisticas/pedidosDiaDia');

    if (!response.ok) {
      throw new Error(`Error al obtener pedidos: ${response.status}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Error al obtener estadísticas de pedidos:', error);
    throw error;
  }
}

export async function obtenerResumenCamiones(): Promise<CamionResumen[]> {
  try {
    const response = await fetch('https://h982equipo7g.duckdns.org/api/camiones/resumen');

    if (!response.ok) {
      throw new Error(`Error al obtener resumen de camiones: ${response.status}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Error al obtener resumen de camiones:', error);
    throw error;
  }
}

export async function obtenerResumenPedidos(
  fechaInicio: string,
  fechaFin: string
): Promise<PedidoResumen> {
  try {
    const url = await fetch(`https://h982equipo7g.duckdns.org/api/pedidos/resumenPedidos?fechaInicio=${fechaInicio}&fechaFin=${fechaFin}`);

    const response = await fetch(url.toString());

    if (!response.ok) {
      throw new Error(`Error al obtener resumen de pedidos: ${response.status}`);
    }

    const data: PedidoResumen = await response.json();
    return data;
  } catch (error) {
    console.error('Error al obtener resumen de pedidos:', error);
    throw error;
  }
}

/**
 * Inicia la simulación semanal por WebSocket.
 * @param fechaInicio - fecha inicial en ISO string sin "Z"
 * @param callback - función que se ejecuta cuando llega cada solución
 */

export function iniciarSimulacionSemanalSocket(
  fechaInicio: string,
  callback: (solucion: Solucion) => void
) {
  conectarSimulacionSemanal(fechaInicio, callback);
}

/**
 * Detiene la escucha de simulación.
 */
export function detenerSimulacionSocket() {
  desconectarSimulacion();
}


/**
 * Inicia la simulación día a día por WebSocket.
 * @param fechaInicio - fecha inicial en ISO string sin "Z"
 * @param callback - función que se ejecuta cuando llega cada solución
 */
export function iniciarSimulacionDiaDiaSocket(
  fechaInicio: string,
  callback: (solucion: Solucion) => void
) {
  conectarSimulacionDiaDia(fechaInicio, callback);
}

/**
 * Detiene la escucha de simulación.
 */
export function detenerSimulacionDiaDiaSocket() {
  desconectarSimulacion();
}
