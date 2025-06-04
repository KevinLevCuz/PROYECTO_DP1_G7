export interface Truck {
  id: number;
  initialPosition: [number, number];
  route: [number, number][];
  color?: string;
}

export interface Plant {
  id: number;
  position: [number, number];
  type: 'PRINCIPAL' | 'SECUNDARIA';
  name: string;
}

export interface Order {
  id: number;
  position: [number, number];
  name: string;
}


