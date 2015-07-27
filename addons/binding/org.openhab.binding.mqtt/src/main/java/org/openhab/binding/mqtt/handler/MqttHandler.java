/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mqtt.handler;

import static org.openhab.binding.mqtt.MqttBindingConstants.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.items.GenericItem;
//import org.eclipse.smarthome.core.library.items.ContactItem;
import org.eclipse.smarthome.core.library.items.ColorItem;
import org.eclipse.smarthome.core.library.items.ContactItem;
import org.eclipse.smarthome.core.library.items.DateTimeItem;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.items.StringItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.mqtt.internal.MqttMessagePublisher;
import org.openhab.binding.mqtt.internal.MqttMessageSubscriber;
import org.openhab.binding.mqtt.internal.MqttMessageSubscriberListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link MqttHandler} is responsible for handling MQTT Topics as Things. MQTT messages are propagated then into
 * relevant channels and vice versa.
 *
 * @author Marcus of Wetware Labs - Initial contribution
 */
public class MqttHandler extends BaseThingHandler implements MqttBridgeListener, MqttMessageSubscriberListener {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(THING_TYPE_TOPIC);

    private Logger logger = LoggerFactory.getLogger(MqttHandler.class);

    // lists for propagating received states and commands into possible channels (eventually reaching an Ttem)
    HashMap<String, GenericItem> itemList = new HashMap<String, GenericItem>();
    List<Class<? extends State>> stateList = new ArrayList<Class<? extends State>>();
    List<Class<? extends Command>> commandList = new ArrayList<Class<? extends Command>>();

    private MqttBridgeHandler bridgeHandler;

    /** Message producer for sending messages to MQTT **/
    private MqttMessagePublisher publisher;

    /** Message consumer for receiving state messages from MQTT **/
    private MqttMessageSubscriber subscriber;

    public MqttHandler(Thing thing) {
        super(thing);
    }

    // Received command from MQTT Subscriber. Try to cast it to every possible Command Type and send it to all channels
    // that support this type
    @Override
    public void mqttCommandReceived(String topic, String command) {

        for (String channel : itemList.keySet()) {
            // go through all channels and check if the Item associated with it has DataTypes that we can cast the
            // command into
            for (Class<? extends Type> asc : itemList.get(channel).getAcceptedDataTypes()) {

                try {
                    Method valueOf = asc.getMethod("valueOf", String.class);
                    Command c = (Command) valueOf.invoke(asc, command);
                    if (c != null) {
                        // command could be casted to type 'type'
                        logger.debug(
                                "MQTT: Received state (topic '{}'). Propagating payload '{}' as type '{}' to channel '{}')",
                                topic, command, c.getClass().getName(), channel);
                        postCommand(channel, c);
                        break;
                    }
                } catch (NoSuchMethodException e) {
                } catch (IllegalArgumentException e) {
                } catch (IllegalAccessException e) {
                } catch (InvocationTargetException e) {
                }
            }
        }
    }

    // Received state from MQTT Subscriber. Try to cast it to every possible State Type and send it to all channels that
    // support this type
    @Override
    public void mqttStateReceived(String topic, String state) {

        for (String channel : itemList.keySet()) {
            // go through all channels and check if the Item associated with it has DataTypes that we can cast the state
            // into
            for (Class<? extends Type> asc : itemList.get(channel).getAcceptedDataTypes()) {

                try {
                    Method valueOf = asc.getMethod("valueOf", String.class);
                    State s = (State) valueOf.invoke(asc, state);
                    if (s != null) {
                        // state could be casted to type 'type'
                        logger.debug(
                                "MQTT: Received state (topic '{}'). Propagating payload '{}' as type '{}' to channel '{}')",
                                topic, state, s.getClass().getName(), channel);
                        updateState(channel, s);
                        break;
                    }
                } catch (NoSuchMethodException e) {
                } catch (IllegalArgumentException e) {
                } catch (IllegalAccessException e) {
                } catch (InvocationTargetException e) {
                }
            }
        }
    }

    /**
     * Initialize subscriber which broadcasts all received state/command events into the associated channels
     *
     * @param topic to subscribe to.
     */
    private void setupSubscriber(String topic, String type, String transform) {

        if (StringUtils.isBlank(topic)) {
            logger.trace("No topic defined for Subscriber");
            return;
        }

        try {
            logger.debug("Setting up Subscriber for topic {}", topic);
            if (transform == null)
                transform = "default";
            subscriber = new MqttMessageSubscriber(
                    getBridgeHandler().getBroker() + ":" + topic + ":" + type + ":" + transform, this);

            getBridgeHandler().registerMessageConsumer(subscriber);

        } catch (Exception e) {
            logger.error("Could not create subscriber: {}", e.getMessage());
        }

    }

    /**
     * Initialize publisher which broadcasts all received state/command events from channel into MQTT broker
     *
     * @param topic to subscribe to.
     */
    private void setupPublisher(String topic, String type, String transform) {

        if (StringUtils.isBlank(topic)) {
            logger.trace("No topic defined for Publisher");
            return;
        }

        try {
            logger.debug("Setting up Publisher for topic {}", topic);
            if (transform == null)
                transform = "default";
            publisher = new MqttMessagePublisher(
                    getBridgeHandler().getBroker() + ":" + topic + ":" + type + ":*:" + transform);

            getBridgeHandler().registerMessageProducer(publisher);

        } catch (Exception e) {
            logger.error("Could not create Publisher: {}", e.getMessage());
        }

    }

    @Override
    public void initialize() {
        logger.debug("Initializing MQTT topic handler.");
        final String topicId = (String) getConfig().get(TOPIC_ID);
        final String type = (String) getConfig().get(TYPE);
        if (topicId != null) {
            if (type != null) {
                if (getBridgeHandler() != null) {
                    if (getConfig().get(DIRECTION) != null) {
                        if (getConfig().get(DIRECTION).equals("in"))
                            setupSubscriber(topicId, type, (String) getConfig().get(TRANSFORM));
                        else if (getConfig().get(DIRECTION).equals("out"))
                            setupPublisher(topicId, type, (String) getConfig().get(TRANSFORM));
                        else
                            throw new IllegalArgumentException("MQTT direction invalid!");
                    } else
                        throw new IllegalArgumentException("MQTT direction must be defined!");
                }
            } else
                throw new IllegalArgumentException("MQTT type must be defined!");
        } else
            throw new IllegalArgumentException("MQTT topic must be defined!");

        stateList.add(OnOffType.class);
        stateList.add(OpenClosedType.class);
        stateList.add(UpDownType.class);
        stateList.add(HSBType.class);
        stateList.add(PercentType.class);
        stateList.add(DecimalType.class);
        stateList.add(DateTimeType.class);
        stateList.add(StringType.class);

        commandList.add(OnOffType.class);
        commandList.add(OpenClosedType.class);
        commandList.add(UpDownType.class);
        commandList.add(IncreaseDecreaseType.class);
        commandList.add(StopMoveType.class);
        commandList.add(HSBType.class);
        commandList.add(PercentType.class);
        commandList.add(DecimalType.class);
        commandList.add(StringType.class);

        itemList.put(CHANNEL_CONTACT, new ContactItem(""));
        itemList.put(CHANNEL_DATETIME, new DateTimeItem(""));
        itemList.put(CHANNEL_DIMMER, new DimmerItem(""));
        itemList.put(CHANNEL_NUMBER, new NumberItem(""));
        itemList.put(CHANNEL_ROLLERSHUTTER, new RollershutterItem(""));
        itemList.put(CHANNEL_STRING, new StringItem(""));
        itemList.put(CHANNEL_SWITCH, new SwitchItem(""));
        itemList.put(CHANNEL_COLOR, new ColorItem(""));

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing MQTT topic handler.");
        if (publisher != null)
            getBridgeHandler().unRegisterMessageProducer(publisher);
        if (subscriber != null)
            getBridgeHandler().unRegisterMessageConsumer(subscriber);
        updateStatus(ThingStatus.REMOVED);
    }

    /**
     * Handles a command for a given channel.
     *
     * @param channelUID unique identifier of the channel on which the update was performed
     * @param command new command
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (publisher != null) {
            String cmdstr = command.toString();
            logger.debug("MQTT: send command '{}' as topic '{}'", cmdstr, publisher.getTopic());
            publisher.publish(publisher.getTopic(), cmdstr.getBytes());
        } else
            logger.warn("MQTT: handleCommand invoked on topic '{}' but declared 'input'! Ignoring..");
        // if (channelUID.getId().equals(CHANNEL_NUMBER)) {
        // // TODO: handle command
        // } else if (channelUID.getId().equals(CHANNEL_SWITCH)) {
        // // TODO: handle command
        // }
    }

    /**
     * Handles a update for a given channel.
     *
     * @param channelUID unique identifier of the channel on which the update was performed
     * @param newState new state
     */
    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {
        if (publisher != null) {
            String statestr = newState.toString();
            logger.debug("MQTT: send state '{}' as topic '{}'", statestr, publisher.getTopic());
            publisher.publish(publisher.getTopic(), statestr.getBytes());
        } else
            logger.warn("MQTT: handleUpdate invoked on topic '{}' but declared 'input'! Ignoring..");

        // if (channelUID.getId().equals(CHANNEL_NUMBER)) {
        // // TODO: handle command
        // } else if (channelUID.getId().equals(CHANNEL_SWITCH)) {
        // // TODO: handle command
        // }

    }

    @Override
    public void thingUpdated(Thing thing) {
        super.thingUpdated(thing);
    }

    private synchronized MqttBridgeHandler getBridgeHandler() {
        if (this.bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof MqttBridgeHandler) {
                this.bridgeHandler = (MqttBridgeHandler) handler;
                this.bridgeHandler.registerMqttBridgeListener(this);
            } else {
                return null;
            }
        }
        return this.bridgeHandler;
    }
}
