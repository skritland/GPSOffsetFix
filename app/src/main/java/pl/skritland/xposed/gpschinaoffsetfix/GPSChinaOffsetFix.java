package pl.skritland.xposed.gpschinaoffsetfix;

/**
 * Created by skritland on 2015-03-09.
 */

import android.location.GpsStatus;
import android.os.*;
import android.os.Process;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class GPSChinaOffsetFix implements IXposedHookLoadPackage {

    public static final String LOGTAG = "GPSChinaOffsetFix";

    @Override
    public void handleLoadPackage(final LoadPackageParam loadPackageParam) throws Throwable {

        String self = GPSChinaOffsetFix.class.getPackage().getName();
        if (loadPackageParam.packageName.equals(self))
            return;

//        // Google Map V2
//        try {
//            Class.forName("com.google.android.gms.maps.GoogleMap", false, loadPackageParam.classLoader);
//            hookAll(XHookLocation.getInstances("GoogleMapsApiV2", null), loadPackageParam.classLoader);
//        } catch (Throwable ignored) {
//        }
//
//        // Location client
//        try {
//            Class.forName("com.google.android.gms.location.LocationClient", false, loadPackageParam.classLoader);
//            hookAll(XHookLocation.getInstances("LocationClient", null), loadPackageParam.classLoader);
//        } catch (Throwable ignored) {
//        }
        try {
            hookAll(XHookLocation.getInstances("LocationManager", null), loadPackageParam.classLoader);
        } catch (Throwable ignored) {
            Log.i("GPSChinaOffsetFix", "Nic nie bangla");
        }
    }

    public static void hookAll(List<XHookLocation> listHook, ClassLoader classLoader) {
        for (XHookLocation hook : listHook)
                hook(hook, classLoader);
    }

    private static void hook(final XHookLocation hook, ClassLoader classLoader) {
        Class<?> hookClass = null;
        Log.i("GPSChinaOffsetFix", "Proba hooka");
        try {
            hookClass = findClass(hook.getClassName(), classLoader);
            // Get members
            List<Member> listMember = new ArrayList<Member>();
            Class<?> clazz = hookClass;
            while (clazz != null && !"android.content.ContentProvider".equals(clazz.getName()))
                try {
                    for (Method method : clazz.getDeclaredMethods())
                        if (method.getName().equals(hook.getMethodName())
                                && !Modifier.isAbstract(method.getModifiers()))
                            listMember.add(method);

                    clazz = clazz.getSuperclass();
                } catch (Throwable ex) {
                    if (ex.getClass().equals(ClassNotFoundException.class))
                        break;
                    else
                        throw ex;
                }

            // Hook members
            for (Member member : listMember)
                try {
                    XposedBridge.hookMethod(member, new XLocationMethodHook(hook));
                } catch (NoSuchFieldError ex) {
                    Log.i(LOGTAG, ex.toString());
                } catch (Throwable ex) {
                    Log.i(LOGTAG, ex.toString());
                }

            // Check if members found
            if (listMember.isEmpty() && !hook.getClassName().startsWith("com.google.android.gms")) {
                String message = "Method not found hook=" + hook;
                Log.e(LOGTAG, message);
            }
            Log.i("GPSChinaOffsetFix", "Wyszlo: " + hook.getClassName());
        }  catch (Throwable ignored) {
            Log.i("GPSChinaOffsetFix", "Nie wyszlo: " + hook.getClassName());
        }

    }

    private static class XLocationMethodHook extends XC_MethodHook {

        private XHookLocation mHook;

        public XLocationMethodHook(XHookLocation hook) {
            mHook = hook;
        }

        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            try {
                Log.i("GPSChinaOffsetFix", "W hooku before: " + mHook.getMethodName());
                mHook.before(param);
            } catch (Throwable ex) {
                Log.i("GPSChinaOffsetFix", "Wyjatek");
            }
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            if (!param.hasThrowable())
                try {
                    Log.i("GPSChinaOffsetFix", "W hooku after: " + mHook.getMethodName());
                    mHook.after(param);
                } catch (Throwable ex) {
                    Log.i("GPSChinaOffsetFix", "Wyjatek");
                }
        }
    }
}