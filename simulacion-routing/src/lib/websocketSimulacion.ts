import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

let client: Client;

export const conectarSimulacionSemanal = (fechaInicio: string, onMensaje: (solucion: any) => void) => {
  client = new Client({
    brokerURL: 'wss://h982equipo7g.duckdns.org/ws', // Cambia si usas nginx o dominio
    connectHeaders: {},
    debug: (str) => console.log(str),
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
    webSocketFactory: () => new SockJS('https://h982equipo7g.duckdns.org/ws'), // Si usas SockJS en back
  });

  client.onConnect = () => {
    console.log("🧩 Conectado al WebSocket");

    client.subscribe('/topic/simulacionSemanal', (message) => {
      const solucion = JSON.parse(message.body);
      if (solucion.colapso) {
        console.warn("💥 Colapso detectado");
        // Aquí podrías mostrar un modal, detener la simulación, redireccionar, etc.
        onMensaje(null);
        return; // Salimos si hay colapso
      }
      console.log("🧩 Mensaje recibido:", solucion);
      onMensaje(solucion); // Callback al frontend
    });

    // Publicar mensaje para iniciar la simulación
    client.publish({
      destination: '/app/IniciarSimulacionSemanal',
      body: JSON.stringify({ ahora: fechaInicio })
    });
  };

  client.activate();
};

export const desconectarSimulacion = () => {
  if (client) {
    client.deactivate();
  }
};
