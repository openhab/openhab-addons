/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal.protocol;

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.POWER_CALIBRATION_RESPONSE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.plugwise.internal.protocol.field.Energy;
import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;
import org.openhab.binding.plugwise.internal.protocol.field.PowerCalibration;

/**
 * Contains the power calibration data of a relay device (Circle, Circle+, Stealth). This message is the response of a
 * {@link PowerCalibrationRequestMessage}. The {@link PowerCalibration} data is used to calculate power (W) and energy
 * (kWh) from pulses with the {@link Energy} class.
 *
 * @author Karel Goderis
 * @author Wouter Born - Initial contribution
 */
public class PowerCalibrationResponseMessage extends Message {

    private static final Pattern PAYLOAD_PATTERN = Pattern.compile("(\\w{16})(\\w{8})(\\w{8})(\\w{8})(\\w{8})");

    private double gainA;
    private double gainB;
    private double offsetTotal;
    private double offsetNoise;

    public PowerCalibrationResponseMessage(int sequenceNumber, String payload) {
        super(POWER_CALIBRATION_RESPONSE, sequenceNumber, payload);
    }

    public double getGainA() {
        return gainA;
    }

    public double getGainB() {
        return gainB;
    }

    public double getOffsetNoise() {
        return offsetNoise;
    }

    public double getOffsetTotal() {
        return offsetTotal;
    }

    public PowerCalibration getCalibration() {
        return new PowerCalibration(gainA, gainB, offsetNoise, offsetTotal);
    }

    @Override
    protected void parsePayload() {
        Matcher matcher = PAYLOAD_PATTERN.matcher(payload);
        if (matcher.matches()) {
            macAddress = new MACAddress(matcher.group(1));

            gainA = Float.intBitsToFloat((int) (Long.parseLong(matcher.group(2), 16)));
            gainB = Float.intBitsToFloat((int) (Long.parseLong(matcher.group(3), 16)));
            offsetTotal = Float.intBitsToFloat((int) (Long.parseLong(matcher.group(4), 16)));
            offsetNoise = Float.intBitsToFloat((int) (Long.parseLong(matcher.group(5), 16)));
        } else {
            throw new PlugwisePayloadMismatchException(POWER_CALIBRATION_RESPONSE, PAYLOAD_PATTERN, payload);
        }
    }

}
