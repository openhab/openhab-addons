/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.enocean.internal.eep.F6_01;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.CommonTriggerEvents;
import org.openhab.binding.enocean.internal.eep.Base._RPSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class F6_01_01 extends _RPSMessage {

    public F6_01_01() {
        super();
    }

    public F6_01_01(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected String convertToEventImpl(String channelId, String channelTypeId, String lastEvent,
            Configuration config) {
        if (!isValid()) {
            return null;
        }

        return getBit(bytes[0], 4) ? CommonTriggerEvents.PRESSED : CommonTriggerEvents.RELEASED;
    }

    @Override
    protected boolean validateData(byte[] bytes) {
        return super.validateData(bytes) && !getBit(bytes[0], 7);
    }
}
