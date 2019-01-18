/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
