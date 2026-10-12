package com.coderpage.mine.crash;

import android.app.Application;
import android.os.Build;

import com.coderpage.mine.BuildConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

/**
 * 把未捕获异常和显式记录的错误写到应用专属目录，不上传。
 */
public final class LocalCrashHandler implements Thread.UncaughtExceptionHandler {

    private static final int MAX_FILES = 30;

    private final Application mApp;
    private final Thread.UncaughtExceptionHandler mDefaultHandler;

    private LocalCrashHandler(Application app) {
        mApp = app;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    public static void install(Application app) {
        Thread.setDefaultUncaughtExceptionHandler(new LocalCrashHandler(app));
    }

    /** 记录已捕获的错误，不中断当前流程。 */
    public static void record(Application app, Throwable error) {
        if (app == null || error == null) {
            return;
        }
        write(app, "caught", Thread.currentThread(), error);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable error) {
        write(mApp, "crash", thread, error);
        if (mDefaultHandler != null && mDefaultHandler != this) {
            mDefaultHandler.uncaughtException(thread, error);
        }
    }

    private static void write(Application app, String kind, Thread thread, Throwable error) {
        FileOutputStream outputStream = null;
        try {
            File dir = app.getExternalFilesDir("crash");
            if (dir == null) {
                dir = new File(app.getFilesDir(), "crash");
            }
            if (!dir.exists() && !dir.mkdirs()) {
                return;
            }
            trimOldFiles(dir);
            String time = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(new Date());
            File file = new File(dir, time + "-" + kind + ".log");
            StringWriter stack = new StringWriter();
            error.printStackTrace(new PrintWriter(stack));
            String body = "time=" + time
                    + "\nkind=" + kind
                    + "\nversion=" + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")"
                    + "\nflavor=" + BuildConfig.FLAVOR
                    + "\nmodel=" + Build.MANUFACTURER + " " + Build.MODEL
                    + "\nsdk=" + Build.VERSION.SDK_INT
                    + "\nthread=" + (thread == null ? "" : thread.getName())
                    + "\n\n" + stack.toString();
            outputStream = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, Charset.forName("UTF-8"));
            writer.write(body);
            writer.flush();
        } catch (Throwable ignored) {
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Throwable ignored) {
                }
            }
        }
    }

    private static void trimOldFiles(File dir) {
        File[] files = dir.listFiles();
        if (files == null || files.length < MAX_FILES) {
            return;
        }
        Arrays.sort(files, (left, right) -> left.getName().compareTo(right.getName()));
        int removeCount = files.length - MAX_FILES + 1;
        for (int i = 0; i < removeCount && i < files.length; i++) {
            files[i].delete();
        }
    }
}
