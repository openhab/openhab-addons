/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.souliss.handler;

import java.net.DatagramSocket;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.SoulissBindingProtocolConstants;
import org.openhab.binding.souliss.internal.protocol.SoulissBindingNetworkParameters;
import org.openhab.binding.souliss.internal.protocol.SoulissCommonCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @author Tonino Fazio
 * @since 1.7.0
 */
public abstract class SoulissGenericHandler extends BaseThingHandler implements typicalCommonMethods {

    Thing thing;

    private int iSlot;
    private int iNode;
    private static Logger logger = LoggerFactory.getLogger(SoulissGenericHandler.class);

    boolean bSecureSend = false; // 0 means that Secure Send is disabled
    boolean bExpectedValueSameAsSet = false; // true means that expected value is setpoint (only for T31, T19 and T6x)

    public SoulissGenericHandler(Thing _thing) {
        super(_thing);
        thing = _thing;
        int iPosNode_Slot = 2; // if uuid is of type souliss:gateway:[typical]:[node]-[slot] then node/slot is at
                               // position 2

        if (thing.getUID().getAsString().split(":").length > 3) {
            iPosNode_Slot = 3; // else, if uuid is of type souliss:gateway:[bridgeID]:[typical]:[node]-[slot] then
                               // node/slot is at position 3
        }
        try {
            iNode = Integer.parseInt(_thing.getUID().toString().split(":")[iPosNode_Slot]
                    .split(SoulissBindingConstants.UUID_NODE_SLOT_SEPARATOR)[0]);
            iSlot = Integer.parseInt(_thing.getUID().toString().split(":")[iPosNode_Slot]
                    .split(SoulissBindingConstants.UUID_NODE_SLOT_SEPARATOR)[1]);

        } catch (Exception e) {
            logger.debug("Item Definition Error. Use ex:'souliss:t11:nodeNumber-slotNumber'");
        }
    }

    /**
     * @return the iSlot
     */
    public int getSlot() {
        return iSlot;
    }

    /**
     * @param SoulissNode
     *            the SoulissNodeID to get
     */
    public int getNode() {
        return iNode;
    }

    /**
     * Send a command as hexadecimal, e.g.: Souliss_T1n_OnCmd = 0x02; short
     * Souliss_T1n_OffCmd = 0x04;
     *
     * @param command
     */
    public void commandSEND(byte command) {

        SoulissCommonCommands.sendFORCEFrame(getDatagramSocket(), getGatewayIP(), getGatewayNodeIndex(),
                getGatewayUserIndex(), this.getNode(), this.getSlot(), command);
    }

    public void commandSEND_RGB(byte command, byte R, byte G, byte B) {
        SoulissCommonCommands.sendFORCEFrame(getDatagramSocket(), getGatewayIP(), getGatewayNodeIndex(),
                getGatewayUserIndex(), this.getNode(), this.getSlot(), command, R, G, B);
    }

    public void commandSEND(byte command, byte B1, byte B2) {
        SoulissCommonCommands.sendFORCEFrameT31SetPoint(getDatagramSocket(), getGatewayIP(), getGatewayNodeIndex(),
                getGatewayUserIndex(), this.getNode(), this.getSlot(), command, B1, B2);
    }

    public void commandSEND(byte B1, byte B2) {
        SoulissCommonCommands.sendFORCEFrameT61SetPoint(getDatagramSocket(), getGatewayIP(), getGatewayNodeIndex(),
                getGatewayUserIndex(), this.getNode(), this.getSlot(), B1, B2);
    }

    /**
     * Create a time stamp as "yyyy-MM-dd'T'HH:mm:ssz"
     *
     * @return String timestamp
     */
    private static String getTimestamp() {
        // Pattern : yyyy-MM-dd'T'HH:mm:ss.SSSz
        String sTimestamp = "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz");
            Date n = new Date();
            sTimestamp = sdf.format(n.getTime());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return sTimestamp;
    }

    @Override
    public void thingUpdated(Thing _thing) {
        updateThing(_thing);
        this.thing = _thing;
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.BRIDGE_OFFLINE);
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @SuppressWarnings("null")
    public String getGatewayIP() {
        // return ((SoulissGatewayHandler) thingRegistry.get(thing.getBridgeUID()).getHandler()).IPAddressOnLAN;
        Bridge bridge = getBridge();
        if (bridge != null) {
            if (bridge.getHandler() != null) {
                return ((SoulissGatewayHandler) bridge.getHandler()).getGatewayIP();
            }
        }
        return null;
    }

    public String getLabel() {
        return thing.getLabel();
    }

    @SuppressWarnings("null")
    public byte getGatewayUserIndex() {
        if (getBridge() != null) {
            return ((SoulissGatewayHandler) getBridge().getHandler()).userIndex;
        }
        return 0;
    }

    @SuppressWarnings("null")
    public byte getGatewayNodeIndex() {
        if (getBridge() != null) {
            return ((SoulissGatewayHandler) getBridge().getHandler()).nodeIndex;
        }
        return 0;
    }

    public DatagramSocket getDatagramSocket() {
        return SoulissBindingNetworkParameters.getDatagramSocket();
    }

    public void setHealty(byte _shHealty) {
        this.updateState(SoulissBindingConstants.HEALTY_CHANNEL, new DecimalType(_shHealty & 0xFF));
    }

    public void setLastStatusStored() {
        this.updateState(SoulissBindingConstants.LASTSTATUSSTORED_CHANNEL, DateTimeType.valueOf(getTimestamp()));
    }

    protected OnOffType getOHState_OnOff_FromSoulissVal(byte sVal) {
        if (sVal == SoulissBindingProtocolConstants.Souliss_T1n_OnCoil) {
            return OnOffType.ON;
        } else if (sVal == SoulissBindingProtocolConstants.Souliss_T1n_OffCoil) {
            return OnOffType.OFF;
        } else if (sVal == SoulissBindingProtocolConstants.Souliss_T1n_OnFeedback) {
            return OnOffType.ON;
        } else if (sVal == SoulissBindingProtocolConstants.Souliss_T1n_OffFeedback) {
            return OnOffType.OFF;
        } else if (sVal == SoulissBindingProtocolConstants.Souliss_T4n_NotArmed) {
            return OnOffType.OFF;
        } else if (sVal == SoulissBindingProtocolConstants.Souliss_T4n_Armed) {
            return OnOffType.ON;
        }

        return null;
    }

    protected OpenClosedType getOHState_OpenClose_FromSoulissVal(byte sVal) {
        if (sVal == SoulissBindingProtocolConstants.Souliss_T1n_OnCoil) {
            return OpenClosedType.CLOSED;
        } else if (sVal == SoulissBindingProtocolConstants.Souliss_T1n_OffCoil) {
            return OpenClosedType.OPEN;
        }
        return null;
    }

}
