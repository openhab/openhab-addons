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
package org.openhab.binding.mihome.internal.handler;

import static org.openhab.binding.mihome.internal.XiaomiGatewayBindingConstants.*;

import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Thing;

import com.google.gson.JsonObject;

/**
 * Handles the Xiaomi Aqara Fingerprint Lock
 *
 * @author Dieter Schmidt - Initial contribution
 */
public class XiaomiSensorLockHandler extends XiaomiSensorBaseHandler {

    private static final String FING_VERIFIED = "fing_verified";
    private static final String PSW_VERIFIED = "psw_verified";
    private static final String CARD_VERIFIED = "card_verified";
    private static final String VERIFIED_WRONG = "verified_wrong";
    private static final String FINGER = "Finger";
    private static final String PASSWORD = "Password";
    private static final String CARD = "Card";
    private static final String WRONG_ACCESS = "Wrong Access";
    private static final String ALARM = "ALARM";
    private static final String STATUS = "status";

    public XiaomiSensorLockHandler(Thing thing) {
        super(thing);
    }

    @Override
    void parseDefault(JsonObject data) {
        if (data.has(FING_VERIFIED)) {
            onOpen();
            updateState(CHANNEL_STATUS, new StringType(FINGER));
            updateState(CHANNEL_ID, new DecimalType(data.get(FING_VERIFIED).getAsInt()));
        } else if (data.has(PSW_VERIFIED)) {
            onOpen();
            updateState(CHANNEL_STATUS, new StringType(PASSWORD));
            updateState(CHANNEL_ID, new DecimalType(data.get(PSW_VERIFIED).getAsInt()));
        } else if (data.has(CARD_VERIFIED)) {
            onOpen();
            updateState(CHANNEL_STATUS, new StringType(CARD));
            updateState(CHANNEL_ID, new DecimalType(data.get(CARD_VERIFIED).getAsInt()));
        } else if (data.has(VERIFIED_WRONG)) {
            updateState(CHANNEL_STATUS, new StringType(WRONG_ACCESS));
            updateState(CHANNEL_ID, new DecimalType(data.get(VERIFIED_WRONG).getAsInt()));
            triggerChannel(CHANNEL_WRONG_ACCESS, ALARM);
        } else if (data.has(STATUS)) {
            updateState(CHANNEL_STATUS, new StringType(data.get(STATUS).getAsString().toUpperCase()));
        }
        super.parseDefault(data);
    }

    private void onOpen() {
        triggerChannel(CHANNEL_IS_OPEN, ALARM);
        updateState(CHANNEL_LAST_OPENED, new DateTimeType());
    }
}
