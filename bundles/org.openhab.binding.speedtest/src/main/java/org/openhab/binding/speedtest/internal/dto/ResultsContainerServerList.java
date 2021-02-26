package org.openhab.binding.speedtest.internal.dto;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResultsContainerServerList {
    @SerializedName("type")
    @Expose
    public String type;
    @SerializedName("timestamp")
    @Expose
    public String timestamp;
    @SerializedName("servers")
    @Expose
    public List<Server> servers = null;

    public class Server {

        @SerializedName("id")
        @Expose
        public Integer id;
        @SerializedName("name")
        @Expose
        public String name;
        @SerializedName("location")
        @Expose
        public String location;
        @SerializedName("country")
        @Expose
        public String country;
        @SerializedName("host")
        @Expose
        public String host;
        @SerializedName("port")
        @Expose
        public Integer port;

    }
}
