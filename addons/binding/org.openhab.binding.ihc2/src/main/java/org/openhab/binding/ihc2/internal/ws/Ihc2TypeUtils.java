/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc2.internal.ws;

import static org.openhab.binding.ihc2.Ihc2BindingConstants.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.ihc2.internal.ws.datatypes.WSBooleanValue;
import org.openhab.binding.ihc2.internal.ws.datatypes.WSDateValue;
import org.openhab.binding.ihc2.internal.ws.datatypes.WSEnumValue;
import org.openhab.binding.ihc2.internal.ws.datatypes.WSFloatingPointValue;
import org.openhab.binding.ihc2.internal.ws.datatypes.WSIntegerValue;
import org.openhab.binding.ihc2.internal.ws.datatypes.WSResourceValue;
import org.openhab.binding.ihc2.internal.ws.datatypes.WSTimeValue;
import org.openhab.binding.ihc2.internal.ws.datatypes.WSTimerValue;
import org.openhab.binding.ihc2.internal.ws.datatypes.WSWeekdayValue;

/**
 * The {@link Ihc2TypeUtils} some type conversion helpers
 *
 * @author Niels Peter Enemark - Initial contribution
 */
public class Ihc2TypeUtils {
    public static WSResourceValue oh2ihc(ChannelUID channelUID, Command command, int resourceID) throws Ihc2Execption {
        if (command instanceof OnOffType) {
            WSBooleanValue wbv = new WSBooleanValue();
            wbv.setValue(((OnOffType) command) == OnOffType.ON);
            wbv.setResourceID(resourceID);
            return wbv;
        }
        if (command instanceof OpenClosedType) {
            WSBooleanValue wbv = new WSBooleanValue();
            wbv.setValue(((OpenClosedType) command) == OpenClosedType.CLOSED);
            wbv.setResourceID(resourceID);
            return wbv;
        }
        if (command instanceof DateTimeType) {
            WSDateValue dv = new WSDateValue();
            ZonedDateTime zdt = ((DateTimeType) command).getZonedDateTime();
            dv.setYear((short) zdt.getYear());
            dv.setMonth((byte) zdt.getMonthValue());
            dv.setDay((byte) zdt.getDayOfMonth());
            dv.setResourceID(resourceID);
            return dv;
        }
        if (command instanceof DecimalType) {
            WSIntegerValue wiv = new WSIntegerValue();
            wiv.setInteger(((DecimalType) command).intValue());
            wiv.setResourceID(resourceID);
            return wiv;
        }
        if (command instanceof PercentType) {
            WSIntegerValue wiv = new WSIntegerValue();
            wiv.setInteger(((DecimalType) command).intValue());
            wiv.setMinimumValue(0);
            wiv.setMaximumValue(100);
            wiv.setResourceID(resourceID);
            return wiv;
        }
        if (command instanceof StringType) {
            WSEnumValue ev = new WSEnumValue();
            ev.setEnumName(((StringType) command).toFullString());
            ev.setResourceID(resourceID);
            return ev;
        }
        throw new Ihc2Execption(String.format("Unhandled OH Type: " + command.toFullString()));
    }

    public static Command ihc2oh(String channelUID, WSResourceValue rv) throws Ihc2Execption {
        if (rv instanceof WSBooleanValue) {
            WSBooleanValue bv = (WSBooleanValue) rv;
            if (channelUID.equals(CHANNEL_SWITCH)) {
                return (bv.isValue() ? OnOffType.ON : OnOffType.OFF);
            }
            return (bv.isValue() ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
        }
        if (rv instanceof WSEnumValue) {
            return new StringType(String.valueOf(((WSEnumValue) rv).getEnumValueID()));
        }

        if (rv instanceof WSDateValue) {
            WSDateValue dv = (WSDateValue) rv;

            LocalDate currentDate = LocalDate.of(dv.getYear(), dv.getMonth(), dv.getDay());
            LocalTime localTime = LocalTime.of(0, 0, 0);
            ZonedDateTime zdt = ZonedDateTime.of(currentDate, localTime, ZoneId.systemDefault());
            return new DateTimeType(zdt);
        }

        if (rv instanceof WSFloatingPointValue) {
            WSFloatingPointValue fp = (WSFloatingPointValue) rv;
            return new DecimalType(fp.getFloatingPointValue());
        }

        if (rv instanceof WSIntegerValue) {
            WSIntegerValue iv = (WSIntegerValue) rv;
            if (channelUID.equals(CHANNEL_PERCENT)) {
                return new PercentType(iv.getInteger());
            }
            return new DecimalType(iv.getInteger());
        }

        if (rv instanceof WSTimerValue) {
            return new DecimalType(((WSTimerValue) rv).getMilliseconds());
        }

        if (rv instanceof WSTimeValue) {
            WSTimeValue tv = (WSTimeValue) rv;

            LocalDate currentDate = LocalDate.now();
            LocalTime localTime = LocalTime.of(tv.getHours(), tv.getMinutes(), tv.getSeconds());
            ZonedDateTime zdt = ZonedDateTime.of(currentDate, localTime, ZoneId.systemDefault());
            return new DateTimeType(zdt);
        }

        if (rv instanceof WSWeekdayValue) {
            return new DecimalType(((WSWeekdayValue) rv).getWeekdayNumber());
        }

        throw new Ihc2Execption(String.format("Unhandled IHC Type: " + rv.toString()));
    }

}
