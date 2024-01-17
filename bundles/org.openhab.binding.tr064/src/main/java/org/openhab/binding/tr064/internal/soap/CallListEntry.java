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
package org.openhab.binding.tr064.internal.soap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tr064.internal.dto.additions.Call;

/**
 * The {@link CallListEntry} is used for post-processing the retrieved call
 * lists
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class CallListEntry {
    private static final SimpleDateFormat DATE_FORMAT_PARSER = new SimpleDateFormat("dd.MM.yy HH:mm");
    public @Nullable String localNumber;
    public @Nullable String remoteNumber;
    public @Nullable Date date;
    public @Nullable Integer type;
    public @Nullable Integer duration;

    public CallListEntry(Call call) {
        try {
            synchronized (DATE_FORMAT_PARSER) {
                date = DATE_FORMAT_PARSER.parse(call.getDate());
            }
        } catch (ParseException e) {
            // ignore parsing error
            date = null;
        }
        String[] durationParts = call.getDuration().split(":");
        duration = Integer.parseInt(durationParts[0]) * 60 + Integer.parseInt(durationParts[1]);
        type = Integer.parseInt(call.getType());
        if (CallListType.OUTBOUND_COUNT.typeString().equals(call.getType())) {
            localNumber = call.getCallerNumber();
            remoteNumber = call.getCalled();
        } else {
            localNumber = call.getCalledNumber();
            remoteNumber = call.getCaller();
        }
    }
}
