import { Routes } from "@angular/router";

export const reportRouts : Routes = [
    {
        path:'', loadComponent: () => import("./reports").then(m => m.Reports)
    }
]