//package com.jukuproject.jukutweet.
//
//import android.content.ContentResolver;
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.drawable.Drawable;
//import android.os.Environment;
//import android.provider.MediaStore;
//import android.support.annotation.Nullable;
//import android.util.Log;
//
//import com.inshodesign.bossrss.DB.InternalDB;
//import com.squareup.picasso.Picasso;
//import com.squareup.picasso.Target;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.lang.ref.WeakReference;
//
///**
// * Taken straight from maros136 from StackOverflow's answer...
// * http://stackoverflow.com/questions/27729976/download-and-save-images-using-picasso
// */
//
//public class TargetPhoneGallery implements Target {
//
//    private final WeakReference<ContentResolver> resolver;
//    private String title;
//    private Context context;
//
//
//    public TargetPhoneGallery(ContentResolver r, @Nullable String title, Context context)
//    {
//        this.resolver = new WeakReference<ContentResolver>(r);
//        this.title = title;
//        this.context = context;
//    }
//
//
//    @Override
//    public void onPrepareLoad (Drawable arg0)
//    {
//    }
//
//    @Override
//    public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
////                String appDirectoryName = "JukuTweetUserIcons";
//
//                new File()
//                File imageRoot = new File(Environment.getExternalStoragePublicDirectory(
//                        Environment.DIRECTORY_PICTURES), "JukuTweetUserIcons");
//
//                try {
//                    File path=new File(Environment.getExternalStoragePublicDirectory(
//                            Environment.DIRECTORY_PICTURES),"JukuTweetUserIcons");
//
//                    File mypath=new File(path,title);
//                    if (!mypath.exists()) {
//                        out = new OutputStreamWriter(openFileOutput( mypath.getAbsolutePath() , MODE_PRIVATE));
//                        out.write("test");
//                        out.close();
//                    }
//                }
//
//                File file = new file
//                File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + url);
//                try {
//                    file.createNewFile();
//                    FileOutputStream ostream = new FileOutputStream(file);
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, ostream);
//                    ostream.flush();
//                    ostream.close();
//                } catch (IOException e) {
//                    Log.e("IOException", e.getLocalizedMessage());
//                }
//            }
//        }).start();
//
//    }
//
//    @Override
//    public void onBitmapFailed (Drawable arg0)
//    {
//    }
//
//}
