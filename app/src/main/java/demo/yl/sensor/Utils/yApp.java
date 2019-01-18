package demo.yl.sensor.Utils;

import android.annotation.TargetApi;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import demo.yl.sensor.MyApplication;

/**
 * Created by Yl on 16/11/28.
 */

public class yApp {
    private static Boolean isDebug;
    public yApp() {
    }

    public static double[] ReverseSelf(double[] d) throws Exception {

        for (int start = 0, end = d.length - 1; start < end; start++, end--) {
            double temp = d[end];
            d[end] = d[start];
            d[start] = temp;
        }
        return d;
    }
    public static boolean getDebugMode() {
        try {
            if(isDebug == null) {
                PackageManager e = MyApplication.getInstance().getApplicationContext().getPackageManager();
                ApplicationInfo info = e.getApplicationInfo(MyApplication.getInstance().getApplicationContext().getPackageName(), 0);
                isDebug = Boolean.valueOf((info.flags & 2) != 0);
            }

            return isDebug.booleanValue();
        } catch (PackageManager.NameNotFoundException var2) {
            isDebug = Boolean.valueOf(false);
            return isDebug.booleanValue();
        }
    }

    public static String getVersionName() {
        try {
            PackageManager e = MyApplication.getInstance().getPackageManager();
            PackageInfo packInfo = e.getPackageInfo(MyApplication.getInstance().getPackageName(), 0);
            return packInfo.versionName;
        } catch (Exception var2) {
            var2.printStackTrace();
            return "";
        }
    }

    public static double toDouble(double f) {
        BigDecimal bg = new BigDecimal(f);
        double f1 = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
//        f1= Math.round(f1);
       return f1;
    }

    public static int getVersionCode() {
        try {
            PackageManager e = MyApplication.getInstance().getPackageManager();
            PackageInfo packInfo = e.getPackageInfo(MyApplication.getInstance().getPackageName(), 0);
            return packInfo.versionCode;
        } catch (Exception var2) {
            return 0;
        }
    }

    public static String getVersionStr() {
        return getVersionName() + "-" + getVersionCode();
    }

    @TargetApi(9)
    public static long getInstallTime() {
        long appInstallTime = 0L;
        PackageManager pm = MyApplication.getInstance().getPackageManager();
        PackageInfo packageInfo = null;

        try {
            packageInfo = pm.getPackageInfo(MyApplication.getInstance().getPackageName(), 0);
            int e = Build.VERSION.SDK_INT;
            if(e >= 9) {
                appInstallTime = packageInfo.lastUpdateTime;
            } else {
                ApplicationInfo appInfo = MyApplication.getInstance().getPackageManager().getApplicationInfo(MyApplication.getInstance().getPackageName(), 0);
                String sAppFile = appInfo.sourceDir;
                appInstallTime = (new File(sAppFile)).lastModified();
            }
        } catch (PackageManager.NameNotFoundException var7) {
            var7.printStackTrace();
        }

        return appInstallTime;
    }

    public static long getAppBuildTime() {
        ZipFile zf = null;
        boolean isError = false;
        long appBuildTime = 0L;

        try {
            ApplicationInfo ex = MyApplication.getInstance().getPackageManager().getApplicationInfo(MyApplication.getInstance().getPackageName(), 0);
            zf = new ZipFile(ex.sourceDir);
            ZipEntry ze = zf.getEntry("classes.dex");
            if(ze != null) {
                appBuildTime = ze.getTime();
            }
        } catch (Throwable var14) {
            isError = true;
        } finally {
            if(zf != null) {
                try {
                    zf.close();
                } catch (IOException var13) {
                    ;
                }
            }

        }

        return isError?1L:appBuildTime;
    }

    public static String byte2HexStr(byte[] paramArrayOfByte, int paramInt)
    {
        StringBuilder localStringBuilder = new StringBuilder("");
        int i = 0;
        if (i >= paramInt) {
            return localStringBuilder.toString().toUpperCase().trim();
        }
        String str = Integer.toHexString(paramArrayOfByte[i] & 0xFF);
        if (str.length() == 1) {
            str = "0" + str;
        }
        for (;;)
        {
            localStringBuilder.append(str);
            localStringBuilder.append(" ");
            i += 1;
            break;
        }
        return localStringBuilder.toString();
    }

    public static boolean checkHexStr(String paramString)
    {
        paramString = paramString.toString().trim().replace(" ", "").toUpperCase();
        int j = paramString.length();
        if ((j > 1) && (j % 2 == 0))
        {
            int i = 0;
            for (;;)
            {
                if (i >= j) {
                    return true;
                }
                if (!"0123456789ABCDEF".contains(paramString.substring(i, i + 1))) {
                    return false;
                }
                i += 1;
            }
        }
        return false;
    }

    public static byte[] hexStr2Bytes(String paramString)
    {
        paramString = paramString.trim().replace(" ", "").toUpperCase();
        int j = paramString.length() / 2;
        byte[] arrayOfByte = new byte[j];
        int i = 0;
        for (;;)
        {
            if (i >= j) {
                return arrayOfByte;
            }
            int k = i * 2 + 1;
            arrayOfByte[i] = ((byte)(Integer.decode("0x" + paramString.substring(i * 2, k) + paramString.substring(k, k + 1)).intValue() & 0xFF));
            i += 1;
        }
    }

//    public static String hexStr2Str(String paramString)
//    {
////        char[] arrayOfChar = paramString.toCharArray();
//////        paramString = new byte[paramString.length() / 2];
////        int i = 0;
////        for (;;)
////        {
////            if (i >= paramString.length) {
////                return new String(paramString);
////            }
////            paramString[i] = ((byte)("0123456789ABCDEF".indexOf(arrayOfChar[(i * 2)]) * 16 + "0123456789ABCDEF".indexOf(arrayOfChar[(i * 2 + 1)]) & 0xFF));
////            i += 1;
////        }
//    }
}
