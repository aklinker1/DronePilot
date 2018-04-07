package com.klinker.android.drone_pilot;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class JoyStickView extends View implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    public static final int TYPE_STRAFE = 11;
    public static final int TYPE_ALTITUDE = 22;

    private JoyStickListener listener;
    private Paint paint;
    private RectF temp;
    private GestureDetector gestureDetector;
    private int type = TYPE_STRAFE;
    private float centerX;
    private float centerY;
    private float posX;
    private float posY;
    private float backRadius;
    private float joystickRadius;
    private int backColor;
    private int joystickColor;

    //Button Size percentage of the minimum(width, height)
    private int percentage = 25;

    public interface JoyStickListener {
        void onMove(JoyStickView joyStick, double x, double y);
        void onTap();
        void onDoubleTap();
    }

    public JoyStickView(Context context) {
        this(context, null);
    }

    public JoyStickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);

        temp = new RectF();

        gestureDetector = new GestureDetector(context, this);
        gestureDetector.setIsLongpressEnabled(false);
        gestureDetector.setOnDoubleTapListener(this);

        backColor = Color.WHITE;
        joystickColor = Color.RED;

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.JoyStickView);
            if (typedArray != null) {
                backColor = typedArray.getColor(R.styleable.JoyStickView_backColor, Color.WHITE);
                joystickColor = typedArray.getColor(R.styleable.JoyStickView_joyStickColor, Color.RED);
                type = typedArray.getInt(R.styleable.JoyStickView_type, TYPE_STRAFE);
                if (percentage > 50) percentage = 50;
                if (percentage < 25) percentage = 25;

                typedArray.recycle();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float width = MeasureSpec.getSize(widthMeasureSpec);
        float height = MeasureSpec.getSize(heightMeasureSpec);
        centerX = width / 2;
        centerY = height / 2;
        float min = Math.min(width, height);
        posX = centerX;
        posY = centerY;
        joystickRadius = (min / 2f * (percentage / 100f));
        backRadius = (min / 2f * ((100f - percentage) / 100f));
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (canvas == null) return;

        paint.setColor(backColor);
        if (type == TYPE_STRAFE) {
            canvas.drawCircle(centerX, centerY, backRadius, paint);
        } else {
            canvas.drawRect(
                    centerX - backRadius,
                    centerY - backRadius,
                    centerX + backRadius,
                    centerY + backRadius,
                    paint
            );
        }

        paint.setColor(joystickColor);
        canvas.drawCircle(posX, posY, joystickRadius, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                posX = event.getX();
                posY = event.getY();

                if (type == TYPE_STRAFE) {
                    float abs = (float) Math.sqrt((posX - centerX) * (posX - centerX)
                            + (posY - centerY) * (posY - centerY));
                    if (abs > backRadius) {
                        posX = ((posX - centerX) * backRadius / abs + centerX);
                        posY = ((posY - centerY) * backRadius / abs + centerY);
                    }
                } else {
                    if (posX < centerX - backRadius) posX = centerX - backRadius;
                    else if (posX > centerX + backRadius) posX = centerX + backRadius;
                    if (posY < centerY - backRadius) posY = centerY - backRadius;
                    else if (posY > centerY + backRadius) posY = centerY + backRadius;
                }

                invalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                posX = centerX;
                if (type == TYPE_STRAFE) posY = centerY;
                invalidate();
                break;
        }

        if (listener != null) {
            listener.onMove(this, posX, posY);
        }
        return true;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {}

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {}

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        if (listener != null) listener.onTap();
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        if (listener != null) listener.onDoubleTap();
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return false;
    }

    public void setListener(JoyStickListener listener) {
        this.listener = listener;
    }

    public int getType() {
        return type;
    }

    //Customization ----------------------------------------------------------------

    //size of button is a percentage of the minimum(width, height)
    //percentage must be between 25 - 50
    public void setButtonRadiusScale(int scale) {
        percentage = scale;
        if (percentage > 50) percentage = 50;
        if (percentage < 25) percentage = 25;
    }

    public void setType(int type) {
        this.type = type;
    }
}