import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Client } from '../models/models';

const API = 'http://localhost:8080/api';

@Injectable({ providedIn: 'root' })
export class ClientService {
  constructor(private http: HttpClient) {}

  list() { return this.http.get<Client[]>(`${API}/clients`); }
  get(id: number) { return this.http.get<Client>(`${API}/clients/${id}`); }
  create(c: any) { return this.http.post<Client>(`${API}/clients`, c); }
  update(id: number, c: any) { return this.http.put<Client>(`${API}/clients/${id}`, c); }
  delete(id: number) { return this.http.delete(`${API}/clients/${id}`); }
}
