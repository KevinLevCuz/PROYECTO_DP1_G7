"use client";

import { BsPlayFill, BsStopFill } from "react-icons/bs";
import { useEffect, useRef, useState, useCallback } from "react";
import { obtenerRutasOptimizadas, obtenerPedidos, obtenerPlantas, obtenerBloqueos, obtenerSimulacionSemanal } from "../../lib/api";
import type { Camion, SubRuta, Ubicacion, Planta, Pedido, Bloqueo, Solucion } from '../../lib/api';
import { useSimTime } from "./TimeContextDaily";
import { useTransport } from "./TransportContextDaily";
import { monitoreoDiario } from "../../lib/api";
import { CamionStats } from "../../lib/api";
import { PedidoStats } from "../../lib/api";
import { obtenerEstadisticasCamionesDiaDia } from "../../lib/api";
import { obtenerEstadisticasPedidosDiaDia } from "../../lib/api";
import { iniciarSimulacionDiaDiaSocket, detenerSimulacionDiaDiaSocket } from "../../lib/api";




export default function SimulationMap() {
  const canvasRef = useRef<HTMLCanvasElement>(null); //Referencia de canvas, es useRef porque no se renderiza si cambia algo.
  const truckImgRef = useRef<HTMLImageElement | null>(null); //Imagenes camiones
  const plantPrincipalImgRef = useRef<HTMLImageElement | null>(null); //Imagenes planta Principal
  const plantSecundariaImgRef = useRef<HTMLImageElement | null>(null); // Imagenes planta Secundaria
  const orderImgRef = useRef<HTMLImageElement | null>(null); // Imagenes para el Pedido
  const { simTime } = useSimTime(); // Obtiene el tiempo actual de simulacion de Time Context.
  const simTimeRef = useRef(simTime); // Falta averiguar sobre esto
  const [hoveredPlant, setHoveredPlant] = useState<Planta | null>(null); // Cuando el mouse esta encima, aqui se guarda la planta en cuestión.
  const [tooltipPosition, setTooltipPosition] = useState({ x: 0, y: 0 }); // Estas son las coordenadas donde mostrar el tooltip en pantalla.
  const [imagesLoaded, setImagesLoaded] = useState({ truck: false, plantPrincipal: false, plantSecundaria: false, order: false }); // Indica si cada imagen ya cargó.
  const animationFrameRef = useRef<number>(0); // Guarda el ID de requestAnimationFrame para poder detenerlo si es necesario
  const lastTimeRef = useRef<number>(0); // Guarda el tiempo de la última iteración de animación
  //Datos de la simulación 
  const [listSolucion, setlistSolucion] = useState<Solucion[]>([]);
  const [solucion, setSolucion] = useState<Solucion | null>(null);
  const [trucks, setTrucks] = useState<Camion[]>([]);
  const [plants, setPlants] = useState<Planta[]>([]);
  const [orders, setOrders] = useState<Pedido[]>([]);
  const [bloqueos, setBloqueos] = useState<Bloqueo[]>([]);
  const [routes, setRoutes] = useState<SubRuta[][]>([]);

  const { setActiveOrders, setActiveTrucks, selectedOrder } = useTransport();

  //Estado de carga.
  const [loading, setLoading] = useState(false); // Mientras es true, se muestra un mensaje de carga.
  const [error, setError] = useState<string | null>(null);

  //Traer controles desde TimeContext
  const { setStartTime, startSimulation, stopSimulation } = useSimTime();

  const [fechaInicio, setFechaInicio] = useState("");//Guarda la fecha y hora seleccionada por el usuario en el input de la interfaz.
  const [positionsInitialized, setPositionsInitialized] = useState(false);
  const fechaInicioRef = useRef<Date | null>(null);
  const routesRef = useRef<SubRuta[][]>([]);
  const trucksRef = useRef<Camion[]>([]);
  const ordersRef = useRef<Pedido[]>([]);
  const selectedOrderRef = useRef<Pedido | null>(null);
  const lastSimTimeRef = useRef<Date | null>(null);
  const [simulationTrigger, setSimulationTrigger] = useState(0);
  const fechaProxima = useRef<Date | null>(null);
  const cont = useRef<number>(0);
  const loop = useRef<number>(0);




  const [textoPedidos, setTextoPedidos] = useState<string>("");
  const [textoSubRutas, setTextoSubRutas] = useState<string>("");

  const [highlightPulse, setHighlightPulse] = useState(0);


  const [showErrorModal, setShowErrorModal] = useState(false);
  const [activeTab, setActiveTab] = useState<'camiones' | 'pedidos'>('camiones');
  const [camionStats, setCamionStats] = useState<CamionStats[]>([]);
  const [pedidoStats, setPedidoStats] = useState<PedidoStats[]>([]);


  const trucksProgressRef = useRef(
    routes.map(() => ({
      currentStep: 0,
      progress: 0,
      currentPos: [0, 0] as [number, number],
      targetPos: [0, 0] as [number, number]
    }))
  );



  useEffect(() => {
    selectedOrderRef.current = selectedOrder;
  }, [selectedOrder]);

  useEffect(() => {
    routesRef.current = routes;
  }, [routes]);

  useEffect(() => {
    ordersRef.current = orders;
  }, [orders]);

  useEffect(() => {
    trucksRef.current = trucks;
  }, [trucks]);



  // Cargar datos del backend
  useEffect(() => {

    const fetchData = async () => {
      console.log("Ingresoo aqui")
      try {
        const [plantas, bloqueosObtenidos] = await Promise.all([
          obtenerPlantas(),
          obtenerBloqueos()
        ]);
        console.log("PLANTAS:" + plantas);
        console.log("BLOQUEOS:" + bloqueosObtenidos);
        setPlants(plantas);
        setBloqueos(bloqueosObtenidos);
        setLoading(false);
        setPositionsInitialized(false);
      } catch (err) {
        setError('Error al cargar los datos de rutas');
        setLoading(false);
        console.error(err);
      }
    };

    fetchData();
  }, []);

  useEffect(() => {
    console.log("PLANTAS:" + plants);
  }, [plants])

  useEffect(() => {
    console.log("BLOQUEOS:" + bloqueos);
  }, [bloqueos])

  /*
  useEffect(() => {
    const fetchData2 = async () => {
      if (fechaInicioRef.current === null) return;
      console.log("Esta ingresando con la fecha de inicio: " + fechaInicio);
      try {
        const solucionesObtenidas: Solucion[] = [];
        const [solucion] = await Promise.all([
          obtenerSimulacionSemanal(fechaInicioRef.current.toISOString().replace("Z", ""), fechaInicioRef.current.toISOString().replace("Z", ""))
        ]);

        console.log("La solucion obtenida es: " + solucion.planesCamion);

        solucionesObtenidas.push(solucion);
        console.log("Las soluciones obtenidas son: " + solucionesObtenidas.map(sol => sol.planesCamion));

        // Procesar camiones y rutas
        const camiones = solucionesObtenidas.map(r => r.planesCamion[0].camion);
        const subRutas = solucionesObtenidas.map(r => r.planesCamion[0].subRutas);
        const pedidos = solucionesObtenidas[0].planesCamion.flatMap(plan =>
          plan.subRutas
            .filter(subRuta => subRuta.pedido)
            .map(subRuta => subRuta.pedido)
        ).filter(pedido => pedido) as Pedido[];
        console.log("Los pedidos son: " + pedidos.map(p => p.id));

        setlistSolucion(solucionesObtenidas);
        setTrucks(camiones);
        setRoutes(subRutas);
        setOrders(pedidos);
        setActiveOrders(pedidos);
        setActiveTrucks(camiones);
        setPositionsInitialized(false);

        startSimulation();
        startAnimation();

      } catch (err) {
        setError('Error al cargar los datos de rutas');
        setLoading(false);
        console.error(err);
      }
    };

    fetchData2();
  }, [simulationTrigger]); */

  //const fetchLoop = async () => {
  /*useEffect(() => {
    if (fechaInicioRef.current === null) return;
    let cancelado = false;

    const ejecutarLoop = async () => {
      try {
        //console.log("FECHA PROXIMA: " + fechaProxima.current + " FECHA INIcIO: " + fechaInicioRef.current + "FECHA ACTUAL: " + simTimeRef.current);
        //********************************** */
  /*if (
    cont.current === 0 ||
    (fechaProxima.current && Math.floor(simTimeRef.current.getTime() / 1000) === Math.floor(fechaProxima.current.getTime() / 1000))
  ) {
    console.log("Ingresooo")*/
  /*if (cont.current === 0) {
    // Establece la próxima fecha esperada 2h más adelante (en tiempo simulado)
    fechaProxima.current = new Date(fechaInicioRef.current.getTime() + 2 * 60 * 60 * 1000);
    console.log("Set fechaProxima por primera vez:", fechaProxima.current);
  }*/

  /*const isoStr = simTimeRef.current.toISOString().replace("Z", "");

  if (fechaInicioRef.current == null) return;
  //const start = Date.now();
  //console.log("FECHA PROXIMA: " + fechaProxima.current?.getTime().toString() + " FECHA INIcIO: " + fechaInicioRef.current + "FECHA ACTUAL: " + simTime);
  const [solucion] = await Promise.all([
    monitoreoDiario(fechaInicioRef.current.toISOString().replace("Z", ""), isoStr, cont.current)
  ]);
  if (!solucion) {
    console.warn("⚠️ La solución es null");
    setShowErrorModal(true);
    return;
  }
  const pedidosObtenidos = solucion.planesCamion.flatMap(plan =>
    plan.subRutas
      .filter(subRuta => subRuta.pedido)
      .map(subRuta => subRuta.pedido)
  ).filter(pedido => pedido) as Pedido[];

  pedidosObtenidos.forEach(pedido => {
    const horaSiguiente = new Date(pedido.horaSiguientePedido);
    console.log("FECHA DEL PEDIDOOOO: " + horaSiguiente);
    if (fechaProxima.current && fechaProxima.current < horaSiguiente) {
      // lógica aquí

      fechaProxima.current = horaSiguiente;
    }
  });
  if (fechaProxima.current != null && (fechaProxima.current?.getTime() < simTimeRef.current.getTime())) {
    // Add 30 seconds (30 * 1000 milliseconds) to the current timestamp of fechaProxima.current
    fechaProxima.current = new Date(simTimeRef.current.getTime() + 30 * 1000);
  }
  console.log("PROXIMA LLAMADA AL BACKKKKKKKK: " + fechaProxima.current);
  //console.log("La solucion obtenida con la nueva fecha:" + isoStr + " es: " + solucion.planesCamion.map(plan => plan.subRutas.map(subR => subR.trayectoria.map(tray => tray.posX + " " + tray.posY))));
  setlistSolucion(prev => [...prev, solucion]);
  //console.log("NUEVOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO") ************************
  ///fechaActual = new Date(fechaActual.getTime() + 6 * 60 * 1000 + 40 * 1000); // 6 min 40 s simulados
  //await new Promise(resolve => setTimeout(resolve, 7));
  //const elapsed = Date.now() - start; 
  /*await new Promise((resolve) =>
    setTimeout(resolve, 0)  // Espera 90 segundos
  );*/
  /*cont.current = 1;
}
} catch (error) {
console.error("Error al obtener solución:", error);
}
setTimeout(ejecutarLoop, 1000);
};
ejecutarLoop();
return () => { cancelado = true; };
}, [loop.current]);*/

  /*useEffect(() => {
    fechaInicioRef.current = new Date();
    fechaProxima.current = fechaInicioRef.current;
    /*********************console.log("FECHA ACTUAL: " + fechaInicioRef.current);*/
  /*if (fechaInicioRef.current !== null) {
    loop.current = 1;
  }
}, []);*/

  useEffect(() => {
    /*if (!fechaInicioRef.current) return;

    const fecha = fechaInicioRef.current.toISOString().replace("Z", "");
    console.log("Iniciando simulación día a día con fecha:", fecha);*/
    const ahora = new Date().toISOString().replace("Z", "");
    iniciarSimulacionDiaDiaSocket(ahora, (solucion) => {
      if (!solucion) {
        // Mostrar error o mensaje de colapso
        return;
      }

      setlistSolucion(prev => [...prev, solucion]);
    });

    return () => {
      detenerSimulacionDiaDiaSocket();
    };
  }, []);


  /*useEffect(() => {
    if (fechaInicioRef.current !== null) {
      fetchLoop();
    }
  }, [simulationTrigger]);*/

  const indiceRef = useRef(0);
  const contadorRef = useRef(0);
  const banderaRef = useRef(true);

  useEffect(() => {
    if (listSolucion.length === 0) return;

    // Este valor solo vive dentro del efecto
    let cont = 0;
    const intervalo = setInterval(() => {
      if (!fechaInicioRef.current) return;

      //********console.log("Indice antiguo:", lastIndiceProcesado + " y indice.RefCurrent:" + indiceRef.current + " y el indice Calculado es: " + indiceCalculado);
      // ✅ Solo procesar si se avanzó a un nuevo bloque
      //if (indiceCalculado > lastIndiceProcesado && (indiceCalculado !== indiceRef.current || indiceRef.current === 0)) {
      console.log("La fecha Proxima es: " + fechaProxima.current + " y la SimTime es: " + simTimeRef.current);
      if (cont == 0 || (fechaProxima.current && Math.floor(simTimeRef.current.getTime() / 1000) === Math.floor(fechaProxima.current.getTime() / 1000))) {
        console.log("✅ Actualizando índice:", indiceRef.current, "en SimTime:", simTimeRef.current.toISOString());


        const sol = listSolucion[indiceRef.current];
        if (!sol) return;

        //console.log("✅ Actualizando índice:", indiceRef.current, "en SimTime:", simTime.toISOString());

        const activeTrucks = sol.planesCamion.map(plan => plan.camion);
        const activeRoutes = sol.planesCamion.map(plan => plan.subRutas);
        const activeOrders = sol.planesCamion.flatMap(plan =>
          plan.subRutas
            .filter(subRuta => subRuta.pedido)
            .map(subRuta => subRuta.pedido)
        ).filter(pedido => pedido) as Pedido[];

        setTextoPedidos(
          activeOrders.map(p => `Pedido ${p.id} en (${p.destino.posX}, ${p.destino.posY}) HoraP:${p.horaPedido} y entregado: ${p.entregado}`).join("\n")
        );

        setTextoSubRutas(
          activeRoutes.map(subRuta =>
            subRuta.map(r => `Inicio: ${r.horaInicio} (${r.inicio.posX},${r.inicio.posY}) → (${r.fin.posX},${r.fin.posY}) ${r.horaFin}`).join("\n")
          ).join("\n\n")
        );

        setTrucks(activeTrucks);
        setRoutes(activeRoutes);
        setOrders(activeOrders);
        setActiveOrders(activeOrders);
        setActiveTrucks(activeTrucks);

        contadorRef.current += 1;
        if (contadorRef.current === 1) {
          startSimulation();
          startAnimation();
        }
        cont = 1;
        indiceRef.current++;
      }
    }, 1000); // Evaluar cada 40ms reales, pero solo ejecutar si hay cambio de bloque

    return () => clearInterval(intervalo);
  }, [listSolucion]);

  useEffect(() => {

    // 1. Captura el valor del input sin modificaciones
    const fechaInput = new Date();
    setFechaInicio(fechaInput.toISOString());

    // 2. Crea la fecha en la zona horaria local del navegador
    const fechaLocal = new Date(fechaInput);

    // 3. Envía la fecha local directamente al TimeContext
    fechaInicioRef.current = fechaLocal;
    setStartTime(fechaLocal);
    startSimulation();

  }, []);

  useEffect(() => {
    simTimeRef.current = simTime;
    //*******console.log("simTime actualizado:", simTime);
  }, [simTime]);

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

  useEffect(() => {
    if (!Object.values(imagesLoaded).every(Boolean) || loading) return;

    trucksProgressRef.current = routes.map((subRutas) => {
      const fullRoute = subRutas.flatMap(sr => sr.trayectoria);
      if (fullRoute.length < 2) {
        const only = fullRoute[0] || { posX: 0, posY: 0 };
        return {
          currentStep: 0,
          progress: 0,
          currentPos: [only.posX, only.posY],
          targetPos: [only.posX, only.posY]
        };
      }

      return {
        currentStep: 0,
        progress: 0,
        currentPos: [fullRoute[0].posX, fullRoute[0].posY],
        targetPos: [fullRoute[1].posX, fullRoute[1].posY]
      };
    });

  }, [trucks, routes]);
  useEffect(() => {

    drawInitialState();
  }, [imagesLoaded, loading]);

  useEffect(() => {
    if (showErrorModal) {
      const fetchResumen = async () => {
        try {
          const [camiones, pedidos] = await Promise.all([
            obtenerEstadisticasCamionesDiaDia(),
            obtenerEstadisticasPedidosDiaDia()
          ]);
          setCamionStats(camiones);
          setPedidoStats(pedidos);
        } catch (err) {
          console.error('Error al cargar resumen:', err);
        }
      };

      fetchResumen();
    }
  }, [showErrorModal]);


  const drawBloqueos = useCallback((
    ctx: CanvasRenderingContext2D,
    spacing: number,
    currentTime: Date
  ) => {
    if (!bloqueos || bloqueos.length === 0) return;

    const ahora = currentTime.getTime();
    const bloqueosActivos = bloqueos.filter(bloqueo => {
      try {
        const inicio = new Date(bloqueo.inicio).getTime();
        const fin = new Date(bloqueo.fin).getTime();
        return ahora >= inicio && ahora <= fin;
      } catch (e) {
        console.error('Error procesando fechas de bloqueo:', e);
        return false;
      }
    });
    //console.log("Dibujando bloqueos activos:", bloqueosActivos);

    bloqueosActivos.forEach(bloqueo => {
      const nodos = bloqueo.nodos;
      if (nodos.length < 2) return; // No se puede dibujar una línea con menos de 2 nodos

      ctx.save();
      ctx.strokeStyle = 'black';
      ctx.lineWidth = 4;
      ctx.beginPath();

      const startX = nodos[0].posX * spacing;
      const startY = nodos[0].posY * spacing;
      ctx.moveTo(startX, startY);

      for (let i = 1; i < nodos.length; i++) {
        const x = nodos[i].posX * spacing;
        const y = nodos[i].posY * spacing;
        ctx.lineTo(x, y);
      }

      ctx.stroke();
      ctx.restore();
    });
  }, [bloqueos]);

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

  // Añade este efecto para la animación pulsante
  useEffect(() => {
    //console.log("selectedOrder CAMBIÓ:", selectedOrder);
    if (!selectedOrder) {
      setHighlightPulse(0); // Resetear animación
      return;
    }


    if (highlightPulse !== 0) return;

    let animationId: number;
    const animate = () => {
      setHighlightPulse(prev => (prev + 0.02) % (Math.PI * 2));
      animationId = requestAnimationFrame(animate);
    };

    animationId = requestAnimationFrame(animate);
    return () => cancelAnimationFrame(animationId);
  }, [selectedOrder]);


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
    drawBloqueos(ctx, spacing, simTimeRef.current);
    if (hoveredPlant && ctx) {
      drawPlantTooltip(ctx, hoveredPlant, tooltipPosition.x, tooltipPosition.y);
    }

    plants.forEach(plant => {
      drawPlant(ctx, plant.ubicacion.posX, plant.ubicacion.posY, plant, spacing);
    });

    ordersRef.current.forEach(order => {
      let horaFin: number | null = null;

      const horaInicio = new Date(order.horaPedido).getTime();
      routesRef.current.forEach(subRutas => {
        subRutas.forEach(subRuta => {
          if (subRuta.pedido && subRuta.pedido.id === order.id) {
            //console.log("El pedido "+order.id+" va desde la hora: "+order.horaPedido+" hasta la hora: "+subRuta.horaFin);
            horaFin = new Date(subRuta.horaFin).getTime();
          }
        });
      });


      //console.log("El pedido "+order.id+" va desde la hora: "+order.horaPedido+" hasta la hora: "+horaFin);
      if (horaFin !== null &&
        simTimeRef.current.getTime() >= horaInicio &&
        simTimeRef.current.getTime() <= horaFin) {

        drawOrder(ctx, order.destino.posX, order.destino.posY, order, spacing);
      }
    });



    // Dibujar rutas activas como en tu versión original  
    routesRef.current.forEach((subRutas, index) => {
      const color = `hsl(${(index * 30) % 360}, 70%, 50%)`;

      const activeSubRutas = subRutas.filter(subRuta => {
        const horaInicio = new Date(subRuta.horaInicio).getTime();
        const horaFin = new Date(subRuta.horaFin).getTime();
        return simTimeRef.current.getTime() >= horaInicio && simTimeRef.current.getTime() <= horaFin;
      });
      activeSubRutas.forEach(subRuta => {
        drawRoute(ctx, subRuta.trayectoria, color, spacing);
      });
    });

    // Calcular avance de camiones en función del tiempo simulado
    const tiempoSimuladoTranscurrido = lastSimTimeRef.current
      ? (simTimeRef.current.getTime() - lastSimTimeRef.current.getTime()) / 1000
      : 0;

    lastSimTimeRef.current = new Date(simTimeRef.current);


    routesRef.current.forEach((subRutas, index) => {
      const progressData = trucksProgressRef.current[index];
      if (!progressData) return;
      const truck = trucksRef.current[index];
      //console.log("El truck es: "+truck+" y el subRutas son: "+subRutas+ " y la subRutas.length es:   "+subRutas.length);
      if (!truck || !subRutas || subRutas.length === 0) return;

      const rutasVisibles = subRutas.filter(subRuta => {
        const horaInicio = new Date(subRuta.horaInicio).getTime();
        return horaInicio <= simTimeRef.current.getTime();
      });
      //console.log("Las rutasVisibles son: "+ rutasVisibles.length);
      const fullRoute = rutasVisibles.flatMap(subRuta => subRuta.trayectoria);
      if (fullRoute.length < 2) return;

      if (progressData.currentStep === -1) {
        const firstPos = fullRoute[0];
        const secondPos = fullRoute[1] || fullRoute[0];
        progressData.currentStep = 0;
        progressData.currentPos = [firstPos.posX, firstPos.posY];
        progressData.targetPos = [secondPos.posX, secondPos.posY];
        progressData.progress = 0;
      }

      if (progressData.currentStep >= fullRoute.length - 1) {
        const lastPos = fullRoute[fullRoute.length - 1] || { posX: 0, posY: 0 };
        progressData.currentPos = [lastPos.posX, lastPos.posY];
        drawTruck(ctx, progressData.currentPos[0], progressData.currentPos[1], truck, spacing, progressData.currentPos, progressData.currentPos, true);
        return;
      }

      const currentNode = fullRoute[progressData.currentStep] || { posX: 0, posY: 0 };
      const nextNode = fullRoute[progressData.currentStep + 1] || { posX: 0, posY: 0 };

      // Suponiendo que 1 unidad = 1 km, ajusta aquí si tu escala es distinta
      const distanciaKm = Math.sqrt(
        Math.pow(nextNode.posX - currentNode.posX, 2) +
        Math.pow(nextNode.posY - currentNode.posY, 2)
      );

      const velocidadKmH = 50;
      const tiempoEntreNodosSegundos = (distanciaKm / velocidadKmH) * 3600;

      progressData.progress += tiempoSimuladoTranscurrido;

      if (progressData.progress >= tiempoEntreNodosSegundos) {
        progressData.progress = 0;
        progressData.currentStep++;
        const currentStep = fullRoute[progressData.currentStep] || { posX: 0, posY: 0 };

        const nextStep = fullRoute[progressData.currentStep + 1] || { posX: 0, posY: 0 };
        progressData.currentPos = [currentStep.posX, currentStep.posY];
        progressData.targetPos = [nextStep.posX, nextStep.posY];
      }

      // Verifica si el currentPos coincide con el final de alguna subRuta
      rutasVisibles.forEach(subRuta => {
        const ultimaUbicacion = subRuta.trayectoria[subRuta.trayectoria.length - 1];
        if (
          progressData.currentPos[0] === ultimaUbicacion.posX &&
          progressData.currentPos[1] === ultimaUbicacion.posY &&
          subRuta.pedido
        ) {
          console.log(`✅ Pedido entregado: ${subRuta.pedido.id}`);
          setActiveOrders(prevOrders =>
            prevOrders.map(order =>
              order.id === subRuta.pedido?.id
                ? { ...order, entregado: true }
                : order
            )
          );
          // Aquí puedes cambiar el estado del pedido, emitir un evento, etc.
          subRuta.pedido = null; // O una propiedad tipo subRuta.entregado = true;
        }
      });

      const t = Math.min(progressData.progress / tiempoEntreNodosSegundos, 1);
      const interpolatedX = progressData.currentPos[0] + (progressData.targetPos[0] - progressData.currentPos[0]) * t;
      const interpolatedY = progressData.currentPos[1] + (progressData.targetPos[1] - progressData.currentPos[1]) * t;

      // console.log("Llego aqui")
      drawTruck(ctx, interpolatedX, interpolatedY, truck, spacing, progressData.targetPos, progressData.currentPos, false);
    });
    // console.log("Al selectOrder ingreso con: "+ selectedOrder);
    if (selectedOrderRef.current) {
      //  console.log("Ingresooooo aquiiii")
      const pulseSize = 15 + Math.sin(highlightPulse) * 5;
      const pulseAlpha = 0.4 + Math.sin(highlightPulse * 2) * 0.3;

      // Círculo de resaltado
      ctx.save();
      ctx.fillStyle = `rgba(255, 255, 0, ${pulseAlpha})`;
      ctx.beginPath();
      ctx.arc(
        selectedOrderRef.current.destino.posX * spacing,
        selectedOrderRef.current.destino.posY * spacing,
        pulseSize,
        0,
        Math.PI * 2
      );
      ctx.fill();
      ctx.restore();

      // Borde
      ctx.save();
      ctx.strokeStyle = `rgba(255, 165, 0, 0.8)`;
      ctx.lineWidth = 2;
      ctx.beginPath();
      ctx.arc(
        selectedOrderRef.current.destino.posX * spacing,
        selectedOrderRef.current.destino.posY * spacing,
        pulseSize + 3,
        0,
        Math.PI * 2
      );
      ctx.stroke();
      ctx.restore();

      // Texto
      ctx.save();
      ctx.scale(1, -1);
      ctx.fillStyle = 'black';
      ctx.font = 'bold 12px Arial';
      ctx.fillText(
        `Pedido ${selectedOrderRef.current.id}`,
        selectedOrderRef.current.destino.posX * spacing - 20,
        -selectedOrderRef.current.destino.posY * spacing - pulseSize - 5
      );
      ctx.restore();
    }
    animationFrameRef.current = requestAnimationFrame(animate);

  }, [drawGrid, drawTruck, drawPlant, drawOrder, drawRoute, plants, orders, trucks, routes, imagesLoaded, simTime, hoveredPlant, tooltipPosition, selectedOrder]);

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
    drawBloqueos(ctx, spacing, simTimeRef.current);
    // Dibujar plantas
    plants.forEach(plant => {
      drawPlant(ctx, plant.ubicacion.posX, plant.ubicacion.posY, plant, spacing);
    });
  }, [imagesLoaded, plants, drawGrid, drawPlant]);


  const startAnimation = useCallback(() => {
    cancelAnimationFrame(animationFrameRef.current);
    lastTimeRef.current = 0;
    animationFrameRef.current = requestAnimationFrame(animate);
  }, [animate]);

  const stopAnimation = useCallback(() => {
    cancelAnimationFrame(animationFrameRef.current);
  }, []);

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
      return x >= plantX - size / 2 &&
        x <= plantX + size / 2 &&
        y >= plantY - size / 2 &&
        y <= plantY + size / 2;
    });

    setHoveredPlant(hovered || null);
  };

  const handleFechaChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    // 1. Captura el valor del input sin modificaciones
    const fechaInput = e.target.value;
    setFechaInicio(fechaInput);

    // 2. Crea la fecha en la zona horaria local del navegador
    const fechaLocal = new Date(fechaInput);

    // 3. Envía la fecha local directamente al TimeContext
    fechaInicioRef.current = fechaLocal;
    setStartTime(fechaLocal);
  };







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
      <div className="absolute top-4 left-16 z-10 flex gap-2">

      </div>

      <div className="absolute inset-0 flex items-center justify-center overflow-auto">
        {/*<div className="absolute bottom-30 left-4 z-20 bg-white p-2 rounded shadow-md max-h-120 overflow-auto w-104 text-xs">
          <strong className="block mb-1">Pedidos Activos:</strong>
          <pre className="mb-2 whitespace-pre-wrap">{textoPedidos}</pre>

          <strong className="block mb-1">SubRutas Activas:</strong>
          <pre className="whitespace-pre-wrap">{textoSubRutas}</pre>
        </div>*/}
        <canvas
          ref={canvasRef}
          className="bg-white border border-gray-400"
          onMouseMove={handleCanvasHover}
          onMouseOut={() => setHoveredPlant(null)}
        />

        {/* ✅ MODAL DE ERROR POR SOLUCIÓN NULA */}
        {showErrorModal && (
          <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center">
            <div className="bg-white rounded-lg w-[90%] max-w-4xl max-h-[90%] overflow-auto shadow-lg">
              <div className="p-4 border-b text-center">
                <h2 className="text-xl font-bold text-gray-800">📊 Pantalla Resumen</h2>
                <div className="mt-4 flex justify-center gap-4">
                  <button
                    className={`px-4 py-1 border-b-2 ${activeTab === 'camiones' ? 'border-blue-500 font-semibold' : 'text-gray-500'}`}
                    onClick={() => setActiveTab('camiones')}
                  >
                    Vehículos
                  </button>
                  <button
                    className={`px-4 py-1 border-b-2 ${activeTab === 'pedidos' ? 'border-blue-500 font-semibold' : 'text-gray-500'}`}
                    onClick={() => setActiveTab('pedidos')}
                  >
                    Pedidos
                  </button>
                </div>
              </div>

              <div className="p-4">
                {activeTab === 'camiones' && (
                  <table className="w-full text-sm text-left border">
                    <thead className="bg-gray-100">
                      <tr>
                        <th className="p-2 border">Código</th>
                        <th className="p-2 border">Total Entregados</th>
                        <th className="p-2 border">% Global</th>
                      </tr>
                    </thead>
                    <tbody>
                      {camionStats.map((camion) => (
                        <tr key={camion.codigo}>
                          <td className="p-2 border">{camion.codigo}</td>
                          <td className="p-2 border">{camion.totalEntregados}</td>
                          <td className="p-2 border">{camion.porcentajeGlobal.toFixed(2)}%</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}

                {activeTab === 'pedidos' && (
                  <table className="w-full text-sm text-left border">
                    <thead className="bg-gray-100">
                      <tr>
                        <th className="p-2 border">ID</th>
                        <th className="p-2 border">Hora Pedido</th>
                        <th className="p-2 border">Plazo Máx</th>
                        <th className="p-2 border">Hora Entrega</th>
                        <th className="p-2 border">T. Disponible</th>
                        <th className="p-2 border">T. Real</th>
                        <th className="p-2 border">% Usado</th>
                      </tr>
                    </thead>
                    <tbody>
                      {pedidoStats.map((pedido) => (
                        <tr key={pedido.id}>
                          <td className="p-2 border">{pedido.id}</td>
                          <td className="p-2 border">{pedido.horaPedido}</td>
                          <td className="p-2 border">{pedido.plazoMaximoEntrega}</td>
                          <td className="p-2 border">{pedido.horaEntrega}</td>
                          <td className="p-2 border">{pedido.tiempoDisponible} min</td>
                          <td className="p-2 border">{pedido.tiempoReal} min</td>
                          <td className="p-2 border">{pedido.porcentajeUtilizado.toFixed(2)}%</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>

              <div className="p-4 border-t text-center">
                <button
                  className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded"
                  onClick={() => window.location.reload()}
                >
                  Aceptar y recargar
                </button>
              </div>
            </div>
          </div>
        )}
      </div>


    </div>
  );
}


