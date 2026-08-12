import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { BACKEND_URL } from '../../../core/config/api.config';
import { DashboardService } from './dashboard.service';

describe('DashboardService', () => {
  it('loads aggregated metrics from the dashboard endpoint', () => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting(),
      { provide: BACKEND_URL, useValue: 'http://backend' }] });
    const service = TestBed.inject(DashboardService);
    const http = TestBed.inject(HttpTestingController);
    service.getDashboard().subscribe(data => expect(data.activeContracts).toBe(10));
    const request = http.expectOne('http://backend/api/v1/dashboard');
    expect(request.request.method).toBe('GET');
    request.flush({ activeContracts:10, expiredContracts:2, expiringIn15Days:1,
      expiringIn30Days:3, expiringIn60Days:5, failedNotifications:4 });
    http.verify();
  });
});
