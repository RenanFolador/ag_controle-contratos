import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { AdminUser, ApplicationRole } from './user';
import { UserAdminService } from './user.service';

type RoleOption = { value: ApplicationRole; label: string };

@Component({
  selector: 'app-user-settings-page',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatSelectModule,
    MatTableModule,
  ],
  templateUrl: './user-settings.page.html',
  styleUrl: './user-settings.page.scss',
})
export class UserSettingsPage {
  private readonly service = inject(UserAdminService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly roleControls = new Map<string, FormControl<ApplicationRole[]>>();

  readonly users = signal<AdminUser[]>([]);
  readonly total = signal(0);
  readonly loading = signal(false);
  readonly savingUserId = signal<string | null>(null);
  readonly search = new FormControl('', { nonNullable: true });
  readonly displayedColumns = ['user', 'email', 'status', 'roles', 'actions'];
  readonly roleOptions: RoleOption[] = [
    { value: 'ADMIN', label: 'Administrador' },
    { value: 'CONTRACT_MANAGER', label: 'Gestor de contratos' },
    { value: 'INSPECTOR', label: 'Inspetor' },
    { value: 'VIEWER', label: 'Visualizador' },
  ];

  private page = 0;
  private size = 20;

  constructor() {
    this.search.valueChanges
      .pipe(debounceTime(250), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.page = 0;
        this.load();
      });
    this.load();
  }

  rolesControl(user: AdminUser): FormControl<ApplicationRole[]> {
    let control = this.roleControls.get(user.id);
    if (!control) {
      control = new FormControl<ApplicationRole[]>([...user.roles], { nonNullable: true });
      this.roleControls.set(user.id, control);
    }
    return control;
  }

  roleLabel(role: ApplicationRole): string {
    return this.roleOptions.find((option) => option.value === role)?.label ?? role;
  }

  displayName(user: AdminUser): string {
    const name = [user.firstName, user.lastName].filter(Boolean).join(' ').trim();
    return name || user.username;
  }

  refresh(): void {
    this.page = 0;
    this.load();
  }

  clearSearch(): void {
    this.search.setValue('');
  }

  pageChanged(event: PageEvent): void {
    this.page = event.pageIndex;
    this.size = event.pageSize;
    this.load();
  }

  save(user: AdminUser): void {
    if (this.savingUserId() !== null) {
      return;
    }
    const roles = this.rolesControl(user).getRawValue();
    const roleNames = roles.map((role) => this.roleLabel(role)).join(', ') || 'nenhuma role';
    if (!confirm(`Atualizar as roles de ${this.displayName(user)} para: ${roleNames}?`)) {
      return;
    }

    this.savingUserId.set(user.id);
    this.service
      .updateRoles(user.id, roles)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (updated) => {
          this.users.update((items) => items.map((item) => (item.id === updated.id ? updated : item)));
          this.rolesControl(updated).setValue([...updated.roles]);
          this.savingUserId.set(null);
        },
        error: () => this.savingUserId.set(null),
      });
  }

  private load(): void {
    this.loading.set(true);
    this.service
      .list(this.page, this.size, this.search.value)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (result) => {
          this.users.set(result.content);
          this.total.set(result.totalElements);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }
}
