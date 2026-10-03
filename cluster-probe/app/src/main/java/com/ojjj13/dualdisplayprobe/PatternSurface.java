package com.ojjj13.dualdisplayprobe;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.Locale;

final class PatternSurface extends SurfaceView implements SurfaceHolder.Callback {
    volatile String status = "starting";
    private final String role;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private volatile boolean running;
    private Thread renderer;
    private long frame;

    PatternSurface(Context context, String role) {
        super(context); this.role = role; getHolder().addCallback(this);
    }
    @Override public void surfaceCreated(SurfaceHolder holder) {
        running = true;
        renderer = new Thread(() -> {
            while (running) {
                Canvas canvas = null;
                try {
                    canvas = holder.lockCanvas();
                    if (canvas != null) drawPattern(canvas);
                } catch (IllegalArgumentException | IllegalStateException ignored) {
                    // Surface can disappear during display migration.
                } finally {
                    if (canvas != null) {
                        try { holder.unlockCanvasAndPost(canvas); }
                        catch (IllegalArgumentException | IllegalStateException ignored) { }
                    }
                }
                SystemClock.sleep(33);
            }
        }, "pattern-" + role);
        renderer.start();
    }
    private void drawPattern(Canvas canvas) {
        float w = canvas.getWidth(), h = canvas.getHeight();
        float unit = Math.max(12f, Math.min(w / 32f, h / 12f));
        boolean main = role.equals("MAIN");
        canvas.drawColor(main ? Color.rgb(8, 32, 70) : Color.rgb(6, 54, 26));
        paint.setColor(Color.WHITE);
        paint.setTextSize(unit * 1.5f);
        canvas.drawText(role + " — independent surface", unit, unit * 2, paint);
        paint.setTextSize(unit * 0.8f);
        canvas.drawText(status, unit, unit * 3.5f, paint);
        canvas.drawText(String.format(Locale.US, "%dx%d | frame %d | uptime %.1fs",
            (int) w, (int) h, ++frame, SystemClock.elapsedRealtime() / 1000.0),
            unit, unit * 5, paint);
        paint.setColor(main ? Color.CYAN : Color.YELLOW);
        float x = unit + (w - 2 * unit) * ((SystemClock.elapsedRealtime() % 4000) / 4000f);
        canvas.drawCircle(x, h * 0.72f, unit, paint);
        paint.setStrokeWidth(3);
        canvas.drawLine(unit, h * 0.85f, w - unit, h * 0.85f, paint);
        paint.setTextSize(unit * 0.75f);
        canvas.drawText("Both displays must keep counting and moving", unit, h - unit, paint);
    }
    @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }
    @Override public void surfaceDestroyed(SurfaceHolder holder) {
        running = false;
        if (renderer != null) {
            boolean interrupted = false;
            while (renderer.isAlive()) {
                try { renderer.join(); } catch (InterruptedException e) { interrupted = true; }
            }
            renderer = null;
            if (interrupted) Thread.currentThread().interrupt();
        }
    }
}
