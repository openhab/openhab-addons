/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.snapcast.internal.data;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Client} is a data structure for the api communication.
 * It represents a snapcast client.
 *
 * @author Steffen Brandemann - Initial contribution
 */
public class Client implements Identifiable {

    @SerializedName("id")
    private String id;

    @SerializedName("config")
    private ClientConfig config;

    @SerializedName("connected")
    private Boolean connected;

    @SerializedName("host")
    private Host host;

    @SerializedName("lastSeen")
    private LastSeen lastSeen;

    @SerializedName("snapclient")
    private Snapclient snapclient;

    public Client() {
    }

    /**
     * @return the id
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    @Override
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the config
     */
    public ClientConfig getConfig() {
        return config;
    }

    /**
     * @param config the config to set
     */
    public void setConfig(ClientConfig config) {
        this.config = config;
    }

    /**
     * @return the connected
     */
    public Boolean getConnected() {
        return connected;
    }

    /**
     * @param connected the connected to set
     */
    public void setConnected(Boolean connected) {
        this.connected = connected;
    }

    /**
     * @return the host
     */
    public Host getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(Host host) {
        this.host = host;
    }

    /**
     * @return the lastSeen
     */
    public LastSeen getLastSeen() {
        return lastSeen;
    }

    /**
     * @param lastSeen the lastSeen to set
     */
    public void setLastSeen(LastSeen lastSeen) {
        this.lastSeen = lastSeen;
    }

    /**
     * @return the snapclient
     */
    public Snapclient getSnapclient() {
        return snapclient;
    }

    /**
     * @param snapclient the snapclient to set
     */
    public void setSnapclient(Snapclient snapclient) {
        this.snapclient = snapclient;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((config == null) ? 0 : config.hashCode());
        result = prime * result + ((connected == null) ? 0 : connected.hashCode());
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((lastSeen == null) ? 0 : lastSeen.hashCode());
        result = prime * result + ((snapclient == null) ? 0 : snapclient.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Client other = (Client) obj;
        if (config == null) {
            if (other.config != null) {
                return false;
            }
        } else if (!config.equals(other.config)) {
            return false;
        }
        if (connected == null) {
            if (other.connected != null) {
                return false;
            }
        } else if (!connected.equals(other.connected)) {
            return false;
        }
        if (host == null) {
            if (other.host != null) {
                return false;
            }
        } else if (!host.equals(other.host)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (lastSeen == null) {
            if (other.lastSeen != null) {
                return false;
            }
        } else if (!lastSeen.equals(other.lastSeen)) {
            return false;
        }
        if (snapclient == null) {
            if (other.snapclient != null) {
                return false;
            }
        } else if (!snapclient.equals(other.snapclient)) {
            return false;
        }
        return true;
    }

    /**
     * The {@link ClientConfig} is a data structure for the api communication.
     * It represents the configurable parameters of a snapcast client.
     *
     * @author Steffen Brandemann - Initial contribution
     */
    public static class ClientConfig implements Identifiable {

        @SerializedName("id")
        private String id;

        @SerializedName("name")
        private String name;

        @SerializedName("volume")
        private Volume volume;

        @SerializedName("instance")
        private Integer instance;

        @SerializedName("latency")
        private Integer latency;

        public ClientConfig() {
        }

        /**
         * @return the id
         */
        @Override
        public String getId() {
            return id;
        }

        /**
         * @param id the id to set
         */
        @Override
        public void setId(String id) {
            this.id = id;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return the volume
         */
        public Volume getVolume() {
            return volume;
        }

        /**
         * @param volume the volume to set
         */
        public void setVolume(Volume volume) {
            this.volume = volume;
        }

        /**
         * @return the instance
         */
        public Integer getInstance() {
            return instance;
        }

        /**
         * @param instance the instance to set
         */
        public void setInstance(Integer instance) {
            this.instance = instance;
        }

        /**
         * @return the latency
         */
        public Integer getLatency() {
            return latency;
        }

        /**
         * @param latency the latency to set
         */
        public void setLatency(Integer latency) {
            this.latency = latency;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            result = prime * result + ((instance == null) ? 0 : instance.hashCode());
            result = prime * result + ((latency == null) ? 0 : latency.hashCode());
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((volume == null) ? 0 : volume.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ClientConfig other = (ClientConfig) obj;
            if (id == null) {
                if (other.id != null) {
                    return false;
                }
            } else if (!id.equals(other.id)) {
                return false;
            }
            if (instance == null) {
                if (other.instance != null) {
                    return false;
                }
            } else if (!instance.equals(other.instance)) {
                return false;
            }
            if (latency == null) {
                if (other.latency != null) {
                    return false;
                }
            } else if (!latency.equals(other.latency)) {
                return false;
            }
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            if (volume == null) {
                if (other.volume != null) {
                    return false;
                }
            } else if (!volume.equals(other.volume)) {
                return false;
            }
            return true;
        }

    }

    /**
     * The {@link Volume} is a data structure for the api communication.
     * It represents the volume of a snapcast client.
     *
     * @author Steffen Brandemann - Initial contribution
     */
    public static class Volume {

        @SerializedName("percent")
        private Integer percent;

        @SerializedName("muted")
        private Boolean muted;

        public Volume() {
        }

        public Volume(Integer percent, Boolean muted) {
            this();
            setPercent(percent);
            setMuted(muted);
        }

        /**
         * @return the percent
         */
        public Integer getPercent() {
            return percent;
        }

        /**
         * @param percent the percent to set
         */
        public void setPercent(Integer percent) {
            this.percent = percent;
        }

        /**
         * @return the muted
         */
        public Boolean getMuted() {
            return muted;
        }

        /**
         * @param muted the muted to set
         */
        public void setMuted(Boolean muted) {
            this.muted = muted;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((muted == null) ? 0 : muted.hashCode());
            result = prime * result + ((percent == null) ? 0 : percent.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Volume other = (Volume) obj;
            if (muted == null) {
                if (other.muted != null) {
                    return false;
                }
            } else if (!muted.equals(other.muted)) {
                return false;
            }
            if (percent == null) {
                if (other.percent != null) {
                    return false;
                }
            } else if (!percent.equals(other.percent)) {
                return false;
            }
            return true;
        }

    }

    /**
     * The {@link LastSeen} is a data structure for the api communication.
     * It provides last seen informations.
     *
     * @author Steffen Brandemann - Initial contribution
     */
    public static class LastSeen {

        @SerializedName("sec")
        private Long sec;

        @SerializedName("usec")
        private Long usec;

        public LastSeen() {
        }

        /**
         * @return the sec
         */
        public Long getSec() {
            return sec;
        }

        /**
         * @param sec the sec to set
         */
        public void setSec(Long sec) {
            this.sec = sec;
        }

        /**
         * @return the usec
         */
        public Long getUsec() {
            return usec;
        }

        /**
         * @param usec the usec to set
         */
        public void setUsec(Long usec) {
            this.usec = usec;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((sec == null) ? 0 : sec.hashCode());
            result = prime * result + ((usec == null) ? 0 : usec.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            LastSeen other = (LastSeen) obj;
            if (sec == null) {
                if (other.sec != null) {
                    return false;
                }
            } else if (!sec.equals(other.sec)) {
                return false;
            }
            if (usec == null) {
                if (other.usec != null) {
                    return false;
                }
            } else if (!usec.equals(other.usec)) {
                return false;
            }
            return true;
        }

    }

    /**
     * The {@link Snapclient} is a data structure for the api communication.
     * It provides information about the clients software.
     *
     * @author Steffen Brandemann - Initial contribution
     */
    public static class Snapclient {

        @SerializedName("name")
        private String name;

        @SerializedName("version")
        private String version;

        @SerializedName("protocolVersion")
        private Integer protocolVersion;

        public Snapclient() {
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return the version
         */
        public String getVersion() {
            return version;
        }

        /**
         * @param version the version to set
         */
        public void setVersion(String version) {
            this.version = version;
        }

        /**
         * @return the protocolVersion
         */
        public Integer getProtocolVersion() {
            return protocolVersion;
        }

        /**
         * @param protocolVersion the protocolVersion to set
         */
        public void setProtocolVersion(Integer protocolVersion) {
            this.protocolVersion = protocolVersion;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((protocolVersion == null) ? 0 : protocolVersion.hashCode());
            result = prime * result + ((version == null) ? 0 : version.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Snapclient other = (Snapclient) obj;
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            if (protocolVersion == null) {
                if (other.protocolVersion != null) {
                    return false;
                }
            } else if (!protocolVersion.equals(other.protocolVersion)) {
                return false;
            }
            if (version == null) {
                if (other.version != null) {
                    return false;
                }
            } else if (!version.equals(other.version)) {
                return false;
            }
            return true;
        }

    }

}
