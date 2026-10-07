import { Routes } from "@angular/router";



export const judgmentRoutes: Routes = [
    {
        path:'',loadComponent: () => import("./judgments-list/judgments-list").then(m => m.JudgmentsList)
    },
    {
        path:'new', loadComponent: () => import("./judgment-form/judgment-form").then(m => m.JudgmentForm)
     },
   //   {
   //      path:'orders', loadComponent: () => import("./orders-list/orders-list").then(m => m.OrdersList)
   //   },
     {
        path:'orders/new', loadComponent: () => import("./order-form/order-form").then(m => m.OrderForm)
     }
]