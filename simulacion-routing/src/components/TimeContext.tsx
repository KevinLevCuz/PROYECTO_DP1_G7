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
  const [simTime, setSimTime] = useState(new Date("2025-06-06T00:00:00")); // Inicial en 0
  const [_startTime, _setStartTime] = useState(new Date("2025-06-06T00:00:00"));
  const [isRunning, setIsRunning] = useState(false);
  const intervalRef = useRef<NodeJS.Timeout | null>(null);

  const startSimulation = () => {
    if (intervalRef.current) return; // prevenir múltiples intervalos
    setIsRunning(true);
    intervalRef.current = setInterval(() => {
      setSimTime((prev) => new Date(prev.getTime() + 1000)); // avanzar 1s
    }, 1000);
  };

  const stopSimulation = () => {
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
      setIsRunning(false);
    }
  };

  const setStartTime = (start: Date) => {
    const startAtMidnight = new Date(start);
    startAtMidnight.setHours(0, 0, 0, 0);
    _setStartTime(startAtMidnight);
    setSimTime(startAtMidnight);
  };

  return (
    <TimeContext.Provider
      value={{
        simTime,
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
