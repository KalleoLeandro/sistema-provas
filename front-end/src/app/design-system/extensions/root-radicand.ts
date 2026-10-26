import Quill from 'quill';
import Inline from 'quill/blots/inline';

/** Mantém a barra da raiz como formatação de texto editável e preservada no HTML. */
class RootRadicand extends Inline {
  static override blotName = 'radicand';
  static override tagName = 'SPAN';
  static override className = 'raiz-radicando';
  static override formats() { return true; }
}
Quill.register(RootRadicand, true);
