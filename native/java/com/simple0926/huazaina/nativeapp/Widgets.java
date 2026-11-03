package com.simple0926.huazaina.nativeapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

class DottedView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    DottedView(Context c) {
        super(c);
        paint.setColor(Ui.LINE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1f);
        paint.setPathEffect(new DashPathEffect(new float[]{Ui.dp(c, 1), Ui.dp(c, 3)}, 0));
    }
    @Override protected void onDraw(Canvas c) {
        float y = getHeight() / 2f;
        c.drawLine(0, y, getWidth(), y, paint);
    }
}

class BarView extends View {
    private final Paint track = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float pct;
    private int color = Ui.ACCENT;
    BarView(Context c) {
        super(c);
        track.setColor(Ui.LINE);
        track.setStrokeWidth(1f);
        fill.setColor(color);
        fill.setStrokeWidth(Ui.dp(c, 3));
        fill.setStrokeCap(Paint.Cap.BUTT);
    }
    void set(float pct, int color) {
        this.pct = Math.max(0, Math.min(100, pct));
        this.color = color;
        fill.setColor(color);
        invalidate();
    }
    @Override protected void onDraw(Canvas c) {
        float y = getHeight() / 2f;
        c.drawLine(0, y, getWidth(), y, track);
        c.drawLine(0, y, getWidth() * pct / 100f, y, fill);
    }
}
