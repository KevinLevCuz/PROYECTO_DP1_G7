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
  startTime: Date;
  realTimeFromStart: Date;
  isRunning: boolean;
  setStartTime: (start: Date) => void;
  startSimulation: () => void;
  stopSimulation: () => void;
  elapsedSimulatedTime: Date;
  elapsedRealTime: Date;
}

const TimeContext = createContext<TimeContextType | null>(null);

export const TimeProvider = ({ children }: { children: ReactNode }) => {
  // Inicializa con UTC explícito
  const initialDate = new Date("2025-06-06T00:00:00Z");

  const [simTime, setSimTime] = useState<Date>(new Date(initialDate));
  const [startTime, setStartTime] = useState<Date>(new Date(initialDate)); // <-- Estado separado para startTime
  const [isRunning, setIsRunning] = useState(false);
  const intervalRef = useRef<NodeJS.Timeout | null>(null);
  const [realTimeFromStart, setRealTimeFromStart] = useState<Date>(new Date(startTime));
  const realTimeIntervalRef = useRef<NodeJS.Timeout | null>(null);


  // Función para normalizar a UTC
  const toUTC = (date: Date) => {
    return new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  };

  // Función para mostrar en hora local
  const toLocal = (date: Date) => {
    return new Date(date.getTime() + date.getTimezoneOffset() * 60000);
  };

  const getElapsedSimulatedTime = () => {
    return new Date(simTime.getTime() - startTime.getTime());
  };

  const getElapsedRealTime = () => {
    return new Date(realTimeFromStart.getTime() - startTime.getTime());
  };

  const startSimulation = () => {
    if (intervalRef.current) return;

    setIsRunning(true);
    intervalRef.current = setInterval(() => {
      setSimTime((prev) => new Date(prev.getTime() + 1000)); // Avanza 1s en UTC
    }, 15);


    // Tiempo real desde startTime
    setRealTimeFromStart(new Date(startTime)); // reinicia
    realTimeIntervalRef.current = setInterval(() => {
      setRealTimeFromStart((prev) => new Date(prev.getTime() + 1000)); // cada 1s
    }, 1000);
  };

  const getLocalTime = (date: Date) => {
    return new Date(date.getTime());
  };

  const stopSimulation = () => {
    /*if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
      setIsRunning(false);
    }*/
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
    }
    if (realTimeIntervalRef.current) {
      clearInterval(realTimeIntervalRef.current);
      realTimeIntervalRef.current = null;
    }
    setIsRunning(false);
  };

  const handleSetStartTime = (start: Date) => {
    const utcDate = new Date(start.getTime());
    setStartTime(utcDate); // <-- Actualizamos startTime
    setSimTime(utcDate);
  };

  return (
    <TimeContext.Provider
      value={{
        simTime: getLocalTime(simTime),
        startTime: getLocalTime(startTime),
        realTimeFromStart: getLocalTime(realTimeFromStart),
        isRunning,
        setStartTime: handleSetStartTime,
        startSimulation,
        stopSimulation,
        elapsedSimulatedTime: getElapsedSimulatedTime(),
        elapsedRealTime: getElapsedRealTime()
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
