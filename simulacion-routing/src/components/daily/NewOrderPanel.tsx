// components/daily/NewOrderPanel.tsx

"use client";
import { useEffect, useState } from "react";
import { FiChevronRight, FiChevronLeft, FiX, FiPlus } from "react-icons/fi";
import axios from "axios";

export default function NewOrderPanel() {
  const [isOpen, setIsOpen] = useState(false);
  const [formData, setFormData] = useState({
    codigoCliente: '',
    posX: '',
    posY: '',
    cantidadGLP: '',
    tiempoMaximo: ''
  });

  useEffect(() => {
    console.log("NewOrderPanel montado");
  }, []);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const ahora = new Date();
    const horaPedido = `${ahora.getFullYear()}-${String(ahora.getMonth() + 1).padStart(2, '0')}-${String(ahora.getDate()).padStart(2, '0')}T${String(ahora.getHours()).padStart(2, '0')}:${String(ahora.getMinutes()).padStart(2, '0')}:${String(ahora.getSeconds()).padStart(2, '0')}`;
    const plazo = new Date(ahora.getTime() + parseInt(formData.tiempoMaximo) * 60 * 60 * 1000);

    const plazoMaximo = `${plazo.getFullYear()}-${String(plazo.getMonth() + 1).padStart(2, '0')}-${String(plazo.getDate()).padStart(2, '0')}T${String(plazo.getHours()).padStart(2, '0')}:${String(plazo.getMinutes()).padStart(2, '0')}:${String(plazo.getSeconds()).padStart(2, '0')}`;


    const payload = {
      idCliente: formData.codigoCliente,
      posX: parseInt(formData.posX),
      posY: parseInt(formData.posY),
      cantidadGlp: parseFloat(formData.cantidadGLP),
      horaPedido: horaPedido,
      plazoMaximoEntrega: plazoMaximo
    };

    try {
      const response = await axios.post("http://localhost:8080/api/pedidos/registrarPedido", payload);
      console.log("Pedido creado correctamente:", response.data);
    } catch (error) {
      console.error("Error al crear pedido:", error);
    }

    // Limpiar formulario
    setFormData({
      codigoCliente: '',
      posX: '',
      posY: '',
      cantidadGLP: '',
      tiempoMaximo: ''
    });
  };

  return (
    <>
      {!isOpen && (
        <button
          onClick={() => setIsOpen(true)}
          className="fixed left-16 top-1/4 transform -translate-y-1/2 bg-red-500 text-white p-2 rounded-r-lg shadow-lg z-30"
        >
          <FiChevronRight size={20} />
        </button>
      )}

      <div className={`fixed left-16 top-22 h-[calc(62vh-3rem)] bg-white border-r shadow-lg transition-transform duration-300 z-20 ${isOpen ? 'translate-x-0' : '-translate-x-full'}`}
        style={{ width: '300px' }}>
        <div className="h-full flex flex-col">
          <div className="bg-red-500 text-white p-3 flex justify-between items-center">
            <h3 className="font-semibold">Nuevo Pedido</h3>
            <button
              onClick={() => setIsOpen(false)}
              className="text-white hover:text-red-200"
            >
              <FiX size={20} />
            </button>
          </div>

          <div className="p-3 flex-1 overflow-y-auto">
            <form onSubmit={handleSubmit} className="space-y-1">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Código del Cliente
                </label>
                <input
                  type="text"
                  name="codigoCliente"
                  value={formData.codigoCliente}
                  onChange={handleInputChange}
                  className="w-full p-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 text-sm"
                  required
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Posición X
                  </label>
                  <input
                    type="number"
                    name="posX"
                    value={formData.posX}
                    onChange={handleInputChange}
                    className="w-full p-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 text-sm"
                    required
                    min="0"
                    max="70"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Posición Y
                  </label>
                  <input
                    type="number"
                    name="posY"
                    value={formData.posY}
                    onChange={handleInputChange}
                    className="w-full p-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 text-sm"
                    required
                    min="0"
                    max="50"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Cantidad de GLP (kg)
                </label>
                <input
                  type="number"
                  name="cantidadGLP"
                  value={formData.cantidadGLP}
                  onChange={handleInputChange}
                  className="w-full p-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 text-sm"
                  required
                  min="1"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Tiempo Máximo de Entrega (horas)
                </label>
                <input
                  type="number"
                  name="tiempoMaximo"
                  value={formData.tiempoMaximo}
                  onChange={handleInputChange}
                  className="w-full p-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 text-sm"
                  required
                  min="2"
                />
              </div>

              <div className="pt-4">
                <button
                  type="submit"
                  className="w-full py-2 bg-red-500 text-white rounded-md hover:bg-red-600 flex items-center justify-center"
                >
                  <FiPlus className="mr-2" />
                  Crear Pedido
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </>
  );
}
