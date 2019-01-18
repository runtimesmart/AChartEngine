package demo.yl.sensor.Utils;

import android.text.TextUtils;
import android.util.Log;

import java.io.FileOutputStream;

import demo.yl.sensor.MyApplication;

/**
 * Created by Yl on 16/11/28.
 */

public class yLog {
    public static final String TAG = "yl";

    public yLog() {
    }



    public static void d(String msg) {
        if(((MyApplication)MyApplication.getInstance()).mLogOpen) {
            String className = Thread.currentThread().getStackTrace()[3].getClassName();
            int index = className.lastIndexOf(".");
            if(index > -1) {
                className = className.substring(index + 1);
            }

            String msgToPrint = Thread.currentThread().getId() + " " + className + "." + Thread.currentThread().getStackTrace()[3].getMethodName();
            if(!TextUtils.isEmpty(msg)) {
                msgToPrint = msgToPrint + "--" + msg;
            }

            println(3, "wiz", msgToPrint);
        }
    }

    public static void d(String tag, String msg) {
        if(((MyApplication)MyApplication.getInstance()).mLogOpen) {

            String msgToPrint = Thread.currentThread().getStackTrace()[3].getMethodName();
            if(!TextUtils.isEmpty(msg)) {
                msgToPrint = msgToPrint + "--" + msg;
            }

            println(3, tag, msgToPrint);
        }
    }

    public static void d(String tag, String msg, Throwable tr) {
        if(((MyApplication)MyApplication.getInstance()).mLogOpen) {

            println(3, tag, msg + '\n' + getStackTraceString(tr));
        }
    }

    public static void i(String msg) {
        if(((MyApplication)MyApplication.getInstance()).mLogOpen) {

            i("wiz", msg);
        }
    }

    public static void i(String tag, String msg) {
        if(((MyApplication)MyApplication.getInstance()).mLogOpen) {

            String msgToPrint = Thread.currentThread().getStackTrace()[3].getMethodName();
            msgToPrint = msgToPrint + "--" + msg;
            println(4, tag, msgToPrint);
        }
    }

    public static void i(String tag, String msg, Throwable tr) {
        if(((MyApplication)MyApplication.getInstance()).mLogOpen) {

            println(4, tag, msg + '\n' + getStackTraceString(tr));
        }
    }

    public static int w(String msg) {
        return !((MyApplication)MyApplication.getInstance()).mLogOpen?-1:println(5, "wiz", msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        return !((MyApplication)MyApplication.getInstance()).mLogOpen?-1:println(5, tag, msg + '\n' + getStackTraceString(tr));
    }

    public static int w(String tag, Throwable tr) {
        return !((MyApplication)MyApplication.getInstance()).mLogOpen?-1:println(5, tag, getStackTraceString(tr));
    }

    public static int e(String msg) {
        return !((MyApplication)MyApplication.getInstance()).mLogOpen?-1:println(6, "wiz", msg);
    }

    public static int e(String tag, String msg) {
        return !((MyApplication)MyApplication.getInstance()).mLogOpen?-1:println(6, tag, msg);
    }

    public static int e(String msg, Throwable tr) {
        return !((MyApplication)MyApplication.getInstance()).mLogOpen?-1:println(6, "wiz", msg + '\n' + getStackTraceString(tr));
    }

    public static int e(String tag, String msg, Throwable tr) {
        return !((MyApplication)MyApplication.getInstance()).mLogOpen?-1:println(6, tag, msg + '\n' + getStackTraceString(tr));
    }

    public static int wtf(String tag, String msg) {
        return !((MyApplication)MyApplication.getInstance()).mLogOpen?-1: Log.wtf(tag, msg, (Throwable)null);
    }

    public static int wtf(String tag, Throwable tr) {
        return !((MyApplication)MyApplication.getInstance()).mLogOpen?-1: Log.wtf(tag, tr.getMessage(), tr);
    }

    public static int wtf(String tag, String msg, Throwable tr) {
        return !((MyApplication)MyApplication.getInstance()).mLogOpen?-1: Log.wtf(tag, msg, tr);
    }

    public static String getStackTraceString(Throwable tr) {
        return !((MyApplication)MyApplication.getInstance()).mLogOpen?"": Log.getStackTraceString(tr);
    }

    public static void footPrint() {
        if(((MyApplication)MyApplication.getInstance()).mLogOpen) {
            String className = Thread.currentThread().getStackTrace()[3].getClassName();
            int index = className.lastIndexOf(".");
            if(index > -1) {
                className = className.substring(index + 1);
            }

            String msgToPrint = Thread.currentThread().getId() + " " + className + "." + Thread.currentThread().getStackTrace()[3].getMethodName();
            println(3, "wiz", msgToPrint);
        }
    }

    public static void footPrint(String tag) {
        if(((MyApplication)MyApplication.getInstance()).mLogOpen) {
            String msgToPrint = Thread.currentThread().getStackTrace()[3].getMethodName();
            println(3, tag, msgToPrint);
        }
    }

    public static int println(int priority, String tag, String msg) {
        if(!((MyApplication)MyApplication.getInstance()).mLogOpen) {
            return -1;
        } else {
            if(TextUtils.isEmpty(msg)) {
                msg = "";
            }

            return Log.println(priority, tag, msg);
        }
    }





}
