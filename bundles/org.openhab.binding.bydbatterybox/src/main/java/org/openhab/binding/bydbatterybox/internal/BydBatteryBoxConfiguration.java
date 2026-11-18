/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.bydbatterybox.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link BydBatteryBoxConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class BydBatteryBoxConfiguration {

    public static final int MIN_REFRESH_INTERVAL = 30;

    public String hostname = "";
    public int port = 8080;
    /** Status interval in seconds. */
    public int refreshInterval = 60;
    /** Cell detail interval in seconds, 0 = disabled. */
    public int detailInterval = 600;
    /** Event log interval in seconds, 0 = disabled. */
    public int eventLogInterval = 600;
    /** Read the cells beyond 128 in a second measurement pass, which needs the "DEBUG" write. */
    public boolean readExtendedCells = false;
    /** Connect and reply timeout in seconds. */
    public int connectionTimeout = 5;
}
