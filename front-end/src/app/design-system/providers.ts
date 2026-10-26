import { provideNzDateFnsAdapter } from 'ng-zorro-antd/core/time';
import { ptBR } from 'date-fns/locale';
import { makeEnvironmentProviders, inject, provideAppInitializer } from '@angular/core';
import { DOCUMENT, registerLocaleData } from '@angular/common';
import localePt from '@angular/common/locales/pt';
import { provideNzI18n, pt_BR } from 'ng-zorro-antd/i18n';
import { provideNzIcons } from 'ng-zorro-antd/icon';
import { NzConfigService } from 'ng-zorro-antd/core/config';
import {
  UserOutline,
  HomeOutline,
  EditOutline,
  DeleteOutline,
  SearchOutline,
  PlusOutline,
  CloseOutline,
  InfoCircleOutline,
  CheckCircleOutline,
  MenuOutline,
  BellOutline,
  FilePdfOutline,
} from '@ant-design/icons-angular/icons';

/** Configuracao compartilhada entre o portal e o Storybook. */
export function provideDesignSystem() {
  registerLocaleData(localePt);
  return makeEnvironmentProviders([
    provideNzI18n(pt_BR),
    provideNzDateFnsAdapter({ locale: ptBR }),
    provideNzIcons([
      UserOutline,
      HomeOutline,
      EditOutline,
      DeleteOutline,
      SearchOutline,
      PlusOutline,
      CloseOutline,
      InfoCircleOutline,
      CheckCircleOutline,
      MenuOutline,
      BellOutline,
      FilePdfOutline,
    ]),
    provideAppInitializer(() => {
      const document = inject(DOCUMENT);
      const styles = document.defaultView?.getComputedStyle(document.documentElement);
      const color = (token: string) => styles?.getPropertyValue(token).trim();
      const primaryColor = color('--cor-primaria');
      if (primaryColor)
        inject(NzConfigService).set('theme', {
          primaryColor,
          processingColor: primaryColor,
          infoColor: color('--cor-info'),
          successColor: color('--cor-sucesso'),
          errorColor: color('--cor-erro'),
          warningColor: color('--cor-aviso'),
        });
    }),
  ]);
}
