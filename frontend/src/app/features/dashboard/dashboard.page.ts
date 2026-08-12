import { Component } from '@angular/core';
import { FeaturePlaceholderComponent } from '../../shared/components/feature-placeholder/feature-placeholder.component';
@Component({ selector: 'app-dashboard-page', imports: [FeaturePlaceholderComponent], template: `<app-feature-placeholder title="Dashboard" description="Indicadores gerais serão apresentados aqui." />` })
export class DashboardPage {}
