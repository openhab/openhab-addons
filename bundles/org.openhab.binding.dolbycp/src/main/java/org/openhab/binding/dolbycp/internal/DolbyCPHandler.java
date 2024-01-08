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
package org.openhab.binding.dolbycp.internal;

import static org.openhab.binding.dolbycp.internal.DolbyCPBindingConstants.*;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
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
 * The {@link DolbyCPHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Roland Tapken - Initial contribution
 */
@NonNullByDefault
public class DolbyCPHandler extends BaseThingHandler implements CP750Listener {

    private final Logger logger = LoggerFactory.getLogger(DolbyCPHandler.class);

    private @Nullable DolbyCPConfiguration config;

    private @Nullable CP750Client client;

    private @Nullable ScheduledFuture<?> scheduleFuture;

    private @Nullable CP750InputMode currentInputMode;

    public DolbyCPHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        CP750Client client = this.client;
        if (client != null) {
            try {
                if (command instanceof RefreshType) {
                    client.refresh();
                    return;
                }

                switch (channelUID.getId()) {
                    case CHANNEL_INPUT -> {
                        if (command instanceof StringType commandAsStringType) {
                            CP750InputMode mode = CP750InputMode.byValue(commandAsStringType.toString());
                            if (mode != null) {
                                client.setInputMode(mode);
                            }
                        }
                    }
                    case CHANNEL_ANALOG -> {
                        if (command == OnOffType.ON) {
                            client.setInputMode(CP750InputMode.ANALOG);
                        } else if (currentInputMode == CP750InputMode.ANALOG) {
                            client.setInputMode(CP750InputMode.LAST);
                        }
                    }
                    case CHANNEL_DIG1 -> {
                        if (command == OnOffType.ON) {
                            client.setInputMode(CP750InputMode.DIG_1);
                        } else if (currentInputMode == CP750InputMode.DIG_1) {
                            client.setInputMode(CP750InputMode.LAST);
                        }
                    }
                    case CHANNEL_DIG2 -> {
                        if (command == OnOffType.ON) {
                            client.setInputMode(CP750InputMode.DIG_2);
                        } else if (currentInputMode == CP750InputMode.DIG_2) {
                            client.setInputMode(CP750InputMode.LAST);
                        }
                    }
                    case CHANNEL_DIG3 -> {
                        if (command == OnOffType.ON) {
                            client.setInputMode(CP750InputMode.DIG_3);
                        } else if (currentInputMode == CP750InputMode.DIG_3) {
                            client.setInputMode(CP750InputMode.LAST);
                        }
                    }
                    case CHANNEL_DIG4 -> {
                        if (command == OnOffType.ON) {
                            client.setInputMode(CP750InputMode.DIG_4);
                        } else if (currentInputMode == CP750InputMode.DIG_4) {
                            client.setInputMode(CP750InputMode.LAST);
                        }
                    }
                    case CHANNEL_MIC -> {
                        if (command == OnOffType.ON) {
                            client.setInputMode(CP750InputMode.MIC);
                        } else if (currentInputMode == CP750InputMode.MIC) {
                            client.setInputMode(CP750InputMode.LAST);
                        }
                    }
                    case CHANNEL_NONSYNC -> {
                        if (command == OnOffType.ON) {
                            client.setInputMode(CP750InputMode.NON_SYNC);
                        } else if (currentInputMode == CP750InputMode.NON_SYNC) {
                            client.setInputMode(CP750InputMode.LAST);
                        }
                    }
                    case CHANNEL_MUTE -> {
                        if (command instanceof OnOffType) {
                            client.setMuted(command == OnOffType.ON);
                        }
                    }
                    case CHANNEL_FADER -> {
                        if (command instanceof DecimalType) {
                            client.setFader(((DecimalType) command).intValue());
                        }
                        if (command instanceof IncreaseDecreaseType) {
                            client.setFaderDelta(command == IncreaseDecreaseType.INCREASE ? 1 : -1);
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
        final DolbyCPConfiguration config = getConfigAs(DolbyCPConfiguration.class);
        this.config = config;
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            try {
                ScheduledFuture<?> scheduleFuture = this.scheduleFuture;
                this.scheduleFuture = null;
                if (scheduleFuture != null) {
                    scheduleFuture.cancel(true);
                }

                CP750Client client = this.client;
                this.client = null;
                if (client != null) {
                    client.close();
                }

                client = new CP750Client(config.hostname, config.port);
                this.client = client;
                for (CP750Field field : CP750Field.values()) {
                    client.addListener(field, this);
                }
                client.refresh();
                logger.info("Connected CP750 Client to {}:{}. OS version is {}", config.hostname, config.port,
                        client.getVersion());
                updateStatus(ThingStatus.ONLINE);

                // Start scheduler
                if (config.refreshInterval > 0) {
                    this.scheduleFuture = scheduler.scheduleWithFixedDelay(this::refresh, config.refreshInterval,
                            config.refreshInterval, TimeUnit.SECONDS);
                }
            } catch (IOException e) {
                disposeClient(e);
            }
        });
    }

    private void disposeClient(@Nullable Exception e) {
        ScheduledFuture<?> scheduleFuture = this.scheduleFuture;
        this.scheduleFuture = null;
        if (scheduleFuture != null) {
            try {
                scheduleFuture.cancel(true);
            } catch (Exception ignore) {
            }
        }
        CP750Client client = this.client;
        this.client = null;
        if (client != null) {
            client.removeListener(this);
            try {
                client.close();
            } catch (Exception ignore) {
            }
        }
        if (e == null) {
            updateStatus(ThingStatus.OFFLINE);
        } else {
            DolbyCPConfiguration config = this.config;
            String hostname = config == null ? "unknown" : config.hostname;
            int port = config == null ? -1 : config.port;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Communication error to " + hostname + ":" + port);
            logger.error("CP750 on {}:{}", hostname, port, e);
            tryToReconnect();
        }
    }

    /**
     * Tries to reconnect until the device becomes available
     */
    private void tryToReconnect() {
        DolbyCPConfiguration config = this.config;
        if (config != null && config.reconnectInterval > 0) {
            logger.debug("DolbyCP at {}:{} try to reconnect in {} seconds", config.hostname, config.port,
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
            CP750Client client = this.client;
            if (client != null) {
                client.refresh();
            }
        } catch (IOException e) {
            disposeClient(e);
        }
    }

    /**
     * Handles input data
     */
    @Override
    public void receive(@Nullable CP750Field field, @Nullable String value) {
        DolbyCPConfiguration config = this.config;
        String hostname = config == null ? "unknown" : config.hostname;
        int port = config == null ? -1 : config.port;

        logger.debug("DolbyCP at {}:{} received {} with value {}", hostname, port, field, value);
        if (field == null || value == null) {
            return;
        }

        switch (field) {
            case SYS_MUTE -> updateState(CHANNEL_MUTE, OnOffType.from(value));
            case SYSINFO_VERSION -> updateState(CHANNEL_VERSION, StringType.valueOf(value));
            case SYS_FADER -> updateState(CHANNEL_FADER, PercentType.valueOf(value));
            case SYS_INPUT_MODE -> {
                CP750InputMode mode = CP750InputMode.byValue(value);
                if (mode != null) {
                    this.currentInputMode = mode;
                    updateState(CHANNEL_INPUT, StringType.valueOf(value));
                    OnOffType analog = OnOffType.OFF;
                    OnOffType dig1 = OnOffType.OFF;
                    OnOffType dig2 = OnOffType.OFF;
                    OnOffType dig3 = OnOffType.OFF;
                    OnOffType dig4 = OnOffType.OFF;
                    OnOffType nonsync = OnOffType.OFF;
                    OnOffType mic = OnOffType.OFF;
                    switch (mode) {
                        case ANALOG -> analog = OnOffType.ON;
                        case DIG_1 -> dig1 = OnOffType.ON;
                        case DIG_2 -> dig2 = OnOffType.ON;
                        case DIG_3 -> dig3 = OnOffType.ON;
                        case DIG_4 -> dig4 = OnOffType.ON;
                        case MIC -> mic = OnOffType.ON;
                        case NON_SYNC -> nonsync = OnOffType.ON;
                        default -> {
                            // Ignore unknown value
                        }
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
            default -> {
                // Ignore unknown value
            }
        }
    }
}
