import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi, HTTP_INTERCEPTORS } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';
import { AuthInterceptor } from './app/core/interceptors/auth.interceptor';
import {provideIcons} from "@ng-icons/core";
import {
  bootstrapEye,
  bootstrapEyeSlash, bootstrapGear,
  bootstrapInfoCircle, bootstrapPencil,
  bootstrapPlusSquare, bootstrapSearch,
  bootstrapTrash3, bootstrapCheckLg,
  bootstrapX
} from "@ng-icons/bootstrap-icons";

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptorsFromDi()),
    provideAnimations(),
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
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
}).catch(err => console.error(err));
