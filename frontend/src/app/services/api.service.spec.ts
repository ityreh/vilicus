import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ApiService } from './api.service';

describe('ApiService', () => {
  let service: ApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ApiService]
    });
    service = TestBed.inject(ApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call login endpoint', () => {
    const loginRequest = { email: 'test@example.com', password: 'password' };
    service.login(loginRequest).subscribe();

    const req = httpMock.expectOne('http://localhost:8080/api/auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(loginRequest);
  });

  it('should call getAccounts endpoint', () => {
    service.getAccounts().subscribe();

    const req = httpMock.expectOne('http://localhost:8080/api/accounts');
    expect(req.request.method).toBe('GET');
  });

  it('should call getTransactions endpoint', () => {
    service.getTransactions({ page: 0, size: 50 }).subscribe();

    const req = httpMock.expectOne(req =>
      req.url === 'http://localhost:8080/api/transactions' &&
      req.params.get('page') === '0' &&
      req.params.get('size') === '50'
    );
    expect(req.request.method).toBe('GET');
  });

  it('should call getDashboard endpoint', () => {
    service.getDashboard('30d').subscribe();

    const req = httpMock.expectOne(req =>
      req.url === 'http://localhost:8080/api/dashboard' &&
      req.params.get('dateRange') === '30d'
    );
    expect(req.request.method).toBe('GET');
  });

  it('should call getAnalytics endpoint', () => {
    service.getAnalytics('6m').subscribe();

    const req = httpMock.expectOne(req =>
      req.url === 'http://localhost:8080/api/analytics' &&
      req.params.get('dateRange') === '6m'
    );
    expect(req.request.method).toBe('GET');
  });
});
