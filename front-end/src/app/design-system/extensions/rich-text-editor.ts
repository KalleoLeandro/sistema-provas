import {
  AfterViewInit,
  ChangeDetectorRef,
  inject,
  Component,
  ElementRef,
  forwardRef,
  Input,
  OnDestroy,
  OnChanges,
  ViewChild,
  ViewEncapsulation,
  HostListener,
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import Quill from 'quill';
import './root-radicand';
@Component({
  selector: 'visual-rich-text-editor',
  templateUrl: './rich-text-editor.html',
  styleUrl: './rich-text-editor.css',
  encapsulation: ViewEncapsulation.None,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => RichTextEditorComponent),
      multi: true,
    },
  ],
})
export class RichTextEditorComponent implements AfterViewInit, OnDestroy, OnChanges, ControlValueAccessor {
  @Input() label = 'Editor de texto';
  @Input() inputId = '';
  @Input() allowImages = false;
  @Input() invalid = false;
  @ViewChild('imageInput') imageInput?: ElementRef<HTMLInputElement>;
  imageError = '';
  selectedImage: HTMLImageElement | null = null;
  imageWidth = '';
  imageAlt = '';
  @ViewChild('frame', { static: true }) frame!: ElementRef<HTMLDivElement>;
  imageBox: {left:number;top:number;width:number;height:number}|null = null;
  readonly imageCorners = ['nw','ne','sw','se'];
  private imageObserver?: ResizeObserver;
  private drag?: {pointer:number;x:number;y:number;width:number;ratio:number;sx:number;sy:number;updated:boolean};
  private imageScroll = () => this.updateImageBox();
  private imageMove = (event: PointerEvent) => {
    if (!this.drag || event.pointerId !== this.drag.pointer) return;
    event.preventDefault();
    const d=this.drag;
    const delta=(d.sx*(event.clientX-d.x)+d.sy*(event.clientY-d.y)/d.ratio)/(1+1/(d.ratio*d.ratio));
    this.resizeImage(d.width+delta);
  };
  private imageEnd = (event: PointerEvent) => { if(event.pointerId===this.drag?.pointer)this.endImageDrag(); };
  private replacingImage: HTMLImageElement | null = null;
  private imageClick = (event: MouseEvent) => {
    const target = event.target;
    if (this.disabled || !this.allowImages || !(target instanceof HTMLImageElement)) return;
    this.selectImage(target);
  };
  symbolsOpen = false;
  rootError = '';
  private normalizingRoot = false;
  readonly symbols = [
    { text: 'log', label: 'Logaritmo', value: 'log()', offset: 4 },
    { text: 'ln', label: 'Logaritmo natural', value: 'ln()', offset: 3 },
    ...[
      ['π', 'Pi'], ['θ', 'Teta'], ['α', 'Alfa'], ['β', 'Beta'], ['Δ', 'Delta'],
      ['±', 'Mais ou menos'], ['×', 'Multiplicação'], ['÷', 'Divisão'], ['·', 'Produto'],
      ['≠', 'Diferente'], ['≤', 'Menor ou igual'], ['≥', 'Maior ou igual'],
      ['≈', 'Aproximadamente igual'], ['∞', 'Infinito'], ['∑', 'Somatório'],
      ['∫', 'Integral'], ['∈', 'Pertence'], ['∉', 'Não pertence'], ['∪', 'União'],
      ['∩', 'Interseção'], ['∅', 'Conjunto vazio'], ['°', 'Graus'], ['½', 'Um meio'],
    ].map(([text, label]) => ({ text, label, value: text, offset: text.length })),
  ];
  get isDisabled() { return this.disabled; }
  private reader?: FileReader;
  @ViewChild('editor', { static: true }) element!: ElementRef<HTMLDivElement>;
  private editor?: Quill;
  private readonly changeDetector = inject(ChangeDetectorRef);
  private value = '';
  private disabled = false;
  private onChange: (value: string) => void = () => {};
  private onTouched: () => void = () => {};
  private changed = () => {
    if (this.editor) {
      this.finishRootsAtWhitespace();
      this.value = this.editor.getSemanticHTML();
      this.onChange(this.value);
      if (this.selectedImage && !this.editor.root.contains(this.selectedImage)) this.closeImage();
      this.updateImageBox();
      this.changeDetector.markForCheck();
    }
  };
  private blurred = (range: unknown) => {
    if (!range) this.onTouched();
  };
  ngOnChanges() {
    if (!this.editor) return;
    this.editor.root.setAttribute('aria-label', this.label);
    this.editor.root.setAttribute('aria-invalid', String(this.invalid));
    if (this.inputId) {
      this.editor.root.id = this.inputId;
      this.editor.root.setAttribute('aria-describedby', this.inputId + '-erro');
    }
  }
  ngAfterViewInit() {
    this.editor = new Quill(this.element.nativeElement, {
      theme: 'snow',
      placeholder: 'Escreva o conteúdo…',
      modules: {
        keyboard: { bindings: { rootSpace: { key: ' ', format: ['radicand'], handler: (range: { index: number; length: number }) => {
          if (!this.editor) return true;
          if (range.length) this.editor.deleteText(range.index, range.length, 'user');
          const formats = { ...this.editor.getFormat(range.index), radicand: false };
          this.editor.insertText(range.index, ' ', formats, 'user');
          this.editor.setSelection(range.index + 1, 0, 'silent');
          this.editor.format('radicand', false, 'silent');
          return false;
        } } } },
        toolbar: { container: [
          ['bold', 'italic', 'underline'],
          [{ script: 'super' }, { script: 'sub' }],
          [{ list: 'ordered' }, { list: 'bullet' }],
          ['clean'],
          ...(this.allowImages ? [['image']] : []),
        ], handlers: { image: () => { this.replacingImage = null; this.imageInput?.nativeElement.click(); } } },
      },
    });
    this.editor.root.setAttribute('aria-label', this.label);
    this.editor.root.setAttribute('role', 'textbox');
    this.editor.root.setAttribute('aria-multiline', 'true');
    this.ngOnChanges();
    if (this.inputId) this.editor.root.id = this.inputId;
    this.element.nativeElement.parentElement?.querySelector('.ql-image')?.setAttribute('aria-label', 'Inserir imagem do computador');
    const toolbar = this.element.nativeElement.parentElement?.querySelector('.ql-toolbar');
    for (const [selector, label] of [
      ['.ql-script[value="super"]', 'Sobrescrito (potência): x²'],
      ['.ql-script[value="sub"]', 'Subscrito (base): log₂'],
    ]) {
      const button = toolbar?.querySelector(selector);
      button?.setAttribute('aria-label', label);
      button?.setAttribute('title', label);
    }
    this.writeValue(this.value);
    this.editor.enable(!this.disabled);
    this.editor.on('text-change', this.changed);
    this.editor.on('selection-change', this.blurred);
    this.editor.root.addEventListener('click', this.imageClick);
    this.editor.root.addEventListener('scroll', this.imageScroll, {passive:true});
  }
  writeValue(value: string | null) {
    this.reader?.abort();
    this.closeImage();
    this.imageError = '';
    this.symbolsOpen = false;
    this.rootError = '';
    this.value = value ?? '';
    if (this.editor)
      this.editor.setContents(this.editor.clipboard.convert({ html: this.value }), 'silent');
    this.changeDetector.markForCheck();
  }
  registerOnChange(fn: (value: string) => void) {
    this.onChange = fn;
  }
  registerOnTouched(fn: () => void) {
    this.onTouched = fn;
  }
  setDisabledState(value: boolean) {
    this.disabled = value;
    if (value) this.symbolsOpen = false;
    if (value) this.closeImage();
    this.editor?.enable(!value);
    this.changeDetector.markForCheck();
  }
  /** Insere uma imagem apenas no rascunho em memória, sem upload para a API. */
  insertLocalImage(event: Event) {
    /* O arquivo é limitado antes da leitura; dados e mensagens são descartados ao limpar ou sair. */
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0]; input.value = ''; this.imageError = '';
    const replacement = this.replacingImage; this.replacingImage = null;
    if (!file || this.disabled || !this.allowImages) return;
    if (!['image/png', 'image/jpeg'].includes(file.type) || file.size > 5 * 1024 * 1024) {
      this.imageError = 'Selecione uma imagem PNG ou JPEG de até 5 MB.';
      this.changeDetector.markForCheck(); return;
    }
    this.reader?.abort();
    this.reader = new FileReader();
    this.reader.onload = () => {
      /* A posição atual é preservada; sair da página ou limpar o campo cancela a leitura. */
      if (!this.editor || this.disabled || typeof this.reader?.result !== 'string') return;
      const index = this.editor.getSelection()?.index ?? Math.max(0, this.editor.getLength() - 1);
      const position = replacement ? this.imageIndex(replacement) : index;
      if (position === null) return;
      const formats = replacement ? { width: replacement.getAttribute('width'), alt: replacement.getAttribute('alt') } : null;
      this.editor.history.cutoff();
      if (replacement) this.editor.deleteText(position, 1, 'user');
      this.editor.insertEmbed(position, 'image', this.reader.result, 'user');
      if (formats) this.editor.formatText(position, 1, formats, 'user');
      this.editor.setSelection(position + 1, 0, 'silent');
      this.editor.history.cutoff();
      const image = Array.from(this.editor.root.querySelectorAll('img')).find(img => this.imageIndex(img) === position);
      if (image) this.selectImage(image);
    };
    this.reader.onerror = () => { this.imageError = 'Não foi possível abrir a imagem.'; this.changeDetector.markForCheck(); };
    this.reader.readAsDataURL(file);
  }
  /** Seleciona uma figura do próprio editor sem incluir controles no HTML da questão. */
  selectImage(image: HTMLImageElement) {
    if (!this.editor?.root.contains(image) || this.disabled) return;
    this.endImageDrag();this.imageObserver?.disconnect();
    this.selectedImage = image;
    this.imageWidth = image.getAttribute('width') ?? String(Math.round(image.getBoundingClientRect().width || image.naturalWidth || 320));
    this.imageAlt = image.getAttribute('alt') ?? '';
    this.imageError = '';
    if(typeof ResizeObserver!=='undefined'){
      this.imageObserver=new ResizeObserver(()=>this.updateImageBox());
      this.imageObserver.observe(image);this.imageObserver.observe(this.editor.root);
    }
    this.updateImageBox();
    this.changeDetector.markForCheck();
  }
  /** Descarta a seleção; nenhuma preferência da imagem é armazenada no navegador. */
  closeImage() {
    this.endImageDrag();this.imageObserver?.disconnect();this.imageBox=null;
    this.selectedImage = null; this.replacingImage = null; this.imageWidth = ''; this.imageAlt = '';
    this.changeDetector.markForCheck();
  }
  /** Posiciona as alças fora do conteúdo salvo, acompanhando imagem e rolagem do editor. */
  @HostListener('window:resize') updateImageBox() {
    if(!this.selectedImage||!this.frame)return;
    const image=this.selectedImage.getBoundingClientRect(),frame=this.frame.nativeElement.getBoundingClientRect();
    this.imageBox={left:image.left-frame.left,top:image.top-frame.top,width:image.width,height:image.height};
    this.changeDetector.markForCheck();
  }
  /** Inicia um gesto proporcional, agrupado em uma única operação de desfazer. */
  startImageDrag(event:PointerEvent,corner:string){
    if(!this.editor||!this.selectedImage||this.disabled||event.button!==0)return;
    event.preventDefault();event.stopPropagation();this.endImageDrag();
    const rect=this.selectedImage.getBoundingClientRect();
    this.editor.history.cutoff();
    this.drag={pointer:event.pointerId,x:event.clientX,y:event.clientY,width:rect.width||Number(this.imageWidth)||320,
      ratio:rect.width&&rect.height?rect.width/rect.height:1,sx:corner.includes('w')?-1:1,sy:corner.includes('n')?-1:1,updated:false};
    (event.currentTarget as HTMLElement).setPointerCapture?.(event.pointerId);
    window.addEventListener('pointermove',this.imageMove,{passive:false});
    window.addEventListener('pointerup',this.imageEnd);window.addEventListener('pointercancel',this.imageEnd);
    window.addEventListener('blur',this.endImageDrag);
  }
  private endImageDrag = () => {
    if(this.drag&&this.editor){this.editor.history.cutoff();this.onTouched();}
    this.drag=undefined;
    window.removeEventListener('pointermove',this.imageMove);window.removeEventListener('pointerup',this.imageEnd);
    window.removeEventListener('pointercancel',this.imageEnd);window.removeEventListener('blur',this.endImageDrag);
  };
  private resizeImage(width:number){
    if(!this.editor||!this.selectedImage||this.disabled)return;
    const index=this.imageIndex(this.selectedImage);if(index===null)return;
    const root=this.editor.root;
    const max=root.clientWidth?Math.max(16,root.clientWidth-30):3000;
    this.imageWidth=String(Math.round(Math.max(16,Math.min(3000,max,width))));
    if(this.drag?.updated)this.editor.history.lastRecorded=Date.now();
    this.editor.formatText(index,1,{width:this.imageWidth,height:false},'user');
    if(this.drag)this.drag.updated=true;
    this.updateImageBox();
  }
  /** Oferece o mesmo ajuste pelas setas para quem navega pelo teclado. */
  imageResizeKey(event:KeyboardEvent){
    if(!['ArrowLeft','ArrowRight','ArrowUp','ArrowDown'].includes(event.key)||!this.editor)return;
    event.preventDefault();event.stopPropagation();this.editor.history.cutoff();
    const delta=(event.key==='ArrowLeft'||event.key==='ArrowDown'?-1:1)*(event.shiftKey?1:10);
    this.resizeImage((this.selectedImage?.getBoundingClientRect().width||Number(this.imageWidth)||320)+delta);
    this.editor.history.cutoff();this.onTouched();
  }
  private imageIndex(image: HTMLImageElement): number | null {
    if (!this.editor?.root.contains(image)) return null;
    const blot = Quill.find(image);
    return blot && !(blot instanceof Quill) ? this.editor.getIndex(blot) : null;
  }
  /** Aplica atributos suportados pelo Quill, preservando proporção, HTML e desfazer. */
  applyImage(original = false) {
    if (!this.editor || !this.selectedImage || this.disabled) return;
    const width = this.imageWidth.trim();
    if (!original && (!/^\d+$/.test(width) || Number(width) < 16 || Number(width) > 3000)) {
      this.imageError = 'Informe uma largura entre 16 e 3.000 pixels.'; return;
    }
    const index = this.imageIndex(this.selectedImage); if (index === null) return;
    this.editor.history.cutoff();
    this.editor.formatText(index, 1, { width: original ? false : String(Number(width)), height: false, alt: this.imageAlt.trim().slice(0, 300) || false }, 'user');
    this.editor.history.cutoff();
    this.imageError = ''; this.onTouched();
  }
  /** Substitui apenas a figura selecionada, conservando largura e descrição. */
  replaceImage() {
    if (this.disabled || !this.selectedImage) return;
    this.replacingImage = this.selectedImage; this.imageInput?.nativeElement.click();
  }
  /** Remove a figura pelo modelo do editor para permitir desfazer a operação. */
  removeImage() {
    if (!this.editor || !this.selectedImage || this.disabled) return;
    const index = this.imageIndex(this.selectedImage); if (index === null) return;
    this.editor.history.cutoff(); this.editor.deleteText(index, 1, 'user'); this.editor.history.cutoff();
    this.closeImage(); this.onTouched();
  }
  /** Insere o símbolo no cursor, substituindo a seleção e preservando o histórico do editor. */
  insertSymbol(symbol: (typeof this.symbols)[number]) {
    if (!this.editor || this.disabled) return;
    const range = this.editor.getSelection(true);
    const index = range?.index ?? Math.max(0, this.editor.getLength() - 1);
    this.editor.history.cutoff();
    if (range?.length) this.editor.deleteText(index, range.length, 'user');
    this.editor.insertText(index, symbol.value, 'user');
    this.editor.setSelection(index + symbol.offset, 0, 'user');
    this.editor.history.cutoff();
    this.onTouched();
  }
  /** Inicia uma raiz editável; o primeiro espaço encerra o radicando. */
  insertRoot(degree = '2') {
    if (!this.editor || this.disabled) return;
    const normalized = degree.trim().toLowerCase();
    if (normalized !== 'n' && (!/^[0-9]{1,3}$/.test(normalized) || Number(normalized) < 2)) {
      this.rootError = 'Informe um índice inteiro de 2 a 999 ou n.';
      return;
    }
    this.rootError = '';
    const indexText = normalized === 'n' ? 'n' : Number(normalized) === 2 ? '' : String(Number(normalized));
    const range = this.editor.getSelection(true);
    const index = range?.index ?? Math.max(0, this.editor.getLength() - 1);
    this.editor.history.cutoff();
    // A raiz envolve o trecho selecionado ou passa a cobrir o próximo texto digitado.
    const selected = range?.length ? this.editor.getText(index, range.length) : '';
    if (range?.length) this.editor.deleteText(index, range.length, 'user');
    if (indexText) this.editor.insertText(index, indexText, { script: 'super', radicand: false }, 'user');
    const radicalIndex = index + indexText.length;
    this.editor.insertText(radicalIndex, '√', { script: false, radicand: false }, 'user');
    if (selected) this.editor.insertText(radicalIndex + 1, selected, { script: false, radicand: true }, 'user');
    this.editor.setSelection(radicalIndex + 1 + selected.length, 0, 'silent');
    this.editor.format('script', false, 'silent');
    this.editor.format('radicand', !/\s/.test(selected), 'silent');
    this.editor.history.cutoff();
    this.onTouched();
  }
  /** Encerra a barra no primeiro espaço, inclusive ao colar vários termos. */
  private finishRootsAtWhitespace() {
    if (!this.editor || this.normalizingRoot) return;
    this.normalizingRoot = true;
    try {
      let index = 0;
      let ended = false;
      for (const op of this.editor.getContents().ops) {
        const length = typeof op.insert === 'string' ? op.insert.length : 1;
        if (!op.attributes?.['radicand']) ended = false;
        else if (typeof op.insert === 'string') {
          const firstSpace = ended ? 0 : op.insert.search(/\s/);
          if (firstSpace >= 0) {
            this.editor.formatText(index + firstSpace, length - firstSpace, 'radicand', false, 'silent');
            ended = true;
          }
        }
        index += length;
      }
    } finally { this.normalizingRoot = false; }
  }
  ngOnDestroy() {
    this.reader?.abort();
    this.editor?.root.removeEventListener('click', this.imageClick);
    this.editor?.root.removeEventListener('scroll', this.imageScroll);
    this.closeImage();
    this.editor?.off('text-change', this.changed);
    this.editor?.off('selection-change', this.blurred);
    this.editor?.enable(false);
  }
}
