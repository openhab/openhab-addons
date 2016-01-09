/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.passthru.config;

/**
 * Configuration properties for connecting to a remote openhab system
 *
 * @author @author J. Geyer - Initial contribution
 *
 */
public class PassthruConfiguration {

    /**
     * ip address of the remote system
     */
    public String host;

    /**
     * port of the remote system as for openhab 8080
     */
    public Integer port;

    /**
     * The version of the target openhab system
     */
    public Integer version;

    /**
     * The rate for status polling refresh
     */
    public Integer refresh;

    /**
     * The items to monitor
     */
    public String monitor;
}
