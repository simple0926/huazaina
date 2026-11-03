import { test } from 'node:test';
import assert from 'node:assert/strict';
import { chromium } from 'playwright-core';
import { readFileSync } from 'node:fs';

const BASE = process.env.BASE || 'http://127.0.0.1:8899/' + encodeURIComponent('花在哪') + '.html';
const EXE = process.env.CHROME;
const browser = await chromium.launch(EXE ? { executablePath: EXE } : {});

async function fresh() {
  const ctx = await browser.newContext({
    viewport: { width: 390, height: 844 }, deviceScaleFactor: 1, isMobile: true, hasTouch: true,
    locale: 'zh-CN', timezoneId: 'Asia/Shanghai', acceptDownloads: true
  });
  await ctx.addInitScript(() => {
    if (!localStorage.getItem('__hz_test')) {
      localStorage.removeItem('huazaina-v1');
      localStorage.setItem('__hz_test', '1');
    }
  });
  const page = await ctx.newPage();
  const errors = [];
  page.on('pageerror', (e) => errors.push(e.message));
  await page.goto(BASE);
  await page.waitForTimeout(350);
  return { ctx, page, errors };
}

async function startApp(page) {
  if (await page.locator('#welcomeStart').count()) {
    await page.click('#welcomeStart');
    await page.waitForTimeout(400);
  }
}

async function addRecord(page, cat, keys, note) {
  await page.click(`.cat[data-cat="${cat}"]`);
  for (const k of keys) await page.click(`.key[data-k="${k}"]`);
  if (note) {
    await page.click('#btnNote');
    await page.fill('#noteInput', note);
    await page.click('#noteSave');
    await page.waitForTimeout(350);
  }
  await page.click('#saveBtn');
  await page.waitForTimeout(500);
}

async function store(page) {
  return page.evaluate(() => JSON.parse(localStorage.getItem('huazaina-v1') || '{}'));
}

test('记一笔：金额、分类、备注与持久化', async () => {
  const { ctx, page, errors } = await fresh();
  await startApp(page);
  await addRecord(page, 'food', ['3', '2', '.', '5'], '午饭');
  const data = await store(page);
  assert.equal(data.records.length, 1);
  assert.equal(data.records[0].amt, 3250);
  assert.equal(data.records[0].cat, 'food');
  assert.equal(data.records[0].note, '午饭');
  assert.match(await page.textContent('#todayLine'), /今天 1 笔/);

  await page.reload();
  await page.waitForTimeout(400);
  const again = await store(page);
  assert.equal(again.records.length, 1, '刷新后仍在');
  assert.equal(await page.textContent('#metaPay'), '微信');
  assert.deepEqual(errors, []);
  await ctx.close();
});

test('保存后按钮可直接撤销', async () => {
  const { ctx, page } = await fresh();
  await startApp(page);
  await addRecord(page, 'food', ['8', '8'], '');
  assert.match(await page.textContent('#saveBtn'), /撤销/);
  await page.click('#saveBtn');
  await page.waitForTimeout(400);
  const data = await store(page);
  assert.equal(data.records.length, 0);
  assert.equal((await page.textContent('#saveBtn')).trim(), '记下这笔');
  await ctx.close();
});

test('未选分类或金额为 0 时不能保存', async () => {
  const { ctx, page } = await fresh();
  await startApp(page);
  assert.equal(await page.isDisabled('#saveBtn'), true);
  await page.click('.cat[data-cat="shop"]');
  assert.equal(await page.isDisabled('#saveBtn'), true);
  await page.click('.key[data-k="0"]');
  assert.equal(await page.isDisabled('#saveBtn'), true);
  await page.click('.key[data-k="1"]');
  assert.equal(await page.isDisabled('#saveBtn'), false);
  await ctx.close();
});

test('明细：列表、搜索、分类筛选与金额', async () => {
  const { ctx, page } = await fresh();
  await startApp(page);
  await addRecord(page, 'food', ['2', '5'], '咖啡');
  await page.waitForTimeout(300);
  await addRecord(page, 'shop', ['1', '0', '0'], '耳机');
  await page.waitForTimeout(300);
  await page.click('.tab[data-tab="list"]');
  await page.waitForTimeout(450);
  assert.equal(await page.locator('.row').count(), 2);
  assert.match(await page.textContent('#listScroll'), /咖啡/);
  assert.match(await page.textContent('#listScroll'), /¥100\.00/);

  await page.click('[data-search]');
  await page.fill('#searchInput', '咖啡');
  await page.waitForTimeout(400);
  assert.equal(await page.locator('.row').count(), 1);
  await page.fill('#searchInput', '');
  await page.waitForTimeout(400);
  await page.click('.chip[data-filter="food"]');
  await page.waitForTimeout(300);
  assert.equal(await page.locator('.row').count(), 1);
  assert.match(await page.textContent('#listScroll'), /¥25\.00/);
  await ctx.close();
});

test('编辑与删除', async () => {
  const { ctx, page } = await fresh();
  await startApp(page);
  await addRecord(page, 'food', ['2', '0'], '');
  await page.click('.tab[data-tab="list"]');
  await page.waitForTimeout(400);
  await page.click('.row');
  await page.waitForTimeout(400);
  await page.fill('#editAmt', '45.5');
  await page.fill('#editNote', '改过');
  await page.click('#editSave');
  await page.waitForTimeout(500);
  let data = await store(page);
  assert.equal(data.records[0].amt, 4550);
  assert.equal(data.records[0].note, '改过');

  await page.click('.row');
  await page.waitForTimeout(400);
  await page.click('#editDel');
  await page.waitForTimeout(500);
  data = await store(page);
  assert.equal(data.records.length, 0);
  await page.click('#toastAction');
  await page.waitForTimeout(300);
  data = await store(page);
  assert.equal(data.records.length, 1, '撤销后恢复');
  await ctx.close();
});

test('报告：日周月年切换与合计', async () => {
  const { ctx, page } = await fresh();
  await startApp(page);
  await addRecord(page, 'food', ['1', '2', '0'], '');
  await page.click('.tab[data-tab="report"]');
  await page.waitForFunction(() => {
    const el = document.querySelector('#heroNum');
    return el && el.textContent.replace(/,/g, '') === '120.00';
  }, null, { timeout: 5000 });
  assert.match(await page.textContent('.pn-label'), /2026年/);

  for (const [kind, re] of [['week', /月\d+日/], ['month', /2026年\d+月/], ['year', /2026年/], ['day', /周一|周[一二三四五六日]/]]) {
    await page.click(`[data-kind="${kind}"]`);
    await page.waitForTimeout(450);
    assert.match(await page.textContent('.pn-label'), re);
  }
  await page.click('[data-kind="day"]');
  await page.waitForFunction(() => {
    const el = document.querySelector('#heroNum');
    return el && el.textContent.replace(/,/g, '') === '120.00';
  }, null, { timeout: 5000 });
  assert.match(await page.textContent('.insight'), /餐饮/);
  await ctx.close();
});

test('导出 CSV：文件名、BOM、表头与内容', async () => {
  const { ctx, page } = await fresh();
  await startApp(page);
  await addRecord(page, 'food', ['8', '.', '5'], '包子');
  await page.click('#btnSettings');
  await page.waitForTimeout(400);
  const [download] = await Promise.all([
    page.waitForEvent('download'),
    page.click('[data-act="csv"]')
  ]);
  assert.match(download.suggestedFilename(), /^花在哪-明细-\d{8}\.csv$/);
  const path = await download.path();
  const text = readFileSync(path, 'utf8');
  assert.ok(text.startsWith('\ufeff日期,月份,类型,分类,金额,支付方式,备注'));
  assert.match(text, /支出,餐饮,8\.50,微信,包子/);
  const data = await store(page);
  assert.ok(data.meta.lastExport, '记录上次导出时间');
  await ctx.close();
});

test('自定义分类 + 清空数据', async () => {
  const { ctx, page } = await fresh();
  await startApp(page);
  await page.click('#btnSettings');
  await page.waitForTimeout(400);
  await page.click('[data-act="cats"]');
  await page.waitForTimeout(400);
  await page.fill('#newCatName', '宠物');
  await page.click('.swatch[data-color="#6A5A86"]');
  await page.click('#addCat');
  await page.waitForTimeout(500);
  const data = await store(page);
  assert.ok(data.cats.some((c) => c.name === '宠物'));
  assert.equal(await page.locator('.cat[data-cat^="c"]').count(), 1);

  await page.click('#btnSettings');
  await page.waitForTimeout(400);
  await page.click('[data-act="clear"]');
  await page.waitForTimeout(500);
  await page.click('[data-ok]');
  await page.waitForTimeout(500);
  const cleared = await store(page);
  assert.equal(cleared.records.length, 0);
  assert.equal(cleared.cats.length, 0);
  await ctx.close();
});

test('示例数据：载入、报告与清空', async () => {
  const { ctx, page, errors } = await fresh();
  await page.click('#welcomeSample');
  await page.waitForTimeout(600);
  await page.click('[data-ok]');
  await page.waitForTimeout(700);
  const data = await store(page);
  assert.ok(data.records.length > 60);
  await page.click('.tab[data-tab="report"]');
  await page.waitForTimeout(800);
  assert.match(await page.textContent('.section-head .label'), /支出|分类/);
  assert.deepEqual(errors, []);
  await ctx.close();
});

test('月份切换与空月提示', async () => {
  const { ctx, page } = await fresh();
  await startApp(page);
  await addRecord(page, 'food', ['1', '0'], '');
  await page.click('.tab[data-tab="list"]');
  await page.waitForTimeout(400);
  const before = await page.textContent('.mb-title');
  await page.click('[data-month="-1"]');
  await page.waitForTimeout(400);
  const after = await page.textContent('.mb-title');
  assert.notEqual(before, after);
  assert.match(await page.textContent('#listScroll'), /这个月还没有记录/);
  await page.click('[data-empty="thisMonth"]');
  await page.waitForTimeout(400);
  assert.equal((await page.textContent('.mb-title')).trim(), before.trim());
  await ctx.close();
});

test.after(async () => { await browser.close(); });


test('收入：切换、录入、明细与报告', async () => {
  const { ctx, page, errors } = await fresh();
  await startApp(page);
  await page.click('#kindSwitch [data-kind="income"]');
  await page.waitForTimeout(300);
  assert.match(await page.textContent('#catLabel'), /来源/);
  assert.equal(await page.locator('#catGrid .cat').count(), 10);
  assert.match(await page.textContent('.cat[data-cat="in_salary"]'), /工资/);
  await page.click('.cat[data-cat="in_salary"]');
  for (const k of ['8', '0', '0', '0']) await page.click(`.key[data-k="${k}"]`);
  await page.click('#saveBtn');
  await page.waitForTimeout(600);
  const data = await store(page);
  assert.equal(data.records[0].kind, 'income');
  assert.equal(data.records[0].cat, 'in_salary');

  await page.click('#kindSwitch [data-kind="expense"]');
  await page.waitForTimeout(250);
  await addRecord(page, 'food', ['3', '2', '.', '5'], '午饭');

  await page.click('[data-tab="list"]');
  await page.waitForTimeout(500);
  const row = page.locator('.row.income').first();
  assert.match(await row.textContent(), /工资/);
  assert.match(await row.textContent(), /\+¥8,000\.00/);

  await row.click();
  await page.waitForTimeout(400);
  assert.match(await page.textContent('#editCats'), /工资/);
  assert.doesNotMatch(await page.textContent('#editCats'), /餐饮/);
  await page.click('#sheetClose');
  await page.waitForTimeout(300);

  await page.click('.typechips [data-type="income"]');
  await page.waitForTimeout(300);
  assert.equal(await page.locator('.row').count(), 1);

  await page.click('[data-tab="report"]');
  await page.waitForTimeout(500);
  await page.click('.rtype [data-rtype="income"]');
  await page.waitForTimeout(300);
  await page.waitForFunction(() => {
    const el = document.querySelector('#heroNum');
    return el && el.textContent.replace(/,/g, '') === '8000.00';
  }, null, { timeout: 5000 });
  assert.match(await page.textContent('.hero-label'), /总收入/);
  await page.click('.rtype [data-rtype="expense"]');
  await page.waitForTimeout(300);
  await page.waitForFunction(() => {
    const el = document.querySelector('#heroNum');
    return el && el.textContent.replace(/,/g, '') === '32.50';
  }, null, { timeout: 5000 });
  await page.click('.rtype [data-rtype="income"]');
  await page.waitForTimeout(300);
  await page.waitForFunction(() => {
    const el = document.querySelector('#heroNum');
    return el && el.textContent.replace(/,/g, '') === '8000.00';
  }, null, { timeout: 5000 });
  assert.deepEqual(errors, []);
  await ctx.close();
});
