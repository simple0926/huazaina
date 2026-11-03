import { readFileSync, writeFileSync, mkdirSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = dirname(fileURLToPath(import.meta.url));
const read = (p) => readFileSync(resolve(root, p), 'utf8');

let html = read('src/app.html');
const css = read('src/app.css');
const core = read('src/core.js');
const app = read('src/app.js');

html = html.replace('<link rel="stylesheet" href="app.css">', () => `<style>\n${css}\n</style>`);
html = html.replace('<script src="core.js"></script>', () => `<script>\n${core}\n</script>`);
html = html.replace('<script src="app.js"></script>', () => `<script>\n${app}\n</script>`);
if (/<(script|link)[^>]+(src|href)="(?!data:)/.test(html.replace(/<script>[\s\S]*?<\/script>/g, ''))) {
  throw new Error('仍有外部资源引用');
}

mkdirSync(resolve(root, 'dist'), { recursive: true });
const out = resolve(root, 'dist/花在哪.html');
writeFileSync(out, html);
mkdirSync(resolve(root, 'android/assets'), { recursive: true });
writeFileSync(resolve(root, 'android/assets/index.html'), html);
console.log('built', out, (Buffer.byteLength(html) / 1024).toFixed(1) + ' KB');
