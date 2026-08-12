import { DatePipe } from '@angular/common';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ContractSummary } from '../../../contracts/models/contract';
import { ContractService } from '../../../contracts/services/contract.service';
import { DashboardMetrics } from '../../models/dashboard';
import { DashboardService } from '../../services/dashboard.service';

export function remainingDays(endDate: string, now = new Date()): number {
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const [year, month, day] = endDate.split('-').map(Number);
  const expiration = new Date(year, month - 1, day);
  return Math.ceil((expiration.getTime() - today.getTime()) / 86_400_000);
}

@Component({ selector: 'app-dashboard', imports: [DatePipe, RouterLink, MatCardModule,
  MatIconModule, MatTableModule], templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss' })
export class DashboardComponent {
  private readonly dashboardService = inject(DashboardService);
  private readonly contractService = inject(ContractService);
  private readonly destroyRef = inject(DestroyRef);
  readonly metrics = signal<DashboardMetrics | null>(null);
  readonly contracts = signal<ContractSummary[]>([]);
  readonly displayedColumns = ['contractNumber', 'companyName', 'endDate', 'remainingDays', 'situation'];

  constructor() {
    forkJoin({
      metrics: this.dashboardService.getDashboard(),
      contracts: this.contractService.list({ page: 0, size: 20, sort: 'endDate,asc', expirationDays: 60 })
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(({ metrics, contracts }) => {
      this.metrics.set(metrics); this.contracts.set(contracts.content);
    });
  }

  days(contract: ContractSummary): number { return remainingDays(contract.endDate); }
  situation(contract: ContractSummary): string {
    const days = this.days(contract);
    if (days < 0) return 'Vencido';
    if (days <= 15) return 'Crítico';
    if (days <= 30) return 'Atenção';
    return 'Próximo do vencimento';
  }
}
