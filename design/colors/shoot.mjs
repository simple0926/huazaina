import { chromium } from 'playwright-core';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const EXE = process.env.CHROME;
const themes = ['pine', 'cobalt', 'clay'];
const screens = ['entry', 'statement'];
const browser = await chromium.launch(EXE ? { executablePath: EXE } : {});
const ctx = await browser.newContext({ viewport: { width: 390, height: 844 }, deviceScaleFactor: 2, isMobile: true, hasTouch: true, locale: 'zh-CN' });
for (const t of themes) {
  for (const s of screens) {
    const page = await ctx.newPage();
    const errs = []; page.on('pageerror', e => errs.push(e.message));
    await page.goto('file://' + resolve(root, s + '.html') + '?theme=' + t);
    await page.waitForTimeout(600);
    await page.screenshot({ path: resolve(root, 'colors/shots', t + '-' + s + '.png'), animations: 'disabled' });
    console.log(t, s, errs.length ? errs : 'ok');
    await page.close();
  }
}
await browser.close();
