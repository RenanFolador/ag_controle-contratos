import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { catchError, finalize, throwError } from 'rxjs';
import { LoadingService } from './loading.service';

type ApiError = { message?: string; fieldErrors?: Record<string, string> };

const fallbackMessages: Record<number, string> = {
  401: 'Sua sessão expirou. Entre novamente.',
  403: 'Você não possui permissão para realizar esta operação.',
  404: 'O recurso solicitado não foi encontrado.',
  409: 'A operação conflita com um registro existente.',
  500: 'Ocorreu um erro inesperado. Tente novamente mais tarde.',
};

export const apiErrorInterceptor: HttpInterceptorFn = (request, next) => {
  const snackBar = inject(MatSnackBar);
  const router = inject(Router);
  const loading = inject(LoadingService);
  loading.start();

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      console.error('Falha na requisição HTTP', {
        method: request.method,
        url: request.url,
        status: error.status,
        error: error.error,
      });

      const body = error.error as ApiError | null;
      const message =
        fallbackMessages[error.status] ??
        body?.message ??
        (error.status === 0
          ? 'Não foi possível conectar ao servidor.'
          : 'Não foi possível concluir a operação.');
      snackBar.open(message, 'Fechar', { duration: 6000 });

      if (error.status === 401) {
        void router.navigate(['/login'], {
          queryParams: { returnUrl: router.url },
        });
      }
      return throwError(() => error);
    }),
    finalize(() => loading.finish()),
  );
};
