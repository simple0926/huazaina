import { chromium } from 'playwright-core';
import { resolve } from 'node:path';
const EXE = process.env.CHROME;
const b = await chromium.launch(EXE ? { executablePath: EXE } : {});
const ctx = await b.newContext({ deviceScaleFactor: 1 });
const jobs = [['a-symbol',1024,1024],['b-symbol',1024,1024],['c-symbol',1024,1024],
              ['a-lockup',1440,512],['b-lockup',1440,512],['c-lockup',1440,512]];
for (const [f,w,h] of jobs) {
  const p = await ctx.newPage();
  await p.setViewportSize({ width: w, height: h });
  await p.goto('file://' + resolve(f + '.svg'));
  await p.evaluate(({w,h}) => {
    const s = document.documentElement;
    s.setAttribute('width', String(w));
    s.setAttribute('height', String(h));
    s.style.width = w + 'px';
    s.style.height = h + 'px';
    s.style.display = 'block';
  }, {w,h});
  await p.evaluate(() => document.fonts.ready);
  await p.waitForTimeout(300);
  await p.screenshot({ path: resolve('renders/' + f + '.png'), omitBackground: true });
  console.log(f, w, h);
  await p.close();
}
await b.close();
