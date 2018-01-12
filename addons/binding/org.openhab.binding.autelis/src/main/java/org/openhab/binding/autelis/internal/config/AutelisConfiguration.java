/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.autelis.internal.config;

/**
 * Configuration properties for connecting to a Autelis Controller
 *
 * @author Dan Cunningham
 *
 */
public class AutelisConfiguration {

    /**
     * Host of the Autelis controller
     */
    public String host;
    /**
     * port of the Autelis controller
     */
    public Integer port;

    /**
     * user to us when connecting to the Autelis controller
     */
    public String user;

    /**
     * password to us when connecting to the Autelis controller
     */
    public String password;

    /**
     * Rate we poll for new data
     */
    public Integer refresh;
}
