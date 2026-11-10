/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.tailwind.internal.state;

import static org.openhab.binding.tailwind.internal.TailwindBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TailwindState.java} class represents/sets the state of channels (associated items) of a TailWind thing.
 *
 * @author Dave J Schoepel - Initial contribution
 */
@NonNullByDefault
public class TailwindState {

    private final Logger logger = LoggerFactory.getLogger(TailwindState.class);

    // ----- General (Controller Specific) ------
    private State doorNum = DecimalType.valueOf("99");
    private State nightModeEnabled = DecimalType.valueOf("99");
    private State ledBrightness = PercentType.ZERO;
    private State routerRSSI = DecimalType.ZERO;
    private State productID = StringType.EMPTY;
    private State deviceID = StringType.EMPTY;
    private State firmwareVersion = StringType.EMPTY;
    private State supportCommand = StringType.valueOf(TAILWIND_JSON_VALUE_NAME_IDENTIFY);
    private State serverMonitor = UnDefType.UNDEF;

    // ----- Door One Specific Channels -----
    private State doorOneIndex = DecimalType.valueOf("99");
    private State doorOneStatus = StringType.EMPTY;
    private State doorOneOpenClose = StringType.EMPTY;
    private State doorOnePartialOpen = UnDefType.UNDEF;
    private State doorOneLockup = DecimalType.valueOf("99");
    private State doorOneDisabled = DecimalType.valueOf("99");

    // ----- Door Two Specific Channels -----
    private State doorTwoIndex = DecimalType.valueOf("99");
    private State doorTwoStatus = StringType.EMPTY;
    private State doorTwoOpenClose = StringType.EMPTY;
    private State doorTwoPartialOpen = DecimalType.valueOf("2.5");
    private State doorTwoLockup = DecimalType.valueOf("99");
    private State doorTwoDisabled = DecimalType.valueOf("99");

    // ----- Door Three Specific Channels -----
    private State doorThreeIndex = DecimalType.valueOf("99");
    private State doorThreeStatus = StringType.EMPTY;
    private State doorThreeOpenClose = StringType.EMPTY;
    private State doorThreePartialOpen = DecimalType.valueOf("2.5");
    private State doorThreeLockup = DecimalType.valueOf("99");
    private State doorThreeDisabled = DecimalType.valueOf("99");

    private TailwindStateChangedListener handler;

    /**
     * TailwindState constructor associates the state changes to the handler for a thing
     *
     * @param handler - handler for this Tailwind thing
     */
    public TailwindState(TailwindStateChangedListener handler) {
        this.handler = handler;
        logger.debug("Night mode enabled = {}", nightModeEnabled);
    }

    /**
     * @param errorMessage
     */
    public void connectionError(String errorMessage) {
        handler.connectionError(errorMessage);
    }

    /**
     * Method to get state for NAD AVR thing channels
     *
     * @param channelID - thing channels supported by this binding
     * @return - current state for the channel
     */
    public State getStateForChannelID(String channelID) {
        switch (channelID) {
            /**
             * General (Controller Channels)
             */
            case CHANNEL_DOOR_NUM:
                return doorNum;
            case CHANNEL_NIGHT_MODE_ENABLED:
                return nightModeEnabled;
            case CHANNEL_LED_BRIGHTNESS:
                return ledBrightness;
            case CHANNEL_ROUTER_RSSI:
                return routerRSSI;
            case CHANNEL_PRODUCT_ID:
                return productID;
            case CHANNEL_DEVICE_ID:
                return deviceID;
            case CHANNEL_FIRMWARE_VERSION:
                return firmwareVersion;
            case CHANNEL_SUPPORT_COMMAND:
                return supportCommand;
            case CHANNEL_SERVER_MONITOR:
                return serverMonitor;
            /**
             * Door One Channels
             */
            case CHANNEL_DOOR_1_CONTROLS_INDEX:
                return doorOneIndex;
            case CHANNEL_DOOR_1_CONTROLS_STATUS:
                return doorOneStatus;
            case CHANNEL_DOOR_1_CONTROLS_OPEN_CLOSE:
                return doorOneOpenClose;
            case CHANNEL_DOOR_1_CONTROLS_PARTIAL_OPEN:
                return doorOnePartialOpen;
            case CHANNEL_DOOR_1_CONTROLS_LOCKUP:
                return doorOneLockup;
            case CHANNEL_DOOR_1_CONTROLS_DISABLED:
                return doorOneDisabled;
            /**
             * Door Two Channels
             */
            case CHANNEL_DOOR_2_CONTROLS_INDEX:
                return doorTwoIndex;
            case CHANNEL_DOOR_2_CONTROLS_STATUS:
                return doorTwoStatus;
            case CHANNEL_DOOR_2_CONTROLS_OPEN_CLOSE:
                return doorTwoOpenClose;
            case CHANNEL_DOOR_2_CONTROLS_PARTIAL_OPEN:
                return doorTwoPartialOpen;
            case CHANNEL_DOOR_2_CONTROLS_LOCKUP:
                return doorTwoLockup;
            case CHANNEL_DOOR_2_CONTROLS_DISABLED:
                return doorTwoDisabled;
            /**
             * Door Three Channels
             */
            case CHANNEL_DOOR_3_CONTROLS_INDEX:
                return doorThreeIndex;
            case CHANNEL_DOOR_3_CONTROLS_STATUS:
                return doorThreeStatus;
            case CHANNEL_DOOR_3_CONTROLS_OPEN_CLOSE:
                return doorThreeOpenClose;
            case CHANNEL_DOOR_3_CONTROLS_PARTIAL_OPEN:
                return doorThreePartialOpen;
            case CHANNEL_DOOR_3_CONTROLS_LOCKUP:
                return doorThreeLockup;
            case CHANNEL_DOOR_3_CONTROLS_DISABLED:
                return doorThreeDisabled;
            default:
                return UnDefType.UNDEF;
        } // Switch/case for ChannelID
    }

    /**
     * Method to set Number of Doors state for the TailWind controller
     *
     * @param doorNum - number of doors being controlled to be set (1-3)
     */
    public void setDoorNum(long doorNum) {
        DecimalType newVal = new DecimalType(doorNum);
        if (!newVal.equals(this.doorNum)) {
            this.doorNum = newVal;
            handler.stateChanged(CHANNEL_DOOR_NUM, this.doorNum);
        } // If doorNum changed
    }

    /**
     * Method to set night mode state for the TailWind controller
     *
     * @param nightModeEnabled - when set to on (1, off=0) ensures door is closed between time range set in app
     */
    public void setNightModeEnabled(long nightModeEnabled) {
        DecimalType newVal = new DecimalType(nightModeEnabled);
        if (!newVal.equals(this.nightModeEnabled)) {
            this.nightModeEnabled = newVal;
            handler.stateChanged(CHANNEL_NIGHT_MODE_ENABLED, this.nightModeEnabled);
        } // If nightModeEnabled changed
    }

    /**
     * Method to set the led brightness state for the TailWind controller
     *
     * @param ledBrightness - brightness of the controller's led lamp ranges from 0 - 100,
     */
    public void setLedBrighness(int ledBrightness) {
        PercentType newVal = new PercentType(ledBrightness);
        if (!newVal.equals(this.ledBrightness)) {
            this.ledBrightness = newVal;
            handler.stateChanged(CHANNEL_LED_BRIGHTNESS, this.ledBrightness);
        } // If led brightness changed
    }

    /**
     * Method to set the Wi-Fi signal strength state for the TailWind controller
     *
     * @param routerRSSI - network Wi-Fi signal strength near TailWind controller (negative decimal)
     */
    public void setRouterRSSI(long routerRSSI) {
        DecimalType newVal = new DecimalType(routerRSSI);
        if (!newVal.equals(this.routerRSSI)) {
            this.routerRSSI = newVal;
            handler.stateChanged(CHANNEL_ROUTER_RSSI, this.routerRSSI);
        } // If routerRSSI changed
    }

    /**
     * Method to set the product ID state for the TailWind controller
     *
     * @param productID - model number of TailWind device (iQ3, ...)
     */
    public void setProductID(String productID) {
        StringType newVal = StringType.valueOf(productID);
        if (!newVal.equals(this.productID)) {
            this.productID = newVal;
            handler.stateChanged(CHANNEL_PRODUCT_ID, this.productID);
        } // If productID changed
    }

    /**
     * Method to set the device ID state for the TailWind controller
     *
     * @param deviceID - network hardware MAC address of TailWind device (format = _aa_bb_cc_dd_ee_ff_)
     */
    public void setDeviceID(String deviceID) {
        StringType newVal = StringType.valueOf(deviceID);
        if (!newVal.equals(this.deviceID)) {
            this.deviceID = newVal;
            handler.stateChanged(CHANNEL_DEVICE_ID, this.deviceID);
        } // If deviceID changed
    }

    /**
     * Method to set the firmware version state for the TailWind controller
     *
     * @param firmwareVersion - string containing version number details
     */
    public void setFirmwareVersion(String firmwareVersion) {
        StringType newVal = StringType.valueOf(firmwareVersion);
        if (!newVal.equals(this.firmwareVersion)) {
            this.firmwareVersion = newVal;
            handler.stateChanged(CHANNEL_FIRMWARE_VERSION, this.firmwareVersion);
        } // If firmwareVersion changed
    }

    /**
     * Method to set the support command state for the TailWind controller
     *
     * @param supportCommand - string containing support command selected (identify, reboot)
     */
    public void setSupportCommand(String supportCommand) {
        StringType newVal = StringType.valueOf(supportCommand);
        if (!newVal.equals(this.supportCommand)) {
            this.supportCommand = newVal;
            handler.stateChanged(CHANNEL_SUPPORT_COMMAND, this.supportCommand);
        } // If firmwareVersion changed
    }

    /**
     * Method to set the server connection monitoring state for the TailWind controller
     *
     * @param serverMonitor - true when the controller reboots itself after losing its cloud connection,
     *            null when the firmware does not report it (before 10.61)
     */
    public void setServerMonitor(@Nullable Boolean serverMonitor) {
        State newVal = serverMonitor == null ? UnDefType.UNDEF : OnOffType.from(serverMonitor);
        if (!newVal.equals(this.serverMonitor)) {
            this.serverMonitor = newVal;
            handler.stateChanged(CHANNEL_SERVER_MONITOR, this.serverMonitor);
        } // If serverMonitor changed
    }

    /**
     * Method to set the Door index state for the TailWind controller doors
     *
     * @param prefix - door index (0=Door 1, 1=Door 2, 2=Door 3)
     * @param status - value indicating index 0 - 2
     */
    public void setDoorIndex(Integer index, long doorIndex) {
        DecimalType newVal = new DecimalType(doorIndex);
        switch (index) {
            case 0:
                if (!newVal.equals(this.doorOneIndex)) {
                    this.doorOneIndex = newVal;
                    handler.stateChanged(CHANNEL_DOOR_1_CONTROLS_INDEX, this.doorOneIndex);
                }
                break;
            case 1:
                if (!newVal.equals(this.doorTwoIndex)) {
                    this.doorTwoIndex = newVal;
                    handler.stateChanged(CHANNEL_DOOR_2_CONTROLS_INDEX, this.doorTwoIndex);
                }
                break;
            case 2:
                if (!newVal.equals(this.doorThreeIndex)) {
                    this.doorThreeIndex = newVal;
                    handler.stateChanged(CHANNEL_DOOR_3_CONTROLS_INDEX, this.doorThreeIndex);
                }
                break;
            default:
                break;
        }
    } // If door (index) Lockup changed

    /**
     * Method to set the Door status state for the TailWind controller
     *
     * @param prefix - door index (0=Door 1, 1=Door 2, 2=Door 3)
     * @param status - string indicating status (open, close, lock, enable, disable, reboot)
     */
    public void setDoorStatus(Integer index, String doorStatus) {
        StringType newVal = StringType.valueOf(doorStatus);
        switch (index) {
            case 0:
                if (!newVal.equals(this.doorOneStatus)) {
                    this.doorOneStatus = newVal;
                    handler.stateChanged(CHANNEL_DOOR_1_CONTROLS_STATUS, this.doorOneStatus);
                }
                break;
            case 1:
                if (!newVal.equals(this.doorTwoStatus)) {
                    this.doorTwoStatus = newVal;
                    handler.stateChanged(CHANNEL_DOOR_2_CONTROLS_STATUS, this.doorTwoStatus);
                }
                break;
            case 2:
                if (!newVal.equals(this.doorThreeStatus)) {
                    this.doorThreeStatus = newVal;
                    handler.stateChanged(CHANNEL_DOOR_3_CONTROLS_STATUS, this.doorThreeStatus);
                }
                break;
            default:
                break;
        }
    } // If door (index) status changed

    /**
     * Method to set the Door open/close state for the TailWind controller
     *
     * @param prefix - door index (0=Door 1, 1=Door 2, 2=Door 3)
     * @param status - string indicating status (open, close, lock, enable, disable, reboot)
     */
    public void setDoorOpenClose(Integer index, String doorOpenClose) {
        StringType newVal = StringType.valueOf(doorOpenClose);
        switch (index) {
            case 0:
                if (!newVal.equals(this.doorOneOpenClose)) {
                    this.doorOneOpenClose = newVal;
                    handler.stateChanged(CHANNEL_DOOR_1_CONTROLS_OPEN_CLOSE, this.doorOneOpenClose);
                }
                break;
            case 1:
                if (!newVal.equals(this.doorTwoOpenClose)) {
                    this.doorTwoOpenClose = newVal;
                    handler.stateChanged(CHANNEL_DOOR_2_CONTROLS_OPEN_CLOSE, this.doorTwoOpenClose);
                }
                break;
            case 2:
                if (!newVal.equals(this.doorThreeOpenClose)) {
                    this.doorThreeOpenClose = newVal;
                    handler.stateChanged(CHANNEL_DOOR_3_CONTROLS_OPEN_CLOSE, this.doorThreeOpenClose);
                }
                break;
            default:
                break;
        }
    } // If door (index) status changed

    /**
     * Method to set the Door partial open state for the TailWind controller
     *
     * @param index
     * @param command
     */
    public void setPartialOpen(Integer index, float partialOpenSeconds) {
        DecimalType newVal = new DecimalType(partialOpenSeconds);
        switch (index) {
            case 0:
                if (!newVal.equals(this.doorOnePartialOpen)) {
                    this.doorOnePartialOpen = newVal;
                    handler.stateChanged(CHANNEL_DOOR_1_CONTROLS_PARTIAL_OPEN, this.doorOnePartialOpen);
                }
                break;
            case 1:
                if (!newVal.equals(this.doorTwoPartialOpen)) {
                    this.doorTwoPartialOpen = newVal;
                    handler.stateChanged(CHANNEL_DOOR_2_CONTROLS_PARTIAL_OPEN, this.doorTwoPartialOpen);
                }
                break;
            case 2:
                if (!newVal.equals(this.doorThreePartialOpen)) {
                    this.doorThreePartialOpen = newVal;
                    handler.stateChanged(CHANNEL_DOOR_3_CONTROLS_PARTIAL_OPEN, this.doorThreePartialOpen);
                }
                break;
            default:
                break;
        }
    } // If door (index) partial open changed

    /**
     * Method to set the Door locked state for the TailWind controller
     *
     * @param prefix - door index (0=Door 1, 1=Door 2, 2=Door 3)
     * @param status - value indicating locked (1) or un-locked (0) status
     */
    public void setLockup(Integer index, long doorLockup) {
        DecimalType newVal = new DecimalType(doorLockup);
        switch (index) {
            case 0:
                if (!newVal.equals(this.doorOneLockup)) {
                    this.doorOneLockup = newVal;
                    handler.stateChanged(CHANNEL_DOOR_1_CONTROLS_LOCKUP, this.doorOneLockup);
                }
                break;
            case 1:
                if (!newVal.equals(this.doorTwoLockup)) {
                    this.doorTwoLockup = newVal;
                    handler.stateChanged(CHANNEL_DOOR_2_CONTROLS_LOCKUP, this.doorTwoLockup);
                }
                break;
            case 2:
                if (!newVal.equals(this.doorThreeLockup)) {
                    this.doorThreeLockup = newVal;
                    handler.stateChanged(CHANNEL_DOOR_3_CONTROLS_LOCKUP, this.doorThreeLockup);
                }
                break;
            default:
                break;
        }
    } // If door (index) Lockup changed

    /**
     * Method to set the Door disabled state for the TailWind controller
     *
     * @param prefix - door index (0=Door 1, 1=Door 2, 2=Door 3)
     * @param status - value indicating door is disabled (1) or not disabled (0) status
     */
    public void setDisabled(Integer index, long doorDisabled) {
        DecimalType newVal = new DecimalType(doorDisabled);
        switch (index) {
            case 0:
                if (!newVal.equals(this.doorOneDisabled)) {
                    this.doorOneDisabled = newVal;
                    handler.stateChanged(CHANNEL_DOOR_1_CONTROLS_DISABLED, this.doorOneDisabled);
                }
                break;
            case 1:
                if (!newVal.equals(this.doorTwoDisabled)) {
                    this.doorTwoDisabled = newVal;
                    handler.stateChanged(CHANNEL_DOOR_2_CONTROLS_DISABLED, this.doorTwoDisabled);
                }
                break;
            case 2:
                if (!newVal.equals(this.doorThreeDisabled)) {
                    this.doorThreeDisabled = newVal;
                    handler.stateChanged(CHANNEL_DOOR_3_CONTROLS_DISABLED, this.doorThreeDisabled);
                }
                break;
            default:
                break;
        }
    } // If door (index) Disabled changed
}
