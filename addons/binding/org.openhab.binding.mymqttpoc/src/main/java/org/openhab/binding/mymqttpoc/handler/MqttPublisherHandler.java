/**
 * Copy	right (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mymqttpoc.handler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.mymqttpoc.MqttBindingConstants;
import org.openhab.binding.mymqttpoc.mqtt.Publisher;
import org.openhab.binding.mymqttpoc.mqtt.transport.MqttBrokerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link MqttPublisherHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin S. Eskildsen - Initial contribution
 */
public class MqttPublisherHandler extends BaseThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets
            .newHashSet(MqttBindingConstants.THING_TYPE_MQTT_CLIENT);

    private Set<String> defaultTags = new HashSet<String>();

    private Logger logger = LoggerFactory.getLogger(MqttPublisherHandler.class);

    private HashMap<String, ChannelUID> dynamicPublisherChannels = new HashMap<String, ChannelUID>();

    public MqttPublisherHandler(Thing thing) {
        super(thing);
    }
    /*
     * @Override
     * public void handleCommand(ChannelUID channelUID, Command command) {
     * if(channelUID.getId().equals(CHANNEL_1)) {
     * // TODO: handle command
     *
     * // Note: if communication with thing fails for some reason,
     * // indicate that by setting the status with detail information
     * // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
     * // "Could not control device at IP address x.x.x.x");
     * }
     * }
     *
     * @Override
     * public void initialize() {
     * // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
     * // Long running initialization should be done asynchronously in background.
     * updateStatus(ThingStatus.ONLINE);
     *
     * // Note: When initialization can NOT be done set the status with more details for further
     * // analysis. See also class ThingStatusDetail for all available status details.
     * // Add a description to give user information to understand why thing does not work
     * // as expected. E.g.
     * // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
     * // "Can not access device as username and/or password are invalid");
     * }
     */

    @Override
    public synchronized void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handle command '{}' for {}", command, channelUID);

        if (command.equals(RefreshType.REFRESH)) {
            update();
        } else {

            // Just ignore publisher channel
            if (channelUID.getIdWithoutGroup().equals(MqttBindingConstants.CHANNELID_PUBLISH)) {
                logger.trace("'handleCommand': Ignoring publisher channel");
            }
            // Check if it is a dynamic channel
            else if (dynamicPublisherChannels.containsKey(channelUID.getIdWithoutGroup())) {
                // logger.info("'handleCommand': ChannelUID='{}'", channelUID);
                logger.info("MQTT Publisher: Publishing '{}' to '{}'", command, channelUID);
            } else {
                logger.warn("Unkown command '{}'.", command);
            }
        }
    }

    @Override
    public void initialize() {

        logger.debug("Initializing test handler '{}'", getThing().getUID());
        defaultTags.add("#MyMQTT#");

        Publisher pub = new Publisher();
        pub.setSenderChannel(pub);

        MqttBrokerConnection conn = new MqttBrokerConnection("TEST");
        conn.setUrl("tcp://192.168.1.50");

        conn.addProducer(pub);
        try {
            conn.start();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing test handler '{}'", getThing().getUID());
    }

    private void update() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (channelUID.getIdWithoutGroup().equals("publish")) {
            synchronizeDynamicChannels(channelUID);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        if (channelUID.getIdWithoutGroup().equals("publish")) {
            synchronizeDynamicChannels(channelUID);
        }
    }

    /**
     * Counts positional channels and restarts astro jobs.
     */
    private void synchronizeDynamicChannels(ChannelUID channelUID) {
        logger.debug("Calling 'linkedChannelChange' for channelUID='{}'", channelUID);

        // Get actual links for channel
        Set<Item> items = linkRegistry.getLinkedItems(channelUID);

        HashSet<String> prevPublisherChannels = new HashSet<String>(dynamicPublisherChannels.keySet());
        dynamicPublisherChannels.clear();

        // Create missing channels
        for (Item item : items) {
            Set<String> tags = item.getTags();

            Set<ChannelUID> channels = linkRegistry.getBoundChannels(item.getName());

            // Set<Thing> things = linkRegistry.getBoundThings(item.getName());
            // ItemThingLinkRegistry
            // int size = things.size();
            // boolean b12 = things.contains(getThing());
            // boolean b1 = channels.contains(channelUID);
            // ChannelUID c = new ChannelUID(getThing().getUID(), MqttBindingConstants.CHANNELID_PUBLISH);

            // Make sure only
            if (!channels.contains(channelUID)) {
                logger.info("Processing item '{}' (ChannelUID='{}'", item.getName(), channelUID);
                if (!dynamicPublisherChannels.containsKey(item.getName())) {
                    ChannelUID dynamicChannelUID = new ChannelUID(getThing().getUID(), item.getName());
                    dynamicPublisherChannels.put(item.getName(), dynamicChannelUID);
                    createDynamicChannel(dynamicChannelUID, item);
                }
            }

        }

        // Remove channels that is no longer mapped
        prevPublisherChannels.removeAll(dynamicPublisherChannels.keySet());
        for (String itemName : prevPublisherChannels) {

            destroyDynamicChannel(itemName);
        }

        // Debug related section
        String itemNameList = "";
        // Build list for print
        for (Item item : linkRegistry.getLinkedItems(channelUID)) {
            if (itemNameList.isEmpty()) {
                itemNameList = item.getName();
            } else {
                itemNameList += ", " + item.getName();
            }
        }
        logger.debug("ChannelID '{}' has items '{}' mapped", channelUID, itemNameList);
    }

    private void createDynamicChannel(ChannelUID channelUID, Item item) {
        /*
         * // Check if it exists
         * Channel ch = getThing().getChannel(item.getName());
         * Channel ch1 = getThing().getChannel(channelUID.getIdWithoutGroup());
         * Set<ChannelUID> boundChannels = linkRegistry.getBoundChannels(item.getName());
         * String itemName = item.getName();
         *
         * boolean b1 = boundChannels.contains(channelUID);
         * // for ()
         * ChannelUID chUID = ch1.getUID();
         * String uid = ch.getUID().toString();
         * String uid1 = ch.getUID().getIdWithoutGroup().toString();
         * boolean b = linkRegistry.isLinked(item.getName(), ch.getUID());
         *
         * if (ch.getUID().getIdWithoutGroup().equals(itemName)) {
         * return;
         * }
         */

        // Check if it exists
        Channel ch = getThing().getChannel(item.getName());
        ThingBuilder thingBuilder = editThing();

        // Removing if exist
        if (ch != null) {
            thingBuilder.withoutChannel(ch.getUID());
        }

        Channel channel = ChannelBuilder.create(channelUID, item.getType()).withLabel(item.getName())
                .withDefaultTags(defaultTags).withDescription(String.format("Dynamic Channel '{}'", item.getName()))
                .build();

        thingBuilder.withChannel(channel);

        updateThing(thingBuilder.build());
        logger.info("Adding dynamic channel '{}' for Item '{}'", channel.getUID(), item.getName());

        // Add Link
        ItemChannelLink link = new ItemChannelLink(item.getName(), channel.getUID());
        linkRegistry.add(link);

    }

    private void destroyDynamicChannel(String channelName) {
        Channel ch = getThing().getChannel(channelName);
        ThingBuilder thingBuilder = editThing();
        if (ch != null) {
            thingBuilder.withoutChannel(ch.getUID());
        }
        updateThing(thingBuilder.build());

        logger.info("Removing dynamic channel '{}' for Item '{}'", ch.getUID(), channelName);

    }

    // TODO @Override
    // TODO public void connectionLost(Throwable arg0) {
    // TODO Auto-generated method stub

    // TODO }

    // TODO @Override
    // TODO public void deliveryComplete(IMqttDeliveryToken arg0) {
    // TODO Auto-generated method stub

    // TODO }

    // TODO @Override
    // TODO public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
    // TODO Auto-generated method stub

    // TODO }
    /*
     * private void linkedChannelChangeHEST(ChannelUID channelUID, int step) {
     * HashMap<String, Item> itemMap = new HashMap<String, Item>();
     * HashMap<String, Channel> thingChannelMap = new HashMap<String, Channel>();
     *
     * Channel ch = getThing().getChannel(CHANNELID_PUBLISH);
     *
     * if (step == -1) {
     * logger.info("Unlinking ChannelUID='{}'  in '{}'", channelUID, getThing().getUID());
     * } else if (step == 1) {
     * logger.info("Linking ChannelUID='{}'  in '{}'", channelUID, getThing().getUID());
     *
     * }
     *
     * Set<Item> set = linkRegistry.getLinkedItems(ch.getUID());
     * List<Channel> list = getThing().getChannels();
     *
     * logger.info("Thing='{}' has Channels='{}'", getThing().getUID(), list.toString());
     *
     * // Add items mapped to this channel to a map
     * for (Item item : set) {
     * itemMap.put(item.getName(), item);
     * }
     *
     * ChannelUID channelPublisher = new ChannelUID(getThing().getUID(), CHANNELID_PUBLISH);
     *
     * for (Channel chn : list) {
     *
     * if (channelPublisher == chn.getUID()) {
     * logger.info("Mapped Channel: '{}' '{}'", chn.getUID().toString(), chn.getChannelTypeUID().toString());
     * thingChannelMap.put(chn.getUID().toString(), chn);
     * }
     * }
     *
     * String itemNames = "";
     *
     * for (Item item : set) {
     *
     * if (itemNames.isEmpty()) {
     * itemNames = item.getName();
     * } else {
     * itemNames += ", " + item.getName();
     * }
     * // logger.info(" Hest b");
     * }
     * logger.info("Linked channel='{}' to Items='{}'", channelUID, itemNames);
     *
     * // logger.info("Hest 3");
     * }
     */
}
