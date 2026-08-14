import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Contract, ContractHistory } from '../../models/contract';
import { ContractService } from '../../services/contract.service';
import { ContractAssignmentsComponent } from '../contract-assignments/contract-assignments.component';
import { PermissionService } from '../../../../core/auth/permission.service';
import { ContractRenewalDialogComponent } from '../contract-renewal-dialog/contract-renewal-dialog.component';

@Component({ selector: 'app-contract-detail', imports: [CurrencyPipe, DatePipe, RouterLink,
  MatButtonModule, MatCardModule, MatIconModule, ContractAssignmentsComponent], templateUrl: './contract-detail.component.html',
  styleUrl: './contract-detail.component.scss' })
export class ContractDetailComponent {
  private readonly service = inject(ContractService);
  readonly permissions = inject(PermissionService);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  readonly contract = signal<Contract | null>(null);
  readonly history = signal<ContractHistory[]>([]);
  private readonly id = this.route.snapshot.paramMap.get('id')!;

  constructor() { this.load(); }

  close(): void { if (confirm('Deseja encerrar este contrato?')) this.changeStatus('close'); }
  cancel(): void { if (confirm('Deseja cancelar este contrato?')) this.changeStatus('cancel'); }
  renew(): void {
    const contract = this.contract();
    if (!contract) return;
    this.dialog.open(ContractRenewalDialogComponent, { data: contract, width: '38rem' })
      .afterClosed().pipe(takeUntilDestroyed(this.destroyRef)).subscribe(payload => {
        if (!payload) return;
        this.service.renew(this.id, payload).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(value => {
          this.contract.set(value);
          this.loadHistory();
          this.snackBar.open('Contrato renovado com sucesso.', 'Fechar', { duration: 4000 });
        });
      });
  }
  private load(): void {
    this.service.getById(this.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(value => this.contract.set(value));
    this.loadHistory();
  }
  private loadHistory(): void { this.service.history(this.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(value => this.history.set(value)); }
  private changeStatus(action: 'close' | 'cancel'): void {
    const request = action === 'close' ? this.service.close(this.id) : this.service.cancel(this.id);
    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(value => this.contract.set(value));
  }
}
