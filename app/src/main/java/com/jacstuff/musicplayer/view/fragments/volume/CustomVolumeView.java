package com.jacstuff.musicplayer.view.fragments.volume;

import android.annotation.SuppressLint;
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

    private int progressColor   = 0xFF4CAF50;

    private final float cornerRadius = 12f;

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
        int backgroundColor = 0xFF333333;
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStyle(Paint.Style.FILL);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(progressColor);
        progressPaint.setStyle(Paint.Style.FILL);

        thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int thumbColor = 0xFFFFFFFF;
        thumbPaint.setColor(thumbColor);
        thumbPaint.setStyle(Paint.Style.FILL);
    }


    public void setOnVolumeChangeListener(OnVolumeChangeListener listener) {
        this.listener = listener;
    }


    public void setVolume(int percentage) {
        progress = Math.clamp(percentage / 100f, 0f, 1f);
        invalidate();
    }


    public void setProgressColor(int progress){
        this.progressColor = progress;
        progressPaint.setColor(progressColor);
        invalidate();
    }


    @Override
    protected void onDraw(@androidx.annotation.NonNull Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        drawProgress(canvas);
        drawThumbLine(canvas);
    }

    private void drawBackground(Canvas canvas){
        bgRect.set(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
        canvas.drawRoundRect(bgRect, cornerRadius, cornerRadius, backgroundPaint);

    }


    private void drawProgress(Canvas canvas){
        float padL = getPaddingLeft();
        float padT = getPaddingTop();
        float padR = getPaddingRight();
        float padB = getPaddingBottom();

        float filledHeight = (getHeight() - padT - padB) * progress;
        float topOfProgress = getHeight() - padB - filledHeight;
        progressRect.set(padL, topOfProgress, getWidth() - padR, getHeight() - padB);
        canvas.drawRoundRect(progressRect, cornerRadius, cornerRadius, progressPaint);
    }

    private void drawThumbLine(Canvas canvas){
        float filledHeight = (getHeight() - getPaddingTop() - getPaddingBottom()) * progress;
        float topOfProgress = getHeight() - getPaddingBottom() - filledHeight;
        float thumbHeight = 8f;
        float thumbTop = topOfProgress - thumbHeight / 2f;
        canvas.drawRoundRect(
                getPaddingLeft(), thumbTop,
                getWidth() - getPaddingRight(), thumbTop + thumbHeight,
                cornerRadius, cornerRadius, thumbPaint);
    }

    // ---------- Touch handling ----------

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        float newProgress = convertToProgress(event.getY());

        return switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                if (Math.abs(newProgress - progress) > 0.001f) {
                    progress = newProgress;
                    invalidate();
                    if (listener != null) {
                        listener.onVolumeChanged(Math.round(progress * 100));
                    }
                }
                yield true;
            }
            case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> true;
            default -> super.onTouchEvent(event);
        };
    }


    private float convertToProgress(float y){
        float usableHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        float newProgress = 1f - (y - getPaddingTop()) / usableHeight;
        return Math.clamp(newProgress, 0f, 1f);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth  = (int) (48 * getResources().getDisplayMetrics().density);
        int desiredHeight = (int) (200 * getResources().getDisplayMetrics().density);

        int width  = resolveSize(desiredWidth, widthMeasureSpec);
        int height = resolveSize(desiredHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }
}