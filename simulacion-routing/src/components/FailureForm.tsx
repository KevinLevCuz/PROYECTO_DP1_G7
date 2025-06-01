export default function FailureForm() {
  return (
    <div className="mb-6">
      <h3 className="font-bold mb-2">Registro de Averías</h3>
      <form className="space-y-2 text-sm">
        <div>
          <label className="block">Camión:</label>
          <select className="border p-1 w-full rounded">
            <option>Seleccione un camión</option>
            <option>Camion 1</option>
            <option>Camion 2</option>
          </select>
        </div>
        <div>
          <label className="block">Tipo de Avería:</label>
          <select className="border p-1 w-full rounded">
            <option>Seleccione tipo</option>
            <option>Mecánica</option>
            <option>Eléctrica</option>
          </select>
        </div>
        <button 
          type="button"
          className="bg-red-500 text-white px-3 py-1 rounded text-sm"
        >
          Reportar Avería
        </button>
      </form>
    </div>
  );
}