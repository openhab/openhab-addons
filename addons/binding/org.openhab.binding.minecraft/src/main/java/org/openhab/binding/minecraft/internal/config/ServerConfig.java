/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.minecraft.internal.config;

/**
 * Configuration settings for a {@link org.openhab.binding.minecraft.handler.MinecraftServerHandler}.
 *
 * @author Mattias Markehed
 */
public class ServerConfig {
    private int port = 10692;
    private String hostname = "127.0.0.1";

    /**
     * Get port used to connect to server.
     *
     * @return server port
     */
    public int getPort() {
        return port;
    }

    /**
     * Set the port used to connect to server.
     *
     * @param port server port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Get host used to connect to server.
     *
     * @return server host
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Set the host used to connect to server.
     *
     * @param port host port
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
}
