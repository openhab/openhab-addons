package org.openhab.binding.zoneminder.internal.api;

import java.util.ArrayList;
import java.util.List;

public class ServerCpuLoad extends ZoneMinderApiData {
    private List<Double> load = new ArrayList<Double>();

    /*
     * public ServerCpuLoad(String strCpuLoad_1, String strCpuLoad_2, String strCpuLoad_3) {
     * cpuLoad_1 = Float.parseFloat(strCpuLoad_1);
     * cpuLoad_2 = Float.parseFloat(strCpuLoad_2);
     * cpuLoad_3 = Float.parseFloat(strCpuLoad_3);
     * }
     */
    public Double getCpuLoad() {
        if (load.size() > 0) {
            return load.get(0);
        }
        return null;
    }
}
