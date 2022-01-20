/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands;

import java.nio.ByteOrder;
import java.util.concurrent.Executor;

import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaMessage;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaParsingException;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaValue;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command returns the operating hours of internal unit
 *
 * @author Benjamin Lafois - Initial contribution
 *
 */
@NonNullByDefault
public class GetOperationHoursCommand extends BRC1HCommand {

    private final Logger logger = LoggerFactory.getLogger(GetOperationHoursCommand.class);

    private @Nullable QuantityType<Time> indoorOperationHours;
    private @Nullable QuantityType<Time> indoorFanHours;
    private @Nullable QuantityType<Time> indoorPowerHours;

    @Override
    public byte[][] getRequest() {
        MadokaValue specificUnitNumber = new MadokaValue(0x02, 1, new byte[] { (byte) 0x00 });
        MadokaValue p40 = new MadokaValue(0x40, 0, new byte[] {});
        MadokaValue p41 = new MadokaValue(0x41, 0, new byte[] {});
        MadokaValue p42 = new MadokaValue(0x42, 0, new byte[] {});
        MadokaValue p43 = new MadokaValue(0x43, 0, new byte[] {});
        MadokaValue p44 = new MadokaValue(0x44, 0, new byte[] {});
        MadokaValue p45 = new MadokaValue(0x45, 0, new byte[] {});
        MadokaValue p46 = new MadokaValue(0x46, 0, new byte[] {});
        MadokaValue p47 = new MadokaValue(0x47, 0, new byte[] {});
        MadokaValue p48 = new MadokaValue(0x48, 0, new byte[] {});

        return MadokaMessage.createRequest(this, specificUnitNumber, p40, p41, p42, p43, p44, p45, p46, p47, p48);
    }

    @Override
    public void handleResponse(Executor executor, ResponseListener listener, MadokaMessage mm)
            throws MadokaParsingException {
        try {

            byte[] msg = mm.getRawMessage();
            if (logger.isDebugEnabled() && msg != null) {
                logger.debug("Got response for {} : {}", this.getClass().getSimpleName(), HexUtils.bytesToHex(msg));
            }

            // The specific GetOperationHours requires 2 consecutive runs for some reason.
            // If value size is 0, then it will be for the next query!
            if (mm.getValues().get(0x40).getSize() == 0) {
                setState(State.SUCCEEDED);
                return;
            }

            Integer iIndoorOperationHours = (int) (mm.getValues().get(0x40).getComputedValue(ByteOrder.LITTLE_ENDIAN));
            Integer iIndoorFanHours = (int) (mm.getValues().get(0x41).getComputedValue(ByteOrder.LITTLE_ENDIAN));
            Integer iIndoorPowerHours = (int) (mm.getValues().get(0x42).getComputedValue(ByteOrder.LITTLE_ENDIAN));

            this.indoorOperationHours = new QuantityType<Time>(iIndoorOperationHours, Units.HOUR);
            this.indoorFanHours = new QuantityType<Time>(iIndoorFanHours, Units.HOUR);
            this.indoorPowerHours = new QuantityType<Time>(iIndoorPowerHours, Units.HOUR);

            logger.debug("indoorOperationHours: {}", indoorOperationHours);
            logger.debug("indoorFanHours: {}", indoorFanHours);
            logger.debug("indoorPowerHours: {}", indoorPowerHours);

            setState(State.SUCCEEDED);
            executor.execute(() -> listener.receivedResponse(this));
        } catch (Exception e) {
            setState(State.FAILED);
            throw new MadokaParsingException(e);
        }
    }

    @Override
    public int getCommandId() {
        return 274;
    }

    public @Nullable QuantityType<Time> getIndoorOperationHours() {
        return indoorOperationHours;
    }

    public @Nullable QuantityType<Time> getIndoorFanHours() {
        return indoorFanHours;
    }

    public @Nullable QuantityType<Time> getIndoorPowerHours() {
        return indoorPowerHours;
    }
}
