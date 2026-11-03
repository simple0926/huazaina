import { chromium } from 'playwright-core';
import { resolve } from 'node:path';
const EXE = process.env.CHROME;
const b = await chromium.launch(EXE ? { executablePath: EXE } : {});
const ctx = await b.newContext({ viewport: { width: 390, height: 844 }, deviceScaleFactor: 2, isMobile: true, hasTouch: true, locale: 'zh-CN' });
for (const k of ['a', 'b', 'c']) {
  const p = await ctx.newPage();
  await p.goto('file://' + resolve('home.html') + '?icon=' + k);
  await p.waitForTimeout(350);
  await p.screenshot({ path: resolve('shots/home-' + k + '.png'), animations: 'disabled' });
  console.log('home', k);
  await p.close();
}
await b.close();
