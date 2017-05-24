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
import org.openhab.binding.loxone.core.LxControl;
import org.openhab.binding.loxone.core.LxControlInfoOnlyAnalog;
import org.openhab.binding.loxone.core.LxControlInfoOnlyDigital;
import org.openhab.binding.loxone.core.LxControlJalousie;
import org.openhab.binding.loxone.core.LxControlLightController;
import org.openhab.binding.loxone.core.LxControlPushbutton;
import org.openhab.binding.loxone.core.LxControlSwitch;
import org.openhab.binding.loxone.core.LxServer;
import org.openhab.binding.loxone.core.LxServerListener;
import org.openhab.binding.loxone.core.LxUuid;
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
    private Logger logger = LoggerFactory.getLogger(LoxoneMiniserverHandler.class);

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

        // do not check for compatibility between command and control type here, each control has to do it itself
        try {
            if (command instanceof RefreshType) {
                updateChannelState(channelUID, control);
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
            updateChannelState(channelUID, control);
        }
    }

    @Override
    public void initialize() {
        LoxoneMiniserverConfig cfg = getConfig().as(LoxoneMiniserverConfig.class);
        try {
            InetAddress ip = InetAddress.getByName(cfg.host);

            // check if server does not need to be created from scratch
            if (server != null && !server.isChanged(ip, cfg.port, cfg.user, cfg.password)) {
                server.update(cfg.firstConDelay, cfg.keepAlivePeriod, cfg.connectErrDelay, cfg.userErrorDelay,
                        cfg.comErrorDelay, cfg.maxBinMsgSize, cfg.maxTextMsgSize);
            } else {
                if (server != null) {
                    server.stop();
                }
                server = new LxServer(ip, cfg.port, cfg.user, cfg.password);
                server.addListener(this);
                server.update(cfg.firstConDelay, cfg.keepAlivePeriod, cfg.connectErrDelay, cfg.userErrorDelay,
                        cfg.comErrorDelay, cfg.maxBinMsgSize, cfg.maxTextMsgSize);
                server.start();
            }
        } catch (UnknownHostException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Unknown host");
        }
    }

    @Override
    public void onNewConfig(LxServer server) {
        Thing thing = getThing();
        thing.setProperty(MINISERVER_PROPERTY_MINISERVER_NAME, server.getMiniserverName());
        thing.setProperty(MINISERVER_PROPERTY_SERIAL, server.getSerial());
        thing.setProperty(MINISERVER_PROPERTY_PROJECT_NAME, server.getProjectName());
        thing.setProperty(MINISERVER_PROPERTY_CLOUD_ADDRESS, server.getCloudAddress());
        // set location only the first time after discovery
        if (thing.getLocation() == null) {
            thing.setLocation(server.getLocation());
        }

        factory.removeChannelTypesForThing(thing.getUID());

        ArrayList<Channel> channels = new ArrayList<Channel>();
        ThingBuilder builder = editThing();

        for (Channel channel : getThing().getChannels()) {
            if (server.findControl(new LxUuid(channel.getUID().getIdWithoutGroup())) == null) {
                builder.withoutChannel(channel.getUID());
            }
        }

        for (LxControl control : server.getControls().values()) {
            Channel channel = createChannelForControl(control);
            if (channel != null) {
                channels.add(channel);
                builder.withoutChannel(channel.getUID());
            }
        }

        builder.withChannels(channels);
        updateThing(builder.build());
    }

    @Override
    public void onControlStateUpdate(LxControl control) {
        ChannelUID channelId = new ChannelUID(getThing().getUID(), control.getUuid().toString());
        updateChannelState(channelId, control);
    }

    @Override
    public void onServerGoesOnline() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void onServerGoesOffline(LxServer.OfflineReason reason, String details) {
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
        if (server != null) {
            server.stop();
            server = null;
        }
        factory.removeChannelTypesForThing(getThing().getUID());
    }

    /**
     * Creates a new {@link Channel} for a single Loxone control object. Registers channel type within the factory,
     * which is the channel type provider.
     *
     * @param control
     *            control object to create a channel for
     * @return
     *         created {@link Channel} object
     */
    private Channel createChannelForControl(LxControl control) {
        Channel channel = null;
        ChannelTypeUID chTypeId = null;
        ChannelType channelType = null;
        StateDescription stateDescr = null;
        String uuid = control.getUuid().toString();
        ChannelUID channelId = new ChannelUID(getThing().getUID(), control.getUuid().toString());

        String name = control.getName();
        LxCategory cat = control.getCategory();

        String category = null;
        if (cat != null) {
            category = cat.getName();
        }

        chTypeId = new ChannelTypeUID(getThing().getUID() + ":" + control.getTypeName() + ":" + uuid);

        if (control instanceof LxControlSwitch) {
            if (control instanceof LxControlPushbutton) {
                channelType = new ChannelType(chTypeId, false, "Switch", name,
                        "Loxone pushbuton control for " + control.getName(), category, null, null, null);
                channel = ChannelBuilder.create(channelId, "Switch").withType(chTypeId).withLabel(name)
                        .withDescription("Pushbutton for " + name).build();
            } else {
                Set<String> tags = Collections.singleton("");
                if (cat != null && cat.getType() == LxCategory.CategoryType.LIGHTS) {
                    tags = Collections.singleton("Lighting");
                }

                channelType = new ChannelType(chTypeId, false, "Switch", control.getName(), "Loxone switch for " + name,
                        category, tags, null, null);
                channel = ChannelBuilder.create(channelId, "Switch").withType(chTypeId).withLabel(name)
                        .withDescription("Switch for " + name).withDefaultTags(tags).build();
            }
        } else if (control instanceof LxControlJalousie) {
            channelType = new ChannelType(chTypeId, false, "Rollershutter", name,
                    "Loxone jalousie control for " + control.getName(), category, null, null, null);
            channel = ChannelBuilder.create(channelId, "Rollershutter").withType(chTypeId).withLabel(name)
                    .withDescription("Rollershutter for " + name).build();

        } else if (control instanceof LxControlInfoOnlyDigital) {
            channelType = new ChannelType(chTypeId, false, "String", name, "Digital virtual state of  " + name,
                    category, null, null, null);
            channel = ChannelBuilder.create(channelId, "String").withType(chTypeId).withLabel(name)
                    .withDescription("Digital virtual state of " + name).build();

        } else if (control instanceof LxControlInfoOnlyAnalog) {
            channelType = new ChannelType(chTypeId, false, "String", name, "Analog virtual state of  " + name, category,
                    null, null, null);
            channel = ChannelBuilder.create(channelId, "String").withType(chTypeId).withLabel(name)
                    .withDescription("Analog virtual state of " + name).build();

        } else if (control instanceof LxControlLightController) {
            List<StateOption> options = new ArrayList<StateOption>();
            for (Map.Entry<String, String> entry : ((LxControlLightController) control).getSceneNames().entrySet()) {
                options.add(new StateOption(entry.getKey(), entry.getValue()));
            }
            stateDescr = new StateDescription(BigDecimal.ZERO,
                    new BigDecimal(LxControlLightController.NUM_OF_SCENES - 1), BigDecimal.ONE, null, false, options);
            channelType = new ChannelType(chTypeId, false, "Number", name, "Light controller for " + name, category,
                    null, stateDescr, null);
            channel = ChannelBuilder.create(channelId, "Number").withType(chTypeId).withLabel(name)
                    .withDescription("Lights controller for " + name).build();
        }

        if (channel != null && channelType != null) {
            factory.removeChannelType(chTypeId);
            factory.addChannelType(channelType);
        }
        return channel;
    }

    private void updateChannelState(ChannelUID channelUID, LxControl control) {
        if (control instanceof LxControlSwitch) {
            double value = ((LxControlSwitch) control).getState();
            if (value == 1.0) {
                updateState(channelUID, OnOffType.ON);
            } else if (value == 0) {
                updateState(channelUID, OnOffType.OFF);
            }
        } else if (control instanceof LxControlJalousie) {
            double value = ((LxControlJalousie) control).getPosition();
            if (value >= 0 && value <= 1) {
                // state UP or DOWN from Loxone indicates blinds are moving up or down
                // state UP in OpenHAB means blinds are fully up (0%) and DOWN means fully down (100%)
                // so we will update only position and not up or down states
                updateState(channelUID, new PercentType((int) (value * 100)));
            }
        } else if (control instanceof LxControlInfoOnlyDigital) {
            String value = ((LxControlInfoOnlyDigital) control).getFormattedValue();
            if (value != null) {
                updateState(channelUID, new StringType(value));
            }
        } else if (control instanceof LxControlInfoOnlyAnalog) {
            String value = ((LxControlInfoOnlyAnalog) control).getFormattedValue();
            if (value != null) {
                updateState(channelUID, new StringType(value));
            }
        } else if (control instanceof LxControlLightController) {
            LxControlLightController controller = (LxControlLightController) control;
            int value = controller.getCurrentScene();
            if (value >= 0 && value < LxControlLightController.NUM_OF_SCENES) {
                updateState(channelUID, new DecimalType(value));
            }
            if (controller.sceneNamesUpdated()) {
                Channel channel = createChannelForControl(control);
                ThingBuilder builder = editThing();
                builder.withoutChannel(channel.getUID());
                builder.withChannel(channel);
                updateThing(builder.build());
            }
        }
    }

    private LxControl getControlFromChannelUID(ChannelUID channelUID) {
        String channelId = channelUID.getIdWithoutGroup();
        return server.findControl(new LxUuid(channelId));
    }
}
