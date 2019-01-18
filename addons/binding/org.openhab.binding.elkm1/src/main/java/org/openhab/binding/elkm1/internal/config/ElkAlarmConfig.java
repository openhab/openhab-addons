/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.elkm1.internal.config;

/**
 * The configuration for the elk alarm.
 *
 * @author David Bennett - Initial Contribution
 */
public class ElkAlarmConfig {
    public String ipAddress;
    public int port;
    public int pincode;
    public boolean useSSL;
    public String username;
    public String password;
}
