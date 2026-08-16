import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { ApiService } from './api.service';
import { ImportPreviewDto, ImportResultDto } from '../models';

@Injectable({
  providedIn: 'root'
})
export class ImportService {
  private previewSubject = new BehaviorSubject<ImportPreviewDto | null>(null);
  public preview$ = this.previewSubject.asObservable();

  constructor(private api: ApiService) {}

  /**
   * Upload file and get preview
   */
  uploadFile(accountId: number, file: File): Observable<ImportPreviewDto> {
    return new Observable(observer => {
      this.api.uploadFile(accountId, file).subscribe({
        next: (data) => {
          this.previewSubject.next(data);
          observer.next(data);
          observer.complete();
        },
        error: (err) => {
          console.error('Error uploading file:', err);
          observer.error(err);
        }
      });
    });
  }

  /**
   * Confirm and execute import
   */
  confirmImport(accountId: number): Observable<ImportResultDto> {
    return this.api.confirmImport(accountId);
  }

  /**
   * Get cached preview
   */
  getPreview(): ImportPreviewDto | null {
    return this.previewSubject.getValue();
  }

  /**
   * Clear preview
   */
  clearPreview(): void {
    this.previewSubject.next(null);
  }
}
