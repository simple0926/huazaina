import { test } from 'node:test';
import assert from 'node:assert/strict';
import { createRequire } from 'node:module';
const require = createRequire(import.meta.url);
const C = require('../src/core.js');

test('区间：日/周/月/年', () => {
  assert.deepEqual(C.rangeOf('day', '2026-11-02'), { start: '2026-11-02', end: '2026-11-02' });
  assert.deepEqual(C.rangeOf('week', '2026-11-02'), { start: '2026-11-02', end: '2026-11-08' });
  assert.deepEqual(C.rangeOf('week', '2026-11-08'), { start: '2026-11-02', end: '2026-11-08' });
  assert.deepEqual(C.rangeOf('month', '2026-02-10'), { start: '2026-02-01', end: '2026-02-28' });
  assert.deepEqual(C.rangeOf('month', '2024-02-10'), { start: '2024-02-01', end: '2024-02-29' });
  assert.deepEqual(C.rangeOf('year', '2026-06-01'), { start: '2026-01-01', end: '2026-12-31' });
});

test('跨月跨年的前后跳转', () => {
  assert.equal(C.shiftAnchor('month', '2026-01-31', -1), '2025-12-01');
  assert.equal(C.shiftAnchor('month', '2026-12-15', 1), '2027-01-01');
  assert.equal(C.shiftAnchor('day', '2026-12-31', 1), '2027-01-01');
  assert.equal(C.shiftAnchor('week', '2026-11-02', -1), '2026-10-26');
  assert.equal(C.shiftAnchor('year', '2026-05-05', -1), '2025-01-01');
});

test('标签：周跨年显示年份', () => {
  assert.match(C.labelOf('week', '2026-01-01'), /2025年12月29日/);
  assert.equal(C.labelOf('month', '2026-11-02'), '2026年11月');
  assert.equal(C.labelOf('year', '2026-11-02'), '2026年');
  assert.equal(C.labelOf('day', '2026-11-02'), '2026年11月2日 周一');
});

test('金额：分与显示', () => {
  assert.equal(C.money(0), '0.00');
  assert.equal(C.money(123456789), '1,234,567.89');
  assert.equal(C.money(5), '0.05');
  assert.equal(C.toCents('32.5'), 3250);
  assert.equal(C.toCents('0.29'), 29);
  assert.equal(C.toCents('abc'), 0);
  assert.equal(C.toCents('-10'), 1000); // 负数取绝对值后再校验
});

test('汇总与分类排序', () => {
  const records = [
    { id: 'a', amt: 1000, cat: 'food', date: '2026-11-01', pay: 'wechat', ts: 1 },
    { id: 'b', amt: 2500, cat: 'shop', date: '2026-11-01', pay: 'alipay', ts: 2 },
    { id: 'c', amt: 500, cat: 'food', date: '2026-11-02', pay: 'cash', ts: 3 },
    { id: 'd', amt: 9999, cat: 'home', date: '2026-10-31', pay: 'card', ts: 4 }
  ];
  const sum = C.summarize(records, '2026-11-01', '2026-11-30');
  assert.equal(sum.total, 4000);
  assert.equal(sum.count, 3);
  assert.deepEqual(sum.byCat, { food: 1500, shop: 2500 });
  assert.equal(sum.byDay['2026-11-02'], 500);
  assert.equal(sum.max.id, 'b');
  const rank = C.ranking(sum, C.CATS);
  assert.deepEqual(rank.map((r) => r.id), ['shop', 'food']);
  assert.ok(Math.abs(rank.reduce((s, r) => s + r.pct, 0) - 100) < 0.001);
});

test('序列：周 7 天、月天数、年 12 个月', () => {
  const byDay = { '2026-11-01': 100, '2026-11-08': 200, '2026-02-03': 50, '2026-02-20': 70 };
  const week = C.series('week', C.rangeOf('week', '2026-11-02'), byDay);
  assert.equal(week.length, 7);
  assert.equal(week[0].key, '2026-11-02');
  assert.equal(week[6].key, '2026-11-08');
  assert.equal(week[6].value, 200);

  const feb = C.series('month', C.rangeOf('month', '2026-02-10'), byDay);
  assert.equal(feb.length, 28);
  assert.equal(feb[2].value, 50);
  assert.equal(feb[19].value, 70);

  const year = C.series('year', C.rangeOf('year', '2026-05-05'), byDay);
  assert.equal(year.length, 12);
  assert.equal(year[1].value, 120);
});

test('环比', () => {
  assert.equal(C.delta(120, 100).pct, 20);
  assert.equal(C.delta(120, 100).dir, 'up');
  assert.equal(C.delta(80, 100).dir, 'down');
  assert.equal(C.delta(100, 0).pct, null);
});

test('CSV：BOM、表头、转义、排序', () => {
  const cats = C.CATS;
  const records = [
    { id: 'b', amt: 2000, cat: 'shop', date: '2026-11-02', pay: 'alipay', note: '耳机, 带"降噪"', ts: 2 },
    { id: 'a', amt: 3250, cat: 'food', date: '2026-11-01', pay: 'wechat', note: '', ts: 1 }
  ];
  const csv = C.csv(records, cats);
  assert.ok(csv.startsWith('\ufeff日期,月份,类型,分类,金额,支付方式,备注\r\n'));
  const lines = csv.trim().split('\r\n');
  assert.equal(lines[1], '2026-11-01,2026-11,支出,餐饮,32.50,微信,');
  assert.equal(lines[2], '2026-11-02,2026-11,支出,购物,20.00,支付宝,"耳机, 带""降噪"""');
});

test('CSV：大额不带千分位，Excel 可直接计算', () => {
  const text = C.csv([{ id: 'z', amt: 320000, cat: 'home', date: '2026-11-01', pay: 'card', note: '' }], C.CATS);
  assert.match(text, /,3200\.00,/);
  assert.ok(!/3,200\.00/.test(text));
});

test('收入：分类、CSV 与旧备份迁移', () => {
  assert.equal(C.kindOf({ cat: 'in_salary' }), 'income');
  assert.equal(C.kindOf({ cat: 'food' }), 'expense');
  assert.equal(C.catsFor('income').length, 10);
  const csv = C.csv([{ id: 'i', amt: 800000, cat: 'in_salary', date: '2026-11-01', pay: 'card', note: '' }], C.CATS);
  assert.match(csv, /2026-11-01,2026-11,收入,工资,8000\.00,银行卡,/);
  assert.equal(C.normalizeBackup({ records: [{ amt: 100, cat: 'food', date: '2026-11-01' }] }).records[0].kind, 'expense');
  assert.equal(C.normalizeBackup({ records: [{ amt: 100, cat: 'in_salary', date: '2026-11-01' }] }).records[0].kind, 'income');
});

test('备份与恢复', () => {
  const records = [{ id: 'x', amt: 1234, cat: 'food', date: '2026-11-02', pay: 'wechat', note: '面', ts: 5 }];
  const text = C.backup(records, C.CATS);
  const data = C.normalizeBackup(JSON.parse(text));
  assert.equal(data.records.length, 1);
  assert.equal(data.records[0].amt, 1234);
  assert.equal(data.cats.length, 10);

  assert.throws(() => C.normalizeBackup({}), /不是/);
  const bad = C.normalizeBackup({ records: [
    { amt: 100, date: 'not-a-date' }, { amt: -5, date: '2026-01-01' }, { amt: 300, date: '2026-01-01' }
  ] });
  assert.equal(bad.records.length, 1);
});

test('示例数据：确定、可解析、范围正确', () => {
  const a = C.sampleRecords('2026-11-02');
  const b = C.sampleRecords('2026-11-02');
  assert.equal(JSON.stringify(a), JSON.stringify(b));
  assert.ok(a.length > 60);
  assert.ok(a.every((r) => /^\d{4}-\d{2}-\d{2}$/.test(r.date)));
  assert.ok(a.every((r) => r.amt > 0 && C.catById(C.CATS, r.cat)));
  assert.ok(a.every((r) => r.date <= '2026-11-02'));
});

test('周期天数', () => {
  assert.equal(C.periodDays('day', C.rangeOf('day', '2026-11-02')), 1);
  assert.equal(C.periodDays('week', C.rangeOf('week', '2026-11-02')), 7);
  assert.equal(C.periodDays('month', C.rangeOf('month', '2024-02-10')), 29);
  assert.equal(C.periodDays('year', C.rangeOf('year', '2026-01-01')), 365);
});
