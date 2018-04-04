/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hdpowerview.internal.config;

/**
 * Basic configuration for the HD Power View HUB
 *
 * @author Andy Lintner - Initial contribution
 */
public class HDPowerViewHubConfiguration {

    public static final String HOST = "host";

    public String host;

    public long refresh;
}
