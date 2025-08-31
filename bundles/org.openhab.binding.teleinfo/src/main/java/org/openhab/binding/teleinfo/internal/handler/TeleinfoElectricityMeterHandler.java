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
package org.openhab.binding.teleinfo.internal.handler;

import static org.openhab.binding.teleinfo.internal.TeleinfoBindingConstants.*;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.teleinfo.internal.TeleinfoBindingConstants;
import org.openhab.binding.teleinfo.internal.data.Frame;
import org.openhab.binding.teleinfo.internal.data.Phase;
import org.openhab.binding.teleinfo.internal.data.Pricing;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.FrameUtil;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.InvalidFrameException;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.Label;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.ValueType;
import org.openhab.binding.teleinfo.internal.serial.TeleinfoTicMode;
import org.openhab.binding.teleinfo.internal.types.Manufacturer;
import org.openhab.binding.teleinfo.internal.types.Meter;
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
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TeleinfoElectricityMeterHandler} class defines a skeleton for Electricity Meters handlers.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
@NonNullByDefault
public class TeleinfoElectricityMeterHandler extends BaseThingHandler implements TeleinfoControllerHandlerListener {

    private final Logger logger = LoggerFactory.getLogger(TeleinfoElectricityMeterHandler.class);
    protected TeleinfoElectricityMeterConfiguration configuration = new TeleinfoElectricityMeterConfiguration();
    private boolean wasLastFrameShort = false;

    private String appKey = "";
    private String ivKey = "";
    private long idd2l = -1;
    private String adco = "";
    private double cosphi = Double.NaN;

    public TeleinfoElectricityMeterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, ERROR_OFFLINE_CONTROLLER_OFFLINE);

        Bridge bridge = getBridge();
        logger.debug("bridge = {}", bridge);
        if (bridge != null) {
            bridgeStatusChanged(bridge.getStatusInfo());
        }

        configuration = getConfigAs(TeleinfoElectricityMeterConfiguration.class);

        adco = configuration.getAdco();
        logger.debug("Initializing Linky handler for {}", adco);

        appKey = configuration.getAppKey();
        ivKey = configuration.getIvKey();
        String idd2lSt = configuration.getIdd2l();
        if (!idd2lSt.isBlank()) {
            idd2l = Long.parseLong(idd2lSt);
        } else {
            idd2l = -1;
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        TeleinfoAbstractControllerHandler controllerHandler = getControllerHandler();
        if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            if (controllerHandler != null) {
                controllerHandler.removeListener(this);
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, ERROR_OFFLINE_CONTROLLER_OFFLINE);
            return;
        }

        if (controllerHandler != null) {
            controllerHandler.addListener(this);
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void dispose() {
        TeleinfoAbstractControllerHandler controllerHandler = getControllerHandler();
        if (controllerHandler != null) {
            controllerHandler.removeListener(this);
        }
        super.dispose();
    }

    private @Nullable TeleinfoAbstractControllerHandler getControllerHandler() {
        Bridge bridge = getBridge();
        return bridge != null ? (TeleinfoAbstractControllerHandler) bridge.getHandler() : null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!(command instanceof RefreshType)) {
            if (channelUID.getId().indexOf("cosphi") >= 0) {
                if (command instanceof DecimalType dc) {
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

        if (!(ThingStatus.ONLINE.equals(status))) {
            for (Channel channel : getThing().getChannels()) {
                if (!CHANNEL_LAST_UPDATE.equals(channel.getUID().getId())) {
                    updateState(channel.getUID(), UnDefType.UNDEF);
                }
            }
        }
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail) {
        this.updateStatus(status, statusDetail, null);
    }

    @Override
    protected void updateStatus(ThingStatus status) {
        this.updateStatus(status, ThingStatusDetail.NONE, null);
    }

    @Override
    public void onFrameReceived(Frame frame) {
        String adco = configuration.getAdco();
        if (adco.equalsIgnoreCase(frame.get(Label.ADCO)) || adco.equalsIgnoreCase(frame.get(Label.ADSC))) {
            updateStatesForChannels(frame);
        }
    }

    private void updateStatesForChannels(Frame frame) {
        double urms = 0.0;
        double sinst = 0.0;

        Map<String, String> props = this.editProperties();

        for (Entry<Label, String> entry : frame.getLabelToValues().entrySet()) {
            Label label = entry.getKey();
            try {
                if (!label.getChannelName().equals(NOT_A_CHANNEL)) {
                    logger.trace("Update channel {} to value {}", label.getChannelName(), entry.getValue());
                    if (label == Label.PTEC) {
                        updateState(label.getChannelName(), StringType.valueOf(entry.getValue().replace(".", "")));
                    } else if (label.getType() == ValueType.STRING) {
                        updateState(label.getChannelName(), StringType.valueOf(entry.getValue()));
                    } else if (label.getType() == ValueType.DATE) {
                        String value = entry.getValue();
                        if (value.isBlank()) {
                            value = frame.getLabelToTimestamp().get(label);
                        }

                        if (value != null && !value.isEmpty()) {
                            Instant timestampConv = getAsInstant(value);

                            if (timestampConv != null) {
                                logger.trace("Update channel {} to value {}", label.getChannelName(), value);

                                updateState(label.getChannelName(), new DateTimeType(timestampConv));
                            }
                        }
                    } else if (label.getType() == ValueType.INTEGER) {
                        if (!entry.getValue().isBlank()) {
                            updateState(label.getChannelName(), QuantityType
                                    .valueOf(label.getFactor() * Integer.parseInt(entry.getValue()), label.getUnit()));
                        }
                    }

                    // handle special case channel that will need additionnal decoding
                    if (label == Label.RELAIS) {
                        handleRelaisPayload(entry.getValue());
                    } else if (label == Label.PJOURF_PLUS_1) {
                        handlePayload(Label.PJOURF_PLUS_1.name(), entry.getValue());
                    } else if (label == Label.PPOINTE) {
                        handlePayload(Label.PPOINTE.name(), entry.getValue());
                    } else if (label == Label.STGE) {
                        handleStgePayload(entry.getValue());
                    } else if (label == Label.URMS1) {
                        urms = Double.valueOf(entry.getValue());
                    } else if (label == Label.SINSTS) {
                        sinst = Double.valueOf(entry.getValue());
                    }
                } else {
                    // handle some channel that we want to have has properties for reference
                    if (label == Label._TYPE_TRAME) {
                        props.put(THING_ELECTRICITY_METER_PROPERTY_TYPETRAME, entry.getValue());
                    } else if (label == Label._DATE_FIRMWARE) {
                        props.put(THING_ELECTRICITY_METER_PROPERTY_DATE_FIRMWARE, entry.getValue());
                    } else if (label == Label.VTIC) {
                        props.put(THING_ELECTRICITY_METER_PROPERTY_VTIC, entry.getValue());
                    } else if (label == Label.ADSC || label == Label.ADCO) {
                        String secondaryAddress = frame.get(Label.ADCO) != null ? frame.get(Label.ADCO)
                                : frame.get(Label.ADSC);

                        if (secondaryAddress != null && secondaryAddress.length() == 12) {
                            String oldMatricule = getThing().getProperties()
                                    .get(THING_ELECTRICITY_METER_PROPERTY_MATRICULE);

                            if (oldMatricule == null || oldMatricule.isBlank()) {
                                String manufacturerSt = secondaryAddress.substring(0, 2);
                                String year = secondaryAddress.substring(2, 4);
                                String type = secondaryAddress.substring(4, 6);
                                String matricule = secondaryAddress.substring(6, 12);

                                Meter meter = Meter.getCompteurForId(Integer.parseInt(type));
                                Manufacturer manufacturer = Manufacturer
                                        .getManufacturerForId(Integer.parseInt(manufacturerSt));

                                props.put(THING_ELECTRICITY_METER_PROPERTY_IDD2L, "" + idd2l);

                                if (manufacturer != null) {
                                    props.put(THING_ELECTRICITY_METER_PROPERTY_MANUFACTURER, manufacturer.getLabel());
                                }
                                if (meter != null) {
                                    props.put(THING_ELECTRICITY_METER_PROPERTY_TYPE, meter.getLabel());
                                    props.put(THING_ELECTRICITY_METER_PROPERTY_CATEGORY,
                                            meter.getCompteurType().getLabel());
                                }
                                props.put(THING_ELECTRICITY_METER_PROPERTY_MATRICULE, matricule);
                                props.put(THING_ELECTRICITY_METER_PROPERTY_MANUFACTURE_YEAR,
                                        "" + ((Integer.parseInt(year) + 2000)));
                            }
                        }
                    }
                }
                if (!label.getTimestampChannelName().equals(NOT_A_CHANNEL)) {
                    String timestamp = frame.getAsDateTime(label);
                    if (!timestamp.isEmpty()) {
                        logger.trace("Update channel {} to value {}", label.getTimestampChannelName(), timestamp);
                        updateState(label.getTimestampChannelName(), DateTimeType.valueOf(timestamp));
                    }
                }
            } catch (Exception ex) {
                logger.warn("Can't update channel {}", label.getChannelName());
            }
        }
        try {
            if (frame.getTicMode() == TeleinfoTicMode.HISTORICAL) {
                try {
                    if (frame.getPricing() == Pricing.TEMPO) {
                        updateState(CHANNEL_TEMPO_FRAME_PROGRAMME_CIRCUIT_1,
                                StringType.valueOf(frame.getProgrammeCircuit1()));
                        updateState(CHANNEL_TEMPO_FRAME_PROGRAMME_CIRCUIT_2,
                                StringType.valueOf(frame.getProgrammeCircuit2()));
                    }
                } catch (InvalidFrameException e) {
                    logger.warn("Can not find pricing option.");
                }

                try {
                    Phase phase = frame.getPhase();
                    if (phase == Phase.ONE_PHASED) {
                        updateStateForMissingAlert(frame, Label.ADPS);
                    } else if (phase == Phase.THREE_PHASED) {
                        if (!wasLastFrameShort) {
                            updateStateForMissingAlert(frame, Label.ADIR1);
                            updateStateForMissingAlert(frame, Label.ADIR2);
                            updateStateForMissingAlert(frame, Label.ADIR3);
                        }
                        wasLastFrameShort = frame.isShortFrame();
                    }
                } catch (InvalidFrameException e) {
                    logger.warn("Can not find phase.");
                }
            } else {
                if (frame.getLabelToValues().containsKey(Label.RELAIS)) {
                    String relaisString = frame.get(Label.RELAIS);
                    if (relaisString != null) {
                        boolean[] relaisStates = FrameUtil.parseRelaisStates(relaisString);
                        for (int i = 0; i <= 7; i++) {
                            updateState(CHANNELS_LSM_RELAIS[i], OnOffType.from(relaisStates[i]));
                        }
                    }
                }
            }
        } catch (InvalidFrameException e) {
            logger.warn("Can not find TIC mode.");
        }

        this.updateProperties(props);
        updateCalcVars(urms, sinst);
        updateState(CHANNEL_LAST_UPDATE, new DateTimeType());
    }

    private void updateCalcVars(double urms, double sinst) {
        if (urms == 0) {
            logger.warn("Invalid input: urms is zero, cannot perform calculations.");
            return;
        }

        if (urms < 0 || sinst < 0) {
            logger.warn("Invalid input: urms and sinst must be positive. Received urms={}, sinst={}", urms, sinst);
            return;
        }

        double irms1c = sinst / urms;

        Label channelIrms1f = Label.IRMS1F;
        Label channelSactive = Label.SACTIVE;
        Label channelSreactive = Label.SREACTIVE;

        updateState(channelIrms1f.getChannelName(),
                QuantityType.valueOf(channelIrms1f.getFactor() * irms1c, channelIrms1f.getUnit()));

        if (Double.isNaN(cosphi)) {
            return;
        }

        double phi = Math.acos(cosphi);
        double sinphi = Math.sin(phi);

        double sactive = sinst * cosphi;
        double sreactive = sinst * sinphi;

        updateState(channelSactive.getChannelName(),
                QuantityType.valueOf(channelSactive.getFactor() * sactive, channelSactive.getUnit()));

        updateState(channelSreactive.getChannelName(),
                QuantityType.valueOf(channelSreactive.getFactor() * sreactive, channelSreactive.getUnit()));
    }

    private void updateStateForMissingAlert(Frame frame, Label label) {
        if (!frame.getLabelToValues().containsKey(label)) {
            updateState(label.getChannelName(), UnDefType.NULL);
        }
    }

    public long getIdd2l() {
        return this.idd2l;
    }

    public String getAdco() {
        return this.adco;
    }

    public String getAppKey() {
        return this.appKey;
    }

    public String getIvKey() {
        return this.ivKey;
    }

    protected @Nullable Instant getAsInstant(String timestamp) {
        LocalDateTime res;

        if (timestamp.isEmpty()) {
            return null;
        }

        if (timestamp.startsWith("H") || timestamp.startsWith("E") || timestamp.startsWith(" ")) {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyMMdd[HH][mm][ss]");
            res = LocalDateTime.parse(timestamp.substring(1), df);

            // Handle summer time
            if (timestamp.startsWith("E")) {
                res = res.minusHours(1);
            }
        } else {
            DateTimeFormatter df = new DateTimeFormatterBuilder().appendPattern("MMM ppd yyyy")
                    .toFormatter(Locale.ENGLISH);
            res = LocalDate.parse(timestamp, df).atStartOfDay();
        }

        return res.toInstant(ZoneOffset.of("+1"));
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

        updateState(TeleinfoBindingConstants.CHANNEL_CONTACT_SEC, getOpenClosed(relais));
        updateState(TeleinfoBindingConstants.CHANNEL_CACHE, getOpenClosed(cache));

        updateState(TeleinfoBindingConstants.CHANNEL_CUT_OFF, new DecimalType(coupure));

        updateState(TeleinfoBindingConstants.CHANNEL_OVER_VOLTAGE, new DecimalType(overVoltage));
        updateState(TeleinfoBindingConstants.CHANNEL_EXCEEDING_POWER, new DecimalType(exceedingPower));
        updateState(TeleinfoBindingConstants.CHANNEL_FUNCTION, new DecimalType(function));
        updateState(TeleinfoBindingConstants.CHANNEL_DIRECTION, new DecimalType(direction));

        updateState(TeleinfoBindingConstants.CHANNEL_SUPPLIER_RATE, new DecimalType(supplierRate));
        updateState(TeleinfoBindingConstants.CHANNEL_DISTRIBUTOR_RATE, new DecimalType(distributorRate));

        updateState(TeleinfoBindingConstants.CHANNEL_CLOCK, new DecimalType(clock));
        updateState(TeleinfoBindingConstants.CHANNEL_PLC, new DecimalType(plc));
        updateState(TeleinfoBindingConstants.CHANNEL_COM_OUTPUT, new DecimalType(comOuput));
        updateState(TeleinfoBindingConstants.CHANNEL_PLC_STATE, new DecimalType(plcState));
        updateState(TeleinfoBindingConstants.CHANNEL_PLC_SYNCHRO, new DecimalType(plcSync));

        updateState(TeleinfoBindingConstants.CHANNEL_TEMPO_TODAY, new DecimalType(tempoToday));
        updateState(TeleinfoBindingConstants.CHANNEL_TEMPO_TOMORROW, new DecimalType(tempoTomorrow));

        updateState(TeleinfoBindingConstants.CHANNEL_MOVING_TIPS_ADVICE, new DecimalType(movingTipsAdvice));
        updateState(TeleinfoBindingConstants.CHANNEL_MOVING_TIPS, new DecimalType(movingTips));
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
            updateState(TeleinfoBindingConstants.CHANNEL_RELAIS + i, OnOffType.from(relaisState[i]));

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
                updateState(TeleinfoBindingConstants.CHANNEL_PJOURF_IDX + idx + "-plus1",
                        new StringType(h + ":" + m + "<>" + tarif + "<>" + relaisSt));
            } else if ("PPOINTE".equals(channelName)) {
                // PJourF
                updateState(TeleinfoBindingConstants.CHANNEL_PPOINTE_IDX + idx,
                        new StringType(h + ":" + m + "<>" + tarif + "<>" + relaisSt));
            }

            idx++;
        }
    }
}
