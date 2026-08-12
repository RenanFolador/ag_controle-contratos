import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PersonPayload } from '../../models/person';
import { PersonService } from '../../services/person.service';

@Component({ selector: 'app-person-form', imports: [ReactiveFormsModule, RouterLink,
  MatButtonModule, MatCardModule, MatCheckboxModule, MatFormFieldModule, MatInputModule],
  templateUrl: './person-form.component.html', styleUrl: './person-form.component.scss' })
export class PersonFormComponent {
  private readonly service = inject(PersonService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  readonly personId = this.route.snapshot.paramMap.get('id');
  readonly saving = signal(false);
  readonly form = new FormGroup({
    name: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(255)] }),
    cpf: new FormControl<string | null>(null, Validators.maxLength(14)),
    registration: new FormControl<string | null>(null, Validators.maxLength(100)),
    email: new FormControl<string | null>(null, [Validators.email, Validators.maxLength(255)]),
    phone: new FormControl<string | null>(null, Validators.maxLength(30)),
    whatsappEnabled: new FormControl(false, { nonNullable: true }),
    active: new FormControl(true, { nonNullable: true })
  });

  constructor() {
    if (this.personId) this.service.getById(this.personId).pipe(
      takeUntilDestroyed(this.destroyRef)).subscribe(person => this.form.patchValue(person));
  }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving.set(true);
    const payload = this.form.getRawValue() as PersonPayload;
    const request = this.personId ? this.service.update(this.personId, payload) : this.service.create(payload);
    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: person => this.router.navigate(['/persons', person.id]),
      error: () => this.saving.set(false)
    });
  }
}
