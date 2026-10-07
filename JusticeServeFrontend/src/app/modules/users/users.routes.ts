import { Routes } from "@angular/router";

export const userRoutes: Routes = [
    {path: '', loadComponent: () => import('./users').then(m => m.Users)}
]