package pl.skritland.xposed.gpsoffsetfix;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

/**
 * Created by skritland on 2015-03-15.
 * Idea based on XPrivacy
 */
public interface XHookLocation {

    public String getClassName();

    /**
     * Get hooked method name.
     *
     * @return hooked method name
     */
    public String getMethodName();

    /**
     * Called before hooked method.
     *
     * @param param
     * @throws Throwable
     */
    public void before(MethodHookParam param) throws Throwable;

    /**
     * Called after hooked method.
     *
     * @param param
     * @throws Throwable
     */
    public void after(MethodHookParam param) throws Throwable;

}
