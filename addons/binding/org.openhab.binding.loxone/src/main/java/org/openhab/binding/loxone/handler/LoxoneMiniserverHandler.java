/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.loxone.handler;

import static org.openhab.binding.loxone.LoxoneBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.loxone.config.LoxoneMiniserverConfig;
import org.openhab.binding.loxone.core.LxCategory;
import org.openhab.binding.loxone.core.LxContainer;
import org.openhab.binding.loxone.core.LxControl;
import org.openhab.binding.loxone.core.LxControlInfoOnlyAnalog;
import org.openhab.binding.loxone.core.LxControlInfoOnlyDigital;
import org.openhab.binding.loxone.core.LxControlJalousie;
import org.openhab.binding.loxone.core.LxControlLightController;
import org.openhab.binding.loxone.core.LxControlPushbutton;
import org.openhab.binding.loxone.core.LxControlRadio;
import org.openhab.binding.loxone.core.LxControlSwitch;
import org.openhab.binding.loxone.core.LxControlTextState;
import org.openhab.binding.loxone.core.LxServer;
import org.openhab.binding.loxone.core.LxServerListener;
import org.openhab.binding.loxone.internal.LoxoneHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Representation of a Loxone Miniserver. It is an OpenHAB {@link Thing}, which is used to communicate with
 * objects (controls) configured in the Miniserver over {@link Channels}.
 *
 * @author Pawel Pieczul - Initial contribution
 */
public class LoxoneMiniserverHandler extends BaseThingHandler implements LxServerListener {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_MINISERVER);

    private LxServer server = null;
    private LoxoneHandlerFactory factory;
    private ChannelTypeUID switchTypeId, roSwitchTypeId, rollerTypeId, infoTypeId;
    private Logger logger = LoggerFactory.getLogger(LoxoneMiniserverHandler.class);
    private Map<ChannelUID, LxControl> controls = new HashMap<ChannelUID, LxControl>();

    /**
     * Create {@link LoxoneMiniserverHandler} object
     *
     * @param thing
     *            Thing object that creates the handler
     * @param factory
     *            factory that creates the handler
     */
    public LoxoneMiniserverHandler(Thing thing, LoxoneHandlerFactory factory) {
        super(thing);
        this.factory = factory;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (server == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "No server attached to this thing");
            return;
        }

        LxControl control = getControlFromChannelUID(channelUID);
        if (control == null) {
            logger.error("Received command {} from unknown control.", command.toString());
            return;
        }

        logger.debug("Control '{}' received command: {}", control.getName(), command.toString());

        try {
            if (command instanceof RefreshType) {
                updateChannelStates(control);
                return;
            }

            if (control instanceof LxControlSwitch) {
                if (command instanceof OnOffType) {
                    if ((OnOffType) command == OnOffType.ON) {
                        if (control instanceof LxControlPushbutton) {
                            ((LxControlPushbutton) control).pulse();
                        } else {
                            ((LxControlSwitch) control).on();
                        }
                    } else {
                        ((LxControlSwitch) control).off();
                    }
                }
                return;
            }

            if (control instanceof LxControlJalousie) {
                LxControlJalousie jalousie = (LxControlJalousie) control;
                if (command instanceof PercentType) {
                    jalousie.moveToPosition(((PercentType) command).doubleValue() / 100);
                } else if (command instanceof UpDownType) {
                    if ((UpDownType) command == UpDownType.UP) {
                        jalousie.fullUp();
                    } else {
                        jalousie.fullDown();
                    }
                } else if (command instanceof StopMoveType) {
                    if ((StopMoveType) command == StopMoveType.STOP) {
                        jalousie.stop();
                    }
                }
                return;
            }

            if (control instanceof LxControlLightController) {
                LxControlLightController controller = (LxControlLightController) control;
                if (command instanceof OnOffType) {
                    if ((OnOffType) command == OnOffType.ON) {
                        controller.allOn();
                    } else {
                        controller.allOff();
                    }
                } else if (command instanceof UpDownType) {
                    if ((UpDownType) command == UpDownType.UP) {
                        controller.nextScene();
                    } else {
                        controller.previousScene();
                    }
                } else if (command instanceof DecimalType) {
                    controller.setScene(((DecimalType) command).intValue());
                }
                return;
            }

            if (control instanceof LxControlRadio) {
                LxControlRadio radio = (LxControlRadio) control;
                if (command instanceof OnOffType) {
                    if ((OnOffType) command == OnOffType.OFF) {
                        radio.setOutput(0);
                    }
                } else if (command instanceof DecimalType) {
                    radio.setOutput(((DecimalType) command).intValue());
                }
                return;
            }

            logger.debug("Incompatible operation on control {}", control.getUuid().toString());

        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("Channel linked: {}", channelUID.getAsString());
        LxControl control = getControlFromChannelUID(channelUID);
        if (control != null) {
            updateChannelStates(control);
        }
    }

    @Override
    public void initialize() {
        logger.trace("Initializing thing");
        switchTypeId = addNewChannelType("switch", "Switch", "Switch", "Loxone Switch", false);
        rollerTypeId = addNewChannelType("rollershutter", "Rollershutter", "Rollershutter", "Loxone Jalousie", false);
        roSwitchTypeId = addNewChannelType("digital", "Switch", "Switch", "Loxone digital read-only information", true);
        infoTypeId = addNewChannelType("text", "String", "Information (string)", "Loxone read-only information", true);

        LoxoneMiniserverConfig cfg = getConfig().as(LoxoneMiniserverConfig.class);
        try {
            InetAddress ip = InetAddress.getByName(cfg.host);

            // check if server does not need to be created from scratch
            if (server != null && !server.isChanged(ip, cfg.port, cfg.user, cfg.password)) {
                server.update(cfg.firstConDelay, cfg.keepAlivePeriod, cfg.connectErrDelay, cfg.responseTimeout,
                        cfg.userErrorDelay, cfg.comErrorDelay, cfg.maxBinMsgSize, cfg.maxTextMsgSize);
            } else {
                if (server != null) {
                    server.stop();
                }
                server = new LxServer(ip, cfg.port, cfg.user, cfg.password);
                server.addListener(this);
                server.update(cfg.firstConDelay, cfg.keepAlivePeriod, cfg.connectErrDelay, cfg.responseTimeout,
                        cfg.userErrorDelay, cfg.comErrorDelay, cfg.maxBinMsgSize, cfg.maxTextMsgSize);
                server.start();
            }
        } catch (UnknownHostException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Unknown host");
        }
    }

    @Override
    public void onNewConfig(LxServer server) {
        logger.trace("Processing new configuration");
        Thing thing = getThing();
        thing.setProperty(MINISERVER_PROPERTY_MINISERVER_NAME, server.getMiniserverName());
        thing.setProperty(MINISERVER_PROPERTY_SERIAL, server.getSerial());
        thing.setProperty(MINISERVER_PROPERTY_PROJECT_NAME, server.getProjectName());
        thing.setProperty(MINISERVER_PROPERTY_CLOUD_ADDRESS, server.getCloudAddress());
        // set location only the first time after discovery
        if (thing.getLocation() == null) {
            thing.setLocation(server.getLocation());
        }

        ArrayList<Channel> channels = new ArrayList<Channel>();
        ThingBuilder builder = editThing();
        controls.clear();

        logger.trace("Building new channels ({} controls)", server.getControls().size());
        for (LxControl control : server.getControls().values()) {
            List<Channel> newChannels = createChannelsForControl(control);
            if (newChannels != null) {
                channels.addAll(newChannels);
                for (Channel channel : newChannels) {
                    ChannelUID id = channel.getUID();
                    controls.put(id, control);
                }
            }
        }

        logger.trace("Sorting channels");
        channels.sort(new Comparator<Channel>() {
            @Override
            public int compare(Channel c1, Channel c2) {
                return c1.getLabel().compareTo(c2.getLabel());
            }
        });

        logger.trace("Updating thing");
        builder.withChannels(channels);
        updateThing(builder.build());
    }

    @Override
    public void onControlStateUpdate(LxControl control) {
        updateChannelStates(control);
    }

    @Override
    public void onServerGoesOnline() {
        logger.debug("Server goes online.");
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void onServerGoesOffline(LxServer.OfflineReason reason, String details) {
        logger.debug("Server goes offline: {}, {}", reason.toString(), details);

        switch (reason) {
            case AUTHENTICATION_TIMEOUT:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "User authentication timeout");
                break;
            case COMMUNICATION_ERROR:
                String text = "Error communicating with Miniserver";
                if (details != null) {
                    text += " (" + details + ")";
                }
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, text);
                break;
            case INTERNAL_ERROR:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Internal error");
                break;
            case TOO_MANY_FAILED_LOGIN_ATTEMPTS:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Too many failed login attempts - stopped trying");
                break;
            case UNAUTHORIZED:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "User authentication error (invalid user name or password)");
                break;
            case IDLE_TIMEOUT:
                logger.warn("Idle timeout from Loxone Miniserver - adjust keepalive settings");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Timeout due to no activity");
                break;
            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Unknown reason");
                break;
        }

    }

    @Override
    public void dispose() {
        logger.debug("Disposing of server");
        if (server != null) {
            server.stop();
            server = null;
        }
        factory.removeChannelTypesForThing(getThing().getUID());
    }

    private void addChannel(List<Channel> channels, String itemType, ChannelTypeUID typeId, ChannelUID channelId,
            String channelLabel, String channelDescription, Set<String> tags) {
        if (itemType != null && typeId != null && channelDescription != null) {
            Channel channel = ChannelBuilder.create(channelId, itemType).withType(typeId).withLabel(channelLabel)
                    .withDescription(channelDescription + " : " + channelLabel).withDefaultTags(tags).build();
            if (channel != null) {
                channels.add(channel);
            }
        }
    }

    /**
     * Creates a new list of {@link Channel} for a single Loxone control object. Registers channel type within the
     * factory, which is the channel type provider, or uses one of pre-registered type.
     * Most of controls create only one channel, but some of them will create more channels to facilitate different
     * types of states they support.
     *
     * @param control
     *            control object to create a channel for
     * @return
     *         created list of {@link Channel} object
     */
    private List<Channel> createChannelsForControl(LxControl control) {

        logger.trace("Creating channels for control: {}, {}", control.getClass().getSimpleName(),
                control.getUuid().toString());

        String label;
        String controlUuid = control.getUuid().toString();
        ChannelUID id = getChannelIdForControl(control, 0);

        List<Channel> channels = new ArrayList<Channel>();

        LxCategory category = control.getCategory();

        LxContainer room = control.getRoom();
        String roomName = null;
        if (room != null) {
            roomName = room.getName();
        }

        String controlName = control.getName();

        if (roomName != null) {
            label = roomName + " / " + controlName;
        } else {
            label = controlName;
        }

        Set<String> tags = Collections.singleton("");

        if (control instanceof LxControlPushbutton || control instanceof LxControlSwitch) {
            if (category != null && category.getType() == LxCategory.CategoryType.LIGHTS) {
                tags = Collections.singleton("Lighting");
            }
            addChannel(channels, "Switch", switchTypeId, id, label, "Switch", tags);
        } else if (control instanceof LxControlJalousie) {
            addChannel(channels, "Rollershutter", rollerTypeId, id, label, "Rollershutter", tags);
        } else if (control instanceof LxControlInfoOnlyDigital) {
            addChannel(channels, "Switch", roSwitchTypeId, id, label, "Digital virtual state", tags);
        } else if (control instanceof LxControlInfoOnlyAnalog) {
            LxControlInfoOnlyAnalog info = (LxControlInfoOnlyAnalog) control;
            ChannelTypeUID typeId = addNewChannelType(control.getTypeName(), "Number", label, "Analog virtual state",
                    info.getFormatString(), true, null, 0, controlUuid);
            addChannel(channels, "Number", typeId, id, label, "Analog virtual state", tags);
        } else if (control instanceof LxControlLightController) {
            ChannelTypeUID typeId = addNewChannelType(control.getTypeName(), "Number", label, "Light controller", null,
                    false, ((LxControlLightController) control).getSceneNames(),
                    (LxControlLightController.NUM_OF_SCENES - 1), controlUuid);
            addChannel(channels, "Number", typeId, id, label, "Light controller", tags);
        } else if (control instanceof LxControlRadio) {
            ChannelTypeUID typeId = addNewChannelType(control.getTypeName(), "Number", label, "Radio button", null,
                    false, ((LxControlRadio) control).getOutputs(), LxControlRadio.MAX_RADIO_OUTPUTS, controlUuid);
            addChannel(channels, "Number", typeId, id, label, "Radio button", tags);
        } else if (control instanceof LxControlTextState) {
            addChannel(channels, "String", infoTypeId, id, label, "Text state", tags);
        }
        return channels;
    }

    /**
     * Update thing's states for all channels associated with the control
     *
     * @param control
     *            control to update states for
     */
    private void updateChannelStates(LxControl control) {
        ChannelUID channelId = getChannelIdForControl(control, 0);

        if (control instanceof LxControlSwitch) {
            double value = ((LxControlSwitch) control).getState();
            if (value == 1.0) {
                updateState(channelId, OnOffType.ON);
            } else if (value == 0) {
                updateState(channelId, OnOffType.OFF);
            }
        } else if (control instanceof LxControlJalousie) {
            double value = ((LxControlJalousie) control).getPosition();
            if (value >= 0 && value <= 1) {
                // state UP or DOWN from Loxone indicates blinds are moving up or down
                // state UP in OpenHAB means blinds are fully up (0%) and DOWN means fully down (100%)
                // so we will update only position and not up or down states
                updateState(channelId, new PercentType((int) (value * 100)));
            }
        } else if (control instanceof LxControlInfoOnlyDigital) {
            double value = ((LxControlInfoOnlyDigital) control).getValue();
            if (value == 0) {
                updateState(channelId, OnOffType.OFF);
            } else if (value == 1.0) {
                updateState(channelId, OnOffType.ON);
            }
        } else if (control instanceof LxControlInfoOnlyAnalog) {
            updateState(channelId, new DecimalType(((LxControlInfoOnlyAnalog) control).getValue()));
        } else if (control instanceof LxControlLightController) {
            LxControlLightController controller = (LxControlLightController) control;
            int value = controller.getCurrentScene();
            if (value >= 0 && value < LxControlLightController.NUM_OF_SCENES) {
                updateState(channelId, new DecimalType(value));
            }
            if (controller.sceneNamesUpdated()) {
                createChannelsForControl(control);
            }
        } else if (control instanceof LxControlRadio) {
            LxControlRadio radio = (LxControlRadio) control;
            int output = radio.getActiveOutput();
            if (output >= 0 && output <= LxControlRadio.MAX_RADIO_OUTPUTS) {
                updateState(channelId, new DecimalType(output));
            }
        } else if (control instanceof LxControlTextState) {
            LxControlTextState state = (LxControlTextState) control;
            String value = state.getText();
            if (value != null) {
                updateState(channelId, new StringType(value));
            }
        }
    }

    /**
     * Create and register a new channel type
     *
     * @param controlType
     *            type of Loxone control (e.g. switch, jalousie)
     * @param itemType
     *            type of OpenHAB item
     * @param label
     *            label for the channel type
     * @param description
     *            description of the channel type
     * @param format
     *            format string to present the value
     * @param readOnly
     *            true if this control does not accept commands
     * @param options
     *            map of options for drop down lists (can be null)
     * @param lastOption
     *            index of last option
     * @param controlUuid
     *            UUID of Loxone control object (can be null if channel type is generic)
     * @return
     *         channel type ID of newly created type
     */
    private ChannelTypeUID addNewChannelType(String controlType, String itemType, String label, String description,
            String format, boolean readOnly, Map<String, String> options, int lastOption, String controlUuid) {
        logger.trace("Creating a new channel type for {}, {}", controlType, itemType);

        String name = getThing().getUID().getAsString() + ":" + controlType;
        if (controlUuid != null) {
            name += ":" + controlUuid;
        }
        ChannelTypeUID typeId = new ChannelTypeUID(name);
        ChannelType type = new ChannelType(typeId, false, itemType, label, description, null, null,
                buildStateDescription(format, readOnly, options, lastOption), null);
        factory.removeChannelType(typeId);
        factory.addChannelType(type);
        return typeId;
    }

    private ChannelTypeUID addNewChannelType(String controlType, String itemType, String label, String description,
            boolean readOnly) {
        return addNewChannelType(controlType, itemType, label, description, null, readOnly, null, 0, null);
    }

    /**
     * Builds {@link StateDescription} for channel type, that has multiple options to select from
     *
     * @param format
     *            format string to present the value
     * @param readOnly
     *            true if this control does not accept commands
     * @param options
     *            collection of options, where key is option ID (number in reality) and value is option name
     * @param lastOption
     *            maximum value an option ID can have
     * @return
     *         state description to be used for creating channel type
     */
    private StateDescription buildStateDescription(String format, boolean readOnly, Map<String, String> options,
            int lastOption) {
        List<StateOption> optionsList = new ArrayList<StateOption>();
        if (options != null) {
            for (Map.Entry<String, String> entry : options.entrySet()) {
                optionsList.add(new StateOption(entry.getKey(), entry.getValue()));
            }
        }
        return new StateDescription(BigDecimal.ZERO, new BigDecimal(lastOption), BigDecimal.ONE, format, readOnly,
                optionsList);
    }

    /**
     * Based on channel ID, return corresponding {@link LxControl} object
     *
     * @param channelUID
     *            channel ID of the control to find
     * @return
     *         control corresponding to the channel ID or null if not found
     */
    private LxControl getControlFromChannelUID(ChannelUID channelUID) {
        return controls.get(channelUID);
    }

    /**
     * Build channel ID for a control, based on control's UUID, thing's UUID and index of the channel for the control
     *
     * @param control
     *            control to build the channel ID for
     * @param index
     *            index of a channel within control (0 for primary channel)
     *            all indexes greater than 0 will have _index added to the channel ID
     * @return
     *         channel ID for the control and index
     */
    private ChannelUID getChannelIdForControl(LxControl control, int index) {
        String controlId = control.getUuid().toString();
        if (index > 0) {
            controlId += "-" + index;
        }
        return new ChannelUID(getThing().getUID(), controlId);
    }
}
