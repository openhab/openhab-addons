/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal.config;

/**
 * The {@link NetatmoBridgeConfiguration} is responsible for holding
 * configuration informations needed to access Netatmo API
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class NetatmoBridgeConfiguration {
    public String clientId;
    public String clientSecret;
    public String username;
    public String password;
    public Boolean readStation;
    public Boolean readThermostat;
    public Boolean readHealthyHomeCoach;
    public Boolean readWelcome;
    public String webHookUrl;
    public Integer reconnectInterval;
}
