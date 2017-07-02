/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russoundserial.handler;

import java.util.LinkedHashMap;
import java.util.Map;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import com.thejholmes.russound.RussoundAction;
import com.thejholmes.russound.RussoundCommander;
import com.thejholmes.russound.RussoundZoneInfoListener;
import com.thejholmes.russound.Zone;
import com.thejholmes.russound.ZoneInfo;
import com.thejholmes.russound.serial.SerialCommandReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This does the actual communicating to the Russound device from the various Things that represent
 * the zones.
 *
 * @author Jason Holmes - Initial contribution
 */
public class RussoundSerialBridgeHandler extends BaseBridgeHandler {
    private final RussoundCommander commander;
    private final SerialCommandReceiver commandReceiver;
    private final ZoneListener zoneListener;

    public RussoundSerialBridgeHandler(Bridge bridge, RussoundCommander commander,
            SerialCommandReceiver commandReceiver, ZoneListener zoneListener) {
        super(bridge);
        this.commander = commander;
        this.commandReceiver = commandReceiver;
        this.zoneListener = zoneListener;
    }

    public RussoundCommander getCommander() {
        return commander;
    }

    @Override
    public void initialize() {
        super.initialize();
        this.commandReceiver.start();
    }

    @Override
    public void dispose() {
        this.commander.destroy();
        this.commandReceiver.stop();
        updateStatus(ThingStatus.OFFLINE);
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        throw new IllegalArgumentException("Bridge handler doesn't know about any native commands.");
    }

    public void register(Zone zone, RussoundSerialZoneHandler zoneHandler) {
        zoneListener.handlers.put(zone.getZoneNumber(), zoneHandler);
    }

    public void unregister(Zone zone) {
        zoneListener.handlers.remove(zone.getZoneNumber());
    }

    /**
     * Listens for updated {@link ZoneInfo} packets and passes them to the appropriate
     * {@link RussoundSerialZoneHandler}.
     *
     * @author Jason Holmes
     */
    public static class ZoneListener implements RussoundZoneInfoListener {
        private final Logger logger = LoggerFactory.getLogger(ZoneListener.class);
        private final Map<Integer, RussoundSerialZoneHandler> handlers = new LinkedHashMap<>();

        @Override
        public void onNext(RussoundAction russoundAction) {
            // We only care about the zone updates.
        }

        @Override
        public void updated(ZoneInfo zoneInfo) {
            logger.debug("Received ZoneInfo for zone: {}", zoneInfo);
            RussoundSerialZoneHandler zoneHandler = handlers.get(zoneInfo.getZone());

            if (zoneHandler != null) {
                zoneHandler.update(zoneInfo);
            }
        }
    }
}
