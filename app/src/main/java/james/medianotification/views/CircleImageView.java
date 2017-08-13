package james.medianotification.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.ThumbnailUtils;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.AttributeSet;
import android.view.View;

import james.medianotification.utils.ImageUtils;

public class CircleImageView extends View {

    private Paint paint;
    private Bitmap bitmap;
    private int size;

    public CircleImageView(final Context context) {
        super(context);
        paint = new Paint();
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        paint = new Paint();
    }

    public void setImageBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (bitmap != null) {
            int size = Math.min(canvas.getWidth(), canvas.getHeight());
            if (size != this.size) {
                this.size = size;
                bitmap = ThumbnailUtils.extractThumbnail(bitmap, size, size);

                RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);

                roundedBitmapDrawable.setCornerRadius(size / 2);
                roundedBitmapDrawable.setAntiAlias(true);

                bitmap = ImageUtils.drawableToBitmap(roundedBitmapDrawable);
            }

            canvas.drawBitmap(bitmap, 0, 0, paint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size = getMeasuredHeight();
        setMeasuredDimension(size, size);
    }

}
