import { Routes } from "@angular/router";

export const hearingRoutes: Routes = [
    {
        path:'', loadComponent: () => import("./hearings-list/hearings-list").then(m => m.HearingsList)
    },
    {
        path:'new', loadComponent: () => import("./hearing-form/hearing-form").then(m => m.HearingForm)
    },
    {
        path:':id', loadComponent: () => import("./hearing-details/hearing-details").then(m => m.HearingDetails)
    }
]