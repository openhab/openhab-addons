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
package org.openhab.binding.sonos.internal.config;

/**
 *
 * @author Karel Goderis - Initial contribution
 */
public class ZonePlayerConfiguration {

    public static final String UDN = "udn";
    public static final String REFRESH = "refresh";
    public static final String NOTIFICATION_TIMEOUT = "notificationTimeout";
    public static final String NOTIFICATION_VOLUME = "notificationVolume";

    public String udn;
    public Integer refresh;
    public Integer notificationTimeout;
    public Integer notificationVolume;
}
