package james.medianotification.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import james.medianotification.R;
import james.medianotification.utils.ImageUtils;

public class AppIconView extends View {

    private Bitmap fgBitmap;
    private Bitmap bgBitmap;
    private Paint paint;
    private int size;
    private float rotation;
    private float fgScale, bgScale;

    private Handler handler;
    private Runnable runnable;

    public AppIconView(Context context) {
        this(context, null);
    }

    public AppIconView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppIconView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                rotation += 8;
                invalidate();
                handler.postDelayed(this, 20);
            }
        }, 20);

        ValueAnimator animator = ValueAnimator.ofFloat(0, 0.8f);
        animator.setInterpolator(new OvershootInterpolator());
        animator.setDuration(2000);
        animator.setStartDelay(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                fgScale = (float) animator.getAnimatedValue();
            }
        });
        animator.start();

        animator = ValueAnimator.ofFloat(0, 0.8f);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(2000);
        animator.setStartDelay(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                bgScale = (float) animator.getAnimatedValue();
            }
        });
        animator.start();
    }

    private Bitmap getRoundBitmap(@DrawableRes int drawable, int size) {
        Bitmap bitmap = ImageUtils.drawableToBitmap(ContextCompat.getDrawable(getContext(), drawable));
        bitmap = Bitmap.createBitmap(bitmap, bitmap.getWidth() / 6, bitmap.getHeight() / 6, (int) (0.666 * bitmap.getWidth()), (int) (0.666 * bitmap.getHeight()));
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, size, size);

        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);

        roundedBitmapDrawable.setCornerRadius(size / 2);
        roundedBitmapDrawable.setAntiAlias(true);

        return ImageUtils.drawableToBitmap(roundedBitmapDrawable);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int size = Math.min(canvas.getWidth(), canvas.getHeight());
        if (this.size != size || fgBitmap == null || bgBitmap == null) {
            this.size = size;
            fgBitmap = getRoundBitmap(R.mipmap.icon_foreground_web, size);
            bgBitmap = getRoundBitmap(R.mipmap.icon_background_web, size);
        }

        Matrix matrix = new Matrix();
        matrix.postTranslate(-bgBitmap.getWidth() / 2, -bgBitmap.getHeight() / 2);
        matrix.postRotate(rotation);
        matrix.postScale(bgScale, bgScale);
        matrix.postTranslate(bgBitmap.getWidth() / 2, bgBitmap.getHeight() / 2);
        canvas.drawBitmap(bgBitmap, matrix, paint);

        matrix = new Matrix();
        matrix.postTranslate(-fgBitmap.getWidth() / 2, -fgBitmap.getHeight() / 2);
        matrix.postScale(fgScale, fgScale);
        matrix.postTranslate(0, 0);
        matrix.postTranslate(fgBitmap.getWidth() / 2, fgBitmap.getHeight() / 2);
        canvas.drawBitmap(fgBitmap, matrix, paint);
    }
}
