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
package org.openhab.binding.chamberlainmyq.config;

import static org.openhab.binding.chamberlainmyq.ChamberlainMyQBindingConstants.*;

/**
 * The {@link ChamberlainMyQGatewayConfig} class represents the configuration of a MyQ Gateway Interface
 *
 * @author Scott Hanson - Initial contribution
 *
 */
public class ChamberlainMyQGatewayConfig {
    public String username;
    public String password;
    public int timeout;
    public int pollPeriod;
    public int quickPollPeriod;
    public String appID = APP_ID;
    public String brandID = BRANDID;
}
