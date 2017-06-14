/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tadoac.handler;

import static org.openhab.binding.tadoac.TadoACBindingConstants.*;

import java.math.BigDecimal;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhdortmund.seelab.tadoac.TadoACConnector;
import de.fhdortmund.seelab.tadoac.Model.TadoACFanSpeed;
import de.fhdortmund.seelab.tadoac.Model.TadoACMode;
import de.fhdortmund.seelab.tadoac.Model.TadoACPower;
import de.fhdortmund.seelab.tadoac.Model.TadoACSetting;
import de.fhdortmund.seelab.tadoac.Model.TadoACTemperature;

/**
 * The {@link TadoACHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jonas Fleck - Initial contribution
 */
public class TadoACHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TadoACHandler.class);
    private TadoACSetting setting;
    private ScheduledFuture<?> sendFuture;
    private ScheduledFuture<?> refreshFuture;

    public TadoACHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_POWER) && command instanceof OnOffType) {
            OnOffType onOff = (OnOffType) command;
            setting.setPower(onOff == OnOffType.ON ? TadoACPower.ON : TadoACPower.OFF);
        } else if (channelUID.getId().equals(CHANNEL_MODE) && command instanceof DecimalType) {
            int mode = ((DecimalType) command).intValue();
            switch (mode) {
                case 1:
                    setting.setMode(TadoACMode.COOL);
                    break;
                case 2:
                    setting.setMode(TadoACMode.DRY);
                    break;
                case 3:
                    setting.setMode(TadoACMode.FAN);
                    break;
                default:
                    logger.error("mode channel out of range");
            }
        } else if (channelUID.getId().equals(CHANNEL_TEMP) && command instanceof DecimalType) {
            float temperature = ((DecimalType) command).floatValue();
            TadoACTemperature tadotemp = new TadoACTemperature();
            tadotemp.setCelsius(temperature);
            setting.setTemperature(tadotemp);
        } else if (channelUID.getId().equals(CHANNEL_FANSPEED) && command instanceof DecimalType) {
            int speed = ((DecimalType) command).intValue();
            switch (speed) {
                case 1:
                    setting.setFanSpeed(TadoACFanSpeed.LOW);
                    break;
                case 2:
                    setting.setFanSpeed(TadoACFanSpeed.MIDDLE);
                    break;
                case 3:
                    setting.setFanSpeed(TadoACFanSpeed.HIGH);
                    break;
                default:
                    logger.error("mode channel out of range");
            }
        }

        synchronized (this) {
            if (sendFuture != null) {
                sendFuture.cancel(false);
            }
            sendFuture = scheduler.schedule(() -> sendSetting(), 1, TimeUnit.SECONDS);
        }
    }

    private synchronized void sendSetting() {
        try {
            String username = (String) getConfig().get("username");
            String password = (String) getConfig().get("password");
            int homeid = ((BigDecimal) getConfig().get("homeid")).intValue();
            int zoneid = ((BigDecimal) getConfig().get("zoneid")).intValue();
            if (username == null || password == null) {
                logger.error("login credentials not configured");
                updateStatus(ThingStatus.OFFLINE);
                return;
            }

            // validate setting
            if (setting.getPower() == null) {
                logger.error("No power setting");
                return;

            }
            if (setting.getPower() == TadoACPower.ON) {
                if (setting.getMode() == null) {
                    logger.error("No mode setting");
                    return;
                }
                if (setting.getFanSpeed() == null) {
                    logger.error("No fan setting");
                    return;
                }
                if (setting.getMode() == TadoACMode.COOL && setting.getTemperature() == null) {
                    logger.error("No temperature setting");
                    return;
                }
            }
            TadoACConnector connector = new TadoACConnector(username, password);
            connector.setSetting(homeid, zoneid, setting);
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("could not send state", e);
        } finally {
            sendFuture = null;
        }

    }

    private synchronized void refreshSetting() {
        try {
            String username = (String) getConfig().get("username");
            String password = (String) getConfig().get("password");
            int homeid = ((BigDecimal) getConfig().get("homeid")).intValue();
            int zoneid = ((BigDecimal) getConfig().get("zoneid")).intValue();
            if (username == null || password == null) {
                logger.error("login credentials not configured");
                updateStatus(ThingStatus.OFFLINE);
                return;
            }
            TadoACConnector connector = new TadoACConnector(username, password);
            TadoACSetting newSetting = connector.getSetting(homeid, zoneid);

            // Check each part and copy them to the previous state (No loss of data)
            if (newSetting.getPower() != null) {
                setting.setPower(newSetting.getPower());
                updateState(CHANNEL_POWER, newSetting.getPower() == TadoACPower.ON ? OnOffType.ON : OnOffType.OFF);
            }
            if (newSetting.getFanSpeed() != null) {
                setting.setFanSpeed(newSetting.getFanSpeed());
                int fanSpeed = 1;
                switch (newSetting.getFanSpeed()) {
                    case LOW:
                        fanSpeed = 1;
                        break;
                    case MIDDLE:
                        fanSpeed = 2;
                        break;
                    case HIGH:
                        fanSpeed = 3;
                        break;
                    default:
                        break;
                }
                updateState(CHANNEL_FANSPEED, new DecimalType(fanSpeed));
            }
            if (newSetting.getMode() != null) {
                setting.setMode(newSetting.getMode());
                int mode = 1;
                switch (newSetting.getMode()) {
                    case COOL:
                        mode = 1;
                        break;
                    case DRY:
                        mode = 2;
                        break;
                    case FAN:
                        mode = 3;
                        break;
                    default:
                        break;
                }
                updateState(CHANNEL_MODE, new DecimalType(mode));
            }
            if (newSetting.getTemperature() != null) {
                setting.setTemperature(newSetting.getTemperature());
                updateState(CHANNEL_TEMP, new DecimalType(newSetting.getTemperature().getCelsius()));
            }
        } catch (Exception e) {
            logger.error("error while polling for new state", e);
        }
    }

    @Override
    public void initialize() {
        setting = new TadoACSetting();
        refreshFuture = scheduler.scheduleAtFixedRate(() -> refreshSetting(), 3, 60, TimeUnit.SECONDS);
        updateStatus(ThingStatus.ONLINE);

    }
}
