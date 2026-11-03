import { chromium } from 'playwright-core';
import { resolve } from 'node:path';
const EXE = process.env.CHROME;
const b = await chromium.launch(EXE ? { executablePath: EXE } : {});
const ctx = await b.newContext({ viewport: { width: 1024, height: 1024 }, deviceScaleFactor: 1 });
for (const k of ['a', 'b', 'c']) {
  const p = await ctx.newPage();
  await p.goto('file://' + resolve('icons/icon-' + k + '.svg'));
  await p.waitForTimeout(150);
  await p.screenshot({ path: resolve('shots/icon-' + k + '.png'), omitBackground: true, animations: 'disabled' });
  console.log('icon', k);
  await p.close();
}
await b.close();
