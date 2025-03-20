/**
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ferroamp.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttMessageSubscriber;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FerroampHandler} is responsible for handling the values sent to and from the binding.
 *
 * @author Örjan Backsell - Initial contribution
 *
 */

@NonNullByDefault
public class FerroampHandler extends BaseThingHandler implements MqttMessageSubscriber {
    private final static Logger logger = LoggerFactory.getLogger(FerroampHandler.class);
    private @Nullable static MqttBrokerConnection ferroampConnection;
    FerroampMqttCommunication ferroampMqttCommunication = new FerroampMqttCommunication(thing);
    FerroampConfiguration ferroampConfig = new FerroampConfiguration();

    private static List<FerroampChannelConfiguration> channelConfigEhub = new ArrayList<>();
    private static List<FerroampChannelConfiguration> channelConfigSsoS1 = new ArrayList<>();
    private static List<FerroampChannelConfiguration> channelConfigSsoS2 = new ArrayList<>();
    private static List<FerroampChannelConfiguration> channelConfigSsoS3 = new ArrayList<>();
    private static List<FerroampChannelConfiguration> channelConfigSsoS4 = new ArrayList<>();
    private static List<FerroampChannelConfiguration> channelConfigEso = new ArrayList<>();
    private static List<FerroampChannelConfiguration> channelConfigEsm = new ArrayList<>();

    public FerroampHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String transId = UUID.randomUUID().toString();
        String valueConfiguration = command.toString();
        if (FerroampBindingConstants.CHANNEL_REQUESTCHARGE.equals(channelUID.getId())) {
            String requestCmdJsonCharge = "{\"" + "transId" + "\":\"" + transId
                    + "\",\"cmd\":{\"name\":\"charge\",\"arg\":\"" + valueConfiguration + "\"}}";
            FerroampMqttCommunication.sendPublishedTopic(requestCmdJsonCharge, ferroampConfig);
        }
        if (FerroampBindingConstants.CHANNEL_REQUESTDISCHARGE.equals(channelUID.getId())) {
            String requestCmdJsonDisCharge = "{\"" + "transId" + "\":\"" + transId
                    + "\",\"cmd\":{\"name\":\"discharge\",\"arg\":\"" + valueConfiguration + "\"}}";
            FerroampMqttCommunication.sendPublishedTopic(requestCmdJsonDisCharge, ferroampConfig);
        }
        if (FerroampBindingConstants.CHANNEL_AUTO.equals(channelUID.getId())) {
            String requestCmdJsonAuto = "{\"" + "transId" + "\":\"" + transId + "\",\"cmd\":{\"name\":\"auto\"}}";
            FerroampMqttCommunication.sendPublishedTopic(requestCmdJsonAuto, ferroampConfig);
        }
    }

    @Override
    public void initialize() {
        // Set configuration parameters
        ferroampConfig = getConfigAs(FerroampConfiguration.class);

        // Set channel configuration parameters
        channelConfigEhub = FerroampChannelConfiguration.getChannelConfigurationEhub();
        channelConfigSsoS1 = FerroampChannelConfiguration.getChannelConfigurationSsoS1();
        channelConfigSsoS2 = FerroampChannelConfiguration.getChannelConfigurationSsoS2();
        channelConfigSsoS3 = FerroampChannelConfiguration.getChannelConfigurationSsoS3();
        channelConfigSsoS4 = FerroampChannelConfiguration.getChannelConfigurationSsoS4();
        channelConfigEso = FerroampChannelConfiguration.getChannelConfigurationEso();
        channelConfigEsm = FerroampChannelConfiguration.getChannelConfigurationEsm();

        if (!ferroampConfig.hostName.isBlank() || !ferroampConfig.password.isBlank()
                || !ferroampConfig.userName.isBlank()) {
            final MqttBrokerConnection ferroampConnection = new MqttBrokerConnection(ferroampConfig.hostName,
                    FerroampBindingConstants.BROKER_PORT, false, false, ferroampConfig.userName);
            updateStatus(ThingStatus.UNKNOWN);
            scheduler.scheduleWithFixedDelay(this::pollTask, 60, ferroampConfig.refreshInterval, TimeUnit.SECONDS);
            this.setFerroampConnection(ferroampConnection);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }
    }

    private void pollTask() {
        try {
            startMqttConnection(getConfigAs(FerroampConfiguration.class));
        } catch (InterruptedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            logger.debug("Connection interrupted");
            return;
        }

        MqttBrokerConnection ferroampConnection = FerroampHandler.ferroampConnection;
        if (ferroampConnection == null || ferroampConnection.connectionState().toString().equals("DISCONNECTED")) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            logger.debug("No connection to MqttBroker");
        } else if (ferroampConnection.connectionState().toString().equals("CONNECTED")) {
            updateStatus(ThingStatus.ONLINE);
            try {
                channelUpdate();
            } catch (RuntimeException scheduleWithFixedDelayException) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        scheduleWithFixedDelayException.getClass().getName() + ":"
                                + scheduleWithFixedDelayException.getMessage());
            }
        }
    }

    private void startMqttConnection(FerroampConfiguration ferroampConfig) throws InterruptedException {
        MqttBrokerConnection localSubscribeConnection = FerroampHandler.getFerroampConnection();
        Objects.requireNonNull(localSubscribeConnection,
                "MqttBrokerConnection localSubscribeConnection cannot be null");
        localSubscribeConnection.start();
        localSubscribeConnection.setCredentials(ferroampConfig.userName, ferroampConfig.password);
        ferroampMqttCommunication.getSubscribedTopic(FerroampBindingConstants.EHUB_TOPIC, ferroampConfig);
        ferroampMqttCommunication.getSubscribedTopic(FerroampBindingConstants.SSO_TOPIC, ferroampConfig);
        ferroampMqttCommunication.getSubscribedTopic(FerroampBindingConstants.ESO_TOPIC, ferroampConfig);
        ferroampMqttCommunication.getSubscribedTopic(FerroampBindingConstants.ESM_TOPIC, ferroampConfig);
    }

    private void channelUpdate() {
        String[] ehubUpdateChannels;
        ehubUpdateChannels = FerroampMqttCommunication.getEhubChannelUpdateValues();
        if (ehubUpdateChannels.length > 0) {
            int channelValuesCounterEhub = 0;
            for (FerroampChannelConfiguration cConfig : channelConfigEhub) {
                String ehubChannel = cConfig.id;
                State ehubState = StringType.valueOf(ehubUpdateChannels[channelValuesCounterEhub]);
                updateState(ehubChannel, ehubState);
                channelValuesCounterEhub++;
            }
        }

        String[] ssoS1UpdateChannels = new String[9];
        ssoS1UpdateChannels = FerroampMqttCommunication.getSsoS1ChannelUpdateValues();
        if (ssoS1UpdateChannels.length > 0) {
            int channelValuesCounterSsoS1 = 0;
            if (ssoS1UpdateChannels.length <= 9) {
                for (FerroampChannelConfiguration cConfig : channelConfigSsoS1) {
                    String ssoS1Channel = cConfig.id;
                    State ssoS1State = StringType.valueOf(ssoS1UpdateChannels[channelValuesCounterSsoS1]);
                    updateState(ssoS1Channel, ssoS1State);
                    channelValuesCounterSsoS1++;
                }
            }
        }

        String[] ssoS2UpdateChannels = new String[9];
        ssoS2UpdateChannels = FerroampMqttCommunication.getSsoS2ChannelUpdateValues();
        if (ssoS2UpdateChannels.length > 0) {
            int channelValuesCounterSsoS2 = 0;
            if (ssoS2UpdateChannels.length <= 9) {
                for (FerroampChannelConfiguration cConfig : channelConfigSsoS2) {
                    String ssoS2Channel = cConfig.id;
                    State ssoS2State = StringType.valueOf(ssoS2UpdateChannels[channelValuesCounterSsoS2]);
                    updateState(ssoS2Channel, ssoS2State);
                    channelValuesCounterSsoS2++;
                }
            }
        }

        String[] ssoS3UpdateChannels = new String[9];
        ssoS3UpdateChannels = FerroampMqttCommunication.getSsoS3ChannelUpdateValues();
        if (ssoS3UpdateChannels.length > 0) {
            int channelValuesCounterSsoS3 = 0;
            if (ssoS3UpdateChannels.length <= 9) {
                for (FerroampChannelConfiguration cConfig : channelConfigSsoS3) {
                    String ssoS3Channel = cConfig.id;
                    State ssoS3State = StringType.valueOf(ssoS3UpdateChannels[channelValuesCounterSsoS3]);
                    updateState(ssoS3Channel, ssoS3State);
                    channelValuesCounterSsoS3++;
                }
            }
        }

        String[] ssoS4UpdateChannels = new String[9];
        ssoS4UpdateChannels = FerroampMqttCommunication.getSsoS4ChannelUpdateValues();
        if (ssoS4UpdateChannels.length > 0) {
            int channelValuesCounterSsoS4 = 0;
            if (ssoS4UpdateChannels.length <= 9) {
                for (FerroampChannelConfiguration cConfig : channelConfigSsoS4) {
                    String ssoS4Channel = cConfig.id;
                    State ssoS4State = StringType.valueOf(ssoS4UpdateChannels[channelValuesCounterSsoS4]);
                    updateState(ssoS4Channel, ssoS4State);
                    channelValuesCounterSsoS4++;
                }
            }
        }

        String[] esoUpdateChannels = new String[11];
        esoUpdateChannels = FerroampMqttCommunication.getEsoChannelUpdateValues();
        if (esoUpdateChannels.length > 0) {
            int channelValuesCounterEso = 0;
            if (esoUpdateChannels.length <= 9) {
                for (FerroampChannelConfiguration cConfig : channelConfigEso) {
                    String esoChannel = cConfig.id;
                    State esoState = StringType.valueOf(esoUpdateChannels[channelValuesCounterEso]);
                    updateState(esoChannel, esoState);
                    channelValuesCounterEso++;
                }
            }
        }

        String[] esmUpdateChannels = new String[7];
        esmUpdateChannels = FerroampMqttCommunication.getEsmChannelUpdateValues();
        if (esmUpdateChannels.length > 0) {
            int channelValuesCounterEsm = 0;
            if (esmUpdateChannels.length <= 9) {
                for (FerroampChannelConfiguration cConfig : channelConfigEsm) {
                    String esmChannel = cConfig.id;
                    State esmState = StringType.valueOf(esmUpdateChannels[channelValuesCounterEsm]);
                    updateState(esmChannel, esmState);
                    channelValuesCounterEsm++;
                }
            }
        }
    }

    // Capture actual Json-topic message
    @Override
    public void processMessage(String topic, byte[] payload) {
    }

    public @Nullable static MqttBrokerConnection getFerroampConnection() {
        try {
            return ferroampConnection;
        } catch (Exception e) {
            logger.debug("Connection to MqttBroker disturbed during startup of MqttConnection");
        }
        return ferroampConnection;
    }

    public void setFerroampConnection(@Nullable MqttBrokerConnection ferroampConnection) {
        FerroampHandler.ferroampConnection = ferroampConnection;
    }
}
