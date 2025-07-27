"use client";
import React, {
  createContext,
  useContext,
  useEffect,
  useState,
  ReactNode,
  useRef,
} from "react";

interface TimeContextType {
  simTime: Date;
  isRunning: boolean;
  setStartTime: (start: Date) => void;
  startSimulation: () => void;
  stopSimulation: () => void;
}

const TimeContext = createContext<TimeContextType | null>(null);

export const TimeProvider = ({ children }: { children: ReactNode }) => {
  // Inicializa con UTC explícito
  const initialDate = new Date("2025-06-06T00:00:00Z"); // Nota la Z al final
  
  const [simTime, setSimTime] = useState<Date>(new Date("2025-06-06T00:00:00Z"));
  const [_startTime, _setStartTime] = useState<Date>(new Date("2025-06-06T00:00:00Z"));
  const [isRunning, setIsRunning] = useState(false);
  const intervalRef = useRef<NodeJS.Timeout | null>(null);

  // Función para normalizar a UTC
  const toUTC = (date: Date) => {
    return new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  };

  // Función para mostrar en hora local
  const toLocal = (date: Date) => {
    return new Date(date.getTime() + date.getTimezoneOffset() * 60000);
  };

  const startSimulation = () => {
    if (intervalRef.current) return;

    // Usa la hora UTC para cálculos
    setSimTime(_startTime);

    setIsRunning(true);
    intervalRef.current = setInterval(() => {
      setSimTime((prev) => new Date(prev.getTime() + 1000)); // Avanza 1s en UTC
    }, 30);
  };

  const getLocalTime = (date: Date) => {
    return new Date(date.getTime());
  };
  const stopSimulation = () => {
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
      setIsRunning(false);
    }
  };

  const setStartTime = (start: Date) => {
    // Convierte a UTC manteniendo el mismo instante temporal
    const utcDate = new Date(start.getTime());
    _setStartTime(utcDate);
    setSimTime(utcDate);
  };

  return (
    <TimeContext.Provider
      value={{
        simTime: getLocalTime(simTime),
        isRunning,
        setStartTime,
        startSimulation,
        stopSimulation,
      }}
    >
      {children}
    </TimeContext.Provider>
  );
};

export const useSimTime = () => {
  const ctx = useContext(TimeContext);
  if (!ctx) throw new Error("useSimTime debe usarse dentro de TimeProvider");
  return ctx;
};