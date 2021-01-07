/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cul.max.internal.handler;

import static org.openhab.binding.cul.max.internal.MaxCulBindingConstants.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openhab.binding.cul.CULCommunicationException;
import org.openhab.binding.cul.CULHandler;
import org.openhab.binding.cul.CULListener;
import org.openhab.binding.cul.max.internal.discovery.MaxDeviceDiscoveryService;
import org.openhab.binding.cul.max.internal.message.sequencers.PairingInitialisationSequence;
import org.openhab.binding.cul.max.internal.message.sequencers.TimeUpdateRequestSequence;
import org.openhab.binding.cul.max.internal.messages.*;
import org.openhab.binding.cul.max.internal.messages.constants.MaxCulDevice;
import org.openhab.binding.cul.max.internal.messages.constants.MaxCulMsgType;
import org.openhab.binding.cul.max.internal.messages.constants.ThermostatControlMode;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Johannes Goehr (johgoe) - Initial contribution
 */
public class MaxCulCunBridgeHandler extends BaseBridgeHandler implements CULHandler, CULListener {

    /**
     * This sets the address of the controller i.e. us!
     */
    private final String srcAddr = "010203";

    /**
     * Set default group ID
     */
    private final byte DEFAULT_GROUP_ID = 0x1;

    private final Logger logger = LoggerFactory.getLogger(MaxCulCunBridgeHandler.class);

    private MaxCulMsgHandler messageHandler;
    private final Set<DevicePairingListener> devicePairingListeners = new CopyOnWriteArraySet<>();
    private final Map<String, MaxDevicesHandler> childThingHandlers = new HashMap<>();
    private final Set<String> knownChildSerials = new CopyOnWriteArraySet<>();
    private final Set<String> rfAddressesToSpyOn = new CopyOnWriteArraySet<>();
    private Timer pairModeTimer = null;
    private int pairModeTimeout = 60000;
    private boolean pairMode = false;
    protected String tzStr;

    public MaxCulCunBridgeHandler(Bridge thing) {
        super(thing);
        messageHandler = new MaxCulMsgHandler(srcAddr, rfAddressesToSpyOn, this);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.unmodifiableSet(Stream.of(MaxDeviceDiscoveryService.class).collect(Collectors.toSet()));
    }

    @Override
    public void initialize() {
        logger.trace("ThingHandler initialize: {}", getThing().getLabel());
        final Configuration config = getThing().getConfiguration();
        final String timezone = (String) config.get(PROPERTY_TIMEZONE);
        if (timezone == null || timezone.isEmpty()) {
            this.tzStr = "Europe/London";
        } else {
            this.tzStr = timezone;
        }
        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (ThingStatus.ONLINE != thing.getStatus()) {
            return;
        }
        switch (channelUID.getIdWithoutGroup()) {
            case CHANNEL_PAIR_MODE:
                if (command instanceof OnOffType) {
                    switch ((OnOffType) command) {
                        case ON:
                            /*
                             * turn on pair mode and schedule disabling of pairing
                             * mode
                             */
                            pairMode = true;
                            TimerTask task = new TimerTask() {
                                @Override
                                public void run() {
                                    logger.debug("{} pairMode timeout executed", getThing().getLabel());
                                    pairMode = false;
                                    updateState(channelUID, OnOffType.OFF);
                                }
                            };
                            if (pairModeTimer != null) {
                                pairModeTimer.cancel();
                                pairModeTimer = null;
                            }
                            pairModeTimer = new Timer();
                            pairModeTimer.schedule(task, pairModeTimeout);
                            logger.debug("{} pairMode enabled & timeout scheduled", getThing().getLabel());
                            break;
                        case OFF:
                            /*
                             * we are manually disabling, so clear the timer and the
                             * flag
                             */
                            pairMode = false;
                            if (pairModeTimer != null) {
                                logger.debug("{} pairMode timer cancelled", getThing().getLabel());
                                pairModeTimer.cancel();
                                pairModeTimer = null;
                            }
                            logger.debug("{} pairMode cleared", getThing().getLabel());
                            break;
                    }
                } else if (command instanceof RefreshType) {
                    updateState(channelUID, pairMode ? OnOffType.ON : OnOffType.OFF);
                } else {
                    logger.debug("{}: Unhandled command type: {}: {}", getThing().getLabel(), channelUID,
                            command.getClass());
                }
                break;
            case CHANNEL_LISTEN_MODE:
                if (command instanceof OnOffType) {
                    messageHandler.setListenMode((command == OnOffType.ON));
                } else if (command instanceof RefreshType) {
                    updateState(channelUID, messageHandler.getListenMode() ? OnOffType.ON : OnOffType.OFF);
                } else {
                    logger.debug("{}: Unhandled command type: {}: {}", getThing().getLabel(), channelUID,
                            command.getClass());
                }
                break;
        }
    }

    public boolean registerDeviceStatusListener(DevicePairingListener devicePairingListener) {
        if (devicePairingListener == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null deviceStatusListener.");
        }
        return devicePairingListeners.add(devicePairingListener);
    }

    public boolean unregisterDeviceStatusListener(DevicePairingListener devicePairingListener) {
        if (devicePairingListener == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null deviceStatusListener.");
        }
        boolean result = devicePairingListeners.remove(devicePairingListener);
        return result;
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        super.childHandlerInitialized(childHandler, childThing);
        if (childHandler instanceof MaxDevicesHandler) {
            try {
                String serialNumber = ((MaxDevicesHandler) childHandler).getDeviceSerial();
                String rfAddress = ((MaxDevicesHandler) childHandler).getRfAddress();
                synchronized (childHandler) {
                    childThingHandlers.put(rfAddress, (MaxDevicesHandler) childHandler);
                    rfAddressesToSpyOn.add(rfAddress);
                    knownChildSerials.add(serialNumber);
                    logger.trace("{}: Bridge added handling for rfAddress {} for serial {}", getThing().getLabel(),
                            rfAddress, serialNumber);
                }
            } catch (Exception e) {
                logger.warn("{}: Bridge can't add handle child {}", getThing().getLabel(), childThing.getLabel(), e);
            }
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof MaxDevicesHandler) {
            try {
                String serialNumber = ((MaxDevicesHandler) childHandler).getDeviceSerial();
                String rfAddress = ((MaxDevicesHandler) childHandler).getRfAddress();
                synchronized (childHandler) {
                    knownChildSerials.remove(serialNumber);
                    rfAddressesToSpyOn.remove(rfAddress);
                    childThingHandlers.remove(rfAddress);
                    logger.trace("{}: Bridge removed handling for rfAddress {} for serial {}", getThing().getLabel(),
                            rfAddress, serialNumber);
                }
            } catch (Exception e) {
                logger.warn("{}: Bridge can't remove handle child {}", getThing().getLabel(), childThing.getLabel(), e);
            }
        }
        super.childHandlerDisposed(childHandler, childThing);
    }

    public void checkDevice(String rfAddress, int type, String serialNumber) {
        if (!knownChildSerials.contains(serialNumber)) {
            // New device, not seen before, pass to Discovery
            for (DevicePairingListener devicePairingListener : devicePairingListeners) {
                try {
                    devicePairingListener.onDeviceAdded(getThing(), rfAddress, MaxCulDevice.getDeviceTypeFromInt(type),
                            serialNumber);
                } catch (Exception e) {
                    logger.error("An exception occurred while calling the DeviceStatusListener", e);
                }
                knownChildSerials.add(serialNumber);
            }
        }
    }

    /**
     * @return Returns the list of associated MAX! devices with this bridge.
     */
    public List<MaxDevicesHandler> getMaxDevicesHandles() {
        return getThing().getThings().stream().map(Thing::getHandler).filter(MaxDevicesHandler.class::isInstance)
                .map(MaxDevicesHandler.class::cast).collect(Collectors.toList());
    }

    public void maxCulMsgReceived(String data, boolean isBroadcast) {
        logger.debug("Received data from CUL: {}", data);
        MaxCulMsgType msgType = BaseMsg.getMsgType(data);
        logger.debug("Received msg type from CUL: {}; Broadcast: {}", msgType, isBroadcast);
        if (msgType == MaxCulMsgType.PAIR_PING) {
            logger.debug("Got PAIR_PING message");
            PairPingMsg pkt = new PairPingMsg(data);
            pkt.printMessage();
            if (!childThingHandlers.containsKey(pkt.srcAddrStr)) {
                checkDevice(pkt.srcAddrStr, pkt.type, pkt.serial);
            } else {
                /*
                 * Check if it's broadcast and we're in pair mode or a PAIR_PING message
                 * directly for us
                 */
                if (((pairMode && isBroadcast) || !isBroadcast)) {
                    logger.debug("Creating pairing sequencer");
                    PairingInitialisationSequence ps = new PairingInitialisationSequence(this.DEFAULT_GROUP_ID,
                            messageHandler, childThingHandlers.get(pkt.srcAddrStr));
                    messageHandler.startSequence(ps, pkt);
                }
            }
        } else {
            switch (msgType) {
                case WALL_THERMOSTAT_CONTROL: {
                    WallThermostatControlMsg wallThermCtrlMsg = new WallThermostatControlMsg(data);
                    wallThermCtrlMsg.printMessage();
                    sendMessageToChildThingHandler(wallThermCtrlMsg, isBroadcast);
                    break;
                }
                case SET_TEMPERATURE: {
                    SetTemperatureMsg setTempMsg = new SetTemperatureMsg(data);
                    setTempMsg.printMessage();
                    sendMessageToChildThingHandler(setTempMsg, isBroadcast);
                    break;
                }
                case THERMOSTAT_STATE: {
                    ThermostatStateMsg thermStateMsg = new ThermostatStateMsg(data);
                    thermStateMsg.printMessage();
                    sendMessageToChildThingHandler(thermStateMsg, isBroadcast);
                    break;
                }
                case WALL_THERMOSTAT_STATE: {
                    WallThermostatStateMsg wallThermStateMsg = new WallThermostatStateMsg(data);
                    wallThermStateMsg.printMessage();
                    sendMessageToChildThingHandler(wallThermStateMsg, isBroadcast);
                    break;
                }
                case TIME_INFO: {
                    TimeInfoMsg timeMsg = new TimeInfoMsg(data);
                    timeMsg.printMessage();
                    TimeUpdateRequestSequence timeSeq = new TimeUpdateRequestSequence(this.tzStr, messageHandler);
                    messageHandler.startSequence(timeSeq, timeMsg);
                    break;
                }
                case PUSH_BUTTON_STATE: {
                    PushButtonMsg pbMsg = new PushButtonMsg(data);
                    pbMsg.printMessage();
                    sendMessageToChildThingHandler(pbMsg, isBroadcast);
                    break;
                }
                case SHUTTER_CONTACT_STATE: {
                    ShutterContactStateMsg shutterContactStateMsg = new ShutterContactStateMsg(data);
                    shutterContactStateMsg.printMessage();
                    sendMessageToChildThingHandler(shutterContactStateMsg, isBroadcast);
                    break;
                }
                case ACK: {
                    AckMsg ackMsg = new AckMsg(data);
                    ackMsg.printMessage();
                    sendMessageToChildThingHandler(ackMsg, isBroadcast);
                    break;
                }
                default:
                    logger.debug("Unhandled message type {}", msgType.toString());
                    break;
            }
        }
    }

    private void sendMessageToChildThingHandler(BaseMsg msg, boolean isBroadcast) {
        MaxDevicesHandler childThingHandler = childThingHandlers.get(msg.srcAddrStr);
        if (childThingHandler == null) {
            logger.warn("No handler for rfAddress {} found", msg.srcAddrStr);
        }
        childThingHandler.processMessage(msg);
        if (!isBroadcast) {
            this.messageHandler.sendAck(msg);
        }
    }

    public void resetDevice(MaxDevicesHandler maxDevicesHandler) {
        messageHandler.sendReset(maxDevicesHandler.getRfAddress());
    }

    public void sendSetDisplayActualTemp(MaxDevicesHandler maxDevicesHandler, boolean b) {
        messageHandler.sendSetDisplayActualTemp(maxDevicesHandler.getRfAddress(), b);
    }

    public void sendSetTemperature(MaxDevicesHandler maxDevicesHandler, ThermostatControlMode mode, double temp) {
        messageHandler.sendSetTemperature(maxDevicesHandler.getRfAddress(), mode, temp);
    }

    private CULHandler getCulHandler() {
        Bridge bridge = getBridge();
        BridgeHandler bridgeHandler = bridge.getHandler();
        if (bridgeHandler instanceof CULHandler) {
            updateStatus(ThingStatus.ONLINE);
            return (CULHandler) bridgeHandler;
        }
        updateStatus(ThingStatus.OFFLINE);
        return null;
    }

    @Override
    public void send(String command) throws CULCommunicationException {
        CULHandler culHandler = getCulHandler();
        if (culHandler != null) {
            culHandler.send(command);
        } else {
            logger.warn("Could not send command {}", command);
        }
    }

    @Override
    public int getCredit10ms() {
        CULHandler culHandler = getCulHandler();
        if (culHandler != null) {
            return culHandler.getCredit10ms();
        } else {
            logger.warn("Could not getCredit10ms");
            return 0;
        }
    }

    @Override
    public void dataReceived(String data) {
        updateStatus(ThingStatus.ONLINE);
        messageHandler.dataReceived(data);
    }

    @Override
    public void error(Exception e) {
        updateStatus(ThingStatus.ONLINE);
        messageHandler.error(e);
    }
}
