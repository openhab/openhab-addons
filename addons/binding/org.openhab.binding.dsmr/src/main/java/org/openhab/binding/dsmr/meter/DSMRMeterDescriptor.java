package org.openhab.binding.dsmr.meter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The DSMRMeterDescriptor describes a meter.
 *
 * A DSMR Meter consists of the following properties:
 * - MeterType
 * - M-Bus channel
 * - Identifier
 *
 * @author M. Volaart
 * @since 2.0.0
 */
public class DSMRMeterDescriptor {
    // logger
    private static final Logger logger = LoggerFactory.getLogger(DSMRMeterDescriptor.class);

    // Meter type
    private final DSMRMeterType meterType;

    // M-Bus channel
    private final Integer channel;

    // Meter identification
    private final String idString;

    /**
     *
     * @param meterType
     * @param channel
     * @param idString
     * @throws IllegalArgumentException if one of the parameters is null
     */
    public DSMRMeterDescriptor(DSMRMeterType meterType, Integer channel, String idString) {
        if (meterType == null || channel == null || idString == null) {
            logger.error("MeterType: {}, channel:{}, idString:{}", meterType, channel, idString);

            throw new IllegalArgumentException("Parameters of DSMRMeterDescription can not be null");
        }

        this.meterType = meterType;
        this.channel = channel;

        /*
         * The implementation needs an identification for every meter (String.length > 0). This identification
         * is used in the Thing configuration and will make potential problems more obvious because not having
         * a meter identification is probably caused by a software bug.
         *
         * To prevent problems with meters that have a identifier but it is real empty we substitute this
         * with a meta value UNKNOWN_ID. This will only be used in finding the meter instance
         * and is saved in the configuration. If the user adds an Item to the particular Channel of this meter
         * the real value ("" in this case) will be send to the Item.
         */
        if (idString.length() == 0) {
            this.idString = DSMRMeterConstants.UNKNOWN_ID;
        } else {
            this.idString = idString;
        }
    }

    /**
     * @return the meterType
     */
    public DSMRMeterType getMeterType() {
        return meterType;
    }

    /**
     * @return the channel
     */
    public Integer getChannel() {
        return channel;
    }

    /**
     * @return the idString
     */
    public String getIdString() {
        return idString;
    }

    /**
     * Returns true if both DSMRMeterDescriptor are equal. I.e.:
     * - meterType is the same
     * - channel is the same
     * - identification is the same
     *
     * @param other DSMRMeterDescriptor to check
     * @return true if both objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof DSMRMeterDescriptor)) {
            return false;
        }
        DSMRMeterDescriptor o = (DSMRMeterDescriptor) other;

        return meterType == o.meterType && channel.equals(o.channel) && idString.equals(o.idString);
    }

    @Override
    public String toString() {
        return "Meter type: " + meterType + ", channel: " + channel + ", identification: " + idString;
    }
}
