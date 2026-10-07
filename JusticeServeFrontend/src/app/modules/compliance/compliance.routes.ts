import { Routes } from "@angular/router";


export const complianceRoutes : Routes = [
    {
        path:'', loadComponent: () => import("./compliance").then(m => m.Compliance)
    }
]