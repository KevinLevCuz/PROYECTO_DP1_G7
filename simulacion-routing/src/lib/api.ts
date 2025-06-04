export interface Pedido {
  // completa según tu modelo
  id: number;
  nodoDestino: number;
  cantidad: number;
  tiempoLimite: string;
}

export interface Camion {
  // completa según tu modelo
  id: number;
  tipo: string;
  capacidad: number;
}

export interface OptimizeRequest {
  pedidos: Pedido[];
  camiones: Camion[];
  ahora: string;
}

export async function fetchSolucion(data: OptimizeRequest) {
  const res = await fetch('http://localhost:8080/api/routing/path', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data),
  });

  if (!res.ok) {
    throw new Error('Error en la llamada al backend');
  }

  return res.json(); // Aquí llega la `Solucion` del backend
}
