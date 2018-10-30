/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.snapcast.internal.data;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Group} is a data structure for the api communication.
 * It represents a snapcast group.
 *
 * @author Steffen Brandemann - Initial contribution
 */
public class Group implements Identifiable {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("stream_id")
    private String stream;

    @SerializedName("mute")
    private Boolean mute;

    @SerializedName("clients")
    private List<Client> clients;

    public Group() {
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
     * @return the stream
     */
    public String getStream() {
        return stream;
    }

    /**
     * @param stream the stream to set
     */
    public void setStream(String stream) {
        this.stream = stream;
    }

    /**
     * @return the mute
     */
    public Boolean getMute() {
        return mute;
    }

    /**
     * @param mute the mute to set
     */
    public void setMute(Boolean mute) {
        this.mute = mute;
    }

    /**
     * @return the clients
     */
    public List<Client> getClients() {
        return clients;
    }

    /**
     * @param clients the clients to set
     */
    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((clients == null) ? 0 : clients.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((mute == null) ? 0 : mute.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((stream == null) ? 0 : stream.hashCode());
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
        Group other = (Group) obj;
        if (clients == null) {
            if (other.clients != null) {
                return false;
            }
        } else if (!clients.equals(other.clients)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (mute == null) {
            if (other.mute != null) {
                return false;
            }
        } else if (!mute.equals(other.mute)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (stream == null) {
            if (other.stream != null) {
                return false;
            }
        } else if (!stream.equals(other.stream)) {
            return false;
        }
        return true;
    }

    /**
     * The {@link GroupClients} is a data structure for the api communication.
     * It contains a list of clients
     *
     * @author Steffen Brandemann - Initial contribution
     */
    public static class GroupClients implements Identifiable {

        @SerializedName("id")
        private String id;

        @SerializedName("clients")
        private List<String> clients;

        public GroupClients() {
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
         * @return the clients
         */
        public List<String> getClients() {
            return clients;
        }

        /**
         * @param clients the clients to set
         */
        public void setClients(List<String> clients) {
            this.clients = clients;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((clients == null) ? 0 : clients.hashCode());
            result = prime * result + ((id == null) ? 0 : id.hashCode());
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
            GroupClients other = (GroupClients) obj;
            if (clients == null) {
                if (other.clients != null) {
                    return false;
                }
            } else if (!clients.equals(other.clients)) {
                return false;
            }
            if (id == null) {
                if (other.id != null) {
                    return false;
                }
            } else if (!id.equals(other.id)) {
                return false;
            }
            return true;
        }

    }

}
