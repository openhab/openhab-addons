/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.denonmarantz.internal.connector;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.denonmarantz.DenonMarantzBindingConstants;
import org.openhab.binding.denonmarantz.internal.DenonMarantzState;
import org.openhab.binding.denonmarantz.internal.UnsupportedCommandTypeException;
import org.openhab.binding.denonmarantz.internal.config.DenonMarantzConfiguration;

/**
 * Abstract class containing common functionality for the connectors.
 *
 * @author Jan-Willem Veldhuis - Initial contribution
 */
public abstract class DenonMarantzConnector {

    private static final BigDecimal POINTFIVE = new BigDecimal("0.5");
    protected ScheduledExecutorService scheduler;
    protected DenonMarantzState state;
    protected DenonMarantzConfiguration config;

    public abstract void connect();

    public abstract void dispose();

    protected abstract void internalSendCommand(String command);

    public void sendCustomCommand(Command command) throws UnsupportedCommandTypeException {
        String cmd;
        if (command instanceof StringType) {
            cmd = command.toString();
        } else {
            throw new UnsupportedCommandTypeException();
        }
        internalSendCommand(cmd);
    }

    public void sendInputCommand(Command command, int zone) throws UnsupportedCommandTypeException {
        String zonePrefix;
        switch (zone) {
            case 1:
                zonePrefix = "SI";
                break;
            case 2:
            case 3:
                zonePrefix = "Z" + zone;
                break;
            default:
                throw new UnsupportedCommandTypeException("Zone must be in range [1-3], zone: " + zone);
        }
        String cmd = zonePrefix;
        if (command instanceof StringType) {
            cmd += command.toString();
        } else if (command instanceof RefreshType) {
            cmd += "?";
        } else {
            throw new UnsupportedCommandTypeException();
        }
        internalSendCommand(cmd);
    }

    public void sendSurroundProgramCommand(Command command) throws UnsupportedCommandTypeException {
        String cmd = "MS";
        if (command instanceof RefreshType) {
            cmd += "?";
        } else {
            throw new UnsupportedCommandTypeException();
        }
        internalSendCommand(cmd);
    }

    public void sendMuteCommand(Command command, int zone) throws UnsupportedCommandTypeException {
        if (zone < 1 || zone > 3) {
            throw new UnsupportedCommandTypeException("Zone must be in range [1-3], zone: " + zone);
        }
        StringBuilder sb = new StringBuilder();
        if (zone != 1) {
            sb.append("Z").append(zone);
        }
        sb.append("MU");
        String cmd = sb.toString();
        if (command == OnOffType.ON) {
            cmd += "ON";
        } else if (command == OnOffType.OFF) {
            cmd += "OFF";
        } else if (command instanceof RefreshType) {
            cmd += "?";
        } else {
            throw new UnsupportedCommandTypeException();
        }
        internalSendCommand(cmd);
    }

    public void sendPowerCommand(Command command, int zone) throws UnsupportedCommandTypeException {
        String zonePrefix;
        switch (zone) {
            case 0:
                zonePrefix = "PW";
                break;
            case 1:
                zonePrefix = "ZM";
                break;
            case 2:
            case 3:
                zonePrefix = "Z" + zone;
                break;
            default:
                throw new UnsupportedCommandTypeException("Zone must be in range [0-3], zone: " + zone);
        }
        String cmd = zonePrefix;
        if (command == OnOffType.ON) {
            cmd += "ON";
        } else if (command == OnOffType.OFF) {
            cmd += (zone == 0) ? "STANDBY" : "OFF";
        } else if (command instanceof RefreshType) {
            cmd += "?";
        } else {
            throw new UnsupportedCommandTypeException();
        }
        internalSendCommand(cmd);
    }

    public void sendVolumeCommand(Command command, int zone) throws UnsupportedCommandTypeException {
        String zonePrefix;
        switch (zone) {
            case 1:
                zonePrefix = "MV";
                break;
            case 2:
            case 3:
                zonePrefix = "Z" + zone;
                break;
            default:
                throw new UnsupportedCommandTypeException("Zone must be in range [1-3], zone: " + zone);
        }
        String cmd = zonePrefix;
        if (command instanceof RefreshType) {
            cmd += "?";
        } else if (command == IncreaseDecreaseType.INCREASE) {
            cmd += "UP";
        } else if (command == IncreaseDecreaseType.DECREASE) {
            cmd += "DOWN";
        } else if (command instanceof DecimalType) {
            cmd += toDenonValue(((DecimalType) command));
        } else if (command instanceof PercentType) {
            cmd += percentToDenonValue(((PercentType) command).toBigDecimal());
        } else {
            throw new UnsupportedCommandTypeException();
        }
        internalSendCommand(cmd);
    }

    public void sendVolumeDbCommand(Command command, int zone) throws UnsupportedCommandTypeException {
        Command dbCommand = command;
        if (dbCommand instanceof PercentType) {
            throw new UnsupportedCommandTypeException();
        } else if (dbCommand instanceof DecimalType) {
            // convert dB to 'normal' volume by adding the offset of 80
            dbCommand = new DecimalType(
                    ((DecimalType) command).toBigDecimal().add(DenonMarantzBindingConstants.DB_OFFSET));
        }
        sendVolumeCommand(dbCommand, zone);
    }

    protected String toDenonValue(DecimalType number) {
        String dbString = String.valueOf(number.intValue());
        BigDecimal num = number.toBigDecimal();
        if (num.compareTo(BigDecimal.TEN) == -1) {
            dbString = "0" + dbString;
        }
        if (num.remainder(BigDecimal.ONE).equals(POINTFIVE)) {
            dbString = dbString + "5";
        }
        return dbString;
    }

    protected String percentToDenonValue(BigDecimal pct) {
        // Round to nearest number divisible by 0.5
        BigDecimal percent = pct.divide(POINTFIVE).setScale(0, RoundingMode.UP).multiply(POINTFIVE)
                .min(config.getMainVolumeMax()).max(BigDecimal.ZERO);

        return toDenonValue(new DecimalType(percent));
    }
}
