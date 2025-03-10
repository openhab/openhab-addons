/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linky.internal.handler;

import static org.openhab.binding.linky.internal.constants.LinkyBindingConstants.*;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.linky.internal.config.LinkyConfiguration;
import org.openhab.binding.linky.internal.constants.LinkyBindingConstants;
import org.openhab.binding.linky.internal.helpers.LinkyFrame;
import org.openhab.binding.linky.internal.types.InvalidFrameException;
import org.openhab.binding.linky.internal.types.LinkyChannel;
import org.openhab.binding.linky.internal.types.LinkyTicMode;
import org.openhab.binding.linky.internal.types.Phase;
import org.openhab.binding.linky.internal.types.ValueType;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ThingLinkyLocalHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Laurent Arnal - Initial contribution
 *
 */

@NonNullByDefault
@SuppressWarnings("null")
public class ThingLinkyLocalHandler extends BaseThingHandler {
    private final TimeZoneProvider timeZoneProvider;
    private ZoneId zoneId = ZoneId.systemDefault();

    private final Logger logger = LoggerFactory.getLogger(ThingLinkyLocalHandler.class);

    private LinkyConfiguration config;

    public String userId = "";

    private String appKey = "";
    private String ivKey = "";
    private long idd2l = -1;
    private String prmId = "";

    private double cosphi = Double.NaN;

    private int frameCount = 0;

    public ThingLinkyLocalHandler(Thing thing, LocaleProvider localeProvider, TimeZoneProvider timeZoneProvider) {
        super(thing);

        config = getConfigAs(LinkyConfiguration.class);
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public synchronized void initialize() {
        prmId = config.prmId;
        logger.debug("Initializing Linky handler for {}", prmId);

        // update the timezone if not set to default to openhab default timezone
        Configuration thingConfig = getConfig();

        Object val = thingConfig.get("timezone");
        if (val == null || "".equals(val)) {
            zoneId = this.timeZoneProvider.getTimeZone();
            thingConfig.put("timezone", zoneId.getId());
        } else {
            zoneId = ZoneId.of((String) val);
        }

        appKey = (String) thingConfig.get("appKey");
        ivKey = (String) thingConfig.get("ivKey");
        String idd2lSt = (String) thingConfig.get("id");
        if (idd2lSt != null) {
            idd2l = Long.parseLong(idd2lSt);
        } else {
            idd2l = -1;
        }
        saveConfiguration(thingConfig);

        // reread config to update timezone field
        config = getConfigAs(LinkyConfiguration.class);

        Bridge bridge = getBridge();
        if (bridge == null) {
            return;
        }

        BridgeLocalBaseHandler bridgeHandler = (BridgeLocalBaseHandler) bridge.getHandler();
        if (bridgeHandler == null) {
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        bridgeHandler.registerNewPrmId(config.prmId);
        updateStatus(ThingStatus.ONLINE);
    }

    public void udpateGroupVisibility(LinkyFrame frame) {
        try {
            String id = getThing().getUID().getId();
            Phase phase = frame.getPhase();
            LinkyTicMode ticMode = frame.getTicMode();
            boolean isProducerMode = frame.isProducerMode();

            if (phase == Phase.ONE_PHASED) {
                udpateGroupVisibility(LINKY_LOCAL_3PHASE_GROUP);
            }

            if (!isProducerMode) {
                udpateGroupVisibility(LINKY_LOCAL_PRODUCER_GROUP);
            }

            if (ticMode == LinkyTicMode.HISTORICAL) {

            } else if (ticMode == LinkyTicMode.STANDARD) {

            }

            frameCount++;
        } catch (InvalidFrameException ex) {
        }
    }

    public void udpateGroupVisibility(String groupName) {
        ThingBuilder updateThing = editThing();

        for (LinkyChannel channel : LinkyChannel.values()) {
            if (channel.getGroupName().equals(groupName)) {

                Channel ch = this.getThing().getChannel(channel.getGroupName() + "#" + channel.getChannelName());
                if (ch != null) {
                    updateThing = updateThing.withoutChannel(ch.getUID());
                }

                ch = this.getThing().getChannel(channel.getGroupName() + "#" + channel.getChannelName() + "-date");
                if (ch != null) {
                    updateThing = updateThing.withoutChannel(ch.getUID());
                }
            }
        }

        updateThing(updateThing.build());
    }

    @Override
    public synchronized void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("Refreshing channel {} {}", config.prmId, channelUID.getId());
        } else {
            if (channelUID.getId().indexOf("cosphi") >= 0) {
                if (command instanceof DecimalType) {
                    DecimalType dc = (DecimalType) command;
                    cosphi = dc.doubleValue();
                }
            } else {
                logger.debug("The Linky local binding is read-only and can not handle command {} {}",
                        channelUID.getId(), command);
            }
        }
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

    public long getIdd2l() {
        return this.idd2l;
    }

    public String getPrmId() {
        return this.prmId;
    }

    public String getAppKey() {
        return this.appKey;
    }

    public String getIvKey() {
        return this.ivKey;
    }

    protected void handleFrame(LinkyFrame frame) {
        double urms = 0.0;
        double sinst = 0.0;

        if (frameCount == 0) {
            udpateGroupVisibility(frame);
        }

        for (Entry<LinkyChannel, String> entry : frame.getChannelToValues().entrySet()) {
            try {
                LinkyChannel channel = entry.getKey();
                if (channel.getChannelName().equals(CHANNEL_NONE)) {
                    continue;
                }

                String value = entry.getValue();
                if (value != null) {
                    String timestamp = null;
                    int pos1 = value.indexOf('|');

                    if (pos1 >= 0) {
                        timestamp = value.substring(0, pos1);
                        value = value.substring(pos1 + 1);
                    }

                    if (channel.getType() == ValueType.STRING) {
                        logger.trace("Update channel {} to value {}", channel.getChannelName(), value);

                        updateState(channel.getGroupName(), channel.getChannelName(), StringType.valueOf(value));
                    } else if (channel.getType() == ValueType.INTEGER) {
                        if (!value.isEmpty()) {
                            logger.trace("Update channel {} to value {}", channel.getChannelName(), value);

                            updateState(channel.getGroupName(), channel.getChannelName(), QuantityType
                                    .valueOf(channel.getFactor() * Integer.parseInt(value), channel.getUnit()));
                        }
                    } else if (channel.getType() == ValueType.DATE) {
                        if (!value.isEmpty()) {
                            Instant timestampConv = getAsInstant(value);

                            if (timestampConv != null) {
                                logger.trace("Update channel {} to value {}", channel.getChannelName(), value);

                                updateState(channel.getGroupName(), channel.getChannelName(),
                                        new DateTimeType(timestampConv));
                            }
                        }
                    }

                    if (channel == LinkyChannel.STGE) {
                        handleStgePayload(value);
                    } else if (channel == LinkyChannel.RELAIS) {
                        handleRelaisPayload(value);
                    } else if (channel == LinkyChannel.PJOURF_PLUS_1) {
                        handlePayload(LinkyChannel.PJOURF_PLUS_1.name(), value);
                    } else if (channel == LinkyChannel.PPOINTE) {
                        handlePayload(LinkyChannel.PPOINTE.name(), value);
                    }

                    if (channel == LinkyChannel.URMS1) {
                        urms = Double.valueOf(value);
                    } else if (channel == LinkyChannel.SINSTS) {
                        sinst = Double.valueOf(value);
                    }

                    if (timestamp != null) {
                        if (!channel.getTimestampChannelName().equals(CHANNEL_NONE)) {
                            Instant timestampConv = getAsInstant(timestamp);

                            if (timestampConv != null) {
                                logger.trace("Update channel {} to value {}", channel.getTimestampChannelName(),
                                        timestamp);

                                updateState(channel.getGroupName(), channel.getTimestampChannelName(),
                                        new DateTimeType(timestampConv));
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                logger.debug("err", ex);
            }
        }

        updateCalcVars(urms, sinst);

        // updateState(LINKY_DIRECT_MAIN_GROUP, "_ID_D2L", new StringType(payLoad.get("_ID_D2L")));
        // updateState(LINKY_DIRECT_MAIN_GROUP, "SINSTS", new StringType(payLoad.get("SINSTS")));
        // updateState(LINKY_DIRECT_MAIN_GROUP, "DATE", new StringType(payLoad.get("DATE")));
        // updateState(LINKY_DIRECT_MAIN_GROUP, "IRMS1", new StringType(payLoad.get("IRMS1")));
    }

    private void updateCalcVars(double urms, double sinst) {
        double irms1c = sinst / urms;

        LinkyChannel channelIrms1f = LinkyChannel.IRMS1F;
        LinkyChannel channelSactive = LinkyChannel.SACTIVE;
        LinkyChannel channelSreactive = LinkyChannel.SREACTIVE;

        updateState(channelIrms1f.getGroupName(), channelIrms1f.getChannelName(),
                QuantityType.valueOf(channelIrms1f.getFactor() * irms1c, channelIrms1f.getUnit()));

        if (Double.isNaN(cosphi)) {
            return;
        }

        double phi = Math.acos(cosphi);
        double sinphi = Math.sin(phi);

        double sactive = sinst * cosphi;
        double sreactive = sinst * sinphi;

        updateState(channelSactive.getGroupName(), channelSactive.getChannelName(),
                QuantityType.valueOf(channelSactive.getFactor() * sactive, channelSactive.getUnit()));

        updateState(channelSreactive.getGroupName(), channelSreactive.getChannelName(),
                QuantityType.valueOf(channelSreactive.getFactor() * sreactive, channelSreactive.getUnit()));
    }

    // @formatter:off
    //  1  0  0  0  0  1  0  0  0  1  1  1  0  1  0  1  1  0  0  1  1  0  0  0  0  0  0  0  0  0  1
    // 00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31
    // 31 30 29 28 27 26 25 24 23 22 21 20 19 18 17 16 15 14 13 12 11 10 09 08 07 06 05 04 03 02 01 00
    //
    //  0  0  0  0  0  0  0  0  0  0  1  1  1  0  1  0  0  1  0  0  0  1  0  0  0  0  0  0  0  0  0  1
    //  P==>  P==>  T==>  T==>  S  S==>  E==>  N  T  H  T==>  T========>  S  F  D  S  N  C  C=====>  C
    //  o     o     e     e     y  t     t     o  é  o  a     a           e  o  é  u  o  a  o        o
    //  i     i     m     m     n  a     a     t  l  r  r     r           n  n  p  r  t  c  u        n
    //  n     n     p     p     c  t     t        é  l  i     i           s  c  a  t     h  p        t
    //  t     t     o     o     h  u           U     o  f     f              t  s  e  u  e  u        a
    //  e     e                 r  t     S     s  i  g                       i  s  n  s     r        c
    //        +                 o        o     e  n  e  D     F              o  e  s  e     e        t
    //        1                    C     r     d  f     i     o              n  m  i  d
    //                          C  P     t        o     s     u                 e  o                 s
    //                          P  L     i              t     r                 n  n                 e
    //                          L        e              i     n                 t                    c

    // @formatter:on

    private void handleStgePayload(String value) {
        String binStr = String.format("%32s", new BigInteger(value, 16).toString(2)).replace(' ', '0');

        String relais = binStr.substring(31, 32);
        int coupure = Integer.parseInt(binStr.substring(28, 31), 2);
        String cache = binStr.substring(27, 28);
        String overVoltage = binStr.substring(25, 26);
        String exceedingPower = binStr.substring(24, 25);
        String function = binStr.substring(23, 24);
        String direction = binStr.substring(22, 23);
        int supplierRate = Integer.parseInt(binStr.substring(18, 22), 2);
        int distributorRate = Integer.parseInt(binStr.substring(16, 18), 2);
        String clock = binStr.substring(15, 16);
        String plc = binStr.substring(14, 15);
        int comOuput = Integer.parseInt(binStr.substring(11, 13), 2);
        int plcState = Integer.parseInt(binStr.substring(9, 11), 2);
        String plcSync = binStr.substring(8, 9);
        int tempoToday = Integer.parseInt(binStr.substring(6, 8), 2);
        int tempoTomorrow = Integer.parseInt(binStr.substring(4, 6), 2);
        int movingTipsAdvice = Integer.parseInt(binStr.substring(2, 4), 2);
        int movingTips = Integer.parseInt(binStr.substring(0, 2), 2);

        updateState(LinkyBindingConstants.LINKY_LOCAL_CALC_GROUP, LinkyBindingConstants.CHANNEL_CONTACT_SEC,
                getOpenClosed(relais));
        updateState(LinkyBindingConstants.LINKY_LOCAL_CALC_GROUP, LinkyBindingConstants.CHANNEL_CACHE,
                getOpenClosed(cache));

        updateState(LinkyBindingConstants.LINKY_LOCAL_CALC_GROUP, LinkyBindingConstants.CHANNEL_CUT_OFF,
                new DecimalType(coupure));

        updateState(LinkyBindingConstants.LINKY_LOCAL_CALC_GROUP, LinkyBindingConstants.CHANNEL_OVER_VOLTAGE,
                new DecimalType(overVoltage));
        updateState(LinkyBindingConstants.LINKY_LOCAL_CALC_GROUP, LinkyBindingConstants.CHANNEL_EXCEEDING_POWER,
                new DecimalType(exceedingPower));
        updateState(LinkyBindingConstants.LINKY_LOCAL_CALC_GROUP, LinkyBindingConstants.CHANNEL_FUNCTION,
                new DecimalType(function));
        updateState(LinkyBindingConstants.LINKY_LOCAL_CALC_GROUP, LinkyBindingConstants.CHANNEL_DIRECTION,
                new DecimalType(direction));

        updateState(LinkyBindingConstants.LINKY_LOCAL_CALC_GROUP, LinkyBindingConstants.CHANNEL_SUPPLIER_RATE,
                new DecimalType(supplierRate));
        updateState(LinkyBindingConstants.LINKY_LOCAL_CALC_GROUP, LinkyBindingConstants.CHANNEL_DISTRIBUTOR_RATE,
                new DecimalType(distributorRate));

        updateState(LinkyBindingConstants.LINKY_LOCAL_CALC_GROUP, LinkyBindingConstants.CHANNEL_CLOCK,
                new DecimalType(clock));
        updateState(LinkyBindingConstants.LINKY_LOCAL_CALC_GROUP, LinkyBindingConstants.CHANNEL_PLC,
                new DecimalType(plc));
        updateState(LinkyBindingConstants.LINKY_LOCAL_CALC_GROUP, LinkyBindingConstants.CHANNEL_COM_OUTPUT,
                new DecimalType(comOuput));
        updateState(LinkyBindingConstants.LINKY_LOCAL_CALC_GROUP, LinkyBindingConstants.CHANNEL_PLC_STATE,
                new DecimalType(plcState));
        updateState(LinkyBindingConstants.LINKY_LOCAL_CALC_GROUP, LinkyBindingConstants.CHANNEL_PLC_SYNCHRO,
                new DecimalType(plcSync));

        updateState(LinkyBindingConstants.LINKY_LOCAL_CALC_GROUP, LinkyBindingConstants.CHANNEL_TEMPO_TODAY,
                new DecimalType(tempoToday));
        updateState(LinkyBindingConstants.LINKY_LOCAL_CALC_GROUP, LinkyBindingConstants.CHANNEL_TEMPO_TOMORROW,
                new DecimalType(tempoTomorrow));

        updateState(LinkyBindingConstants.LINKY_LOCAL_CALC_GROUP, LinkyBindingConstants.CHANNEL_MOVING_TIPS_ADVICE,
                new DecimalType(movingTipsAdvice));
        updateState(LinkyBindingConstants.LINKY_LOCAL_CALC_GROUP, LinkyBindingConstants.CHANNEL_MOVING_TIPS,
                new DecimalType(movingTips));
    }

    private OpenClosedType getOpenClosed(String val) {
        if ("0".equals(val)) {
            return OpenClosedType.CLOSED;
        } else if ("1".equals(val)) {
            return OpenClosedType.OPEN;
        }

        return OpenClosedType.CLOSED;
    }

    private void handleRelaisPayload(String value) {
        boolean[] relaisState = new boolean[8];
        int valuei = Integer.parseInt(value);
        for (int i = 0; i <= 7; i++) {
            relaisState[i] = (valuei & 1) == 1;
            valuei >>= 1;
            updateState(LinkyBindingConstants.LINKY_LOCAL_CALC_GROUP, LinkyBindingConstants.CHANNEL_RELAIS + i,
                    OnOffType.from(relaisState[i]));

        }
    }

    private void handlePayload(String channelName, String value) {
        if (value.isEmpty()) {
            return;
        }

        String[] parts = value.split(" ");
        int idx = 1;
        for (String part : parts) {
            if ("NONUTILE".equals(part)) {
                continue;
            }

            int h = Integer.parseInt(part.substring(0, 2), 16);
            int m = Integer.parseInt(part.substring(2, 4), 16);

            String p2 = part.substring(4, 8);
            String binStr = String.format("%16s", new BigInteger(p2, 16).toString(2)).replace(' ', '0');

            String index = binStr.substring(12, 16);
            String relais = binStr.substring(0, 2);

            int indexI = Integer.parseInt(index, 2);

            String tarif = "";

            if (indexI == 1) {
                tarif = "Bleue-HC";
            } else if (indexI == 2) {
                tarif = "Bleue-HP";
            } else if (indexI == 3) {
                tarif = "Blanc-HC";
            } else if (indexI == 4) {
                tarif = "Blanc-HP";
            } else if (indexI == 5) {
                tarif = "Red-HC";
            } else if (indexI == 6) {
                tarif = "Red-HP";
            }

            String relaisSt = "";
            if ("00".equals(relais)) {
                relaisSt = "Fermé";
            } else if ("01".equals(relais)) {
                relaisSt = "Ouvert";
            }

            if ("PJOURF_PLUS_1".equals(channelName)) {
                // PJourF
                updateState(LinkyBindingConstants.LINKY_LOCAL_CALC_GROUP,
                        LinkyBindingConstants.CHANNEL_PJOURF_IDX + idx + "-plus1",
                        new StringType(h + ":" + m + "<>" + tarif + "<>" + relaisSt));
            } else if ("PPOINTE".equals(channelName)) {
                // PJourF
                updateState(LinkyBindingConstants.LINKY_LOCAL_CALC_GROUP,
                        LinkyBindingConstants.CHANNEL_PPOINTE_IDX + idx,
                        new StringType(h + ":" + m + "<>" + tarif + "<>" + relaisSt));
            }

            idx++;
        }
    }

    protected @Nullable Instant getAsInstant(String timestamp) {
        LocalDateTime res;

        if (timestamp.isEmpty()) {
            return null;
        }

        if (timestamp.startsWith("H") || timestamp.startsWith(" ")) {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyMMdd[HH][mm][ss]");
            res = LocalDateTime.parse(timestamp.substring(1), df);
        } else {
            DateTimeFormatter df = new DateTimeFormatterBuilder().appendPattern("MMM ppd yyyy")
                    .toFormatter(Locale.ENGLISH);
            res = LocalDate.parse(timestamp, df).atStartOfDay();
        }

        return res.toInstant(ZoneOffset.of("+1"));
    }

    protected void updateState(String groupId, String channelID, State state) {
        super.updateState(groupId + "#" + channelID, state);
    }

    public @Nullable LinkyConfiguration getLinkyConfig() {
        return config;
    }

    public void saveConfiguration(Configuration config) {
        updateConfiguration(config);
    }
}
