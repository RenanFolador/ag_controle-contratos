import { Component, input } from '@angular/core';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-feature-placeholder',
  imports: [MatCardModule],
  template: `<mat-card><mat-card-header><mat-card-title>{{ title() }}</mat-card-title></mat-card-header>
    <mat-card-content><p>{{ description() }}</p></mat-card-content></mat-card>`,
  styles: [`mat-card { max-width: 900px; }`]
})
export class FeaturePlaceholderComponent {
  readonly title = input.required<string>();
  readonly description = input.required<string>();
}
