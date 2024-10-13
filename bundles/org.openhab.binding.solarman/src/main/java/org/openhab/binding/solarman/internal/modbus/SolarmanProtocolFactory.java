package org.openhab.binding.solarman.internal.modbus;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarman.internal.SolarmanLoggerConfiguration;

/**
 * @author Peter Kretz - Added RAW Modbus for LAN Stick
 */
@NonNullByDefault
public class SolarmanProtocolFactory {

    public static ISolarmanProtocol CreateSolarmanProtocol(SolarmanLoggerConfiguration solarmanLoggerConfiguration) {
        if (solarmanLoggerConfiguration.getRawLanMode()) {
            return new SolarmanRawProtocol(solarmanLoggerConfiguration);
        } else {
            return new SolarmanV5Protocol(solarmanLoggerConfiguration);
        }
    }
}
