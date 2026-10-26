import { TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { vi } from 'vitest';
import { ApiService } from './api.service';
import { DesignSystemService } from './design-system.service';

describe('Permissão do design system', () => {
  const get = vi.fn();
  beforeEach(() => {
    get.mockReset();
    TestBed.configureTestingModule({ providers: [{ provide: ApiService, useValue: { get } }] });
  });

  it('libera o menu apenas após confirmação do servidor', async () => {
    get.mockResolvedValue(null);
    const service = TestBed.inject(DesignSystemService);
    expect(service.allowed()).toBe(false);
    expect(await service.checkAccess()).toBe(true);
    expect(get).toHaveBeenCalledWith('/design-system/acesso');
    expect(service.allowed()).toBe(true);
  });

  it.each([401, 403])('remove uma permissão anterior ao receber %s', async (status) => {
    const service = TestBed.inject(DesignSystemService);
    service.allowed.set(true);
    get.mockRejectedValue(new HttpErrorResponse({ status }));
    expect(await service.checkAccess()).toBe(false);
    expect(service.allowed()).toBe(false);
  });

  it('oculta o catálogo e preserva a falha quando o servidor está indisponível', async () => {
    const service = TestBed.inject(DesignSystemService);
    service.allowed.set(true);
    get.mockRejectedValue(new HttpErrorResponse({ status: 404 }));
    await expect(service.checkAccess()).rejects.toBeInstanceOf(HttpErrorResponse);
    expect(service.allowed()).toBe(false);
  });
});
