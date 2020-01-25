package com.example.ffmpegdemo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    String outPutFile;
    Uri filePath, filePathSecond, photoUri, AudioUri;
    ProgressDialog progressDialog;
    private int REQUEST_TAKE_GALLERY_VIDEO = 110;
    private int REQUEST_TAKE_GALLERY_VIDEO_2 = 115;
    private int REQUEST_TAKE_GALLERY_PHOTO = 120;
    private int REQUEST_TAKE_GALLERY_AUDIO = 130;
    private FFmpeg ffmpeg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressDialog = new ProgressDialog(this);
        findViewById(R.id.selectVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO);
            }
        });
        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filePath != null) {
                    executeCutVideoCommand(1 * 1000, 4 * 1000, filePath);
                }
            }
        });
        findViewById(R.id.background).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filePath != null && photoUri != null) {
                    executeBackgroundCommand(filePath, photoUri);
                }
            }
        });
        findViewById(R.id.selectPhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_PHOTO);
            }
        });
        findViewById(R.id.speed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filePath != null) {
                    executeSpeedCommand(.9f, filePath);
                }
            }
        });
        findViewById(R.id.rotate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filePath != null) {
                    executeRotateCommand(3, filePath);
                }
            }
        });
        findViewById(R.id.selectAudio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("audio/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_AUDIO);
            }
        });
        findViewById(R.id.setAudio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filePath != null && AudioUri != null) {
                    executeChangeMusicCommand(filePath, AudioUri);
                }
            }
        });
        findViewById(R.id.mute).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filePath != null) {
                    executeMuteCommand(filePath);
                }
            }
        });
        findViewById(R.id.volume).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filePath != null) {
                    executeVolumeCommand(filePath, .5f);
                }
            }
        });
        findViewById(R.id.audio_trim).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AudioUri != null) {
                    executeCutAudioCommand(1 * 1000, 10 * 1000, AudioUri);
                }
            }
        });
        findViewById(R.id.marge_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filePath != null && filePathSecond != null)
                    executeMargeVideoCommand(filePath, filePathSecond);
            }
        });
        findViewById(R.id.selectVideo_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO_2);
            }
        });
        findViewById(R.id.ratio_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filePath != null)
                    executeRatioCommand(filePath, 2, 5);
            }
        });
        findViewById(R.id.filter_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(filePath!=null){
                    executeFilterCommand(filePath);
                }
            }
        });
        loadFFMpegBinary();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
            Uri uri = data.getData();
            if (uri != null) {
                filePath = uri;
            }
        }
        if (requestCode == REQUEST_TAKE_GALLERY_VIDEO_2) {
            Uri uri = data.getData();
            if (uri != null) {
                filePathSecond = uri;
            }
        }
        if (requestCode == REQUEST_TAKE_GALLERY_PHOTO) {
            Uri uri = data.getData();
            if (uri != null) {
                photoUri = uri;
            }
        }
        if (requestCode == REQUEST_TAKE_GALLERY_AUDIO) {
            Uri uri = data.getData();
            if (uri != null) {
                AudioUri = uri;
            }
        }
    }

    private void executeBackgroundCommand(Uri filePath, Uri photoUri) {
        File moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        );
        String filePrefix = "set_background_video";
        String fileExtn = ".mp4";
        String yourRealPath = getPath(MainActivity.this, filePath);
        String yourRealPathImage = getPath(MainActivity.this, photoUri);
        File dest = new File(moviesDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
        }
       /* Log.d(TAG, "startTrim: src: " + yourRealPath);
        Log.d(TAG, "startTrim: dest: " + dest.getAbsolutePath());
        Log.d(TAG, "startTrim: startMs: " + startMs);
        Log.d(TAG, "startTrim: endMs: " + endMs);*/
        outPutFile = dest.getAbsolutePath();
        //String[] complexCommand = {"-i", yourRealPath, "-ss", "" + startMs / 1000, "-t", "" + endMs / 1000, dest.getAbsolutePath()};
        String[] complexCommand = {"-i", yourRealPathImage, "-i", yourRealPath
                , "-filter_complex", "overlay=(main_w-overlay_w)/2:(main_h-overlay_h)/2", outPutFile};
        //   String[] complexCommand = {"-i", "" + yourRealPath, "-i", "" +yourRealPathImage, "-filter_complex", "overlay=10:main_h-overlay_h-10",outPutFile};
        execFFmpegBinary(complexCommand);
    }

    // video trim
    private void executeCutVideoCommand(int startMs, int endMs, Uri filePath) {
        File moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        );
        String filePrefix = "cut_video";
        String fileExtn = ".mp4";
        String yourRealPath = getPath(MainActivity.this, filePath);
        File dest = new File(moviesDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
        }
       /* Log.d(TAG, "startTrim: src: " + yourRealPath);
        Log.d(TAG, "startTrim: dest: " + dest.getAbsolutePath());
        Log.d(TAG, "startTrim: startMs: " + startMs);
        Log.d(TAG, "startTrim: endMs: " + endMs);*/
        outPutFile = dest.getAbsolutePath();
        //String[] complexCommand = {"-i", yourRealPath, "-ss", "" + startMs / 1000, "-t", "" + endMs / 1000, dest.getAbsolutePath()};
        String[] complexCommand = {"-ss",
                "" + (startMs / 1000),
                "-y",
                "-i",
                yourRealPath,
                "-t",
                "" + ((endMs - startMs) / 1000),
                "-vcodec",
                "mpeg4",
                "-b:v",
                "2097152",
                "-b:a",
                "48000",
                "-ac",
                "2",
                "-ar",
                "22050",
                outPutFile};
        execFFmpegBinary(complexCommand);
    }

    private String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
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
                    final String id = DocumentsContract.getDocumentId(uri);
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
        }
        return null;
    }

    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private String getDataColumn(Context context, Uri uri, String selection,
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

    private void loadFFMpegBinary() {
        try {
            if (ffmpeg == null) {
                ffmpeg = FFmpeg.getInstance(this);
            }
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                }

                @Override
                public void onSuccess() {
                }
            });
        } catch (FFmpegNotSupportedException e) {
        } catch (Exception e) {
        }
    }

    private void execFFmpegBinary(final String[] command) {
        progressDialog.show();
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    // Log.d(TAG, "FAILED with output : " + s);
                }

                @Override
                public void onSuccess(String s) {
                    //    Log.d(TAG, "SUCCESS with output : " + s);
                    Toast.makeText(MainActivity.this, "SUCCESS" + outPutFile, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onProgress(String s) {
                }

                @Override
                public void onStart() {
                    progressDialog.setMessage("Processing...");
                    progressDialog.show();
                }

                @Override
                public void onFinish() {
                    progressDialog.dismiss();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            Toast.makeText(MainActivity.this, "EXCEPTION", Toast.LENGTH_SHORT).show();

            // do nothing for now
        }
    }

    private void executeSpeedCommand(float speed, Uri filePath) {
        File moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        );
        String filePrefix = "speed_change_video";
        String fileExtn = ".mp4";
        String yourRealPath = getPath(MainActivity.this, filePath);
        File dest = new File(moviesDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
        }

        outPutFile = dest.getAbsolutePath();
        String[] complexCommand = {"-y", "-i", yourRealPath, "-filter_complex",
                "[0:v]setpts=" + (2.5 - speed) + "*PTS[v];[0:a]atempo=" + speed + "[a]", "-map", "[v]", "-map", "[a]", "-b:v", "2097k", "-r", "60", "-vcodec", "mpeg4", outPutFile};
        execFFmpegBinary(complexCommand);
    }


    private void executeRotateCommand(int rotate, Uri filePath) {

        File moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        );
        String filePrefix = "speed_rotation_video";
        String fileExtn = ".mp4";
        String yourRealPath = getPath(MainActivity.this, filePath);
        File dest = new File(moviesDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
        }

        outPutFile = dest.getAbsolutePath();
        String[] complexCommand = null;
        if (rotate == 3) {
            complexCommand = new String[]{"-i", yourRealPath, "-filter:v", "transpose=2,transpose=2", outPutFile};

        } else {
            complexCommand = new String[]{"-i", yourRealPath, "-filter:v", "transpose=" + rotate, outPutFile};
        }
        execFFmpegBinary(complexCommand);
    }

    private void executeChangeMusicCommand(Uri filePath, Uri audioUri) {
        File moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        );
        String filePrefix = "audio_change_video";
        String fileExtn = ".mp4";
        String yourRealPath = getPath(MainActivity.this, filePath);
        String yourRealPathAudio = getPath(MainActivity.this, audioUri);
        File dest = new File(moviesDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
        }

        outPutFile = dest.getAbsolutePath();
        String[] complexCommand = {"-i", yourRealPath, "-i", yourRealPathAudio, "-c:v", "copy", "-c:a", "aac", "-map", "0:v:0", "-map", "1:a:0", "-shortest", outPutFile};
        execFFmpegBinary(complexCommand);
    }

    private void executeMuteCommand(Uri filePath) {
        File moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        );
        String filePrefix = "mute_change_video";
        String fileExtn = ".mp4";
        String yourRealPath = getPath(MainActivity.this, filePath);
        File dest = new File(moviesDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
        }

        outPutFile = dest.getAbsolutePath();
        String[] complexCommand = {"-i", yourRealPath, "-vcodec", "copy", "-an", outPutFile};
        execFFmpegBinary(complexCommand);
    }

    private void executeVolumeCommand(Uri filePath, float volume) {
        File moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        );
        String filePrefix = "volume_change_video";
        String fileExtn = ".mp4";
        String yourRealPath = getPath(MainActivity.this, filePath);
        File dest = new File(moviesDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
        }

        outPutFile = dest.getAbsolutePath();
        String[] complexCommand = {"-i", yourRealPath, "-filter:a", "volume=" + volume, outPutFile};
        execFFmpegBinary(complexCommand);
    }

    private void executeCutAudioCommand(int startMs, int endMs, Uri filePath) {
        File moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        );
        String filePrefix = "cut_video";
        String fileExtn = ".mp3";
        String yourRealPath = getPath(MainActivity.this, filePath);
        File dest = new File(moviesDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
        }
       /* Log.d(TAG, "startTrim: src: " + yourRealPath);
        Log.d(TAG, "startTrim: dest: " + dest.getAbsolutePath());
        Log.d(TAG, "startTrim: startMs: " + startMs);
        Log.d(TAG, "startTrim: endMs: " + endMs);*/
        outPutFile = dest.getAbsolutePath();
        //String[] complexCommand = {"-i", yourRealPath, "-ss", "" + startMs / 1000, "-t", "" + endMs / 1000, dest.getAbsolutePath()};
        String[] complexCommand = {"-ss",
                "" + (startMs / 1000),
                "-y",
                "-i",
                yourRealPath,
                "-t",
                "" + ((endMs - startMs) / 1000),
                "-vcodec",
                "mpeg4",
                "-b:v",
                "2097152",
                "-b:a",
                "48000",
                "-ac",
                "2",
                "-ar",
                "22050",
                outPutFile};
        execFFmpegBinary(complexCommand);
    }

    private void executeMargeVideoCommand(Uri filePath, Uri filePathSecond) {
        File moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        );
        String filePrefix = "marge_change_video";
        String fileExtn = ".mp4";
        String yourRealPath = getPath(MainActivity.this, filePath);
        String yourRealPath2 = getPath(MainActivity.this, filePathSecond);
        File dest = new File(moviesDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
        }

        outPutFile = dest.getAbsolutePath();
        String complexCommand[] = {"-y", "-i", yourRealPath, "-i", yourRealPath2, "-strict", "experimental", "-filter_complex",
                "[0:v]scale=iw*min(1920/iw\\,1080/ih):ih*min(1920/iw\\,1080/ih), pad=1920:1080:(1920-iw*min(1920/iw\\,1080/ih))/2:(1080-ih*min(1920/iw\\,1080/ih))/2,setsar=1:1[v0];[1:v] scale=iw*min(1920/iw\\,1080/ih):ih*min(1920/iw\\,1080/ih), pad=1920:1080:(1920-iw*min(1920/iw\\,1080/ih))/2:(1080-ih*min(1920/iw\\,1080/ih))/2,setsar=1:1[v1];[v0][0:a][v1][1:a] concat=n=2:v=1:a=1",
                "-ab", "48000", "-ac", "2", "-ar", "22050", "-s", "1920x1080", "-vcodec", "libx264", "-crf", "27",
                "-q", "4", "-preset", "ultrafast", outPutFile};
        execFFmpegBinary(complexCommand);
    }

    private void executeRatioCommand(Uri filePath, int w, int h) {
        File moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        );
        String filePrefix = "ratio_change_video";
        String fileExtn = ".mp4";
        String yourRealPath = getPath(MainActivity.this, filePath);
        File dest = new File(moviesDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
        }

        outPutFile = dest.getAbsolutePath();
        //  String complexCommand[] = {"-i", yourRealPath,"-r", "15", "-aspect" ,""+w+":"+""+h ,"-strict" ,"-2",outPutFile};
        String complexCommand[] = new String[]{"-i", yourRealPath, "-lavf", "[0:v]scale=1920*2:1080*2,boxblur=luma_radius=min(h,w)/20:luma_power=1:chroma_radius=min(cw,ch)/20:chroma_power=1[bg];[0:v]scale=-1:1080[ov];[bg][ov]overlay=(W-w)/2:(H-h)/2,crop=w=1920:h=1080", outPutFile};
        execFFmpegBinary(complexCommand);
    }


    private void executeFilterCommand(Uri filePath) {
        File moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        );
        String filePrefix = "filter_change_video";
        String fileExtn = ".mp4";
        String yourRealPath = getPath(MainActivity.this, filePath);
        File dest = new File(moviesDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
        }

        outPutFile = dest.getAbsolutePath();
        //  String complexCommand[] = {"-i", yourRealPath,"-r", "15", "-aspect" ,""+w+":"+""+h ,"-strict" ,"-2",outPutFile};
        String complexCommand[] = { "-i",yourRealPath,"-vf", "split [main][tmp]; [tmp] lutyuv=","y=val*5"," [tmp2]; [main][tmp2] overlay", outPutFile};
        execFFmpegBinary(complexCommand);
    }

}
