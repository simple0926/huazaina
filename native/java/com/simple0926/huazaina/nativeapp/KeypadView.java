package com.simple0926.huazaina.nativeapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;

class KeypadView extends View {
    interface Listener { void onKey(String key); }
    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint back = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Listener listener;
    private final String[] keys = {"7", "8", "9", "4", "5", "6", "1", "2", "3", ".", "0", "back"};

    KeypadView(Context c) {
        super(c);
        line.setColor(Ui.LINE);
        line.setStrokeWidth(1f);
        text.setColor(Ui.INK);
        text.setTypeface(Typeface.create("serif", Typeface.NORMAL));
        text.setTextAlign(Paint.Align.CENTER);
        back.setColor(Ui.INK2);
        back.setStrokeWidth(Ui.dp(c, 1.6f));
        back.setStyle(Paint.Style.STROKE);
        back.setStrokeCap(Paint.Cap.ROUND);
        back.setStrokeJoin(Paint.Join.ROUND);
        setBackgroundColor(Ui.PAPER);
    }

    void setListener(Listener l) { listener = l; }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        int w = getWidth(), h = getHeight();
        for (int i = 1; i < 4; i++) c.drawLine(0, h * i / 4f, w, h * i / 4f, line);
        for (int i = 1; i < 3; i++) c.drawLine(w * i / 3f, 0, w * i / 3f, h, line);
        text.setTextSize(Ui.dp(getContext(), 23));
        for (int i = 0; i < keys.length; i++) {
            int row = i / 3, col = i % 3;
            float cx = w * (col + 0.5f) / 3f;
            float cy = h * (row + 0.5f) / 4f;
            if ("back".equals(keys[i])) {
                float bw = Ui.dp(getContext(), 11);
                float bh = Ui.dp(getContext(), 8);
                android.graphics.Path p = new android.graphics.Path();
                p.moveTo(cx - bw, cy);
                p.lineTo(cx - bw + Ui.dp(getContext(), 3), cy - bh / 2);
                p.lineTo(cx + bw, cy - bh / 2);
                p.lineTo(cx + bw, cy + bh / 2);
                p.lineTo(cx - bw + Ui.dp(getContext(), 3), cy + bh / 2);
                p.close();
                c.drawPath(p, back);
                c.drawLine(cx - bw + Ui.dp(getContext(), 4), cy, cx + Ui.dp(getContext(), 3), cy, back);
                c.drawLine(cx + Ui.dp(getContext(), 4), cy - Ui.dp(getContext(), 2.4f),
                        cx + Ui.dp(getContext(), 8), cy + Ui.dp(getContext(), 2.4f), back);
                c.drawLine(cx + Ui.dp(getContext(), 8), cy - Ui.dp(getContext(), 2.4f),
                        cx + Ui.dp(getContext(), 4), cy + Ui.dp(getContext(), 2.4f), back);
            } else {
                Paint.FontMetrics fm = text.getFontMetrics();
                float baseline = cy - (fm.ascent + fm.descent) / 2f;
                c.drawText(keys[i], cx, baseline, text);
            }
        }
        c.drawLine(0, h - 1, w, h - 1, line);
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            int row = (int) Math.min(3, Math.max(0, e.getY() / (getHeight() / 4f)));
            int col = (int) Math.min(2, Math.max(0, e.getX() / (getWidth() / 3f)));
            if (listener != null) listener.onKey(keys[row * 3 + col]);
            return true;
        }
        return true;
    }
}
