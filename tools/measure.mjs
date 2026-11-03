import { chromium } from 'playwright-core';
import { createRequire } from 'node:module';
const require = createRequire(import.meta.url);
const C = require('../src/core.js');
const EXE = process.env.CHROME;
const b = await chromium.launch(EXE ? { executablePath: EXE } : {});
const ctx = await b.newContext({viewport:{width:390,height:844},deviceScaleFactor:1,isMobile:true,hasTouch:true});
await ctx.addInitScript((d)=>{localStorage.setItem('huazaina-v1', d);}, JSON.stringify({v:1,records:C.sampleRecords(C.todayISO()),cats:[],welcomed:true,meta:{}}));
const p = await ctx.newPage();
await p.goto('http://127.0.0.1:8899/'+encodeURIComponent('花在哪')+'.html');
await p.waitForTimeout(500);
console.log(await p.evaluate(()=>{
  const r=(s)=>{const e=document.querySelector(s); if(!e) return null; const b=e.getBoundingClientRect(); return {y:Math.round(b.y),h:Math.round(b.height)};};
  return {app:r('#app'), top:r('.top'), amount:r('.amount-block'), cats:r('.cats'), meta:r('.meta'), pad:r('.pad'), save:r('#saveBtn'), tab:r('.tabbar')};
}));
await b.close();
