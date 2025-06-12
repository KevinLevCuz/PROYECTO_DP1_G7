// components/daily/SimulationMap.tsx
"use client";

import { BsPlayFill, BsStopFill } from "react-icons/bs";
import { useEffect, useRef, useState, useCallback } from "react";
import { obtenerRutasOptimizadas, obtenerPedidos, obtenerPlantas } from "../../lib/api";
import type {Camion, SubRuta, Ubicacion, Planta, Pedido } from '../../lib/api';
import NewOrderModal from "@/components/daily/NewOrderPanel";

export default function SimulationMap() {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const truckImgRef = useRef<HTMLImageElement | null>(null);
  const plantPrincipalImgRef = useRef<HTMLImageElement | null>(null);
  const plantSecundariaImgRef = useRef<HTMLImageElement | null>(null);
  const orderImgRef = useRef<HTMLImageElement | null>(null);
  
  const [simulationTime, setSimulationTime] = useState<number>(Date.now());
  const [isSimulating, setIsSimulating] = useState<boolean>(true);

  useEffect(() => {
    const interval = setInterval(() => {
      if (isSimulating) {
        setSimulationTime(Date.now());
      }
    }, 1000);
    
    return () => clearInterval(interval);
  }, [isSimulating]);
  // Función para verificar si un camión debe comenzar a moverse
  const shouldTruckStart = useCallback((subRutas: SubRuta[], currentTime: number) => {
    return subRutas.some(subRuta => {
      const startTime = new Date(subRuta.horaInicio).getTime();
      return startTime <= currentTime;
    });
  }, []);

  // Función para verificar si un camión ha completado su ruta
  const hasTruckFinished = useCallback((subRutas: SubRuta[], currentTime: number) => {
    if (!subRutas || subRutas.length === 0) return true;
    
    const lastSubRuta = subRutas[subRutas.length - 1];
    const endTime = new Date(lastSubRuta.horaFin).getTime();
    
    return endTime <= currentTime;
  }, []);


  const [hoveredPlant, setHoveredPlant] = useState<Planta | null>(null);
  const [tooltipPosition, setTooltipPosition] = useState({ x: 0, y: 0 });

  const [imagesLoaded, setImagesLoaded] = useState({
    truck: false,
    plantPrincipal: false,
    plantSecundaria: false,
    order: false
  });
  
  const animationFrameRef = useRef<number>(0);
  const lastTimeRef = useRef<number>(0);
  
  const [trucks, setTrucks] = useState<Camion[]>([]);
  const [plants, setPlants] = useState<Planta[]>([]);
  const [orders, setOrders] = useState<Pedido[]>([]);
  const [routes, setRoutes] = useState<SubRuta[][]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const trucksProgressRef = useRef(
    routes.map(() => ({
      currentStep: 0,
      progress: 0,
      currentPos: [0, 0] as [number, number],
      targetPos: [0, 0] as [number, number]
    }))
  );

  // Cargar datos del backend
  useEffect(() => {
    const fetchData = async () => {
      try {
        const [rutasOptimizadas, pedidos, plantas] = await Promise.all([
          obtenerRutasOptimizadas(),
          obtenerPedidos(),
          obtenerPlantas()
        ]);

        // Procesar camiones y rutas
        const camiones = rutasOptimizadas.map(r => r.camion);
        const subRutas = rutasOptimizadas.map(r => r.subRutas);


        setTrucks(camiones);
        setRoutes(subRutas);
        setOrders(pedidos);
        setPlants(plantas);
        setLoading(false);
      } catch (err) {
        setError('Error al cargar los datos de rutas');
        setLoading(false);
        console.error(err);
      }
    };

    fetchData();
  }, []);

  // Cargar imágenes
  useEffect(() => {
    const loadImages = async () => {
      try {
        const loadImage = (src: string) => new Promise<HTMLImageElement>((resolve, reject) => {
          const img = new Image();
          img.src = src;
          img.onload = () => resolve(img);
          img.onerror = reject;
        });

        const [truckImg, plantPrincipalImg, plantSecundariaImg, orderImg] = await Promise.all([
          loadImage('/camionRuta.png'),
          loadImage('/plantaPrincipal.png'),
          loadImage('/plantaSecundaria.png'),
          loadImage('/pedido.png')
        ]);

        truckImgRef.current = truckImg;
        plantPrincipalImgRef.current = plantPrincipalImg;
        plantSecundariaImgRef.current = plantSecundariaImg;
        orderImgRef.current = orderImg;

        setImagesLoaded({
          truck: true,
          plantPrincipal: true,
          plantSecundaria: true,
          order: true
        });
      } catch (error) {
        console.error("Error al cargar las imágenes:", error);
      }
    };
    
    loadImages();
  }, []);

  // Inicializar posiciones cuando los datos estén listos
  useEffect(() => {
    if (!Object.values(imagesLoaded).every(Boolean) || loading) return;

    trucksProgressRef.current = routes.map((subRutas, index) => {
      const initialPos = trucks[index]?.ubicacionActual || { posX: 0, posY: 0 };
      const firstRoute = subRutas[0]?.trayectoria || [];
      
      return {
        currentStep: 0,
        progress: 0,
        currentPos: [initialPos.posX, initialPos.posY] as [number, number],
        targetPos: firstRoute.length > 0 
          ? [firstRoute[0].posX, firstRoute[0].posY] as [number, number] 
          : [initialPos.posX, initialPos.posY] as [number, number]
      };
    });

    // Dibujar estado inicial
    drawInitialState();
  }, [imagesLoaded, loading, trucks, plants, orders, routes]);

  const drawGrid = useCallback((ctx: CanvasRenderingContext2D, cols: number, rows: number, spacing: number) => {
    ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);
    ctx.strokeStyle = "#ccc";
    
    for (let x = 0; x <= cols; x++) {
      ctx.beginPath();
      ctx.moveTo(x * spacing, 0);
      ctx.lineTo(x * spacing, rows * spacing);
      ctx.stroke();
    }
    for (let y = 0; y <= rows; y++) {
      ctx.beginPath();
      ctx.moveTo(0, y * spacing);
      ctx.lineTo(cols * spacing, y * spacing);
      ctx.stroke();
    }
  }, []);

  const drawPlant = useCallback((
    ctx: CanvasRenderingContext2D, 
    x: number, 
    y: number, 
    plant: Planta, 
    spacing: number
  ) => {
    const isPrincipal = plant.id == "1"; // Ajusta esta lógica según tu API
    const img = isPrincipal ? plantPrincipalImgRef.current : plantSecundariaImgRef.current;
    
    if (!img) return;

    const imgSize = isPrincipal ? 30 : 25;
    const canvasX = x * spacing;
    const canvasY = y * spacing;
    
    // Guardar posición para el hover
    plant.canvasPosition = { x: canvasX, y: canvasY, size: imgSize };
    
    ctx.save();
    ctx.translate(canvasX, canvasY);
    ctx.rotate(Math.PI);
    ctx.drawImage(img, -imgSize / 2, -imgSize / 2, imgSize, imgSize);
    ctx.restore();

    // Dibujar tooltip si esta planta está siendo hovered
    if (hoveredPlant?.id === plant.id) {
      drawPlantTooltip(ctx, plant, tooltipPosition.x, tooltipPosition.y);
    }
  }, [hoveredPlant, tooltipPosition]);

  const drawPlantTooltip = (
  ctx: CanvasRenderingContext2D,
  plant: Planta,
  x: number,
  y: number
) => {
  ctx.save();
  ctx.scale(1, -1); // Invertir el eje Y para dibujar correctamente

  const tooltipWidth = 160;
  const tooltipHeight = 55;
  const padding = 10;

  // Ajuste para que no se salga del canvas
  const adjustedX = x + 20 > ctx.canvas.width ? x - tooltipWidth - 10 : x + 12;
  const adjustedY = -y + tooltipHeight > ctx.canvas.height ? -y - 12 : -y + 20;

  // Estilo de fondo con sombra
  ctx.shadowColor = 'rgba(225, 16, 16, 0.3)';
  ctx.shadowBlur = 6;
  ctx.shadowOffsetX = 2;
  ctx.shadowOffsetY = 2;

  ctx.fillStyle = '#ffffff';
  ctx.strokeStyle = '#e0e0e0';
  ctx.lineWidth = 1;
  ctx.beginPath();
  ctx.roundRect(adjustedX, adjustedY, tooltipWidth, tooltipHeight, 8);
  ctx.fill();
  ctx.stroke();

  // Quitar sombra para texto
  ctx.shadowColor = 'transparent';

  // Texto principal
  ctx.fillStyle = '#333';
  ctx.font = 'bold 13px Arial';
  ctx.fillText(`Planta ${plant.id}`, adjustedX + padding, adjustedY + 20);

  // Texto de capacidad
  ctx.fillStyle = '#555';
  ctx.font = '12px Arial';
  ctx.fillText(
    `Capacidad: ${plant.glpDisponible}/${plant.capacidadMaxima}`,
    adjustedX + padding,
    adjustedY + 38
  );

  ctx.restore();
};


  const drawOrder = useCallback((
    ctx: CanvasRenderingContext2D, 
    x: number, 
    y: number, 
    order: Pedido, 
    spacing: number
  ) => {
    if (!orderImgRef.current) return;

    const imgSize = 20;
    
    ctx.save();
    ctx.translate(x * spacing, y * spacing);
    ctx.rotate(Math.PI);
    ctx.drawImage(orderImgRef.current, -imgSize / 2, -imgSize / 2, imgSize, imgSize);
    ctx.restore();
  }, []);

  const drawRoute = useCallback((
    ctx: CanvasRenderingContext2D,
    route: Ubicacion[],
    color: string,
    spacing: number
  ) => {
    if (!route || route.length === 0) return;

    ctx.save();
    ctx.strokeStyle = color || '#888888';
    ctx.lineWidth = 2;
    ctx.beginPath();
    
    // Dibujar línea entre los puntos de la ruta
    for (let i = 0; i < route.length - 1; i++) {
      const { posX: x1, posY: y1 } = route[i];
      const { posX: x2, posY: y2 } = route[i + 1];
      
      if (i === 0) {
        ctx.moveTo(x1 * spacing, y1 * spacing);
      }
      ctx.lineTo(x2 * spacing, y2 * spacing);
    }
    
    ctx.stroke();
    
    // Dibujar puntos en cada coordenada de la ruta
    ctx.fillStyle = color || '#888888';
    route.forEach(({ posX: x, posY: y }) => {
      ctx.beginPath();
      ctx.arc(x * spacing, y * spacing, 3, 0, Math.PI * 2);
      ctx.fill();
    });
    
    ctx.restore();
  }, []);

  const drawTruck = useCallback((
    ctx: CanvasRenderingContext2D, 
    x: number, 
    y: number, 
    truck: Camion, 
    spacing: number,
    targetPos?: [number, number], 
    currentPos?: [number, number],
    isFinalPosition: boolean = false
  ) => {
    if (!truckImgRef.current) return;

    const img = truckImgRef.current;
    const imgSize = 20;

    ctx.save();
    ctx.translate(x * spacing, y * spacing);

    if (targetPos && currentPos) {
      const dx = targetPos[0] - currentPos[0];
      const dy = targetPos[1] - currentPos[1];

      if (isFinalPosition) {
        ctx.rotate(Math.PI);  
      } else {
        // Comportamiento normal durante el movimiento
        if (dx === 1 && dy === 0) {
          ctx.rotate(Math.PI);
        } else if (dx === -1 && dy === 0) {
          ctx.scale(1, -1);
          ctx.rotate(0);
        } else if (dx === 0 && dy === 1) {
          ctx.rotate(3 * Math.PI / 2);
        } else if (dx === 0 && dy === -1) {
          ctx.rotate(Math.PI / 2);
        }
      }
    }

    ctx.drawImage(img, -imgSize / 2, -imgSize / 2, imgSize, imgSize);
    ctx.restore();

    // Dibujar ID
    ctx.save();
    ctx.scale(1, -1);
    ctx.fillStyle = '#000000';
    ctx.font = '10px Arial';
    ctx.fillText(truck.codigo, x * spacing - 5, -y * spacing + 5);
    ctx.restore();
  }, []);

  const animate = useCallback((timestamp: number) => {
  if (!canvasRef.current || !Object.values(imagesLoaded).every(Boolean)) return;
  
  const ctx = canvasRef.current.getContext("2d");
  if (!ctx) return;

  const cols = 70;
  const rows = 50;
  const spacing = 13;

  if (!lastTimeRef.current) {
    lastTimeRef.current = timestamp;
  }
  
  const deltaTime = timestamp - lastTimeRef.current;
  lastTimeRef.current = timestamp;
  
  drawGrid(ctx, cols, rows, spacing);
  
  // Dibujar plantas
  plants.forEach(plant => {
    drawPlant(ctx, plant.ubicacion.posX, plant.ubicacion.posY, plant, spacing);
  });

  // Dibujar pedidos
  orders.forEach(order => {
    drawOrder(ctx, order.destino.posX, order.destino.posY, order, spacing);
  });

  let anyTruckActive = false;

  routes.forEach((subRutas, index) => {
    const progressData = trucksProgressRef.current[index];
    const truck = trucks[index];
    
    if (!truck || !subRutas || subRutas.length === 0) return;

    // Verificar si el camión debe comenzar o ya terminó
    const shouldStart = shouldTruckStart(subRutas, simulationTime);
    const hasFinished = hasTruckFinished(subRutas, simulationTime);

    if (!shouldStart) {
      // Dibujar en posición inicial si no ha comenzado
      drawTruck(
        ctx,
        truck.ubicacionActual.posX, 
        truck.ubicacionActual.posY, 
        truck,
        spacing,
        [truck.ubicacionActual.posX, truck.ubicacionActual.posY],
        [truck.ubicacionActual.posX, truck.ubicacionActual.posY],
        false
      );
      return;
    }

    if (hasFinished) {
      // Dibujar en posición final sin animar
      const finalPos = subRutas[subRutas.length - 1].trayectoria.slice(-1)[0];
      drawTruck(
        ctx,
        finalPos.posX, 
        finalPos.posY, 
        truck,
        spacing,
        [finalPos.posX, finalPos.posY],
        [finalPos.posX, finalPos.posY],
        true
      );
      return;
    }

    anyTruckActive = true;
    
    // Combinar todas las trayectorias de las subrutas activas
    const activeSubRutas = subRutas.filter(subRuta => 
      new Date(subRuta.horaInicio).getTime() <= simulationTime
    );
    const fullRoute = activeSubRutas.flatMap(subRuta => subRuta.trayectoria);
    
    // Si es la primera vez que este camión comienza, inicializar posición
    if (progressData.currentStep === -1) {
      progressData.currentStep = 0;
      progressData.currentPos = [fullRoute[0].posX, fullRoute[0].posY];
      progressData.targetPos = fullRoute.length > 1 
        ? [fullRoute[1].posX, fullRoute[1].posY]
        : [fullRoute[0].posX, fullRoute[0].posY];
    }

    // Lógica de animación
    progressData.progress += deltaTime / 1000;
    const transitionDuration = 72; // 1 segundo por segmento
    
    if (progressData.progress >= transitionDuration) {
      progressData.progress = 0;
      progressData.currentStep++;
      
      if (progressData.currentStep < fullRoute.length - 1) {
        const currentPos = fullRoute[progressData.currentStep];
        const nextPos = fullRoute[progressData.currentStep + 1];
        progressData.currentPos = [currentPos.posX, currentPos.posY];
        progressData.targetPos = [nextPos.posX, nextPos.posY];
      } else {
        // Llegamos al final de la ruta
        const finalPos = fullRoute[fullRoute.length - 1];
        progressData.currentPos = [finalPos.posX, finalPos.posY];
        progressData.targetPos = [finalPos.posX, finalPos.posY];
      }
    }
    
    const t = Math.min(progressData.progress / transitionDuration, 1);
    const interpolatedX = progressData.currentPos[0] + (progressData.targetPos[0] - progressData.currentPos[0]) * t;
    const interpolatedY = progressData.currentPos[1] + (progressData.targetPos[1] - progressData.currentPos[1]) * t;
    
    drawTruck(
      ctx,
      interpolatedX, 
      interpolatedY, 
      truck,
      spacing,
      progressData.targetPos,
      progressData.currentPos,
      false
    );
  });

  // Continuar la animación solo si hay camiones activos
  if (anyTruckActive) {
    animationFrameRef.current = requestAnimationFrame(animate);
  }
}, [drawGrid, drawTruck, drawPlant, drawOrder, plants, orders, trucks, routes, imagesLoaded, simulationTime, shouldTruckStart, hasTruckFinished]);
  const drawInitialState = useCallback(() => {
    if (!canvasRef.current || !Object.values(imagesLoaded).every(Boolean)) return;
    
    const ctx = canvasRef.current.getContext("2d");
    if (!ctx) return;

    const cols = 70;
    const rows = 50;
    const spacing = 13;

    // Configuración inicial del canvas
    canvasRef.current.width = cols * spacing;
    canvasRef.current.height = rows * spacing;

    ctx.translate(0, canvasRef.current.height);
    ctx.scale(1, -1);

    drawGrid(ctx, cols, rows, spacing);
    
    // Dibujar plantas
    plants.forEach(plant => {
      drawPlant(ctx, plant.ubicacion.posX, plant.ubicacion.posY, plant, spacing);
    });

    // Dibujar pedidos
    orders.forEach(order => {
      drawOrder(ctx, order.destino.posX, order.destino.posY, order, spacing);
    });

    routes.forEach((subRutas, index) => {
      const color = `hsl(${(index * 30) % 360}, 70%, 50%)`;
      subRutas.forEach(subRuta => {
        console.log("La hora de inicio es: "+ subRuta.horaInicio+ "y la de llegada es: "+ subRuta.horaFin+" y su nodo de llegada es: X:"+subRuta.fin.posX+" Y:"+subRuta.fin.posY);
      });
    });


    // Dibujar camiones en posición inicial
    trucks.forEach((truck, index) => {
      const progressData = trucksProgressRef.current[index];
      drawTruck(
        ctx,
        progressData.currentPos[0],
        progressData.currentPos[1],
        truck,
        spacing,
        progressData.targetPos,
        progressData.currentPos,
        false
      );
    });
  }, [imagesLoaded, trucks, plants, orders, drawGrid, drawTruck, drawPlant, drawOrder]);

  const startAnimation = useCallback(() => {
    cancelAnimationFrame(animationFrameRef.current);
    lastTimeRef.current = 0;
    animationFrameRef.current = requestAnimationFrame(animate);
  }, [animate]);

  const stopAnimation = useCallback(() => {
    cancelAnimationFrame(animationFrameRef.current);
  }, []);

  // Configuración inicial del canvas
  useEffect(() => {
    if (!Object.values(imagesLoaded).every(Boolean) || !canvasRef.current) return;

    const canvas = canvasRef.current;
    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    const cols = 70;
    const rows = 50;
    const spacing = 13;

    canvas.width = cols * spacing;
    canvas.height = rows * spacing;

    ctx.translate(0, canvas.height);
    ctx.scale(1, -1);

    drawGrid(ctx, cols, rows, spacing);
    
    // Dibujar plantas
    plants.forEach(plant => {
      drawPlant(ctx, plant.ubicacion.posX, plant.ubicacion.posY, plant, spacing);
    });

    // Dibujar pedidos
    orders.forEach(order => {
      drawOrder(ctx, order.destino.posX, order.destino.posY, order, spacing);
    });
    
    // Dibujar camiones en posición inicial
    trucks.forEach((truck, index) => {
      const progressData = trucksProgressRef.current[index];
      drawTruck(
        ctx,
        progressData.currentPos[0],
        progressData.currentPos[1],
        truck,
        spacing,
        progressData.targetPos,
        progressData.currentPos,
        false
      );
    });
  }, [imagesLoaded, trucks, plants, orders, drawGrid, drawTruck, drawPlant, drawOrder]);
  const handleCanvasHover = (e: React.MouseEvent<HTMLCanvasElement>) => {
  if (!canvasRef.current) return;
  
  const canvas = canvasRef.current;
  const rect = canvas.getBoundingClientRect();
  const x = e.clientX - rect.left;
  const y = rect.bottom - e.clientY; // Invertir Y para coincidir con el sistema de coordenadas del canvas
  
  setTooltipPosition({ x, y });
  
  // Verificar si el mouse está sobre alguna planta
  const hovered = plants.find(plant => {
    if (!plant.canvasPosition) return false;
    const { x: plantX, y: plantY, size } = plant.canvasPosition;
    return x >= plantX - size/2 && 
           x <= plantX + size/2 && 
           y >= plantY - size/2 && 
           y <= plantY + size/2;
  });
  
  setHoveredPlant(hovered || null);
};


  useEffect(() => {
    if (isSimulating) {
      startAnimation();
    } else {
      stopAnimation();
    }
    
    return () => {
      stopAnimation();
    };
  }, [isSimulating, startAnimation, stopAnimation]);

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-200 flex items-center justify-center">
        <div className="text-xl">Cargando datos de simulación...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-200 flex items-center justify-center">
        <div className="text-xl text-red-500">{error}</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-200 relative overflow-auto">
      <div className="absolute inset-0 flex items-center justify-center overflow-auto">
        <canvas 
  ref={canvasRef} 
  className="bg-white border border-gray-400"
  onMouseMove={handleCanvasHover}
  onMouseOut={() => setHoveredPlant(null)}
/>
      </div>
    </div>
  );
}