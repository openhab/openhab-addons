/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.speedtest.internal.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link ResultContainer} class defines a container for Speedtest results.
 *
 * @author Brian Homeyer - Initial contribution
 */
public class ResultContainer {

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("timestamp")
    @Expose
    private String timestamp;
    @SerializedName("ping")
    @Expose
    private Ping ping;
    @SerializedName("download")
    @Expose
    private Download download;
    @SerializedName("upload")
    @Expose
    private Upload upload;
    @SerializedName("packetLoss")
    @Expose
    private Double packetLoss;
    @SerializedName("isp")
    @Expose
    private String isp;
    @SerializedName("interface")
    @Expose
    private Interface networkInterface;
    @SerializedName("server")
    @Expose
    private Server server;
    @SerializedName("result")
    @Expose
    private Result result;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Ping getPing() {
        return ping;
    }

    public void setPing(Ping ping) {
        this.ping = ping;
    }

    public Download getDownload() {
        return download;
    }

    public void setDownload(Download download) {
        this.download = download;
    }

    public Upload getUpload() {
        return upload;
    }

    public void setUpload(Upload upload) {
        this.upload = upload;
    }

    public Double getPacketLoss() {
        return packetLoss;
    }

    public void setPacketLoss(Double packetLoss) {
        this.packetLoss = packetLoss;
    }

    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    public Interface getInterface() {
        return networkInterface;
    }

    public void setInterface(Interface networkInterface) {
        this.networkInterface = networkInterface;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public class Download {

        @SerializedName("bandwidth")
        @Expose
        private String bandwidth;
        @SerializedName("bytes")
        @Expose
        private String bytes;
        @SerializedName("elapsed")
        @Expose
        private String elapsed;

        public String getBandwidth() {
            return bandwidth;
        }

        public void setBandwidth(String bandwidth) {
            this.bandwidth = bandwidth;
        }

        public String getBytes() {
            return bytes;
        }

        public void setBytes(String bytes) {
            this.bytes = bytes;
        }

        public String getElapsed() {
            return elapsed;
        }

        public void setElapsed(String elapsed) {
            this.elapsed = elapsed;
        }
    }

    public class Interface {

        @SerializedName("internalIp")
        @Expose
        private String internalIp;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("macAddr")
        @Expose
        private String macAddr;
        @SerializedName("isVpn")
        @Expose
        private Boolean isVpn;
        @SerializedName("externalIp")
        @Expose
        private String externalIp;

        public String getInternalIp() {
            return internalIp;
        }

        public void setInternalIp(String internalIp) {
            this.internalIp = internalIp;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMacAddr() {
            return macAddr;
        }

        public void setMacAddr(String macAddr) {
            this.macAddr = macAddr;
        }

        public Boolean getIsVpn() {
            return isVpn;
        }

        public void setIsVpn(Boolean isVpn) {
            this.isVpn = isVpn;
        }

        public String getExternalIp() {
            return externalIp;
        }

        public void setExternalIp(String externalIp) {
            this.externalIp = externalIp;
        }
    }

    public class Ping {

        @SerializedName("jitter")
        @Expose
        private String jitter;
        @SerializedName("latency")
        @Expose
        private String latency;

        public String getJitter() {
            return jitter;
        }

        public void setJitter(String jitter) {
            this.jitter = jitter;
        }

        public String getLatency() {
            return latency;
        }

        public void setLatency(String latency) {
            this.latency = latency;
        }
    }

    public class Result {

        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("url")
        @Expose
        private String url;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public class Server {

        @SerializedName("id")
        @Expose
        private Integer id;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("location")
        @Expose
        private String location;
        @SerializedName("country")
        @Expose
        private String country;
        @SerializedName("host")
        @Expose
        private String host;
        @SerializedName("port")
        @Expose
        private Integer port;
        @SerializedName("ip")
        @Expose
        private String ip;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }
    }

    public class Upload {

        @SerializedName("bandwidth")
        @Expose
        private String bandwidth;
        @SerializedName("bytes")
        @Expose
        private String bytes;
        @SerializedName("elapsed")
        @Expose
        private String elapsed;

        public String getBandwidth() {
            return bandwidth;
        }

        public void setBandwidth(String bandwidth) {
            this.bandwidth = bandwidth;
        }

        public String getBytes() {
            return bytes;
        }

        public void setBytes(String bytes) {
            this.bytes = bytes;
        }

        public String getElapsed() {
            return elapsed;
        }

        public void setElapsed(String elapsed) {
            this.elapsed = elapsed;
        }
    }
}
