import { ChangeDetectorRef, Component, ElementRef, forwardRef, inject, Input, Output, EventEmitter } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

export interface ChipSelectOption { value: number; label: string; }

@Component({
  selector: 'visual-chip-select',
  templateUrl: './chip-select.html',
  styleUrl: './chip-select.css',
  providers: [{ provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => ChipSelectComponent), multi: true }],
})
export class ChipSelectComponent implements ControlValueAccessor {
  @Input({ required: true, alias: 'inputId' }) id = '';
  @Input() label = 'Opções';
  @Input() placeholder = 'Digite para buscar';
  @Input() emptyMessage = 'Nenhuma opção encontrada.';
  @Input() invalid = false;
  @Input() maxSelected=Number.POSITIVE_INFINITY;
  @Input() allowCreate=false;
  @Input() createLabel='Criar opção';
  @Input() fuzzySearch=false;
  @Output() createOption=new EventEmitter<string>();
  get canCreate(){const normalize=(s:string)=>s.normalize('NFD').replace(/[\u0300-\u036f]/g,'').toLowerCase().trim();return this.allowCreate&&this.value.length<this.maxSelected&&!!this.query.trim()&&this.query.trim().length<=120&&!this.options.some(o=>normalize(o.label)===normalize(this.query));}
  requestCreate(){if(!this.disabled&&this.canCreate)this.createOption.emit(this.query.trim().replace(/\s+/g,' '));}
  private distance(a:string,b:string){let row=Array.from({length:b.length+1},(_,i)=>i);for(let i=1;i<=a.length;i++){const next=[i];for(let j=1;j<=b.length;j++)next[j]=Math.min(next[j-1]+1,row[j]+1,row[j-1]+(a[i-1]===b[j-1]?0:1));row=next;}return row[b.length];}

  @Input() options: ChipSelectOption[] = [];
  private readonly element = inject(ElementRef<HTMLElement>);
  private readonly changeDetector = inject(ChangeDetectorRef);
  value: number[] = [];
  query = '';
  open = false;
  activeIndex = 0;
  disabled = false;
  private onChange: (value: number[]) => void = () => {};
  onTouched: () => void = () => {};

  /** Retorna sugestões existentes, ignorando acentos, caixa e itens já selecionados. */
  get filteredOptions(): ChipSelectOption[] {
    /* Texto digitado filtra o catálogo; nunca cria valores livres ou IDs desconhecidos. */
    const normalize = (text: string) => text.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
    const query = normalize(this.query.trim());
    if(this.value.length>=this.maxSelected)return [];
    return this.options.filter(option => !this.value.includes(option.value) && (normalize(option.label).includes(query)||this.fuzzySearch&&query.split(/\s+/).every(term=>normalize(option.label).includes(term)||term.length>=4&&normalize(option.label).split(/\s+/).some(word=>word.replace(/(?:oes|ao)$/,'').startsWith(term.replace(/(?:oes|ao)$/,''))||this.distance(term,word)<=Math.min(2,Math.floor(term.length/3))))));
  }

  /** Retorna os rótulos dos chips na ordem de seleção. */
  get selectedOptions(): ChipSelectOption[] {
    /* IDs sem opção correspondente não recebem rótulos inventados. */
    return this.value.flatMap(id => this.options.filter(option => option.value === id));
  }

  /** Atualiza a seleção e descarta a busca ao redefinir o formulário. */
  writeValue(value: number[] | null): void {
    /* Copiar e eliminar repetições impede alteração indireta do array do formulário. */
    this.value = [...new Set(value ?? [])];
    this.query = ''; this.open = false; this.activeIndex = 0;
    this.changeDetector.markForCheck();
  }
  registerOnChange(fn: (value: number[]) => void): void { this.onChange = fn; }
  registerOnTouched(fn: () => void): void { this.onTouched = fn; }
  setDisabledState(disabled: boolean): void {
    this.disabled = disabled;
    if (disabled) this.open = false;
    this.changeDetector.markForCheck();
  }

  /** Filtra as sugestões enquanto o usuário digita. */
  search(event: Event): void {
    /* Uma nova busca reinicia a navegação por teclado na primeira sugestão. */
    this.query = (event.target as HTMLInputElement).value;
    this.activeIndex = 0; this.open = true;
  }

  /** Seleciona somente uma opção válida, sem repetir chips. */
  select(value: number): void {
    /* Após selecionar, a busca é limpa para facilitar a escolha de outros itens. */
    if (this.disabled || this.value.length>=this.maxSelected || this.value.includes(value) || !this.options.some(option => option.value === value)) return;
    this.value = [...this.value, value];
    this.query = ''; this.activeIndex = 0;if(this.value.length>=this.maxSelected)this.open=false;
    this.onChange([...this.value]); this.onTouched();
  }

  /** Remove uma seleção e devolve a opção à lista de sugestões. */
  remove(value: number): void {
    /* A remoção também respeita o bloqueio do formulário durante o envio. */
    if (this.disabled) return;
    this.value = this.value.filter(item => item !== value);
    this.activeIndex = 0;
    this.onChange([...this.value]); this.onTouched();
  }

  /** Permite navegar, selecionar e fechar as sugestões sem mouse. */
  keydown(event: KeyboardEvent): void {
    /* Enter nunca envia o formulário ao tentar selecionar; texto livre não vira chip. */
    if (this.disabled || this.value.length>=this.maxSelected) return;
    const count = this.filteredOptions.length+(this.canCreate?1:0);
    if (event.key === 'Enter') {
      event.preventDefault();
      if(this.open&&count){if(this.canCreate&&this.activeIndex===this.filteredOptions.length)this.requestCreate();else this.select(this.filteredOptions[this.activeIndex]?.value??this.filteredOptions[0].value);}
    } else if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
      event.preventDefault();
      this.activeIndex = !this.open ? 0 : count ? (this.activeIndex + (event.key === 'ArrowDown' ? 1 : count - 1)) % count : 0;
      this.open = true;
    } else if (event.key === 'Escape') {
      event.preventDefault(); event.stopPropagation(); this.open = false;
    } else if (event.key === 'Backspace' && !this.query && this.value.length) {
      this.remove(this.value[this.value.length - 1]);
    }
  }

  /** Fecha as sugestões quando o foco deixa todo o controle, incluindo os chips. */
  focusout(event: FocusEvent): void {
    /* Cliques internos não são interpretados como abandono do campo. */
    if (!this.element.nativeElement.contains(event.relatedTarget as Node | null)) {
      this.open = false; this.onTouched();
    }
  }
}
