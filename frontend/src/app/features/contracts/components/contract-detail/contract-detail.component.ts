import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Contract } from '../../models/contract';
import { ContractService } from '../../services/contract.service';

@Component({ selector: 'app-contract-detail', imports: [CurrencyPipe, DatePipe, RouterLink,
  MatButtonModule, MatCardModule, MatIconModule], templateUrl: './contract-detail.component.html',
  styleUrl: './contract-detail.component.scss' })
export class ContractDetailComponent {
  private readonly service = inject(ContractService);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  readonly contract = signal<Contract | null>(null);
  private readonly id = this.route.snapshot.paramMap.get('id')!;

  constructor() { this.load(); }

  close(): void { if (confirm('Deseja encerrar este contrato?')) this.changeStatus('close'); }
  cancel(): void { if (confirm('Deseja cancelar este contrato?')) this.changeStatus('cancel'); }
  private load(): void { this.service.getById(this.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(value => this.contract.set(value)); }
  private changeStatus(action: 'close' | 'cancel'): void {
    const request = action === 'close' ? this.service.close(this.id) : this.service.cancel(this.id);
    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(value => this.contract.set(value));
  }
}
