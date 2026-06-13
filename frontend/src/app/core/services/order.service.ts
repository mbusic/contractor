import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Order, OrderSummary } from '../models/models';

const API = 'http://localhost:8080/api';

@Injectable({ providedIn: 'root' })
export class OrderService {
  constructor(private http: HttpClient) {}

  list() {
    return this.http.get<OrderSummary[]>(`${API}/orders`);
  }

  get(id: number) {
    return this.http.get<Order>(`${API}/orders/${id}`);
  }

  create(payload: any) {
    return this.http.post<Order>(`${API}/orders`, payload);
  }

  update(id: number, payload: any) {
    return this.http.put<Order>(`${API}/orders/${id}`, payload);
  }

  delete(id: number) {
    return this.http.delete(`${API}/orders/${id}`);
  }

  changeStatus(id: number, status: string) {
    return this.http.patch<Order>(`${API}/orders/${id}/status`, { status });
  }

  addNote(id: number, text: string) {
    return this.http.post<Order>(`${API}/orders/${id}/notes`, { text });
  }

  uploadPhoto(id: number, file: File) {
    const fd = new FormData();
    fd.append('file', file);
    return this.http.post<Order>(`${API}/orders/${id}/photos`, fd);
  }

  deletePhoto(orderId: number, photoId: number) {
    return this.http.delete(`${API}/orders/${orderId}/photos/${photoId}`);
  }

  getDocument(id: number, type: 'quote' | 'workorder' | 'report' | 'invoice') {
    return this.http.get(`${API}/orders/${id}/documents/${type}`, { responseType: 'text' });
  }
}
