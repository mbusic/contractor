import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Branch } from '../models/models';

const API = 'http://localhost:8080/api';

@Injectable({ providedIn: 'root' })
export class BranchService {
  constructor(private http: HttpClient) {}

  list() { return this.http.get<Branch[]>(`${API}/branches`); }
  create(b: Partial<Branch>) { return this.http.post<Branch>(`${API}/branches`, b); }
  update(id: number, b: Partial<Branch>) { return this.http.put<Branch>(`${API}/branches/${id}`, b); }
  delete(id: number) { return this.http.delete(`${API}/branches/${id}`); }
}
