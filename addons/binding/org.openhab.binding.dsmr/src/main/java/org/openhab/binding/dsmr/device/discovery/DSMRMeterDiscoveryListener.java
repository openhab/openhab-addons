package org.openhab.binding.dsmr.device.discovery;

import org.openhab.binding.dsmr.meter.DSMRMeterDescriptor;

/**
 * This interface is notified of new meter discoveries
 *
 * @author Marcel Volaart
 * @since 2.0.0
 */
public interface DSMRMeterDiscoveryListener {
    /**
     * A new meter is discovered
     *
     * The implementation must return if the new discovered meter is accepted.
     *
     * @param meterDescriptor {@link DSMRMeterDescriptor} describing the new meter
     * @return true if the new meter is accepted, false otherwise
     */
    public boolean meterDiscovered(DSMRMeterDescriptor meterDescriptor);
}
