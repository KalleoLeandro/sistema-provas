import type { Meta, StoryObj } from '@storybook/angular-vite';
const meta = { title: 'Design system/Identidade visual' } satisfies Meta;
export default meta;
export const Colors: StoryObj = { name: "Cores", render: () => ({ template: '<h2>Cores do ESFINORA</h2><div class="form-visual">'+['primaria','texto','fundo','superficie','erro','sucesso','info'].map(c=>'<div><span style="display:inline-block;width:72px;height:40px;vertical-align:middle;border:1px solid var(--cor-borda);background:var(--cor-'+c+')"></span> --cor-'+c+'</div>').join('')+'</div>' }) };

export const Gradientes: StoryObj = {name:'Gradientes',render:()=>({template:'<div class="ds-grid">'+['azul','verde','laranja','rosa'].map(c=>'<div class="ds-col-6" style="padding:28px;border-radius:5px;color:white;background:var(--gradiente-'+c+')">Gradiente '+c+'</div>').join('')+'</div>'})};
export const Tipografia: StoryObj = {name:'Tipografia',render:()=>({template:'<h1>ESFINORA</h1><h2>Tipografia Poppins</h2><h3>Avaliação e gestão educacional</h3><p>Componentes, formulários e relatórios compartilham a mesma família tipográfica.</p>'})};
