package pl.skritland.xposed.gpsoffsetfix;

import android.location.Location;

/**
 * Created by multipit on 2015-03-19.
 * Used only for debug and testing.
 */
public class TestLocationTransform implements LocationTransform {
    @Override
    public Location transform(Location loc) {
        Location outloc = new Location(loc);
        outloc.setAltitude(123.0);
        outloc.setLatitude(loc.getLatitude() + 0.01);
        return outloc;
    }
}
