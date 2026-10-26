import { applicationConfig, type Meta, type StoryObj } from '@storybook/angular-vite';
import { provideRouter } from '@angular/router';
import { MetricCardComponent } from '../metric-card';
const meta = { title: 'Componentes/Card de indicador', component: MetricCardComponent,
  decorators: [applicationConfig({ providers: [provideRouter([])] })],
  args: { value: 27, description: 'Questões a cadastrar faltando', link: '/questoes/listar', actionLabel: 'Ver questões' },
} satisfies Meta<MetricCardComponent>;
export default meta;
export const Padrao: StoryObj<MetricCardComponent> = {};
export const SemPendencias: StoryObj<MetricCardComponent> = { args: { value: 0 } };
