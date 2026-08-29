package com.jacstuff.musicplayer.view.fragments.volume;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class CustomVolumeView extends View {

    public interface OnVolumeChangeListener {
        void onVolumeChanged(int percentage);
    }

    private Paint backgroundPaint;
    private Paint progressPaint;
    private Paint thumbPaint;

    private float progress = 0.5f;
    private OnVolumeChangeListener listener;

    private final RectF bgRect = new RectF();
    private final RectF progressRect = new RectF();

    // Colors – change these to match your theme
    private int backgroundColor = 0xFF333333;
    private int progressColor   = 0xFF4CAF50;   // the “different color”
    private int thumbColor      = 0xFFFFFFFF;

    private float cornerRadius = 12f;   // rounded corners
    private float thumbHeight  = 8f;    // thin horizontal line

    public CustomVolumeView(Context context) {
        super(context);
        init();
    }

    public CustomVolumeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomVolumeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStyle(Paint.Style.FILL);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(progressColor);
        progressPaint.setStyle(Paint.Style.FILL);

        thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        thumbPaint.setColor(thumbColor);
        thumbPaint.setStyle(Paint.Style.FILL);
    }

    // ---------- Public API ----------

    public void setOnVolumeChangeListener(OnVolumeChangeListener listener) {
        this.listener = listener;
    }

    /** Set volume programmatically (0 – 100) */
    public void setVolume(int percentage) {
        progress = Math.max(0f, Math.min(1f, percentage / 100f));
        invalidate();
    }

    public int getVolume() {
        return Math.round(progress * 100);
    }

    public void setColors(int background, int progress, int thumb) {
        backgroundColor = background;
        progressColor = progress;
        thumbColor = thumb;
        backgroundPaint.setColor(backgroundColor);
        progressPaint.setColor(progressColor);
        thumbPaint.setColor(thumbColor);
        invalidate();
    }

    // ---------- Drawing ----------

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width  = getWidth();
        int height = getHeight();
        float padL = getPaddingLeft();
        float padT = getPaddingTop();
        float padR = getPaddingRight();
        float padB = getPaddingBottom();

        // Background (full height)
        bgRect.set(padL, padT, width - padR, height - padB);
        canvas.drawRoundRect(bgRect, cornerRadius, cornerRadius, backgroundPaint);

        // Progress – everything BELOW the current progress point
        // progress = 1.0 → full height filled, progress = 0.0 → nothing filled
        float filledHeight = (height - padT - padB) * progress;
        float topOfProgress = height - padB - filledHeight;

        progressRect.set(padL, topOfProgress, width - padR, height - padB);
        canvas.drawRoundRect(progressRect, cornerRadius, cornerRadius, progressPaint);

        // Optional thin thumb line at the top of the filled area
        float thumbTop = topOfProgress - thumbHeight / 2f;
        canvas.drawRoundRect(
                padL, thumbTop,
                width - padR, thumbTop + thumbHeight,
                cornerRadius, cornerRadius, thumbPaint);
    }

    // ---------- Touch handling ----------

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) return false;

        float y = event.getY();
        int height = getHeight();
        float padT = getPaddingTop();
        float padB = getPaddingBottom();
        float usableHeight = height - padT - padB;

        // Convert touch Y → progress (0 at bottom, 1 at top)
        float newProgress = 1f - (y - padT) / usableHeight;
        newProgress = Math.max(0f, Math.min(1f, newProgress));

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(newProgress - progress) > 0.001f) {
                    progress = newProgress;
                    invalidate();
                    if (listener != null) {
                        listener.onVolumeChanged(Math.round(progress * 100));
                    }
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // final value already sent on MOVE
                return true;
        }
        return super.onTouchEvent(event);
    }

    // Make the view a bit wider than tall if needed (optional)
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Prefer a reasonable default size if wrap_content is used
        int desiredWidth  = (int) (48 * getResources().getDisplayMetrics().density);
        int desiredHeight = (int) (200 * getResources().getDisplayMetrics().density);

        int width  = resolveSize(desiredWidth, widthMeasureSpec);
        int height = resolveSize(desiredHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }
}