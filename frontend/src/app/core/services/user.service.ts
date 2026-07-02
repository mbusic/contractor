import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { UserDto } from '../models/models';
import { environment } from '../../../environments/environment';

const API = `${environment.apiUrl}`;

@Injectable({ providedIn: 'root' })
export class UserService {
  constructor(private http: HttpClient) {}

  list(role?: string) {
    const params: Record<string, string> = role ? { role } : {};
    return this.http.get<UserDto[]>(`${API}/users`, { params });
  }

  create(u: any) { return this.http.post<UserDto>(`${API}/users`, u); }
  update(id: number, u: any) { return this.http.put<UserDto>(`${API}/users/${id}`, u); }
  delete(id: number) { return this.http.delete(`${API}/users/${id}`); }
}
