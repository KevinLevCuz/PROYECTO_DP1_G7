// lib/api.ts
export interface Ubicacion {
  posX: number;
  posY: number;
  bloqueado: boolean;
}

export interface Planta{
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
  idCliente: string;
  estado?: string;  // Opcional si lo agregas después
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


export interface Camion {
  codigo: string;
  ubicacionActual: Ubicacion;
  capacidadMaxima: number;
  glpActual: number;
  enRuta: boolean;
  disponibleDesde: string;
  horaLibre?: String;
  SubRutasExistentes?: String;
}

export interface RutaCamion {
  camion: Camion;
  subRutas: SubRuta[];
}



export async function obtenerRutasOptimizadas(): Promise<RutaCamion[]> {
  try {
    const response = await fetch('http://localhost:8080/api/routing/optimize', { method: 'POST' });
    
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
    const response = await fetch('http://localhost:8080/api/routing/obtenerPedidos', { method: 'POST' });
    
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
    const response = await fetch('http://localhost:8080/api/routing/obtenerPlantas', { method: 'POST' });
    
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