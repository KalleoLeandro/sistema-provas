import { provideDesignSystem } from '../src/app/design-system/providers';
import { applicationConfig, type Preview } from '@storybook/angular-vite';
import '../src/app/design-system/catalog.css';
import '../src/styles.css';
const preview: Preview = {
  tags: ['autodocs'],
  decorators: [applicationConfig({ providers: [provideDesignSystem()] })],
  parameters: { layout: 'padded', controls: { expanded: true } },
};
export default preview;
