/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.souliss.internal.handler;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.souliss.internal.SoulissBindingConstants;
import org.openhab.binding.souliss.internal.SoulissProtocolConstants;
import org.openhab.binding.souliss.internal.config.GatewayConfig;
import org.openhab.binding.souliss.internal.protocol.CommonCommands;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;

/**
 * This class implements the base Souliss Typical All other Typicals derive from
 * this class
 *
 * ...from wiki of Dario De Maio
 * In Souliss the logics that drive your lights, curtains, LED, and
 * others are pre-configured into so called Typicals. A Typical is a
 * logic with a predefined set of inputs and outputs and a know
 * behavior, are used to standardize the user interface and have a
 * configuration-less behavior.
 *
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */

@NonNullByDefault
public abstract class SoulissGenericHandler extends BaseThingHandler implements TypicalCommonMethods {

    private int iSlot;
    private int iNode;

    private final CommonCommands commonCommands = new CommonCommands();

    // 0 means that Secure Send is disabled
    boolean bSecureSend = false;
    // true means that expected value is setpoint (only for T31, T19 and T6x)
    boolean bExpectedValueSameAsSet = false;

    protected SoulissGenericHandler(Thing thing) {
        super(thing);
    }

    /**
     * @return the iSlot
     */
    public int getSlot() {
        return iSlot;
    }

    @Override
    public void initialize() {
        try {
            var cfg = thing.getConfiguration();
            var props = cfg.getProperties();

            var pNode = props.get("node");
            var pSlot = props.get("slot");

            if ((pNode != null) && (pSlot != null)) {
                iNode = Integer.parseInt(pNode.toString());
                iSlot = Integer.parseInt(pSlot.toString());
                updateProperty(SoulissBindingConstants.PROPERTY_NODE, Integer.toString(iNode));
                updateProperty(SoulissBindingConstants.PROPERTY_SLOT, Integer.toString(iSlot));
                updateProperty(SoulissBindingConstants.PROPERTY_UNIQUEID,
                        "N" + Integer.toString(iNode) + "S" + Integer.toString(iSlot));
            }
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Error getting node/slot from souliss typical (thing config)");
        }
    }

    /**
     * @return the SoulissNodeID
     */
    public int getNode() {
        return iNode;
    }

    protected synchronized void commandReadNodeTypsStates() {
        var gwConfig = getGatewayConfig();
        if (gwConfig != null) {
            commonCommands.sendTypicalRequestFrame(gwConfig, this.getNode(), 1);
        }
    }

    /**
     * Send a command as hexadecimal, e.g.: SOULISS_T1N_ON_CMD = 0x02; short
     * SOULISS_T1N_OFF_CMD = 0x04;
     *
     * @param command
     */
    public void commandSEND(byte command) {
        var gwConfig = getGatewayConfig();
        if (gwConfig != null) {
            commonCommands.sendFORCEFrame(gwConfig, this.getNode(), this.getSlot(), command);
        }
    }

    public void commandSendRgb(byte command, byte r, byte g, byte b) {
        var gwConfig = getGatewayConfig();
        if (gwConfig != null) {
            commonCommands.sendFORCEFrame(gwConfig, command, r, g, b);
        }
    }

    public void commandSEND(byte command, byte b1, byte b2) {
        var gwConfig = getGatewayConfig();
        if (gwConfig != null) {
            commonCommands.sendFORCEFrameT31SetPoint(gwConfig, this.getNode(), this.getSlot(), command, b1, b2);
        }
    }

    public void commandSEND(byte b1, byte b2) {
        var gwConfig = getGatewayConfig();
        if (gwConfig != null) {
            commonCommands.sendFORCEFrameT61SetPoint(gwConfig, this.getNode(), this.getSlot(), b1, b2);
        }
    }

    /**
     * Create a time stamp as "yyyy-MM-dd'T'HH:mm:ssz"
     *
     * @return String timestamp
     */
    private static String getTimestamp() {
        // Pattern : yyyy-MM-dd'T'HH:mm:ssz
        var sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz");
        var n = new Date();
        return sdf.format(n.getTime());
    }

    @Override
    public void thingUpdated(Thing thing) {
        updateThing(thing);
    }

    public @Nullable GatewayConfig getGatewayConfig() {
        var bridge = getBridge();
        if (bridge != null) {
            SoulissGatewayHandler bridgeHandler = (SoulissGatewayHandler) bridge.getHandler();
            if (bridgeHandler != null) {
                return bridgeHandler.getGwConfig();
            }
        }
        return null;
    }

    public @Nullable String getLabel() {
        return getThing().getLabel();
    }

    public void setHealthy(byte shHealthy) {
        this.updateState(SoulissBindingConstants.HEALTHY_CHANNEL, new DecimalType(shHealthy & 0xFF));
        this.updateStatus(ThingStatus.ONLINE);
    }

    public void setLastStatusStored() {
        this.updateState(SoulissBindingConstants.LASTSTATUSSTORED_CHANNEL, DateTimeType.valueOf(getTimestamp()));
    }

    protected @Nullable OnOffType getOhStateOnOffFromSoulissVal(byte sVal) {
        if (sVal == SoulissProtocolConstants.SOULISS_T1N_ON_COIL) {
            return OnOffType.ON;
        } else if (sVal == SoulissProtocolConstants.SOULISS_T1N_OFF_COIL) {
            return OnOffType.OFF;
        } else if (sVal == SoulissProtocolConstants.SOULISS_T1N_ON_FEEDBACK) {
            return OnOffType.ON;
        } else if (sVal == SoulissProtocolConstants.SOULISS_T1N_OFF_FEEDBACK) {
            return OnOffType.OFF;
        } else if (sVal == SoulissProtocolConstants.SOULISS_T4N_NOT_ARMED) {
            return OnOffType.OFF;
        } else if (sVal == SoulissProtocolConstants.SOULISS_T4N_ARMED) {
            return OnOffType.ON;
        }

        return null;
    }

    protected @Nullable OpenClosedType getOhStateOpenCloseFromSoulissVal(byte sVal) {
        if (sVal == SoulissProtocolConstants.SOULISS_T1N_ON_COIL) {
            return OpenClosedType.CLOSED;
        } else if (sVal == SoulissProtocolConstants.SOULISS_T1N_OFF_COIL) {
            return OpenClosedType.OPEN;
        }
        return null;
    }
}
