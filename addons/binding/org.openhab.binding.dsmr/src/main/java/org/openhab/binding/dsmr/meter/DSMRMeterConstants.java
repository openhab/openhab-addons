package org.openhab.binding.dsmr.meter;

import java.util.concurrent.TimeUnit;

/**
 * Class containing constants that are applicable to the DSMRMeter
 *
 * @author M. Volaart
 * @since 2.0.0
 */
public class DSMRMeterConstants {
    // Timeout for receiving meter values
    public static final int METER_VALUES_RECEIVED_TIMEOUT = (int) TimeUnit.MINUTES.toMillis(1);
    // period between evaluations of meter values are received
    public static final int METER_VALUES_TIMER_PERIOD = (int) TimeUnit.SECONDS.toMillis(10);
    // unknown M-Bus channel
    public static final int UNKNOWN_CHANNEL = -1;
    // Unknown meter identifier
    public static final String UNKNOWN_ID = "unknown_id";

}
