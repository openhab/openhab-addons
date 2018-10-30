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
 * The {@link Params} is a data structure for the api communication.
 * It is used to provide the diffrent message parameters.
 *
 * @author Steffen Brandemann - Initial contribution
 */
public class Params implements Identifiable {

    @SerializedName("id")
    private String id;

    @SerializedName("client")
    private Client client;

    @SerializedName("group")
    private Group group;

    @SerializedName("server")
    private Server server;

    @SerializedName("stream")
    private Stream stream;

    public Params() {
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
     * @return the client
     */
    public Client getClient() {
        return client;
    }

    /**
     * @param client the client to set
     */
    public void setClient(Client client) {
        this.client = client;
    }

    /**
     * @return the group
     */
    public Group getGroup() {
        return group;
    }

    /**
     * @param group the group to set
     */
    public void setGroup(Group group) {
        this.group = group;
    }

    /**
     * @return the server
     */
    public Server getServer() {
        return server;
    }

    /**
     * @param server the server to set
     */
    public void setServer(Server server) {
        this.server = server;
    }

    /**
     * @return the stream
     */
    public Stream getStream() {
        return stream;
    }

    /**
     * @param stream the stream to set
     */
    public void setStream(Stream stream) {
        this.stream = stream;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((client == null) ? 0 : client.hashCode());
        result = prime * result + ((group == null) ? 0 : group.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((server == null) ? 0 : server.hashCode());
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
        Params other = (Params) obj;
        if (client == null) {
            if (other.client != null) {
                return false;
            }
        } else if (!client.equals(other.client)) {
            return false;
        }
        if (group == null) {
            if (other.group != null) {
                return false;
            }
        } else if (!group.equals(other.group)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (server == null) {
            if (other.server != null) {
                return false;
            }
        } else if (!server.equals(other.server)) {
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

}
