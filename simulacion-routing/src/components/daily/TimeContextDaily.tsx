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
  // Inicializa con fecha actual
  const initialDate = new Date();

  const [simTime, setSimTime] = useState<Date>(initialDate);
  const [_startTime, _setStartTime] = useState<Date>(initialDate);
  const [isRunning, setIsRunning] = useState(false);
  const intervalRef = useRef<NodeJS.Timeout | null>(null);

  const startSimulation = () => {
    if (intervalRef.current) return;

    setSimTime(_startTime);
    setIsRunning(true);
    intervalRef.current = setInterval(() => {
      setSimTime((prev) => new Date(prev.getTime() + 1000)); // Avanza 1s en UTC
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
    _setStartTime(start);
    setSimTime(start);
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
