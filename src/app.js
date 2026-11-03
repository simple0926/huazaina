/* 花在哪 · 交互 */
(function () {
  'use strict';

  const C = window.HZCore;
  const $ = function (s, r) { return (r || document).querySelector(s); };
  const $$ = function (s, r) { return Array.prototype.slice.call((r || document).querySelectorAll(s)); };
  const KEY = 'huazaina-v1';

  const ICONS = {
    bowl: '<path d="M3.5 11h17a8.5 8.5 0 0 1-17 0z"/><path d="M8 8.2c.55-1 .55-2.1 0-3.1M12 8.2c.55-1 .55-2.1 0-3.1M16 8.2c.55-1 .55-2.1 0-3.1"/>',
    bus: '<rect x="4" y="4" width="16" height="12.5" rx="3"/><path d="M4 10.5h16"/><path d="M7.5 16.5v2.2M16.5 16.5v2.2"/><circle cx="8" cy="13.6" r=".9" fill="currentColor" stroke="none"/><circle cx="16" cy="13.6" r=".9" fill="currentColor" stroke="none"/>',
    bag: '<path d="M5.4 8.2h13.2L17.4 20H6.6z"/><path d="M9 8.2V6.4a3 3 0 0 1 6 0v1.8"/>',
    home: '<path d="M4 10.6 12 4l8 6.6V20H4z"/><path d="M9.8 20v-5.2h4.4V20"/>',
    ticket: '<path d="M4 8.2A2.2 2.2 0 0 1 6.2 6h11.6A2.2 2.2 0 0 1 20 8.2v1.4a2.4 2.4 0 0 0 0 4.8v1.4a2.2 2.2 0 0 1-2.2 2.2H6.2A2.2 2.2 0 0 1 4 15.8v-1.4a2.4 2.4 0 0 0 0-4.8z"/><path d="M14.5 6.4v11.2" stroke-dasharray="2 2.4"/>',
    cross: '<circle cx="12" cy="12" r="8.2"/><path d="M12 8.4v7.2M8.4 12h7.2"/>',
    book: '<path d="M5 5.4A1.9 1.9 0 0 1 6.9 3.5H19V17H6.9A1.9 1.9 0 0 0 5 18.9z"/><path d="M5 18.9A1.9 1.9 0 0 0 6.9 20.8H19V17"/>',
    gift: '<rect x="4" y="9" width="16" height="4" rx="1"/><path d="M5.5 13v7h13v-7M12 9v11"/><path d="M12 9S10.8 4.5 8.4 4.5a2.1 2.1 0 0 0 0 4.5H12zM12 9s1.2-4.5 3.6-4.5a2.1 2.1 0 0 1 0 4.5H12z"/>',
    phone: '<rect x="7" y="3" width="10" height="18" rx="2.6"/><path d="M10.5 6.2h3"/><circle cx="12" cy="17.8" r=".7" fill="currentColor" stroke="none"/>',
    dots: '<circle cx="6" cy="12" r="1.1" fill="currentColor" stroke="none"/><circle cx="12" cy="12" r="1.1" fill="currentColor" stroke="none"/><circle cx="18" cy="12" r="1.1" fill="currentColor" stroke="none"/>',
    download: '<path d="M12 3.5v11"/><path d="M7.5 10.5 12 15l4.5-4.5"/><path d="M4 16.5v2.3a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-2.3"/>',
    share: '<path d="M12 15.5v-11"/><path d="M7.5 8.5 12 4l4.5 4.5"/><path d="M4 16.5v2.3a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-2.3"/>',
    upload: '<path d="M12 15.5v-11"/><path d="M7.5 8.5 12 4l4.5 4.5"/><path d="M4 16.5v2.3a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-2.3"/>',
    trash: '<path d="M4.5 6.5h15"/><path d="M9.5 6.5V4.8a1.3 1.3 0 0 1 1.3-1.3h2.4a1.3 1.3 0 0 1 1.3 1.3v1.7"/><path d="M6.5 6.5 7.5 20h9l1-13.5"/><path d="M10.5 10v6M13.5 10v6"/>',
    wallet: '<path d="M4 7.5A2.5 2.5 0 0 1 6.5 5H19a1 1 0 0 1 1 1v12a1 1 0 0 1-1 1H6.5A2.5 2.5 0 0 1 4 16.5z"/><path d="M4 8h15"/><circle cx="16.5" cy="13" r="1.1" fill="currentColor" stroke="none"/>',
    award: '<circle cx="12" cy="9" r="5"/><path d="M8.6 13.4 7 21l5-2.5L17 21l-1.6-7.6"/>',
    trend: '<path d="M4 17l5-5 3 3 7-8"/><path d="M14 7h5v5"/>',
    briefcase: '<rect x="3.5" y="7" width="17" height="12.5" rx="2"/><path d="M9 7V5.5A1.5 1.5 0 0 1 10.5 4h3A1.5 1.5 0 0 1 15 5.5V7M3.5 12h17"/>',
    receipt: '<path d="M6 3.5h12v17l-3-2-3 2-3-2-3 2z"/><path d="M9 8h6M9 12h6M9 16h3"/>',
    refund: '<path d="M4 10a8 8 0 1 1 2.3 6.3"/><path d="M4 4v6h6"/>',
    coins: '<ellipse cx="12" cy="7" rx="6" ry="3"/><path d="M6 7v5c0 1.7 2.7 3 6 3s6-1.3 6-3V7"/><path d="M6 12v5c0 1.7 2.7 3 6 3s6-1.3 6-3v-5"/>',
    gear: '<circle cx="12" cy="12" r="3.1"/><path d="M19.4 14.6a1.7 1.7 0 0 0 .34 1.88l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.7 1.7 0 0 0-1.88-.34 1.7 1.7 0 0 0-1 1.55V21a2 2 0 1 1-4 0v-.1a1.7 1.7 0 0 0-1.1-1.55 1.7 1.7 0 0 0-1.88.34l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.7 1.7 0 0 0 .34-1.88 1.7 1.7 0 0 0-1.55-1H3a2 2 0 1 1 0-4h.1a1.7 1.7 0 0 0 1.55-1.1 1.7 1.7 0 0 0-.34-1.88l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.7 1.7 0 0 0 1.88.34H9a1.7 1.7 0 0 0 1-1.55V3a2 2 0 1 1 4 0v.1a1.7 1.7 0 0 0 1 1.55 1.7 1.7 0 0 0 1.88-.34l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.7 1.7 0 0 0-.34 1.88V9a1.7 1.7 0 0 0 1.55 1H21a2 2 0 1 1 0 4h-.1a1.7 1.7 0 0 0-1.5 1z"/>'
  };

  function ico(name) {
    return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" '
      + 'stroke-linecap="round" stroke-linejoin="round">' + (ICONS[name] || ICONS.dots) + '</svg>';
  }
  function esc(s) {
    return String(s == null ? '' : s).replace(/[&<>"']/g, function (ch) {
      return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[ch];
    });
  }
  function hexA(hex, a) {
    const h = hex.replace('#', '');
    const n = parseInt(h.length === 3 ? h.split('').map(function (c) { return c + c; }).join('') : h, 16);
    return 'rgba(' + ((n >> 16) & 255) + ',' + ((n >> 8) & 255) + ',' + (n & 255) + ',' + a + ')';
  }
  function haptic(ms) { try { if (navigator.vibrate) navigator.vibrate(ms || 8); } catch (e) {} }

  const PAY_COLORS = { wechat: '#557E5C', alipay: '#466E92', card: '#33302A', cash: '#B08A52', other: '#857D6D' };

  function state_meta_lastPay() {
    try { return JSON.parse(localStorage.getItem('huazaina-v1') || '{}').meta.lastPay; } catch (e) { return null; }
  }

  const state = {
    records: [],
    cats: C.CATS.map(function (c) { return Object.assign({}, c); }),
    welcomed: false,
    meta: {},
    tab: 'add',
    draft: { kind: 'expense', amt: '0', cat: null, date: C.todayISO(), note: '', pay: (state_meta_lastPay() || 'wechat') },
    list: { anchor: C.todayISO(), type: 'all', filter: 'all', search: '', searchOpen: false, scroll: 0 },
    report: { kind: 'month', rtype: 'expense', anchor: C.todayISO() },
    sheetOpen: false
  };

  /* ---------------- 存储 ---------------- */
  function load() {
    let raw = null;
    try { raw = localStorage.getItem(KEY); } catch (e) {}
    if (!raw) return;
    try {
      const data = JSON.parse(raw);
      if (Array.isArray(data.records)) {
        state.records = data.records.filter(function (r) {
          return r && typeof r.date === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(r.date)
            && Number.isFinite(Number(r.amt)) && Number(r.amt) > 0;
        }).map(function (r) {
          return { id: String(r.id || C.uid()), amt: Math.round(Number(r.amt)),
            cat: String(r.cat || 'other'), kind: C.kindOf(r), date: r.date, pay: r.pay || 'other',
            note: typeof r.note === 'string' ? r.note : '', ts: Number(r.ts) || 0 };
        });
      }
      if (Array.isArray(data.cats)) {
        const base = C.CATS.map(function (c) { return Object.assign({}, c); });
        const custom = data.cats.filter(function (c) {
          return c && c.custom && c.id && c.name && !base.some(function (b) { return b.id === c.id; });
        }).map(function (c) {
          return { id: String(c.id), name: String(c.name).slice(0, 8),
            color: /^#[0-9a-f]{6}$/i.test(c.color) ? c.color : '#7B7568',
            icon: 'dots', custom: true };
        });
        state.cats = base.concat(custom);
      }
      state.welcomed = !!data.welcomed;
      state.meta = data.meta || {};
    } catch (e) {}
  }

  function save() {
    try {
      localStorage.setItem(KEY, JSON.stringify({
        v: 1, records: state.records, cats: state.cats.filter(function (c) { return c.custom; }),
        welcomed: state.welcomed, meta: state.meta
      }));
      return true;
    } catch (e) {
      toast('存储空间不足，请先导出备份');
      return false;
    }
  }

  /* ---------------- 通用 UI ---------------- */
  let toastTimer = null;
  function toast(msg, actionLabel, onAction) {
    const el = $('#toast'), btn = $('#toastAction');
    $('#toastText').textContent = msg;
    if (actionLabel) {
      btn.hidden = false;
      btn.textContent = actionLabel;
      btn.onclick = function () {
        el.classList.remove('on');
        clearTimeout(toastTimer);
        if (onAction) onAction();
      };
    } else {
      btn.hidden = true;
      btn.onclick = null;
    }
    el.classList.add('on');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(function () { el.classList.remove('on'); }, actionLabel ? 4200 : 2000);
  }

  function openSheet(opt) {
    const sheet = $('#sheet'), scrim = $('#scrim');
    $('#sheetTitle').textContent = opt.title || '';
    $('#sheetBody').innerHTML = opt.body || '';
    $('#sheetFoot').innerHTML = opt.foot || '';
    $('#sheet').classList.toggle('sheet--tall', !!opt.tall);
    sheet.classList.add('on');
    scrim.classList.add('on');
    state.sheetOpen = true;
    $('#sheetBody').scrollTop = 0;
    if (opt.onMount) opt.onMount($('#sheetBody'), $('#sheetFoot'));
    if (opt.autofocus) {
      const input = $('#sheetBody .autofocus');
      if (input) input.focus();
    }
  }

  function closeSheet() {
    if (!state.sheetOpen) return;
    state.sheetOpen = false;
    $('#sheet').classList.remove('on');
    $('#scrim').classList.remove('on');
    $('#sheet').classList.remove('sheet--tall');
    setTimeout(function () {
      if (!state.sheetOpen) {
        $('#sheetBody').innerHTML = '';
        $('#sheetFoot').innerHTML = '';
      }
    }, 360);
  }

  function confirmSheet(opt) {
    openSheet({
      title: opt.title,
      body: '<p class="confirm-text">' + esc(opt.text) + '</p>',
      foot: '<button class="btn btn-ghost" data-cancel>取消</button>'
        + '<button class="btn ' + (opt.danger ? 'btn-danger' : 'btn-primary') + '" data-ok>'
        + esc(opt.ok || '确定') + '</button>',
      onMount: function (body, foot) {
        foot.querySelector('[data-cancel]').onclick = closeSheet;
        foot.querySelector('[data-ok]').onclick = function () { closeSheet(); opt.onOk(); };
      }
    });
  }

  /* ---------------- 记账 ---------------- */
  function draftCents() { return C.toCents(state.draft.amt); }

  function updateAmountDisplay() {
    const el = $('#amountVal'), box = $('#amount');
    const s = state.draft.amt;
    let text;
    if (s === '0') text = '0.00';
    else {
      const parts = s.split('.');
      let int = parts[0].replace(/^0+(?=\d)/, '');
      int = int.replace(/\B(?=(\d{3})+(?!\d))/g, ',');
      text = parts.length > 1 ? int + '.' + parts[1] : int;
    }
    el.textContent = text;
    box.classList.toggle('zero', s === '0');
    box.classList.toggle('sm', text.length > 8 && text.length <= 11);
    box.classList.toggle('xs', text.length > 11);
    $('#amountHint').classList.remove('warn');
  }

  function pressKey(k) {
    if (undoState) clearUndoState();
    const d = state.draft;
    if (k === 'back') {
      if (d.amt.endsWith('.')) d.amt = d.amt.slice(0, -1) || '0';
      else d.amt = d.amt.slice(0, -1) || '0';
    } else if (k === '.') {
      if (!d.amt.includes('.')) d.amt = (d.amt === '0' ? '0' : d.amt) + '.';
    } else {
      if (d.amt === '0') d.amt = k;
      else {
        const parts = d.amt.split('.');
        if (parts[1] && parts[1].length >= 2) return;
        if (!parts[1] && parts[0].replace(/\D/g, '').length >= 7) return;
        d.amt += k;
      }
    }
    haptic(5);
    updateAmountDisplay();
    updateSaveState();
  }

  function updateSaveState() {
    const ok = !!state.draft.cat && draftCents() > 0;
    const btn = $('#saveBtn');
    btn.disabled = !ok;
    if (!undoState) btn.textContent = saveLabel();
  }

  function renderCats() {
    $('#catGrid').innerHTML = C.catsFor(state.draft.kind, state.cats).map(function (c) {
      return '<button class="cat' + (state.draft.cat === c.id ? ' on' : '') + '"'
        + ' data-cat="' + c.id + '" style="--c:' + c.color + '">'
        + '<span class="cat-ico">' + ico(c.icon) + '</span>'
        + '<span class="cat-name">' + esc(c.name) + '</span></button>';
    }).join('');
  }

  function renderMeta() {
    const d = state.draft;
    const today = C.todayISO();
    const label = d.date === today ? '今天' : d.date === C.addDays(today, -1) ? '昨天'
      : C.fmtDate(d.date, false);
    $('#metaDate').textContent = label;
    $('#btnDate').classList.toggle('is-set', d.date !== today);
    $('#metaNote').textContent = d.note ? d.note : '无';
    $('#btnNote').classList.toggle('is-set', !!d.note);
    $('#metaPay').textContent = C.payName(d.pay);
  }

  function monthTotal(anchor, kind) {
    const p = C.period('month', anchor);
    const recs = state.records.filter(function (r) { return C.kindOf(r) === (kind || 'expense'); });
    return C.summarize(recs, p.range.start, p.range.end).total;
  }

  function defaultHint() {
    return state.draft.kind === 'income' ? '输入金额，选个来源' : '输入金额，选个分类';
  }
  function saveLabel() {
    return state.draft.kind === 'income' ? '记下收入' : '记下这笔';
  }

  function updateToday() {
    const el = $('#todayLine');
    if (!el) return;
    const type = state.draft.kind;
    const list = state.records.filter(function (r) {
      return r.date === C.todayISO() && C.kindOf(r) === type;
    });
    const total = list.reduce(function (a, r) { return a + r.amt; }, 0);
    el.innerHTML = list.length
      ? '<i></i>今天 <b>' + list.length + ' 笔' + (type === 'income' ? '收入' : '') + '</b> · ¥' + C.money(total)
      : '<i style="background:var(--ink-4)"></i>今天还没有' + (type === 'income' ? '收入' : '记录');
  }

  function updateHeader() {
    updateToday();
    const t = state.tab;
    const brand = $('#brandline'), title = $('#topTitle'), sub = $('#topSub');
    const headRow = $('.top-row');
    if (headRow) headRow.style.display = t === 'add' ? 'flex' : 'none';
    if (t === 'add') {
      brand.textContent = '花在哪';
      title.textContent = C.fmtDate(state.draft.date, true);
      sub.textContent = (state.draft.kind === 'income' ? '本月收入 ¥' : '本月已花 ¥')
        + C.money(monthTotal(C.todayISO(), state.draft.kind));
    } else if (t === 'list') {
      brand.textContent = '明细 / LEDGER';
      title.textContent = '明细';
      sub.textContent = '数据只在本机 · 已记 ' + state.records.length + ' 笔';
    } else {
      brand.textContent = '月结单 / STATEMENT';
      title.textContent = '报告';
      sub.textContent = '日 / 周 / 月 / 年 · 数据只在本机';
    }
  }

  function updateKindUI() {
    const d = state.draft;
    $$('#kindSwitch button').forEach(function (b) {
      b.classList.toggle('on', b.dataset.kind === d.kind);
    });
    $('#catLabel').textContent = d.kind === 'income' ? '来源 / SOURCE' : '分类 / CATEGORY';
    $('#payLabel').textContent = d.kind === 'income' ? '收款' : '付款';
    const hint = $('#amountHint');
    if (!hint.classList.contains('warn')) hint.textContent = defaultHint();
    if (!undoState) $('#saveBtn').textContent = saveLabel();
    updateHeader();
  }

  let saving = false;

  let undoState = null;
  let undoTimer = null;

  function clearUndoState() {
    if (undoTimer) clearTimeout(undoTimer);
    undoTimer = null;
    undoState = null;
    const btn = $('#saveBtn');
    if (btn) {
      btn.classList.remove('is-done');
      btn.textContent = saveLabel();
    }
  }

  function showUndo(rec) {
    const btn = $('#saveBtn');
    undoState = rec;
    btn.disabled = false;
    btn.classList.add('is-done');
    btn.textContent = '已记下 ¥' + C.money(rec.amt) + ' · 撤销';
    clearTimeout(undoTimer);
    undoTimer = setTimeout(function () {
      clearUndoState();
      updateSaveState();
    }, 4200);
  }

  function undoSave() {
    if (!undoState) return;
    const rec = undoState;
    clearUndoState();
    state.records = state.records.filter(function (r) { return r.id !== rec.id; });
    save();
    updateHeader();
    if (state.tab === 'list') renderListView();
    if (state.tab === 'report') renderReport();
    updateSaveState();
    $('#amountHint').textContent = '已撤销这一笔';
    setTimeout(function () {
      if ($('#amountHint').textContent === '已撤销这一笔') {
        $('#amountHint').textContent = defaultHint();
      }
    }, 1800);
    haptic(10);
  }

  function saveRecord() {
    if (saving) return;
    const d = state.draft;
    const cents = draftCents();
    if (!d.cat || cents <= 0) {
      $('#amountHint').textContent = !d.cat
        ? (d.kind === 'income' ? '先选一个来源' : '先选一个分类') : '金额要大于 0';
      $('#amountHint').classList.add('warn');
      haptic(20);
      return;
    }
    saving = true;
    setTimeout(function () { saving = false; }, 650);
    const rec = { id: C.uid(), amt: cents, cat: d.cat, kind: d.kind, date: d.date,
      pay: d.pay, note: d.note.trim(), ts: Date.now() };
    state.records.push(rec);
    state.meta.lastPay = d.pay;
    if (!save()) {
      state.records.pop();
      saving = false;
      return;
    }
    haptic(14);
    state.draft.amt = '0';
    state.draft.note = '';
    updateAmountDisplay();
    updateSaveState();
    renderMeta();
    updateHeader();
    showUndo(rec);
  }

  function setTab(tab) {
    if (state.tab === tab) return;
    state.tab = tab;
    $$('.tab').forEach(function (b) { b.classList.toggle('is-on', b.dataset.tab === tab); });
    $$('.view').forEach(function (v) { v.classList.toggle('is-on', v.dataset.view === tab); });
    updateHeader();
    if (tab === 'list') renderListView();
    if (tab === 'report') renderReport();
    if (tab === 'add') { updateAmountDisplay(); renderMeta(); }
  }

  /* ---------------- 明细 ---------------- */
  function timeLabel(ts) {
    if (!ts) return '';
    const d = new Date(ts);
    return String(d.getHours()).padStart(2, '0') + ':' + String(d.getMinutes()).padStart(2, '0');
  }

  function dayLabel(iso) {
    const today = C.todayISO();
    if (iso === today) return '今天';
    if (iso === C.addDays(today, -1)) return '昨天';
    const d = C.parseISO(iso);
    return (d.getMonth() + 1) + '月' + d.getDate() + '日 ' + C.WEEKDAYS[d.getDay()];
  }

  function filteredRecords() {
    const p = C.period('month', state.list.anchor);
    const q = state.list.search.trim().toLowerCase();
    return state.records.filter(function (r) {
      if (r.date < p.range.start || r.date > p.range.end) return false;
      if (state.list.type !== 'all' && C.kindOf(r) !== state.list.type) return false;
      if (state.list.filter !== 'all' && r.cat !== state.list.filter) return false;
      if (q) {
        const cat = C.catById(state.cats, r.cat).name;
        const hay = (r.note + ' ' + cat + ' ' + C.payName(r.pay)).toLowerCase();
        if (hay.indexOf(q) < 0) return false;
      }
      return true;
    }).sort(function (a, b) {
      if (a.date !== b.date) return a.date < b.date ? 1 : -1;
      return (b.ts || 0) - (a.ts || 0);
    });
  }

  function renderListView() {
    const view = $('.view[data-view="list"]');
    if (!view) return;
    const p = C.period('month', state.list.anchor);
    const monthAll = state.records.filter(function (r) {
      return r.date >= p.range.start && r.date <= p.range.end;
    });
    const exp = C.summarize(monthAll.filter(function (r) { return C.kindOf(r) === 'expense'; }), p.range.start, p.range.end);
    const inc = C.summarize(monthAll.filter(function (r) { return C.kindOf(r) === 'income'; }), p.range.start, p.range.end);
    const visible = state.list.type === 'all' ? monthAll : monthAll.filter(function (r) {
      return C.kindOf(r) === state.list.type;
    });
    const sum = C.summarize(visible, p.range.start, p.range.end);
    const isCurrent = p.range.start <= C.todayISO() && C.todayISO() <= p.range.end;
    const allCats = state.cats.concat(C.INCOME_CATS);
    const chips = C.ranking(sum, allCats).map(function (c) {
      return '<button class="chip' + (state.list.filter === c.id ? ' on' : '') + '" data-filter="' + c.id + '">'
        + esc(c.name) + '</button>';
    }).join('');
    const typeRow = monthAll.length
      ? '<div class="typechips">'
        + [['all', '全部'], ['expense', '支出'], ['income', '收入']].map(function (x) {
          return '<button data-type="' + x[0] + '" class="' + (state.list.type === x[0] ? 'on' : '') + '">' + x[1] + '</button>';
        }).join('')
        + '</div>' : '';

    view.innerHTML =
      '<div class="month-bar">'
        + '<button class="mb-btn" data-month="-1" aria-label="上个月">' + chevron('left') + '</button>'
        + '<div class="mb-main"><div class="mb-title">' + p.label + '</div>'
        + '<div class="mb-sub"><b>支出 ¥' + C.money(exp.total) + '</b><span class="mb-sep">·</span><b>收入 ¥' + C.money(inc.total) + '</b></div></div>'
        + '<button class="mb-btn" data-month="1" aria-label="下个月"' + (isCurrent ? ' disabled' : '') + '>' + chevron('right') + '</button>'
        + '<button class="mb-btn" data-search aria-label="搜索">' + searchIcon() + '</button>'
      + '</div>'
      + (state.list.searchOpen
        ? '<div class="search-row"><input class="search-input" id="searchInput" placeholder="搜备注、分类、支付方式" value="' + esc(state.list.search) + '"></div>'
        : '')
      + typeRow
      + (sum.count ? '<div class="chips"><button class="chip' + (state.list.filter === 'all' ? ' on' : '')
        + '" data-filter="all">全部分类</button>' + chips + '</div>' : '')
      + '<div class="scroll" id="listScroll"></div>';

    if (state.list.searchOpen) {
      const input = $('#searchInput');
      input.focus();
      input.setSelectionRange(input.value.length, input.value.length);
      let timer = null;
      input.addEventListener('input', function () {
        clearTimeout(timer);
        timer = setTimeout(function () {
          state.list.search = input.value;
          renderListRows();
        }, 120);
      });
    }
    view.querySelector('.month-bar').addEventListener('click', function (e) {
      const nav = e.target.closest('[data-month]');
      if (nav) {
        state.list.anchor = C.shiftAnchor('month', state.list.anchor, Number(nav.dataset.month));
        state.list.filter = 'all';
        state.list.scroll = 0;
        renderListView();
        updateHeader();
        return;
      }
      if (e.target.closest('[data-search]')) {
        state.list.searchOpen = !state.list.searchOpen;
        if (!state.list.searchOpen) state.list.search = '';
        renderListView();
      }
    });
    const chipWrap = view.querySelector('.chips');
    if (chipWrap) chipWrap.addEventListener('click', function (e) {
      const b = e.target.closest('[data-filter]');
      if (!b) return;
      state.list.filter = b.dataset.filter;
      chipWrap.querySelectorAll('.chip').forEach(function (x) { x.classList.toggle('on', x === b); });
      renderListRows();
    });
    const typeWrap = view.querySelector('.typechips');
    if (typeWrap) typeWrap.addEventListener('click', function (e) {
      const b = e.target.closest('[data-type]');
      if (!b || state.list.type === b.dataset.type) return;
      state.list.type = b.dataset.type;
      state.list.filter = 'all';
      state.list.scroll = 0;
      renderListView();
    });
    renderListRows();
    const sc = $('#listScroll');
    sc.addEventListener('scroll', function () { state.list.scroll = sc.scrollTop; });
    sc.scrollTop = state.list.scroll;
  }

  function chevron(dir) {
    return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="'
      + (dir === 'left' ? 'M14.5 5.5 8 12l6.5 6.5' : 'M9.5 5.5 16 12l-6.5 6.5') + '"/></svg>';
  }
  function searchIcon() {
    return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"><circle cx="10.8" cy="10.8" r="6.4"/><path d="M15.5 15.5 20 20"/></svg>';
  }

  function renderListRows() {
    const sc = $('#listScroll');
    if (!sc) return;
    const list = filteredRecords();
    if (!state.records.length) {
      sc.innerHTML = emptyState(
        '还没有记过账',
        '花销和收入都随手记一笔，金额和分类就够了。之后日、周、月、年的账，这里都会替你算好。',
        '记一笔', 'add', '看示例数据', 'sample');
      bindEmpty(sc);
      return;
    }
    if (!list.length) {
      const typeName = state.list.type === 'income' ? '收入' : state.list.type === 'expense' ? '支出' : '记录';
      sc.innerHTML = emptyState(
        state.list.search ? '没有匹配的记录' : '这个月还没有' + typeName,
        state.list.search ? '换个关键词，或清空搜索。'
          : state.list.type === 'income' ? '换个月份看看，或者回「记账」切到收入记一笔。'
          : '换个月份看看，或者回「记账」记下第一笔。',
        state.list.search ? '清空搜索' : '回到本月', state.list.search ? 'clearSearch' : 'thisMonth');
      bindEmpty(sc);
      return;
    }
    const p = C.period('month', state.list.anchor);
    const monthList = state.records.filter(function (r) {
      return r.date >= p.range.start && r.date <= p.range.end;
    }).sort(function (a, b) {
      if (a.date !== b.date) return a.date < b.date ? -1 : 1;
      return (a.ts || 0) - (b.ts || 0);
    });
    const idxMap = {};
    monthList.forEach(function (r, i) { idxMap[r.id] = String(i + 1).padStart(2, '0'); });

    const groups = {};
    list.forEach(function (r) { (groups[r.date] = groups[r.date] || []).push(r); });
    sc.innerHTML = Object.keys(groups).sort(function (a, b) { return a < b ? 1 : -1; }).map(function (date) {
      const rows = groups[date];
      const de = rows.filter(function (r) { return C.kindOf(r) === 'expense'; })
        .reduce(function (s, r) { return s + r.amt; }, 0);
      const di = rows.filter(function (r) { return C.kindOf(r) === 'income'; })
        .reduce(function (s, r) { return s + r.amt; }, 0);
      const dayTotal = de && di
        ? '<em>支出</em> ¥' + C.money(de) + ' · <em>收入</em> ¥' + C.money(di)
        : '<em>小计</em> ¥' + C.money(de || di);
      return '<div class="day"><div class="day-head"><span class="day-date"><b>' + dayLabel(date) + '</b></span>'
        + '<span class="day-total">' + dayTotal + '</span></div>'
        + '<div class="rows">' + rows.map(function (r) {
          const cat = C.catById(state.cats, r.cat);
          const isIncome = C.kindOf(r) === 'income';
          const bits = [];
          const t = timeLabel(r.ts);
          if (r.note) bits.push(r.note);
          bits.push(C.payName(r.pay));
          if (t) bits.push(t);
          return '<button class="row' + (isIncome ? ' income' : '') + '" data-id="' + r.id + '">'
            + '<span class="row-idx">' + (idxMap[r.id] || '') + '</span>'
            + '<span class="row-name">' + esc(cat.name) + '</span>'
            + '<span class="row-note">' + esc(bits.join(' · ')) + '</span>'
            + '<span class="leader"></span>'
            + '<span class="row-amt">' + (isIncome ? '+' : '') + '¥' + C.money(r.amt) + '</span></button>';
        }).join('') + '</div></div>';
    }).join('');

    sc.onclick = function (e) {
      const row = e.target.closest('[data-id]');
      if (!row) return;
      const rec = state.records.find(function (r) { return r.id === row.dataset.id; });
      if (rec) openEdit(rec);
    };
  }

  function emptyState(title, text, btn, action, btn2, action2) {
    return '<div class="empty empty-page">'
      + '<svg class="empty-mark" viewBox="0 0 64 64" fill="none" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round">'
      + '<path d="M18 10h28v44l-7-5-7 5-7-5-7 5z" stroke-dasharray="3 3"/>'
      + '<path d="M26 24h12M26 32h12M26 40h7"/>'
      + '</svg>'
      + '<div class="empty-title">' + esc(title) + '</div>'
      + '<div class="empty-text">' + esc(text) + '</div>'
      + '<div class="empty-actions">'
      + '<button class="empty-btn primary" data-empty="' + action + '">' + esc(btn) + '</button>'
      + (btn2 ? '<button class="empty-btn" data-empty="' + action2 + '">' + esc(btn2) + '</button>' : '')
      + '</div></div>';
  }
  function bindEmpty(root) {
    const b = root.querySelector('[data-empty]');
    if (!b) return;
    b.onclick = function () {
      const act = b.dataset.empty;
      if (act === 'add') setTab('add');
      if (act === 'sample') loadSample();
      if (act === 'thisMonth') { state.list.anchor = C.todayISO(); renderListView(); updateHeader(); }
      if (act === 'clearSearch') {
        state.list.search = ''; state.list.searchOpen = false; renderListView();
      }
    };
  }

  /* ---------------- 报告 ---------------- */
  function compactMoney(cents) {
    const yuan = cents / 100;
    if (yuan >= 10000) return '¥' + (yuan / 10000).toFixed(yuan >= 100000 ? 0 : 1) + '万';
    return '¥' + C.money(cents).replace(/\.00$/, '');
  }

  function countUp(el, cents) {
    const reduce = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (reduce || !cents) { el.textContent = C.money(cents); return; }
    const start = performance.now();
    function tick(t) {
      const p = Math.min(1, (t - start) / 460);
      const e = 1 - Math.pow(1 - p, 3);
      el.textContent = C.money(Math.round(cents * e));
      if (p < 1) requestAnimationFrame(tick);
    }
    requestAnimationFrame(tick);
  }

  function elapsedDays(kind, range) {
    if (kind === 'day') return 1;
    const today = C.todayISO();
    if (today < range.start) return 1;
    const end = today < range.end ? today : range.end;
    return Math.round((C.parseISO(end) - C.parseISO(range.start)) / 86400000) + 1;
  }

  function reportBody() {
    const kind = state.report.kind;
    const rtype = state.report.rtype;
    const anchor = state.report.anchor;
    const p = C.period(kind, anchor);
    const base = state.records.filter(function (r) { return C.kindOf(r) === rtype; });
    const sum = C.summarize(base, p.range.start, p.range.end);
    const prevP = C.period(kind, C.prevAnchor(kind, anchor));
    const prev = C.summarize(base, prevP.range.start, prevP.range.end);
    const days = elapsedDays(kind, p.range);
    const periodLen = C.periodDays(kind, p.range);
    const avg = Math.round(sum.total / days);
    const today = C.todayISO();
    const isCurrent = p.range.start <= today && today <= p.range.end;
    const elapsed = elapsedDays(kind, p.range);
    let prevSum = prev;
    let cmpLabel = { day: '比昨天', week: '比上周', month: '比上月', year: '比去年' }[kind];
    let prevLabel = { day: '昨天', week: '上周', month: '上个月', year: '去年' }[kind];
    if (isCurrent && kind !== 'day') {
      const end = C.addDays(prevP.range.start, elapsed - 1);
      prevSum = C.summarize(state.records, prevP.range.start, end > prevP.range.end ? prevP.range.end : end);
      cmpLabel = '比' + prevLabel + '同期';
      prevLabel = prevLabel + '同期';
    }
    const d = C.delta(sum.total, prevSum.total);
    const cats = C.catsFor(rtype, state.cats);
    const rank = C.ranking(sum, cats);
    const pays = C.payRanking(sum);
    const nowLabel = { day: '这一天', week: '这一周', month: '这个月', year: '这一年' }[kind];
    const isIncome = rtype === 'income';
    const trendTitle = (isIncome
      ? { week: '每日收入 / DAILY', month: '每日收入 / DAILY', year: '每月收入 / MONTHLY' }
      : { week: '每日支出 / DAILY', month: '每日支出 / DAILY', year: '每月支出 / MONTHLY' })[kind];
    const heroLabel = isIncome ? '总收入' : '总支出';
    const payLabel = isIncome ? '收款方式 / PAYMENT' : '支付方式 / PAYMENT';
    const emptyLabel = isIncome ? '这一期还没有收入' : '这一期还没有支出';

    let deltaHtml;
    if (isCurrent && kind !== 'day' && days < 3) deltaHtml = '';
    else if (!prevSum.count) deltaHtml = '<span class="delta">' + prevLabel + '无记录</span>';
    else if (d.pct != null && d.pct > 300) deltaHtml = '<span class="delta up">↑ 远高于' + prevLabel + '</span>';
    else if (d.pct == null || Math.abs(d.pct) < 0.5) deltaHtml = '<span class="delta">与' + prevLabel + '持平</span>';
    else deltaHtml = '<span class="delta ' + (d.dir === 'up' ? 'up' : 'down') + '">'
      + (d.dir === 'up' ? '↑ ' : '↓ ') + Math.round(Math.abs(d.pct)) + '% ' + cmpLabel + '</span>';

    let html = '<div class="report-top' + (isIncome ? ' income' : '') + '">'
      + '<div class="seg" id="seg">'
      + '<span class="seg-ind" id="segInd" style="transform:translateX(' + (['day', 'week', 'month', 'year'].indexOf(kind) * 100) + '%)"></span>'
      + ['day', 'week', 'month', 'year'].map(function (k, i) {
        return '<button data-kind="' + k + '" class="' + (k === kind ? 'on' : '') + '">'
          + ['日', '周', '月', '年'][i] + '</button>';
      }).join('') + '</div>'
      + '<div class="period-nav">'
      + '<button class="mb-btn" data-pn="-1" aria-label="上一期">' + chevron('left') + '</button>'
      + '<div class="pn-label">' + p.label + (isCurrent ? '' : '<span class="pn-today" data-today>回到' + ({ day: '今天', week: '本周', month: '本月', year: '今年' }[kind]) + '</span>') + '</div>'
      + '<button class="mb-btn" data-pn="1" aria-label="下一期"' + (isCurrent ? ' disabled' : '') + '>' + chevron('right') + '</button>'
      + '</div>'
      + '<div class="hero"><div class="hero-head"><div class="hero-label">' + heroLabel + '</div>'
      + '<div class="rtype" id="rtype"><button data-rtype="expense" class="' + (!isIncome ? 'on' : '') + '">支出</button>'
      + '<button data-rtype="income" class="' + (isIncome ? 'on' : '') + '">收入</button></div></div>'
      + '<div class="hero-num"><small>¥</small><b id="heroNum">0.00</b></div>'
      + '<div class="hero-meta"><span>' + sum.count + ' 笔</span>'
      + (sum.count ? '<i></i>' + (kind === 'day' ? '<span>' + rank.length + ' 个分类</span>'
        : '<span>日均 ¥' + C.money(avg) + (days < periodLen ? '（' + days + ' 天）' : '') + '</span>') + deltaHtml : '')
      + '</div></div>'
      + '</div>'
      + '<div class="scroll">';

    if (!sum.count) {
      html += '<div class="section"><div class="empty" style="padding:26px 10px">'
        + '<div class="empty-title">' + emptyLabel + '</div>'
        + '<div class="empty-text">' + (state.records.length ? '换到有记录的时间段，或切换支出 / 收入看看。' : '记上几笔之后，这里会告诉你钱花在了哪里。') + '</div>'
        + '<button class="empty-btn" data-goto-add>去记一笔</button></div></div>'
        + '</div></div>';
      return html;
    }

    /* 三条 KPI */
    const maxDayKey = Object.keys(sum.byDay).reduce(function (a, b) {
      return sum.byDay[b] > sum.byDay[a] ? b : a;
    }, Object.keys(sum.byDay)[0]);
    const maxCat = sum.max ? C.catById(cats, sum.max.cat) : null;
    html += '<div class="kpi-row">'
      + '<div class="kpi"><span class="k">最大一笔</span><b>' + (maxCat ? esc(maxCat.name) + ' ¥' + C.money(sum.max.amt) : '—') + '</b></div>'
      + '<div class="kpi"><span class="k">最高一天</span><b>' + C.fmtDate(maxDayKey, false).replace('月', '.').replace('日', '') + ' ¥' + C.money(sum.byDay[maxDayKey]) + '</b></div>'
      + '<div class="kpi"><span class="k">笔均</span><b>¥' + C.money(Math.round(sum.total / sum.count)) + '</b></div>'
      + '</div>';

    /* 趋势：每日 / 每月折线 */
    if (kind !== 'day') {
      const series = C.series(kind, p.range, sum.byDay);
      const vals = series.map(function (s) { return s.value; });
      const max = Math.max.apply(null, vals.concat([1]));
      const maxIdx = vals.indexOf(max);
      const W = 354, H = 64;
      const pts = vals.map(function (v, i) {
        return [3 + i * ((W - 6) / Math.max(1, vals.length - 1)), H - 3 - (v / max) * (H - 12)];
      });
      const line = pts.map(function (pt, i) {
        return (i ? 'L' : 'M') + pt[0].toFixed(1) + ' ' + pt[1].toFixed(1);
      }).join(' ');
      const area = line + ' L' + (W - 3) + ' ' + (H - 1) + ' L3 ' + (H - 1) + ' Z';
      const maxWhen = kind === 'year' ? Number(series[maxIdx].key.slice(5, 7)) + '月' : C.fmtDate(series[maxIdx].key, false);
      html += '<div class="section"><div class="section-head"><span class="label">' + trendTitle + '</span>'
        + '<span class="sub">最高 ' + maxWhen + ' ¥' + C.money(max) + '</span></div>'
        + '<svg class="spark" viewBox="0 0 354 64" preserveAspectRatio="none">'
        + '<line class="base" x1="0" y1="' + (H - 1) + '" x2="' + W + '" y2="' + (H - 1) + '"/>'
        + '<path class="area" d="' + area + '"/><path class="line" d="' + line + '"/>'
        + '<circle class="dot" cx="' + pts[maxIdx][0].toFixed(1) + '" cy="' + pts[maxIdx][1].toFixed(1) + '" r="2.6"/>'
        + '</svg>'
        + '<div class="spark-x">' + series.map(function (s) {
          return '<span class="' + (s.show ? 'on' : '') + '">' + s.label.replace('周', '') + '</span>';
        }).join('') + '</div></div>';
    }

    /* 分类排行 */
    html += '<div class="section"><div class="section-head"><span class="label">分类 / BREAKDOWN</span>'
      + '<span class="sub">占比 · 金额</span></div><div class="sranks">'
      + rank.map(function (c, i) {
        return '<div class="srank"><span class="idx">' + String(i + 1).padStart(2, '0') + '</span>'
          + '<span class="nm">' + esc(c.name) + '</span>'
          + '<span class="track"><i style="width:' + Math.max(2, c.pct) + '%;background:' + c.color + '"></i></span>'
          + '<span class="pct">' + Math.round(c.pct) + '%</span>'
          + '<span class="amt">¥' + C.money(c.amt) + '</span></div>';
      }).join('') + '</div></div>';

    /* 支付方式 */
    if (pays.length) {
      html += '<div class="section"><div class="section-head"><span class="label">' + payLabel + '</span>'
        + '<span class="sub">' + pays.length + ' 种</span></div>'
        + '<div class="pay-bar">' + pays.map(function (x) {
          return '<span class="pay-seg" style="width:' + x.pct + '%;--c:' + (PAY_COLORS[x.id] || PAY_COLORS.other) + '"></span>';
        }).join('') + '</div>'
        + '<div class="pay-legend">' + pays.map(function (x) {
          return '<span class="pay-item"><i style="--c:' + (PAY_COLORS[x.id] || PAY_COLORS.other) + '"></i>'
            + esc(x.name) + ' <b>¥' + C.money(x.amt) + '</b></span>';
        }).join('') + '</div></div>';
    }

    /* 结论 */
    const topCat = rank[0];
    const prevTop = prevSum.byCat[topCat.id] || 0;
    let sentence = '<b>' + esc(topCat.name) + '</b> 占 <b>' + Math.round(topCat.pct) + '%</b>，共 ¥'
      + C.money(topCat.amt) + '，是' + nowLabel + '最大的一类。';
    if (prevSum.count) {
      if (!prevTop) sentence += prevLabel + '没有' + esc(topCat.name) + (isIncome ? '收入。' : '支出。');
      else {
        const diff = topCat.amt - prevTop;
        if (diff === 0) sentence += esc(topCat.name) + '与' + prevLabel + '持平。';
        else sentence += esc(topCat.name) + '比' + prevLabel
          + (diff > 0 ? '多 ' : '少 ') + '<b>¥' + C.money(Math.abs(diff)) + '</b>。';
      }
    }
    if (sum.max) {
      const mc = C.catById(cats, sum.max.cat);
      sentence += '<br>最大的一笔：' + esc(mc.name) + ' <b>¥' + C.money(sum.max.amt) + '</b>，'
        + C.fmtDate(sum.max.date, false) + '。';
    }
    html += '<div class="insight">' + sentence + '</div>';

    if (kind === 'day') {
      const list = sum.list.slice().sort(function (a, b) { return (b.ts || 0) - (a.ts || 0); });
      html += '<div class="section"><div class="section-head"><span class="label">当天各笔 / ENTRIES</span>'
        + '<span class="sub">' + list.length + ' 笔</span></div><div class="mini-list">'
        + list.map(function (r) {
          const cat = C.catById(cats, r.cat);
          return '<div class="mini-row"><i style="--c:' + cat.color + '"></i><span class="mini-name">' + esc(cat.name)
            + (r.note ? '<em>' + esc(r.note) + '</em>' : '') + '</span><b>'
            + (isIncome ? '+' : '') + '¥' + C.money(r.amt) + '</b></div>';
        }).join('') + '</div></div>';
    }

    html += '<div class="report-note"><button class="text-btn" data-export>' + ico('download')
      + '导出这一期 CSV</button></div>';
    html += '</div>';
    return html;
  }

  function renderReport() {
    const view = $('.view[data-view="report"]');
    if (!view) return;
    view.innerHTML = reportBody();
    updateHeader();

    view.onclick = function (e) {
      if (e.target.closest('[data-goto-add]')) { setTab('add'); return; }
      if (e.target.closest('[data-today]')) {
        state.report.anchor = C.todayISO();
        renderReport();
        return;
      }
      if (e.target.closest('[data-export]')) { exportPeriod(); return; }
      const rtBtn = e.target.closest('[data-rtype]');
      if (rtBtn) {
        state.report.rtype = rtBtn.dataset.rtype;
        renderReport();
        return;
      }
      const segBtn = e.target.closest('[data-kind]');
      if (segBtn) {
        state.report.kind = segBtn.dataset.kind;
        state.report.anchor = C.todayISO();
        renderReport();
        return;
      }
      const pn = e.target.closest('[data-pn]');
      if (pn) {
        const next = C.shiftAnchor(state.report.kind, state.report.anchor, Number(pn.dataset.pn));
        const range = C.rangeOf(state.report.kind, next);
        if (pn.dataset.pn === '1' && range.start > C.todayISO()) return;
        state.report.anchor = next;
        renderReport();
        return;
      }
    };
    const heroNum = $('#heroNum');
    if (heroNum) {
      const per = C.period(state.report.kind, state.report.anchor);
      const rtype = state.report.rtype || 'expense';
      const base = state.records.filter(function (r) { return C.kindOf(r) === rtype; });
      const sum = C.summarize(base, per.range.start, per.range.end);
      countUp(heroNum, sum.total);
    }
  }

  /* ---------------- 弹层：日期 ---------------- */
  function openCalendar(current, onPick) {
    let view = current.slice(0, 7);
    const today = C.todayISO();
    openSheet({
      title: '选择日期',
      body: '<div class="quick" id="calQuick">'
        + '<button data-off="0">今天</button><button data-off="-1">昨天</button><button data-off="-2">前天</button></div>'
        + '<div class="cal" id="cal"></div>',
      onMount: function (body) {
        const cal = $('#cal', body);
        function paint() {
          const parts = view.split('-').map(Number);
          const y = parts[0], m = parts[1];
          const first = new Date(y, m - 1, 1);
          const offset = (first.getDay() + 6) % 7;
          const total = new Date(y, m, 0).getDate();
          const cells = [];
          for (let i = offset; i > 0; i--) cells.push({ d: new Date(y, m - 1, 1 - i), dim: true });
          for (let d = 1; d <= total; d++) cells.push({ d: new Date(y, m - 1, d), dim: false });
          let n = 1;
          const need = Math.ceil(cells.length / 7) * 7;
          while (cells.length < need) cells.push({ d: new Date(y, m, n++), dim: true });
          cal.innerHTML = '<div class="cal-head">'
            + '<button class="cal-nav" data-cal-nav="-1">' + chevron('left') + '</button>'
            + '<span class="cal-title">' + y + '年' + m + '月</span>'
            + '<button class="cal-nav" data-cal-nav="1">' + chevron('right') + '</button></div>'
            + '<div class="cal-week"><span>一</span><span>二</span><span>三</span><span>四</span><span>五</span><span>六</span><span>日</span></div>'
            + '<div class="cal-grid">' + cells.map(function (c) {
              const iso = C.isoOf(c.d);
              const future = iso > today;
              const cls = [c.dim ? 'dim' : '', future ? 'future' : '', iso === today ? 'today' : '',
                iso === current ? 'on' : ''].join(' ');
              return '<button class="cal-cell ' + cls + '" data-day="' + iso + '"'
                + (future ? ' disabled' : '') + '>' + c.d.getDate() + '</button>';
            }).join('') + '</div>';
        }
        paint();
        $('#calQuick', body).addEventListener('click', function (e) {
          const b = e.target.closest('[data-off]');
          if (!b) return;
          onPick(C.addDays(today, Number(b.dataset.off)));
          closeSheet();
        });
        cal.addEventListener('click', function (e) {
          const nav = e.target.closest('[data-cal-nav]');
          if (nav) {
            const p = view.split('-').map(Number);
            const d = new Date(p[0], p[1] - 1 + Number(nav.dataset.calNav), 1);
            view = d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0');
            paint();
            return;
          }
          const day = e.target.closest('[data-day]');
          if (day) { onPick(day.dataset.day); closeSheet(); }
        });
      }
    });
  }

  /* ---------------- 弹层：编辑 ---------------- */
  function openEdit(rec) {
    const kind = C.kindOf(rec);
    const catsFor = C.catsFor(kind, state.cats);
    let draft = { amt: (rec.amt / 100).toFixed(2), cat: rec.cat, date: rec.date, note: rec.note, pay: rec.pay || 'other' };
    const body =
      '<div class="field"><div class="field-label">金额</div>'
        + '<input class="input input-num" id="editAmt" inputmode="decimal" value="' + draft.amt + '"></div>'
      + '<div class="field"><div class="field-label">' + (kind === 'income' ? '来源' : '分类') + '</div><div class="picks" id="editCats">'
        + catsFor.map(function (c) {
          return '<button class="pick' + (c.id === draft.cat ? ' on' : '') + '" data-cat="' + c.id + '" style="--c:' + c.color + '">'
            + ico(c.icon) + esc(c.name) + '</button>';
        }).join('') + '</div></div>'
      + '<div class="field"><div class="field-label">日期</div>'
        + '<input class="input" type="date" id="editDate" value="' + draft.date + '" max="' + C.todayISO() + '"></div>'
      + '<div class="field"><div class="field-label">备注</div>'
        + '<input class="input" id="editNote" maxlength="40" placeholder="可选" value="' + esc(draft.note) + '"></div>'
      + '<div class="field"><div class="field-label">' + (kind === 'income' ? '收款方式' : '支付方式') + '</div><div class="picks" id="editPays">'
        + C.PAYS.map(function (p) {
          return '<button class="pick' + (p.id === draft.pay ? ' on' : '') + '" data-pay="' + p.id + '" style="--c:' + PAY_COLORS[p.id] + '"><i></i>' + p.name + '</button>';
        }).join('') + '</div></div>';

    openSheet({
      title: kind === 'income' ? '编辑收入' : '编辑记录',
      body: body,
      foot: '<button class="btn btn-ghost" id="editDel" style="flex:0 0 84px;color:var(--red)">删除</button>'
        + '<button class="btn btn-primary" id="editSave">保存</button>',
      onMount: function (root, foot) {
        const cats = $('#editCats', root), pays = $('#editPays', root);
        cats.addEventListener('click', function (e) {
          const b = e.target.closest('[data-cat]');
          if (!b) return;
          draft.cat = b.dataset.cat;
          $$('.pick', cats).forEach(function (x) { x.classList.toggle('on', x === b); });
        });
        pays.addEventListener('click', function (e) {
          const b = e.target.closest('[data-pay]');
          if (!b) return;
          draft.pay = b.dataset.pay;
          $$('.pick', pays).forEach(function (x) { x.classList.toggle('on', x === b); });
        });
        $('#editDel', foot).onclick = function () {
          closeSheet();
          state.records = state.records.filter(function (r) { return r.id !== rec.id; });
          save();
          updateHeader();
          if (state.tab === 'list') renderListView();
          if (state.tab === 'report') renderReport();
          toast('已删除', '撤销', function () {
            state.records.push(rec);
            save();
            updateHeader();
            if (state.tab === 'list') renderListView();
            if (state.tab === 'report') renderReport();
          });
        };
        $('#editSave', foot).onclick = function () {
          const amt = C.toCents($('#editAmt', root).value);
          const date = $('#editDate', root).value || rec.date;
          if (!amt) { toast('金额要大于 0'); return; }
          rec.amt = amt; rec.cat = draft.cat; rec.kind = kind; rec.date = date;
          rec.note = $('#editNote', root).value.trim().slice(0, 40);
          rec.pay = draft.pay;
          save();
          closeSheet();
          updateHeader();
          if (state.tab === 'list') renderListView();
          if (state.tab === 'report') renderReport();
          toast('已保存修改');
        };
      }
    });
  }

  /* ---------------- 弹层：设置 ---------------- */
  function storageKB() {
    try { return (JSON.stringify(state).length / 1024).toFixed(1) + ' KB'; } catch (e) { return '—'; }
  }

  function openSettings() {
    const first = state.records.length
      ? state.records.reduce(function (a, b) { return a.date < b.date ? a : b; }).date : '';
    const expCount = state.records.filter(function (r) { return C.kindOf(r) === 'expense'; }).length;
    const incCount = state.records.filter(function (r) { return C.kindOf(r) === 'income'; }).length;
    const lastExport = state.meta.lastExport ? C.fmtDate(state.meta.lastExport.slice(0, 10), false) : '还没有导出过';
    openSheet({
      title: '设置',
      body:
        '<div class="group"><div class="group-label">数据</div><div class="list-card">'
        + '<button class="set-row" data-act="stats">'
        + '<span class="set-ico">' + ico('book') + '</span><span class="set-main"><span class="set-name">数据概况</span>'
        + '<span class="set-desc">共 ' + state.records.length + ' 笔 · 支出 ' + expCount + ' · 收入 ' + incCount
        + (first ? ' · 最早 ' + Number(first.slice(0, 4)) + '年' + Number(first.slice(5, 7)) + '月' : '')
        + ' · 约 ' + storageKB() + '</span></span></button>'
        + '<button class="set-row" data-act="csv">'
        + '<span class="set-ico">' + ico('download') + '</span><span class="set-main"><span class="set-name">导出 CSV</span>'
        + '<span class="set-desc">Excel、Numbers 都能直接打开，含日期、月份、类型、分类、金额、支付方式、备注</span></span></button>'
        + '<button class="set-row" data-act="json">'
        + '<span class="set-ico">' + ico('download') + '</span><span class="set-main"><span class="set-name">备份 JSON</span>'
        + '<span class="set-desc">完整数据 + 分类，换手机时用它恢复</span></span></button>'
        + '<button class="set-row" data-act="restore">'
        + '<span class="set-ico">' + ico('upload') + '</span><span class="set-main"><span class="set-name">从备份恢复</span>'
        + '<span class="set-desc">选择之前导出的 .json 文件</span></span></button>'
        + '</div><div class="hint-text">上次导出：' + esc(lastExport) + '。卸载或清除应用数据会一并删除记录，建议每周导出一次。</div></div>'

        + '<div class="group"><div class="group-label">分类</div><div class="list-card">'
        + '<button class="set-row" data-act="cats">'
        + '<span class="set-ico">' + ico('gift') + '</span><span class="set-main"><span class="set-name">管理分类</span>'
        + '<span class="set-desc">支出 ' + state.cats.length + ' 类可自定义；收入 10 类固定</span></span>'
        + '<span class="chev">' + chevron('right') + '</span></button></div></div>'

        + '<div class="group"><div class="group-label">关于</div><div class="list-card">'
        + '<button class="set-row" data-act="about">'
        + '<span class="set-ico">' + ico('dots') + '</span><span class="set-main"><span class="set-name">关于花在哪</span>'
        + '<span class="set-desc">v' + C.VERSION + ' · 不联网 · 不上传 · 无广告</span></span>'
        + '<span class="chev">' + chevron('right') + '</span></button>'
        + '</div></div>'

        + '<div class="group"><div class="group-label">危险操作</div><div class="list-card">'
        + '<button class="set-row danger" data-act="clear">'
        + '<span class="set-ico">' + ico('trash') + '</span><span class="set-main"><span class="set-name">清空所有数据</span>'
        + '<span class="set-desc">删除全部记录和自定义分类，不可恢复</span></span></button>'
        + '</div></div>',
      onMount: function (root) {
        root.addEventListener('click', function (e) {
          const row = e.target.closest('[data-act]');
          if (!row) return;
          const act = row.dataset.act;
          if (act === 'csv') { exportCsv(state.records, '全部'); closeSheet(); }
          if (act === 'json') { exportJson(); closeSheet(); }
          if (act === 'restore') { closeSheet(); setTimeout(importJson, 320); }
          if (act === 'cats') openCategoryManager();
          if (act === 'clear') {
            closeSheet();
            setTimeout(function () {
              confirmSheet({
                title: '清空所有数据？',
                text: '全部 ' + state.records.length + ' 笔记录和自定义分类都会被删除，无法恢复。建议先导出 JSON 备份。',
                ok: '清空', danger: true, onOk: function () {
                  state.records = [];
                  state.cats = C.CATS.map(function (c) { return Object.assign({}, c); });
                  save();
                  updateHeader();
                  if (state.tab === 'list') renderListView();
                  if (state.tab === 'report') renderReport();
                  toast('已清空');
                }
              });
            }, 340);
          }
          if (act === 'about') openAbout();
          if (act === 'stats') {
            toast('共 ' + state.records.length + ' 笔 · 约 ' + storageKB());
          }
        });
      }
    });
  }

  function openAbout() {
    openSheet({
      title: '关于',
      body: '<div class="about"><p><b>花在哪</b> 是一个只属于你的记账本。数据存在手机本地，没有账号、没有云同步、没有广告，也不会上传任何一笔记录。</p>'
        + '<p>导出 CSV 可以直接在 Excel 里透视；导出 JSON 可以在换手机时完整恢复。分析报告支持日、周、月、年，并可在支出 / 收入之间切换。</p>'
        + '<div class="kv"><span>版本</span><span>v' + C.VERSION + '</span></div>'
        + '<div class="kv"><span>存储</span><span>本机 · 约 ' + storageKB() + '</span></div>'
        + '<div class="kv"><span>网络</span><span>未申请网络权限</span></div>'
        + '<div class="kv"><span>包名</span><span>com.simple0926.huazaina</span></div>'
        + '</div>'
    });
  }

  /* ---------------- 分类管理 ---------------- */
  function openCategoryManager() {
    let color = C.COLOR_PICKS[0];
    function bodyHtml() {
      const counts = {};
      state.records.forEach(function (r) { counts[r.cat] = (counts[r.cat] || 0) + 1; });
      return '<div class="hint-text" style="padding:0 0 10px">收入 10 类固定；下面是支出分类，可自定义。</div>'
        + '<div class="cat-manage">' + state.cats.map(function (c) {
        return '<div class="cm-row"><span class="cm-dot" style="--c:' + c.color + ';--c-soft:' + hexA(c.color, .15) + '">' + ico(c.icon) + '</span>'
          + '<span class="cm-name">' + esc(c.name) + '</span>'
          + '<span class="cm-count">' + (counts[c.id] || 0) + ' 笔</span>'
          + (c.custom ? '<button class="cm-del" data-del="' + c.id + '">删除</button>' : '')
          + '</div>';
      }).join('') + '</div>'
        + '<div class="field"><div class="field-label">新增分类</div>'
        + '<input class="input" id="newCatName" maxlength="6" placeholder="比如：宠物、健身、订阅">'
        + '<div class="swatches" id="swatches">' + C.COLOR_PICKS.map(function (h) {
          return '<button class="swatch' + (h === color ? ' on' : '') + '" data-color="' + h + '" style="--c:' + h + '"></button>';
        }).join('') + '</div></div>';
    }
    openSheet({
      title: '管理分类',
      tall: true,
      body: bodyHtml(),
      foot: '<button class="btn btn-primary" id="addCat">添加分类</button>',
      onMount: function (root, foot) {
        root.addEventListener('click', function (e) {
          const sw = e.target.closest('[data-color]');
          if (sw) {
            color = sw.dataset.color;
            $$('.swatch', root).forEach(function (x) { x.classList.toggle('on', x === sw); });
            return;
          }
          const del = e.target.closest('[data-del]');
          if (del) {
            const id = del.dataset.del;
            const cat = state.cats.find(function (c) { return c.id === id; });
            const n = state.records.filter(function (r) { return r.cat === id; }).length;
            closeSheet();
            setTimeout(function () {
              confirmSheet({
                title: '删除「' + cat.name + '」？',
                text: n ? n + ' 笔记录会归到「其他」，分类本身删除。' : '这个分类还没有记录，可以直接删除。',
                ok: '删除', danger: true, onOk: function () {
                  state.records.forEach(function (r) { if (r.cat === id) r.cat = 'other'; });
                  state.cats = state.cats.filter(function (c) { return c.id !== id; });
                  if (state.draft.cat === id) state.draft.cat = null;
                  state.list.filter = 'all';
                  save();
                  renderCats();
                  updateHeader();
                  if (state.tab === 'list') renderListView();
                  if (state.tab === 'report') renderReport();
                  toast('已删除分类');
                }
              });
            }, 320);
          }
        });
        $('#addCat', foot).onclick = function () {
          const input = $('#newCatName', root);
          const name = input.value.trim();
          if (!name) { input.focus(); toast('先给分类起个名字'); return; }
          if (state.cats.some(function (c) { return c.name === name; })
            || C.INCOME_CATS.some(function (c) { return c.name === name; })) {
            toast('已经有同名分类'); return;
          }
          state.cats.push({ id: 'c' + C.uid(), name: name.slice(0, 6), color: color, icon: 'dots', custom: true });
          save();
          closeSheet();
          renderCats();
          updateHeader();
          toast('已添加「' + name + '」');
        };
      }
    });
  }

  /* ---------------- 导入导出 ---------------- */
  function fileName(kind, extra) {
    const d = new Date();
    const stamp = d.getFullYear() + String(d.getMonth() + 1).padStart(2, '0') + String(d.getDate()).padStart(2, '0');
    return '花在哪-' + kind + '-' + (extra || stamp);
  }

  function saveFile(name, mime, text) {
    try {
      if (window.HZNative && window.HZNative.saveFile) {
        const bytes = new TextEncoder().encode(text);
        let bin = '';
        for (let i = 0; i < bytes.length; i += 0x8000) {
          bin += String.fromCharCode.apply(null, bytes.subarray(i, i + 0x8000));
        }
        const res = window.HZNative.saveFile(name, mime, btoa(bin));
        if (res && res.indexOf('保存失败') === 0) { toast(res); return false; }
        toast(res || ('已保存到 下载/花在哪/' + name));
        return true;
      }
      const blob = new Blob([text], { type: mime });
      const a = document.createElement('a');
      a.href = URL.createObjectURL(blob);
      a.download = name;
      document.body.appendChild(a);
      a.click();
      setTimeout(function () { URL.revokeObjectURL(a.href); a.remove(); }, 1000);
      toast('已导出 ' + name);
      return true;
    } catch (e) {
      toast('导出失败，请重试');
      return false;
    }
  }

  function markExport() {
    state.meta.lastExport = new Date().toISOString();
    save();
  }

  function exportCsv(records, scope) {
    if (!records.length) { toast('还没有记录可以导出'); return; }
    const sorted = records.slice().sort(function (a, b) { return a.date < b.date ? -1 : 1; });
    const text = C.csv(sorted, state.cats);
    if (saveFile(fileName('明细', scope === '全部' ? '' : C.period('month', state.list.anchor).label.replace(/[年月]/g, '')), 'text/csv', text)) {
      markExport();
    }
  }

  function exportPeriod() {
    const kind = state.report.kind;
    const rtype = state.report.rtype || 'expense';
    const p = C.period(kind, state.report.anchor);
    const base = state.records.filter(function (r) { return C.kindOf(r) === rtype; });
    const sum = C.summarize(base, p.range.start, p.range.end);
    if (!sum.count) { toast(rtype === 'income' ? '这一期没有收入' : '这一期没有支出'); return; }
    let suffix;
    if (kind === 'year') suffix = p.range.start.slice(0, 4);
    else if (kind === 'month') suffix = p.range.start.slice(0, 7);
    else if (kind === 'week') suffix = p.range.start + '_' + p.range.end.slice(5);
    else suffix = p.range.start;
    saveFile(fileName(rtype === 'income' ? '收入' : '支出', suffix), 'text/csv',
      C.csv(sum.list, C.catsFor(rtype, state.cats)));
    markExport();
  }

  function exportJson() {
    if (!state.records.length && !state.cats.some(function (c) { return c.custom; })) {
      toast('还没有数据可以备份');
      return;
    }
    saveFile(fileName('备份'), 'application/json', C.backup(state.records, state.cats));
    markExport();
  }

  function importJson() {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'application/json,.json';
    input.style.display = 'none';
    document.body.appendChild(input);
    input.onchange = function () {
      const file = input.files && input.files[0];
      input.remove();
      if (!file) return;
      const reader = new FileReader();
      reader.onload = function () {
        let data;
        try {
          data = C.normalizeBackup(JSON.parse(String(reader.result)));
        } catch (e) {
          toast(e.message || '文件读不出来');
          return;
        }
        if (!data.records.length) { toast('这个备份里没有记录'); return; }
        confirmSheet({
          title: '恢复 ' + data.records.length + ' 笔记录？',
          text: '会用备份里的记录替换当前 ' + state.records.length + ' 笔数据。如果只想补回一部分，建议先导出当前备份。',
          ok: '覆盖恢复', onOk: function () {
            state.records = data.records;
            if (data.cats.length) {
              const custom = data.cats.filter(function (c) { return c.custom; });
              state.cats = C.CATS.map(function (c) { return Object.assign({}, c); }).concat(custom);
            }
            save();
            updateHeader();
            if (state.tab === 'list') renderListView();
            if (state.tab === 'report') renderReport();
            toast('已恢复 ' + data.records.length + ' 笔');
          }
        });
      };
      reader.readAsText(file);
    };
    input.click();
  }

  /* ---------------- 示例 & 欢迎 ---------------- */
  function loadSample() {
    const records = C.sampleRecords(C.todayISO());
    confirmSheet({
      title: '载入示例数据？',
      text: '会加入约 78 天的示例记录，方便先看报告效果。随时可以在设置里一键清空。',
      ok: '载入', onOk: function () {
        const ids = new Set(state.records.map(function (r) { return r.id; }));
        records.forEach(function (r) { if (!ids.has(r.id)) state.records.push(r); });
        save();
        state.welcomed = true;
        updateHeader();
        if (state.tab === 'list') renderListView();
        if (state.tab === 'report') renderReport();
        toast('已载入示例，去「报告」看看');
      }
    });
  }

  function openWelcome() {
    openSheet({
      title: '欢迎',
      body: '<div class="welcome">'
        + '<div class="welcome-mark"><span></span></div>'
        + '<h3 class="welcome-title">钱花在哪，<br>你说了算。</h3>'
        + '<p class="welcome-text">没有账号，没有云同步，没有会员费。<br>每一笔只在本机，随时能导出。</p>'
        + '<ul class="welcome-list">'
        + '<li><b>3 秒记一笔</b>金额、分类，支出收入都能记</li>'
        + '<li><b>看得懂的报告</b>日 / 周 / 月 / 年，钱去了哪一目了然</li>'
        + '<li><b>数据是你的</b>一键导出，不锁在别人的服务器里</li>'
        + '</ul></div>',
      foot: '<button class="btn btn-ghost" id="welcomeSample">先看示例</button>'
        + '<button class="btn btn-primary" id="welcomeStart">开始记账</button>',
      onMount: function (root, foot) {
        $('#welcomeStart', foot).onclick = function () {
          state.welcomed = true; save(); closeSheet();
        };
        $('#welcomeSample', foot).onclick = function () {
          state.welcomed = true; save(); closeSheet();
          setTimeout(loadSample, 380);
        };
      }
    });
  }

  /* ---------------- 事件绑定 ---------------- */
  function bind() {
    $('#tabbar').addEventListener('click', function (e) {
      const b = e.target.closest('[data-tab]');
      if (b) setTab(b.dataset.tab);
    });
    $('#kindSwitch').addEventListener('click', function (e) {
      const b = e.target.closest('[data-kind]');
      if (!b || b.dataset.kind === state.draft.kind) return;
      if (undoState) clearUndoState();
      state.draft.kind = b.dataset.kind;
      state.draft.cat = null;
      renderCats();
      updateSaveState();
      updateKindUI();
      $('#amountHint').classList.remove('warn');
      $('#amountHint').textContent = defaultHint();
      haptic(6);
    });
    $('#catGrid').addEventListener('click', function (e) {
      const b = e.target.closest('[data-cat]');
      if (!b) return;
      if (undoState) clearUndoState();
      state.draft.cat = b.dataset.cat;
      $$('.cat', $('#catGrid')).forEach(function (x) { x.classList.toggle('on', x === b); });
      haptic(6);
      updateSaveState();
      $('#amountHint').textContent = defaultHint();
      $('#amountHint').classList.remove('warn');
    });
    $('#keypad').addEventListener('click', function (e) {
      const b = e.target.closest('[data-k]');
      if (b) pressKey(b.dataset.k);
    });
    $('#saveBtn').addEventListener('click', function () {
      if (undoState) undoSave();
      else saveRecord();
    });
    $('#btnSettings').addEventListener('click', openSettings);
    $('#btnDate').addEventListener('click', function () {
      openCalendar(state.draft.date, function (iso) {
        state.draft.date = iso;
        renderMeta();
        updateHeader();
      });
    });
    $('#btnNote').addEventListener('click', function () {
      openSheet({
        title: state.draft.kind === 'income' ? '收入备注' : '备注',
        body: '<div class="field"><div class="field-label">'
          + (state.draft.kind === 'income' ? '这一笔收入是什么' : '这一笔花在什么上') + '</div>'
          + '<input class="input autofocus" id="noteInput" maxlength="40" placeholder="'
          + (state.draft.kind === 'income' ? '可选，比如：10 月工资' : '可选，比如：和朋友吃火锅') + '" value="'
          + esc(state.draft.note) + '"></div>',
        autofocus: true,
        foot: '<button class="btn btn-ghost" id="noteClear">清空</button>'
          + '<button class="btn btn-primary" id="noteSave">确定</button>',
        onMount: function (root, foot) {
          $('#noteSave', foot).onclick = function () {
            state.draft.note = $('#noteInput', root).value.trim().slice(0, 40);
            renderMeta(); closeSheet();
          };
          $('#noteClear', foot).onclick = function () {
            state.draft.note = ''; renderMeta(); closeSheet();
          };
          $('#noteInput', root).addEventListener('keydown', function (e) {
            if (e.key === 'Enter') { $('#noteSave', foot).click(); }
          });
        }
      });
    });
    $('#btnPay').addEventListener('click', function () {
      openSheet({
        title: state.draft.kind === 'income' ? '收款方式' : '支付方式',
        body: '<div class="picks" id="payPicks" style="flex-direction:column">' + C.PAYS.map(function (p) {
          return '<button class="pick' + (p.id === state.draft.pay ? ' on' : '') + '" data-pay="' + p.id
            + '" style="--c:' + PAY_COLORS[p.id] + ';height:44px"><i></i>' + p.name + '</button>';
        }).join('') + '</div>',
        onMount: function (root) {
          $('#payPicks', root).addEventListener('click', function (e) {
            const b = e.target.closest('[data-pay]');
            if (!b) return;
            state.draft.pay = b.dataset.pay;
            renderMeta(); closeSheet();
          });
        }
      });
    });
    $('#scrim').addEventListener('click', closeSheet);
    $('#sheetClose').addEventListener('click', closeSheet);
    document.addEventListener('keydown', function (e) {
      if (e.key === 'Escape') { closeSheet(); return; }
      if (state.tab !== 'add' || state.sheetOpen) return;
      if (/^[0-9]$/.test(e.key)) pressKey(e.key);
      if (e.key === '.' || e.key === ',') pressKey('.');
      if (e.key === 'Backspace') { e.preventDefault(); pressKey('back'); }
      if (e.key === 'Enter') { if (!$('#saveBtn').disabled) saveRecord(); }
    });
    document.addEventListener('visibilitychange', function () {
      if (!document.hidden) updateHeader();
    });
  }

  window.HZBack = function () {
    if (state.sheetOpen) { closeSheet(); return true; }
    if (state.tab !== 'add') { setTab('add'); return true; }
    return false;
  };

  function init() {
    load();
    renderCats();
    renderMeta();
    updateAmountDisplay();
    updateSaveState();
    updateKindUI();
    bind();
    if (!state.welcomed) setTimeout(openWelcome, 260);
  }

  init();
})();
