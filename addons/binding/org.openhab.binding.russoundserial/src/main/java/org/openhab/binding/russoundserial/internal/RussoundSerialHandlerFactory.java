/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russoundserial.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import com.thejholmes.russound.Russound;
import com.thejholmes.russound.RussoundCommandSender;
import com.thejholmes.russound.RussoundCommander;
import com.thejholmes.russound.RussoundTranslator;
import com.thejholmes.russound.Zone;
import com.thejholmes.russound.serial.SerialCommandReceiver;
import com.thejholmes.russound.serial.SerialCommandSender;
import org.openhab.binding.russoundserial.handler.RussoundSerialBridgeHandler;
import org.openhab.binding.russoundserial.handler.RussoundSerialBridgeHandler.ZoneListener;
import org.openhab.binding.russoundserial.handler.RussoundSerialZoneHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openhab.binding.russoundserial.RussoundSerialBindingConstants.SERIAL_BRIDGE;
import static org.openhab.binding.russoundserial.RussoundSerialBindingConstants.ZONE_HANDLER;

/**
 * The {@link RussoundSerialHandlerFactory} is responsible for creating things and thing handlers.
 *
 * There is one RussoundSerialBridgeHandler per binding, and one RussoundSerialZoneHandler
 * created per zone.
 *
 * @author Jason Holmes - Initial contribution
 */
public class RussoundSerialHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(Arrays.asList(SERIAL_BRIDGE, ZONE_HANDLER));

    private final Logger logger = LoggerFactory.getLogger(RussoundSerialHandlerFactory.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(SERIAL_BRIDGE)) {
            String serialPort = (String) thing.getConfiguration().get("serialPort");
            File deviceLocation = new File(serialPort);

            try {
                RussoundCommandSender commandSender = SerialCommandSender.Factory.fromFile(deviceLocation);
                RussoundCommander commander = Russound.Companion.sender("russound-serial-bridge", commandSender);

                ZoneListener zoneInfoListener = new ZoneListener();
                RussoundTranslator translator = Russound.Companion.receiver("russound-serial-bridge", zoneInfoListener);
                SerialCommandReceiver commandReceiver = new SerialCommandReceiver(translator,
                        new FileInputStream(deviceLocation));

                return new RussoundSerialBridgeHandler((Bridge) thing, commander, commandReceiver, zoneInfoListener);
            } catch (FileNotFoundException e) {
                logger.info("Couldn't find serialPort at {}", serialPort, e);
                return null;
            }
        } else if (thingTypeUID.equals(ZONE_HANDLER)) {
            // Hardcoding Controller 0 since that's all I have for now.
            BigDecimal zoneNumber = (BigDecimal) thing.getConfiguration().get("zoneNumber");
            return new RussoundSerialZoneHandler(thing, new Zone(0, zoneNumber.intValue()));
        }

        throw new IllegalArgumentException("Unsupported thing type: " + thing);
    }
}
