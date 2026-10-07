import {  Routes } from "@angular/router";
import { notCitizenGuard } from "../../core/guards/auth-guard";

export const citizenRoutes: Routes = [
    {
        path:'my-profile',
        loadComponent: () => import('./my-profile/my-profile').then(m => m.MyProfile)
    },
    {
        path:'',
        canActivate: [notCitizenGuard],
        loadComponent: () => import('./citizens-list/citizens-list').then(m => m.CitizensList)
    },
    {
        path:'new',
        canActivate: [notCitizenGuard],
        loadComponent: () => import("./citizen-form/citizen-form").then(m => m.CitizenForm)
    },
    {
        path:':id',
        canActivate: [notCitizenGuard],
        loadComponent: () => import('./citizen-details/citizen-details').then(m => m.CitizenDetails)
    }
]

