package pl.skritland.xposed.gpsoffsetfix;

/**
 * Created by skritland on 2015-03-09.
 */

import android.util.Log;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class GPSOffsetFix implements IXposedHookLoadPackage {

    public static final String LOGTAG = GPSOffsetFix.class.getName();

    @Override
    public void handleLoadPackage(final LoadPackageParam loadPackageParam) throws Throwable {

        String self = GPSOffsetFix.class.getPackage().getName();
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
            hookAll(XLocationManager.getInstances(null), loadPackageParam.classLoader);
        } catch (Throwable ignored) {
            Log.w(LOGTAG, "LocationManager not used for: " + loadPackageParam.packageName + " in " + loadPackageParam.processName);
        }
    }

    public static void hookAll(List<XHookLocation> listHook, ClassLoader classLoader) {
        for (XHookLocation hook : listHook)
                hook(hook, classLoader);
    }

    private static void hook(final XHookLocation hook, ClassLoader classLoader) {
        Class<?> hookClass = null;
//        Log.d(LOGTAG, "Hooking: " + hook.getMethodName() + " in " + hook.getClassName());
        try {
            hookClass = findClass(hook.getClassName(), classLoader);
            // get members (also from superclass(es))
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

            // hook members
            for (Member member : listMember)
                try {
                    XposedBridge.hookMethod(member, new XLocationMethodHook(hook));
                } catch (NoSuchFieldError ex) {
                    Log.e(LOGTAG, ex.toString());
                } catch (Throwable ex) {
                    Log.e(LOGTAG, ex.toString());
                }

            // check if members found
            if (listMember.isEmpty() && !hook.getClassName().startsWith("com.google.android.gms")) {
                Log.e(LOGTAG, "Method for hook not found: " + hook.getMethodName());
            }
            //Log.v(LOGTAG, "Hooking OK: " + hook.getClassName() + " in " + hook.getClassName());
        }  catch (Throwable ignored) {
            Log.e(LOGTAG, "Error with hooking: " + hook.getMethodName() + " in " + hook.getClassName(), ignored);
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
                //Log.v(LOGTAG, "In hook before: " + mHook.getMethodName() + " in " + mHook.getClassName());
                mHook.before(param);
            } catch (Throwable ex) {
                Log.e(LOGTAG, "Exception in: " + mHook.getMethodName() + " in " + mHook.getClassName(), ex);
            }
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            if (!param.hasThrowable())
                try {
                    //Log.v(LOGTAG, "In hook after: " + mHook.getMethodName() + " in " + mHook.getClassName());
                    mHook.after(param);
                } catch (Throwable ex) {
                    Log.e(LOGTAG, "Exception in: " + mHook.getMethodName() + " in " + mHook.getClassName(), ex);
                }
        }
    }
}
