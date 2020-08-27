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

package org.openhab.binding.elkm1.internal.config;

/**
 * The configuration for the elk alarm.
 *
 * @author David Bennett - Initial Contribution
 */

public class ElkAlarmConfig {
    public String ipAddress = "";
    public int port;
    public int pincode;
    public boolean useSSL;
    public String username = "";
    public String password = "";
}
