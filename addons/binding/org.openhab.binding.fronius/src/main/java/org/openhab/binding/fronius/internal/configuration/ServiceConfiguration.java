/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.fronius.internal.configuration;

/**
 * Contains the service configuration.
 *
 * @author Gerrit Beine
 */
public class ServiceConfiguration {

    private final String hostname;
    private final int device;

    public ServiceConfiguration(String hostname, int device) {
        super();
        this.hostname = hostname;
        this.device = device;
    }

    public String getHostname() {
        return hostname;
    }

    public int getDevice() {
        return device;
    }
}
