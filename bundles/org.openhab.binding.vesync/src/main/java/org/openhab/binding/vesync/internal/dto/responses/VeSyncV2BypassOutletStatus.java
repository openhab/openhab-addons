package org.openhab.binding.vesync.internal.dto.responses;

import com.google.gson.annotations.SerializedName;

public class VeSyncV2BypassOutletStatus extends VeSyncResponse {

    @SerializedName("result")
    public OutletStatusResult outletResult;

    public class OutletStatusResult extends VeSyncResponse {

        @SerializedName("module")
        public Object object = null;

        @SerializedName("stacktrace")
        public Object object2 = null;

        @SerializedName("result")
        public Result result = new Result();

        public class Result {

            @SerializedName("enabled")
            public boolean enabled = false;

            @SerializedName("voltage")
            public double voltage = 0.00;

            @SerializedName("energy")
            public double energy = 0.00;

            @SerializedName("power")
            public double power = 0.00;

            @SerializedName("current")
            public double current = 0.00;

            @SerializedName("highestVoltage")
            public int highestVoltage = 0;

            @SerializedName("voltagePTStatus")
            public boolean voltagePTStatus = false;

            public String getDeviceStatus() {
                return enabled ? "on" : "off";
            }
        }
    }
}
