package com.project.videoeditor.support;

import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;

import androidx.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

public class SupportUtil {
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                String id = DocumentsContract.getDocumentId(uri);
                if (!TextUtils.isEmpty(id)) {

                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:", "");
                    }
                    if (id.startsWith("msf:")) {
                        return  safUriToFFmpegPath(context,uri);
                    }
                    try {
                        final Uri contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                        return getDataColumn(context, contentUri, null, null);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }



                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }


    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static File CreateFolder(String newFolderPath) throws Exception {

        File folderFile = new File(newFolderPath);
        if (!folderFile.exists())
                folderFile.mkdirs();
        return  folderFile;
    }
    public static String safUriToFFmpegPath(final Context context, final Uri uri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
            return String.format(Locale.getDefault(), "pipe:%d", parcelFileDescriptor.getFd());
        } catch (FileNotFoundException e) {
            return "";
        }
    }
    public static String getInfoByUri(final Context context,final Uri uri,final String columnName)
    {
        File myFile = new File(uri.toString());
        if (uri.toString().startsWith("content://")) {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    return cursor.getString(cursor.getColumnIndex(columnName));
                }
            } finally {
                cursor.close();
            }
        } else if (uri.toString().startsWith("file://")) {
            if(columnName.equals(OpenableColumns.DISPLAY_NAME))
                return myFile.getName();
            else if(columnName.equals(OpenableColumns.SIZE))
                return String.valueOf(myFile.getTotalSpace());
        }
        return null;
    }
    public static File CreateFileInFolder(String folderPath,String filename)
    {
        return new File(folderPath, filename);
    }

    public static String OpenRawResourcesAsString(Context context,int rawResoursePath)
    {
        InputStream inputStream = context.getResources().openRawResource(rawResoursePath);
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder total = new StringBuilder();
        String line = null;
        try {
            line = r.readLine();
            while (line != null) {
                total.append(line).append('\n');
                line = r.readLine();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return  total.toString();
    }
    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static Bitmap[] splitBitmap(Bitmap bmp, int splitPosition)
    {
        Paint paint=new Paint();
        Bitmap bitmaps[] = new Bitmap[2];
        Rect splitBox1 = new Rect(0,0,splitPosition,bmp.getHeight());
        Rect destBox1 = splitBox1;
        Rect splitBox2 = new Rect(splitPosition,0,bmp.getWidth(),bmp.getHeight());
        Rect destBox2 = new Rect(0,0,bmp.getWidth() - splitPosition,bmp.getHeight());

        bitmaps[0] = Bitmap.createBitmap(splitPosition,bmp.getHeight(),bmp.getConfig());
        bitmaps[1] = Bitmap.createBitmap(bmp.getWidth() - splitPosition,bmp.getHeight(),bmp.getConfig());
        Canvas canvas = new Canvas(bitmaps[0]);
        canvas.drawBitmap(bmp,splitBox1,destBox1,null);
        Canvas canvas2 = new Canvas(bitmaps[1]);
        canvas2.drawBitmap(bmp,splitBox2,destBox2,null);
        return bitmaps;
    }

    public static Bitmap stickBitmap(Bitmap bmp1, Bitmap bmp2)
    {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth() + bmp2.getWidth(),
                bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, bmp1.getWidth(), 0, null);
        return bmOverlay;
    }

    public static Bitmap scaleBitmap(Bitmap bmp, int scaleWidthInPx, int scaleHeightInPx)
    {
        if(bmp != null)
            return Bitmap.createScaledBitmap(bmp, scaleWidthInPx, scaleHeightInPx, false);
        else
            throw new RuntimeException("SCALE BITMAP");
    }

    public static String changeFormatFilename(String filename,String newFormat)
    {
        int index = filename.lastIndexOf('.');
        return filename.substring(0,index) + "."+newFormat;
    }

    public static String getFilenameByPath(String path)
    {
        return path.substring(path.lastIndexOf("/")+1);
    }

    public static String getSettingCodecEncode(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("list_preference_ffmpeg_encode", "");
    }
    public static String getSettingEncodeVideoPath(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("dir_encode_preference", "");
    }

    public static String getSettingFilteredVideoPath(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("dir_filter_preference", "");
    }

    public static String getSettingExtractFramePath(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("dir_frames_extract_preference", "");
    }

    public static String getSettingExtractAudioPath(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("dir_audio_extract_preference", "");
    }
}
