package com.cam.scanner.scantopdf.android.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.models.WaterMark;
import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.Image;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.ColumnText;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfPageEventHelper;
import org.openpdf.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class WatermarkPageEvent extends PdfPageEventHelper {

    public WaterMark getWaterMark() {
        return waterMark;
    }

    private WaterMark waterMark;
    private Phrase mPhrase;
    private Paragraph mParagraph;
    private static final String TAG = WatermarkPageEvent.class.getSimpleName();
    private Image mLogoImage;

    @Override
    public void onEndPage(PdfWriter writer, Document document) {

        PdfContentByte canvas = writer.getDirectContent();
        /*float x = (document.getPageSize().getLeft() + document.getPageSize().getRight()) / 2;
        float y = (document.getPageSize().getTop() + document.getPageSize().getBottom()) / 2;*/
        Log.e(TAG, "get left" + document.getPageSize().getLeft());
        Log.e(TAG, "get right" + document.getPageSize().getRight());
        Log.e(TAG, "get top" + document.getPageSize().getTop());
        Log.e(TAG, "get bottom" + document.getPageSize().getBottom());
        try {
            int half = (int) document.getPageSize().getRight() / 2;
            float x = half + ((float) half / 4);
            float y = 15;
            canvas = addBackgroundInWaterMark(canvas, x, y - 7);
            ColumnText.showTextAligned(canvas, Element.ALIGN_BOTTOM, mPhrase, x, y, waterMark.getRotationAngle());
        } catch (NoSuchMethodError e) {
            e.printStackTrace();
        }
    }

    public void setWatermark(Context context, WaterMark waterMark, String selectedPdfPageSize) {
        this.waterMark = waterMark;
        float textSize = 20;
        switch (selectedPdfPageSize) {
            case "EXECUTIVE":
                textSize = 15;
                break;
            case "A3":
                textSize = 25;
                break;
            case "A4":
                textSize = 20;
                break;
            case "A5":
                textSize = 12;
                break;
            default:
                textSize = 20;
                break;
        }
        try {
            this.mPhrase = new Phrase("",
                    new Font(waterMark.getFontFamily(), waterMark.getTextSize(),
                            waterMark.getFontStyle(), waterMark.getTextColor()));
            /*Font font = new Font(waterMark.getFontFamily(), waterMark.getTextSize(),
                    waterMark.getFontStyle(), waterMark.getTextColor());
            mParagraph = createWatermarkParagraph(context, waterMark, font);*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setWatermark(Context context, WaterMark waterMark) {
        this.waterMark = waterMark;
        /*this.mPhrase = new Phrase(waterMark.getWaterMarkText(),
                new Font(waterMark.getFontFamily(), 20,
                        waterMark.getFontStyle(), waterMark.getTextColor()));*/
        Font font = new Font(waterMark.getFontFamily(), 20,
                waterMark.getFontStyle(), waterMark.getTextColor());
        mParagraph = createWatermarkParagraph(context, waterMark, font);
    }

    private Paragraph createWatermarkParagraph(Context context, WaterMark waterMark, Font font) {
        Paragraph paragraph = new Paragraph();
        try {
            // Load the image
            Bitmap bitmap = getBitmapFromDrawable(context,R.drawable.ic_camera_sign);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            if (bitmap != null) {
                Log.e("watermarkPage11", "bitmap::" + bitmap);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                Log.e("watermarkPage22", "bitmap::" + bitmap);
                Image image = Image.getInstance(stream.toByteArray());
                image.scaleAbsolute(30f, 30f); // Adjust image size as needed
                Log.e("watermarkPage33", "bitmap::" + image.toString());
                Chunk imageChunk = new Chunk(image, 0, -5); // Adjust vertical alignment as needed
                paragraph.add(imageChunk);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        Chunk textChunk = new Chunk("  " + waterMark.getWaterMarkText(), font);
//        paragraph.add(new Phrase(waterMark.getWaterMarkText(),font));
        Log.e("watermarkPage33", "bitmap::" + paragraph.toString());
        return paragraph;
    }

    public static Bitmap getBitmapFromDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable == null) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }


    /*public static Bitmap getBitmapFromDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable == null) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }*/

    public PdfContentByte addBackgroundInWaterMark(PdfContentByte pdfContentByte, float rectX, float rectY) {
        try {
            mLogoImage = Image.getInstance(waterMark.getImage());

            pdfContentByte.setColorFill(BaseColor.WHITE);
            mLogoImage.setAbsolutePosition(rectX, rectY);
            mLogoImage.setBackgroundColor(Color.WHITE);
            mLogoImage.scaleToFit(mLogoImage.getWidth() , mLogoImage.getHeight() - 20);
            pdfContentByte.addImage(mLogoImage);
            pdfContentByte.setColorFill(Color.WHITE);
            pdfContentByte.fill();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return pdfContentByte;
    }

}
