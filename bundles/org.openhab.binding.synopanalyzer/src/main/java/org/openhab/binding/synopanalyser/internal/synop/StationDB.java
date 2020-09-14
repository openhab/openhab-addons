package org.openhab.binding.synopanalyser.internal.synop;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class StationDB {
    public class Station {
        public String pack;
        @SerializedName("id_omm")
        public long idOmm;
        @SerializedName("numer_sta")
        public long numerSta;
        @SerializedName("usual_name")
        public String usualName;
        public double latitude;
        public double longitude;
        public double elevation;
        @SerializedName("station_type")
        public int stationType;
    }

    public List<Station> stations;
}
