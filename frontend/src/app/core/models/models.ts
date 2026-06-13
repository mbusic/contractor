export type Role = 'ADMIN' | 'OFFICE' | 'SERVICER' | 'CLIENT';
export type OrderStatus = 'DRAFT' | 'PENDING' | 'IN_PROGRESS' | 'RESOLVED' | 'CANCELLED';

export interface AuthUser {
  token: string;
  username: string;
  role: Role;
  displayName: string;
  userId: number;
  clientId: number | null;
  branchId: number | null;
}

export interface Branch {
  id: number;
  name: string;
  city: string;
}

export interface Location {
  id: number;
  address: string;
  city: string;
}

export interface Client {
  id: number;
  type: 'COMPANY' | 'INDIVIDUAL';
  name: string;
  contactPerson: string;
  phone: string;
  email: string;
  address: string;
  locations: Location[];
}

export interface UserDto {
  id: number;
  username: string;
  role: Role;
  displayName: string;
  clientId: number | null;
  clientName: string | null;
  branchId: number | null;
  branchName: string | null;
}

export interface NoteDto {
  id: number;
  text: string;
  authorName: string;
  createdAt: string;
}

export interface PhotoDto {
  id: number;
  url: string;
}

export interface OrderSummary {
  id: number;
  orderNumber: string;
  branch: Branch | null;
  urgency: string;
  status: OrderStatus;
  clientName: string | null;
  location: string | null;
  createdAt: string;
  actualKm: number | null;
  actualTotalHours: number | null;
  actualNumberOfWorkers: number | null;
}

export interface Order {
  id: number;
  orderNumber: string;
  branch: Branch | null;
  client: Client | null;
  location: string;
  contactPerson: string;
  phone: string;
  email: string;
  description: string;
  urgency: string;
  status: OrderStatus;
  assignedServicer: UserDto | null;
  estimatedKm: number | null;
  estimatedWorkHours: number | null;
  estimatedNumberOfWorkers: number | null;
  estimatedTotalHours: number | null;
  estimatedMaterialCost: number | null;
  actualKm: number | null;
  actualWorkHours: number | null;
  actualNumberOfWorkers: number | null;
  actualTotalHours: number | null;
  actualMaterialCost: number | null;
  createdAt: string;
  updatedAt: string;
  notes: NoteDto[];
  photos: PhotoDto[];
}
