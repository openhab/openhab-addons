/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.plugwise.internal.protocol;

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.SENSE_BOUNDARIES_SET_REQUEST;

import org.openhab.binding.plugwise.internal.protocol.field.BoundaryAction;
import org.openhab.binding.plugwise.internal.protocol.field.BoundaryType;
import org.openhab.binding.plugwise.internal.protocol.field.Humidity;
import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;
import org.openhab.binding.plugwise.internal.protocol.field.Temperature;

/**
 * Sets the Sense boundary switching parameters. These parameters control when the Sense sends on/off commands.
 *
 * @author Wouter Born - Initial contribution
 */
public class SenseBoundariesSetRequestMessage extends Message {

    private static final String MIN_BOUNDARY_VALUE = "0000";
    private static final String MAX_BOUNDARY_VALUE = "FFFF";

    private BoundaryType boundaryType;
    private BoundaryAction boundaryAction;
    private String lowerBoundaryHex;
    private String upperBoundaryHex;

    /**
     * Disables Sense boundary switching.
     */
    public SenseBoundariesSetRequestMessage(MACAddress macAddress) {
        super(SENSE_BOUNDARIES_SET_REQUEST, macAddress);
        this.boundaryType = BoundaryType.TEMPERATURE;
        this.boundaryAction = BoundaryAction.OFF_BELOW_ON_ABOVE;
        this.lowerBoundaryHex = MIN_BOUNDARY_VALUE;
        this.upperBoundaryHex = MAX_BOUNDARY_VALUE;
    }

    public SenseBoundariesSetRequestMessage(MACAddress macAddress, Temperature lowerBoundary, Temperature upperBoundary,
            BoundaryAction boundaryAction) {
        super(SENSE_BOUNDARIES_SET_REQUEST, macAddress);
        this.boundaryType = BoundaryType.TEMPERATURE;
        this.boundaryAction = boundaryAction;
        this.lowerBoundaryHex = lowerBoundary.toHex();
        this.upperBoundaryHex = upperBoundary.toHex();
    }

    public SenseBoundariesSetRequestMessage(MACAddress macAddress, Humidity lowerBoundary, Humidity upperBoundary,
            BoundaryAction boundaryAction) {
        super(SENSE_BOUNDARIES_SET_REQUEST, macAddress);
        this.boundaryType = BoundaryType.HUMIDITY;
        this.boundaryAction = boundaryAction;
        this.lowerBoundaryHex = lowerBoundary.toHex();
        this.upperBoundaryHex = upperBoundary.toHex();
    }

    @Override
    protected String payloadToHexString() {
        String boundaryTypeHex = String.format("%02X", boundaryType.toInt());
        String lowerBoundaryActionHex = String.format("%02X", boundaryAction.getLowerAction());
        String upperBoundaryActionHex = String.format("%02X", boundaryAction.getUpperAction());
        return boundaryTypeHex + upperBoundaryHex + upperBoundaryActionHex + lowerBoundaryHex + lowerBoundaryActionHex;
    }
}
