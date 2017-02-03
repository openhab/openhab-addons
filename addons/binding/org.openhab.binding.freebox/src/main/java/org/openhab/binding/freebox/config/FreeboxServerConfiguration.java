/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.freebox.config;

/**
 * The {@link FreeboxServerConfiguration} is responsible for holding
 * configuration informations needed to access/poll the freebox server
 *
 * @author Gaël L'hopital
 */
public class FreeboxServerConfiguration {

    public static final String FQDN = "fqdn";
    public static final String APP_TOKEN = "appToken";
    public static final String REFRESH_INTERVAL = "refreshInterval";

    public String fqdn;
    public String appToken;
    public Integer refreshInterval;

}
