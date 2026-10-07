import { Routes } from "@angular/router";
import { CasesList } from "./cases-list/cases-list";
import { CaseForm } from "./case-form/case-form";
import { CaseDetails } from "./case-details/case-details";


export const caseRoutes: Routes = [
    {
        path:'', //loadComponent: () => import("./cases-list/cases-list").then(m => m.CasesList)
        component: CasesList
    },
    {
        path:'new', //loadComponent: () => import("./case-form/case-form").then(m => m.CaseForm)
        component: CaseForm
    },
    {
        path:':id', //loadComponent: () => import("./case-details/case-details").then(m => m.CaseDetails)
        component: CaseDetails
    }
]