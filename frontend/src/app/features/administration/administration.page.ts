import { Component } from '@angular/core';
import { FeaturePlaceholderComponent } from '../../shared/components/feature-placeholder/feature-placeholder.component';
@Component({ selector: 'app-administration-page', imports: [FeaturePlaceholderComponent], template: `<app-feature-placeholder title="Administração" description="Configurações do sistema serão mantidas nesta área." />` })
export class AdministrationPage {}
