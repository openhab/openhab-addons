/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb.service;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The class will represent a command request deserialized from a webpage. A command request is basically a
 * ScalarWebRequest to run against a sony device
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class CommandRequest {
    /** The base URL to run the command against */
    private @Nullable String baseUrl;

    /** The service to run against */
    private @Nullable String serviceName;

    /** The transport name to use */
    private @Nullable String transport;

    /** The command to run */
    private @Nullable String command;

    /** The version of the command to use */
    private @Nullable String version;

    /** Any parameters to use */
    private @Nullable String parms;

    /**
     * Empty constructor used for deserialization
     */
    public CommandRequest() {
    }

    /**
     * Gets the base URL
     * 
     * @return the base URL
     */
    public @Nullable String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Gets the service name
     * 
     * @return the service name
     */
    public @Nullable String getServiceName() {
        return serviceName;
    }

    /**
     * Gets the transport
     * 
     * @return the transport
     */
    public @Nullable String getTransport() {
        return transport;
    }

    /**
     * Gets the command
     * 
     * @return the command
     */
    public @Nullable String getCommand() {
        return command;
    }

    /**
     * Gets the command version
     * 
     * @return the command version
     */
    public @Nullable String getVersion() {
        return version;
    }

    /**
     * Gets any parameters
     * 
     * @return the parameters
     */
    public @Nullable String getParms() {
        return parms;
    }

    @Override
    public String toString() {
        return "CommandRequest [baseUrl=" + baseUrl + ", serviceName=" + serviceName + ", transport=" + transport
                + ", command=" + command + ", version=" + version + ", parms=" + parms + "]";
    }
}
