import { chromium } from 'playwright-core';
import { createRequire } from 'node:module';
import { resolve } from 'node:path';
const require = createRequire(import.meta.url);
const C = require('../src/core.js');
const EXE = process.env.CHROME;
const b = await chromium.launch(EXE ? { executablePath: EXE } : {});
const seed = {v:1, records: C.sampleRecords(C.todayISO()), cats:[], welcomed:true, meta:{}};
for (const [w,h,name] of [[360,740,'21-report-360'],[360,740,'22-add-360']]) {
  const ctx = await b.newContext({viewport:{width:w,height:h},deviceScaleFactor:2,isMobile:true,hasTouch:true,locale:'zh-CN',timezoneId:'Asia/Shanghai'});
  await ctx.addInitScript((d)=>{localStorage.setItem('huazaina-v1', d);}, JSON.stringify(seed));
  const p = await ctx.newPage();
  await p.goto('http://127.0.0.1:8899/'+encodeURIComponent('花在哪')+'.html');
  await p.waitForTimeout(500);
  if (name.includes('report')) { await p.click('.tab[data-tab="report"]'); await p.waitForTimeout(900); }
  await p.screenshot({path: resolve('shots', name+'.png')});
  await ctx.close();
  console.log('shot', name);
}
await b.close();
