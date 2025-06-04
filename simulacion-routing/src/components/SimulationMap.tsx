"use client";

import { BsPlayFill, BsStopFill } from "react-icons/bs";
import { useEffect, useRef, useState } from "react";

export default function SimulationMap() {
  const canvasRef = useRef(null);
  const truckImgRef = useRef<HTMLImageElement | null>(null);
  const [imageLoaded, setImageLoaded] = useState(false);
  const animationFrameRef = useRef<number>(0);
  const lastTimeRef = useRef<number>(0);
  const progressRef = useRef<number>(0);

  // Cargar la imagen del camión
  useEffect(() => {
    const img = new Image();
    img.src = '/camionRuta.png'; // Asegúrate de tener esta imagen en tu carpeta public
    img.onload = () => {
      truckImgRef.current = img;
      setImageLoaded(true);
    };
    img.onerror = () => {
      console.error("Error al cargar la imagen del camión");
    };
  }, []);

  useEffect(() => {
    if (!imageLoaded) return; // No hacer nada hasta que la imagen esté cargada

    const canvas = canvasRef.current as HTMLCanvasElement | null;
    if (!canvas) return;

    const ctx = canvas.getContext("2d");
    if (!ctx) {
      console.error("No se pudo obtener el contexto 2D del canvas.");
      return;
    }

    const cols = 70;
    const rows = 50;
    const spacing = 13;

    canvas.width = cols * spacing;
    canvas.height = rows * spacing;

    
    // Trasladar el sistema de coordenadas al fondo del canvas
    ctx.translate(0, canvas.height);

    // Invertir el eje Y
    ctx.scale(1, -1);


    // Coordenadas (x, y) simuladas
    const rutaCamion1 = [
      [3, 3],
      [3, 2],
      [3, 1],
      [3, 0],
      [2, 0],
      [1, 0],
      [0, 0]
    ];


    const drawGrid = () => {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      ctx.strokeStyle = "#ccc";
      
      // Redibujar el grid
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
    };

    const drawTruck = (x: number, y: number) => {
  if (!truckImgRef.current) return;

  const img = truckImgRef.current;
  const imgSize = 20;

  ctx.save();

  // Mover al centro de la celda
  ctx.translate(x * spacing, y * spacing);

  if (targetPos) {
    const dx = targetPos[0] - currentPos[0];
    const dy = targetPos[1] - currentPos[1];

    if (dx === 1 && dy === 0) {
      // ➡ Derecha
      ctx.rotate(Math.PI); // Ajusta si no apunta bien
    } else if (dx === -1 && dy === 0) {
      // ⬅ Izquierda
      ctx.scale(1, -1)
      ctx.rotate(0); // 180°
      
    } else if (dx === 0 && dy === 1) {
      // ⬆ Arriba (recuerda que Y está invertido)
      ctx.rotate(3*Math.PI / 2); // 90°
    } else if (dx === 0 && dy === -1) {
      // ⬇ Abajo
      ctx.rotate(Math.PI / 2); // -90°
    }
  }


  // Dibujar imagen centrada
  ctx.drawImage(img, -imgSize / 2, -imgSize / 2, imgSize, imgSize);

  ctx.restore();
};




    let currentStep = 0;
    let currentPos = [...rutaCamion1[0]] as [number, number];
    let targetPos = [...rutaCamion1[1]] as [number, number];

    const animate = (timestamp: number) => {
      if (!lastTimeRef.current) {
        lastTimeRef.current = timestamp;
      }
      
      const deltaTime = timestamp - lastTimeRef.current;
      lastTimeRef.current = timestamp;
      
      progressRef.current += deltaTime / 1000;
      const transitionDuration = 0.5;
      
      if (progressRef.current >= transitionDuration) {
        progressRef.current = 0;
        currentStep++;
        
        if (currentStep >= rutaCamion1.length - 1) {
          cancelAnimationFrame(animationFrameRef.current);
          return;
        }
        
        currentPos = [...rutaCamion1[currentStep]] as [number, number];
        targetPos = [...rutaCamion1[currentStep + 1]] as [number, number];
      }
      
      const t = Math.min(progressRef.current / transitionDuration, 1);
      const interpolatedX = currentPos[0] + (targetPos[0] - currentPos[0]) * t;
      const interpolatedY = currentPos[1] + (targetPos[1] - currentPos[1]) * t;
      
      drawGrid();
      drawTruck(interpolatedX, interpolatedY);
      
      animationFrameRef.current = requestAnimationFrame(animate);
    };

    // Dibujar grid inicial
    drawGrid();
    drawTruck(currentPos[0], currentPos[1]);

    // Iniciar animación
    animationFrameRef.current = requestAnimationFrame(animate);

    return () => {
      cancelAnimationFrame(animationFrameRef.current);
    };
  }, [imageLoaded]); // Este efecto depende de imageLoaded

  return (
    <div className="min-h-screen bg-gray-200 relative overflow-auto">
      {/* Controles de simulación */}
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

          <button className="w-8 h-8 rounded-full bg-teal-500 text-white flex items-center justify-center">
            <BsPlayFill className="w-4 h-4" />
          </button>

          <button className="w-8 h-8 rounded-full border-2 border-red-500 text-red-500 flex items-center justify-center">
            <BsStopFill className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Mapa */}
      <div className="absolute inset-0 flex items-center justify-center overflow-auto">
        <canvas ref={canvasRef} className="bg-white border border-gray-400" />
      </div>
    </div>
  );
}