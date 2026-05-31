/**
 * Diese Datei enthält die Konfiguration für die Angular-Anwendung, einschließlich der Bereitstellung von
 * Diensten, Routen und HTTP-Interzeptoren. Hier werden auch die benötigten Icons aus der Bootstrap-Icons-Bibliothek
 * bereitgestellt.
 */
import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './core/auth-interceptor';
import { provideIcons } from '@ng-icons/core';
import {
  bootstrapCheckLg,
  bootstrapEye,
  bootstrapEyeSlash,
  bootstrapGear,
  bootstrapInfoCircle,
  bootstrapPencil,
  bootstrapPlusSquare,
  bootstrapSearch,
  bootstrapTrash3,
  bootstrapX,
} from '@ng-icons/bootstrap-icons';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideIcons({
      bootstrapPlusSquare,
      bootstrapTrash3,
      bootstrapPencil,
      bootstrapGear,
      bootstrapSearch,
      bootstrapEye,
      bootstrapEyeSlash,
      bootstrapInfoCircle,
      bootstrapCheckLg,
      bootstrapX,
    }),
  ],
};
