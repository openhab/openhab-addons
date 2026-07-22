package org.openhab.binding.transitapp.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class TransitAppStopConfiguration {
    public String globalStopId = "";
    public long time = 0;
    public boolean removeCancelled = false;
    public String locale = "en";
    public boolean shouldUpdateRealtime = true;
    public int maxNumDepartures = 3;
    public boolean includeStopsAndShapes = false;
    public boolean stopDetailed = false;
}
