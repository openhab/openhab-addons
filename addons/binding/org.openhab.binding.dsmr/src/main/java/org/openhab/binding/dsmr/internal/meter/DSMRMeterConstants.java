/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.meter;

import java.util.concurrent.TimeUnit;

/**
 * Class containing constants that are applicable to the DSMRMeter
 *
 * @author M. Volaart
 * @since 2.1.0
 */
public class DSMRMeterConstants {
    // Timeout for receiving meter values (in seconds)
    public static final int METER_VALUES_RECEIVED_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(60);
    // period between evaluations of meter values are received
    public static final int METER_VALUES_TIMER_PERIOD = (int) TimeUnit.SECONDS.toMillis(10);
    // unknown M-Bus channel
    public static final int UNKNOWN_CHANNEL = -1;
}
