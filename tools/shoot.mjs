import { chromium } from 'playwright-core';
import { createRequire } from 'node:module';
import { mkdirSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const require = createRequire(import.meta.url);
const C = require('../src/core.js');
const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const shots = resolve(root, 'shots');
mkdirSync(shots, { recursive: true });

const BASE = process.env.BASE || 'http://127.0.0.1:8899/花在哪.html';
const EXE = process.env.CHROME;

const seed = {
  v: 1,
  records: C.sampleRecords(C.todayISO()),
  cats: [],
  welcomed: true,
  meta: { lastExport: new Date().toISOString() }
};

const browser = await chromium.launch(EXE ? { executablePath: EXE } : {});
const errors = [];

async function makePage(withData = true) {
  const ctx = await browser.newContext({
    viewport: { width: 390, height: 844 },
    deviceScaleFactor: 2,
    isMobile: true,
    hasTouch: true,
    locale: 'zh-CN',
    timezoneId: 'Asia/Shanghai'
  });
  if (withData) {
    await ctx.addInitScript((data) => {
      localStorage.setItem('huazaina-v1', data);
    }, JSON.stringify(seed));
  } else {
    await ctx.addInitScript(() => { localStorage.removeItem('huazaina-v1'); });
  }
  const page = await ctx.newPage();
  page.on('pageerror', (e) => errors.push('PAGEERROR: ' + e.message + '\n' + e.stack));
  page.on('console', (m) => { if (m.type() === 'error') errors.push('CONSOLE: ' + m.text()); });
  await page.goto(BASE, { waitUntil: 'networkidle' });
  await page.waitForTimeout(500);
  return { ctx, page };
}

async function shot(page, name) {
  await page.screenshot({ path: resolve(shots, name + '.png'), animations: 'disabled' });
  console.log('shot', name);
}

const { ctx, page } = await makePage(true);

/* 1. 记账初始 */
await shot(page, '01-add');

/* 2. 选中分类并输入金额 */
await page.click('.cat[data-cat="food"]');
for (const k of ['3', '2', '.', '5']) await page.click(`.key[data-k="${k}"]`);
await page.waitForTimeout(250);
await shot(page, '02-add-filled');

/* 3. 保存 */
await page.click('#saveBtn');
await page.waitForTimeout(700);
await shot(page, '03-saved');
await page.waitForTimeout(4300);

/* 4. 明细 */
await page.click('.tab[data-tab="list"]');
await page.waitForTimeout(450);
await shot(page, '04-list');

/* 5. 搜索 */
await page.click('[data-search]');
await page.fill('#searchInput', '咖啡');
await page.waitForTimeout(350);
await shot(page, '05-search');
await page.click('[data-search]');
await page.waitForTimeout(200);

/* 6. 报告 月 */
await page.click('.tab[data-tab="report"]');
await page.waitForTimeout(900);
await shot(page, '06-report-month');

/* 7. 报告 周 */
await page.evaluate(() => { const s = document.querySelector('[data-view="report"] .scroll'); if (s) s.scrollTop = s.scrollHeight; });
await page.waitForTimeout(400);
await shot(page, '06b-report-bottom');
await page.evaluate(() => { const s = document.querySelector('[data-view="report"] .scroll'); if (s) s.scrollTop = 0; });
await page.click('[data-kind="week"]');
await page.waitForTimeout(800);
await shot(page, '07-report-week');

/* 8. 报告 年 */
await page.click('[data-kind="year"]');
await page.waitForTimeout(800);
await shot(page, '08-report-year');

/* 9. 报告 日 */
await page.click('[data-kind="day"]');
await page.waitForTimeout(700);
await shot(page, '09-report-day');

/* 10. 设置 */
await page.click('.tab[data-tab="add"]');
await page.waitForTimeout(300);
await page.click('#btnSettings');
await page.waitForTimeout(450);
await shot(page, '10-settings');

/* 11. 分类管理 */
await page.click('[data-act="cats"]');
await page.waitForTimeout(450);
await shot(page, '11-cats');
await page.click('#sheetClose');
await page.waitForTimeout(400);

/* 12. 日期选择 */
await page.click('#btnDate');
await page.waitForTimeout(450);
await shot(page, '12-calendar');
await page.click('#sheetClose');
await page.waitForTimeout(400);

/* 13. 编辑记录 */
await page.click('.tab[data-tab="list"]');
await page.waitForTimeout(400);
await page.click('.row');
await page.waitForTimeout(450);
await shot(page, '13-edit');

/* 小屏 360x740 */
const small = await browser.newContext({ viewport: { width: 360, height: 740 }, deviceScaleFactor: 2, isMobile: true, hasTouch: true, locale: 'zh-CN', timezoneId: 'Asia/Shanghai' });
await small.addInitScript((data) => { localStorage.setItem('huazaina-v1', data); }, JSON.stringify(seed));
const sp = await small.newPage();
await sp.goto(BASE, { waitUntil: 'networkidle' });
await sp.waitForTimeout(500);
await sp.screenshot({ path: resolve(shots, '17-small-360.png'), animations: 'disabled' });
console.log('shot 17-small-360');
await small.close();

/* 大屏 412x915 */
const large = await browser.newContext({ viewport: { width: 412, height: 915 }, deviceScaleFactor: 2, isMobile: true, hasTouch: true, locale: 'zh-CN', timezoneId: 'Asia/Shanghai' });
await large.addInitScript((data) => { localStorage.setItem('huazaina-v1', data); }, JSON.stringify(seed));
const lp = await large.newPage();
await lp.goto(BASE, { waitUntil: 'networkidle' });
await lp.waitForTimeout(500);
await lp.screenshot({ path: resolve(shots, '18-large-412.png'), animations: 'disabled' });
console.log('shot 18-large-412');
await large.close();

/* 14. 欢迎页 */
const fresh = await makePage(false);
await fresh.page.waitForTimeout(500);
await shot(fresh.page, '14-welcome');
await fresh.ctx.close();

/* 15. 空态 */
const empty = await makePage(false);
await empty.page.click('#welcomeStart');
await empty.page.waitForTimeout(450);
await empty.page.click('.tab[data-tab="list"]');
await empty.page.waitForTimeout(400);
await shot(empty.page, '15-empty-list');
await empty.page.click('.tab[data-tab="report"]');
await empty.page.waitForTimeout(500);
await shot(empty.page, '16-empty-report');
await empty.ctx.close();

/* 溢出检查 */
const metrics = await page.evaluate(() => ({
  docW: document.documentElement.scrollWidth,
  winW: window.innerWidth,
  docH: document.documentElement.scrollHeight,
  winH: window.innerHeight,
  addScroll: (() => { const v = document.querySelector('[data-view="add"]'); return v ? [v.scrollHeight, v.clientHeight] : null; })()
}));
console.log('metrics', JSON.stringify(metrics));
console.log('errors', errors.length ? errors : 'none');

await browser.close();
