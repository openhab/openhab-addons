/**
 *
 */
package org.openhab.binding.knx.handler.physical;

import java.util.Collection;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.knx.handler.PhysicalActorThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.IndividualAddress;

/**
 * The {@link GroupAddressThingHandler} is responsible for handling commands, which are
 * sent to one of the channels. It is a stub for Group Addresses that are
 * discovered on the KNX bus, and converts any data that is sent to the
 * Group Address into a StringType
 *
 * @author Karel Goderis - Initial contribution
 */
public class GroupAddressThingHandler extends PhysicalActorThingHandler {

    protected Logger logger = LoggerFactory.getLogger(GroupAddressThingHandler.class);

    // List of all Channel ids
    public final static String CHANNEL_STRING = "string";
    public final static String CHANNEL_NUMBER = "number";
    public final static String CHANNEL_SWITCH = "switch";
    public final static String CHANNEL_DATETIME = "datetime";
    public final static String CHANNEL_CONTACT = "contact";
    public final static String CHANNEL_DIMMER = "dimmer";

    public final static Collection<String> SUPPORTED_CHANNEL_TYPES = Lists.newArrayList(CHANNEL_STRING, CHANNEL_NUMBER,
            CHANNEL_SWITCH, CHANNEL_DATETIME, CHANNEL_CONTACT, CHANNEL_DIMMER);

    // List of all Configuration parameters
    public static final String GROUP_ADDRESS = "groupaddress";
    public static final String AUTO_UPDATE = "autoupdate";
    public static final String DPT = "dpt";

    protected ItemRegistry itemRegistry;

    boolean autoUpdated = false;

    public GroupAddressThingHandler(Thing thing, ItemChannelLinkRegistry itemChannelLinkRegistry,
            ItemRegistry itemRegistry) {
        super(thing, itemChannelLinkRegistry);
        this.itemRegistry = itemRegistry;
    }

    @Override
    public void initialize() {

        try {
            GroupAddress groupaddress = new GroupAddress((String) getConfig().get(GROUP_ADDRESS));

            if (groupaddress != null) {
                groupAddresses.add(groupaddress);
                if ((Boolean) getConfig().get(READ)) {
                    logger.debug("Registering {} in read Addresses", groupaddress);
                    readAddresses.add(groupaddress);
                }
            }
        } catch (Exception e) {
            logger.error("An exception occurred while creating a Group Address : '{}'", e.getMessage());
        }

        super.initialize();
    }

    @Override
    public boolean listensTo(IndividualAddress source) {
        // we listen to telegrams coming from any source
        return true;
    }

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {

        if (autoUpdated) {
            autoUpdated = false;
        } else {
            super.handleUpdate(channelUID, newState);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        super.handleCommand(channelUID, command);

        if (!(command instanceof RefreshType) && getConfig().get(AUTO_UPDATE) != null
                && (boolean) getConfig().get(AUTO_UPDATE)) {
            autoUpdated = true;
            updateState(channelUID, (State) command);
        }
    }

    @Override
    public void processDataReceived(GroupAddress destination, Type state) {

        for (String channelType : SUPPORTED_CHANNEL_TYPES) {
            for (String anItem : itemChannelLinkRegistry
                    .getLinkedItems(new ChannelUID(getThing().getUID(), channelType))) {
                try {
                    Item theItem = itemRegistry.getItem(anItem);
                    if (theItem != null) {
                        boolean isAccepted = false;
                        if (theItem.getAcceptedDataTypes().contains(state.getClass())) {
                            isAccepted = true;
                        } else {
                            // Look for class hierarchy
                            for (Class<? extends State> aState : theItem.getAcceptedDataTypes()) {
                                try {
                                    if (!aState.isEnum()
                                            && aState.newInstance().getClass().isAssignableFrom(state.getClass())) {
                                        isAccepted = true;
                                        break;
                                    }
                                } catch (InstantiationException e) {
                                    logger.warn("InstantiationException on {}", e.getMessage()); // Should never happen
                                } catch (IllegalAccessException e) {
                                    logger.warn("IllegalAccessException on {}", e.getMessage()); // Should never happen
                                }
                            }
                        }

                        if (isAccepted) {
                            updateState(new ChannelUID(getThing().getUID(), channelType), (State) state);
                        } else {
                            logger.warn("The item '{}' does not accept states updates of type '{}'. Check your .items",
                                    anItem, state.getClass().getName());
                            if (theItem != null) {
                                for (Class<?> aClass : theItem.getAcceptedDataTypes()) {
                                    logger.trace("item '{}' accepts {}", anItem, aClass);
                                }
                            }
                        }
                    }
                } catch (ItemNotFoundException e) {
                    logger.error("An exception occurred while searching the registry for item '{}':'{}'", anItem,
                            e.getMessage());
                }
            }
        }

    }

    @Override
    public String getDPT(GroupAddress destination) {
        return (String) getConfig().get(DPT);
    }

    @Override
    public String getDPT(ChannelUID channelUID, Type command) {
        return (String) getConfig().get(DPT);
    }

    @Override
    public String getAddress(ChannelUID channelUID, Type command) {
        return (String) getConfig().get(GROUP_ADDRESS);
    }

    @Override
    public Type getType(ChannelUID channelUID, Type command) {
        return command;
    }

}
