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
package org.openhab.binding.ferroamp.internal;

import java.util.ArrayList;
import java.util.List;
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
 * The {@link FerroampHandler} is responsible for handling of the values sent to and from the binding.
 *
 * @author Örjan Backsell - Initial contribution
 *
 */
@NonNullByDefault
public class FerroampHandler extends BaseThingHandler implements MqttMessageSubscriber {
    private final static Logger logger = LoggerFactory.getLogger(FerroampHandler.class);
    private @Nullable static MqttBrokerConnection ferroampConnection;
    FerroampMqttCommunication ferroampMqttCommunication = new FerroampMqttCommunication(thing);
    final FerroampConfiguration ferroampConfig = getConfigAs(FerroampConfiguration.class);

    private List<FerroampChannelConfiguration> channelConfigEhub = new ArrayList<>();
    private static List<FerroampChannelConfiguration> channelConfigSsoS1 = new ArrayList<>();
    private static List<FerroampChannelConfiguration> channelConfigSsoS2 = new ArrayList<>();
    private static List<FerroampChannelConfiguration> channelConfigSsoS3 = new ArrayList<>();
    private static List<FerroampChannelConfiguration> channelConfigSsoS4 = new ArrayList<>();
    private static List<FerroampChannelConfiguration> channelConfigEso = new ArrayList<>();
    private static List<FerroampChannelConfiguration> channelConfigEsm = new ArrayList<>();

    long refreshInterval = 30;
    static boolean isEsoAvailable = false;
    static boolean isEsmAvailable = false;

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
        // Set channel configuration parameters
        channelConfigEhub = FerroampChannelConfiguration.getChannelConfigurationEhub();
        channelConfigSsoS1 = FerroampChannelConfiguration.getChannelConfigurationSsoS1();
        channelConfigSsoS2 = FerroampChannelConfiguration.getChannelConfigurationSsoS2();
        channelConfigSsoS3 = FerroampChannelConfiguration.getChannelConfigurationSsoS3();
        channelConfigSsoS4 = FerroampChannelConfiguration.getChannelConfigurationSsoS4();
        channelConfigEso = FerroampChannelConfiguration.getChannelConfigurationEso();
        channelConfigEsm = FerroampChannelConfiguration.getChannelConfigurationEsm();

        final MqttBrokerConnection ferroampConnection = new MqttBrokerConnection(ferroampConfig.hostName,
                FerroampBindingConstants.BROKER_PORT, false, false, ferroampConfig.userName);

        scheduler.scheduleWithFixedDelay(this::pollTask, 60, refreshInterval, TimeUnit.SECONDS);
        this.setFerroampConnection(ferroampConnection);
    }

    private void pollTask() {
        try {
            startMqttConnection();
        } catch (InterruptedException e) {
            logger.debug("Problems with startMqttConnection()");
        }
        if (getFerroampConnection().connectionState().toString().equals("DISCONNECTED")) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            logger.debug("Problem connection to MqttBroker");
            // } else {
        }
        if (getFerroampConnection().connectionState().toString().equals("CONNECTED")) {
            try {
                channelUpdate();
                updateStatus(ThingStatus.ONLINE);

            } catch (RuntimeException scheduleWithFixedDelayException) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        scheduleWithFixedDelayException.getClass().getName() + ":"
                                + scheduleWithFixedDelayException.getMessage());
            }
        }
    }

    private void startMqttConnection() throws InterruptedException {
        MqttBrokerConnection localSubscribeConnection = FerroampHandler.getFerroampConnection();

        localSubscribeConnection.start();
        localSubscribeConnection.setCredentials(ferroampConfig.userName, ferroampConfig.password);

        ferroampMqttCommunication.getSubscribedTopic("ehubTopic", ferroampConfig);
        ferroampMqttCommunication.getSubscribedTopic("ssoTopic", ferroampConfig);
        ferroampMqttCommunication.getSubscribedTopic("esoTopic", ferroampConfig);
        ferroampMqttCommunication.getSubscribedTopic("esmTopic", ferroampConfig);
    }

    private void channelUpdate() {
        String[] ehubUpdateChannels;
        ehubUpdateChannels = FerroampMqttCommunication.getEhubChannelUpdateValues();
        int channelValuesCounterEhub = 0;
        for (FerroampChannelConfiguration cConfig : channelConfigEhub) {
            String ehubChannel = cConfig.id;
            State ehubState = StringType.valueOf(ehubUpdateChannels[channelValuesCounterEhub]);
            updateState(ehubChannel, ehubState);
            channelValuesCounterEhub++;
        }

        String[] ssoS1UpdateChannels = new String[9];
        ssoS1UpdateChannels = FerroampMqttCommunication.getSsoS1ChannelUpdateValues();
        int channelValuesCounterSsoS1 = 0;
        if (ssoS1UpdateChannels.length <= 9) {
            for (FerroampChannelConfiguration cConfig : channelConfigSsoS1) {
                String ssoS1Channel = cConfig.id;
                State ssoS1State = StringType.valueOf(ssoS1UpdateChannels[channelValuesCounterSsoS1]);
                updateState(ssoS1Channel, ssoS1State);
                channelValuesCounterSsoS1++;
            }
        }

        String[] ssoS2UpdateChannels = new String[9];
        ssoS2UpdateChannels = FerroampMqttCommunication.getSsoS2ChannelUpdateValues();
        int channelValuesCounterSsoS2 = 0;
        if (ssoS2UpdateChannels.length <= 9) {
            for (FerroampChannelConfiguration cConfig : channelConfigSsoS2) {
                String ssoS2Channel = cConfig.id;
                State ssoS2State = StringType.valueOf(ssoS2UpdateChannels[channelValuesCounterSsoS2]);
                updateState(ssoS2Channel, ssoS2State);
                channelValuesCounterSsoS2++;
            }
        }

        String[] ssoS3UpdateChannels = new String[9];
        ssoS3UpdateChannels = FerroampMqttCommunication.getSsoS3ChannelUpdateValues();
        int channelValuesCounterSsoS3 = 0;
        if (ssoS3UpdateChannels.length <= 9) {
            for (FerroampChannelConfiguration cConfig : channelConfigSsoS3) {
                String ssoS3Channel = cConfig.id;
                State ssoS3State = StringType.valueOf(ssoS3UpdateChannels[channelValuesCounterSsoS3]);
                updateState(ssoS3Channel, ssoS3State);
                channelValuesCounterSsoS3++;
            }
        }

        String[] ssoS4UpdateChannels = new String[9];
        ssoS4UpdateChannels = FerroampMqttCommunication.getSsoS4ChannelUpdateValues();
        int channelValuesCounterSsoS4 = 0;
        if (ssoS4UpdateChannels.length <= 9) {
            for (FerroampChannelConfiguration cConfig : channelConfigSsoS4) {
                String ssoS4Channel = cConfig.id;
                State ssoS4State = StringType.valueOf(ssoS4UpdateChannels[channelValuesCounterSsoS4]);
                updateState(ssoS4Channel, ssoS4State);
                channelValuesCounterSsoS4++;
            }
        }

        if (ferroampConfig.eso == true) {
            String[] esoUpdateChannels;
            esoUpdateChannels = FerroampMqttCommunication.getEsoChannelUpdateValues();
            int channelValuesCounterEso = 0;
            for (FerroampChannelConfiguration cConfig : channelConfigEso) {
                String esoChannel = cConfig.id;
                State esoState = StringType.valueOf(esoUpdateChannels[channelValuesCounterEso]);
                updateState(esoChannel, esoState);
                channelValuesCounterEso++;
            }
        }

        if (ferroampConfig.esm == true) {
            String[] esmUpdateChannels;
            esmUpdateChannels = FerroampMqttCommunication.getEsmChannelUpdateValues();
            int channelValuesCounterEsm = 0;
            for (FerroampChannelConfiguration cConfig : channelConfigEsm) {
                String esmChannel = cConfig.id;
                State esmState = StringType.valueOf(esmUpdateChannels[channelValuesCounterEsm]);
                updateState(esmChannel, esmState);
                channelValuesCounterEsm++;
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
