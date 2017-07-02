/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russoundserial.handler;

import com.thejholmes.russound.Balance;
import com.thejholmes.russound.BassLevel;
import com.thejholmes.russound.Loudness;
import com.thejholmes.russound.PowerChange;
import com.thejholmes.russound.RussoundCommander;
import com.thejholmes.russound.Source;
import com.thejholmes.russound.TrebleLevel;
import com.thejholmes.russound.VolumeChange;
import com.thejholmes.russound.Zone;
import com.thejholmes.russound.ZoneInfo;
import java.util.concurrent.ScheduledFuture;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.eclipse.smarthome.core.library.types.IncreaseDecreaseType.INCREASE;
import static org.openhab.binding.russoundserial.RussoundSerialBindingConstants.CHANNEL_ZONEBALANCE;
import static org.openhab.binding.russoundserial.RussoundSerialBindingConstants.CHANNEL_ZONEBASS;
import static org.openhab.binding.russoundserial.RussoundSerialBindingConstants.CHANNEL_ZONELOUDNESS;
import static org.openhab.binding.russoundserial.RussoundSerialBindingConstants.CHANNEL_ZONEPOWER;
import static org.openhab.binding.russoundserial.RussoundSerialBindingConstants.CHANNEL_ZONESOURCE;
import static org.openhab.binding.russoundserial.RussoundSerialBindingConstants.CHANNEL_ZONETREBLE;
import static org.openhab.binding.russoundserial.RussoundSerialBindingConstants.CHANNEL_ZONETURNONVOLUME;
import static org.openhab.binding.russoundserial.RussoundSerialBindingConstants.CHANNEL_ZONEVOLUME;

/**
 * A new RussoundSerialZoneHandler is created per zone on the Russound device. Each of these zones
 * communicates/updates state via the RussoundSerialBridgeHandler.
 *
 * Note: A status request is issued after a command is issued. We will continue to request the status until
 * we hear back from the device. While 100ms sounds frequent, it's only run (normally once) immediately after
 * a command.
 *
 * @author Jason Holmes - Initial contribution
 */
public class RussoundSerialZoneHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(RussoundSerialZoneHandler.class);
    private final Zone zone;
    private ScheduledFuture<?> refreshFuture;

    public RussoundSerialZoneHandler(Thing thing, Zone zone) {
        super(thing);
        this.zone = zone;
    }

    @Override
    public void initialize() {
        ((RussoundSerialBridgeHandler) getBridge().getHandler()).register(zone, this);
        scheduleRefresh();
    }

    @Override
    public void dispose() {
        ((RussoundSerialBridgeHandler) getBridge().getHandler()).unregister(zone);
        super.dispose();
    }

    public void update(ZoneInfo zoneInfo) {
        if (refreshFuture != null) {
            refreshFuture.cancel(true);
        }

        updateStatus(ThingStatus.ONLINE);

        updateState(CHANNEL_ZONEPOWER, zoneInfo.getPower() ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_ZONESOURCE, new DecimalType(zoneInfo.getSource()));
        updateState(CHANNEL_ZONEVOLUME, new PercentType(zoneInfo.getVolume()));

        updateState(CHANNEL_ZONEBASS, new DecimalType(zoneInfo.getBass()));
        updateState(CHANNEL_ZONETREBLE, new DecimalType(zoneInfo.getTreble()));
        updateState(CHANNEL_ZONEBALANCE, new DecimalType(zoneInfo.getBalance()));
        updateState(CHANNEL_ZONELOUDNESS, zoneInfo.getLoudness() ? OnOffType.ON : OnOffType.OFF);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduleRefresh();
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_ZONEPOWER:
                if (command instanceof OnOffType) {
                    PowerChange level = (command == OnOffType.ON) ? PowerChange.ON : PowerChange.OFF;
                    commander().power(zone, level);
                } else {
                    logger.info("Received a ZONE POWER channel command with a non OnOffType: {}", command);
                }
                break;
            case CHANNEL_ZONEVOLUME:
                if (command instanceof IncreaseDecreaseType) {
                    VolumeChange change = command == IncreaseDecreaseType.INCREASE ? new VolumeChange.Up()
                            : new VolumeChange.Down();
                    commander().volume(zone, change);
                } else if (command instanceof PercentType) {
                    int volumeLevel = ((PercentType) command).intValue();
                    commander().volume(zone, new VolumeChange.Set(volumeLevel));
                } else {
                    logger.info(
                            "Received a ZONE VOLUME channel command with a non IncreaseDecreaseType/PercentType: {}",
                            command);
                }
                break;
            case CHANNEL_ZONESOURCE:
                if (command instanceof DecimalType) {
                    Source source = new Source(zone.getControllerId(), ((DecimalType) command).intValue());
                    commander().source(zone, source);
                } else {
                    logger.info("Received a ZONE SOURCE channel command with a non DecimalType: {}", command);
                }
                break;
            case CHANNEL_ZONEBASS:
                if (command instanceof IncreaseDecreaseType) {
                    BassLevel level = (command == INCREASE) ? new BassLevel.Up() : new BassLevel.Down();
                    commander().bass(zone, level);
                } else if (command instanceof DecimalType) {
                    int bassLevel = ((DecimalType) command).intValue();
                    commander().bass(zone, new BassLevel.Set(bassLevel));
                } else {
                    logger.info("Received a ZONE BASS channel command with a non IncreaseDecreaseType/DecimalType: {}",
                            command);
                }
                break;
            case CHANNEL_ZONETREBLE:
                if (command instanceof IncreaseDecreaseType) {
                    TrebleLevel level = (command == INCREASE) ? new TrebleLevel.Up() : new TrebleLevel.Down();
                    commander().treble(zone, level);
                } else if (command instanceof DecimalType) {
                    int level = ((DecimalType) command).intValue();
                    commander().treble(zone, new TrebleLevel.Set(level));
                } else {
                    logger.info(
                            "Received a ZONE TREBLE channel command with a non IncreaseDecreaseType/DecimalType: {}",
                            command);
                }
                break;
            case CHANNEL_ZONEBALANCE:
                if (command instanceof IncreaseDecreaseType) {
                    Balance level = (command == INCREASE) ? new Balance.Right() : new Balance.Left();
                    commander().balance(zone, level);
                } else if (command instanceof DecimalType) {
                    int level = ((DecimalType) command).intValue();
                    commander().balance(zone, new Balance.Set(level));
                } else {
                    logger.info(
                            "Received a ZONE BALANCE channel command with a non IncreaseDecreaseType/DecimalType: {}",
                            command);
                }
                break;
            case CHANNEL_ZONELOUDNESS:
                if (command instanceof OnOffType) {
                    Loudness level = (command == OnOffType.ON) ? Loudness.ON : Loudness.OFF;
                    commander().loudness(zone, level);
                } else {
                    logger.info("Received a ZONE LOUDNESS channel command with a non OnOffType: {}", command);
                }
                break;
            case CHANNEL_ZONETURNONVOLUME:
                if (command instanceof PercentType) {
                    int volumeLevel = ((PercentType) command).intValue();
                    commander().initialVolume(zone, new VolumeChange.Set(volumeLevel));
                } else {
                    logger.info("Received a ZONE TURNONVOLUME channel command with a non PercentType: {}", command);
                }
                break;
            default:
                logger.info("Unknown/Unsupported Channel id: {}", channelUID.getId());
        }

        scheduleRefresh();
    }

    /**
     * Every command requires a status request. Continue to request status until we receive an update.
     */
    private void scheduleRefresh() {
        if (refreshFuture == null || refreshFuture.isCancelled()) {
            refreshFuture = scheduler.scheduleWithFixedDelay(() -> commander().requestStatus(zone), 0, 200, MILLISECONDS);
        }
    }

    private RussoundCommander commander() {
        return ((RussoundSerialBridgeHandler) getBridge().getHandler()).getCommander();
    }
}
