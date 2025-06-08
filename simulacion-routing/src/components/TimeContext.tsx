"use client";
import React, { createContext, useContext, useEffect, useState, ReactNode } from "react";

interface TimeContextType {
    simTime: Date;
    isRunning: boolean;
    setStartTime: (start: Date) => void;
    start: () => void;
    stop: () => void;
}

const TimeContext = createContext<TimeContextType | null>(null);

export const TimeProvider = ({ children }: { children: ReactNode }) => {
    const [simTime, setSimTime] = useState(new Date());
    const [_startTime, _setStartTime] = useState(new Date());
    const [isRunning, setIsRunning] = useState(false);
    const [t0, setT0] = useState(Date.now());

    useEffect(() => {
        let interval: NodeJS.Timeout;
        if (isRunning) {
            interval = setInterval(() => {
                const delta = Date.now() - t0;
                setSimTime(new Date(_startTime.getTime() + delta));
            }, 1000);
        }
        return () => clearInterval(interval);
    }, [isRunning, _startTime, t0]);

    return (
        <TimeContext.Provider
            value={{
                simTime,
                isRunning,
                setStartTime: (start: Date) => {
                    const midnight = new Date(start);
                    midnight.setHours(0, 0, 0, 0);

                    _setStartTime(midnight);     // ✅ esto es correcto
                    setSimTime(midnight);
                    setT0(Date.now());
                },
                start: () => {
                    setT0(Date.now());
                    setIsRunning(true);
                },
                stop: () => setIsRunning(false),
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
