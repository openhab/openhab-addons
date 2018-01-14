/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazonechocontrol.handler;

import static org.openhab.binding.amazonechocontrol.AmazonEchoControlBindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.HttpException;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates.BluetoothState;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates.PairedDevice;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonDevices.Device;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonMediaState;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonMediaState.QueueEntry;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlayerState;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlayerState.PlayerInfo;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlayerState.PlayerInfo.InfoText;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlayerState.PlayerInfo.MainArt;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlayerState.PlayerInfo.Provider;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlayerState.PlayerInfo.Volume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EchoHandler} is responsible for the handling of the echo device
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class EchoHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EchoHandler.class);

    private @Nullable Device device;
    private @Nullable Connection connection;
    private @Nullable ScheduledFuture<?> updateStateJob;
    private @Nullable String lastKnownRadioStationId;
    private @Nullable String lastKnownBluetoothId;
    private int lastKnownVolume = 25;
    private @Nullable BluetoothState bluetoothState;
    private boolean disableUpdate = false;

    public EchoHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.info("Amazon Echo Control Binding initialized");
        updateStatus(ThingStatus.ONLINE);
    }

    public void intialize(Connection connection, @Nullable Device deviceJson) {
        this.connection = connection;
        this.device = deviceJson;
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> updateStateJob = this.updateStateJob;
        this.updateStateJob = null;
        if (updateStateJob != null) {
            updateStateJob.cancel(false);
        }
        super.dispose();
    }

    public String findSerialNumber() {
        String id = (String) getConfig().get(DEVICE_PROPERTY_SERIAL_NUMBER);
        if (id == null) {
            return "";
        }
        return id;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {

            int waitForUpdate = 1000;
            boolean needBluetoothRefresh = false;
            ScheduledFuture<?> updateStateJob = this.updateStateJob;
            String lastKnownBluetoothId = this.lastKnownBluetoothId;

            this.updateStateJob = null;
            if (updateStateJob != null) {
                updateStateJob.cancel(false);
            }

            Connection temp = connection;
            if (temp == null) {
                return;
            }
            Device device = this.device;
            if (device == null) {
                return;
            }

            // Player commands
            String channelId = channelUID.getId();
            if (channelId.equals(CHANNEL_PLAYER)) {
                if (command == PlayPauseType.PAUSE || command == OnOffType.OFF) {
                    temp.command(device, "{\"type\":\"PauseCommand\"}");
                } else if (command == PlayPauseType.PLAY || command == OnOffType.ON) {
                    temp.command(device, "{\"type\":\"PlayCommand\"}");
                } else if (command == NextPreviousType.NEXT) {
                    temp.command(device, "{\"type\":\"NextCommand\"}");
                } else if (command == NextPreviousType.PREVIOUS) {
                    temp.command(device, "{\"type\":\"PreviousCommand\"}");
                } else if (command == RewindFastforwardType.FASTFORWARD) {
                    temp.command(device, "{\"type\":\"ForwardCommand\"}");
                } else if (command == RewindFastforwardType.REWIND) {
                    temp.command(device, "{\"type\":\"RewindCommand\"}");
                }
            }
            // Volume commands
            if (channelId.equals(CHANNEL_VOLUME)) {
                if (command instanceof PercentType) {
                    PercentType value = (PercentType) command;
                    int volume = value.intValue();
                    temp.command(device, "{\"type\":\"VolumeLevelCommand\",\"volumeLevel\":" + volume + "}");
                } else if (command == OnOffType.OFF) {
                    temp.command(device, "{\"type\":\"VolumeLevelCommand\",\"volumeLevel\":" + 0 + "}");
                } else if (command == OnOffType.ON) {
                    temp.command(device, "{\"type\":\"VolumeLevelCommand\",\"volumeLevel\":" + lastKnownVolume + "}");
                } else if (command == IncreaseDecreaseType.INCREASE) {
                    if (lastKnownVolume < 100) {
                        lastKnownVolume++;
                        updateState(CHANNEL_VOLUME, new PercentType(lastKnownVolume));
                        temp.command(device,
                                "{\"type\":\"VolumeLevelCommand\",\"volumeLevel\":" + lastKnownVolume + "}");
                    }
                } else if (command == IncreaseDecreaseType.DECREASE) {
                    if (lastKnownVolume > 0) {
                        lastKnownVolume--;
                        updateState(CHANNEL_VOLUME, new PercentType(lastKnownVolume));
                        temp.command(device,
                                "{\"type\":\"VolumeLevelCommand\",\"volumeLevel\":" + lastKnownVolume + "}");
                    }
                }
            }
            // shuffle command
            if (channelId.equals(CHANNEL_SHUFFLE)) {
                if (command instanceof OnOffType) {
                    OnOffType value = (OnOffType) command;

                    temp.command(device, "{\"type\":\"ShuffleCommand\",\"shuffle\":\""
                            + (value == OnOffType.ON ? "true" : "false") + "\"}");
                }
            }

            // bluetooth commands
            if (channelId.equals(CHANNEL_BLUETOOTH_ID)) {
                needBluetoothRefresh = true;
                if (command instanceof StringType) {
                    String address = ((StringType) command).toFullString();
                    if (!address.isEmpty()) {
                        waitForUpdate = 4000;
                    }
                    temp.bluetooth(device, address);
                }
            }
            if (channelId.equals(CHANNEL_BLUETOOTH)) {
                needBluetoothRefresh = true;
                if (command == OnOffType.ON) {
                    waitForUpdate = 4000;
                    String bluetoothId = lastKnownBluetoothId;
                    BluetoothState state = bluetoothState;
                    if (state != null && (bluetoothId == null || bluetoothId.isEmpty())) {
                        if (state.pairedDeviceList != null) {
                            for (PairedDevice paired : state.pairedDeviceList) {
                                if (paired.address != null && !paired.address.isEmpty()) {
                                    lastKnownBluetoothId = paired.address;
                                    break;
                                }
                            }
                        }
                    }
                    if (lastKnownBluetoothId != null && !lastKnownBluetoothId.isEmpty()) {
                        temp.bluetooth(device, lastKnownBluetoothId);
                    }
                } else if (command == OnOffType.OFF) {
                    temp.bluetooth(device, null);
                }
            }
            if (channelId.equals(CHANNEL_BLUETOOTH_DEVICE_NAME)) {
                needBluetoothRefresh = true;
            }
            // radio commands
            if (channelId.equals(CHANNEL_RADIO_STATION_ID)) {
                if (command instanceof StringType) {

                    String stationId = ((StringType) command).toFullString();
                    if (stationId != null && !stationId.isEmpty()) {
                        waitForUpdate = 3000;
                    }
                    temp.playRadio(device, stationId);

                }
            }
            if (channelId.equals(CHANNEL_RADIO)) {

                if (command == OnOffType.ON) {
                    if (lastKnownRadioStationId != null && !lastKnownRadioStationId.isEmpty()) {
                        waitForUpdate = 3000;
                    }
                    temp.playRadio(device, lastKnownRadioStationId);
                } else if (command == OnOffType.OFF) {
                    temp.playRadio(device, "");
                }

            }

            // force update of the state
            this.disableUpdate = true;
            final boolean bluetoothRefresh = needBluetoothRefresh;
            Runnable doRefresh = () -> {
                BluetoothState state = null;
                if (bluetoothRefresh) {
                    JsonBluetoothStates states;
                    try {
                        states = temp.getBluetoothConnectionStates();
                        state = states.findStateByDevice(device);
                    } catch (Exception e) {
                        logger.info("getBluetoothConnectionStates fails: {}", e);
                    }

                }
                this.disableUpdate = false;
                updateState(device, state);
            };
            if (command instanceof RefreshType) {
                waitForUpdate = 0;
            }
            if (waitForUpdate == 0) {
                doRefresh.run();
            } else {
                this.updateStateJob = scheduler.schedule(doRefresh, waitForUpdate, TimeUnit.MILLISECONDS);
            }

        } catch (Exception e) {
            logger.info("handleCommand fails: {}", e);
        }
    }

    public void updateState(@Nullable Device device, @Nullable BluetoothState bluetoothState) {
        if (this.disableUpdate) {
            return;
        }
        if (device == null) {
            updateStatus(ThingStatus.UNKNOWN);
            return;
        }
        this.device = device;
        if (!device.online) {
            updateStatus(ThingStatus.OFFLINE);
            return;
        }
        updateStatus(ThingStatus.ONLINE);
        Connection connection = this.connection;
        if (connection == null) {
            return;
        }

        PlayerInfo playerInfo = null;
        Provider provider = null;
        InfoText infoText = null;
        MainArt mainArt = null;
        try {
            JsonPlayerState playerState = connection.getPlayer(device);
            playerInfo = playerState.playerInfo;
            if (playerInfo != null) {
                infoText = playerInfo.miniInfoText;
                if (infoText == null) {
                    infoText = playerInfo.infoText;
                }
                mainArt = playerInfo.mainArt;
                provider = playerInfo.provider;
            }
        } catch (HttpException e) {
            if (e.getCode() == 400) {
                // Ignore
            } else {
                logger.info("getPlayer fails: {}", e);
            }
        } catch (Exception e) {
            logger.info("getPlayer fails: {}", e);
        }
        JsonMediaState mediaState = null;
        try {
            mediaState = connection.getMediaState(device);

        } catch (HttpException e) {
            if (e.getCode() == 400) {

                updateState(CHANNEL_RADIO_STATION_ID, new StringType(""));

            } else {
                logger.info("getMediaState fails: {}", e);
            }
        } catch (Exception e) {
            logger.info("getMediaState fails: {}", e);
        }

        // check playing
        boolean playing = playerInfo != null && playerInfo.state != null && playerInfo.state.equals("PLAYING");

        // handle bluetooth
        String bluetoothId = "";
        String bluetoothDeviceName = "";
        boolean bluetoothIsConnected = false;
        if (bluetoothState != null) {
            this.bluetoothState = bluetoothState;
            if (bluetoothState.pairedDeviceList != null) {
                for (PairedDevice paired : bluetoothState.pairedDeviceList) {
                    if (paired.connected && paired.address != null) {
                        bluetoothIsConnected = true;
                        bluetoothId = paired.address;
                        bluetoothDeviceName = paired.friendlyName;
                        if (bluetoothDeviceName == null || bluetoothDeviceName.isEmpty()) {
                            bluetoothDeviceName = paired.address;
                        }
                        break;
                    }
                }
            }

        }
        if (bluetoothId != null && !bluetoothId.isEmpty()) {
            lastKnownBluetoothId = bluetoothId;
        }
        // handle radio
        boolean isRadio = false;
        if (mediaState != null && mediaState.radioStationId != null && !mediaState.radioStationId.isEmpty()) {
            lastKnownRadioStationId = mediaState.radioStationId;
            if (provider != null && provider.providerName.equalsIgnoreCase("TuneIn Live-Radio")) {
                isRadio = true;
            }
        }
        String radioStationId = "";

        if (isRadio && mediaState != null && mediaState.radioStationId != null) {
            radioStationId = mediaState.radioStationId;
        }
        // handle title, subtitle, imageUrl
        String title = "";
        String subTitle1 = "";
        String subTitle2 = "";
        String imageUrl = "";
        if (infoText != null) {
            if (infoText.title != null) {
                title = infoText.title;
            }
            if (infoText.subText1 != null) {
                subTitle1 = infoText.subText1;
            }

            if (infoText.subText2 != null) {
                subTitle2 = infoText.subText2;
            }
        }
        if (mainArt != null) {
            if (mainArt.url != null) {
                imageUrl = mainArt.url;
            }
        }
        if (mediaState != null) {
            QueueEntry[] queueEntries = mediaState.queue;
            if (queueEntries != null && queueEntries.length > 0) {
                QueueEntry entry = queueEntries[0];
                if (entry != null) {

                    if (isRadio) {
                        if (imageUrl.isEmpty() && entry.imageURL != null) {
                            imageUrl = entry.imageURL;
                        }
                        if (subTitle1.isEmpty() && entry.radioStationSlogan != null) {
                            subTitle1 = entry.radioStationSlogan;
                        }
                        if (subTitle2.isEmpty() && entry.radioStationLocation != null) {
                            subTitle2 = entry.radioStationLocation;
                        }
                    }
                }
            }
        }
        // handle provider
        String providerDisplayName = "";
        if (provider != null) {
            if (provider.providerDisplayName != null) {
                providerDisplayName = provider.providerDisplayName;
            }
            if (provider.providerName != null) {
                if (providerDisplayName.isEmpty()) {
                    providerDisplayName = provider.providerName;
                }
            }
        }
        // handle volume
        Integer volume = null;
        if (mediaState != null) {
            volume = mediaState.volume;
        } else if (playerInfo != null) {

            Volume volumnInfo = playerInfo.volume;
            if (volumnInfo != null) {
                volume = volumnInfo.volume;
            }
        }

        if (volume != null && volume > 0) {
            lastKnownVolume = volume;
        }

        // Update states
        updateState(CHANNEL_RADIO_STATION_ID, new StringType(radioStationId));
        updateState(CHANNEL_RADIO, playing && isRadio ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_VOLUME, volume != null ? new PercentType(volume) : UnDefType.UNDEF);
        updateState(CHANNEL_PROVIDER_DISPLAY_NAME, new StringType(providerDisplayName));
        updateState(CHANNEL_PLAYER, playing ? PlayPauseType.PLAY : PlayPauseType.PAUSE);
        updateState(CHANNEL_IMAGE_URL, new StringType(imageUrl));
        updateState(CHANNEL_TITLE, new StringType(title));
        updateState(CHANNEL_SUBTITLE1, new StringType(subTitle1));
        updateState(CHANNEL_SUBTITLE2, new StringType(subTitle2));

        if (bluetoothState != null) {
            updateState(CHANNEL_BLUETOOTH, bluetoothIsConnected ? OnOffType.ON : OnOffType.OFF);
            updateState(CHANNEL_BLUETOOTH_ID, new StringType(bluetoothId));
            updateState(CHANNEL_BLUETOOTH_DEVICE_NAME, new StringType(bluetoothDeviceName));
        }

    }

}
