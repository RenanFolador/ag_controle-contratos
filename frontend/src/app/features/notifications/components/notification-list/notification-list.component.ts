import { DatePipe } from '@angular/common';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';
import { debounceTime } from 'rxjs';
import { Notification, NotificationChannel, NotificationStatus } from '../../models/notification';
import { NotificationService } from '../../services/notification.service';
import { NotificationFailureDialogComponent } from '../notification-failure-dialog/notification-failure-dialog.component';

@Component({ selector: 'app-notification-list', imports: [DatePipe, ReactiveFormsModule,
  RouterLink, MatButtonModule, MatCardModule, MatDialogModule, MatFormFieldModule,
  MatIconModule, MatInputModule, MatPaginatorModule, MatSelectModule, MatTableModule],
  templateUrl: './notification-list.component.html', styleUrl: './notification-list.component.scss' })
export class NotificationListComponent {
  private readonly service = inject(NotificationService);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);
  readonly notifications = signal<Notification[]>([]);
  readonly total = signal(0);
  readonly statuses: NotificationStatus[] = ['PENDING', 'SENT', 'FAILED', 'CANCELLED'];
  readonly channels: NotificationChannel[] = ['EMAIL', 'WHATSAPP'];
  readonly displayedColumns = ['contract', 'recipient', 'channel', 'deadline', 'date', 'status', 'error'];
  readonly filters = new FormGroup({
    status: new FormControl<NotificationStatus | ''>('', { nonNullable: true }),
    channel: new FormControl<NotificationChannel | ''>('', { nonNullable: true }),
    contract: new FormControl('', { nonNullable: true }),
    date: new FormControl('', { nonNullable: true })
  });
  private page = 0;
  private size = 20;

  constructor() {
    this.filters.valueChanges.pipe(debounceTime(250), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => { this.page = 0; this.load(); });
    this.load();
  }

  pageChanged(event: PageEvent): void { this.page = event.pageIndex; this.size = event.pageSize; this.load(); }
  clear(): void { this.filters.reset({ status: '', channel: '', contract: '', date: '' }); }
  showFailure(notification: Notification): void {
    this.dialog.open(NotificationFailureDialogComponent, { width: '560px', data: notification });
  }
  private load(): void {
    const filters = this.filters.getRawValue();
    this.service.list({ page: this.page, size: this.size, ...filters }).pipe(
      takeUntilDestroyed(this.destroyRef)).subscribe(result => {
        this.notifications.set(result.content); this.total.set(result.totalElements);
      });
  }
}
