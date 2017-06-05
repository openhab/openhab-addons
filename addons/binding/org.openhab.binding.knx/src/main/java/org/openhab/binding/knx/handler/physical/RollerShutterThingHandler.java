/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.handler.physical;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.knx.handler.KNXBridgeBaseThingHandler;
import org.openhab.binding.knx.handler.PhysicalActorThingHandler;

import tuwien.auto.calimero.GroupAddress;

/**
 * The {@link RollerShutterThingHandler} is responsible for handling commands, which are
 * sent to one of the channels. It implements a KNX roller/shutter/blind actor
 *
 * @author Karel Goderis - Initial contribution
 */
public class RollerShutterThingHandler extends PhysicalActorThingHandler {

    // List of all Channel ids
    public final static String CHANNEL_ROLLERSHUTTER = "rollershutter";

    // List of all Configuration parameters
    public static final String UP_DOWN_GA = "upDownGA";
    public static final String UP_DOWN_STATUS_GA = "upDownStatusGA";
    public static final String STOP_MOVE_GA = "stopMoveGA";
    public static final String STOP_MOVE_STATUS_GA = "stopMoveStatusGA";
    public static final String POSITION_GA = "positionGA";
    public static final String POSITION_STATUS_GA = "positionStatusGA";

    protected PercentType lastPosition = null;

    public RollerShutterThingHandler(Thing thing, ItemChannelLinkRegistry itemChannelLinkRegistry) {
        super(thing, itemChannelLinkRegistry);
    }

    @Override
    public void initialize() {

        try {
            if ((String) getConfig().get(UP_DOWN_GA) != null) {
                GroupAddress address = new GroupAddress((String) getConfig().get(UP_DOWN_GA));
                if (address != null) {
                    groupAddresses.add(address);
                }
            }
        } catch (Exception e) {
            logger.error("An exception occurred while creating a Group Address : '{}'", e.getMessage());
        }

        try {
            if ((String) getConfig().get(UP_DOWN_STATUS_GA) != null) {
                GroupAddress address = new GroupAddress((String) getConfig().get(UP_DOWN_STATUS_GA));
                if (address != null) {
                    groupAddresses.add(address);
                    if ((Boolean) getConfig().get(READ)) {
                        readAddresses.add(address);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("An exception occurred while creating a Group Address : '{}'", e.getMessage());
        }

        try {
            if ((String) getConfig().get(STOP_MOVE_GA) != null) {
                GroupAddress address = new GroupAddress((String) getConfig().get(STOP_MOVE_GA));
                if (address != null) {
                    groupAddresses.add(address);
                }
            }
        } catch (Exception e) {
            logger.error("An exception occurred while creating a Group Address : '{}'", e.getMessage());
        }

        try {
            if ((String) getConfig().get(STOP_MOVE_STATUS_GA) != null) {
                GroupAddress address = new GroupAddress((String) getConfig().get(STOP_MOVE_STATUS_GA));
                if (address != null) {
                    groupAddresses.add(address);
                    if ((Boolean) getConfig().get(READ)) {
                        readAddresses.add(address);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("An exception occurred while creating a Group Address : '{}'", e.getMessage());
        }

        try {
            if ((String) getConfig().get(POSITION_GA) != null) {
                GroupAddress address = new GroupAddress((String) getConfig().get(POSITION_GA));
                if (address != null) {
                    groupAddresses.add(address);
                }
            }
        } catch (Exception e) {
            logger.error("An exception occurred while creating a Group Address : '{}'", e.getMessage());
        }

        try {
            if ((String) getConfig().get(POSITION_STATUS_GA) != null) {
                GroupAddress address = new GroupAddress((String) getConfig().get(POSITION_STATUS_GA));
                if (address != null) {
                    groupAddresses.add(address);
                    if ((Boolean) getConfig().get(READ)) {
                        readAddresses.add(address);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("An exception occurred while creating a Group Address : '{}'", e.getMessage());
        }

        super.initialize();
    }

    @Override
    public void processDataReceived(GroupAddress destination, Type state) {

        if (state instanceof PercentType) {
            lastPosition = (PercentType) state;
        }

        try {
            GroupAddress address = new GroupAddress((String) getConfig().get(POSITION_STATUS_GA));
            if (address.equals(destination)) {
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_ROLLERSHUTTER), (State) state);
            }
        } catch (Exception e) {
            // do nothing, we move on (either config parameter null, or wrong address format)
        }

        try {
            GroupAddress address = new GroupAddress((String) getConfig().get(STOP_MOVE_STATUS_GA));
            if (address.equals(destination)) {
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_ROLLERSHUTTER), (State) state);
            }
        } catch (Exception e) {
            // do nothing, we move on (either config parameter null, or wrong address format)
        }

        try {
            GroupAddress address = new GroupAddress((String) getConfig().get(UP_DOWN_STATUS_GA));
            if (address.equals(destination)) {
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_ROLLERSHUTTER), (State) state);
            }
        } catch (Exception e) {
            // do nothing, we move on (either config parameter null, or wrong address format)
        }

    }

    @Override
    public String getDPT(GroupAddress destination) {

        try {
            GroupAddress address = new GroupAddress((String) getConfig().get(RollerShutterThingHandler.UP_DOWN_GA));

            if (address.equals(destination)) {
                return "1.008";
            }
        } catch (Exception e) {
            // do nothing, we move on (either config parameter null, or wrong address format)
        }

        try {
            GroupAddress address = new GroupAddress(
                    (String) getConfig().get(RollerShutterThingHandler.UP_DOWN_STATUS_GA));

            if (address.equals(destination)) {
                return "1.008";
            }
        } catch (Exception e) {
            // do nothing, we move on (either config parameter null, or wrong address format)
        }

        try {
            GroupAddress address = new GroupAddress((String) getConfig().get(RollerShutterThingHandler.STOP_MOVE_GA));

            if (address.equals(destination)) {
                return "1.010";
            }
        } catch (Exception e) {
            // do nothing, we move on (either config parameter null, or wrong address format)
        }

        try {
            GroupAddress address = new GroupAddress(
                    (String) getConfig().get(RollerShutterThingHandler.STOP_MOVE_STATUS_GA));

            if (address.equals(destination)) {
                return "1.010";
            }
        } catch (Exception e) {
            // do nothing, we move on (either config parameter null, or wrong address format)
        }

        try {
            GroupAddress address = new GroupAddress((String) getConfig().get(RollerShutterThingHandler.POSITION_GA));

            if (address.equals(destination)) {
                return "5.001";
            }
        } catch (Exception e) {
            // do nothing, we move on (either config parameter null, or wrong address format)
        }

        try {
            GroupAddress address = new GroupAddress(
                    (String) getConfig().get(RollerShutterThingHandler.POSITION_STATUS_GA));

            if (address.equals(destination)) {
                return "5.001";
            }
        } catch (Exception e) {
            // do nothing, we move on (either config parameter null, or wrong address format)
        }

        return null;
    }

    @Override
    public String getDPT(ChannelUID channelUID, Type command) {

        if ((String) getConfig().get(RollerShutterThingHandler.UP_DOWN_GA) == null
                && (String) getConfig().get(RollerShutterThingHandler.POSITION_GA) != null) {
            return ((KNXBridgeBaseThingHandler) getBridge().getHandler()).toDPTid(PercentType.class);
        }

        return ((KNXBridgeBaseThingHandler) getBridge().getHandler()).toDPTid(command.getClass());
    }

    @Override
    public String getAddress(ChannelUID channelUID, Type command) {

        if ((String) getConfig().get(RollerShutterThingHandler.UP_DOWN_GA) == null
                && (String) getConfig().get(RollerShutterThingHandler.POSITION_GA) != null) {
            return (String) getConfig().get(RollerShutterThingHandler.POSITION_GA);
        }

        if (command instanceof PercentType) {
            return (String) getConfig().get(RollerShutterThingHandler.POSITION_GA);
        }

        if (command instanceof UpDownType) {
            return (String) getConfig().get(RollerShutterThingHandler.UP_DOWN_GA);
        }

        if (command instanceof StopMoveType) {
            return (String) getConfig().get(RollerShutterThingHandler.STOP_MOVE_GA);
        }

        return null;
    }

    @Override
    public Type getType(ChannelUID channelUID, Type command) {

        if (command instanceof UpDownType) {
            if ((String) getConfig().get(RollerShutterThingHandler.UP_DOWN_GA) == null
                    && (String) getConfig().get(RollerShutterThingHandler.POSITION_GA) != null) {
                BigDecimal newCommand = null;
                if (command == UpDownType.UP) {
                    newCommand = lastPosition.toBigDecimal().add(new BigDecimal(1));
                }
                if (command == UpDownType.DOWN) {
                    newCommand = lastPosition.toBigDecimal().add(new BigDecimal(-1));
                }

                newCommand = new BigDecimal(Math.max(0, newCommand.longValue()));
                newCommand = new BigDecimal(Math.min(100, newCommand.longValue()));

                lastPosition = new PercentType(newCommand);

                return lastPosition;
            }
        }

        if (command instanceof PercentType) {
            lastPosition = (PercentType) command;
        }

        // by default, just return the Type we received initially, without any pre-processing
        return command;
    }
}
