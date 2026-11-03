package com.simple0926.huazaina.nativeapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

class LineChartView extends View {
    private final Paint base = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint area = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<SeriesPoint> data = new ArrayList<>();

    LineChartView(Context c) {
        super(c);
        base.setColor(Ui.LINE);
        base.setStrokeWidth(1f);
        line.setColor(Ui.ACCENT);
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(Ui.dp(c, 1.3f));
        line.setStrokeJoin(Paint.Join.ROUND);
        area.setColor(Ui.ACCENT);
        area.setAlpha(18);
        area.setStyle(Paint.Style.FILL);
        dot.setColor(Ui.WARN);
        label.setColor(Ui.INK3);
        label.setTextSize(Ui.dp(c, 9));
        label.setTypeface(Ui.mono());
    }

    void setData(List<SeriesPoint> d) { data = d == null ? new ArrayList<>() : d; invalidate(); }

    @Override protected void onDraw(Canvas c) {
        int w = getWidth(), h = getHeight();
        if (data.isEmpty()) return;
        float left = Ui.dp(getContext(), 2);
        float right = w - Ui.dp(getContext(), 2);
        float top = Ui.dp(getContext(), 5);
        float bottom = h - Ui.dp(getContext(), 18);
        c.drawLine(left, bottom, right, bottom, base);
        int max = 1;
        int maxIdx = 0;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).value > max) { max = data.get(i).value; maxIdx = i; }
        }
        int n = data.size();
        float[] xs = new float[n], ys = new float[n];
        for (int i = 0; i < n; i++) {
            xs[i] = left + (right - left) * (n == 1 ? 0.5f : i / (float) (n - 1));
            ys[i] = bottom - (bottom - top) * (data.get(i).value / (float) max);
        }
        Path p = new Path();
        for (int i = 0; i < n; i++) {
            if (i == 0) p.moveTo(xs[i], ys[i]); else p.lineTo(xs[i], ys[i]);
        }
        Path fill = new Path(p);
        fill.lineTo(xs[n - 1], bottom);
        fill.lineTo(xs[0], bottom);
        fill.close();
        c.drawPath(fill, area);
        c.drawPath(p, line);
        c.drawCircle(xs[maxIdx], ys[maxIdx], Ui.dp(getContext(), 2.6f), dot);
        for (int i = 0; i < n; i++) {
            if (!data.get(i).show) continue;
            String s = data.get(i).label.replace("周", "");
            float tw = label.measureText(s);
            float x = Math.max(left, Math.min(right - tw, xs[i] - tw / 2));
            c.drawText(s, x, h - Ui.dp(getContext(), 4), label);
        }
    }
}
