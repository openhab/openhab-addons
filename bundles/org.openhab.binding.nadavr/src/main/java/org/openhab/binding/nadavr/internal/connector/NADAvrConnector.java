/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.nadavr.internal.connector;

import java.math.BigDecimal;
import java.util.concurrent.ScheduledExecutorService;

import org.openhab.binding.nadavr.internal.NADAvrConfiguration;
import org.openhab.binding.nadavr.internal.NADAvrState;
import org.openhab.binding.nadavr.internal.UnsupportedCommandTypeException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link NADAvrConnector} class contains fields mapping thing configuration parameters.
 *
 * @author Dave J Schoepel - Initial contribution
 */
public abstract class NADAvrConnector {

    private static final BigDecimal ONESTEP = new BigDecimal("1.0");
    private static final BigDecimal VOLUME_RANGE = new BigDecimal("118");
    private static final BigDecimal VOLUME_DB_MIN = new BigDecimal("-99");
    private static final BigDecimal VOLUME_DB_MAX = new BigDecimal("19");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    protected ScheduledExecutorService scheduler;
    protected NADAvrState state;
    protected NADAvrConfiguration config;

    public abstract void connect();

    public abstract void dispose();

    protected abstract void internalSendCommand(String command);

    /**
     *
     */
    public void sendPowerCommand(Command command, int zone) throws UnsupportedCommandTypeException {
        String zonePrefix;
        switch (zone) {
            case 0:
                zonePrefix = "Main.Power";
                break;
            case 2:
                zonePrefix = "Zone2.Power";
                break;
            case 3:
                zonePrefix = "Zone3.Power";
                break;
            case 4:
                zonePrefix = "Zone4.Power";
                break;
            default:
                throw new UnsupportedCommandTypeException("Zone must be in range [0-4], zone: " + zone);
        }
        String cmd = zonePrefix;
        if (command == OnOffType.ON) {
            cmd += "=On";
        } else if (command == OnOffType.OFF) {
            cmd += "=Off";
        } else if (command instanceof RefreshType) {
            cmd += "?";
        } else {
            throw new UnsupportedCommandTypeException();
        }
        internalSendCommand(cmd);
    }

    public void sendListeningModeCommand(Command command) throws UnsupportedCommandTypeException {
        String cmd = "Main.ListeningMode";
        if (command instanceof StringType) {
            cmd += "=" + command.toString();
        } else {
            throw new UnsupportedCommandTypeException();
        }
        internalSendCommand(cmd);
    }

    public void sendMuteCommand(Command command, int zone) throws UnsupportedCommandTypeException {
        if (zone < 1 || zone > 4) {
            throw new UnsupportedCommandTypeException("Zone must be in range [1-4], zone: " + zone);
        }
        StringBuilder sb = new StringBuilder();
        if (zone != 1) {
            sb.append("Zone").append(zone).append(".Mute");
        }
        sb.append("Main.Mute");
        String cmd = sb.toString();
        if (command == OnOffType.ON) {
            cmd += "=ON";
        } else if (command == OnOffType.OFF) {
            cmd += "=OFF";
        } else if (command instanceof RefreshType) {
            cmd += "?";
        } else {
            throw new UnsupportedCommandTypeException();
        }
        internalSendCommand(cmd);
    }

    // Command represents a % from the dimmer type control, need to convert to dB
    public void sendVolumeCommand(Command command, int zone) throws UnsupportedCommandTypeException {
        String zonePrefix;
        switch (zone) {
            case 1:
                zonePrefix = "Main.Volume=";
                break;
            case 2:
                zonePrefix = "Zone" + zone + ".Volume=";
                break;
            case 3:
                zonePrefix = "Zone" + zone + ".Volume=";
                break;
            case 4:
                zonePrefix = "Zone" + zone + ".Volume=";
                break;
            default:
                throw new UnsupportedCommandTypeException("Zone must be in range [1-4], zone: " + zone);
        }
        String cmd = zonePrefix;
        if (command instanceof RefreshType) {
            cmd += "?";
            // } else if (command == IncreaseDecreaseType.INCREASE) {
            // cmd += "UP";
            // } else if (command == IncreaseDecreaseType.DECREASE) {
            // cmd += "DOWN";
        } else if (command instanceof PercentType) {
            cmd += percentToDenonValue(((PercentType) command).toBigDecimal());
        } else if (command instanceof DecimalType) {
            cmd += toDenonValue(((DecimalType) command));
        } else {
            throw new UnsupportedCommandTypeException();
        }
        internalSendCommand(cmd);
    }

    public void sendVolumeDbCommand(Command command, int zone) throws UnsupportedCommandTypeException {
        Command dbCommand = command;
        if (dbCommand instanceof PercentType) {
            throw new UnsupportedCommandTypeException();
            // } else if (dbCommand instanceof DecimalType) {
            // // convert dB to 'normal' volume by adding the offset of 80
            // dbCommand = new DecimalType(((DecimalType) command).toBigDecimal().add(DB_OFFSET));
        }
        sendVolumeCommand(dbCommand, zone);
    }

    protected String toDenonValue(DecimalType number) {
        String dbString = String.valueOf(number.intValue());
        BigDecimal num = number.toBigDecimal();
        // if (num.compareTo(BigDecimal.TEN) == -1) {
        // dbString = "0" + dbString;
        // }
        // if (num.remainder(BigDecimal.ONE).equals(ONESTEP)) {
        // dbString = dbString + "0";
        // }
        return dbString;
    }

    protected String percentToDenonValue(BigDecimal pct) {
        // Round to nearest number divisible by 1.0
        // BigDecimal percent = pct.divide(ONESTEP).setScale(0, RoundingMode.UP).multiply(ONESTEP)
        // .min(config.getMainVolumeMax()).max(BigDecimal.ZERO);

        BigDecimal percent = pct.multiply(VOLUME_RANGE).divide(ONE_HUNDRED).add(VOLUME_DB_MIN);

        return toDenonValue(new DecimalType(percent));
    }
}
