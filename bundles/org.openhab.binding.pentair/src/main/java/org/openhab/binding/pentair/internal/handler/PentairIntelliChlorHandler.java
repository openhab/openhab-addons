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
package org.openhab.binding.pentair.internal.handler;

import static org.openhab.binding.pentair.internal.PentairBindingConstants.*;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.pentair.internal.parser.PentairBasePacket;
import org.openhab.binding.pentair.internal.parser.PentairIntelliChlorPacket;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PentairIntelliChlorHandler} is responsible for implementation of the Intellichlor Salt generator. It will
 * process Intellichlor commands and set the appropriate channel states. There are currently no commands implemented for
 * this Thing to receive from the framework.
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class PentairIntelliChlorHandler extends PentairBaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(PentairIntelliChlorHandler.class);

    public int version;
    public String name = "";

    /** for a saltoutput packet, represents the salt output percent */
    private int saltOutput;
    /** for a salinity packet, is value of salinity. Must be multiplied by 50 to get the actual salinity value. */
    private int salinity;

    private boolean ok;
    private boolean lowFlow;
    private boolean lowSalt;
    private boolean veryLowSalt;
    private boolean highCurrent;
    private boolean cleanCell;
    private boolean lowVoltage;
    private boolean lowWaterTemp;
    private boolean commError;

    public PentairIntelliChlorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void goOnline() {
        PentairIntelliChlorHandler handler = Objects.requireNonNull(getBridgeHandler()).findIntellichlor();

        if (handler != null && !handler.equals(this)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.duplicate-intllichlor");
            return;
        } else {
            super.goOnline();
        }
    }

    @Override
    public void goOffline(ThingStatusDetail detail) {
        super.goOffline(detail);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.trace("IntelliChlor received refresh command");

            switch (channelUID.getId()) {
                case CHANNEL_INTELLICHLOR_SALTOUTPUT:
                    updateChannel(channelUID, saltOutput, Units.PERCENT);
                    break;
                case CHANNEL_INTELLICHLOR_SALINITY:
                    updateChannel(channelUID, salinity, Units.PARTS_PER_MILLION);
                    break;
                case CHANNEL_INTELLICHLOR_OK:
                    updateChannel(channelUID, ok);
                    break;
                case CHANNEL_INTELLICHLOR_LOWFLOW:
                    updateChannel(channelUID, lowFlow);
                    break;
                case CHANNEL_INTELLICHLOR_LOWSALT:
                    updateChannel(channelUID, lowSalt);
                    break;
                case CHANNEL_INTELLICHLOR_VERYLOWSALT:
                    updateChannel(channelUID, veryLowSalt);
                    break;
                case CHANNEL_INTELLICHLOR_HIGHCURRENT:
                    updateChannel(channelUID, highCurrent);
                    break;
                case CHANNEL_INTELLICHLOR_CLEANCELL:
                    updateChannel(channelUID, cleanCell);
                    break;
                case CHANNEL_INTELLICHLOR_LOWVOLTAGE:
                    updateChannel(channelUID, lowVoltage);
                    break;
                case CHANNEL_INTELLICHLOR_LOWWATERTEMP:
                    updateChannel(channelUID, lowWaterTemp);
                    break;
                case CHANNEL_INTELLICHLOR_COMMERROR:
                    updateChannel(channelUID, commError);
                    break;
            }
        }
    }

    @Override
    public void processPacketFrom(PentairBasePacket packet) {
        PentairIntelliChlorPacket p = (PentairIntelliChlorPacket) packet;

        switch (p.getByte(PentairIntelliChlorPacket.ACTION)) {
            case 0x03:
                version = p.getVersion();
                name = p.getName();

                Map<String, String> editProperties = editProperties();
                editProperties.put(CHANNEL_INTELLICHLOR_PROPERTYVERSION, Integer.toString(version));
                editProperties.put(CHANNEL_INTELLICHLOR_PROPERTYMODEL, name);
                updateProperties(editProperties);

                logger.debug("Intellichlor version: {}, {}", version, name);
                break;

            case 0x11: // set salt output % command
                saltOutput = p.getSaltOutput();
                updateChannel(new ChannelUID(getThing().getUID(), CHANNEL_INTELLICHLOR_SALTOUTPUT), saltOutput,
                        Units.PERCENT);
                logger.debug("Intellichlor set output % {}", saltOutput);
                break;
            case 0x12: // response to set salt output
                if (waitStatusForOnline) { // Only go online after first response from the Intellichlor
                    finishOnline();
                }

                salinity = p.getSalinity();

                ok = p.getOk();
                lowFlow = p.getLowFlow();
                lowSalt = p.getLowSalt();
                veryLowSalt = p.getVeryLowSalt();
                highCurrent = p.getHighCurrent();
                cleanCell = p.getCleanCell();
                lowVoltage = p.getLowVoltage();
                lowWaterTemp = p.getLowWaterTemp();

                this.refreshAllChannels();

                if (logger.isDebugEnabled()) {
                    String status = String.format(
                            "saltoutput = %d, salinity = %d, ok = %b, lowflow = %b, lowsalt = %b, verylowsalt = %b, highcurrent = %b, cleancell = %b, lowvoltage = %b, lowwatertemp = %b",
                            saltOutput, salinity, ok, lowFlow, lowSalt, veryLowSalt, highCurrent, cleanCell, lowVoltage,
                            lowWaterTemp);
                    logger.debug("IntelliChlor salinity/status: {}, {}", salinity, status);
                }
                break;
            case 0x14:
                logger.debug("IntelliChlor GetModel request (0x14): {}", p.toString());
                break;
        }
    }
}
