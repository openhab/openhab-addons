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
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.loxone.config.LoxoneMiniserverConfig;
import org.openhab.binding.loxone.internal.core.LxCategory;
import org.openhab.binding.loxone.internal.core.LxContainer;
import org.openhab.binding.loxone.internal.core.LxControl;
import org.openhab.binding.loxone.internal.core.LxControlDimmer;
import org.openhab.binding.loxone.internal.core.LxControlInfoOnlyAnalog;
import org.openhab.binding.loxone.internal.core.LxControlInfoOnlyDigital;
import org.openhab.binding.loxone.internal.core.LxControlJalousie;
import org.openhab.binding.loxone.internal.core.LxControlLightController;
import org.openhab.binding.loxone.internal.core.LxControlPushbutton;
import org.openhab.binding.loxone.internal.core.LxControlRadio;
import org.openhab.binding.loxone.internal.core.LxControlSwitch;
import org.openhab.binding.loxone.internal.core.LxControlTextState;
import org.openhab.binding.loxone.internal.core.LxControlTimedSwitch;
import org.openhab.binding.loxone.internal.core.LxOfflineReason;
import org.openhab.binding.loxone.internal.core.LxServer;
import org.openhab.binding.loxone.internal.core.LxServerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Representation of a Loxone Miniserver. It is an openHAB {@link Thing}, which is used to communicate with
 * objects (controls) configured in the Miniserver over {@link Channels}.
 *
 * @author Pawel Pieczul - Initial contribution
 */
public class LoxoneMiniserverHandler extends BaseThingHandler implements LxServerListener {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_MINISERVER);

    private LxServer server;

    private ChannelTypeUID switchTypeId = new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_SWITCH);
    private ChannelTypeUID lightCtrlTypeId = new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_LIGHT_CTRL);
    private ChannelTypeUID radioButtonTypeId = new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RADIO_BUTTON);
    private ChannelTypeUID rollershutterTypeId = new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_ROLLERSHUTTER);
    private ChannelTypeUID dimmerTypeId = new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_DIMMER);
    private ChannelTypeUID roTextTypeId = new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_TEXT);
    private ChannelTypeUID roSwitchTypeId = new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_SWITCH);
    private ChannelTypeUID roAnalogTypeId = new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_ANALOG);
    private ChannelTypeUID roTimedSwitchDeactivationDelayTypeId = new ChannelTypeUID(BINDING_ID,
            MINISERVER_CHANNEL_TYPE_RO_NUMBER);

    private Logger logger = LoggerFactory.getLogger(LoxoneMiniserverHandler.class);
    private Map<ChannelUID, LxControl> controls = new HashMap<>();

    /**
     * Create {@link LoxoneMiniserverHandler} object
     *
     * @param thing
     *            Thing object that creates the handler
     */
    public LoxoneMiniserverHandler(Thing thing) {
        super(thing);
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
            // This situation should not happen under normal circumstances, it indicates binding somehow lost its
            // controls
            logger.error("Received command {} for unknown control.", command);
            return;
        }

        logger.debug("Control '{}' received command: {}", control.getName(), command);

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

            if (control instanceof LxControlTimedSwitch) {
                if (command instanceof OnOffType) {
                    if (command == OnOffType.ON) {
                        ((LxControlTimedSwitch) control).pulse();
                    } else {
                        ((LxControlTimedSwitch) control).off();
                    }
                }
                return;
            }

            if (control instanceof LxControlDimmer) {
                LxControlDimmer dimmer = (LxControlDimmer) control;
                if (command instanceof OnOffType) {
                    if (command == OnOffType.ON) {
                        dimmer.on();
                    } else {
                        dimmer.off();
                    }
                } else if (command instanceof PercentType) {
                    PercentType percentCmd = (PercentType) command;
                    dimmer.setPosition(percentCmd.doubleValue());
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

            logger.debug("Incompatible operation on control {}", control.getUuid());

        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
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
        LoxoneMiniserverConfig cfg = getConfig().as(LoxoneMiniserverConfig.class);
        try {
            InetAddress ip = InetAddress.getByName(cfg.host);
            server = new LxServer(ip, cfg.port, cfg.user, cfg.password);
            server.addListener(this);
            server.update(cfg.firstConDelay, cfg.keepAlivePeriod, cfg.connectErrDelay, cfg.responseTimeout,
                    cfg.userErrorDelay, cfg.comErrorDelay, cfg.maxBinMsgSize, cfg.maxTextMsgSize);
            server.start();
        } catch (UnknownHostException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Unknown host");
        }
    }

    @Override
    public void onNewConfig(LxServer server) {
        logger.trace("Processing new configuration");
        Thing thing = getThing();
        thing.setProperty(MINISERVER_PROPERTY_MINISERVER_NAME, server.getMiniserverName());
        thing.setProperty(MINISERVER_PROPERTY_PROJECT_NAME, server.getProjectName());
        thing.setProperty(MINISERVER_PROPERTY_CLOUD_ADDRESS, server.getCloudAddress());
        thing.setProperty(MINISERVER_PROPERTY_PHYSICAL_LOCATION, server.getLocation());

        ArrayList<Channel> channels = new ArrayList<>();
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
                String label = c1.getLabel();
                if (label == null) {
                    return 1;
                }
                return label.compareTo(c2.getLabel());
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
    public void onServerGoesOffline(LxOfflineReason reason, String details) {
        logger.debug("Server goes offline: {}, {}", reason, details);

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
    }

    private void addChannel(List<Channel> channels, String itemType, ChannelTypeUID typeId, ChannelUID channelId,
            String channelLabel, String channelDescription, Set<String> tags) {
        if (channelId != null && itemType != null && typeId != null && channelDescription != null) {
            channels.add(ChannelBuilder.create(channelId, itemType).withType(typeId).withLabel(channelLabel)
                    .withDescription(channelDescription + " : " + channelLabel).withDefaultTags(tags).build());
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

        logger.trace("Creating channels for control: {}, {}", control.getClass().getSimpleName(), control.getUuid());

        String label;
        ChannelUID id = getChannelIdForControl(control, 0);

        List<Channel> channels = new ArrayList<>();

        LxCategory category = control.getCategory();

        LxContainer room = control.getRoom();
        String roomName = null;
        if (room != null) {
            roomName = room.getName();
        }

        String controlName = control.getName();
        if (controlName == null) {
            // Each control on a Miniserver must have a name defined, but in case this is a subject
            // of some malicious data attack, we'll prevent null pointer exception
            controlName = "Undefined name";
        }

        if (roomName != null) {
            label = roomName + " / " + controlName;
        } else {
            label = controlName;
        }

        Set<String> tags = Collections.singleton("");

        if (control instanceof LxControlPushbutton || control instanceof LxControlSwitch
                || control instanceof LxControlTimedSwitch) {
            if (category != null && category.getType() == LxCategory.CategoryType.LIGHTS) {
                tags = Collections.singleton("Lighting");
            }
            addChannel(channels, "Switch", switchTypeId, id, label, "Switch", tags);
            // adding a deactivation delay channel for timed switch
            if (control instanceof LxControlTimedSwitch) {
                ChannelUID deactivationDelayChannelId = getChannelIdForControl(control, 1);
                addChannel(channels, "Number", roTimedSwitchDeactivationDelayTypeId, deactivationDelayChannelId,
                        label + " / Deactivation Delay", "Deactivation Delay", tags);
            }
        } else if (control instanceof LxControlJalousie) {
            addChannel(channels, "Rollershutter", rollershutterTypeId, id, label, "Rollershutter", tags);
        } else if (control instanceof LxControlInfoOnlyDigital) {
            addChannel(channels, "Switch", roSwitchTypeId, id, label, "Digital virtual state", tags);
        } else if (control instanceof LxControlInfoOnlyAnalog) {
            addChannel(channels, "Number", roAnalogTypeId, id, label, "Analog virtual state", tags);
        } else if (control instanceof LxControlLightController) {
            addChannel(channels, "Number", lightCtrlTypeId, id, label, "Light controller", tags);
        } else if (control instanceof LxControlRadio) {
            addChannel(channels, "Number", radioButtonTypeId, id, label, "Radio button", tags);
        } else if (control instanceof LxControlTextState) {
            addChannel(channels, "String", roTextTypeId, id, label, "Text state", tags);
        } else if (control instanceof LxControlDimmer) {
            addChannel(channels, "Dimmer", dimmerTypeId, id, label, "Dimmer", tags);
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
            Double value = ((LxControlSwitch) control).getState();
            if (value != null) {
                if (value == 1.0) {
                    updateState(channelId, OnOffType.ON);
                } else if (value == 0) {
                    updateState(channelId, OnOffType.OFF);
                }
            }
        } else if (control instanceof LxControlTimedSwitch) {
            LxControlTimedSwitch timedSwitch = ((LxControlTimedSwitch) control);
            Double value = timedSwitch.getState();
            if (value != null) {
                if (value == 1d) {
                    updateState(channelId, OnOffType.ON);
                } else if (value == 0d) {
                    updateState(channelId, OnOffType.OFF);
                }
            }

            // getting second channel for this control and update the state
            Double deactivationValue = timedSwitch.getDeactivationDelay();
            if (deactivationValue != null) {
                ChannelUID deactivationDelayChannelId = getChannelIdForControl(control, 1);
                updateState(deactivationDelayChannelId, new DecimalType(deactivationValue));
            }
        } else if (control instanceof LxControlJalousie) {
            Double value = ((LxControlJalousie) control).getPosition();
            if (value != null && value >= 0 && value <= 1) {
                // state UP or DOWN from Loxone indicates blinds are moving up or down
                // state UP in openHAB means blinds are fully up (0%) and DOWN means fully down (100%)
                // so we will update only position and not up or down states
                updateState(channelId, new PercentType((int) (value * 100)));
            }
        } else if (control instanceof LxControlDimmer) {
            Double value = ((LxControlDimmer) control).getPosition();
            if (value != null && value >= 0 && value <= 100) {
                updateState(channelId, new PercentType(value.intValue()));
            }
        } else if (control instanceof LxControlInfoOnlyDigital) {
            Double value = ((LxControlInfoOnlyDigital) control).getValue();
            if (value != null) {
                if (value == 0) {
                    updateState(channelId, OnOffType.OFF);
                } else if (value == 1.0) {
                    updateState(channelId, OnOffType.ON);
                }
            }
        } else if (control instanceof LxControlInfoOnlyAnalog) {
            Double value = ((LxControlInfoOnlyAnalog) control).getValue();
            if (value != null) {
                updateState(channelId, new DecimalType(value));
            }
        } else if (control instanceof LxControlLightController) {
            LxControlLightController controller = (LxControlLightController) control;
            Integer value = controller.getCurrentScene();
            if (value != null && value >= 0 && value < LxControlLightController.NUM_OF_SCENES) {
                updateState(channelId, new DecimalType(value));
            }
            if (controller.sceneNamesUpdated()) {
                createChannelsForControl(control);
            }
        } else if (control instanceof LxControlRadio) {
            LxControlRadio radio = (LxControlRadio) control;
            Integer output = radio.getActiveOutput();
            if (output != null && output >= 0 && output <= LxControlRadio.MAX_RADIO_OUTPUTS) {
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
     *            all indexes greater than 0 will have -index added to the channel ID
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
