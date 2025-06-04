"use client";

import { BsPlayFill, BsStopFill } from "react-icons/bs";
import { useEffect, useRef, useState, useCallback } from "react";

interface Truck {
  id: number;
  initialPosition: [number, number];
  route: [number, number][];
  color?: string;
}

interface Plant {
  id: number;
  position: [number, number];
  type: 'PRINCIPAL' | 'SECUNDARIA';
  name: string;
}

interface Order {
  id: number;
  position: [number, number];
  name: string;
}

export default function SimulationMap() {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const truckImgRef = useRef<HTMLImageElement | null>(null);
  const plantPrincipalImgRef = useRef<HTMLImageElement | null>(null);
  const plantSecundariaImgRef = useRef<HTMLImageElement | null>(null);
  const orderImgRef = useRef<HTMLImageElement | null>(null);
  
  const [imagesLoaded, setImagesLoaded] = useState({
    truck: false,
    plantPrincipal: false,
    plantSecundaria: false,
    order: false
  });
  
  const animationFrameRef = useRef<number>(0);
  const lastTimeRef = useRef<number>(0);
  
  const [trucks, setTrucks] = useState<Truck[]>([
    {
      id: 1,
      initialPosition: [3, 3],
      route: [
        [3, 3],[3, 2],[3, 1],[3, 0],[2, 0],[1, 0],[0, 0]
      ],
      color: '#FF0000'
    },
    {
      id: 2,
      initialPosition: [10, 5],
      route: [
        [10, 5],[10, 6],[11, 6],[12, 6],[12, 7],[12, 8]
      ],
      color: '#00FF00'
    }
  ]);

  const [plants, setPlants] = useState<Plant[]>([
    {
      id: 1,
      position: [5, 5],
      type: 'PRINCIPAL',
      name: 'Planta Central'
    },
    {
      id: 2,
      position: [15, 10],
      type: 'SECUNDARIA',
      name: 'Planta Norte'
    },
    {
      id: 3,
      position: [8, 15],
      type: 'SECUNDARIA',
      name: 'Planta Este'
    }
  ]);

  const [orders, setOrders] = useState<Order[]>([
    {
      id: 1,
      position: [7, 7],
      name: 'Pedido 1'
    },
    {
      id: 2,
      position: [12, 12],
      name: 'Pedido 2'
    },
    {
      id: 3,
      position: [18, 5],
      name: 'Pedido 3'
    }
  ]);

  const trucksProgressRef = useRef(
    trucks.map(() => ({
      currentStep: 0,
      progress: 0,
      currentPos: [0, 0] as [number, number],
      targetPos: [0, 0] as [number, number]
    }))
  );

  // Cargar imágenes
  useEffect(() => {
    const loadImages = async () => {
      try {
        // Cargar imagen del camión
        const truckImg = new Image();
        truckImg.src = '/camionRuta.png';
        await new Promise((resolve, reject) => {
          truckImg.onload = resolve;
          truckImg.onerror = reject;
        });
        truckImgRef.current = truckImg;
        
        // Cargar imagen de planta principal
        const plantPrincipalImg = new Image();
        plantPrincipalImg.src = '/plantaPrincipal.png';
        await new Promise((resolve, reject) => {
          plantPrincipalImg.onload = resolve;
          plantPrincipalImg.onerror = reject;
        });
        plantPrincipalImgRef.current = plantPrincipalImg;
        
        // Cargar imagen de planta secundaria
        const plantSecundariaImg = new Image();
        plantSecundariaImg.src = '/plantaSecundaria.png';
        await new Promise((resolve, reject) => {
          plantSecundariaImg.onload = resolve;
          plantSecundariaImg.onerror = reject;
        });
        plantSecundariaImgRef.current = plantSecundariaImg;
        
        // Cargar imagen de pedido
        const orderImg = new Image();
        orderImg.src = '/pedido.png';
        await new Promise((resolve, reject) => {
          orderImg.onload = resolve;
          orderImg.onerror = reject;
        });
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

  // Inicializar posiciones
  useEffect(() => {
    if (!Object.values(imagesLoaded).every(Boolean)) return;

    trucksProgressRef.current = trucks.map((truck, index) => ({
      currentStep: 0,
      progress: 0,
      currentPos: [...truck.initialPosition] as [number, number],
      targetPos: truck.route.length > 1 
        ? [...truck.route[1]] as [number, number] 
        : [...truck.initialPosition] as [number, number]
    }));
  }, [imagesLoaded, trucks]);

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
    plant: Plant, 
    spacing: number
  ) => {
    const img = plant.type === 'PRINCIPAL' 
      ? plantPrincipalImgRef.current 
      : plantSecundariaImgRef.current;
    
    if (!img) return;

    const imgSize = plant.type === 'PRINCIPAL' ? 30 : 25;
    
    ctx.save();
    ctx.translate(x * spacing, y * spacing);
    ctx.rotate(Math.PI);
    ctx.drawImage(img, -imgSize / 2, -imgSize / 2, imgSize, imgSize);
    ctx.restore();
  }, []);

  const drawOrder = useCallback((
    ctx: CanvasRenderingContext2D, 
    x: number, 
    y: number, 
    order: Order, 
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
    route: [number, number][],
    color: string,
    spacing: number
  ) => {
    ctx.save();
    ctx.strokeStyle = color;
    ctx.lineWidth = 2;
    ctx.beginPath();
    
    // Dibujar línea entre los puntos de la ruta
    for (let i = 0; i < route.length - 1; i++) {
      const [x1, y1] = route[i];
      const [x2, y2] = route[i + 1];
      
      if (i === 0) {
        ctx.moveTo(x1 * spacing, y1 * spacing);
      }
      ctx.lineTo(x2 * spacing, y2 * spacing);
    }
    
    ctx.stroke();
    
    // Dibujar puntos en cada coordenada de la ruta
    ctx.fillStyle = color;
    route.forEach(([x, y]) => {
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
    truck: Truck, 
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
    ctx.fillStyle = truck.color || '#000000';
    ctx.font = '10px Arial';
    ctx.fillText(truck.id.toString(), x * spacing - 5, -y * spacing + 5);
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
      drawPlant(ctx, plant.position[0], plant.position[1], plant, spacing);
    });

    // Dibujar pedidos
    orders.forEach(order => {
      drawOrder(ctx, order.position[0], order.position[1], order, spacing);
    });

    // Dibujar rutas de los camiones
    trucks.forEach(truck => {
      drawRoute(ctx, truck.route, truck.color || '#888888', spacing);
    });

    let allTrucksFinished = true;

    trucks.forEach((truck, index) => {
      const progressData = trucksProgressRef.current[index];
      const truckRoute = truck.route;
      
      if (progressData.currentStep >= truckRoute.length - 1) {
        // Asegurarse de que la posición final sea exactamente la última de la ruta
        progressData.currentPos = [...truckRoute[truckRoute.length - 1]] as [number, number];
        drawTruck(
          ctx,
          progressData.currentPos[0], 
          progressData.currentPos[1], 
          truck,
          spacing,
          progressData.currentPos,
          progressData.currentPos,
          true // Es la posición final
        );
        return;
      }

      allTrucksFinished = false;
      
      progressData.progress += deltaTime / 1000;
      const transitionDuration = 0.5;
      
      if (progressData.progress >= transitionDuration) {
        progressData.progress = 0;
        progressData.currentStep++;
        
        if (progressData.currentStep < truckRoute.length - 1) {
          progressData.currentPos = [...truckRoute[progressData.currentStep]] as [number, number];
          progressData.targetPos = [...truckRoute[progressData.currentStep + 1]] as [number, number];
        } else {
          // Si llegó al último paso, fijamos la posición exactamente al final
          progressData.currentPos = [...truckRoute[truckRoute.length - 1]] as [number, number];
          progressData.targetPos = [...truckRoute[truckRoute.length - 1]] as [number, number];
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
        false // No es la posición final
      );
    });

    if (!allTrucksFinished) {
      animationFrameRef.current = requestAnimationFrame(animate);
    }
  }, [drawGrid, drawTruck, drawPlant, drawOrder, drawRoute, plants, orders, trucks, imagesLoaded]);

  const startAnimation = useCallback(() => {
    cancelAnimationFrame(animationFrameRef.current);
    lastTimeRef.current = 0;
    animationFrameRef.current = requestAnimationFrame(animate);
  }, [animate]);

  const stopAnimation = useCallback(() => {
    cancelAnimationFrame(animationFrameRef.current);
  }, []);

  const addTruck = useCallback(() => {
    const newId = trucks.length > 0 ? Math.max(...trucks.map(t => t.id)) + 1 : 1;
    const newTruck: Truck = {
      id: newId,
      initialPosition: [Math.floor(Math.random() * 10), Math.floor(Math.random() * 10)],
      route: [
        [Math.floor(Math.random() * 10), Math.floor(Math.random() * 10)],
        [Math.floor(Math.random() * 10), Math.floor(Math.random() * 10)],
        [Math.floor(Math.random() * 10), Math.floor(Math.random() * 10)]
      ],
      color: `#${Math.floor(Math.random()*16777215).toString(16).padStart(6, '0')}`
    };
    setTrucks([...trucks, newTruck]);
  }, [trucks]);

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
      drawPlant(ctx, plant.position[0], plant.position[1], plant, spacing);
    });

    // Dibujar pedidos
    orders.forEach(order => {
      drawOrder(ctx, order.position[0], order.position[1], order, spacing);
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

  return (
    <div className="min-h-screen bg-gray-200 relative overflow-auto">
      <div className="absolute top-4 left-16 z-10 flex gap-2">
        <div className="flex items-end gap-2">
          <div>
            <label className="block text-sm font-medium mb-1">Fecha y Hora de Inicio</label>
            <div className="flex items-center border rounded px-2 py-1 bg-white">
              <input
                type="datetime-local"
                className="text-sm outline-none border-none bg-transparent"
                placeholder="dd:mm:aaaa hh:mm"
              />
              <span className="text-gray-400 ml-2">📅</span>
            </div>
          </div>

          <button 
            className="w-8 h-8 rounded-full bg-teal-500 text-white flex items-center justify-center"
            onClick={startAnimation}>
            <BsPlayFill className="w-4 h-4" />
          </button>

          <button 
            className="w-8 h-8 rounded-full border-2 border-red-500 text-red-500 flex items-center justify-center"
            onClick={stopAnimation}>
            <BsStopFill className="w-4 h-4" />
          </button>

          <button 
            className="w-8 h-8 rounded-full bg-blue-500 text-white flex items-center justify-center"
            onClick={addTruck}>
            +
          </button>
        </div>
      </div>

      <div className="absolute inset-0 flex items-center justify-center overflow-auto">
        <canvas ref={canvasRef} className="bg-white border border-gray-400" />
      </div>
    </div>
  );
}