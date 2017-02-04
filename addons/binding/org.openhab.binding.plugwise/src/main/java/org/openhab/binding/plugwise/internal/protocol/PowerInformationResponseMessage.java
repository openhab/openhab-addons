/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal.protocol;

import static java.time.ZoneOffset.UTC;
import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.POWER_INFORMATION_RESPONSE;

import java.time.ZonedDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.plugwise.internal.protocol.field.Energy;
import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * Contains the real-time energy consumption of a relay device (Circle, Circle+, Stealth). This
 * message is the response of a {@link PowerInformationRequestMessage}.
 *
 * @author Karel Goderis
 * @author Wouter Born - Initial contribution
 */
public class PowerInformationResponseMessage extends Message {

    private static final Pattern PAYLOAD_PATTERN = Pattern.compile("(\\w{16})(\\w{4})(\\w{4})(\\w{8})(\\w{8})(\\w{4})");

    private Energy oneSecond;
    private Energy eightSecond;
    private Energy oneHourConsumed;
    private Energy oneHourProduced;
    private double secondsCorrection;

    public PowerInformationResponseMessage(int sequenceNumber, String payload) {
        super(POWER_INFORMATION_RESPONSE, sequenceNumber, payload);
    }

    public Energy getEightSecond() {
        return eightSecond;
    }

    public Energy getOneHourConsumed() {
        return oneHourConsumed;
    }

    public Energy getOneHourProduced() {
        return oneHourProduced;
    }

    public Energy getOneSecond() {
        return oneSecond;
    }

    @Override
    protected void parsePayload() {
        Matcher matcher = PAYLOAD_PATTERN.matcher(payload);
        if (matcher.matches()) {
            ZonedDateTime utcNow = ZonedDateTime.now(UTC);
            macAddress = new MACAddress(matcher.group(1));
            secondsCorrection = Integer.parseInt(matcher.group(6), 16) / 46875.0;
            oneSecond = new Energy(utcNow, Integer.parseInt(matcher.group(2), 16), 1 + secondsCorrection);
            eightSecond = new Energy(utcNow, Integer.parseInt(matcher.group(3), 16), 8 + secondsCorrection);
            oneHourConsumed = new Energy(utcNow, Long.parseLong(matcher.group(4), 16), 3600);
            oneHourProduced = new Energy(utcNow, Long.parseLong(matcher.group(5), 16), 3600);
        } else {
            throw new PlugwisePayloadMismatchException(POWER_INFORMATION_RESPONSE, PAYLOAD_PATTERN, payload);
        }
    }

}
