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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.pentair.internal.handler.helpers.PentairIntelliChem;
import org.openhab.binding.pentair.internal.parser.PentairBasePacket;
import org.openhab.binding.pentair.internal.parser.PentairStandardPacket;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PentairIntelliChemHandler} is responsible for implementation of the IntelliChem. This will
 * parse of status packets to set the stat for various channels. All channels are read only.
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class PentairIntelliChemHandler extends PentairBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(PentairIntelliChemHandler.class);

    private PentairIntelliChem pic = new PentairIntelliChem();

    private String firmwareVersion = "";

    public PentairIntelliChemHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            // The IntelliChem routinely updates the state, so just refresh to last state
            switch (channelUID.getId()) {
                case CHANNEL_INTELLICHEM_PHREADING:
                    updateChannel(channelUID, pic.phReading);
                    break;
                case CHANNEL_INTELLICHEM_ORPREADING:
                    updateChannel(channelUID, pic.orpReading);
                    break;
                case CHANNEL_INTELLICHEM_PHSETPOINT:
                    updateChannel(channelUID, pic.phSetPoint);
                    break;
                case CHANNEL_INTELLICHEM_ORPSETPOINT:
                    updateChannel(channelUID, pic.orpSetPoint);
                    break;
                case CHANNEL_INTELLICHEM_TANK1LEVEL:
                    updateChannel(channelUID, pic.tank1Level);
                    break;
                case CHANNEL_INTELLICHEM_TANK2LEVEL:
                    updateChannel(channelUID, pic.tank2Level);
                    break;
                case CHANNEL_INTELLICHEM_CALCIUMHARDNESS:
                    updateChannel(channelUID, pic.calciumHardness, Units.PARTS_PER_MILLION);
                    break;
                case CHANNEL_INTELLICHEM_CYAREADING:
                    updateChannel(channelUID, pic.cyaReading);
                    break;
                case CHANNEL_INTELLICHEM_ALKALINITY:
                    updateChannel(channelUID, pic.alkalinity);
                    break;
                case CHANNEL_INTELLICHEM_LSI:
                    updateChannel(channelUID, pic.lsi);
                    break;
                case CHANNEL_INTELLICHEM_PHDOSERTYPE:
                    updateChannel(channelUID, pic.phDoserType.name());
                    break;
                case CHANNEL_INTELLICHEM_ORPDOSERTYPE:
                    updateChannel(channelUID, pic.orpDoserType.name());
                    break;
                case CHANNEL_INTELLICHEM_PHDOSERSTATUS:
                    updateChannel(channelUID, pic.phDoserStatus.name());
                    break;
                case CHANNEL_INTELLICHEM_ORPDOSERSTATUS:
                    updateChannel(channelUID, pic.orpDoserStatus.name());
                    break;
                case CHANNEL_INTELLICHEM_PHDOSETIME:
                    updateChannel(channelUID, pic.phDoseTime, Units.SECOND);
                    break;
                case CHANNEL_INTELLICHEM_ORPDOSETIME:
                    updateChannel(channelUID, pic.orpDoseTime, Units.SECOND);
                    break;
                case CHANNEL_INTELLICHEM_SALTLEVEL:
                    updateChannel(channelUID, pic.saltLevel);
                    break;
                case CHANNEL_INTELLICHEM_ALARMWATERFLOW:
                    updateChannel(channelUID, pic.alarmWaterFlow);
                    break;
                case CHANNEL_INTELLICHEM_ALARMPH:
                    updateChannel(channelUID, pic.alarmPh);
                    break;
                case CHANNEL_INTELLICHEM_ALARMORP:
                    updateChannel(channelUID, pic.alarmOrp);
                    break;
                case CHANNEL_INTELLICHEM_ALARMPHTANK:
                    updateChannel(channelUID, pic.alarmPhTank);
                    break;
                case CHANNEL_INTELLICHEM_ALARMORPTANK:
                    updateChannel(channelUID, pic.alarmOrpTank);
                    break;
                case CHANNEL_INTELLICHEM_ALARMPROBEFAULT:
                    updateChannel(channelUID, pic.alarmProbeFault);
                    break;
                case CHANNEL_INTELLICHEM_WARNINGPHLOCKOUT:
                    updateChannel(channelUID, pic.warningPhLockout);
                    break;
                case CHANNEL_INTELLICHEM_WARNINGPHDAILYLIMITREACHED:
                    updateChannel(channelUID, pic.warningPhDailyLimitReached);
                    break;
                case CHANNEL_INTELLICHEM_WARNINGORPDAILYLIMITREACHED:
                    updateChannel(channelUID, pic.warningOrpDailyLimitReached);
                    break;
                case CHANNEL_INTELLICHEM_WARNINGINVALIDSETUP:
                    updateChannel(channelUID, pic.warningInvalidSetup);
                    break;
                case CHANNEL_INTELLICHEM_WARNINGCHLORINATORCOMMERROR:
                    updateChannel(channelUID, pic.warningChlorinatorCommError);
                    break;
            }
        }
    }

    @Override
    public void processPacketFrom(PentairBasePacket packet) {
        if (waitStatusForOnline) {
            finishOnline();
        }

        PentairStandardPacket p = (PentairStandardPacket) packet;

        switch (p.getByte(PentairStandardPacket.ACTION)) {
            case 0x12: // Status packet
                pic.parsePacket(p);
                logger.debug("Intellichem status: {}: ", pic.toString());

                this.refreshAllChannels();

                if (!this.firmwareVersion.equals(pic.firmwareVersion)) {
                    firmwareVersion = pic.firmwareVersion;
                    updateProperty(PROPERTY_INTELLICHEM_FIRMWAREVERSION, pic.firmwareVersion);
                }
                break;

            default:
                logger.debug("Unhandled Intellichem packet: {}", p.toString());
                break;
        }
    }
}
