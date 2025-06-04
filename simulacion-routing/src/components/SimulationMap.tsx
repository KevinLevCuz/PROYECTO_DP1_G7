"use client";

import { BsPlayFill, BsStopFill } from "react-icons/bs";
import { useEffect, useRef, useState, useCallback } from "react";

interface Truck {
  id: number;
  initialPosition: [number, number];
  route: [number, number][];
  color?: string;
}

export default function SimulationMap() {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const truckImgRef = useRef<HTMLImageElement | null>(null);
  const [imageLoaded, setImageLoaded] = useState(false);
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

  const trucksProgressRef = useRef(
    trucks.map(() => ({
      currentStep: 0,
      progress: 0,
      currentPos: [0, 0] as [number, number],
      targetPos: [0, 0] as [number, number]
    }))
  );

  // Cargar imagen del camión
  useEffect(() => {
    const img = new Image();
    img.src = '/camionRuta.png';
    img.onload = () => {
      truckImgRef.current = img;
      setImageLoaded(true);
    };
    img.onerror = () => {
      console.error("Error al cargar la imagen del camión");
    };
  }, []);

  // Inicializar posiciones
  useEffect(() => {
    if (!imageLoaded) return;

    trucksProgressRef.current = trucks.map((truck, index) => ({
      currentStep: 0,
      progress: 0,
      currentPos: [...truck.initialPosition] as [number, number],
      targetPos: truck.route.length > 1 
        ? [...truck.route[1]] as [number, number] 
        : [...truck.initialPosition] as [number, number]
    }));
  }, [imageLoaded, trucks]);

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

  const drawTruck = useCallback((
    ctx: CanvasRenderingContext2D, 
    x: number, 
    y: number, 
    truck: Truck, 
    spacing: number,
    targetPos?: [number, number], 
    currentPos?: [number, number]
  ) => {
    if (!truckImgRef.current) return;

    const img = truckImgRef.current;
    const imgSize = 20;

    ctx.save();
    ctx.translate(x * spacing, y * spacing);

    if (targetPos && currentPos) {
      const dx = targetPos[0] - currentPos[0];
      const dy = targetPos[1] - currentPos[1];

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
    if (!canvasRef.current || !truckImgRef.current) return;
    
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

    let allTrucksFinished = true;

    trucks.forEach((truck, index) => {
      const progressData = trucksProgressRef.current[index];
      const truckRoute = truck.route;
      
      if (progressData.currentStep >= truckRoute.length - 1) {
        drawTruck(
          ctx,
          progressData.currentPos[0], 
          progressData.currentPos[1], 
          truck,
          spacing,
          progressData.targetPos,
          progressData.currentPos
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
        progressData.currentPos
      );
    });

    if (!allTrucksFinished) {
      animationFrameRef.current = requestAnimationFrame(animate);
    }
  }, [drawGrid, drawTruck, trucks]);

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
    if (!imageLoaded || !canvasRef.current) return;

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
        progressData.currentPos
      );
    });
  }, [imageLoaded, trucks, drawGrid, drawTruck]);

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