import { Component } from '@angular/core';
import { FeaturePlaceholderComponent } from '../../shared/components/feature-placeholder/feature-placeholder.component';

@Component({
  selector: 'app-expiring-contracts-page',
  imports: [FeaturePlaceholderComponent],
  template: `<app-feature-placeholder title="Próximos do vencimento" description="Contratos com vigência próxima do fim serão apresentados nesta área." />`
})
export class ExpiringContractsPage {}
