import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { UserRolesDialogComponent } from './user-roles-dialog.component';
import {
  AdminUser,
  APPLICATION_ROLE_OPTIONS,
  ApplicationRole,
  ApplicationRoleOption,
} from './user';
import { UserAdminService } from './user.service';

@Component({
  selector: 'app-user-settings-page',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatSnackBarModule,
    MatTableModule,
  ],
  templateUrl: './user-settings.page.html',
  styleUrl: './user-settings.page.scss',
})
export class UserSettingsPage {
  private readonly service = inject(UserAdminService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);
  private readonly roleControls = new Map<string, FormControl<ApplicationRole[]>>();

  readonly users = signal<AdminUser[]>([]);
  readonly total = signal(0);
  readonly loading = signal(false);
  readonly savingUserId = signal<string | null>(null);
  readonly errorMessage = signal<string | null>(null);
  readonly search = new FormControl('', { nonNullable: true });
  readonly displayedColumns = ['username', 'name', 'email', 'roles', 'actions'];
  readonly roleOptions: ApplicationRoleOption[] = APPLICATION_ROLE_OPTIONS;

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
    return name || 'Nome não informado';
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

  openRoleDialog(user: AdminUser): void {
    if (this.savingUserId() !== null) {
      return;
    }

    const dialogRef = this.dialog.open(UserRolesDialogComponent, {
      width: 'min(560px, calc(100vw - 32px))',
      maxHeight: '90vh',
      data: { user, roleOptions: this.roleOptions },
    });

    dialogRef
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((roles: ApplicationRole[] | undefined) => {
        if (roles === undefined) {
          return;
        }
        this.rolesControl(user).setValue(roles);
        this.save(user);
      });
  }

  save(user: AdminUser): void {
    if (this.savingUserId() !== null) {
      return;
    }

    const roles = this.rolesControl(user).getRawValue();
    const roleNames = roles.map((role) => this.roleLabel(role)).join(', ') || 'nenhuma permissão';
    if (!confirm(`Atualizar as permissões de ${this.displayName(user)} para: ${roleNames}?`)) {
      return;
    }

    this.errorMessage.set(null);
    this.savingUserId.set(user.id);
    this.service
      .updateRoles(user.id, roles)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (updated) => {
          this.users.update((items) =>
            items.map((item) => (item.id === updated.id ? updated : item)),
          );
          this.rolesControl(updated).setValue([...updated.roles]);
          this.savingUserId.set(null);
          this.snackBar.open('Permissões atualizadas com sucesso.', 'Fechar', {
            duration: 4000,
          });
        },
        error: () => {
          this.savingUserId.set(null);
          this.errorMessage.set('Não foi possível atualizar as permissões do usuário.');
        },
      });
  }

  retry(): void {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.service
      .list(this.page, this.size, this.search.value)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (result) => {
          this.users.set(result.content);
          this.total.set(result.totalElements);
          this.loading.set(false);
        },
        error: () => {
          this.users.set([]);
          this.total.set(0);
          this.loading.set(false);
          this.errorMessage.set(
            'Não foi possível acessar o gerenciamento de usuários no momento. Tente novamente mais tarde.',
          );
        },
      });
  }
}
