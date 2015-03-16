package pl.skritland.xposed.gpschinaoffsetfix;

import android.app.PendingIntent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.IInterface;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

/**
 * Created by skritland on 2015-03-15.
 * Idea based on XPrivacy
 */
public class XHookLocation {
    private MethodsToHook mMethod;
    private String mClassName;
    public static final String LOGTAG = GPSChinaOffsetFix.LOGTAG + "/XHL";
    private static final String cLocationManagerClassName = "android.location.LocationManager";
    private static final String cLocationClientClassName = "com.google.android.gms.location.LocationClient";
    private static final String cGoogleMapClassName = "com.google.android.gms.maps.GoogleMap";
    private static final Map<Object, Object> mMapProxy = new WeakHashMap<Object, Object>();

    private static final LocationTransform mLocationTransform = new GPSCoordEvilTransform();

    private XHookLocation(MethodsToHook method, String className) {
        mMethod = method;
        mClassName = className;
    }

    public String getClassName() {
        return mClassName;
    }

    public String getMethodName() {
        return mMethod.name();
    }

    // @formatter:off

    // public Location getLastKnownLocation(String provider)
    // public void removeUpdates(LocationListener listener)
    // public void removeUpdates(PendingIntent intent)
    // public void requestLocationUpdates(String provider, long minTime, float minDistance, LocationListener listener)
    // public void requestLocationUpdates(String provider, long minTime, float minDistance, LocationListener listener, Looper looper)
    // public void requestLocationUpdates(long minTime, float minDistance, Criteria criteria, LocationListener listener, Looper looper)
    // public void requestLocationUpdates(String provider, long minTime, float minDistance, PendingIntent intent)
    // public void requestLocationUpdates(long minTime, float minDistance, Criteria criteria, PendingIntent intent)
    // public void requestSingleUpdate(String provider, LocationListener listener, Looper looper)
    // public void requestSingleUpdate(Criteria criteria, LocationListener listener, Looper looper)
    // public void requestSingleUpdate(String provider, PendingIntent intent)
    // public void requestSingleUpdate(Criteria criteria, PendingIntent intent)
    // frameworks/base/location/java/android/location/LocationManager.java
    // http://developer.android.com/reference/android/location/LocationManager.html


    // @formatter:on

    private enum MethodsToHook {
        getLastKnownLocation,
        removeUpdates,
        requestLocationUpdates, requestSingleUpdate,
    }


    public static List<XHookLocation> getInstances(String module, String className) {
        List<XHookLocation> listHook = new ArrayList<XHookLocation>();
        if (module.equals("LocationManager"))
        {
            if (className == null)
                className = cLocationManagerClassName;
            listHook.add(new XHookLocation(MethodsToHook.getLastKnownLocation, className));
            listHook.add(new XHookLocation(MethodsToHook.removeUpdates, className));
            listHook.add(new XHookLocation(MethodsToHook.requestLocationUpdates, className));
            listHook.add(new XHookLocation(MethodsToHook.requestSingleUpdate, className));
        }
        else if (module.equals("LocationClient"))
        {

        }
        else if (module.equals("GoogleMapsApiV2"))
        {

        }
        else if (module.equals("FusedLocationApi"))
        {

        }

        return listHook;
    }

    protected void before(MethodHookParam param) throws Throwable {
        switch (mMethod) {
            case removeUpdates:
                unproxyLocationListener(param, 0);
                break;

            case requestLocationUpdates:
                proxyLocationListener(param, 3);
                break;

            case requestSingleUpdate:
                proxyLocationListener(param, 1);
                break;

            default:
                break;

        }
    }

    protected void after(MethodHookParam param) throws Throwable {
        switch (mMethod) {
            case getLastKnownLocation:
                if (param.args.length > 0 && param.getResult() instanceof Location) {
                    Location location = (Location) param.getResult();
                    param.setResult(mLocationTransform.transform(location));
                }
                break;

            default:
                break;
        }
    }

    private void proxyLocationListener(MethodHookParam param, int arg) throws Throwable {
        Log.i(LOGTAG, "proxyLocationListener: Inside func");
        if (param.args.length > arg)
            if (param.args[arg] instanceof PendingIntent)
                Log.i(LOGTAG, "proxyLocationListener: PendingIntent not supported");

            else if (param.args[arg] != null) {

                    Object key = param.args[arg];
                    synchronized (mMapProxy) {
                        // Reuse existing proxy
                        if (mMapProxy.containsKey(key)) {
                            Log.i(LOGTAG, "Reuse existing proxy uid=" + Binder.getCallingUid());
                            param.args[arg] = mMapProxy.get(key);
                            return;
                        }

                        // Already proxied
                        if (mMapProxy.containsValue(key)) {
                            Log.i(LOGTAG, "Already proxied uid=" + Binder.getCallingUid());
                            return;
                        }
                    }

                    // Create proxy
                    Log.i(LOGTAG, "Creating proxy uid=" + Binder.getCallingUid());
                    Object proxy = new ProxyLocationListener(Binder.getCallingUid(), (LocationListener) param.args[arg]);

                    // Use proxy
                    synchronized (mMapProxy) {
                        mMapProxy.put(key, proxy);
                    }
                    param.args[arg] = proxy;

            }
    }

    private void unproxyLocationListener(MethodHookParam param, int arg) {
        if (param.args.length > arg)
            if (param.args[arg] instanceof PendingIntent)
                Log.i(LOGTAG, "unproxyLocationListener: PendingIntent not supported");

            else if (param.args[arg] != null) {
                    Object key = param.args[arg];
                    synchronized (mMapProxy) {
                        if (mMapProxy.containsKey(key)) {
                            Log.i(LOGTAG, "Removing proxy uid=" + Binder.getCallingUid());
                            param.args[arg] = mMapProxy.get(key);
                        }
                    }
            }
    }

    private static class ProxyLocationListener implements LocationListener {
        private int mUid;
        private LocationListener mListener;

        public ProxyLocationListener(int uid, LocationListener listener) {
            mUid = uid;
            mListener = listener;
        }

        @Override
        public void onLocationChanged(Location location) {
            Location transformedLoc = mLocationTransform.transform(location);
            transformedLoc.setAltitude(99.0);
            Log.i(LOGTAG, "Location changed uid=" + Binder.getCallingUid() + " alt: " + transformedLoc.getAltitude());
            mListener.onLocationChanged(transformedLoc);
        }

        @Override
        public void onProviderDisabled(String provider) {
            mListener.onProviderDisabled(provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            mListener.onProviderEnabled(provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            mListener.onStatusChanged(provider, status, extras);
        }
    }
}
