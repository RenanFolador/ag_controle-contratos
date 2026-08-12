import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { Notification } from '../../models/notification';

@Component({ selector: 'app-notification-failure-dialog', imports: [MatDialogModule, MatButtonModule],
  template: `<h2 mat-dialog-title>Falha no envio</h2><mat-dialog-content>
    <dl><dt>Contrato</dt><dd>{{data.contractNumber}}</dd><dt>Destinatário</dt><dd>{{data.recipientName}}</dd>
      <dt>Canal</dt><dd>{{data.channel}}</dd><dt>Tentativas</dt><dd>{{data.retryCount}}</dd></dl>
    <h3>Erro</h3><pre>{{data.errorMessage || 'Erro não informado.'}}</pre>
  </mat-dialog-content><mat-dialog-actions align="end"><button mat-button mat-dialog-close>Fechar</button></mat-dialog-actions>`,
  styles: [`dl{display:grid;grid-template-columns:auto 1fr;gap:.5rem 1rem}dt{font-weight:600}dd{margin:0}pre{white-space:pre-wrap;background:var(--mat-sys-surface-container);padding:1rem;border-radius:.5rem}`] })
export class NotificationFailureDialogComponent { readonly data = inject<Notification>(MAT_DIALOG_DATA); }
