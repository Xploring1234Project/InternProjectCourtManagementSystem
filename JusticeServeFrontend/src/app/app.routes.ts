

import { Routes } from '@angular/router';
import { adminGuard, authGuard, guestGuard, notCitizenGuard } from './core/guards/auth-guard';

export const routes: Routes = [

    { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    {
        path : 'auth',
        canActivate: [guestGuard],
        loadChildren: () => import("./modules/auth/auth.routes").then(m => m.authRoutes)
    },
    {
        path:'',
        canActivate:[authGuard],
        loadComponent: () => import('./shared/layout/layout').then(m => m.Layout),
        children: [
            {
                path:'dashboard',
                loadComponent : () => import('./modules/dashboard/dashboard').then(m => m.Dashboard)
            },
            {
                path:'users',
                canActivate: [adminGuard],
                loadChildren: () => import('./modules/users/users.routes').then(m => m.userRoutes)
            },
            {
                path:'citizens',
                loadChildren: () => import('./modules/citizens/citizens.routes').then(m => m.citizenRoutes)
            },
            {
                path:'cases',
                loadChildren: () => import('./modules/cases/cases.routes').then(m => m.caseRoutes) 
            },
            {
                path:'hearings',
                canActivate: [notCitizenGuard],
                loadChildren: () => import('./modules/hearings/hearings.routes').then(m => m.hearingRoutes)
            },
            {
                path:'judgments',
                canActivate: [notCitizenGuard],
                loadChildren: () => import('./modules/judgments/judgments.routes').then(m => m.judgmentRoutes)
            },
            {
                path:'compliance',
                canActivate: [notCitizenGuard],
                loadChildren: () => import('./modules/compliance/compliance.routes').then(m => m.complianceRoutes)
            },
            {
                path:'reports',
                canActivate: [notCitizenGuard],
                loadChildren: () => import('./modules/reports/reports.routes').then(m => m.reportRouts) 
            },
            {
                path:'notifications',
                loadComponent: () => import('./modules/notifications/notifications').then(m => m.Notifications)
            },
            {
                path:'audit-logs',
                canActivate: [adminGuard],
                loadComponent: () => import('./modules/auditlogs/auditlogs').then(m => m.AuditLogs)
            }
        ] 
    },
    { path: '**', redirectTo: 'dashboard' }
];
