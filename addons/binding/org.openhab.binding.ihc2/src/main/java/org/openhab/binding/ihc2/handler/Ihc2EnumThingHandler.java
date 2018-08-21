/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc2.handler;

import static org.eclipse.smarthome.core.types.RefreshType.REFRESH;
import static org.openhab.binding.ihc2.Ihc2BindingConstants.CHANNEL_STRING;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.ihc2.internal.config.Ihc2PatternThingConfig;
import org.openhab.binding.ihc2.internal.ws.Ihc2Client;
import org.openhab.binding.ihc2.internal.ws.Ihc2EnumValue;
import org.openhab.binding.ihc2.internal.ws.Ihc2EventListener;
import org.openhab.binding.ihc2.internal.ws.Ihc2Execption;
import org.openhab.binding.ihc2.internal.ws.Ihc2TypeUtils;
import org.openhab.binding.ihc2.internal.ws.datatypes.WSControllerState;
import org.openhab.binding.ihc2.internal.ws.datatypes.WSEnumValue;
import org.openhab.binding.ihc2.internal.ws.datatypes.WSResourceValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Ihc2EnumThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Niels Peter Enemark - Initial contribution
 */
@NonNullByDefault
public class Ihc2EnumThingHandler extends BaseThingHandler implements Ihc2EventListener {

    private final Logger logger = LoggerFactory.getLogger(Ihc2EnumThingHandler.class);

    private final Ihc2Client ihcClient = Ihc2Client.getInstance();

    // Enum
    private int enumDefinitionTypeID = 0;
    private HashMap<Integer, String> enumMap = new HashMap<Integer, String>();

    @Nullable
    private Ihc2PatternThingConfig config = null;

    public Ihc2EnumThingHandler(Thing thing) {
        super(thing);
        logger.debug("Ihc2EnumThingHandler() for: {}", thing.getUID());
        config = thing.getConfiguration().as(Ihc2PatternThingConfig.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand() {}", command.toFullString());

        if (command == REFRESH) {
            try {
                // Get info about the Enum dynamically from the Controller.
                // We could read it from the project file instead.
                // But for now we only get info about the Enums from the project file.
                // Not the resources relation to the enum.

                WSResourceValue rv = ihcClient.resourceQuery(config.getResourceId());

                if (rv instanceof WSEnumValue) {
                    WSEnumValue ev = (WSEnumValue) rv;
                    enumDefinitionTypeID = ev.getDefinitionTypeID(); // Which Enum
                    List<Ihc2EnumValue> enumList = ihcClient.getEnumValues(ev.getDefinitionTypeID()); // Legal Values
                                                                                                      // for this Enum

                    List<StateOption> stateOptionList = new ArrayList<StateOption>();
                    for (Ihc2EnumValue l : enumList) {
                        enumMap.put(l.id, l.name);
                        StateOption s = new StateOption(String.valueOf(l.id), l.name);
                        // StateOption s = new StateOption(l.name, l.name);
                        stateOptionList.add(s);
                    }

                    ihcClient.setStateDecription(channelUID, new StateDescription(null, null, null, config.getPattern(),
                            config.isReadonly(), stateOptionList));
                }
                Command cmd = Ihc2TypeUtils.ihc2oh(CHANNEL_STRING, ihcClient.resourceQuery(config.getResourceId()));
                postCommand(CHANNEL_STRING, cmd);
            } catch (Ihc2Execption e) {
                logger.error("handleCommand() ", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_REGISTERING_ERROR, e.getMessage());
            }
            return;
        }

        try {
            if (command instanceof StringType) {
                String enumStringId = ((StringType) command).toFullString();
                int enumId = Integer.decode(enumStringId);

                String enumLabel = enumMap.get(enumId);

                WSEnumValue ev = new WSEnumValue();
                ev.setEnumName(enumLabel);
                ev.setEnumValueID(enumId);
                ev.setResourceID(config.getResourceId());
                ev.setDefinitionTypeID(enumDefinitionTypeID);

                ihcClient.resourceUpdate(ev);
            }
        } catch (Ihc2Execption e) {
            logger.error("handleCommand() ", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_REGISTERING_ERROR, e.getMessage());
        }
    }

    @Override
    public void initialize() {
        logger.debug("initialize()");
        config = thing.getConfiguration().as(Ihc2PatternThingConfig.class);

        ihcClient.addEventListener(this, config.getResourceId());

        try {
            ihcClient.addResourceId(config.getResourceId());
            updateStatus(ThingStatus.ONLINE);
        } catch (Ihc2Execption e) {
            logger.error("enableRuntimeValueNotifications() FAILED", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void dispose() {
        logger.debug("dispose()");
        ihcClient.removeEventListener(this);
    }

    @Override
    public void statusUpdateReceived(@Nullable EventObject event, @Nullable WSControllerState status) {
        logger.debug("statusUpdateReceived()");
    }

    @Override
    public void resourceValueUpdateReceived(@Nullable EventObject event, @Nullable WSResourceValue value) {
        logger.debug("resourceValueUpdateReceived() {}", value.getResourceID());
        try {
            Command cmd = Ihc2TypeUtils.ihc2oh(CHANNEL_STRING, value);
            postCommand(CHANNEL_STRING, cmd);
        } catch (Ihc2Execption e) {
            logger.error("resourceValueUpdateReceived() FAILED", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, e.getMessage());
        }
    }

    @Override
    public void errorOccured(@Nullable EventObject event, @Nullable Ihc2Execption e) {
        logger.debug("errorOccured()");
    }
}
