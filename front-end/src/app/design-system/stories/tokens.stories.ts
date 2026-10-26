import type { Meta, StoryObj } from '@storybook/angular-vite';
const meta = { title: 'Design system/Identidade visual' } satisfies Meta;
export default meta;
export const Colors: StoryObj = { name: "Cores", render: () => ({ template: '<h2>Cores do ESFINORA</h2><div class="form-visual">'+['primaria','texto','fundo','superficie','erro','sucesso','info'].map(c=>'<div><span style="display:inline-block;width:72px;height:40px;vertical-align:middle;border:1px solid var(--cor-borda);background:var(--cor-'+c+')"></span> --cor-'+c+'</div>').join('')+'</div>' }) };
