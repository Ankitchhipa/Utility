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
import com.cam.scanner.scantopdf.android.util.BaseColor;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

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
        float pageWidth = document.getPageSize().getWidth();
        float pageHeight = document.getPageSize().getHeight();
        float x = document.getPageSize().getRight() - (pageWidth * 0.05f);
        float y = document.getPageSize().getBottom() + (pageHeight * 0.05f);

        try {
            if (waterMark != null && waterMark.getImage() != null) {
                try {
                    mLogoImage = Image.getInstance(waterMark.getImage());
                    // Adjust scale for corner placement
                    mLogoImage.scaleToFit(150, 150);
                    mLogoImage.setAbsolutePosition(x - mLogoImage.getScaledWidth(), y);
                    canvas.addImage(mLogoImage);
                    Log.d(TAG, "Image watermark added at bottom right corner");
                } catch (Exception e) {
                    Log.e(TAG, "Error adding image watermark", e);
                }
            }

            if (mPhrase != null) {
                ColumnText.showTextAligned(canvas, Element.ALIGN_RIGHT, mPhrase, x, y, waterMark.getRotationAngle());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onEndPage", e);
        }
    }

    public void setWatermark(Context context, WaterMark waterMark, String selectedPdfPageSize) {
        this.waterMark = waterMark;
        float textSize = 30;
        switch (selectedPdfPageSize) {
            case "EXECUTIVE":
                textSize = 25;
                break;
            case "A3":
                textSize = 40;
                break;
            case "A4":
                textSize = 30;
                break;
            case "A5":
                textSize = 20;
                break;
            default:
                textSize = 30;
                break;
        }
        try {
            this.mPhrase = new Phrase(waterMark.getWaterMarkText() != null ? waterMark.getWaterMarkText() : "",
                    new Font(waterMark.getFontFamily(), textSize,
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
        float textSize = 30;
        this.mPhrase = new Phrase(waterMark.getWaterMarkText() != null ? waterMark.getWaterMarkText() : "",
                new Font(waterMark.getFontFamily(), textSize,
                        waterMark.getFontStyle(), waterMark.getTextColor()));
        Font font = new Font(waterMark.getFontFamily(), textSize,
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
