package com.simple0926.huazaina.nativeapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity {
    private static final int REQ_IMPORT = 1001;
    private static final String[] TYPE_NAMES = {"全部", "支出", "收入"};
    private static final String[] TYPE_IDS = {"all", "expense", "income"};
    private static final String[] REPORT_NAMES = {"日", "周", "月", "年"};
    private static final String[] REPORT_IDS = {"day", "week", "month", "year"};
    private static final String[] CAT_COLORS = {"#9C6A2A", "#3D6B57", "#9B4F58", "#5D6B3E",
            "#6A5A86", "#9A3E35", "#2F6B64", "#A97834"};

    private Db db;
    private List<Rec> records = new ArrayList<>();
    private List<Cat> customCats = new ArrayList<>();
    private LinearLayout header, content, tabbar;
    private final List<FrameLayout> tabCells = new ArrayList<>();
    private final List<View> tabLines = new ArrayList<>();
    private final List<ImageView> tabIcons = new ArrayList<>();
    private final List<TextView> tabIndexes = new ArrayList<>();
    private final List<TextView> tabLabels = new ArrayList<>();
    private String tab = "add";
    private String settingsFrom = "add";

    private String dKind = "expense", dRaw = "0", dCat = null, dDate = Core.todayISO(), dNote = "", dPay = "wechat";
    private TextView amountView, hintView, saveView, todayView, catLabelView, payLabelView, metaDate, metaNote, metaPay, topTitle, topSub;
    private LinearLayout kindExpense, kindIncome;
    private android.widget.GridLayout catGrid;
    private View caret;
    private boolean hintWarn = false;
    private boolean saving = false;
    private Rec undoRec = null;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable undoTick = () -> { undoRec = null; updateSave(); };
    private final Runnable caretTick = new Runnable() {
        @Override public void run() {
            if (caret != null && "add".equals(tab)) {
                caret.setAlpha(caret.getAlpha() > 0.5f ? 0f : 1f);
                handler.postDelayed(this, 520);
            }
        }
    };

    private String listType = "all", listFilter = "all", listSearch = "";
    private boolean searchOpen = false;
    private LocalDate listAnchor = LocalDate.now();
    private String reportKind = "month", reportType = "expense";
    private LocalDate reportAnchor = LocalDate.now();
    private TextView heroNumView;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        Ui.statusBar(this);
        db = new Db(this);
        dPay = db.meta("lastPay", "wechat");
        if (!isPay(dPay)) dPay = "wechat";
        loadData();
        if (!records.isEmpty()) db.writeAutoBackup(records, customCats);
        buildShell();
        setTab("add");
        handler.postDelayed(caretTick, 520);
        if (!db.hasWelcomed()) {
            if (records.isEmpty() && db.autoBackupCount() > 0) showAutoRestore();
            else if (records.isEmpty()) showWelcome();
            else db.setWelcomed(); // 有记录就不弹欢迎、不清空
        }
    }

    @Override protected void onStop() {
        super.onStop();
        if (!records.isEmpty()) db.writeAutoBackup(records, customCats);
    }

    @Override protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void loadData() {
        records = db.records();
        customCats = db.customCats();
    }

    private boolean isPay(String p) {
        for (String s : Core.PAY_IDS) if (s.equals(p)) return true;
        return false;
    }

    private void buildShell() {
        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(Ui.PAPER);
        header = Ui.column(this);
        content = Ui.column(this);
        content.setBackgroundColor(Ui.PAPER);
        tabbar = buildTabbar();
        root.addView(header);
        root.addView(content, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        root.addView(tabbar);
        setContentView(root);
    }

    private View spacer() {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1));
        return v;
    }

    private View hline() {
        View v = new View(this);
        v.setBackgroundColor(Ui.LINE);
        v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 1)));
        return v;
    }

    private View hlineSoft() {
        View v = new View(this);
        v.setBackgroundColor(Ui.LINE_SOFT);
        v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        return v;
    }

    private View vline() {
        View v = new View(this);
        v.setBackgroundColor(Ui.LINE);
        v.setLayoutParams(new LinearLayout.LayoutParams(1, ViewGroup.LayoutParams.MATCH_PARENT));
        return v;
    }

    private void pad(View v, int l, int t, int r, int b) {
        v.setPadding(Ui.dp(this, l), Ui.dp(this, t), Ui.dp(this, r), Ui.dp(this, b));
    }

    private void margins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) v.getLayoutParams()).setMargins(Ui.dp(this, l), Ui.dp(this, t), Ui.dp(this, r), Ui.dp(this, b));
        }
    }

    private TextView label(String s) {
        TextView t = Ui.text(this, s, 10.5f, Ui.INK3, Ui.sans());
        t.setLetterSpacing(0.14f);
        return t;
    }

    private ImageView icon(int res, int color, int size) {
        return Ui.icon(this, res, color, size);
    }

    private void buildHeader(String brand, String title, String sub, boolean topRow, boolean gear, boolean close) {
        header.removeAllViews();
        topTitle = null; topSub = null;
        LinearLayout line = Ui.row(this);
        pad(line, 18, 14, 18, 0);
        TextView b = Ui.text(this, brand, 11, Ui.INK2, Ui.sans());
        b.setLetterSpacing(0.18f);
        line.addView(b);
        line.addView(spacer());
        if (gear) {
            ImageView g = icon(R.drawable.ic_gear, Ui.INK2, 20);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.dp(this, 34), Ui.dp(this, 34));
            g.setLayoutParams(lp);
            g.setPadding(Ui.dp(this, 7), Ui.dp(this, 7), Ui.dp(this, 7), Ui.dp(this, 7));
            g.setOnClickListener(v -> openSettings());
            line.addView(g);
        }
        if (close) {
            ImageView x = icon(R.drawable.ic_close, Ui.INK2, 20);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.dp(this, 34), Ui.dp(this, 34));
            x.setLayoutParams(lp);
            x.setPadding(Ui.dp(this, 7), Ui.dp(this, 7), Ui.dp(this, 7), Ui.dp(this, 7));
            x.setOnClickListener(v -> setTab(settingsFrom));
            line.addView(x);
        }
        header.addView(line);
        if (topRow) {
            LinearLayout row = Ui.row(this);
            pad(row, 18, 15, 18, 0);
            TextView t = Ui.text(this, title, 25, Ui.INK, Ui.serif());
            t.setSingleLine(true);
            t.setEllipsize(TextUtils.TruncateAt.END);
            row.addView(t);
            row.addView(spacer());
            TextView s = Ui.text(this, sub, 11, Ui.INK3, Ui.sans());
            s.setGravity(Gravity.RIGHT);
            s.setSingleLine(true);
            row.addView(s);
            topTitle = t; topSub = s;
            header.addView(row);
        }
        View rule = hline();
        margins(rule, 18, 12, 18, 0);
        header.addView(rule);
    }

    private LinearLayout buildTabbar() {
        LinearLayout wrap = Ui.column(this);
        wrap.setBackgroundColor(Ui.PAPER);
        View top = new View(this);
        top.setBackgroundColor(Ui.INK);
        wrap.addView(top, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 1)));
        LinearLayout bar = Ui.row(this);
        bar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 54)));
        String[][] data = {{"add", "01", "记账", "pen"}, {"list", "02", "明细", "list"}, {"report", "03", "报告", "chart"}};
        for (int i = 0; i < data.length; i++) {
            final String key = data[i][0];
            FrameLayout cell = new FrameLayout(this);
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
            cell.setLayoutParams(clp);
            View line = new View(this);
            line.setBackgroundColor(Ui.ACCENT);
            FrameLayout.LayoutParams llp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 2));
            llp.gravity = Gravity.TOP;
            llp.leftMargin = Ui.dp(this, 12); llp.rightMargin = Ui.dp(this, 12);
            cell.addView(line, llp);
            LinearLayout inner = Ui.row(this);
            inner.setGravity(Gravity.CENTER);
            FrameLayout.LayoutParams ilp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            ilp.gravity = Gravity.CENTER;
            cell.addView(inner, ilp);
            ImageView iv = icon(iconForTab(data[i][3]), Ui.INK3, 17);
            inner.addView(iv);
            TextView idx = Ui.text(this, data[i][1], 10, Ui.INK3, Ui.mono());
            LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ip.leftMargin = Ui.dp(this, 6);
            inner.addView(idx, ip);
            TextView lab = Ui.text(this, data[i][2], 12, Ui.INK3, Ui.sans());
            LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp2.leftMargin = Ui.dp(this, 4);
            inner.addView(lab, lp2);
            cell.setOnClickListener(v -> setTab(key));
            bar.addView(cell);
            if (i < 2) {
                View sep = vline();
                bar.addView(sep, new LinearLayout.LayoutParams(1, ViewGroup.LayoutParams.MATCH_PARENT));
            }
            tabCells.add(cell); tabLines.add(line); tabIcons.add(iv); tabIndexes.add(idx); tabLabels.add(lab);
        }
        wrap.addView(bar);
        tabbar = wrap;
        return wrap;
    }

    private int iconForTab(String name) {
        if ("pen".equals(name)) return R.drawable.ic_pen;
        if ("list".equals(name)) return R.drawable.ic_list;
        return R.drawable.ic_chart;
    }

    private void updateTabbar() {
        String[] keys = {"add", "list", "report"};
        for (int i = 0; i < tabCells.size(); i++) {
            boolean on = keys[i].equals(tab);
            tabCells.get(i).setBackgroundColor(on ? Ui.ACCENT_SOFT : Color.TRANSPARENT);
            tabLines.get(i).setVisibility(on ? View.VISIBLE : View.INVISIBLE);
            tabIcons.get(i).setColorFilter(on ? Ui.ACCENT : Ui.INK3);
            tabIndexes.get(i).setTextColor(on ? Ui.ACCENT : Ui.INK3);
            tabLabels.get(i).setTextColor(on ? Ui.INK : Ui.INK3);
        }
    }

    private void setTab(String t) {
        if ("settings".equals(t)) settingsFrom = ("settings".equals(tab) ? settingsFrom : tab);
        tab = t;
        header.removeAllViews();
        content.removeAllViews();
        if ("add".equals(t)) {
            buildHeader("花在哪", Core.fmtDate(Core.parseISO(dDate), true), "", true, true, false);
            buildAdd();
        } else if ("list".equals(t)) {
            buildHeader("明细 / LEDGER", "", "", false, true, false);
            buildList();
        } else if ("report".equals(t)) {
            buildHeader("月结单 / STATEMENT", "", "", false, true, false);
            buildReport();
        } else {
            buildHeader("设置 / SETTINGS", "设置", "数据只在本机", true, false, true);
            buildSettings();
        }
        updateTabbar();
    }

    private void openSettings() {
        if ("settings".equals(tab)) return;
        settingsFrom = tab;
        setTab("settings");
    }

    /* ================= 记账 ================= */

    private void buildAdd() {
        content.removeAllViews();
        LinearLayout root = Ui.column(this);
        root.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        content.addView(root);

        ScrollView upper = new ScrollView(this);
        upper.setVerticalScrollBarEnabled(false);
        upper.setFillViewport(false);
        upper.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        LinearLayout top = Ui.column(this);
        upper.addView(top);
        root.addView(upper);

        LinearLayout head = Ui.row(this);
        pad(head, 18, 14, 18, 0);
        head.addView(label("金额 / AMOUNT"));
        head.addView(spacer());
        LinearLayout kinds = Ui.row(this);
        kinds.setBackground(Ui.stroke(this, Color.TRANSPARENT, Ui.LINE, 1, 0));
        kinds.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, Ui.dp(this, 26)));
        kindExpense = kindButton("支出", true);
        kindIncome = kindButton("收入", false);
        kinds.addView(kindExpense);
        View sep = vline(); sep.setBackgroundColor(Ui.LINE);
        kinds.addView(sep, new LinearLayout.LayoutParams(1, ViewGroup.LayoutParams.MATCH_PARENT));
        kinds.addView(kindIncome);
        head.addView(kinds);
        top.addView(head);
        kindExpense.setOnClickListener(v -> setKind("expense"));
        kindIncome.setOnClickListener(v -> setKind("income"));

        LinearLayout amountRow = Ui.row(this);
        pad(amountRow, 18, 6, 18, 0);
        amountRow.setGravity(Gravity.BOTTOM);
        TextView cur = Ui.text(this, "¥", 18, Ui.INK3, Ui.serif());
        amountRow.addView(cur);
        amountView = Ui.text(this, "0.00", 50, Ui.INK, Ui.serif());
        amountView.setPadding(Ui.dp(this, 4), 0, 0, 0);
        amountView.setSingleLine(true);
        amountRow.addView(amountView);
        caret = new View(this);
        caret.setBackgroundColor(Ui.ACCENT);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(Ui.dp(this, 2), Ui.dp(this, 30));
        clp.leftMargin = Ui.dp(this, 2);
        amountRow.addView(caret, clp);
        top.addView(amountRow);

        View rule = new View(this);
        rule.setBackgroundColor(Ui.ACCENT);
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 2));
        rlp.leftMargin = Ui.dp(this, 18); rlp.rightMargin = Ui.dp(this, 18); rlp.topMargin = Ui.dp(this, 8);
        rule.setLayoutParams(rlp);
        top.addView(rule);

        hintView = Ui.text(this, "", 10.5f, Ui.INK3, Ui.sans());
        pad(hintView, 18, 5, 18, 0);
        top.addView(hintView);

        LinearLayout catHead = Ui.row(this);
        pad(catHead, 18, 13, 18, 0);
        catLabelView = label("分类 / CATEGORY");
        catHead.addView(catLabelView);
        top.addView(catHead);

        LinearLayout catWrap = Ui.column(this);
        catWrap.setBackgroundColor(Ui.PAPER);
        View topLine = new View(this); topLine.setBackgroundColor(Ui.LINE);
        catWrap.addView(topLine, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 1)));
        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 130)));
        catGrid = new android.widget.GridLayout(this);
        catGrid.setColumnCount(5);
        catGrid.setPadding(Ui.dp(this, 14), Ui.dp(this, 5), Ui.dp(this, 14), 0);
        catGrid.setLayoutParams(new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        scroll.addView(catGrid);
        catWrap.addView(scroll);
        View bottomLine = new View(this); bottomLine.setBackgroundColor(Ui.LINE);
        catWrap.addView(bottomLine, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 1)));
        top.addView(catWrap);

        LinearLayout meta = Ui.row(this);
        pad(meta, 18, 12, 18, 0);
        meta.addView(metaCell("日期", true), new LinearLayout.LayoutParams(0, Ui.dp(this, 44), 0.9f));
        View gap1 = new View(this); gap1.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(this, 6), 1));
        meta.addView(gap1);
        meta.addView(metaCell("备注", false), new LinearLayout.LayoutParams(0, Ui.dp(this, 44), 1.3f));
        View gap2 = new View(this); gap2.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(this, 6), 1));
        meta.addView(gap2);
        meta.addView(metaCell("付款", false), new LinearLayout.LayoutParams(0, Ui.dp(this, 44), 0.9f));
        top.addView(meta);

        todayView = Ui.text(this, "", 11, Ui.INK3, Ui.sans());
        pad(todayView, 18, 9, 18, 0);
        top.addView(todayView);

        KeypadView keypad = new KeypadView(this);
        LinearLayout.LayoutParams klp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 200));
        klp.topMargin = Ui.dp(this, 8);
        keypad.setLayoutParams(klp);
        keypad.setListener(this::onKey);
        root.addView(keypad);

        saveView = Ui.text(this, "记下这笔", 15, Ui.PAPER, Ui.sans());
        saveView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 52));
        slp.leftMargin = Ui.dp(this, 18); slp.rightMargin = Ui.dp(this, 18);
        slp.topMargin = Ui.dp(this, 10); slp.bottomMargin = Ui.dp(this, 8);
        saveView.setLayoutParams(slp);
        saveView.setOnClickListener(v -> { if (undoRec != null) undoSave(); else saveRecord(); });
        root.addView(saveView);

        renderCats();
        renderMeta();
        updateKindLabels();
        updateAdd();
    }

    private LinearLayout kindButton(String text, boolean expense) {
        TextView t = Ui.text(this, text, 10.5f, Ui.INK3, Ui.sans());
        t.setGravity(Gravity.CENTER);
        LinearLayout wrap = Ui.row(this);
        wrap.setGravity(Gravity.CENTER);
        wrap.addView(t);
        wrap.setPadding(Ui.dp(this, 9), 0, Ui.dp(this, 9), 0);
        wrap.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        wrap.setTag(t);
        return wrap;
    }

    private void setKind(String kind) {
        if (kind.equals(dKind)) return;
        if (undoRec != null) clearUndo();
        dKind = kind;
        dCat = null;
        renderCats();
        updateKindLabels();
        updateAdd();
        haptic(saveView);
    }

    private void updateKindLabels() {
        boolean income = "income".equals(dKind);
        if (catLabelView != null) catLabelView.setText(income ? "来源 / SOURCE" : "分类 / CATEGORY");
        if (payLabelView != null) payLabelView.setText(income ? "收款" : "付款");
        if (hintView != null && !hintWarn) hintView.setText(income ? "输入金额，选个来源" : "输入金额，选个分类");
        TextView te = (TextView) kindExpense.getTag();
        TextView ti = (TextView) kindIncome.getTag();
        kindExpense.setBackgroundColor(income ? Color.TRANSPARENT : Ui.INK);
        kindIncome.setBackgroundColor(income ? Ui.ACCENT : Color.TRANSPARENT);
        te.setTextColor(income ? Ui.INK3 : Ui.PAPER);
        ti.setTextColor(income ? Ui.PAPER : Ui.INK3);
        if (undoRec == null) updateSave();
        updateAddHeader();
        updateToday();
    }

    private View metaCell(String labelText, boolean isDate) {
        LinearLayout cell = Ui.column(this);
        cell.setBackground(Ui.stroke(this, Color.TRANSPARENT, Ui.LINE, 1, 0));
        cell.setGravity(Gravity.CENTER_VERTICAL);
        pad(cell, 9, 0, 9, 0);
        TextView lab = Ui.text(this, labelText, 9.5f, Ui.INK3, Ui.sans());
        lab.setLetterSpacing(0.08f);
        TextView value = Ui.text(this, "", 12, Ui.INK, Ui.sans());
        value.setSingleLine(true);
        value.setEllipsize(TextUtils.TruncateAt.END);
        cell.addView(lab);
        cell.addView(value);
        if (isDate) { metaDate = value; cell.setOnClickListener(v -> pickDate()); }
        else if ("备注".equals(labelText)) { metaNote = value; cell.setOnClickListener(v -> pickNote()); }
        else { payLabelView = lab; metaPay = value; cell.setOnClickListener(v -> pickPay()); }
        return cell;
    }

    private void renderCats() {
        if (catGrid == null) return;
        catGrid.removeAllViews();
        List<Cat> cats = Core.catsFor(dKind, customCats);
        for (Cat c : cats) {
            boolean on = c.id.equals(dCat);
            LinearLayout cell = Ui.column(this);
            cell.setGravity(Gravity.CENTER);
            cell.setBackgroundColor(on ? Ui.ACCENT : Color.TRANSPARENT);
            ImageView iv = icon(iconForCat(c.icon), on ? Ui.PAPER : Ui.color(c.color), 22);
            cell.addView(iv);
            TextView name = Ui.text(this, c.name, 11.5f, on ? Ui.PAPER : Ui.INK2, Ui.sans());
            LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            nlp.topMargin = Ui.dp(this, 4);
            cell.addView(name, nlp);
            cell.setOnClickListener(v -> {
                if (undoRec != null) clearUndo();
                dCat = c.id;
                renderCats();
                updateSave();
                setHint(defaultHint(), false);
                haptic(v);
            });
            android.widget.GridLayout.LayoutParams glp = new android.widget.GridLayout.LayoutParams(
                    android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f),
                    android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f));
            glp.width = 0;
            glp.height = Ui.dp(this, 62);
            catGrid.addView(cell, glp);
        }
    }

    private int iconForCat(String name) {
        switch (name) {
            case "bowl": return R.drawable.ic_bowl;
            case "bus": return R.drawable.ic_bus;
            case "bag": return R.drawable.ic_bag;
            case "home": return R.drawable.ic_home;
            case "ticket": return R.drawable.ic_ticket;
            case "cross": return R.drawable.ic_cross;
            case "book": return R.drawable.ic_book;
            case "gift": return R.drawable.ic_gift;
            case "phone": return R.drawable.ic_phone;
            case "wallet": return R.drawable.ic_wallet;
            case "award": return R.drawable.ic_award;
            case "trend": return R.drawable.ic_trend;
            case "briefcase": return R.drawable.ic_briefcase;
            case "receipt": return R.drawable.ic_receipt;
            case "refund": return R.drawable.ic_refund;
            case "coins": return R.drawable.ic_coins;
            default: return R.drawable.ic_dots;
        }
    }

    private String defaultHint() {
        return "income".equals(dKind) ? "输入金额，选个来源" : "输入金额，选个分类";
    }

    private void onKey(String key) {
        if (undoRec != null) clearUndo();
        hintWarn = false;
        if ("back".equals(key)) {
            if (dRaw.length() <= 1) dRaw = "0";
            else dRaw = dRaw.substring(0, dRaw.length() - 1);
        } else if (".".equals(key)) {
            if (!dRaw.contains(".")) dRaw = (dRaw.isEmpty() ? "0" : dRaw) + ".";
        } else {
            int dot = dRaw.indexOf('.');
            if (dot >= 0) {
                if (dRaw.length() - dot > 2) return;
            } else if (dRaw.replace(".", "").length() >= 7 && !"0".equals(dRaw)) {
                return;
            }
            if ("0".equals(dRaw)) dRaw = key;
            else dRaw += key;
        }
        updateAdd();
        haptic(saveView);
    }

    private void updateAdd() {
        if (amountView == null) return;
        String s = dRaw.isEmpty() ? "0" : dRaw;
        if ("0".equals(s)) amountView.setText("0.00");
        else amountView.setText(s);
        int len = amountView.getText().length();
        amountView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, len > 9 ? 32 : len > 7 ? 40 : 50);
        if (hintView != null && !hintWarn) hintView.setText(defaultHint());
        updateSave();
        updateAddHeader();
        updateToday();
    }

    private void updateAddHeader() {
        if (topTitle != null) topTitle.setText(Core.fmtDate(Core.parseISO(dDate), true));
        if (topSub != null) {
            boolean income = "income".equals(dKind);
            topSub.setText((income ? "本月收入 ¥" : "本月已花 ¥") + Core.money(monthTotal(dKind)));
        }
    }

    private int monthTotal(String kind) {
        Period p = Core.period("month", LocalDate.now());
        Sum s = Core.summarize(filterKind(kind), p);
        return s.total;
    }

    private List<Rec> filterKind(String kind) {
        List<Rec> out = new ArrayList<>();
        for (Rec r : records) if (Core.kindOf(r).equals(kind)) out.add(r);
        return out;
    }

    private void updateSave() {
        if (saveView == null) return;
        if (undoRec != null) {
            saveView.setEnabled(true);
            saveView.setText("已记下 ¥" + Core.money(undoRec.amt) + " · 撤销");
            saveView.setBackground(Ui.bg(Ui.INK, 0));
            saveView.setTextColor(Ui.PAPER);
            return;
        }
        boolean ok = dCat != null && Core.toCents(dRaw) > 0;
        saveView.setEnabled(ok);
        saveView.setText("income".equals(dKind) ? "记下收入" : "记下这笔");
        saveView.setBackground(ok ? Ui.bg(Ui.ACCENT, 0) : Ui.stroke(this, Color.TRANSPARENT, Ui.LINE, 1, 0));
        saveView.setTextColor(ok ? Ui.PAPER : Ui.INK4);
    }

    private void renderMeta() {
        if (metaDate == null) return;
        LocalDate today = LocalDate.now();
        LocalDate d = Core.parseISO(dDate);
        if (d.equals(today)) metaDate.setText("今天");
        else if (d.equals(today.minusDays(1))) metaDate.setText("昨天");
        else metaDate.setText(Core.fmtDate(d, false));
        metaNote.setText(dNote == null || dNote.isEmpty() ? "无" : dNote);
        metaPay.setText(Core.payName(dPay));
    }

    private void setHint(String s, boolean warn) {
        hintWarn = warn;
        if (hintView != null) {
            hintView.setText(s);
            hintView.setTextColor(warn ? Ui.WARN : Ui.INK3);
        }
    }

    private void saveRecord() {
        if (saving) return;
        int cents = Core.toCents(dRaw);
        if (dCat == null || cents <= 0) {
            setHint(dCat == null ? ("income".equals(dKind) ? "先选一个来源" : "先选一个分类") : "金额要大于 0", true);
            haptic(saveView);
            return;
        }
        Rec r = new Rec("r" + System.currentTimeMillis(), cents, dCat, dKind, dDate, dPay,
                dNote == null ? "" : dNote.trim(), System.currentTimeMillis());
        saving = true;
        handler.postDelayed(() -> saving = false, 600);
        db.addRecord(r);
        db.setMeta("lastPay", dPay);
        records.add(r);
        db.writeAutoBackup(records, customCats);
        dRaw = "0";
        dNote = "";
        hintWarn = false;
        renderMeta();
        updateAdd();
        showUndo(r);
        haptic(saveView);
    }

    private void showUndo(Rec r) {
        undoRec = r;
        handler.removeCallbacks(undoTick);
        updateSave();
        handler.postDelayed(undoTick, 4200);
    }

    private void clearUndo() {
        handler.removeCallbacks(undoTick);
        undoRec = null;
        updateSave();
    }

    private void undoSave() {
        if (undoRec == null) return;
        Rec r = undoRec;
        handler.removeCallbacks(undoTick);
        undoRec = null;
        db.deleteRecord(r.id);
        for (int i = records.size() - 1; i >= 0; i--) if (records.get(i).id.equals(r.id)) records.remove(i);
        if (records.isEmpty()) db.deleteAutoBackup(); else db.writeAutoBackup(records, customCats);
        updateAdd();
        setHint("已撤销这一笔", false);
        handler.postDelayed(() -> { if ("已撤销这一笔".equals(hintView.getText().toString())) setHint(defaultHint(), false); }, 1800);
        haptic(saveView);
    }

    private void updateToday() {
        if (todayView == null) return;
        String kind = dKind;
        int n = 0, total = 0;
        for (Rec r : records) {
            if (Core.todayISO().equals(r.date) && Core.kindOf(r).equals(kind)) { n++; total += r.amt; }
        }
        if (n > 0) {
            todayView.setText("▪ 今天 " + n + " 笔" + ("income".equals(kind) ? "收入" : "") + " · ¥" + Core.money(total));
            todayView.setTextColor(Ui.INK3);
        } else {
            todayView.setText("▪ 今天还没有" + ("income".equals(kind) ? "收入" : "记录"));
            todayView.setTextColor(Ui.INK3);
        }
    }

    private void pickDate() {
        LocalDate d = Core.parseISO(dDate);
        DatePickerDialog dp = new DatePickerDialog(this, (view, y, m, day) -> {
            dDate = LocalDate.of(y, m + 1, day).toString();
            renderMeta();
            updateAdd();
        }, d.getYear(), d.getMonthValue() - 1, d.getDayOfMonth());
        dp.getDatePicker().setMaxDate(System.currentTimeMillis());
        dp.show();
    }

    private void pickNote() {
        EditText et = styledInput("这一笔" + ("income".equals(dKind) ? "收入是什么" : "花在什么上"), dNote);
        et.setHint("可选");
        LinearLayout wrap = Ui.column(this);
        wrap.setPadding(Ui.dp(this, 18), Ui.dp(this, 8), Ui.dp(this, 18), 0);
        TextView lab = label("备注");
        wrap.addView(lab);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 44));
        lp.topMargin = Ui.dp(this, 6);
        wrap.addView(et, lp);
        new AlertDialog.Builder(this, R.style.DialogTheme)
                .setTitle("备注")
                .setView(wrap)
                .setPositiveButton("确定", (d, w) -> { dNote = et.getText().toString().trim(); renderMeta(); })
                .setNegativeButton("取消", null)
                .show();
    }

    private void pickPay() {
        int checked = 0;
        for (int i = 0; i < Core.PAY_IDS.length; i++) if (Core.PAY_IDS[i].equals(dPay)) checked = i;
        new AlertDialog.Builder(this, R.style.DialogTheme)
                .setTitle("income".equals(dKind) ? "收款方式" : "支付方式")
                .setSingleChoiceItems(Core.PAY_NAMES, checked, (d, w) -> {
                    dPay = Core.PAY_IDS[w];
                    db.setMeta("lastPay", dPay);
                    renderMeta();
                    d.dismiss();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private EditText styledInput(String hint, String value) {
        EditText et = new EditText(this);
        et.setHint(hint);
        et.setText(value == null ? "" : value);
        et.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 14);
        et.setTextColor(Ui.INK);
        et.setHintTextColor(Ui.INK4);
        et.setBackground(Ui.stroke(this, Color.TRANSPARENT, Ui.LINE, 1, 0));
        et.setPadding(Ui.dp(this, 12), Ui.dp(this, 8), Ui.dp(this, 12), Ui.dp(this, 8));
        et.setSingleLine(true);
        return et;
    }

    /* ================= 明细 ================= */

    private void buildList() {
        content.removeAllViews();
        LinearLayout root = Ui.column(this);
        root.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        content.addView(root);
        Period p = Core.period("month", listAnchor);
        Sum exp = Core.summarize(filterKind("expense"), p);
        Sum inc = Core.summarize(filterKind("income"), p);
        LinearLayout mb = Ui.row(this);
        pad(mb, 14, 2, 14, 0);
        TextView prev = Ui.chevron(this, "‹");
        prev.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(this, 32), Ui.dp(this, 32)));
        prev.setOnClickListener(v -> { listAnchor = listAnchor.minusMonths(1); buildList(); });
        mb.addView(prev);
        LinearLayout center = Ui.column(this);
        center.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        TextView title = Ui.text(this, p.label, 22, Ui.INK, Ui.serif());
        TextView sub = Ui.text(this, "支出 ¥" + Core.money(exp.total) + " · 收入 ¥" + Core.money(inc.total), 10.5f, Ui.INK3, Ui.sans());
        center.addView(title); center.addView(sub);
        mb.addView(center);
        boolean current = YearMonth.from(listAnchor).equals(YearMonth.now());
        TextView next = Ui.chevron(this, "›");
        next.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(this, 32), Ui.dp(this, 32)));
        next.setAlpha(current ? 0.25f : 1f);
        next.setEnabled(!current);
        next.setOnClickListener(v -> { if (!current) { listAnchor = listAnchor.plusMonths(1); buildList(); } });
        mb.addView(next);
        ImageView search = icon(R.drawable.ic_search, Ui.INK2, 18);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(Ui.dp(this, 32), Ui.dp(this, 32));
        search.setPadding(Ui.dp(this, 7), Ui.dp(this, 7), Ui.dp(this, 7), Ui.dp(this, 7));
        search.setOnClickListener(v -> {
            searchOpen = !searchOpen;
            if (!searchOpen) listSearch = "";
            buildList();
        });
        mb.addView(search, slp);
        root.addView(mb);

        root.addView(hline());

        if (searchOpen) {
            EditText et = new EditText(this);
            et.setSingleLine(true);
            et.setHint("搜备注、分类、支付方式");
            et.setText(listSearch);
            et.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12.5f);
            et.setTextColor(Ui.INK);
            et.setHintTextColor(Ui.INK4);
            et.setBackground(Ui.stroke(this, Color.TRANSPARENT, Ui.LINE, 1, 0));
            et.setPadding(Ui.dp(this, 10), 0, Ui.dp(this, 10), 0);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 34));
            lp.leftMargin = Ui.dp(this, 18); lp.rightMargin = Ui.dp(this, 18); lp.topMargin = Ui.dp(this, 8);
            et.setLayoutParams(lp);
            et.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
                @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
                @Override public void afterTextChanged(Editable s) { listSearch = s.toString(); renderListRows(); }
            });
            root.addView(et);
            et.requestFocus();
            et.postDelayed(() -> {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
            }, 120);
        }

        LinearLayout type = Ui.row(this);
        type.setBackground(Ui.stroke(this, Color.TRANSPARENT, Ui.LINE, 1, 0));
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 26));
        tlp.leftMargin = Ui.dp(this, 18); tlp.rightMargin = Ui.dp(this, 18); tlp.topMargin = Ui.dp(this, 8);
        type.setLayoutParams(tlp);
        for (int i = 0; i < TYPE_IDS.length; i++) {
            final String id = TYPE_IDS[i];
            boolean on = listType.equals(id);
            TextView t = Ui.text(this, TYPE_NAMES[i], 11, on ? Ui.PAPER : Ui.INK3, Ui.sans());
            t.setGravity(Gravity.CENTER);
            t.setBackgroundColor(on ? Ui.INK : Color.TRANSPARENT);
            t.setOnClickListener(v -> { listType = id; listFilter = "all"; buildList(); });
            type.addView(t, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            if (i < 2) {
                View sep = vline(); sep.setBackgroundColor(Ui.LINE);
                type.addView(sep, new LinearLayout.LayoutParams(1, ViewGroup.LayoutParams.MATCH_PARENT));
            }
        }
        root.addView(type);

        HorizontalScrollView chipsScroll = new HorizontalScrollView(this);
        chipsScroll.setHorizontalScrollBarEnabled(false);
        chipsScroll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 38)));
        LinearLayout chips = Ui.row(this);
        chips.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        chips.setId(android.R.id.text1);
        chips.setPadding(Ui.dp(this, 18), Ui.dp(this, 8), Ui.dp(this, 18), 0);
        chipsScroll.addView(chips);
        root.addView(chipsScroll);
        renderChips(chips);

        ScrollView sc = new ScrollView(this);
        sc.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        sc.setFillViewport(true);
        LinearLayout list = Ui.column(this);
        list.setId(android.R.id.text2);
        sc.addView(list);
        root.addView(sc);
        renderListRows(list);
    }

    private void renderChips(LinearLayout chips) {
        chips.removeAllViews();
        Period p = Core.period("month", listAnchor);
        List<Rec> visible = new ArrayList<>();
        for (Rec r : records) {
            if (r.date.compareTo(p.start.toString()) < 0 || r.date.compareTo(p.end.toString()) > 0) continue;
            if (!"all".equals(listType) && !Core.kindOf(r).equals(listType)) continue;
            visible.add(r);
        }
        if (visible.isEmpty()) return;
        Sum s = Core.summarize(visible, p);
        List<Cat> allCats = new ArrayList<>(Core.EXPENSE);
        allCats.addAll(customCats);
        allCats.addAll(Core.INCOME);
        List<Rank> ranks = Core.ranking(s, allCats);
        addChip(chips, "all", "全部分类", ranks.isEmpty() ? Ui.INK3 : Ui.INK3);
        for (Rank r : ranks) addChip(chips, r.id, r.name, Ui.color(r.color));
    }

    private void addChip(LinearLayout chips, String id, String name, int color) {
        boolean on = id.equals(listFilter);
        LinearLayout cell = Ui.column(this);
        cell.setGravity(Gravity.CENTER);
        TextView t = Ui.text(this, name, 12, on ? Ui.INK : Ui.INK3, Ui.sans());
        cell.addView(t);
        View line = new View(this);
        line.setBackgroundColor(on ? Ui.ACCENT : Color.TRANSPARENT);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 2));
        llp.topMargin = Ui.dp(this, 5);
        cell.addView(line, llp);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.rightMargin = Ui.dp(this, 16);
        cell.setLayoutParams(lp);
        cell.setOnClickListener(v -> {
            listFilter = id;
            renderChips(chips);
            renderListRows((LinearLayout) content.findViewWithTag("listRows"));
        });
        chips.addView(cell);
    }

    private void renderListRows() {
        View v = content.findViewWithTag("listRows");
        if (v instanceof LinearLayout) renderListRows((LinearLayout) v);
    }

    private void renderListRows(LinearLayout list) {
        if (list == null) {
            list = content.findViewWithTag("listRows");
            if (!(list instanceof LinearLayout)) return;
        }
        list.setTag("listRows");
        list.removeAllViews();
        Period p = Core.period("month", listAnchor);
        List<Rec> month = new ArrayList<>();
        for (Rec r : records) {
            if (r.date.compareTo(p.start.toString()) >= 0 && r.date.compareTo(p.end.toString()) <= 0) month.add(r);
        }
        List<Rec> filtered = new ArrayList<>();
        String q = listSearch.trim().toLowerCase(Locale.US);
        for (Rec r : month) {
            if (!"all".equals(listType) && !Core.kindOf(r).equals(listType)) continue;
            if (!"all".equals(listFilter) && !r.cat.equals(listFilter)) continue;
            if (!q.isEmpty()) {
                Cat c = Core.catById(customCats, r.cat);
                String hay = ((r.note == null ? "" : r.note) + " " + c.name + " " + Core.payName(r.pay)).toLowerCase(Locale.US);
                if (!hay.contains(q)) continue;
            }
            filtered.add(r);
        }
        if (filtered.isEmpty()) {
            list.addView(emptyList());
            return;
        }
        Collections.sort(month, (a, b) -> a.date.equals(b.date) ? Long.compare(a.ts, b.ts) : a.date.compareTo(b.date));
        Map<String, String> index = new HashMap<>();
        for (int i = 0; i < month.size(); i++) index.put(month.get(i).id, String.format(Locale.US, "%02d", i + 1));
        Map<String, List<Rec>> groups = new java.util.TreeMap<>(Collections.reverseOrder());
        for (Rec r : filtered) {
            List<Rec> g = groups.get(r.date);
            if (g == null) { g = new ArrayList<>(); groups.put(r.date, g); }
            g.add(r);
        }
        for (Map.Entry<String, List<Rec>> e : groups.entrySet()) {
            List<Rec> rows = e.getValue();
            int de = 0, di = 0;
            for (Rec r : rows) if ("income".equals(Core.kindOf(r))) di += r.amt; else de += r.amt;
            LinearLayout head = Ui.row(this);
            pad(head, 18, 13, 18, 6);
            head.addView(Ui.text(this, dayLabel(e.getKey()), 11.5f, Ui.INK2, Ui.sans()));
            head.addView(spacer());
            String total = (de > 0 && di > 0) ? "支出 ¥" + Core.money(de) + " · 收入 ¥" + Core.money(di)
                    : "小计 ¥" + Core.money(de > 0 ? de : di);
            head.addView(Ui.text(this, total, 11, Ui.INK3, Ui.sans()));
            list.addView(head);
            list.addView(hline());
            for (Rec r : rows) {
                Cat c = Core.catById(customCats, r.cat);
                boolean income = "income".equals(Core.kindOf(r));
                LinearLayout row = Ui.row(this);
                row.setPadding(Ui.dp(this, 18), 0, Ui.dp(this, 18), 0);
                row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 38)));
                TextView idx = Ui.text(this, index.getOrDefault(r.id, ""), 10, Ui.INK4, Ui.mono());
                idx.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(this, 24), ViewGroup.LayoutParams.WRAP_CONTENT));
                row.addView(idx);
                TextView name = Ui.text(this, c.name, 12.5f, Ui.INK, Ui.sans());
                name.setSingleLine(true);
                name.setEllipsize(TextUtils.TruncateAt.END);
                row.addView(name, new LinearLayout.LayoutParams(Ui.dp(this, 38), ViewGroup.LayoutParams.WRAP_CONTENT));
                String bits = (r.note == null || r.note.isEmpty() ? "" : r.note + " · ") + Core.payName(r.pay)
                        + (timeLabel(r.ts).isEmpty() ? "" : " · " + timeLabel(r.ts));
                TextView note = Ui.text(this, bits, 11.5f, Ui.INK3, Ui.sans());
                note.setSingleLine(true);
                note.setEllipsize(TextUtils.TruncateAt.END);
                LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                row.addView(note, nlp);
                DottedView dot = new DottedView(this);
                LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(0, Ui.dp(this, 12), 1);
                dlp.leftMargin = Ui.dp(this, 8); dlp.rightMargin = Ui.dp(this, 8);
                row.addView(dot, dlp);
                TextView amt = Ui.text(this, (income ? "+" : "") + "¥" + Core.money(r.amt), 13.5f,
                        income ? Ui.ACCENT : Ui.INK, Ui.serif());
                amt.setGravity(Gravity.RIGHT);
                row.addView(amt, new LinearLayout.LayoutParams(Ui.dp(this, 74), ViewGroup.LayoutParams.WRAP_CONTENT));
                row.setOnClickListener(v -> openEdit(r));
                list.addView(row);
                list.addView(hlineSoft());
            }
        }
    }

    private View emptyList() {
        LinearLayout wrap = Ui.column(this);
        wrap.setGravity(Gravity.CENTER);
        pad(wrap, 30, 60, 30, 30);
        boolean noRecords = records.isEmpty();
        String title;
        String text;
        if (noRecords) { title = "还没有记过账"; text = "花销和收入都随手记一笔，之后日、周、月、年的账都会算好。"; }
        else if (!listSearch.isEmpty()) { title = "没有匹配的记录"; text = "换个关键词，或清空搜索。"; }
        else { title = "这个月还没有" + ("income".equals(listType) ? "收入" : "支出"); text = "换个月份看看，或者回「记账」记一笔。"; }
        TextView t = Ui.text(this, title, 15.5f, Ui.INK, Ui.serif());
        t.setGravity(Gravity.CENTER);
        wrap.addView(t);
        TextView d = Ui.text(this, text, 11.5f, Ui.INK3, Ui.sans());
        d.setGravity(Gravity.CENTER);
        d.setPadding(0, Ui.dp(this, 8), 0, Ui.dp(this, 18));
        wrap.addView(d);
        LinearLayout actions = Ui.row(this);
        actions.setGravity(Gravity.CENTER);
        TextView primary = Ui.text(this, noRecords ? "记一笔" : (!listSearch.isEmpty() ? "清空搜索" : "回到本月"),
                14, Ui.PAPER, Ui.sans());
        primary.setGravity(Gravity.CENTER);
        primary.setBackground(Ui.bg(Ui.ACCENT, 0));
        primary.setPadding(Ui.dp(this, 18), Ui.dp(this, 11), Ui.dp(this, 18), Ui.dp(this, 11));
        primary.setOnClickListener(v -> {
            if (noRecords) setTab("add");
            else if (!listSearch.isEmpty()) { listSearch = ""; buildList(); }
            else { listAnchor = LocalDate.now(); buildList(); }
        });
        actions.addView(primary);
        if (noRecords) {
            TextView sample = Ui.text(this, "看示例数据", 14, Ui.INK2, Ui.sans());
            sample.setGravity(Gravity.CENTER);
            sample.setBackground(Ui.stroke(this, Color.TRANSPARENT, Ui.LINE, 1, 0));
            sample.setPadding(Ui.dp(this, 18), Ui.dp(this, 11), Ui.dp(this, 18), Ui.dp(this, 11));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.leftMargin = Ui.dp(this, 10);
            sample.setLayoutParams(lp);
            sample.setOnClickListener(v -> loadSample());
            actions.addView(sample);
        }
        wrap.addView(actions);
        if (noRecords) {
            TextView restore = Ui.text(this, "从旧版备份恢复  →", 11.5f, Ui.ACCENT, Ui.sans());
            restore.setGravity(Gravity.CENTER);
            restore.setPadding(0, Ui.dp(this, 14), 0, 0);
            restore.setOnClickListener(v -> importJson());
            wrap.addView(restore);
        }
        return wrap;
    }

    private String dayLabel(String iso) {
        LocalDate d = Core.parseISO(iso);
        LocalDate today = LocalDate.now();
        if (d.equals(today)) return "今天";
        if (d.equals(today.minusDays(1))) return "昨天";
        return d.getMonthValue() + "月" + d.getDayOfMonth() + "日 " + Core.WEEKDAYS[(d.getDayOfWeek().getValue() + 6) % 7];
    }

    private String timeLabel(long ts) {
        if (ts <= 0) return "";
        return new SimpleDateFormat("HH:mm", Locale.US).format(new Date(ts));
    }

    /* ================= 编辑 ================= */

    private TextView editLabel(String text) {
        TextView t = label(text);
        t.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 5));
        return t;
    }

    private void openEdit(Rec rec) {
        final String kind = Core.kindOf(rec);
        final List<Cat> cats = Core.catsFor(kind, customCats);
        final String[] catId = {rec.cat};
        final String[] date = {rec.date};
        final String[] pay = {rec.pay};
        LinearLayout root = Ui.column(this);
        root.setPadding(Ui.dp(this, 18), Ui.dp(this, 8), Ui.dp(this, 18), 0);
        root.addView(editLabel("金额"));
        EditText amt = styledInput("0.00", Core.money(rec.amt));
        amt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        amt.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 24);
        amt.setTypeface(Ui.serif());
        root.addView(amt, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 48)));
        root.addView(editLabel(kind.equals("income") ? "来源" : "分类"));
        TextView catBtn = Ui.text(this, Core.catById(customCats, rec.cat).name + "  ›", 13, Ui.INK, Ui.sans());
        catBtn.setBackground(Ui.stroke(this, Color.TRANSPARENT, Ui.LINE, 1, 0));
        catBtn.setPadding(Ui.dp(this, 12), 0, Ui.dp(this, 12), 0);
        catBtn.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(catBtn, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 44)));
        root.addView(editLabel("日期"));
        TextView dateBtn = Ui.text(this, date[0], 13, Ui.INK, Ui.sans());
        dateBtn.setBackground(Ui.stroke(this, Color.TRANSPARENT, Ui.LINE, 1, 0));
        dateBtn.setPadding(Ui.dp(this, 12), 0, Ui.dp(this, 12), 0);
        dateBtn.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(dateBtn, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 44)));
        root.addView(editLabel("备注"));
        EditText note = styledInput("可选", rec.note);
        root.addView(note, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 44)));
        root.addView(editLabel(kind.equals("income") ? "收款方式" : "支付方式"));
        TextView payBtn = Ui.text(this, Core.payName(pay[0]) + "  ›", 13, Ui.INK, Ui.sans());
        payBtn.setBackground(Ui.stroke(this, Color.TRANSPARENT, Ui.LINE, 1, 0));
        payBtn.setPadding(Ui.dp(this, 12), 0, Ui.dp(this, 12), 0);
        payBtn.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(payBtn, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 44)));

        catBtn.setOnClickListener(v -> {
            String[] names = new String[cats.size()];
            int checked = 0;
            for (int i = 0; i < cats.size(); i++) {
                names[i] = cats.get(i).name;
                if (cats.get(i).id.equals(catId[0])) checked = i;
            }
            new AlertDialog.Builder(this, R.style.DialogTheme).setTitle("选择分类")
                    .setSingleChoiceItems(names, checked, (d, w) -> {
                        catId[0] = cats.get(w).id;
                        catBtn.setText(cats.get(w).name + "  ›");
                        d.dismiss();
                    }).show();
        });
        dateBtn.setOnClickListener(v -> {
            LocalDate d = Core.parseISO(date[0]);
            DatePickerDialog dp = new DatePickerDialog(this, (view, y, m, day) -> {
                date[0] = LocalDate.of(y, m + 1, day).toString();
                dateBtn.setText(date[0]);
            }, d.getYear(), d.getMonthValue() - 1, d.getDayOfMonth());
            dp.getDatePicker().setMaxDate(System.currentTimeMillis());
            dp.show();
        });
        payBtn.setOnClickListener(v -> {
            int checked = 0;
            for (int i = 0; i < Core.PAY_IDS.length; i++) if (Core.PAY_IDS[i].equals(pay[0])) checked = i;
            new AlertDialog.Builder(this, R.style.DialogTheme).setTitle("支付方式")
                    .setSingleChoiceItems(Core.PAY_NAMES, checked, (d, w) -> {
                        pay[0] = Core.PAY_IDS[w];
                        payBtn.setText(Core.PAY_NAMES[w] + "  ›");
                        d.dismiss();
                    }).show();
        });

        AlertDialog dlg = new AlertDialog.Builder(this, R.style.DialogTheme)
                .setTitle(kind.equals("income") ? "编辑收入" : "编辑记录")
                .setView(root)
                .setPositiveButton("保存", null)
                .setNegativeButton("删除", null)
                .setNeutralButton("取消", null)
                .create();
        dlg.setOnShowListener(d -> {
            dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                int cents = Core.toCents(amt.getText().toString());
                if (cents <= 0) { Ui.toast(this, "金额要大于 0"); return; }
                rec.amt = cents; rec.cat = catId[0]; rec.kind = kind; rec.date = date[0];
                rec.note = note.getText().toString().trim(); rec.pay = pay[0];
                db.updateRecord(rec);
                loadData();
                dlg.dismiss();
                refresh();
                Ui.toast(this, "已保存");
            });
            dlg.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
                new AlertDialog.Builder(this, R.style.DialogTheme)
                        .setTitle("删除这一笔？")
                        .setMessage("删除后无法恢复。")
                        .setPositiveButton("删除", (d2, w2) -> {
                            db.deleteRecord(rec.id);
                            loadData();
                            dlg.dismiss();
                            refresh();
                            Ui.toast(this, "已删除");
                        })
                        .setNegativeButton("取消", null).show();
            });
        });
        dlg.show();
    }

    /* ================= 报告 ================= */

    private void buildReport() {
        content.removeAllViews();
        Period p = Core.period(reportKind, reportAnchor);
        List<Rec> base = filterKind(reportType);
        Sum sum = Core.summarize(base, p);
        Period prevP = Core.period(reportKind, Core.shift(reportKind, reportAnchor, -1));
        List<Rec> prevBase = filterKind(reportType);
        Sum prev = Core.summarize(prevBase, prevP);
        boolean current = !LocalDate.now().isBefore(p.start) && !LocalDate.now().isAfter(p.end);
        int elapsed = Core.elapsedDays(p);
        if (current && !"day".equals(reportKind)) {
            LocalDate end = prevP.start.plusDays(elapsed - 1L);
            if (end.isAfter(prevP.end)) end = prevP.end;
            prev = Core.summarize(prevBase, new Period(prevP.kind, prevP.label, prevP.start, end));
        }
        Delta delta = Core.delta(sum.total, prev.total);
        List<Rank> rank = Core.ranking(sum, Core.catsFor(reportType, customCats));
        List<Rank> pays = Core.payRanking(sum);
        List<SeriesPoint> series = Core.series(reportKind, p, sum.byDay);
        boolean income = "income".equals(reportType);

        ScrollView sc = new ScrollView(this);
        sc.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        sc.setFillViewport(true);
        LinearLayout root = Ui.column(this);
        sc.addView(root);
        content.addView(sc);

        LinearLayout seg = Ui.row(this);
        seg.setBackground(Ui.bg(Ui.PAPER2, 0));
        LinearLayout.LayoutParams segLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 38));
        segLp.leftMargin = Ui.dp(this, 18); segLp.rightMargin = Ui.dp(this, 18); segLp.topMargin = Ui.dp(this, 10);
        seg.setPadding(Ui.dp(this, 3), Ui.dp(this, 3), Ui.dp(this, 3), Ui.dp(this, 3));
        for (int i = 0; i < REPORT_IDS.length; i++) {
            final String id = REPORT_IDS[i];
            boolean on = id.equals(reportKind);
            TextView t = Ui.text(this, REPORT_NAMES[i], 12, on ? Ui.INK : Ui.INK3, Ui.sans());
            t.setGravity(Gravity.CENTER);
            t.setBackground(on ? Ui.stroke(this, Ui.CARD, Ui.LINE, 1, 0) : Ui.bg(Color.TRANSPARENT, 0));
            t.setOnClickListener(v -> { reportKind = id; reportAnchor = LocalDate.now(); buildReport(); });
            seg.addView(t, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        }
        root.addView(seg);

        LinearLayout pnav = Ui.row(this);
        pad(pnav, 18, 9, 18, 0);
        TextView left = Ui.chevron(this, "‹");
        left.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(this, 32), Ui.dp(this, 32)));
        left.setOnClickListener(v -> { reportAnchor = Core.shift(reportKind, reportAnchor, -1); buildReport(); });
        pnav.addView(left);
        TextView pn = Ui.text(this, p.label, 17, Ui.INK, Ui.serif());
        pn.setGravity(Gravity.CENTER);
        pnav.addView(pn, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        if (!current) {
            TextView back = Ui.text(this, "回到" + ("day".equals(reportKind) ? "今天" : "本周"), 10, Ui.ACCENT, Ui.sans());
            back.setBackground(Ui.stroke(this, Color.TRANSPARENT, Ui.ACCENT, 1, 0));
            back.setPadding(Ui.dp(this, 6), Ui.dp(this, 2), Ui.dp(this, 6), Ui.dp(this, 2));
            back.setOnClickListener(v -> { reportAnchor = LocalDate.now(); buildReport(); });
            LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            blp.rightMargin = Ui.dp(this, 6);
            back.setLayoutParams(blp);
            pnav.addView(back);
        }
        boolean atCurrent = current;
        TextView right = Ui.chevron(this, "›");
        right.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(this, 32), Ui.dp(this, 32)));
        right.setAlpha(atCurrent ? 0.25f : 1f);
        right.setEnabled(!atCurrent);
        right.setOnClickListener(v -> { if (!atCurrent) { reportAnchor = Core.shift(reportKind, reportAnchor, 1); buildReport(); } });
        pnav.addView(right);
        root.addView(pnav);

        LinearLayout heroHead = Ui.row(this);
        pad(heroHead, 18, 12, 18, 0);
        heroHead.addView(label(income ? "总收入" : "总支出"));
        heroHead.addView(spacer());
        LinearLayout rtype = Ui.row(this);
        rtype.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        String[] rNames = {"支出", "收入"};
        String[] rIds = {"expense", "income"};
        for (int i = 0; i < 2; i++) {
            final String id = rIds[i];
            boolean on = id.equals(reportType);
            LinearLayout cell = Ui.column(this);
            cell.setGravity(Gravity.CENTER_HORIZONTAL);
            TextView t = Ui.text(this, rNames[i], 10.5f, on ? Ui.INK : Ui.INK3, Ui.sans());
            t.setLetterSpacing(0.04f);
            cell.addView(t);
            View l = new View(this);
            l.setBackgroundColor(on ? Ui.ACCENT : Color.TRANSPARENT);
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(Ui.dp(this, 24), Ui.dp(this, 2));
            llp.topMargin = Ui.dp(this, 4);
            cell.addView(l, llp);
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            clp.leftMargin = Ui.dp(this, i == 0 ? 0 : 12);
            cell.setLayoutParams(clp);
            cell.setOnClickListener(v -> { reportType = id; buildReport(); });
            rtype.addView(cell);
        }
        heroHead.addView(rtype);
        root.addView(heroHead);

        LinearLayout hero = Ui.row(this);
        hero.setGravity(Gravity.BOTTOM);
        pad(hero, 18, 2, 18, 0);
        hero.addView(Ui.text(this, "¥", 18, Ui.INK3, Ui.serif()));
        heroNumView = Ui.text(this, "0.00", 42, income ? Ui.ACCENT : Ui.INK, Ui.serif());
        heroNumView.setPadding(Ui.dp(this, 3), 0, 0, 0);
        heroNumView.setSingleLine(true);
        hero.addView(heroNumView);
        root.addView(hero);
        animateAmount(heroNumView, sum.total);

        LinearLayout meta = Ui.row(this);
        pad(meta, 18, 3, 18, 0);
        String avg = Core.money(sum.count == 0 ? 0 : sum.total / Math.max(1, elapsed));
        meta.addView(Ui.text(this, sum.count + " 笔  |  日均 ¥" + avg + ("day".equals(reportKind) ? "" : "（" + elapsed + " 天）"),
                11, Ui.INK3, Ui.sans()));
        meta.addView(spacer());
        if (current && !"day".equals(reportKind) && elapsed < 3) {
            // 月初不显示环比，避免误导
        } else if (prev.count == 0) {
            meta.addView(deltaChip(("income".equals(reportType) ? "" : "上月") + "无记录", false, Ui.INK3, Ui.PAPER2));
        } else if (Math.abs(delta.pct) < 0.5) {
            meta.addView(deltaChip("与上期持平", false, Ui.INK3, Ui.PAPER2));
        } else {
            boolean up = "up".equals(delta.dir);
            boolean good = income == up;
            meta.addView(deltaChip((up ? "↑ " : "↓ ") + Math.round(Math.abs(delta.pct)) + "% 比上期",
                    good, good ? Ui.ACCENT : Ui.WARN, good ? Ui.ACCENT_SOFT : Color.parseColor("#F0DDD5")));
        }
        root.addView(meta);

        LinearLayout kpi = Ui.row(this);
        LinearLayout.LayoutParams klp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        klp.leftMargin = Ui.dp(this, 18); klp.rightMargin = Ui.dp(this, 18); klp.topMargin = Ui.dp(this, 12);
        kpi.setLayoutParams(klp);
        kpi.setBackgroundColor(Ui.PAPER);
        View ktop = new View(this); ktop.setBackgroundColor(Ui.LINE);
        LinearLayout kpiWrap = Ui.column(this);
        kpiWrap.addView(ktop, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 1)));
        kpiWrap.addView(kpi);
        View kbottom = new View(this); kbottom.setBackgroundColor(Ui.LINE);
        kpiWrap.addView(kbottom, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 1)));
        root.addView(kpiWrap);

        String maxDayKey = null; int maxDay = 0;
        for (Map.Entry<String, Integer> e : sum.byDay.entrySet()) {
            if (e.getValue() > maxDay) { maxDay = e.getValue(); maxDayKey = e.getKey(); }
        }
        String maxCat = sum.max == null ? "—" : Core.catById(customCats, sum.max.cat).name;
        addKpi(kpi, "最大一笔", sum.max == null ? "—" : maxCat + " ¥" + Core.money(sum.max.amt));
        addKpi(kpi, "最高一天", maxDayKey == null ? "—" : shortDate(maxDayKey) + " ¥" + Core.money(maxDay));
        addKpi(kpi, "笔均", "¥" + Core.money(sum.count == 0 ? 0 : sum.total / sum.count));

        if (sum.count == 0) {
            LinearLayout empty = Ui.column(this);
            empty.setGravity(Gravity.CENTER);
            pad(empty, 30, 38, 30, 30);
            empty.addView(Ui.text(this, "这一期还没有" + (income ? "收入" : "支出"), 15, Ui.INK, Ui.serif()));
            TextView b = Ui.text(this, "去记一笔", 13, Ui.PAPER, Ui.sans());
            b.setGravity(Gravity.CENTER);
            b.setBackground(Ui.bg(Ui.ACCENT, 0));
            b.setPadding(Ui.dp(this, 18), Ui.dp(this, 10), Ui.dp(this, 18), Ui.dp(this, 10));
            b.setOnClickListener(v -> setTab("add"));
            LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            blp.topMargin = Ui.dp(this, 14);
            empty.addView(b, blp);
            root.addView(empty);
        } else {
            if (!"day".equals(reportKind)) {
                LinearLayout section = section(root, income ? "每日收入 / DAILY" : "每日支出 / DAILY",
                        "最高 " + (maxDayKey == null ? "—" : Core.fmtDate(Core.parseISO(maxDayKey), false)) + " ¥" + Core.money(maxDay));
                LineChartView chart = new LineChartView(this);
                chart.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 74)));
                chart.setData(series);
                section.addView(chart);
            }

            LinearLayout catSection = section(root, "分类 / BREAKDOWN", "占比 · 金额");
            for (int i = 0; i < rank.size(); i++) {
                Rank r = rank.get(i);
                LinearLayout row = Ui.row(this);
                row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 31)));
                TextView idx = Ui.text(this, String.format(Locale.US, "%02d", i + 1), 9.5f, Ui.INK4, Ui.mono());
                row.addView(idx, new LinearLayout.LayoutParams(Ui.dp(this, 20), ViewGroup.LayoutParams.WRAP_CONTENT));
                TextView name = Ui.text(this, r.name, 12, Ui.INK, Ui.sans());
                name.setSingleLine(true);
                name.setEllipsize(TextUtils.TruncateAt.END);
                row.addView(name, new LinearLayout.LayoutParams(Ui.dp(this, 38), ViewGroup.LayoutParams.WRAP_CONTENT));
                BarView bar = new BarView(this);
                LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(0, Ui.dp(this, 12), 1);
                blp.rightMargin = Ui.dp(this, 8);
                row.addView(bar, blp);
                bar.set((float) r.pct, Ui.color(r.color));
                TextView pct = Ui.text(this, Math.round(r.pct) + "%", 10, Ui.INK3, Ui.sans());
                pct.setGravity(Gravity.RIGHT);
                row.addView(pct, new LinearLayout.LayoutParams(Ui.dp(this, 32), ViewGroup.LayoutParams.WRAP_CONTENT));
                TextView amt = Ui.text(this, "¥" + Core.money(r.amt), 11.5f, Ui.INK, Ui.sans());
                amt.setGravity(Gravity.RIGHT);
                row.addView(amt, new LinearLayout.LayoutParams(Ui.dp(this, 64), ViewGroup.LayoutParams.WRAP_CONTENT));
                catSection.addView(row);
                catSection.addView(hlineSoft());
            }

            if (!pays.isEmpty()) {
                LinearLayout paySection = section(root, income ? "收款方式 / PAYMENT" : "支付方式 / PAYMENT", pays.size() + " 种");
                LinearLayout bar = Ui.row(this);
                bar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 4)));
                bar.setBackgroundColor(Ui.PAPER2);
                for (Rank r : pays) {
                    View pseg = new View(this);
                    pseg.setBackgroundColor(Ui.color(r.color));
                    bar.addView(pseg, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, (float) Math.max(0.1, r.pct)));
                }
                paySection.addView(bar);
                HorizontalScrollView hs = new HorizontalScrollView(this);
                hs.setHorizontalScrollBarEnabled(false);
                LinearLayout legend = Ui.row(this);
                legend.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                legend.setPadding(0, Ui.dp(this, 8), 0, 0);
                for (Rank r : pays) {
                    LinearLayout item = Ui.row(this);
                    View d = new View(this);
                    d.setBackground(Ui.bg(Ui.color(r.color), 4));
                    item.addView(d, new LinearLayout.LayoutParams(Ui.dp(this, 6), Ui.dp(this, 6)));
                    TextView t = Ui.text(this, r.name + " ¥" + Core.money(r.amt), 10.5f, Ui.INK3, Ui.sans());
                    LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    tlp.leftMargin = Ui.dp(this, 4);
                    item.addView(t, tlp);
                    LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    ilp.rightMargin = Ui.dp(this, 14);
                    item.setLayoutParams(ilp);
                    legend.addView(item);
                }
                hs.addView(legend);
                paySection.addView(hs);
            }

            if (!rank.isEmpty()) {
                Rank top = rank.get(0);
                StringBuilder sb = new StringBuilder();
                sb.append(top.name).append(" 占 ").append(Math.round(top.pct)).append("%，共 ¥").append(Core.money(top.amt))
                        .append("，是这一期最大的一类。");
                if (prev.count > 0) {
                    int prevTop = prev.byCat.getOrDefault(top.id, 0);
                    if (prevTop == 0) sb.append("上期没有").append(top.name).append(income ? "收入。" : "支出。");
                    else {
                        int diff = top.amt - prevTop;
                        if (diff == 0) sb.append(top.name).append("与上期持平。");
                        else sb.append(top.name).append("比上期").append(diff > 0 ? "多 " : "少 ")
                                .append("¥").append(Core.money(Math.abs(diff))).append("。");
                    }
                }
                if (sum.max != null) {
                    sb.append("\n最大的一笔：").append(Core.catById(customCats, sum.max.cat).name)
                            .append(" ¥").append(Core.money(sum.max.amt)).append("，")
                            .append(Core.fmtDate(Core.parseISO(sum.max.date), false)).append("。");
                }
                LinearLayout insight = Ui.row(this);
                LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                ilp.leftMargin = Ui.dp(this, 18); ilp.rightMargin = Ui.dp(this, 18); ilp.topMargin = Ui.dp(this, 14);
                insight.setLayoutParams(ilp);
                View accent = new View(this);
                accent.setBackgroundColor(Ui.ACCENT);
                insight.addView(accent, new LinearLayout.LayoutParams(Ui.dp(this, 2), ViewGroup.LayoutParams.MATCH_PARENT));
                TextView text = Ui.text(this, sb.toString(), 12, Ui.INK2, Ui.sans());
                text.setLineSpacing(0, 1.45f);
                text.setPadding(Ui.dp(this, 10), 0, 0, 0);
                insight.addView(text, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                root.addView(insight);
            }
        }

        TextView export = Ui.text(this, "↓  导出这一期 CSV", 11, Ui.INK3, Ui.sans());
        export.setGravity(Gravity.CENTER);
        export.setPaintFlags(export.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
        LinearLayout.LayoutParams elp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 46));
        elp.topMargin = Ui.dp(this, 12);
        export.setLayoutParams(elp);
        export.setOnClickListener(v -> exportPeriodCsv());
        root.addView(export);

        View bottom = new View(this);
        bottom.setLayoutParams(new LinearLayout.LayoutParams(1, Ui.dp(this, 16)));
        root.addView(bottom);
    }

    private LinearLayout section(LinearLayout root, String title, String sub) {
        LinearLayout section = Ui.column(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = Ui.dp(this, 18); lp.rightMargin = Ui.dp(this, 18); lp.topMargin = Ui.dp(this, 14);
        section.setLayoutParams(lp);
        View top = new View(this); top.setBackgroundColor(Ui.LINE);
        section.addView(top, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 1)));
        LinearLayout head = Ui.row(this);
        head.setPadding(0, Ui.dp(this, 9), 0, 0);
        head.addView(label(title));
        head.addView(spacer());
        if (sub != null) head.addView(Ui.text(this, sub, 10, Ui.INK3, Ui.sans()));
        section.addView(head);
        root.addView(section);
        return section;
    }

    private View deltaChip(String text, boolean good, int color, int bg) {
        TextView t = Ui.text(this, text, 10.5f, color, Ui.sans());
        t.setBackground(Ui.bg(bg, 0));
        t.setPadding(Ui.dp(this, 5), Ui.dp(this, 1), Ui.dp(this, 5), Ui.dp(this, 1));
        return t;
    }

    private void addKpi(LinearLayout row, String label, String value) {
        LinearLayout cell = Ui.column(this);
        cell.setPadding(Ui.dp(this, 8), Ui.dp(this, 8), Ui.dp(this, 8), Ui.dp(this, 9));
        TextView l = Ui.text(this, label, 9.5f, Ui.INK3, Ui.sans());
        l.setLetterSpacing(0.06f);
        TextView v = Ui.text(this, value, 11, Ui.INK, Ui.sans());
        v.setSingleLine(true);
        v.setEllipsize(TextUtils.TruncateAt.END);
        v.setPadding(0, Ui.dp(this, 4), 0, 0);
        cell.addView(l); cell.addView(v);
        row.addView(cell, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
    }

    private String shortDate(String iso) {
        LocalDate d = Core.parseISO(iso);
        return d.getMonthValue() + "." + d.getDayOfMonth();
    }

    private void animateAmount(TextView view, int target) {
        android.animation.ValueAnimator a = android.animation.ValueAnimator.ofInt(0, target);
        a.setDuration(460);
        a.addUpdateListener(an -> view.setText(Core.money((int) an.getAnimatedValue())));
        a.start();
    }

    private void exportPeriodCsv() {
        Period p = Core.period(reportKind, reportAnchor);
        List<Rec> list = new ArrayList<>();
        for (Rec r : filterKind(reportType)) {
            if (r.date.compareTo(p.start.toString()) >= 0 && r.date.compareTo(p.end.toString()) <= 0) list.add(r);
        }
        if (list.isEmpty()) { Ui.toast(this, "这一期没有记录"); return; }
        String suffix;
        if ("year".equals(reportKind)) suffix = p.start.toString().substring(0, 4);
        else if ("month".equals(reportKind)) suffix = p.start.toString().substring(0, 7);
        else if ("week".equals(reportKind)) suffix = p.start + "_" + p.end.toString().substring(5);
        else suffix = p.start.toString();
        String name = "花在哪-" + ("income".equals(reportType) ? "收入" : "支出") + "-" + suffix + "-" + new SimpleDateFormat("HHmmss", Locale.US).format(new Date()) + ".csv";
        if (writeDownload(name, "text/csv", Core.csv(list, customCats))) {
            markExport();
        }
    }

    /* ================= 设置 ================= */

    private void buildSettings() {
        content.removeAllViews();
        ScrollView sc = new ScrollView(this);
        sc.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        sc.setFillViewport(true);
        LinearLayout root = Ui.column(this);
        sc.addView(root);
        content.addView(sc);

        root.addView(groupLabel("数据"));
        LinearLayout data = card();
        int exp = 0, inc = 0;
        for (Rec r : records) if ("income".equals(Core.kindOf(r))) inc++; else exp++;
        data.addView(settingsRow(R.drawable.ic_book, "数据概况", "共 " + records.size() + " 笔 · 支出 " + exp + " · 收入 " + inc + " · " + storageSize(), false, v -> {
            Ui.toast(this, "共 " + records.size() + " 笔，约 " + storageSize());
        }));
        data.addView(settingsRow(R.drawable.ic_download, "导出 CSV", "Excel、Numbers 可直接打开，含日期、类型、分类、金额、支付方式、备注", false, v -> exportAllCsv()));
        data.addView(settingsRow(R.drawable.ic_download, "备份 JSON", "完整数据 + 自定义分类，换手机时用它恢复", false, v -> exportJson()));
        data.addView(settingsRow(R.drawable.ic_upload, "从备份恢复", "旧版「花在哪」的 .json 备份也在这里导入", false, v -> importJson()));
        data.addView(settingsRow(R.drawable.ic_check, "数据保护", "版本更新不会清空记录；升级前建议备份", false,
                v -> Ui.toast(this, "更新只做加法迁移，不删除本地记录")));
        data.addView(settingsRow(R.drawable.ic_check, "安全备份",
                "本地自动备份 " + db.autoBackupCount() + " 笔 · " + db.autoBackupTime(), false,
                v -> showSafetyBackupDialog()));
        String last = db.meta("lastExport", "");
        TextView hint = Ui.text(this, "上次导出：" + (last.isEmpty() ? "还没有导出过" : last) + "。建议每周导出一次。",
                10.5f, Ui.INK3, Ui.sans());
        hint.setPadding(Ui.dp(this, 18), Ui.dp(this, 10), Ui.dp(this, 18), Ui.dp(this, 12));
        hint.setLineSpacing(0, 1.4f);
        root.addView(data);
        root.addView(hint);

        root.addView(groupLabel("分类"));
        LinearLayout cats = card();
        cats.addView(settingsRow(R.drawable.ic_gift, "管理分类",
                "支出 " + (Core.EXPENSE.size() + customCats.size()) + " 类可自定义；收入 10 类固定", false,
                v -> openCategoryManager()));
        root.addView(cats);

        root.addView(groupLabel("关于"));
        LinearLayout about = card();
        about.addView(settingsRow(R.drawable.ic_dots, "关于花在哪", "v" + Core.VERSION + " · 不联网 · 不上传 · 无广告", false, v -> openAbout()));
        root.addView(about);

        root.addView(groupLabel("危险操作"));
        LinearLayout danger = card();
        danger.addView(settingsRow(R.drawable.ic_trash, "清空所有数据", "删除全部记录和自定义分类，不可恢复", true, v -> clearData()));
        root.addView(danger);

        View bottom = new View(this);
        bottom.setLayoutParams(new LinearLayout.LayoutParams(1, Ui.dp(this, 20)));
        root.addView(bottom);
    }

    private View groupLabel(String text) {
        TextView t = label(text);
        t.setPadding(Ui.dp(this, 18), Ui.dp(this, 16), Ui.dp(this, 18), Ui.dp(this, 7));
        return t;
    }

    private LinearLayout card() {
        LinearLayout l = Ui.column(this);
        l.setPadding(Ui.dp(this, 18), 0, Ui.dp(this, 18), 0);
        return l;
    }

    private View settingsRow(int iconRes, String title, String desc, boolean danger, View.OnClickListener click) {
        LinearLayout row = Ui.row(this);
        row.setPadding(0, Ui.dp(this, 12), 0, Ui.dp(this, 12));
        ImageView ic = icon(iconRes, danger ? Ui.WARN : Ui.INK2, 18);
        LinearLayout iconBox = Ui.row(this);
        iconBox.setGravity(Gravity.CENTER);
        iconBox.setBackground(Ui.bg(danger ? Color.parseColor("#F0DDD5") : Ui.PAPER2, 0));
        iconBox.addView(ic);
        row.addView(iconBox, new LinearLayout.LayoutParams(Ui.dp(this, 30), Ui.dp(this, 30)));
        LinearLayout main = Ui.column(this);
        LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        mlp.leftMargin = Ui.dp(this, 10);
        main.setLayoutParams(mlp);
        main.addView(Ui.text(this, title, 13, danger ? Ui.WARN : Ui.INK, Ui.sans()));
        TextView d = Ui.text(this, desc, 10.5f, Ui.INK3, Ui.sans());
        d.setLineSpacing(0, 1.35f);
        main.addView(d);
        row.addView(main);
        TextView chev = Ui.text(this, "›", 18, Ui.INK4, Ui.sans());
        row.addView(chev);
        row.setOnClickListener(click);
        LinearLayout wrap = Ui.column(this);
        wrap.addView(row);
        wrap.addView(hlineSoft());
        return wrap;
    }

    private String storageSize() {
        try {
            File f = new File(db.getReadableDatabase().getPath());
            long kb = Math.max(1, f.length() / 1024);
            return kb + " KB";
        } catch (Exception e) { return "—"; }
    }

    private String stamp() {
        return LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-"
                + new SimpleDateFormat("HHmmss", Locale.US).format(new Date());
    }

    private void exportAllCsv() {
        if (records.isEmpty()) { Ui.toast(this, "还没有记录可以导出"); return; }
        String name = "花在哪-明细-" + stamp() + ".csv";
        if (writeDownload(name, "text/csv", Core.csv(records, customCats))) markExport();
    }

    private void exportJson() {
        String name = "花在哪-备份-" + stamp() + ".json";
        String text = Core.backupJson(records, customCats, java.time.Instant.now().toString());
        if (writeDownload(name, "application/json", text)) markExport();
    }

    private boolean writeDownload(String name, String mime, String text) {
        try {
            ContentValues v = new ContentValues();
            v.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            v.put(MediaStore.MediaColumns.MIME_TYPE, mime);
            v.put(MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS + "/花在哪");
            Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v);
            if (uri == null) { Ui.toast(this, "导出失败，请重试"); return false; }
            OutputStream os = getContentResolver().openOutputStream(uri);
            if (os == null) { Ui.toast(this, "导出失败，请重试"); return false; }
            os.write(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            os.close();
            Ui.toast(this, "已导出到 下载/花在哪/" + name);
            return true;
        } catch (Exception e) {
            Ui.toast(this, "导出失败，请重试");
            return false;
        }
    }

    private void markExport() {
        db.setMeta("lastExport", LocalDate.now().toString());
    }

    private void importJson() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/json");
        i.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/json", "text/plain", "application/octet-stream"});
        startActivityForResult(i, REQ_IMPORT);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQ_IMPORT || resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        try {
            InputStream in = getContentResolver().openInputStream(uri);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) bout.write(buf, 0, n);
            in.close();
            ImportData imported = Core.parseBackup(bout.toString("UTF-8"));
            if (imported.records.isEmpty()) { Ui.toast(this, "这个备份里没有记录"); return; }
            new AlertDialog.Builder(this, R.style.DialogTheme)
                    .setTitle("恢复 " + imported.records.size() + " 笔记录？")
                    .setMessage("会用备份里的记录替换当前 " + records.size() + " 笔数据。建议先导出当前备份。")
                    .setPositiveButton("覆盖恢复", (d, w) -> {
                        db.replaceAll(imported.records, imported.cats);
                        db.setWelcomed();
                        loadData();
                        db.writeAutoBackup(records, customCats);
                        setTab("add");
                        Ui.toast(this, "已恢复 " + imported.records.size() + " 笔");
                    })
                    .setNegativeButton("取消", null)
                    .show();
        } catch (Exception e) {
            Ui.toast(this, e.getMessage() == null ? "文件读不出来" : e.getMessage());
        }
    }

    private void openAbout() {
        new AlertDialog.Builder(this, R.style.DialogTheme)
                .setTitle("关于花在哪")
                .setMessage("花在哪是一个只属于你的记账本。\n\n数据存在手机本地，没有账号、没有云同步、没有广告，也不会上传任何记录。\n\n版本 v" + Core.VERSION
                        + "\n包名 com.simple0926.huazaina.nativeapp\n网络：未申请网络权限")
                .setPositiveButton("知道了", null)
                .show();
    }

    private void clearData() {
        new AlertDialog.Builder(this, R.style.DialogTheme)
                .setTitle("清空所有数据？")
                .setMessage("全部 " + records.size() + " 笔记录和自定义分类都会被删除，无法恢复。建议先导出 JSON 备份。")
                .setPositiveButton("清空", (d, w) -> {
                    db.clearAll();
                    db.setWelcomed();
                    db.deleteAutoBackup();
                    loadData();
                    setTab("add");
                    Ui.toast(this, "已清空");
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void openCategoryManager() {
        LinearLayout root = Ui.column(this);
        root.setPadding(Ui.dp(this, 18), Ui.dp(this, 8), Ui.dp(this, 18), 0);
        TextView hint = Ui.text(this, "收入 10 类固定；下面是支出自定义分类。", 10.5f, Ui.INK3, Ui.sans());
        root.addView(hint);
        ScrollView sc = new ScrollView(this);
        LinearLayout list = Ui.column(this);
        sc.addView(list);
        root.addView(sc, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 240)));
        for (Cat c : customCats) {
            LinearLayout row = Ui.row(this);
            row.setPadding(0, Ui.dp(this, 10), 0, Ui.dp(this, 10));
            View dot = new View(this);
            dot.setBackground(Ui.bg(Ui.color(c.color), 6));
            row.addView(dot, new LinearLayout.LayoutParams(Ui.dp(this, 12), Ui.dp(this, 12)));
            TextView name = Ui.text(this, c.name, 13, Ui.INK, Ui.sans());
            LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            nlp.leftMargin = Ui.dp(this, 8);
            row.addView(name, nlp);
            TextView del = Ui.text(this, "删除", 11, Ui.WARN, Ui.sans());
            del.setOnClickListener(v -> {
                new AlertDialog.Builder(this, R.style.DialogTheme)
                        .setTitle("删除「" + c.name + "」？")
                        .setMessage("这个分类下的记录会归入「其他」。")
                        .setPositiveButton("删除", (d, w) -> {
                            db.deleteCustomCat(c.id);
                            loadData();
                            if (!records.isEmpty()) db.writeAutoBackup(records, customCats);
                            Ui.toast(this, "已删除");
                        })
                        .setNegativeButton("取消", null).show();
            });
            row.addView(del);
            list.addView(row);
            list.addView(hlineSoft());
        }
        if (customCats.isEmpty()) {
            list.addView(Ui.text(this, "还没有自定义分类。", 11.5f, Ui.INK4, Ui.sans()));
        }
        TextView add = Ui.text(this, "＋ 新增支出分类", 13, Ui.ACCENT, Ui.sans());
        add.setGravity(Gravity.CENTER);
        add.setBackground(Ui.stroke(this, Color.TRANSPARENT, Ui.ACCENT, 1, 0));
        LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 44));
        alp.topMargin = Ui.dp(this, 12);
        add.setLayoutParams(alp);
        root.addView(add);
        AlertDialog dlg = new AlertDialog.Builder(this, R.style.DialogTheme)
                .setTitle("管理分类")
                .setView(root)
                .setPositiveButton("关闭", null)
                .create();
        add.setOnClickListener(v -> { dlg.dismiss(); showAddCategory(); });
        dlg.show();
    }

    private void showAddCategory() {
        LinearLayout root = Ui.column(this);
        root.setPadding(Ui.dp(this, 18), Ui.dp(this, 8), Ui.dp(this, 18), 0);
        EditText name = styledInput("比如：宠物、健身、订阅", "");
        name.setSingleLine(true);
        root.addView(name, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 44)));
        TextView colorLabel = label("颜色");
        colorLabel.setPadding(0, Ui.dp(this, 12), 0, Ui.dp(this, 6));
        root.addView(colorLabel);
        LinearLayout swatches = Ui.row(this);
        final String[] selected = {CAT_COLORS[0]};
        for (String hex : CAT_COLORS) {
            View sw = new View(this);
            GradientDrawable g = Ui.bg(Ui.color(hex), 8);
            sw.setBackground(g);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.dp(this, 26), Ui.dp(this, 26));
            lp.rightMargin = Ui.dp(this, 8);
            sw.setLayoutParams(lp);
            if (hex.equals(selected[0])) sw.setAlpha(1f); else sw.setAlpha(0.55f);
            sw.setOnClickListener(v -> {
                selected[0] = hex;
                for (int i = 0; i < swatches.getChildCount(); i++) swatches.getChildAt(i).setAlpha(selected[0].equals(CAT_COLORS[i]) ? 1f : 0.55f);
            });
            swatches.addView(sw);
        }
        root.addView(swatches);
        new AlertDialog.Builder(this, R.style.DialogTheme)
                .setTitle("新增支出分类")
                .setView(root)
                .setPositiveButton("添加", (d, w) -> {
                    String n = name.getText().toString().trim();
                    if (n.isEmpty()) { Ui.toast(this, "先给分类起个名字"); return; }
                    if (n.length() > 6) n = n.substring(0, 6);
                    for (Cat c : Core.EXPENSE) if (c.name.equals(n)) { Ui.toast(this, "已经有同名分类"); return; }
                    for (Cat c : Core.INCOME) if (c.name.equals(n)) { Ui.toast(this, "已经有同名分类"); return; }
                    for (Cat c : customCats) if (c.name.equals(n)) { Ui.toast(this, "已经有同名分类"); return; }
                    Cat c = new Cat("c" + System.currentTimeMillis(), n, selected[0], "dots", true);
                    db.addCustomCat(c);
                    loadData();
                    if (!records.isEmpty()) db.writeAutoBackup(records, customCats);
                    Ui.toast(this, "已添加「" + n + "」");
                })
                .setNegativeButton("取消", null).show();
    }

    /* ================= 欢迎 / 示例 ================= */

    private void showWelcome() {
        Dialog d = new Dialog(this, R.style.DialogTheme);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(Ui.PAPER);
        root.setPadding(Ui.dp(this, 24), Ui.dp(this, 22), Ui.dp(this, 24), Ui.dp(this, 20));
        TextView kicker = label("花在哪 / PRIVATE LEDGER");
        root.addView(kicker);
        TextView title = Ui.text(this, "钱花在哪，\n你说了算。", 30, Ui.INK, Ui.serif());
        title.setLineSpacing(Ui.dp(this, 2), 1f);
        title.setPadding(0, Ui.dp(this, 18), 0, Ui.dp(this, 12));
        root.addView(title);
        TextView sub = Ui.text(this, "没有账号，没有云同步，没有会员费。\n每一笔只在本机，随时能导出。", 12, Ui.INK2, Ui.sans());
        sub.setLineSpacing(0, 1.5f);
        root.addView(sub);
        String[] bullets = {"3 秒记一笔", "看得懂的报告", "数据是你的"};
        String[] descs = {"金额、分类，支出收入都能记", "日 / 周 / 月 / 年，钱去了哪一目了然", "一键导出，不锁在别人的服务器里"};
        for (int i = 0; i < 3; i++) {
            LinearLayout row = Ui.row(this);
            row.setGravity(Gravity.TOP);
            row.setPadding(0, Ui.dp(this, 14), 0, 0);
            View dot = new View(this);
            dot.setBackgroundColor(Ui.ACCENT);
            LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(Ui.dp(this, 5), Ui.dp(this, 5));
            dlp.topMargin = Ui.dp(this, 5); dlp.rightMargin = Ui.dp(this, 8);
            row.addView(dot, dlp);
            LinearLayout col = Ui.column(this);
            col.addView(Ui.text(this, bullets[i], 12, Ui.INK, Ui.sans()));
            col.addView(Ui.text(this, descs[i], 11, Ui.INK3, Ui.sans()));
            row.addView(col);
            root.addView(row);
        }
        LinearLayout actions = Ui.row(this);
        actions.setPadding(0, Ui.dp(this, 22), 0, 0);
        TextView sample = Ui.text(this, "先看示例", 14, Ui.INK2, Ui.sans());
        sample.setGravity(Gravity.CENTER);
        sample.setBackground(Ui.stroke(this, Color.TRANSPARENT, Ui.LINE, 1, 0));
        sample.setPadding(0, Ui.dp(this, 11), 0, Ui.dp(this, 11));
        actions.addView(sample, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        TextView start = Ui.text(this, "开始记账", 14, Ui.PAPER, Ui.sans());
        start.setGravity(Gravity.CENTER);
        start.setBackground(Ui.bg(Ui.ACCENT, 0));
        start.setPadding(0, Ui.dp(this, 11), 0, Ui.dp(this, 11));
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        slp.leftMargin = Ui.dp(this, 10);
        actions.addView(start, slp);
        root.addView(actions);
        TextView restore = Ui.text(this, "从旧版备份恢复  →", 11.5f, Ui.ACCENT, Ui.sans());
        restore.setGravity(Gravity.CENTER);
        restore.setPadding(0, Ui.dp(this, 14), 0, 0);
        restore.setOnClickListener(v -> { d.dismiss(); importJson(); });
        root.addView(restore);
        d.setContentView(root);
        sample.setOnClickListener(v -> { d.dismiss(); loadSample(); });
        start.setOnClickListener(v -> { db.setWelcomed(); d.dismiss(); });
        d.setCancelable(false);
        d.show();
        Window w = d.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(Ui.bg(Ui.PAPER, 18));
            w.setLayout((int) (Ui.screenWidth(this) * 0.9f), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void loadSample() {
        List<Rec> sample = Core.sample(LocalDate.now());
        db.replaceAll(sample, new ArrayList<>());
        db.setWelcomed();
        loadData();
        db.writeAutoBackup(records, customCats);
        setTab("add");
        Ui.toast(this, "已载入示例数据");
    }


    private void showAutoRestore() {
        final ImportData saved = db.readAutoBackup();
        if (saved == null || saved.records.isEmpty()) { showWelcome(); return; }
        new AlertDialog.Builder(this, R.style.DialogTheme)
                .setTitle("发现本地安全备份")
                .setMessage("当前没有记录，但本地安全备份里有 " + saved.records.size()
                        + " 笔（" + db.autoBackupTime() + "）。\n\n可能是更新后数据没有载入。要恢复吗？")
                .setPositiveButton("恢复", (d, w) -> {
                    db.replaceAll(saved.records, saved.cats);
                    db.setWelcomed();
                    loadData();
                    db.writeAutoBackup(records, customCats);
                    setTab("add");
                    Ui.toast(this, "已从安全备份恢复 " + saved.records.size() + " 笔");
                })
                .setNegativeButton("先不用", (d, w) -> {
                    db.setWelcomed();
                    Ui.toast(this, "可在设置 → 安全备份里恢复");
                })
                .setCancelable(false)
                .show();
    }

    private void showSafetyBackupDialog() {
        final ImportData auto = db.readAutoBackup();
        final int n = auto == null ? 0 : auto.records.size();
        String time = db.autoBackupTime();
        String msg = "当前 " + records.size() + " 笔；本地安全备份 " + n + " 笔"
                + (time.isEmpty() ? "" : "（" + time + "）") + "。\n\n"
                + "版本更新不会清空记录；如果更新后为空，可从安全备份恢复。";
        new AlertDialog.Builder(this, R.style.DialogTheme)
                .setTitle("安全备份")
                .setMessage(msg)
                .setPositiveButton("立即备份", (d, w) -> {
                    db.writeAutoBackup(records, customCats);
                    Ui.toast(this, "已写入本地安全备份");
                })
                .setNeutralButton("恢复安全备份", (d, w) -> {
                    if (auto == null || auto.records.isEmpty()) {
                        Ui.toast(this, "还没有可恢复的安全备份");
                        return;
                    }
                    new AlertDialog.Builder(this, R.style.DialogTheme)
                            .setTitle("恢复安全备份？")
                            .setMessage("会用备份里的 " + n + " 笔替换当前 " + records.size() + " 笔。")
                            .setPositiveButton("恢复", (d2, w2) -> {
                                db.replaceAll(auto.records, auto.cats);
                                db.setWelcomed();
                                loadData();
                                db.writeAutoBackup(records, customCats);
                                setTab("add");
                                Ui.toast(this, "已恢复 " + n + " 笔");
                            })
                            .setNegativeButton("取消", null)
                            .show();
                })
                .setNegativeButton("关闭", null)
                .show();
    }

    private void refresh() {
        loadData();
        if (records.isEmpty()) db.deleteAutoBackup(); else db.writeAutoBackup(records, customCats);
        if ("add".equals(tab)) { renderCats(); renderMeta(); updateAdd(); }
        else if ("list".equals(tab)) buildList();
        else if ("report".equals(tab)) buildReport();
        else if ("settings".equals(tab)) buildSettings();
    }

    private void haptic(View v) { if (v != null) v.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP); }

    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if ("add".equals(tab) && event.getAction() == KeyEvent.ACTION_DOWN) {
            int c = event.getKeyCode();
            if (c >= KeyEvent.KEYCODE_0 && c <= KeyEvent.KEYCODE_9) {
                onKey(String.valueOf(c - KeyEvent.KEYCODE_0)); return true;
            }
            if (c == KeyEvent.KEYCODE_PERIOD || c == KeyEvent.KEYCODE_NUMPAD_DOT) { onKey("."); return true; }
            if (c == KeyEvent.KEYCODE_DEL) { onKey("back"); return true; }
            if (c == KeyEvent.KEYCODE_ENTER || c == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                if (saveView != null && saveView.isEnabled()) saveView.performClick();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override public void onBackPressed() {
        if ("settings".equals(tab)) { setTab(settingsFrom); return; }
        if (!"add".equals(tab)) { setTab("add"); return; }
        super.onBackPressed();
    }

    private void closeSettings() { setTab(settingsFrom); }

    /* helper unused */
}
