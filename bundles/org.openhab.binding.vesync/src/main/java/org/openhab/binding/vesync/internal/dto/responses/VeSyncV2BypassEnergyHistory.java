package org.openhab.binding.vesync.internal.dto.responses;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class VeSyncV2BypassEnergyHistory extends VeSyncResponse {

    @SerializedName("result")
    public EnergyHistory result;

    public class EnergyHistory extends VeSyncResponse {

        @SerializedName("result")
        public Result result = new Result();

        public class Result {

            @SerializedName("energyInfos")
            public List<EnergyInfo> energyInfos = new ArrayList<EnergyInfo>();

            public class EnergyInfo {

                @SerializedName("timestamp")
                public long timestamp = 0;

                @SerializedName("energy")
                public double energy = 0.00;
            }

            @SerializedName("total")
            public int total = 0;
        }
    }
}
