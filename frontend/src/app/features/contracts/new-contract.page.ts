import { Component } from '@angular/core';
import { FeaturePlaceholderComponent } from '../../shared/components/feature-placeholder/feature-placeholder.component';

@Component({
  selector: 'app-new-contract-page',
  imports: [FeaturePlaceholderComponent],
  template: `<app-feature-placeholder title="Novo contrato" description="O formulário de cadastro será implementado nesta área." />`
})
export class NewContractPage {}
