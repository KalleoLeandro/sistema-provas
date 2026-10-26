import { generateKeyPairSync } from 'node:crypto';
import { existsSync, mkdirSync, writeFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';

const pasta = new URL('./keys/', import.meta.url);
const privada = new URL('privada.pem', pasta);
const publica = new URL('publica.pem', pasta);

if (existsSync(privada) || existsSync(publica)) {
  throw new Error('Já existem chaves locais. A geração não substitui chaves existentes.');
}

mkdirSync(pasta, { recursive: true, mode: 0o700 });
const { privateKey, publicKey } = generateKeyPairSync('rsa', {
  modulusLength: 3072,
  privateKeyEncoding: { type: 'pkcs8', format: 'pem' },
  publicKeyEncoding: { type: 'spki', format: 'pem' },
});

writeFileSync(privada, privateKey, { flag: 'wx', mode: 0o600 });
writeFileSync(publica, publicKey, { flag: 'wx', mode: 0o644 });
console.log(`Chaves geradas em ${fileURLToPath(pasta)}`);
