import { AbstractControl, ValidationErrors } from '@angular/forms';

export function parsePrecoBrasileiro(valor: unknown): number {
  if (typeof valor === 'number') {
    return valor;
  }

  const texto = String(valor ?? '')
    .trim()
    .replace(/\s/g, '')
    .replace(/^R\$/i, '');

  if (!texto) {
    return Number.NaN;
  }

  let normalizado = texto;

  if (normalizado.includes(',')) {
    normalizado = normalizado.replace(/\./g, '').replace(',', '.');
  } else {
    const pontos = normalizado.match(/\./g)?.length ?? 0;
    const pareceDecimalComPonto = pontos === 1 && /\.\d{1,2}$/.test(normalizado);
    normalizado = pareceDecimalComPonto ? normalizado : normalizado.replace(/\./g, '');
  }

  return Number(normalizado);
}

export function formatPrecoBrasileiro(valor: unknown): string {
  const preco = parsePrecoBrasileiro(valor);

  if (!Number.isFinite(preco)) {
    return '';
  }

  return preco.toLocaleString('pt-BR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
}

export function precoBrasileiroValidator(control: AbstractControl): ValidationErrors | null {
  const preco = parsePrecoBrasileiro(control.value);
  return Number.isFinite(preco) && preco > 0 ? null : { precoInvalido: true };
}
