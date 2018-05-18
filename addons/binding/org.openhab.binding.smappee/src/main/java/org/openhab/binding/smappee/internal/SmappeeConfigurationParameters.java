/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smappee.internal;

/**
 * Contains the configuration parameters for the smappee device.
 *
 * @author Niko Tanghe - Initial contribution
 */
public class SmappeeConfigurationParameters {

    /** The Smappee Api Oauth client id (obtain by mail from smappee support). */
    public String clientId;

    /** The Smappee Api Oauth client secret (obtain by mail from smappee support). */
    public String clientSecret;

    /** The username for your Smappee. */
    public String username;

    /** The password for your Smappee. */
    public String password;

    /** The name of your Smappee installation. */
    public String serviceLocationName;

    /** How often (in minutes) does the smappee needs to be checked ? */
    public int pollingInterval;
}
