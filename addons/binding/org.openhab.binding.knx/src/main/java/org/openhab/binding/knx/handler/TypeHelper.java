package org.openhab.binding.knx.handler;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.types.Type;

import tuwien.auto.calimero.datapoint.Datapoint;

public interface TypeHelper {

    /**
     * Transforms the raw KNX bus data of a given datapoint into an openHAB type (command or state)
     *
     * @param datapoint
     *            the datapoint to which the data belongs
     * @param asdu
     *            the byte array of the raw data from the KNX bus
     * @return the openHAB command or state that corresponds to the data
     */
    @Nullable
    Type getType(Datapoint datapoint, byte[] asdu);

    boolean isDPTSupported(@Nullable String dpt);

    @Nullable
    Class<? extends Type> toTypeClass(String dpt);

}
