import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import {
  AdminUser,
  ApplicationRole,
  ApplicationRoleOption,
} from './user';

export interface UserRolesDialogData {
  user: AdminUser;
  roleOptions: ApplicationRoleOption[];
}

@Component({
  selector: 'app-user-roles-dialog',
  imports: [MatButtonModule, MatCheckboxModule, MatDialogModule, MatIconModule],
  templateUrl: './user-roles-dialog.component.html',
  styleUrl: './user-roles-dialog.component.scss',
})
export class UserRolesDialogComponent {
  private readonly dialogRef = inject(MatDialogRef<UserRolesDialogComponent>);
  protected readonly data = inject<UserRolesDialogData>(MAT_DIALOG_DATA);
  protected selectedRoles: ApplicationRole[] = [...this.data.user.roles];

  hasRole(role: ApplicationRole): boolean {
    return this.selectedRoles.includes(role);
  }

  toggleRole(role: ApplicationRole, checked: boolean): void {
    if (checked && !this.selectedRoles.includes(role)) {
      this.selectedRoles = [...this.selectedRoles, role];
    } else if (!checked) {
      this.selectedRoles = this.selectedRoles.filter((item) => item !== role);
    }
  }

  cancel(): void {
    this.dialogRef.close();
  }

  save(): void {
    this.dialogRef.close([...this.selectedRoles]);
  }
}
