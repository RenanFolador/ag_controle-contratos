import { Component } from '@angular/core';
import { FeaturePlaceholderComponent } from '../../shared/components/feature-placeholder/feature-placeholder.component';
@Component({ selector: 'app-persons-page', imports: [FeaturePlaceholderComponent], template: `<app-feature-placeholder title="Pessoas" description="Gestores e fiscais serão administrados nesta área." />` })
export class PersonsPage {}
