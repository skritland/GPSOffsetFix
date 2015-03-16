package pl.skritland.xposed.gpschinaoffsetfix;

import android.location.Location;

/**
 * Created by skritland on 2015-03-15.
 */
public interface LocationTransform {
    public Location transform(Location loc);
}
