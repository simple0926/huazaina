/* 花在哪 · 数据层（纯函数，可在浏览器与 Node 中共用） */
(function (root, factory) {
  const api = factory();
  if (typeof module === 'object' && module.exports) module.exports = api;
  root.HZCore = api;
})(typeof globalThis !== 'undefined' ? globalThis : this, function () {
  'use strict';

  const VERSION = '1.2.0';
  const WEEKDAYS = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];

  const CATS = [
    { id: 'food',   name: '餐饮', color: '#9C6A2A', icon: 'bowl' },
    { id: 'transit',name: '交通', color: '#3D6B57', icon: 'bus' },
    { id: 'shop',   name: '购物', color: '#9B4F58', icon: 'bag' },
    { id: 'home',   name: '居住', color: '#5D6B3E', icon: 'home' },
    { id: 'fun',    name: '娱乐', color: '#6A5A86', icon: 'ticket' },
    { id: 'med',    name: '医疗', color: '#9A3E35', icon: 'cross' },
    { id: 'study',  name: '学习', color: '#2F6B64', icon: 'book' },
    { id: 'social', name: '人情', color: '#A97834', icon: 'gift' },
    { id: 'phone',  name: '通讯', color: '#4C5A63', icon: 'phone' },
    { id: 'other',  name: '其他', color: '#7A7263', icon: 'dots' }
  ];


  const INCOME_CATS = [
    { id: 'in_salary',    name: '工资', color: '#2F5A43', icon: 'wallet' },
    { id: 'in_bonus',     name: '奖金', color: '#A97834', icon: 'award' },
    { id: 'in_invest',    name: '理财', color: '#3D6B57', icon: 'trend' },
    { id: 'in_side',      name: '兼职', color: '#6A5A86', icon: 'briefcase' },
    { id: 'in_reimburse', name: '报销', color: '#4C5A63', icon: 'receipt' },
    { id: 'in_gift',      name: '礼金', color: '#9B4F58', icon: 'gift' },
    { id: 'in_refund',    name: '退款', color: '#5D6B3E', icon: 'refund' },
    { id: 'in_rent',      name: '租金', color: '#9C6A2A', icon: 'home' },
    { id: 'in_dividend',  name: '分红', color: '#2F6B64', icon: 'coins' },
    { id: 'in_other',     name: '其他', color: '#7A7263', icon: 'dots' }
  ];

  function isIncomeCat(id) {
    return String(id || '').indexOf('in_') === 0;
  }

  function kindOf(r) {
    if (!r) return 'expense';
    if (r.kind === 'income' || r.kind === 'expense') return r.kind;
    return isIncomeCat(r.cat) ? 'income' : 'expense';
  }

  function catsFor(kind, custom) {
    if (kind === 'income') return INCOME_CATS;
    const list = custom && custom.length ? custom : CATS;
    return list.filter(function (c) { return !isIncomeCat(c.id); });
  }

  const PAYS = [
    { id: 'wechat', name: '微信' },
    { id: 'alipay', name: '支付宝' },
    { id: 'card',   name: '银行卡' },
    { id: 'cash',   name: '现金' },
    { id: 'other',  name: '其他' }
  ];

  const COLOR_PICKS = ['#9C6A2A', '#3D6B57', '#9B4F58', '#5D6B3E', '#6A5A86',
    '#9A3E35', '#2F6B64', '#A97834', '#4C5A63', '#7A7263', '#B08A52', '#6B6254'];

  function pad2(n) { return String(n).padStart(2, '0'); }

  function uid() {
    return Date.now().toString(36) + Math.random().toString(36).slice(2, 8);
  }

  function isoOf(d) {
    return d.getFullYear() + '-' + pad2(d.getMonth() + 1) + '-' + pad2(d.getDate());
  }

  function todayISO() { return isoOf(new Date()); }

  function parseISO(s) {
    const p = String(s).split('-').map(Number);
    return new Date(p[0], p[1] - 1, p[2]);
  }

  function addDays(iso, n) {
    const d = parseISO(iso);
    d.setDate(d.getDate() + n);
    return isoOf(d);
  }

  function mondayOf(iso) {
    const d = parseISO(iso);
    const wd = (d.getDay() + 6) % 7; // 周一=0
    d.setDate(d.getDate() - wd);
    return isoOf(d);
  }

  function daysInMonth(y, m) { return new Date(y, m, 0).getDate(); }

  function rangeOf(kind, anchor) {
    const d = parseISO(anchor);
    const y = d.getFullYear();
    const m = d.getMonth();
    if (kind === 'day') return { start: anchor, end: anchor };
    if (kind === 'week') {
      const start = mondayOf(anchor);
      return { start, end: addDays(start, 6) };
    }
    if (kind === 'month') {
      return { start: isoOf(new Date(y, m, 1)), end: isoOf(new Date(y, m + 1, 0)) };
    }
    return { start: isoOf(new Date(y, 0, 1)), end: isoOf(new Date(y, 11, 31)) };
  }

  function shiftAnchor(kind, anchor, delta) {
    const d = parseISO(anchor);
    if (kind === 'day') return addDays(anchor, delta);
    if (kind === 'week') return addDays(anchor, delta * 7);
    if (kind === 'month') {
      d.setDate(1);
      d.setMonth(d.getMonth() + delta);
      return isoOf(d);
    }
    d.setMonth(0, 1);
    d.setFullYear(d.getFullYear() + delta);
    return isoOf(d);
  }

  function fmtDate(iso, withWeek) {
    const d = parseISO(iso);
    let s = (d.getMonth() + 1) + '月' + d.getDate() + '日';
    if (withWeek) s += ' ' + WEEKDAYS[d.getDay()];
    return s;
  }

  function labelOf(kind, anchor) {
    const r = rangeOf(kind, anchor);
    const d = parseISO(anchor);
    if (kind === 'day') return d.getFullYear() + '年' + fmtDate(anchor, true);
    if (kind === 'week') {
      const a = parseISO(r.start), b = parseISO(r.end);
      const sameYear = a.getFullYear() === b.getFullYear();
      const head = sameYear ? '' : a.getFullYear() + '年';
      return head + (a.getMonth() + 1) + '月' + a.getDate() + '日 – '
        + (b.getMonth() + 1) + '月' + b.getDate() + '日';
    }
    if (kind === 'month') return d.getFullYear() + '年' + (d.getMonth() + 1) + '月';
    return d.getFullYear() + '年';
  }

  function period(kind, anchor) {
    return { kind, anchor, range: rangeOf(kind, anchor), label: labelOf(kind, anchor) };
  }

  function prevAnchor(kind, anchor) { return shiftAnchor(kind, anchor, -1); }

  function money(cents) {
    const neg = cents < 0;
    const v = Math.abs(Math.round(cents));
    const yuan = Math.floor(v / 100);
    const fen = v % 100;
    const s = String(yuan).replace(/\B(?=(\d{3})+(?!\d))/g, ',') + '.' + pad2(fen);
    return (neg ? '-' : '') + s;
  }

  function toCents(text) {
    const t = String(text == null ? '' : text).replace(/[^\d.]/g, '');
    if (!t) return 0;
    const n = Number(t);
    if (!isFinite(n) || n <= 0) return 0;
    return Math.round(n * 100);
  }

  function catById(cats, id) {
    let c = (cats || []).find(function (x) { return x.id === id; });
    if (!c) c = INCOME_CATS.find(function (x) { return x.id === id; });
    if (!c) c = { id: 'other', name: '其他', color: '#7A7263', icon: 'dots' };
    return c;
  }

  function payName(id) {
    const p = PAYS.find(function (x) { return x.id === id; });
    return p ? p.name : '其他';
  }

  /* 汇总一段区间 */
  function summarize(records, start, end) {
    const list = records.filter(function (r) { return r.date >= start && r.date <= end; });
    const byCat = {}, byPay = {}, byDay = {};
    let total = 0, max = null;
    for (let i = 0; i < list.length; i++) {
      const r = list[i];
      total += r.amt;
      byCat[r.cat] = (byCat[r.cat] || 0) + r.amt;
      byPay[r.pay || 'other'] = (byPay[r.pay || 'other'] || 0) + r.amt;
      byDay[r.date] = (byDay[r.date] || 0) + r.amt;
      if (!max || r.amt > max.amt) max = r;
    }
    return { list: list, total: total, count: list.length, byCat: byCat, byPay: byPay, byDay: byDay, max: max };
  }

  function ranking(sum, cats) {
    return Object.keys(sum.byCat).map(function (id) {
      const cat = catById(cats, id);
      return { id: id, name: cat.name, color: cat.color, icon: cat.icon, amt: sum.byCat[id],
        pct: sum.total ? sum.byCat[id] / sum.total * 100 : 0 };
    }).sort(function (a, b) { return b.amt - a.amt; });
  }

  function payRanking(sum) {
    return Object.keys(sum.byPay).map(function (id) {
      return { id: id, name: payName(id), amt: sum.byPay[id],
        pct: sum.total ? sum.byPay[id] / sum.total * 100 : 0 };
    }).sort(function (a, b) { return b.amt - a.amt; });
  }

  function series(kind, range, byDay) {
    const out = [];
    if (kind === 'day') return out;
    if (kind === 'week') {
      const start = range.start;
      for (let i = 0; i < 7; i++) {
        const iso = addDays(start, i);
        const d = parseISO(iso);
        out.push({ key: iso, label: '周' + ['一', '二', '三', '四', '五', '六', '日'][i],
          short: WEEKDAYS[d.getDay()].slice(1), value: byDay[iso] || 0, show: true });
      }
      return out;
    }
    if (kind === 'month') {
      const d = parseISO(range.start);
      const n = daysInMonth(d.getFullYear(), d.getMonth() + 1);
      for (let i = 1; i <= n; i++) {
        const iso = isoOf(new Date(d.getFullYear(), d.getMonth(), i));
        out.push({ key: iso, label: String(i), value: byDay[iso] || 0,
          show: i === 1 || i === n || (i % 5 === 0 && i <= n - 2) });
      }
      return out;
    }
    const y = parseISO(range.start).getFullYear();
    for (let m = 1; m <= 12; m++) {
      let v = 0;
      Object.keys(byDay).forEach(function (iso) {
        if (Number(iso.slice(0, 4)) === y && Number(iso.slice(5, 7)) === m) v += byDay[iso];
      });
      out.push({ key: y + '-' + pad2(m), label: m + '月', value: v, show: true });
    }
    return out;
  }

  function delta(cur, prev) {
    if (!prev) return { pct: null, dir: cur > 0 ? 'up' : 'flat' };
    const pct = (cur - prev) / prev * 100;
    return { pct: pct, dir: pct > 0.5 ? 'up' : pct < -0.5 ? 'down' : 'flat' };
  }

  function periodDays(kind, range) {
    if (kind === 'day') return 1;
    const a = parseISO(range.start), b = parseISO(range.end);
    return Math.round((b - a) / 86400000) + 1;
  }

  /* CSV：带 BOM，Excel 直接打开 */
  function csv(records, cats) {
    const esc = function (v) {
      const s = String(v == null ? '' : v);
      return /[",\n\r]/.test(s) ? '"' + s.replace(/"/g, '""') + '"' : s;
    };
    const header = ['日期', '月份', '类型', '分类', '金额', '支付方式', '备注'];
    const rows = records.slice().sort(function (a, b) {
      return a.date === b.date ? (a.ts || 0) - (b.ts || 0) : (a.date < b.date ? -1 : 1);
    }).map(function (r) {
      return [r.date, r.date.slice(0, 7), kindOf(r) === 'income' ? '收入' : '支出',
        catById(cats, r.cat).name, (r.amt / 100).toFixed(2), payName(r.pay), r.note || '']
        .map(esc).join(',');
    });
    return '\ufeff' + [header.join(',')].concat(rows).join('\r\n') + '\r\n';
  }

  function summaryText(kind, anchor, sum, cats) {
    const r = ranking(sum, cats);
    const top = r[0];
    if (!top) return labelOf(kind, anchor) + '还没有记账。';
    return labelOf(kind, anchor) + '共花 ¥' + money(sum.total) + '，' + top.name + '最多，占 '
      + Math.round(top.pct) + '%。';
  }

  function backup(records, cats, now) {
    return JSON.stringify({
      app: '花在哪', version: VERSION, exportedAt: new Date(now || Date.now()).toISOString(),
      records: records, cats: cats
    }, null, 2);
  }

  function normalizeBackup(obj) {
    if (!obj || !Array.isArray(obj.records)) throw new Error('不是「花在哪」的备份文件');
    const records = obj.records.filter(function (r) {
      return r && typeof r.date === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(r.date)
        && Number.isFinite(Number(r.amt)) && Number(r.amt) > 0;
    }).map(function (r) {
      return {
        id: String(r.id || uid()),
        amt: Math.round(Number(r.amt)),
        cat: String(r.cat || 'other'),
        date: r.date,
        pay: r.pay ? String(r.pay) : 'other',
        note: typeof r.note === 'string' ? r.note.slice(0, 80) : '',
        kind: kindOf(r),
        ts: Number(r.ts) || 0
      };
    });
    const cats = Array.isArray(obj.cats) ? obj.cats.filter(function (c) {
      return c && c.id && c.name;
    }).map(function (c) {
      return { id: String(c.id), name: String(c.name).slice(0, 8),
        color: /^#[0-9a-f]{6}$/i.test(c.color) ? c.color : '#7B7568',
        icon: String(c.icon || 'dots'), custom: !!c.custom };
    }) : [];
    return { records: records, cats: cats };
  }

  /* 示例数据：固定种子，保证每次一致 */
  function mulberry32(a) {
    return function () {
      a |= 0; a = a + 0x6D2B79F5 | 0;
      let t = Math.imul(a ^ a >>> 15, 1 | a);
      t = t + Math.imul(t ^ t >>> 7, 61 | t) ^ t;
      return ((t ^ t >>> 14) >>> 0) / 4294967296;
    };
  }

  function sampleRecords(today) {
    const rnd = mulberry32(20261102);
    const pick = function (arr) { return arr[Math.floor(rnd() * arr.length)]; };
    const notes = {
      food: ['早饭 豆浆油条', '公司楼下简餐', '和朋友吃火锅', '咖啡', '外卖', '超市买菜', '面馆'],
      transit: ['地铁', '公交', '打车', '共享单车月卡', '高铁票'],
      shop: ['洗衣液 + 纸巾', '一件外套', '耳机', '日用品', '快递'],
      home: ['水电燃气', '网费', '物业费', '日用品'],
      fun: ['电影票', '演出', '游戏', '周末爬山'],
      med: ['感冒药', '门诊挂号', '体检'],
      study: ['技术书', '网课', '订阅'],
      social: ['朋友生日礼物', '同事份子钱', '请客吃饭'],
      phone: ['话费充值', '流量包'],
      other: ['理发', '捐款', '停车费']
    };
    const range = { food: [1200, 8800], transit: [300, 2600], shop: [2000, 26000],
      home: [8000, 34000], fun: [3000, 18000], med: [1500, 12000], study: [3000, 20000],
      social: [5000, 40000], phone: [2000, 10000], other: [500, 5000] };
    const pays = ['wechat', 'alipay', 'alipay', 'wechat', 'card', 'cash'];
    const ids = CATS.map(function (c) { return c.id; });
    const out = [];
    const end = parseISO(today);
    for (let i = 0; i < 78; i++) {
      const d = new Date(end.getFullYear(), end.getMonth(), end.getDate());
      d.setDate(d.getDate() - i);
      const dow = d.getDay();
      const isWeekend = dow === 0 || dow === 6;
      const n = i === 0 ? 2 : Math.max(1, (isWeekend ? 2 : 2) + (rnd() < 0.35 ? 1 : 0));
      for (let k = 0; k < n; k++) {
        let cat, amt, note;
        if (d.getDate() === 1 && k === 0) {
          // 每月一号一笔房租
          cat = 'home'; amt = 320000; note = '房租';
        } else {
          cat = pick(ids);
          if (cat === 'home') {
            amt = Math.round((8000 + rnd() * 26000) / 100) * 100;
          } else {
            const r = range[cat];
            amt = Math.round((r[0] + rnd() * (r[1] - r[0])) / 100) * 100;
          }
          note = pick(notes[cat]);
        }
        out.push({
          id: 's' + i + '_' + k, amt: amt, cat: cat, date: isoOf(d), pay: pick(pays),
          note: note,
          ts: new Date(d.getFullYear(), d.getMonth(), d.getDate(),
            7 + Math.floor(rnd() * 14), Math.floor(rnd() * 60)).getTime()
        });
      }
    }
    return out.sort(function (a, b) { return a.date < b.date ? 1 : a.date > b.date ? -1 : b.ts - a.ts; });
  }

  return {
    VERSION: VERSION, WEEKDAYS: WEEKDAYS, CATS: CATS, INCOME_CATS: INCOME_CATS,
    PAYS: PAYS, COLOR_PICKS: COLOR_PICKS,
    uid: uid, isoOf: isoOf, todayISO: todayISO, parseISO: parseISO, addDays: addDays,
    mondayOf: mondayOf, rangeOf: rangeOf, shiftAnchor: shiftAnchor, labelOf: labelOf,
    period: period, prevAnchor: prevAnchor, fmtDate: fmtDate, money: money, toCents: toCents,
    catById: catById, isIncomeCat: isIncomeCat, kindOf: kindOf, catsFor: catsFor,
    payName: payName, summarize: summarize, ranking: ranking,
    payRanking: payRanking, series: series, delta: delta, periodDays: periodDays,
    csv: csv, summaryText: summaryText, backup: backup, normalizeBackup: normalizeBackup,
    sampleRecords: sampleRecords
  };
});
