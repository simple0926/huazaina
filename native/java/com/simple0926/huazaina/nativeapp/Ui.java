package com.simple0926.huazaina.nativeapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

final class Ui {
    static final int PAPER = Color.parseColor("#F1EEE0");
    static final int PAPER2 = Color.parseColor("#E8E3D2");
    static final int PAPER3 = Color.parseColor("#DDD5C0");
    static final int CARD = Color.parseColor("#F7F5EB");
    static final int INK = Color.parseColor("#1A2018");
    static final int INK2 = Color.parseColor("#4D5247");
    static final int INK3 = Color.parseColor("#6A6A59");
    static final int INK4 = Color.parseColor("#8A8574");
    static final int ACCENT = Color.parseColor("#2F5A43");
    static final int ACCENT_SOFT = Color.parseColor("#E4EBE3");
    static final int WARN = Color.parseColor("#A3472F");
    static final int DANGER = Color.parseColor("#963A2A");
    static final int LINE = Color.parseColor("#2B1A2018");
    static final int LINE_SOFT = Color.parseColor("#1A1A2018");

    static int dp(Context c, float v) {
        return (int) (v * c.getResources().getDisplayMetrics().density + 0.5f);
    }

    static Typeface serif() { return Typeface.create("serif", Typeface.NORMAL); }
    static Typeface serifBold() { return Typeface.create("serif", Typeface.BOLD); }
    static Typeface sans() { return Typeface.SANS_SERIF; }
    static Typeface mono() { return Typeface.MONOSPACE; }

    static TextView text(Context c, String s, float size, int color) {
        return text(c, s, size, color, sans());
    }

    static TextView text(Context c, String s, float size, int color, Typeface tf) {
        TextView t = new TextView(c);
        t.setText(s == null ? "" : s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        t.setTextColor(color);
        t.setTypeface(tf);
        t.setIncludeFontPadding(false);
        return t;
    }

    static TextView label(Context c, String s) {
        TextView t = text(c, s, 10.5f, INK3, sans());
        t.setLetterSpacing(0.14f);
        return t;
    }

    static View rule(Context c) {
        View v = new View(c);
        v.setBackgroundColor(LINE);
        v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(c, 1)));
        return v;
    }

    static View ruleSoft(Context c) {
        View v = new View(c);
        v.setBackgroundColor(LINE_SOFT);
        v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        return v;
    }

    static LinearLayout row(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        l.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return l;
    }

    static LinearLayout column(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return l;
    }

    static void margins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) v.getLayoutParams()).setMargins(l, t, r, b);
        }
    }

    static void weight(View v, float w) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) v.getLayoutParams();
        lp.weight = w;
        v.setLayoutParams(lp);
    }

    static LinearLayout.LayoutParams lp(int w, int h) {
        return new LinearLayout.LayoutParams(w, h);
    }

    static LinearLayout.LayoutParams lp(int w, int h, float weight) {
        return new LinearLayout.LayoutParams(w, h, weight);
    }

    static GradientDrawable bg(int color, float radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radiusDp);
        return d;
    }

    static GradientDrawable bg(Context c, int color, float radiusDp) {
        return bg(color, dp(c, radiusDp));
    }

    static GradientDrawable stroke(Context c, int fill, int strokeColor, float widthDp, float radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(fill);
        d.setStroke(Math.max(1, dp(c, widthDp)), strokeColor);
        d.setCornerRadius(dp(c, radiusDp));
        return d;
    }

    static ImageView icon(Context c, int res, int tint, int sizeDp) {
        ImageView v = new ImageView(c);
        v.setImageResource(res);
        v.setColorFilter(tint);
        v.setScaleType(ImageView.ScaleType.FIT_CENTER);
        v.setLayoutParams(new LinearLayout.LayoutParams(dp(c, sizeDp), dp(c, sizeDp)));
        return v;
    }

    static int color(String hex) {
        try { return Color.parseColor(hex); } catch (Exception e) { return INK3; }
    }

    static void toast(Context c, String s) {
        Toast.makeText(c, s, Toast.LENGTH_SHORT).show();
    }

    static void haptic(View v) {
        v.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP);
    }

    static void statusBar(Activity a) {
        Window w = a.getWindow();
        w.setStatusBarColor(PAPER);
        w.setNavigationBarColor(PAPER);
        if (Build.VERSION.SDK_INT >= 29) {
            w.setStatusBarContrastEnforced(false);
            w.setNavigationBarContrastEnforced(false);
        }
        int flags = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        w.getDecorView().setSystemUiVisibility(flags);
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    }

    static int screenWidth(Context c) { return c.getResources().getDisplayMetrics().widthPixels; }
    static int screenHeight(Context c) { return c.getResources().getDisplayMetrics().heightPixels; }

    static TextView chevron(Context c, String s) {
        TextView t = text(c, s, 20, INK2, sans());
        t.setGravity(Gravity.CENTER);
        return t;
    }
}
