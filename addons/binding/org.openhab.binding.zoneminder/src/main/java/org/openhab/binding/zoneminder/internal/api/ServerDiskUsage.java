package org.openhab.binding.zoneminder.internal.api;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class ServerDiskUsage extends ZoneMinderApiData {

    private String space;
    private String color;

    public String getDiskUsage() {
        DecimalFormat df = new DecimalFormat("#.##");
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.ROOT);
        df.setDecimalFormatSymbols(dfs);
        return df.format(Double.parseDouble(space));

    }
}
