import { DatePipe } from '@angular/common';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Person } from '../../models/person';
import { PersonService } from '../../services/person.service';

@Component({ selector:'app-person-detail', imports:[DatePipe,RouterLink,MatButtonModule,MatCardModule,MatIconModule],
  templateUrl:'./person-detail.component.html', styleUrl:'./person-detail.component.scss' })
export class PersonDetailComponent {
  private readonly service=inject(PersonService); private readonly route=inject(ActivatedRoute); private readonly destroyRef=inject(DestroyRef);
  readonly person=signal<Person|null>(null); private readonly id=this.route.snapshot.paramMap.get('id')!;
  constructor(){this.load();}
  deactivate():void{const person=this.person();if(person?.active&&confirm('Deseja desativar esta pessoa?'))this.service.deactivate(person).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(value=>this.person.set(value));}
  private load():void{this.service.getById(this.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(value=>this.person.set(value));}
}
