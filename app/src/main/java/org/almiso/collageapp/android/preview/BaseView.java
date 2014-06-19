package org.almiso.collageapp.android.preview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import org.almiso.collageapp.android.base.CollageApplication;

/**
 * Created by almiso on 10.06.2014.
 */
public abstract class BaseView<T extends BaseLoader> extends View implements ImageReceiver {

    private static final String TAG = "BaseView";
    private long arriveTime;
    private Drawable emptyDrawable;
    private ImageHolder holder;
    protected CollageApplication application;
    private T loader;

    private Rect rect = new Rect();
    private Rect rect1 = new Rect();
    private Paint avatarPaint = new Paint(Paint.FILTER_BITMAP_FLAG);

    private boolean isBinded = false;

    private boolean useFillMode = true;

    public BaseView(Context context) {
        super(context);
        application = (CollageApplication) context.getApplicationContext();
        loader = bindLoader();
    }

    public BaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        application = (CollageApplication) context.getApplicationContext();
        loader = bindLoader();

    }

    public BaseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        application = (CollageApplication) context.getApplicationContext();
        loader = bindLoader();

    }

    protected abstract T bindLoader();

    public Drawable getEmptyDrawable() {
        return emptyDrawable;
    }

    protected T getLoader() {
        return loader;
    }

    protected abstract void bind();

    public void setEmptyDrawable(Drawable emptyDrawable) {
        this.emptyDrawable = emptyDrawable;
    }

    public void setEmptyDrawable(int res) {
        this.emptyDrawable = getResources().getDrawable(res);
    }

    private void unbindAvatar() {
        if (holder != null) {
            holder.release();
            holder = null;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isBinded) {
            bind();
        }
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unbindAvatar();
        if (isBinded) {
            loader.cancelRequest(this);
            isBinded = false;
        }
        invalidate();
    }

    protected void requestBind() {
        unbindAvatar();
        bind();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (emptyDrawable != null) {
            emptyDrawable.setBounds(0, 0, getWidth(), getHeight());
            emptyDrawable.draw(canvas);
        }

        if (holder != null) {
            if (useFillMode) {
                float scale = Math.max(getWidth() / (float) holder.getW(), getHeight() / (float) holder.getH());
                rect1.set((int) ((getWidth() - scale * holder.getW()) / 2),
                        (int) ((getHeight() - scale * holder.getH()) / 2), (int) (scale * holder.getW()),
                        (int) (scale * holder.getH()));
            } else {
                rect1.set(0, 0, getWidth(), getHeight());
            }


            rect.set(0, 0, holder.getW(), holder.getH());
            canvas.drawBitmap(holder.getBitmap(), rect, rect1, avatarPaint);
        }
    }

    @Override
    public void onImageReceived(ImageHolder avatarHolder, boolean intermediate) {
        unbindAvatar();
        holder = avatarHolder;
        if (intermediate) {
            this.arriveTime = 0;
        } else {
            this.arriveTime = SystemClock.uptimeMillis();
        }
        invalidate();
    }
}