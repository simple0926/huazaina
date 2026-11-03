package com.simple0926.huazaina.nativeapp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

class Cat {
    String id, name, color, icon;
    boolean custom;
    Cat(String id, String name, String color, String icon) { this(id, name, color, icon, false); }
    Cat(String id, String name, String color, String icon, boolean custom) {
        this.id = id; this.name = name; this.color = color; this.icon = icon; this.custom = custom;
    }
}

class Rec {
    String id, cat, kind, date, pay, note;
    int amt;
    long ts;
    Rec() {}
    Rec(String id, int amt, String cat, String kind, String date, String pay, String note, long ts) {
        this.id = id; this.amt = amt; this.cat = cat; this.kind = kind; this.date = date;
        this.pay = pay; this.note = note; this.ts = ts;
    }
}

class Period {
    String kind, label;
    LocalDate start, end;
    Period(String kind, String label, LocalDate start, LocalDate end) {
        this.kind = kind; this.label = label; this.start = start; this.end = end;
    }
}

class Sum {
    List<Rec> list = new ArrayList<>();
    int total, count;
    Map<String, Integer> byCat = new LinkedHashMap<>();
    Map<String, Integer> byPay = new LinkedHashMap<>();
    Map<String, Integer> byDay = new LinkedHashMap<>();
    Rec max;
}

class Rank {
    String id, name, color, icon;
    int amt;
    double pct;
    Rank(String id, String name, String color, String icon, int amt, double pct) {
        this.id = id; this.name = name; this.color = color; this.icon = icon; this.amt = amt; this.pct = pct;
    }
}

class SeriesPoint {
    String key, label;
    int value;
    boolean show;
    SeriesPoint(String key, String label, int value, boolean show) {
        this.key = key; this.label = label; this.value = value; this.show = show;
    }
}

class Delta {
    double pct;
    String dir;
    Delta(double pct, String dir) { this.pct = pct; this.dir = dir; }
}

class ImportData {
    List<Rec> records = new ArrayList<>();
    List<Cat> cats = new ArrayList<>();
}

final class Core {
    static final String VERSION = "2.0.2";
    static final String[] WEEKDAYS = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
    static final String[] PAY_IDS = {"wechat", "alipay", "card", "cash", "other"};
    static final String[] PAY_NAMES = {"微信", "支付宝", "银行卡", "现金", "其他"};
    static final String[] PAY_COLORS = {"#557E5C", "#466E92", "#33302A", "#B08A52", "#857D6D"};

    static final List<Cat> EXPENSE = new ArrayList<>();
    static final List<Cat> INCOME = new ArrayList<>();
    static {
        EXPENSE.add(new Cat("food", "餐饮", "#9C6A2A", "bowl"));
        EXPENSE.add(new Cat("transit", "交通", "#3D6B57", "bus"));
        EXPENSE.add(new Cat("shop", "购物", "#9B4F58", "bag"));
        EXPENSE.add(new Cat("home", "居住", "#5D6B3E", "home"));
        EXPENSE.add(new Cat("fun", "娱乐", "#6A5A86", "ticket"));
        EXPENSE.add(new Cat("med", "医疗", "#9A3E35", "cross"));
        EXPENSE.add(new Cat("study", "学习", "#2F6B64", "book"));
        EXPENSE.add(new Cat("social", "人情", "#A97834", "gift"));
        EXPENSE.add(new Cat("phone", "通讯", "#4C5A63", "phone"));
        EXPENSE.add(new Cat("other", "其他", "#7A7263", "dots"));
        INCOME.add(new Cat("in_salary", "工资", "#2F5A43", "wallet"));
        INCOME.add(new Cat("in_bonus", "奖金", "#A97834", "award"));
        INCOME.add(new Cat("in_invest", "理财", "#3D6B57", "trend"));
        INCOME.add(new Cat("in_side", "兼职", "#6A5A86", "briefcase"));
        INCOME.add(new Cat("in_reimburse", "报销", "#4C5A63", "receipt"));
        INCOME.add(new Cat("in_gift", "礼金", "#9B4F58", "gift"));
        INCOME.add(new Cat("in_refund", "退款", "#5D6B3E", "refund"));
        INCOME.add(new Cat("in_rent", "租金", "#9C6A2A", "home"));
        INCOME.add(new Cat("in_dividend", "分红", "#2F6B64", "coins"));
        INCOME.add(new Cat("in_other", "其他", "#7A7263", "dots"));
    }

    static String todayISO() { return LocalDate.now().toString(); }

    static LocalDate parseISO(String iso) { return LocalDate.parse(iso); }

    static String fmtDate(LocalDate d, boolean weekday) {
        String s = d.getMonthValue() + "月" + d.getDayOfMonth() + "日";
        if (weekday) s += " " + WEEKDAYS[(d.getDayOfWeek().getValue() + 6) % 7];
        return s;
    }

    static String fmtShort(LocalDate d) { return d.getMonthValue() + "." + d.getDayOfMonth(); }

    static String money(int cents) {
        long c = Math.abs((long) cents);
        String s = String.format(Locale.US, "%,d.%02d", c / 100, c % 100);
        return (cents < 0 ? "-" : "") + s;
    }

    static int toCents(String text) {
        if (text == null) return 0;
        String t = text.replaceAll("[^0-9.]", "");
        if (t.isEmpty()) return 0;
        try {
            double v = Double.parseDouble(t);
            if (!Double.isFinite(v) || v <= 0) return 0;
            return (int) Math.round(v * 100);
        } catch (Exception e) { return 0; }
    }

    static boolean isIncomeCat(String id) { return id != null && id.startsWith("in_"); }

    static String kindOf(Rec r) {
        if (r == null) return "expense";
        if ("income".equals(r.kind) || "expense".equals(r.kind)) return r.kind;
        return isIncomeCat(r.cat) ? "income" : "expense";
    }

    static Cat catById(List<Cat> custom, String id) {
        for (Cat c : EXPENSE) if (c.id.equals(id)) return c;
        for (Cat c : INCOME) if (c.id.equals(id)) return c;
        if (custom != null) for (Cat c : custom) if (c.id.equals(id)) return c;
        return EXPENSE.get(EXPENSE.size() - 1);
    }

    static List<Cat> catsFor(String kind, List<Cat> custom) {
        if ("income".equals(kind)) return new ArrayList<>(INCOME);
        List<Cat> list = new ArrayList<>(EXPENSE);
        if (custom != null) list.addAll(custom);
        return list;
    }

    static String payName(String id) {
        for (int i = 0; i < PAY_IDS.length; i++) if (PAY_IDS[i].equals(id)) return PAY_NAMES[i];
        return "其他";
    }

    static String payColor(String id) {
        for (int i = 0; i < PAY_IDS.length; i++) if (PAY_IDS[i].equals(id)) return PAY_COLORS[i];
        return PAY_COLORS[4];
    }

    static Period period(String kind, LocalDate anchor) {
        LocalDate start, end;
        String label;
        if ("day".equals(kind)) {
            start = anchor; end = anchor; label = fmtDate(anchor, true);
        } else if ("week".equals(kind)) {
            start = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            end = start.plusDays(6);
            label = fmtShort(start) + " - " + fmtShort(end);
        } else if ("year".equals(kind)) {
            start = LocalDate.of(anchor.getYear(), 1, 1);
            end = LocalDate.of(anchor.getYear(), 12, 31);
            label = anchor.getYear() + "年";
        } else {
            YearMonth ym = YearMonth.from(anchor);
            start = ym.atDay(1); end = ym.atEndOfMonth();
            label = ym.getYear() + "年" + ym.getMonthValue() + "月";
        }
        return new Period(kind, label, start, end);
    }

    static LocalDate shift(String kind, LocalDate anchor, int n) {
        if ("day".equals(kind)) return anchor.plusDays(n);
        if ("week".equals(kind)) return anchor.plusWeeks(n);
        if ("year".equals(kind)) return anchor.plusYears(n);
        return anchor.plusMonths(n);
    }

    static int elapsedDays(Period p) {
        LocalDate today = LocalDate.now();
        if (today.isBefore(p.start)) return 1;
        LocalDate end = today.isBefore(p.end) ? today : p.end;
        return (int) (end.toEpochDay() - p.start.toEpochDay()) + 1;
    }

    static Sum summarize(List<Rec> records, Period p) {
        Sum s = new Sum();
        for (Rec r : records) {
            if (r.date.compareTo(p.start.toString()) < 0 || r.date.compareTo(p.end.toString()) > 0) continue;
            s.list.add(r);
            s.total += r.amt; s.count++;
            s.byCat.put(r.cat, s.byCat.getOrDefault(r.cat, 0) + r.amt);
            s.byPay.put(r.pay == null ? "other" : r.pay, s.byPay.getOrDefault(r.pay == null ? "other" : r.pay, 0) + r.amt);
            s.byDay.put(r.date, s.byDay.getOrDefault(r.date, 0) + r.amt);
            if (s.max == null || r.amt > s.max.amt) s.max = r;
        }
        return s;
    }

    static List<Rank> ranking(Sum s, List<Cat> cats) {
        List<Rank> out = new ArrayList<>();
        for (Map.Entry<String, Integer> e : s.byCat.entrySet()) {
            Cat c = catById(cats, e.getKey());
            out.add(new Rank(e.getKey(), c.name, c.color, c.icon, e.getValue(),
                    s.total == 0 ? 0 : e.getValue() * 100.0 / s.total));
        }
        Collections.sort(out, (a, b) -> Integer.compare(b.amt, a.amt));
        return out;
    }

    static List<Rank> payRanking(Sum s) {
        List<Rank> out = new ArrayList<>();
        for (Map.Entry<String, Integer> e : s.byPay.entrySet()) {
            out.add(new Rank(e.getKey(), payName(e.getKey()), payColor(e.getKey()), "card", e.getValue(),
                    s.total == 0 ? 0 : e.getValue() * 100.0 / s.total));
        }
        Collections.sort(out, (a, b) -> Integer.compare(b.amt, a.amt));
        return out;
    }

    static List<SeriesPoint> series(String kind, Period p, Map<String, Integer> byDay) {
        List<SeriesPoint> out = new ArrayList<>();
        if ("day".equals(kind)) return out;
        if ("week".equals(kind)) {
            for (int i = 0; i < 7; i++) {
                LocalDate d = p.start.plusDays(i);
                out.add(new SeriesPoint(d.toString(), WEEKDAYS[i], byDay.getOrDefault(d.toString(), 0), true));
            }
            return out;
        }
        if ("month".equals(kind)) {
            int n = p.start.lengthOfMonth();
            for (int i = 1; i <= n; i++) {
                LocalDate d = p.start.withDayOfMonth(i);
                out.add(new SeriesPoint(d.toString(), String.valueOf(i), byDay.getOrDefault(d.toString(), 0),
                        i == 1 || i == n || (i % 5 == 0 && i <= n - 2)));
            }
            return out;
        }
        for (int m = 1; m <= 12; m++) {
            int v = 0;
            for (Map.Entry<String, Integer> e : byDay.entrySet()) {
                if (e.getKey().startsWith(p.start.getYear() + "-" + String.format(Locale.US, "%02d", m))) {
                    v += e.getValue();
                }
            }
            out.add(new SeriesPoint(p.start.getYear() + "-" + String.format(Locale.US, "%02d", m), m + "月", v, true));
        }
        return out;
    }

    static Delta delta(int cur, int prev) {
        if (prev == 0) return new Delta(cur > 0 ? 100 : 0, cur > 0 ? "up" : "flat");
        double pct = (cur - prev) * 100.0 / prev;
        return new Delta(pct, pct > 0.5 ? "up" : pct < -0.5 ? "down" : "flat");
    }

    static String csv(List<Rec> records, List<Cat> custom) {
        List<Rec> sorted = new ArrayList<>(records);
        Collections.sort(sorted, (a, b) -> a.date.equals(b.date)
                ? Long.compare(a.ts, b.ts) : a.date.compareTo(b.date));
        StringBuilder sb = new StringBuilder();
        sb.append('\ufeff').append("日期,月份,类型,分类,金额,支付方式,备注\r\n");
        for (Rec r : sorted) {
            Cat c = catById(custom, r.cat);
            sb.append(r.date).append(',').append(r.date.substring(0, 7)).append(',')
              .append("income".equals(kindOf(r)) ? "收入" : "支出").append(',')
              .append(esc(c.name)).append(',').append(String.format(Locale.US, "%d.%02d", r.amt / 100, r.amt % 100)).append(',')
              .append(esc(payName(r.pay))).append(',').append(esc(r.note == null ? "" : r.note)).append("\r\n");
        }
        return sb.toString();
    }

    static String esc(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    static String backupJson(List<Rec> records, List<Cat> custom, String now) {
        try {
            JSONObject root = new JSONObject();
            root.put("app", "花在哪");
            root.put("version", VERSION);
            root.put("exportedAt", now);
            JSONArray arr = new JSONArray();
            for (Rec r : records) {
                JSONObject o = new JSONObject();
                o.put("id", r.id); o.put("amt", r.amt); o.put("cat", r.cat);
                o.put("kind", kindOf(r)); o.put("date", r.date); o.put("pay", r.pay);
                o.put("note", r.note == null ? "" : r.note); o.put("ts", r.ts);
                arr.put(o);
            }
            root.put("records", arr);
            JSONArray cats = new JSONArray();
            if (custom != null) for (Cat c : custom) {
                JSONObject o = new JSONObject();
                o.put("id", c.id); o.put("name", c.name); o.put("color", c.color);
                o.put("icon", c.icon); o.put("custom", true);
                cats.put(o);
            }
            root.put("cats", cats);
            return root.toString(2);
        } catch (Exception e) { return "{}"; }
    }

    static ImportData parseBackup(String text) {
        try {
            JSONObject root = new JSONObject(text);
            if (!root.has("records")) throw new IllegalArgumentException("不是「花在哪」的备份文件");
            JSONArray arr = root.getJSONArray("records");
            ImportData data = new ImportData();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                String date = o.optString("date", "");
                if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) continue;
                int amt = o.optInt("amt", 0);
                if (amt <= 0) continue;
                Rec r = new Rec();
                r.id = o.optString("id", "r" + i);
                r.amt = amt;
                r.cat = o.optString("cat", "other");
                r.kind = "income".equals(o.optString("kind", "")) || isIncomeCat(r.cat) ? "income" : "expense";
                r.date = date;
                r.pay = o.optString("pay", "other");
                r.note = o.optString("note", "");
                r.ts = o.optLong("ts", 0);
                data.records.add(r);
            }
            JSONArray cats = root.optJSONArray("cats");
            if (cats != null) for (int i = 0; i < cats.length(); i++) {
                JSONObject o = cats.getJSONObject(i);
                String id = o.optString("id", "");
                if (id.isEmpty() || !o.optBoolean("custom", false)) continue;
                String color = o.optString("color", "#7A7263");
                if (!color.matches("#[0-9a-fA-F]{6}")) color = "#7A7263";
                data.cats.add(new Cat(id, o.optString("name", "分类"), color,
                        o.optString("icon", "dots"), true));
            }
            return data;
        } catch (IllegalArgumentException e) { throw e; }
        catch (Exception e) { throw new IllegalArgumentException("文件读不出来"); }
    }

    static List<Rec> sample(LocalDate today) {
        Random rnd = new Random(20261102L);
        String[][] notes = {
                {"早饭 豆浆油条", "公司楼下简餐", "和朋友吃火锅", "咖啡", "外卖", "超市买菜"},
                {"地铁", "公交", "打车", "共享单车月卡"},
                {"洗衣液 + 纸巾", "一件外套", "耳机", "日用品"},
                {"水电燃气", "网费", "物业费"},
                {"电影票", "演出", "游戏", "周末爬山"},
                {"感冒药", "门诊挂号"},
                {"技术书", "网课", "订阅"},
                {"朋友生日礼物", "同事份子钱", "请客吃饭"},
                {"话费充值", "流量包"}
        };
        String[] cats = {"food", "transit", "shop", "home", "fun", "med", "study", "social", "phone"};
        String[] pays = {"wechat", "alipay", "alipay", "wechat", "card", "cash"};
        List<Rec> out = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            LocalDate d = today.minusDays(i);
            int n = 1 + rnd.nextInt(2);
            for (int k = 0; k < n; k++) {
                int ci = rnd.nextInt(cats.length);
                String cat = cats[ci];
                int amt;
                if ("home".equals(cat) && d.getDayOfMonth() == 1 && k == 0) {
                    amt = 320000;
                } else {
                    int bound = "home".equals(cat) ? 30000 : 16000;
                    amt = 1200 + rnd.nextInt(bound);
                }
                String note = notes[ci][rnd.nextInt(notes[ci].length)];
                Rec r = new Rec("s" + i + "_" + k, amt, cat, "expense", d.toString(),
                        pays[rnd.nextInt(pays.length)], note, System.currentTimeMillis() - i * 86400000L - k * 600000L);
                out.add(r);
            }
        }
        LocalDate m = today.withDayOfMonth(1);
        out.add(new Rec("s_salary", 800000, "in_salary", "income", m.toString(), "card", "月工资", System.currentTimeMillis() - 5000000L));
        out.add(new Rec("s_bonus", 120000, "in_bonus", "income", today.minusDays(6).toString(), "card", "季度奖金", System.currentTimeMillis() - 4000000L));
        out.add(new Rec("s_side", 35000, "in_side", "income", today.minusDays(12).toString(), "alipay", "稿费", System.currentTimeMillis() - 3000000L));
        Collections.sort(out, (a, b) -> a.date.equals(b.date) ? Long.compare(b.ts, a.ts) : b.date.compareTo(a.date));
        return out;
    }
}
