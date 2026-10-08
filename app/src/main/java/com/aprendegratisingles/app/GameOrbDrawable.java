package com.aprendegratisingles.app;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/** A 3D arcade-style orb. Its content and touch handling remain native Views. */
public final class GameOrbDrawable extends Drawable {
    public static final int LOCKED = 0;
    public static final int AVAILABLE = 1;
    public static final int COMPLETE = 2;
    private final int state;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int alpha = 255;
    GameOrbDrawable(int state) { this.state=state; }
    @Override public void draw(Canvas canvas) {
        Rect b=getBounds();
        float x=b.exactCenterX(), y=b.exactCenterY();
        float size=Math.min(b.width(),b.height());
        float r=size*.46f;
        int halo=state==LOCKED?Color.rgb(127,150,195):state==COMPLETE?Color.rgb(123,255,100):Color.rgb(0,229,255);
        int top=state==LOCKED?Color.rgb(151,164,190):state==COMPLETE?Color.rgb(50,255,161):Color.rgb(28,239,255);
        int middle=state==LOCKED?Color.rgb(93,107,142):state==COMPLETE?Color.rgb(13,210,111):Color.rgb(0,146,253);
        int bottom=state==LOCKED?Color.rgb(56,66,98):state==COMPLETE?Color.rgb(6,112,100):Color.rgb(6,54,183);
        p.reset();p.setAntiAlias(true);
        p.setColor(Color.argb(state==LOCKED?70:145,Color.red(halo),Color.green(halo),Color.blue(halo)));
        p.setShadowLayer(r*.25f,0,r*.02f,halo);
        canvas.drawCircle(x,y,r*.99f,p);
        p.clearShadowLayer();
        p.setColor(Color.rgb(3,45,156)); canvas.drawCircle(x,y+r*.10f,r*.95f,p);
        p.setShader(new RadialGradient(x-r*.28f,y-r*.38f,r*1.55f,
                new int[]{top,middle,bottom},new float[]{0f,.49f,1f},Shader.TileMode.CLAMP));
        canvas.drawCircle(x,y-r*.075f,r*.86f,p);
        p.setShader(null);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(r*.10f);p.setColor(Color.WHITE);
        canvas.drawCircle(x,y-r*.075f,r*.84f,p);
        p.setStrokeWidth(r*.040f);p.setColor(halo);
        canvas.drawCircle(x,y-r*.075f,r*.94f,p);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.argb(state==LOCKED?45:130,255,255,255));
        canvas.drawOval(x-r*.58f,y-r*.70f,x+r*.09f,y-r*.47f,p);
        p.setColor(Color.argb(85,255,255,255));
        canvas.drawCircle(x-r*.55f,y-r*.35f,r*.075f,p);
    }
    @Override public void setAlpha(int a){alpha=a;invalidateSelf();}
    @Override public void setColorFilter(ColorFilter c){}
    @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
