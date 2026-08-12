import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { BACKEND_URL } from '../../../core/config/api.config';
import { NotificationService } from './notification.service';

describe('NotificationService', () => {
  it('sends all selected filters to the notifications endpoint', () => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting(),
      { provide: BACKEND_URL, useValue: 'http://backend' }] });
    const service = TestBed.inject(NotificationService);
    const http = TestBed.inject(HttpTestingController);
    service.list({ page:1,size:10,status:'FAILED',channel:'EMAIL',contract:'025',date:'2026-08-12' }).subscribe();
    const request = http.expectOne(req => req.url === 'http://backend/api/v1/notifications');
    expect(request.request.params.get('status')).toBe('FAILED');
    expect(request.request.params.get('channel')).toBe('EMAIL');
    expect(request.request.params.get('contract')).toBe('025');
    expect(request.request.params.get('date')).toBe('2026-08-12');
    request.flush({content:[],page:1,size:10,totalElements:0,totalPages:0,first:false,last:true});
    http.verify();
  });
});
