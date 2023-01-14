/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.smsmodem.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smsmodem.internal.handler.SMSConversationHandler;
import org.openhab.binding.smsmodem.internal.handler.SMSModemBridgeHandler;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link SMSModemHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Gwendal ROULLEAU - Initial contribution
 */
@Component(configurationPid = "binding.smsmodem", service = ThingHandlerFactory.class)
@NonNullByDefault
public class SMSModemHandlerFactory extends BaseThingHandlerFactory {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>();
    {
        SUPPORTED_THING_TYPES_UIDS.add(SMSConversationHandler.SUPPORTED_THING_TYPES_UIDS);
        SUPPORTED_THING_TYPES_UIDS.addAll(SMSModemBridgeHandler.SUPPORTED_THING_TYPES_UIDS);
    }

    private @NonNullByDefault({}) SerialPortManager serialPortManager;

    @Reference
    protected void setSerialPortManager(final SerialPortManager serialPortManager) {
        this.serialPortManager = serialPortManager;
    }

    protected void unsetSerialPortManager(final SerialPortManager serialPortManager) {
        this.serialPortManager = null;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (SMSModemBridgeHandler.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new SMSModemBridgeHandler((Bridge) thing, serialPortManager);
        } else if (SMSConversationHandler.SUPPORTED_THING_TYPES_UIDS.equals(thingTypeUID)) {
            return new SMSConversationHandler(thing);
        }

        return null;
    }
}
