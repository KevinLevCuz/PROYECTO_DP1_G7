// utils/transformData.ts
import { RutaCamion } from '../lib/api';
import type { Truck, Plant, Order } from '../types/simulation';


const TRUCK_COLORS = ['#FF0000', '#00FF00', '#0000FF', '#FFFF00', '#FF00FF', '#00FFFF'];

export function transformRouteData(data: RutaCamion[]) {
  const trucks: Truck[] = [];
  const plants: Map<string, Plant> = new Map();
  const orders: Order[] = [];

  
  // Asumimos que la primera posición de la primera subruta del primer camión es la planta principal
  if (data.length > 0 && data[0].subRutas.length > 0) {
    const mainPlantPos = data[0].subRutas[0].inicio;
    const mainPlantKey = `${mainPlantPos.posX},${mainPlantPos.posY}`;
    plants.set(mainPlantKey, {
      id: 1,
      position: [mainPlantPos.posX, mainPlantPos.posY],
      type: 'PRINCIPAL',
      name: 'Planta Central'
    });
  }

  data.forEach((ruta, index) => {
    const color = TRUCK_COLORS[index % TRUCK_COLORS.length];
    const allRoutePoints: [number, number][] = [];

    // Procesar cada subruta
    ruta.subRutas.forEach(subRuta => {
      // Agregar puntos de la trayectoria
      subRuta.trayectoria.forEach(punto => {
        allRoutePoints.push([punto.posX, punto.posY]);
      });

      // Procesar pedidos
      if (subRuta.pedido) {
        orders.push({
          id: orders.length + 1,
          position: [subRuta.pedido.destino.posX, subRuta.pedido.destino.posY],
          name: `Pedido ${orders.length + 1} (Camión ${ruta.camion.codigo})`
        });
      }

      // Identificar plantas secundarias
      const startKey = `${subRuta.inicio.posX},${subRuta.inicio.posY}`;
      const endKey = `${subRuta.fin.posX},${subRuta.fin.posY}`;
      
      if (!plants.has(startKey) && !orders.some(o => 
        o.position[0] === subRuta.inicio.posX && o.position[1] === subRuta.inicio.posY)) {
        plants.set(startKey, {
          id: plants.size + 1,
          position: [subRuta.inicio.posX, subRuta.inicio.posY],
          type: 'SECUNDARIA',
          name: `Planta ${plants.size + 1}`
        });
      }

      if (!plants.has(endKey) && !orders.some(o => 
        o.position[0] === subRuta.fin.posX && o.position[1] === subRuta.fin.posY)) {
        plants.set(endKey, {
          id: plants.size + 1,
          position: [subRuta.fin.posX, subRuta.fin.posY],
          type: 'SECUNDARIA',
          name: `Planta ${plants.size + 1}`
        });
      }
    });

    // Agregar el camión con su ruta completa
    if (allRoutePoints.length > 0) {
      trucks.push({
        id: trucks.length + 1,
        initialPosition: allRoutePoints[0],
        route: allRoutePoints,
        color
      });
    }
  });

  return {
    trucks,
    plants: Array.from(plants.values()),
    orders
  };
}

