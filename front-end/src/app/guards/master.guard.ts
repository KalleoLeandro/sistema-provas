import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { DesignSystemService } from '../services/design-system.service';

export const masterGuard: CanActivateFn = async (_route, state) => {
  const designSystem = inject(DesignSystemService);
  const router = inject(Router);
  try {
    return await designSystem.checkAccess() ? true : router.createUrlTree(['/dashboard']);
  } catch {
    return router.createUrlTree(['/indisponivel'], { queryParams: { retorno: state.url } });
  }
};
