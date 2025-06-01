export default function StatusBar() {
  const currentDate = new Date().toLocaleDateString('es-ES');
  const currentTime = new Date().toLocaleTimeString('es-ES');
  
  return (
    <div className="bg-red-500 text-white p-2 text-sm grid grid-cols-4 gap-4 justify-center">
      <div className="text-center">
        <span className="font-semibold">Tiempo Real:</span> {currentTime}
      </div>
      <div className="text-center">
        <span className="font-semibold">Tiempo Simulación:</span> {currentTime}
      </div>
      <div className="text-center">
        <span className="font-semibold">Vehículos:</span> 20
      </div>
      <div className="text-center">
        <span className="font-semibold">Ped. Entregados:</span> 20/722
      </div>
    </div>
  );
}