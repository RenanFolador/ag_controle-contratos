import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, DestroyRef, inject, signal, ViewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { ContractSummary } from '../../models/contract';
import { ContractService } from '../../services/contract.service';

@Component({
  selector: 'app-contract-list',
  imports: [CurrencyPipe, DatePipe, ReactiveFormsModule, RouterLink, MatButtonModule,
    MatCardModule, MatFormFieldModule, MatIconModule, MatInputModule,
    MatPaginatorModule, MatSortModule, MatTableModule],
  templateUrl: './contract-list.component.html',
  styleUrl: './contract-list.component.scss'
})
export class ContractListComponent {
  @ViewChild(MatPaginator) private paginator?: MatPaginator;
  private readonly service = inject(ContractService);
  private readonly destroyRef = inject(DestroyRef);
  readonly search = new FormControl('', { nonNullable: true });
  readonly contracts = signal<ContractSummary[]>([]);
  readonly total = signal(0);
  readonly loading = signal(false);
  readonly displayedColumns = ['contractNumber', 'companyName', 'endDate', 'initialValue', 'status', 'actions'];
  private pageIndex = 0;
  private pageSize = 20;
  private sorting = 'contractNumber,asc';

  constructor() {
    this.search.valueChanges.pipe(debounceTime(300), distinctUntilChanged(),
      takeUntilDestroyed(this.destroyRef)).subscribe(() => {
        this.pageIndex = 0;
        if (this.paginator) this.paginator.firstPage();
        this.load();
      });
    this.load();
  }

  pageChanged(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.load();
  }

  sortChanged(event: Sort): void {
    this.sorting = event.direction ? `${event.active},${event.direction}` : 'contractNumber,asc';
    this.pageIndex = 0;
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.service.list({ page: this.pageIndex, size: this.pageSize,
      sort: this.sorting, search: this.search.value }).pipe(
      takeUntilDestroyed(this.destroyRef)).subscribe({
        next: page => { this.contracts.set(page.content); this.total.set(page.totalElements); this.loading.set(false); },
        error: () => this.loading.set(false)
      });
  }
}
