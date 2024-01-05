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
package org.openhab.binding.cp750.internal;

import static org.openhab.binding.cp750.internal.CP750BindingConstants.*;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.*;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.cybso.cp750.CP750Client;
import de.cybso.cp750.CP750Field;
import de.cybso.cp750.CP750InputMode;
import de.cybso.cp750.CP750Listener;

/**
 * The {@link CP750Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Roland Tapken - Initial contribution
 */
public class CP750Handler extends BaseThingHandler implements CP750Listener {

    private final Logger logger = LoggerFactory.getLogger(CP750Handler.class);

    private @Nullable CP750Configuration config;

    private @Nullable CP750Client client;

    private @Nullable ScheduledFuture scheduleFuture;

    public CP750Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (this.client != null) {
            try {
                if (command instanceof RefreshType) {
                    this.client.refresh();
                    return;
                }

                switch (channelUID.getId()) {
                    case CHANNEL_INPUT -> {
                        if (command instanceof StringType) {
                            CP750InputMode mode = CP750InputMode.byValue(((StringType) command).toString());
                            if (mode != null) {
                                this.client.setInputMode(mode);
                            }
                        }
                    }
                    case CHANNEL_ANALOG -> {
                        if (command == OnOffType.ON) {
                            this.client.setInputMode(CP750InputMode.ANALOG);
                        }
                    }
                    case CHANNEL_DIG1 -> {
                        if (command == OnOffType.ON) {
                            this.client.setInputMode(CP750InputMode.DIG_1);
                        }
                    }
                    case CHANNEL_DIG2 -> {
                        if (command == OnOffType.ON) {
                            this.client.setInputMode(CP750InputMode.DIG_2);
                        }
                    }
                    case CHANNEL_DIG3 -> {
                        if (command == OnOffType.ON) {
                            this.client.setInputMode(CP750InputMode.DIG_3);
                        }
                    }
                    case CHANNEL_DIG4 -> {
                        if (command == OnOffType.ON) {
                            this.client.setInputMode(CP750InputMode.DIG_4);
                        }
                    }
                    case CHANNEL_MIC -> {
                        if (command == OnOffType.ON) {
                            this.client.setInputMode(CP750InputMode.MIC);
                        }
                    }
                    case CHANNEL_NONSYNC -> {
                        if (command == OnOffType.ON) {
                            this.client.setInputMode(CP750InputMode.NON_SYNC);
                        }
                    }
                    case CHANNEL_MUTE -> {
                        if (command instanceof OnOffType) {
                            this.client.setMuted(command == OnOffType.ON);
                        }
                    }
                    case CHANNEL_FADER -> {
                        if (command instanceof DecimalType) {
                            this.client.setFader(((DecimalType) command).intValue());
                        }
                        if (command instanceof IncreaseDecreaseType) {
                            this.client.setFaderDelta(command == IncreaseDecreaseType.INCREASE ? 1 : -1);
                        }
                    }
                }
            } catch (IOException e) {
                disposeClient(e);
            }
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(CP750Configuration.class);
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            try {
                if (this.scheduleFuture != null) {
                    this.scheduleFuture.cancel(true);
                    this.scheduleFuture = null;
                }
                if (this.client != null) {
                    this.client.close();
                    this.client = null;
                }

                this.client = new CP750Client(config.hostname, config.port);
                for (CP750Field field : CP750Field.values()) {
                    this.client.addListener(field, this);
                }
                this.client.refresh();
                logger.info("Connected CP750 Client to {}:{}. Server version is {}", config.hostname, config.port,
                        this.client.getVersion());
                updateStatus(ThingStatus.ONLINE);

                // Start scheduler
                if (config.refreshInterval > 0) {
                    scheduleFuture = scheduler.scheduleWithFixedDelay(() -> {
                        refresh();
                    }, config.refreshInterval, config.refreshInterval, TimeUnit.SECONDS);
                }

            } catch (IOException e) {
                disposeClient(e);
            }
        });
    }

    private void disposeClient(Exception e) {
        if (this.scheduleFuture != null) {
            try {
                this.scheduleFuture.cancel(true);
            } catch (Exception ignore) {
            }
            this.scheduleFuture = null;
        }
        if (this.client != null) {
            this.client.removeListener(this);
            try {
                this.client.close();
            } catch (Exception ignore) {
            }
            this.client = null;
        }
        if (e == null) {
            updateStatus(ThingStatus.OFFLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Communication error to " + config.hostname + ":" + config.port);
            logger.error("CP750 on " + config.hostname + ":" + config.port, e);
            tryToReconnect();
        }
    }

    /**
     * Tries to reconnect until the device becomes available
     */
    private void tryToReconnect() {
        if (config.reconnectInterval > 0) {
            logger.info("CP750 at {}:{} try to reconnect in {} seconds", config.hostname, config.port,
                    config.reconnectInterval);
            scheduler.schedule(() -> {
                if (getThing().getStatus() == ThingStatus.OFFLINE) {
                    // Will call tryToReconnect if something goes wrong
                    initialize();
                }
            }, config.reconnectInterval, TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        disposeClient(null);
        super.dispose();
    }

    public void refresh() {
        try {
            if (this.client != null) {
                this.client.refresh();
            }
        } catch (IOException e) {
            disposeClient(e);
        }
    }

    /**
     * Handles input data
     */
    @Override
    public void receive(CP750Field field, String value) {
        logger.debug("CP750 at {}:{} received {} with value {}", config.hostname, config.port, field, value);
        switch (field) {
            case SYS_MUTE -> updateState(CHANNEL_MUTE, OnOffType.from(value));
            case SYSINFO_VERSION -> updateState(CHANNEL_VERSION, StringType.valueOf(value));
            case SYS_FADER -> updateState(CHANNEL_FADER, PercentType.valueOf(value));
            case SYS_INPUT_MODE -> {
                CP750InputMode mode = CP750InputMode.byValue(value);
                if (mode != null) {
                    updateState(CHANNEL_INPUT, StringType.valueOf(value));
                    OnOffType analog = OnOffType.OFF;
                    OnOffType dig1 = OnOffType.OFF;
                    OnOffType dig2 = OnOffType.OFF;
                    OnOffType dig3 = OnOffType.OFF;
                    OnOffType dig4 = OnOffType.OFF;
                    OnOffType nonsync = OnOffType.OFF;
                    OnOffType mic = OnOffType.OFF;
                    switch (CP750InputMode.byValue(value)) {
                        case ANALOG -> analog = OnOffType.ON;
                        case DIG_1 -> dig1 = OnOffType.ON;
                        case DIG_2 -> dig2 = OnOffType.ON;
                        case DIG_3 -> dig3 = OnOffType.ON;
                        case DIG_4 -> dig4 = OnOffType.ON;
                        case MIC -> mic = OnOffType.ON;
                        case NON_SYNC -> nonsync = OnOffType.ON;
                    }
                    updateState(CHANNEL_ANALOG, analog);
                    updateState(CHANNEL_DIG1, dig1);
                    updateState(CHANNEL_DIG2, dig2);
                    updateState(CHANNEL_DIG3, dig3);
                    updateState(CHANNEL_DIG4, dig4);
                    updateState(CHANNEL_MIC, mic);
                    updateState(CHANNEL_NONSYNC, nonsync);
                }
            }
        }
    }
}
