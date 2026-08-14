import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-login-page',
  imports: [MatButtonModule, MatCardModule, MatIconModule],
  templateUrl: './login.page.html',
  styleUrl: './login.page.scss',
})
export class LoginPage {
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);

  protected readonly loading = signal(false);

  login(): void {
    if (this.loading()) {
      return;
    }

    this.loading.set(true);
    const requestedReturnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
    const returnUrl = requestedReturnUrl?.startsWith('/') && !requestedReturnUrl.startsWith('//')
      ? requestedReturnUrl
      : '/dashboard';
    void this.auth.login(`${window.location.origin}${returnUrl}`).catch(() => {
      this.loading.set(false);
    });
  }
}
