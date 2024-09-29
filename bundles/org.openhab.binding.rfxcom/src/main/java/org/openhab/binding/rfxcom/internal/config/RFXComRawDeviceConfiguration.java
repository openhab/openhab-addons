/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.rfxcom.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComInvalidParameterException;

/**
 * Configuration class for Raw RFXCOM device.
 *
 * @author James Hewitt-Thomas - Initial contribution
 */
@NonNullByDefault
public class RFXComRawDeviceConfiguration extends RFXComGenericDeviceConfiguration {
    public static final String REPEAT_LABEL = "repeat";
    public int repeat;

    public static final String ON_PULSES_LABEL = "onPulses";
    public static final String OFF_PULSES_LABEL = "offPulses";
    @Nullable
    public String onPulses;
    @Nullable
    public String offPulses;
    public short @Nullable [] onPulsesArray;
    public short @Nullable [] offPulsesArray;

    public static final String OPEN_PULSES_LABEL = "openPulses";
    public static final String CLOSED_PULSES_LABEL = "closedPulses";
    @Nullable
    public String openPulses;
    @Nullable
    public String closedPulses;
    public short @Nullable [] openPulsesArray;
    public short @Nullable [] closedPulsesArray;

    @Override
    public void parseAndValidate() throws RFXComInvalidParameterException {
        super.parseAndValidate();

        onPulsesArray = parseAndValidatePulses("onPulses", onPulses);
        offPulsesArray = parseAndValidatePulses("offPulses", offPulses);
        openPulsesArray = parseAndValidatePulses("openPulses", openPulses);
        closedPulsesArray = parseAndValidatePulses("closedPulses", closedPulses);
    }

    private static short @Nullable [] parseAndValidatePulses(String parameter, @Nullable String pulses)
            throws RFXComInvalidParameterException {
        if (pulses != null) {
            return parseAndValidatePulsesNonNull(parameter, pulses);
        } else {
            return null;
        }
    }

    private static short[] parseAndValidatePulsesNonNull(String parameter, String pulses)
            throws RFXComInvalidParameterException {
        String[] strings = pulses.trim().split("\\s+");

        if (strings.length > 124) {
            throw new RFXComInvalidParameterException(parameter, pulses, "Cannot have more than 124 pulses");
        }

        if (strings.length % 2 != 0) {
            throw new RFXComInvalidParameterException(parameter, pulses, "Pulses must be in pairs");
        }

        try {
            short[] shorts = new short[strings.length];
            for (int i = 0; i < strings.length; i++) {
                int pulse = Integer.parseInt(strings[i]);
                if (pulse > 65535) {
                    throw new RFXComInvalidParameterException(parameter, pulses, "Cannot have pulse above 65535 usec");
                } else if (pulse < 0) {
                    throw new RFXComInvalidParameterException(parameter, pulses, "Cannot have negative pulse");
                } else if (pulse == 0) {
                    // The user guide suggests that received pulses of size 0 should be
                    // replaced with something above 8000, as they represent gaps.
                    shorts[i] = 10000;
                } else {
                    shorts[i] = (short) pulse;
                }
            }
            return shorts;
        } catch (NumberFormatException e) {
            throw new RFXComInvalidParameterException(parameter, pulses, e.getMessage(), e);
        }
    }
}
