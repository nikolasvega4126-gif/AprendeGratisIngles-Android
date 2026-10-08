package com.aprendegratisingles.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.View;

/** Purely decorative trail; all touch targets are separate native Android views. */
public final class GameTrailView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path trail = new Path();
    private final float density;
    public GameTrailView(Context context) {
        super(context);
        density = getResources().getDisplayMetrics().density;
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }
    private float px(float d) { return d * density; }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth(), h = getHeight();
        float[] xs = {0.51f, 0.53f, 0.48f, 0.54f, 0.49f, 0.52f};
        float[] ys = {0.10f, 0.245f, 0.39f, 0.535f, 0.68f, 0.83f};
        // Night-blue haze intentionally separates the functional pathway from the illustrated background.
        for (int i=0; i<ys.length; i++) {
            float x=xs[i]*w, y=ys[i]*h;
            float radius = (i == 0 ? w*.17f : w*.125f);
            paint.reset(); paint.setAntiAlias(true);
            paint.setShader(new RadialGradient(x,y,radius,
                    new int[]{Color.argb(155,5,45,145), Color.argb(80,7,69,167),Color.TRANSPARENT},
                    null,Shader.TileMode.CLAMP));
            canvas.drawCircle(x,y,radius,paint);
        }
        trail.reset();
        trail.moveTo(xs[0]*w,ys[0]*h);
        for (int i=1;i<xs.length;i++) {
            float mid = (ys[i-1]+ys[i])*.5f*h;
            trail.cubicTo(xs[i-1]*w,mid,xs[i]*w,mid,xs[i]*w,ys[i]*h);
        }
        paint.reset(); paint.setAntiAlias(true);
        paint.setColor(Color.argb(110,4,240,255));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(px(15)); paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setShadowLayer(px(11),0,0,0xff00dbff);
        canvas.drawPath(trail,paint);
        paint.clearShadowLayer();
        paint.setColor(Color.WHITE); paint.setStrokeWidth(px(5));
        paint.setPathEffect(new DashPathEffect(new float[]{px(2),px(12)},0));
        canvas.drawPath(trail,paint);
        paint.setPathEffect(null);
        for (int i=0;i<xs.length;i++) {
            float x=xs[i]*w,y=ys[i]*h;
            paint.setStyle(Paint.Style.FILL); paint.setColor(Color.rgb(125,250,255));
            paint.setShadowLayer(px(6),0,0,0xff00dfff);
            canvas.drawCircle(x,y,px(4),paint);
            paint.clearShadowLayer();
        }
    }
}
