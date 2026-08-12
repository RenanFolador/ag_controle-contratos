export interface Person {
  id: string;
  name: string;
  cpf: string | null;
  registration: string | null;
  email: string | null;
  phone: string | null;
  whatsappEnabled: boolean;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface PersonPayload {
  name: string;
  cpf: string | null;
  registration: string | null;
  email: string | null;
  phone: string | null;
  whatsappEnabled: boolean;
  active?: boolean;
}
