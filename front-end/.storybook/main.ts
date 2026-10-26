import type { StorybookConfig } from '@storybook/angular-vite';
const config: StorybookConfig = {
  framework: '@storybook/angular-vite',
  stories: ['../src/app/design-system/**/*.stories.ts'],
  addons: ['@storybook/addon-docs'],
  docs: { defaultName: 'Documentação' },
  staticDirs: ['../public'],
  core: { disableTelemetry: true },
};
export default config;
