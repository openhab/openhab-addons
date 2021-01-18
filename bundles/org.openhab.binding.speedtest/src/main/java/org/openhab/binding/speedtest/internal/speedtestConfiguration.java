/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.speedtest.internal;

/**
 * The {@link speedtestConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Brian Homeyer - Initial contribution
 */
public class speedtestConfiguration {
    public int refreshInterval;
    public String execPath = "";
    public String serverID = "";
    public String speedTestVersion = "";
}
