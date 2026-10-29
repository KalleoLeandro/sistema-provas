import { inject } from '@angular/core';
import { CanActivateFn, CanActivateChildFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Perfil } from '../models/usuario';

export const authGuard: CanActivateChildFn = async (route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  try {
    const usuario = await auth.restore();
    if (!usuario) return router.createUrlTree(['/login']);
    if (usuario.trocarSenha && state.url !== '/trocar-senha') return router.createUrlTree(['/trocar-senha']);
    const perfis = route.data['perfis'] as Perfil[] | undefined;
    if (perfis && !perfis.includes(usuario.perfil)) {
      return router.createUrlTree([usuario.trocarSenha ? '/trocar-senha' : usuario.perfil === 'aluno' ? '/aluno/provas' : '/dashboard']);
    }
    return true;
  } catch {
    return router.createUrlTree(['/indisponivel'], {
      queryParams: { retorno: state.url || '/dashboard' },
    });
  }
};

export const guestGuard: CanActivateFn = async () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  try {
    const usuario = await auth.restore();
    return usuario
      ? router.createUrlTree([usuario.trocarSenha ? '/trocar-senha' : usuario.perfil === 'aluno' ? '/aluno/provas' : '/dashboard'])
      : true;
  } catch {
    return router.createUrlTree(['/indisponivel'], { queryParams: { retorno: '/dashboard' } });
  }
};
