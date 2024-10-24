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

    private static @Nullable FerroampConfiguration ferroampConfig;
    private @Nullable static MqttBrokerConnection ferroampConnection;
    FerroampMqttCommunication ferroampMqttCommunication = new FerroampMqttCommunication(thing);

    private List<FerroampChannelConfiguration> channelConfigEhub = new ArrayList<>();
    private static List<FerroampChannelConfiguration> channelConfigSsoS0 = new ArrayList<>();
    private static List<FerroampChannelConfiguration> channelConfigSsoS1 = new ArrayList<>();
    private static List<FerroampChannelConfiguration> channelConfigSsoS2 = new ArrayList<>();
    private static List<FerroampChannelConfiguration> channelConfigSsoS3 = new ArrayList<>();
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
            sendMQTT(requestCmdJsonCharge);
        }
        if (FerroampBindingConstants.CHANNEL_REQUESTDISCHARGE.equals(channelUID.getId())) {
            String requestCmdJsonDisCharge = "{\"" + "transId" + "\":\"" + transId
                    + "\",\"cmd\":{\"name\":\"discharge\",\"arg\":\"" + valueConfiguration + "\"}}";
            sendMQTT(requestCmdJsonDisCharge);
        }
        if (FerroampBindingConstants.CHANNEL_AUTO.equals(channelUID.getId())) {
            String requestCmdJsonAuto = "{\"" + "transId" + "\":\"" + transId + "\",\"cmd\":{\"name\":\"auto\"}}";
            sendMQTT(requestCmdJsonAuto);
        }
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        // Set channel configuration parameters
        channelConfigEhub = FerroampChannelConfiguration.getChannelConfigurationEhub();
        channelConfigSsoS0 = FerroampChannelConfiguration.getChannelConfigurationSsoS0();
        channelConfigSsoS1 = FerroampChannelConfiguration.getChannelConfigurationSsoS1();
        channelConfigSsoS2 = FerroampChannelConfiguration.getChannelConfigurationSsoS2();
        channelConfigSsoS3 = FerroampChannelConfiguration.getChannelConfigurationSsoS3();
        channelConfigEso = FerroampChannelConfiguration.getChannelConfigurationEso();
        channelConfigEsm = FerroampChannelConfiguration.getChannelConfigurationEsm();

        ferroampConfig = getConfigAs(FerroampConfiguration.class);

        @SuppressWarnings("null")
        final MqttBrokerConnection ferroampConnection = new MqttBrokerConnection(ferroampConfig.hostName,
                FerroampBindingConstants.BROKER_PORT, false, false, ferroampConfig.userName);

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            boolean thingReachable = true;

            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
                try {
                    startMqttConnection();
                } catch (InterruptedException e) {
                    logger.debug("Connection to MqttBroker disturbed during configuration");
                }
            } else {
                updateStatus(ThingStatus.OFFLINE);
                thingReachable = false;
            }
        });

        // Start channel-update as configured
        scheduler.scheduleWithFixedDelay(() -> {
            if (getFerroampConnection().connectionState().toString().equals("DISCONNECTED")) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                logger.debug("Problem connection to MqttBroker");
            } else {
                try {
                    channelUpdate();
                    updateStatus(ThingStatus.ONLINE);
                } catch (RuntimeException scheduleWithFixedDelayException) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            scheduleWithFixedDelayException.getClass().getName() + ":"
                                    + scheduleWithFixedDelayException.getMessage());
                }
            }
        }, 60, refreshInterval, TimeUnit.SECONDS);

        this.setFerroampConnection(ferroampConnection);
    }

    private void startMqttConnection() throws InterruptedException {
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            logger.debug("Connection to MqttBroker disturbed during startup of MqttConnection");
        }
        ferroampMqttCommunication.getMQTT("ehubTopic", ferroampConfig);
        ferroampMqttCommunication.getMQTT("ssoTopic", ferroampConfig);
        ferroampMqttCommunication.getMQTT("esoTopic", ferroampConfig);
        ferroampMqttCommunication.getMQTT("esmTopic", ferroampConfig);
    }

    @SuppressWarnings("null")
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

        if (ferroampConfig.ssoS0 == true) {
            String[] ssoS0UpdateChannels = new String[9];
            ssoS0UpdateChannels = FerroampMqttCommunication.getSsoS0ChannelUpdateValues();
            int channelValuesCounterSsoS0 = 0;
            if (ssoS0UpdateChannels.length <= 9) {
                for (FerroampChannelConfiguration cConfig : channelConfigSsoS0) {
                    String ssoS0Channel = cConfig.id;
                    State ssoS0State = StringType.valueOf(ssoS0UpdateChannels[channelValuesCounterSsoS0]);
                    updateState(ssoS0Channel, ssoS0State);
                    channelValuesCounterSsoS0++;
                }
            }
        }

        if (ferroampConfig.ssoS1 == true) {
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
        }

        if (ferroampConfig.ssoS2 == true) {
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
        }

        if (ferroampConfig.ssoS3 == true) {
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

    // Handles request topic
    @SuppressWarnings("null")
    private void sendMQTT(String payload) {
        MqttBrokerConnection localConfigurationConnection = getFerroampConnection();
        localConfigurationConnection.start();
        localConfigurationConnection.setCredentials(ferroampConfig.userName, ferroampConfig.password);

        if (localConfigurationConnection != null) {
            localConfigurationConnection.publish(FerroampBindingConstants.REQUEST_TOPIC, payload.getBytes(), 1, false);
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

    @SuppressWarnings({ "null" })
    public static boolean gethasBattery() {
        try {
            return ferroampConfig.hasBattery;
        } catch (Exception e) {
            logger.debug("Failed at check of configuration-parameter, hasBattery");
        }
        return ferroampConfig.hasBattery;
    }
}
