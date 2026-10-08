package com.aprendegratisingles.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

/** High-contrast white icons drawn as vectors, rather than emoji fonts or baked image controls. */
public final class GameSymbolView extends View {
    private final int kind; // 0 book, 1 chat, 2 headphones, 3 star, 4 language
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    GameSymbolView(Context context,int kind){super(context);this.kind=kind;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        float s=Math.min(getWidth(),getHeight())*.68f;
        c.save(); c.translate(getWidth()/2f,getHeight()/2f); c.scale(s/100f,s/100f);
        p.reset();p.setAntiAlias(true);p.setColor(Color.WHITE);p.setStyle(Paint.Style.FILL);
        p.setShadowLayer(2.5f,0,3f,0xff156bc4);
        if(kind==0){
            // Pages of an open book: separated fold and gently curved top edges.
            Path book=new Path();
            book.moveTo(-48,-31);book.quadTo(-25,-40,-4,-28);book.lineTo(-4,32);
            book.quadTo(-29,19,-48,28);book.close();c.drawPath(book,p);
            book.reset();book.moveTo(4,-28);book.quadTo(29,-40,48,-31);book.lineTo(48,28);
            book.quadTo(27,19,4,32);book.close();c.drawPath(book,p);
            p.clearShadowLayer();p.setColor(0xff2f95fa);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);
            c.drawLine(0,-26,0,33,p);
        } else if(kind==1){
            c.drawRoundRect(new RectF(-44,-32,44,22),21,21,p);
            Path tip=new Path();tip.moveTo(-9,17);tip.lineTo(-26,39);tip.lineTo(6,18);tip.close();c.drawPath(tip,p);
            p.clearShadowLayer();p.setColor(0xff1189e9);
            for(int x=-23;x<=23;x+=23)c.drawCircle(x,-4,5,p);
        } else if(kind==2){
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(13);p.setStrokeCap(Paint.Cap.ROUND);
            c.drawArc(new RectF(-39,-41,39,32),180,180,false,p);
            p.setStyle(Paint.Style.FILL);
            c.drawRoundRect(new RectF(-47,-5,-26,34),8,8,p);
            c.drawRoundRect(new RectF(26,-5,47,34),8,8,p);
        } else if(kind==3){
            Path star=new Path();
            for(int i=0;i<10;i++){
                double a=-Math.PI/2 + i*Math.PI/5;
                float r=(i%2==0)?48:20;
                float x=(float)Math.cos(a)*r, y=(float)Math.sin(a)*r;
                if(i==0)star.moveTo(x,y);else star.lineTo(x,y);
            }
            star.close();c.drawPath(star,p);
        } else {
            p.clearShadowLayer();p.setTypeface(android.graphics.Typeface.create("sans-serif",android.graphics.Typeface.BOLD));
            p.setTextSize(48);p.setTextAlign(Paint.Align.CENTER);
            c.drawText("文",-15,18,p);
            p.setTextSize(39);c.drawText("A",27,28,p);
        }
        p.clearShadowLayer();c.restore();
    }
}
