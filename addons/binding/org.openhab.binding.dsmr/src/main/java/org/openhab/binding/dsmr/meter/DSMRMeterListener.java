package org.openhab.binding.dsmr.meter;

import org.openhab.binding.dsmr.device.cosem.CosemObject;

/**
 * The DSMRMeterListener provides the interface for listeners for new meter values
 * for this meter
 *
 * @author M. Volaart
 * @since 2.0.0
 */
public interface DSMRMeterListener {
    /**
     * This method is called when a new meter value is received.
     *
     * The value received is a {@link CosemObject}
     * 
     * @param newMeterValue {@link CosemObject} containing the new meter value
     */
    public void meterValueReceived(CosemObject newMeterValue);
}
