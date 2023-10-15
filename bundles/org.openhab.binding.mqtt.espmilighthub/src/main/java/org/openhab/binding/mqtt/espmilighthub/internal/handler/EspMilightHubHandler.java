/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

package org.openhab.binding.mqtt.espmilighthub.internal.handler;

import static org.openhab.binding.mqtt.MqttBindingConstants.BINDING_ID;
import static org.openhab.binding.mqtt.espmilighthub.internal.EspMilightHubBindingConstants.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.espmilighthub.internal.ConfigOptions;
import org.openhab.binding.mqtt.espmilighthub.internal.Helper;
import org.openhab.binding.mqtt.handler.AbstractBrokerHandler;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttMessageSubscriber;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EspMilightHubHandler} is responsible for handling commands of the globes, which are then
 * sent to one of the bridges to be sent out by MQTT.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class EspMilightHubHandler extends BaseThingHandler implements MqttMessageSubscriber {
    // these are all constants used in color conversion calcuations.
    // strings are necessary to prevent floating point loss of precision
    private static final BigDecimal BIG_DECIMAL_THOUSAND = new BigDecimal(1000);
    private static final BigDecimal BIG_DECIMAL_MILLION = new BigDecimal(1000000);

    private static final BigDecimal[][] KANG_X_COEFFICIENTS = {
            { new BigDecimal("-3.0258469"), new BigDecimal("2.1070379"), new BigDecimal("0.2226347"),
                    new BigDecimal("0.24039") },
            { new BigDecimal("-0.2661239"), new BigDecimal("-0.234589"), new BigDecimal("0.8776956"),
                    new BigDecimal("0.179910") } };

    private static final BigDecimal[][] KANG_Y_COEFFICIENTS = {
            { new BigDecimal("3.0817580"), new BigDecimal("-5.8733867"), new BigDecimal("3.75112997"),
                    new BigDecimal("-0.37001483") },
            { new BigDecimal("-0.9549476"), new BigDecimal("-1.37418593"), new BigDecimal("2.09137015"),
                    new BigDecimal("-0.16748867") },
            { new BigDecimal("-1.1063814"), new BigDecimal("-1.34811020"), new BigDecimal("2.18555832"),
                    new BigDecimal("-0.20219683") } };

    private static final BigDecimal BIG_DECIMAL_03320 = new BigDecimal("0.3320");
    private static final BigDecimal BIG_DECIMAL_01858 = new BigDecimal("0.1858");
    private static final BigDecimal[] MCCAMY_COEFFICIENTS = { new BigDecimal(437), new BigDecimal(3601),
            new BigDecimal(6862), new BigDecimal(5517) };

    private static final BigDecimal BIG_DECIMAL_2 = new BigDecimal(2);
    private static final BigDecimal BIG_DECIMAL_3 = new BigDecimal(3);
    private static final BigDecimal BIG_DECIMAL_4 = new BigDecimal(4);
    private static final BigDecimal BIG_DECIMAL_6 = new BigDecimal(6);
    private static final BigDecimal BIG_DECIMAL_12 = new BigDecimal(12);

    private static final BigDecimal BIG_DECIMAL_0292 = new BigDecimal("0.292");
    private static final BigDecimal BIG_DECIMAL_024 = new BigDecimal("0.24");

    private static final BigDecimal[] CORM_COEFFICIENTS = { new BigDecimal("-0.00616793"), new BigDecimal("0.0893944"),
            new BigDecimal("-0.5179722"), new BigDecimal("1.5317403"), new BigDecimal("-2.4243787"),
            new BigDecimal("1.925865"), new BigDecimal("-0.471106") };

    private static final BigDecimal BIG_DECIMAL_153 = new BigDecimal(153);
    private static final BigDecimal BIG_DECIMAL_217 = new BigDecimal(217);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private @Nullable MqttBrokerConnection connection;
    private ThingRegistry thingRegistry;
    private String globeType = "";
    private String bulbMode = "";
    private String remotesGroupID = "";
    private String channelPrefix = "";
    private String fullCommandTopic = "";
    private String fullStatesTopic = "";
    private BigDecimal maxColourTemp = BigDecimal.ZERO;
    private BigDecimal minColourTemp = BigDecimal.ZERO;
    private BigDecimal savedLevel = BIG_DECIMAL_100;
    private ConfigOptions config = new ConfigOptions();

    public EspMilightHubHandler(Thing thing, ThingRegistry thingRegistry) {
        super(thing);
        this.thingRegistry = thingRegistry;
    }

    void changeChannel(String channel, State state) {
        updateState(new ChannelUID(channelPrefix + channel), state);
        // Remote code of 0 means that all channels need to follow this change.
        if ("0".equals(remotesGroupID)) {
            switch (globeType) {
                // These two are 8 channel remotes
                case "fut091":
                case "fut089":
                    updateState(new ChannelUID(channelPrefix.substring(0, channelPrefix.length() - 2) + "5:" + channel),
                            state);
                    updateState(new ChannelUID(channelPrefix.substring(0, channelPrefix.length() - 2) + "6:" + channel),
                            state);
                    updateState(new ChannelUID(channelPrefix.substring(0, channelPrefix.length() - 2) + "7:" + channel),
                            state);
                    updateState(new ChannelUID(channelPrefix.substring(0, channelPrefix.length() - 2) + "8:" + channel),
                            state);
                default:
                    updateState(new ChannelUID(channelPrefix.substring(0, channelPrefix.length() - 2) + "1:" + channel),
                            state);
                    updateState(new ChannelUID(channelPrefix.substring(0, channelPrefix.length() - 2) + "2:" + channel),
                            state);
                    updateState(new ChannelUID(channelPrefix.substring(0, channelPrefix.length() - 2) + "3:" + channel),
                            state);
                    updateState(new ChannelUID(channelPrefix.substring(0, channelPrefix.length() - 2) + "4:" + channel),
                            state);
            }
        }
    }

    private void processIncomingState(String messageJSON) {
        // Need to handle State and Level at the same time to process level=0 as off//
        PercentType tempBulbLevel = PercentType.ZERO;
        String bulbState = Helper.resolveJSON(messageJSON, "\"state\":\"", 3);
        String bulbLevel = Helper.resolveJSON(messageJSON, "\"level\":", 3);
        if (!bulbLevel.isEmpty()) {
            if ("0".equals(bulbLevel) || "OFF".equals(bulbState)) {
                changeChannel(CHANNEL_LEVEL, OnOffType.OFF);
            } else {
                tempBulbLevel = new PercentType(Integer.valueOf(bulbLevel));
                changeChannel(CHANNEL_LEVEL, tempBulbLevel);
            }
        } else if ("ON".equals(bulbState) || "OFF".equals(bulbState)) { // NOTE: Level is missing when this runs
            changeChannel(CHANNEL_LEVEL, OnOffType.valueOf(bulbState));
        }
        bulbMode = Helper.resolveJSON(messageJSON, "\"bulb_mode\":\"", 5);
        switch (bulbMode) {
            case "white":
                if (hasRGB()) {
                    changeChannel(CHANNEL_BULB_MODE, new StringType("white"));
                    changeChannel(CHANNEL_DISCO_MODE, new StringType("None"));
                }
                String bulbCTempS = Helper.resolveJSON(messageJSON, "\"color_temp\":", 3);
                if (!bulbCTempS.isEmpty()) {
                    var bulbCTemp = Integer.valueOf(bulbCTempS);
                    changeChannel(CHANNEL_COLOURTEMP, scaleMireds(bulbCTemp));
                    if (hasRGB()) {
                        changeChannel(CHANNEL_COLOUR, calculateHSBFromColorTemp(bulbCTemp, tempBulbLevel));
                    }
                }
                break;
            case "color":
                changeChannel(CHANNEL_BULB_MODE, new StringType("color"));
                changeChannel(CHANNEL_DISCO_MODE, new StringType("None"));
                String bulbHue = Helper.resolveJSON(messageJSON, "\"hue\":", 3);
                String bulbSaturation = Helper.resolveJSON(messageJSON, "\"saturation\":", 3);
                if (bulbHue.isEmpty()) {
                    logger.warn("Milight MQTT message came in as being a colour mode, but was missing a HUE value.");
                } else {
                    if (bulbSaturation.isEmpty()) {
                        bulbSaturation = "100";
                    }
                    // 360 isn't allowed by OpenHAB
                    if ("360".equals(bulbHue)) {
                        bulbHue = "0";
                    }
                    var hsb = new HSBType(new DecimalType(Integer.valueOf(bulbHue)),
                            new PercentType(Integer.valueOf(bulbSaturation)), tempBulbLevel);
                    changeChannel(CHANNEL_COLOUR, hsb);
                    if (hasCCT()) {
                        changeChannel(CHANNEL_COLOURTEMP, scaleMireds(calculateColorTempFromHSB(hsb)));
                    }
                }
                break;
            case "scene":
                if (hasRGB()) {
                    changeChannel(CHANNEL_BULB_MODE, new StringType("scene"));
                }
                String bulbDiscoMode = Helper.resolveJSON(messageJSON, "\"mode\":", 1);
                if (!bulbDiscoMode.isEmpty()) {
                    changeChannel(CHANNEL_DISCO_MODE, new StringType(bulbDiscoMode));
                }
                break;
            case "night":
                if (hasRGB()) {
                    changeChannel(CHANNEL_BULB_MODE, new StringType("night"));
                    if (config.oneTriggersNightMode) {
                        changeChannel(CHANNEL_LEVEL, new PercentType("1"));
                    }
                }
                break;
        }
    }

    private boolean hasCCT() {
        switch (globeType) {
            case "rgb_cct":
            case "cct":
            case "fut089":
            case "fut091":
                return true;
            default:
                return false;
        }
    }

    private boolean hasRGB() {
        switch (globeType) {
            case "rgb_cct":
            case "rgb":
            case "rgbw":
            case "fut089":
                return true;
            default:
                return false;
        }
    }

    /**
     * Scales mireds to 0-100%
     */
    private static PercentType scaleMireds(int mireds) {
        // range in mireds is 153-370
        // 100 - (mireds - 153) / (370 - 153) * 100
        if (mireds >= 370) {
            return PercentType.HUNDRED;
        } else if (mireds <= 153) {
            return PercentType.ZERO;
        }
        return new PercentType(BIG_DECIMAL_100.subtract(new BigDecimal(mireds).subtract(BIG_DECIMAL_153)
                .divide(BIG_DECIMAL_217, MathContext.DECIMAL128).multiply(BIG_DECIMAL_100)));
    }

    private static BigDecimal polynomialFit(BigDecimal x, BigDecimal[] coefficients) {
        var result = BigDecimal.ZERO;
        var xAccumulator = BigDecimal.ONE;
        // forms K[4]*x^0 + K[3]*x^1 + K[2]*x^2 + K[1]*x^3 + K[0]*x^4
        // (or reverse the order of terms for the usual way of writing it in academic papers)
        for (int i = coefficients.length - 1; i >= 0; i--) {
            result = result.add(coefficients[i].multiply(xAccumulator));
            xAccumulator = xAccumulator.multiply(x);
        }
        return result;
    }

    // https://www.jkps.or.kr/journal/download_pdf.php?spage=865&volume=41&number=6 (8) and (9)
    private static HSBType calculateHSBFromColorTemp(int mireds, PercentType brightness) {
        var cct = BIG_DECIMAL_MILLION.divide(new BigDecimal(mireds), MathContext.DECIMAL128);
        var cctInt = cct.intValue();

        BigDecimal[] coefficients;
        // 1667K to 4000K and 4000K to 25000K; no range checks since our mired range fits within this
        if (cctInt <= 4000) {
            coefficients = KANG_X_COEFFICIENTS[1];
        } else {
            coefficients = KANG_X_COEFFICIENTS[0];
        }
        BigDecimal x = polynomialFit(BIG_DECIMAL_THOUSAND.divide(cct, MathContext.DECIMAL128), coefficients);

        if (cctInt <= 2222) {
            coefficients = KANG_Y_COEFFICIENTS[2];
        } else if (cctInt <= 4000) {
            coefficients = KANG_Y_COEFFICIENTS[1];
        } else {
            coefficients = KANG_Y_COEFFICIENTS[0];
        }
        BigDecimal y = polynomialFit(x, coefficients);
        var rawHsb = HSBType.fromXY(x.floatValue() * 100.0f, y.floatValue() * 100.0f);
        return new HSBType(rawHsb.getHue(), rawHsb.getSaturation(), brightness);
    }

    // https://www.waveformlighting.com/tech/calculate-color-temperature-cct-from-cie-1931-xy-coordinates/
    private static int calculateColorTempFromHSB(HSBType hsb) {
        PercentType[] xy = hsb.toXY();
        var x = xy[0].toBigDecimal().divide(BIG_DECIMAL_100);
        var y = xy[1].toBigDecimal().divide(BIG_DECIMAL_100);
        var n = x.subtract(BIG_DECIMAL_03320).divide(BIG_DECIMAL_01858.subtract(y), MathContext.DECIMAL128);
        BigDecimal cctK = polynomialFit(n, MCCAMY_COEFFICIENTS);
        return BIG_DECIMAL_MILLION.divide(cctK, MathContext.DECIMAL128).round(new MathContext(0)).intValue();
    }

    // https://cormusa.org/wp-content/uploads/2018/04/CORM_2011_Calculation_of_CCT_and_Duv_and_Practical_Conversion_Formulae.pdf
    // page 19
    private static BigDecimal calculateDuvFromHSB(HSBType hsb) {
        PercentType[] xy = hsb.toXY();
        var x = xy[0].toBigDecimal().divide(BIG_DECIMAL_100);
        var y = xy[1].toBigDecimal().divide(BIG_DECIMAL_100);
        var u = BIG_DECIMAL_4.multiply(x).divide(
                BIG_DECIMAL_2.multiply(x).negate().add(BIG_DECIMAL_12.multiply(y).add(BIG_DECIMAL_3)),
                MathContext.DECIMAL128);
        var v = BIG_DECIMAL_6.multiply(y).divide(
                BIG_DECIMAL_2.multiply(x).negate().add(BIG_DECIMAL_12.multiply(y).add(BIG_DECIMAL_3)),
                MathContext.DECIMAL128);
        var Lfp = u.subtract(BIG_DECIMAL_0292).pow(2).add(v.subtract(BIG_DECIMAL_024).pow(2))
                .sqrt(MathContext.DECIMAL128);
        var a = new BigDecimal(
                Math.acos(u.subtract(BIG_DECIMAL_0292).divide(Lfp, MathContext.DECIMAL128).doubleValue()));
        BigDecimal Lbb = polynomialFit(a, CORM_COEFFICIENTS);
        return Lfp.subtract(Lbb);
    }

    /*
     * Used to calculate the colour temp for a globe if you want the light to get warmer as it is dimmed like a
     * traditional halogen globe
     */
    private int autoColourTemp(int brightness) {
        return minColourTemp.subtract(
                minColourTemp.subtract(maxColourTemp).divide(BIG_DECIMAL_100).multiply(new BigDecimal(brightness)))
                .intValue();
    }

    void turnOff() {
        if (config.powerFailsToMinimum) {
            sendMQTT("{\"state\":\"OFF\",\"level\":0}");
        } else {
            sendMQTT("{\"state\":\"OFF\"}");
        }
    }

    void handleLevelColour(Command command) {
        int mireds;

        if (command instanceof OnOffType) {
            if (OnOffType.ON.equals(command)) {
                sendMQTT("{\"state\":\"ON\",\"level\":" + savedLevel + "}");
                return;
            } else {
                turnOff();
            }
        } else if (command instanceof IncreaseDecreaseType) {
            if (IncreaseDecreaseType.INCREASE.equals(command)) {
                if (savedLevel.intValue() <= 90) {
                    savedLevel = savedLevel.add(BigDecimal.TEN);
                }
            } else {
                if (savedLevel.intValue() >= 10) {
                    savedLevel = savedLevel.subtract(BigDecimal.TEN);
                }
            }
            sendMQTT("{\"state\":\"ON\",\"level\":" + savedLevel.intValue() + "}");
            return;
        } else if (command instanceof HSBType hsb) {
            // This feature allows google home or Echo to trigger white mode when asked to turn color to white.
            if (hsb.getHue().intValue() == config.whiteHue && hsb.getSaturation().intValue() == config.whiteSat) {
                if (hasCCT()) {
                    sendMQTT("{\"state\":\"ON\",\"color_temp\":" + config.favouriteWhite + "}");
                } else {// globe must only have 1 type of white
                    sendMQTT("{\"command\":\"set_white\"}");
                }
                return;
            } else if (PercentType.ZERO.equals(hsb.getBrightness())) {
                turnOff();
                return;
            } else if (config.duvThreshold.compareTo(BigDecimal.ONE) < 0
                    && calculateDuvFromHSB(hsb).abs().compareTo(config.duvThreshold) <= 0
                    && (mireds = calculateColorTempFromHSB(hsb)) >= 153 && mireds <= 370) {
                sendMQTT("{\"state\":\"ON\",\"level\":" + hsb.getBrightness() + ",\"color_temp\":" + mireds + "}");
            } else if (config.whiteThreshold != -1 && hsb.getSaturation().intValue() <= config.whiteThreshold) {
                sendMQTT("{\"command\":\"set_white\"}");// Can't send the command and level in the same message.
                sendMQTT("{\"level\":" + hsb.getBrightness().intValue() + "}");
            } else {
                sendMQTT("{\"state\":\"ON\",\"level\":" + hsb.getBrightness().intValue() + ",\"hue\":"
                        + hsb.getHue().intValue() + ",\"saturation\":" + hsb.getSaturation().intValue() + "}");
            }
            savedLevel = hsb.getBrightness().toBigDecimal();
            return;
        } else if (command instanceof PercentType percentType) {
            if (percentType.intValue() == 0) {
                turnOff();
                return;
            } else if (percentType.intValue() == 1 && config.oneTriggersNightMode) {
                sendMQTT("{\"command\":\"night_mode\"}");
                return;
            }
            sendMQTT("{\"state\":\"ON\",\"level\":" + command + "}");
            savedLevel = percentType.toBigDecimal();
            if (hasCCT()) {
                if (config.dimmedCT > 0 && "white".equals(bulbMode)) {
                    sendMQTT("{\"state\":\"ON\",\"color_temp\":" + autoColourTemp(savedLevel.intValue()) + "}");
                }
            }
            return;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            return;
        }
        switch (channelUID.getId()) {
            case CHANNEL_COLOUR:
            case CHANNEL_LEVEL:
                handleLevelColour(command);
                break;
            case CHANNEL_BULB_MODE:
                bulbMode = command.toString();
                break;
            case CHANNEL_COLOURTEMP:
                int scaledCommand = (int) Math.round((370 - (2.17 * Float.valueOf(command.toString()))));
                sendMQTT("{\"state\":\"ON\",\"level\":" + savedLevel + ",\"color_temp\":" + scaledCommand + "}");
                break;
            case CHANNEL_COMMAND:
                sendMQTT("{\"command\":\"" + command + "\"}");
                break;
            case CHANNEL_DISCO_MODE:
                sendMQTT("{\"mode\":\"" + command + "\"}");
                break;
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(ConfigOptions.class);
        if (config.dimmedCT > 0) {
            maxColourTemp = new BigDecimal(config.favouriteWhite);
            minColourTemp = new BigDecimal(config.dimmedCT);
            if (minColourTemp.intValue() <= maxColourTemp.intValue()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "The dimmedCT config value must be greater than the favourite White value.");
                return;
            }
        }

        globeType = thing.getThingTypeUID().getId();// eg rgb_cct
        String globeLocation = this.getThing().getUID().getId();// eg 0x014
        remotesGroupID = globeLocation.substring(globeLocation.length() - 1, globeLocation.length());// eg 4
        String remotesIDCode = globeLocation.substring(0, globeLocation.length() - 1);// eg 0x01
        fullCommandTopic = COMMANDS_BASE_TOPIC + remotesIDCode + "/" + globeType + "/" + remotesGroupID;
        fullStatesTopic = STATES_BASE_TOPIC + remotesIDCode + "/" + globeType + "/" + remotesGroupID;
        // Need to remove the lowercase x from 0x12AB in case it contains all numbers
        String caseCheck = globeLocation.substring(2, globeLocation.length() - 1);
        if (!caseCheck.equals(caseCheck.toUpperCase())) {
            logger.warn("The milight globe {}{} is using lowercase for the remote code when the hub needs UPPERCASE",
                    remotesIDCode, remotesGroupID);
        }
        channelPrefix = BINDING_ID + ":" + globeType + ":" + thing.getBridgeUID().getId() + ":" + remotesIDCode
                + remotesGroupID + ":";
        bridgeStatusChanged(getBridgeStatus());
    }

    private void sendMQTT(String payload) {
        MqttBrokerConnection localConnection = connection;
        if (localConnection != null) {
            localConnection.publish(fullCommandTopic, payload.getBytes(), 1, false);
        }
    }

    @Override
    public void processMessage(String topic, byte[] payload) {
        String state = new String(payload, StandardCharsets.UTF_8);
        logger.trace("Received the following new Milight state:{}:{}", topic, state);

        if (topic.equals(STATUS_TOPIC)) {
            if (state.equals(CONNECTED)) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Waiting for 'milight/status: connected' MQTT message to be sent from your ESP Milight hub.");
            }
        } else {
            try {
                processIncomingState(state);
            } catch (Exception e) {
                logger.warn("Failed processing Milight state {} for {}", state, topic, e);
            }
        }
    }

    public ThingStatusInfo getBridgeStatus() {
        Bridge b = getBridge();
        if (b != null) {
            return b.getStatusInfo();
        } else {
            return new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, null);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            connection = null;
            return;
        }
        if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            return;
        }

        Bridge localBridge = this.getBridge();
        if (localBridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Bridge is missing or offline, you need to setup a working MQTT broker first.");
            return;
        }
        ThingHandler handler = localBridge.getHandler();
        if (handler instanceof AbstractBrokerHandler abh) {
            final MqttBrokerConnection connection;
            try {
                connection = abh.getConnectionAsync().get(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException ignored) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                        "Bridge handler has no valid broker connection!");
                return;
            }
            this.connection = connection;
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING,
                    "Waiting for 'milight/status: connected' MQTT message to be received. Check hub has 'MQTT Client Status Topic' configured.");
            connection.subscribe(fullStatesTopic, this);
            connection.subscribe(STATUS_TOPIC, this);
        }
        return;
    }

    @Override
    public void dispose() {
        MqttBrokerConnection localConnection = connection;
        if (localConnection != null) {
            localConnection.unsubscribe(fullStatesTopic, this);
            localConnection.unsubscribe(STATUS_TOPIC, this);
        }
    }
}
