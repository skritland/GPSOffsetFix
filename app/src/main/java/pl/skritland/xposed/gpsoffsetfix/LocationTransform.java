package pl.skritland.xposed.gpsoffsetfix;

import android.location.Location;

/**
 * Created by skritland on 2015-03-15.
 */
public interface LocationTransform {
    /**
     * Returns transformed location.
     * @param loc Original location
     * @return Transformed location
     */
    public Location transform(Location loc);
}
