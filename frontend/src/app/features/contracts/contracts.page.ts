import { Component } from '@angular/core';
import { ReactiveFormsModule, FormControl } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FeaturePlaceholderComponent } from '../../shared/components/feature-placeholder/feature-placeholder.component';
@Component({ selector: 'app-contracts-page', imports: [FeaturePlaceholderComponent, ReactiveFormsModule, MatFormFieldModule, MatInputModule], template: `<app-feature-placeholder title="Contratos" description="Cadastro e consulta de contratos serão implementados nesta área." /><mat-form-field><mat-label>Pesquisa</mat-label><input matInput [formControl]="search" /></mat-form-field>` })
export class ContractsPage { readonly search = new FormControl('', { nonNullable: true }); }
