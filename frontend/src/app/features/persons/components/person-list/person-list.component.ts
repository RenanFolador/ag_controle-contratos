import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { Person } from '../../models/person';
import { PersonService } from '../../services/person.service';
import { PermissionService } from '../../../../core/auth/permission.service';

@Component({ selector: 'app-person-list', imports: [ReactiveFormsModule, RouterLink,
  MatButtonModule, MatCardModule, MatFormFieldModule, MatIconModule, MatInputModule,
  MatTableModule], templateUrl: './person-list.component.html', styleUrl: './person-list.component.scss' })
export class PersonListComponent {
  private readonly service = inject(PersonService);
  readonly permissions = inject(PermissionService);
  private readonly destroyRef = inject(DestroyRef);
  readonly search = new FormControl('', { nonNullable: true });
  readonly persons = signal<Person[]>([]);
  readonly loading = signal(false);
  readonly displayedColumns = ['name', 'registration', 'email', 'phone', 'active', 'actions'];

  constructor() {
    this.search.valueChanges.pipe(debounceTime(300), distinctUntilChanged(),
      takeUntilDestroyed(this.destroyRef)).subscribe(name => this.load(name));
    this.load();
  }

  private load(name?: string): void {
    this.loading.set(true);
    this.service.list(name).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: persons => { this.persons.set(persons); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }
}
