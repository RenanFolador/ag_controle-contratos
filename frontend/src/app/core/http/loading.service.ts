import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class LoadingService {
  readonly active = signal(false);
  private pending = 0;

  start(): void {
    this.pending++;
    this.active.set(true);
  }

  finish(): void {
    this.pending = Math.max(0, this.pending - 1);
    this.active.set(this.pending > 0);
  }
}
