/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.snapcast.internal.data;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Server} is a data structure for the api communication.
 * It represents a complete snapcast server with all groups, clients and streams.
 *
 * @author Steffen Brandemann - Initial contribution
 */
public class Server {

    @SerializedName("server")
    private ServerAttributes server;

    @SerializedName("streams")
    private ArrayList<Stream> streams;

    @SerializedName("groups")
    private ArrayList<Group> groups;

    public Server() {
    }

    /**
     * @return the server
     */
    public ServerAttributes getServer() {
        return server;
    }

    /**
     * @param server the server to set
     */
    public void setServer(ServerAttributes server) {
        this.server = server;
    }

    /**
     * @return the streams
     */
    public ArrayList<Stream> getStreams() {
        return streams;
    }

    /**
     * @param streams the streams to set
     */
    public void setStreams(ArrayList<Stream> streams) {
        this.streams = streams;
    }

    /**
     * @return the groups
     */
    public ArrayList<Group> getGroups() {
        return groups;
    }

    /**
     * @param groups the groups to set
     */
    public void setGroups(ArrayList<Group> groups) {
        this.groups = groups;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((groups == null) ? 0 : groups.hashCode());
        result = prime * result + ((server == null) ? 0 : server.hashCode());
        result = prime * result + ((streams == null) ? 0 : streams.hashCode());
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
        Server other = (Server) obj;
        if (groups == null) {
            if (other.groups != null) {
                return false;
            }
        } else if (!groups.equals(other.groups)) {
            return false;
        }
        if (server == null) {
            if (other.server != null) {
                return false;
            }
        } else if (!server.equals(other.server)) {
            return false;
        }
        if (streams == null) {
            if (other.streams != null) {
                return false;
            }
        } else if (!streams.equals(other.streams)) {
            return false;
        }
        return true;
    }

    /**
     * The {@link ServerAttributes} is a data structure for the api communication.
     * It represents some information about the server itself.
     *
     * @author Steffen Brandemann - Initial contribution
     */
    public static class ServerAttributes implements Identifiable {

        @SerializedName("host")
        private Host host;

        @SerializedName("snapserver")
        private Snapserver snapserver;

        public ServerAttributes() {
        }

        @Override
        public String getId() {
            return null;
        }

        @Override
        public void setId(String id) {
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
         * @return the snapserver
         */
        public Snapserver getSnapserver() {
            return snapserver;
        }

        /**
         * @param snapserver the snapserver to set
         */
        public void setSnapserver(Snapserver snapserver) {
            this.snapserver = snapserver;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((host == null) ? 0 : host.hashCode());
            result = prime * result + ((snapserver == null) ? 0 : snapserver.hashCode());
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
            ServerAttributes other = (ServerAttributes) obj;
            if (host == null) {
                if (other.host != null) {
                    return false;
                }
            } else if (!host.equals(other.host)) {
                return false;
            }
            if (snapserver == null) {
                if (other.snapserver != null) {
                    return false;
                }
            } else if (!snapserver.equals(other.snapserver)) {
                return false;
            }
            return true;
        }

    }

    /**
     * The {@link Snapserver} is a data structure for the api communication.
     * It provides information about the server software.
     *
     * @author Steffen Brandemann - Initial contribution
     */
    public static class Snapserver {

        @SerializedName("name")
        private String name;

        @SerializedName("version")
        private String version;

        @SerializedName("protocolVersion")
        private Integer protocolVersion;

        @SerializedName("controlProtocolVersion")
        private Integer controlProtocolVersion;

        public Snapserver() {
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

        /**
         * @return the controlProtocolVersion
         */
        public Integer getControlProtocolVersion() {
            return controlProtocolVersion;
        }

        /**
         * @param controlProtocolVersion the controlProtocolVersion to set
         */
        public void setControlProtocolVersion(Integer controlProtocolVersion) {
            this.controlProtocolVersion = controlProtocolVersion;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((controlProtocolVersion == null) ? 0 : controlProtocolVersion.hashCode());
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
            Snapserver other = (Snapserver) obj;
            if (controlProtocolVersion == null) {
                if (other.controlProtocolVersion != null) {
                    return false;
                }
            } else if (!controlProtocolVersion.equals(other.controlProtocolVersion)) {
                return false;
            }
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
