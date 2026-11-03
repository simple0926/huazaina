import { chromium } from 'playwright-core';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = resolve(dirname(fileURLToPath(import.meta.url)));
const EXE = process.env.CHROME;
const screens = ['entry', 'ledger', 'statement', 'data', 'cover'];
const browser = await chromium.launch(EXE ? { executablePath: EXE } : {});
const ctx = await browser.newContext({ viewport: { width: 390, height: 844 }, deviceScaleFactor: 2, isMobile: true, hasTouch: true, locale: 'zh-CN' });
for (const name of screens) {
  const page = await ctx.newPage();
  const errs = [];
  page.on('pageerror', (e) => errs.push(e.message));
  await page.goto('file://' + resolve(root, name + '.html'));
  await page.waitForTimeout(400);
  await page.screenshot({ path: resolve(root, 'shots', name + '.png'), animations: 'disabled' });
  console.log(name, errs.length ? errs : 'ok');
  await page.close();
}
await browser.close();
