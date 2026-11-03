import { chromium } from 'playwright-core';
import { createRequire } from 'node:module';
import { resolve } from 'node:path';
const require = createRequire(import.meta.url);
const C = require('../src/core.js');
const EXE = process.env.CHROME;
const b = await chromium.launch(EXE ? { executablePath: EXE } : {});
const ctx = await b.newContext({ viewport: { width: 390, height: 844 }, deviceScaleFactor: 2, isMobile: true, hasTouch: true, locale: 'zh-CN', timezoneId: 'Asia/Shanghai' });
const today = C.todayISO();
const seed = {
  v: 1, welcomed: true, meta: {},
  records: [
    { id: 'i1', amt: 800000, cat: 'in_salary', kind: 'income', date: today, pay: 'card', note: '10 月工资', ts: Date.now() - 3000 },
    { id: 'i2', amt: 120000, cat: 'in_bonus', kind: 'income', date: today, pay: 'card', note: '季度奖金', ts: Date.now() - 2000 },
    { id: 'e1', amt: 3250, cat: 'food', kind: 'expense', date: today, pay: 'wechat', note: '午饭', ts: Date.now() - 1000 },
    { id: 'e2', amt: 320000, cat: 'home', kind: 'expense', date: C.addDays(today, -1), pay: 'card', note: '房租', ts: Date.now() - 9000 }
  ],
  cats: []
};
await ctx.addInitScript((d) => localStorage.setItem('huazaina-v1', d), JSON.stringify(seed));
const p = await ctx.newPage();
const errs = []; p.on('pageerror', e => errs.push(e.message));
await p.goto('http://127.0.0.1:8899/' + encodeURIComponent('花在哪') + '.html');
await p.waitForTimeout(500);
await p.click('#kindSwitch [data-kind="income"]');
await p.waitForTimeout(350);
await p.screenshot({ path: resolve('shots/income-entry.png'), animations: 'disabled' });
await p.click('[data-tab="list"]');
await p.waitForTimeout(500);
await p.screenshot({ path: resolve('shots/income-list.png'), animations: 'disabled' });
await p.click('[data-tab="report"]');
await p.waitForTimeout(500);
await p.click('.rtype [data-rtype="income"]');
await p.waitForTimeout(600);
await p.screenshot({ path: resolve('shots/income-report.png'), animations: 'disabled' });
console.log('errors', errs);
await b.close();
