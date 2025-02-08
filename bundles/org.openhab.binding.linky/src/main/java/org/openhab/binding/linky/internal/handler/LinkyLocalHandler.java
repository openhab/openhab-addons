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

import static org.openhab.binding.linky.internal.LinkyBindingConstants.*;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.ZoneId;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.linky.internal.Label;
import org.openhab.binding.linky.internal.LinkyConfiguration;
import org.openhab.binding.linky.internal.ValueType;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link LinkyLocalHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Arnal - Rewrite addon to use official dataconect API
 */

@NonNullByDefault
@SuppressWarnings("null")
public class LinkyLocalHandler extends BaseThingHandler {
    private final TimeZoneProvider timeZoneProvider;
    private ZoneId zoneId = ZoneId.systemDefault();

    private final Logger logger = LoggerFactory.getLogger(LinkyLocalHandler.class);

    private LinkyConfiguration config;

    public String userId = "";

    private String appKey = "";
    private String ivKey = "";

    public LinkyLocalHandler(Thing thing, LocaleProvider localeProvider, TimeZoneProvider timeZoneProvider) {
        super(thing);

        config = getConfigAs(LinkyConfiguration.class);
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public synchronized void initialize() {
        logger.debug("Initializing Linky handler for {}", config.prmId);

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
        saveConfiguration(thingConfig);

        // reread config to update timezone field
        config = getConfigAs(LinkyConfiguration.class);

        Bridge bridge = getBridge();
        if (bridge == null) {
            return;
        }

        BridgeLinkyHandler bridgeHandler = (BridgeLinkyHandler) bridge.getHandler();
        if (bridgeHandler == null) {
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        bridgeHandler.registerNewPrmId(config.prmId);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public synchronized void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("Refreshing channel {} {}", config.prmId, channelUID.getId());
        } else {
            logger.debug("The Linky binding is read-only and can not handle command {}", command);
        }
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

    public void handleRead(ByteBuffer byteBuffer) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        // int version = byteBuffer.get(0);
        int length = byteBuffer.getShort(2);
        // long idd2l = byteBuffer.getLong(4);

        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

            byte[] bytesKey = new BigInteger("7F" + appKey, 16).toByteArray();
            SecretKeySpec key = new SecretKeySpec(bytesKey, 1, bytesKey.length - 1, "AES");

            byte[] bytesIv = new BigInteger(ivKey, 16).toByteArray();
            IvParameterSpec iv = new IvParameterSpec(bytesIv);

            // cipher.init(Cipher.DECRYPT_MODE, key, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);

            byte[] bufferToDecode = new byte[length];
            byteBuffer.get(16, bufferToDecode, 0, length - 16);
            byte[] plainText = cipher.doFinal(bufferToDecode);

            ByteBuffer byteBufferDecode = ByteBuffer.wrap(plainText);
            byteBufferDecode.order(ByteOrder.LITTLE_ENDIAN);
            // int crc16 = byteBufferDecode.getShort(16);
            int payloadLength = byteBufferDecode.getShort(18);
            int payloadType = byteBufferDecode.get(20) & 0x7f;
            // int requestType = byteBufferDecode.get(20) & 0x80;
            // int nextQuery = byteBufferDecode.get(21) & 0x7f;
            // int isErrorOrSuccess = byteBufferDecode.get(21) & 0x80;

            String st1 = new String(plainText, 22, payloadLength);

            Bridge bridge = getBridge();
            if (bridge == null) {
                return;
            }

            BridgeLinkyHandler bridgeHandler = (BridgeLinkyHandler) bridge.getHandler();
            if (bridgeHandler == null) {
                return;
            }
            Gson gson = bridgeHandler.getGson();

            if (payloadType == 3) {
                Type type = new TypeToken<Map<String, String>>() {
                }.getType();

                Map<String, String> r1 = gson.fromJson(st1, type);
                if (r1 != null) {
                    handlePayload(r1);
                }
            }

            System.out.print(st1);

        } catch (Exception ex) {

            System.out.print(ex.toString());
        }
    }

    protected void handlePayload(Map<String, String> payLoad) {

        for (String key : payLoad.keySet()) {
            String value = payLoad.get(key);

            try {
                Label label = Label.getEnum(key);

                if (label.getChannelName().equals(CHANNEL_NONE)) {
                    continue;
                }

                if (value != null) {
                    if (label.getType() == ValueType.STRING) {
                        updateState(LINKY_DIRECT_MAIN_GROUP, label.getChannelName(), StringType.valueOf(value));
                    } else if (label.getType() == ValueType.INTEGER) {
                        updateState(LINKY_DIRECT_MAIN_GROUP, label.getChannelName(),
                                QuantityType.valueOf(label.getFactor() * Integer.parseInt(value), label.getUnit()));
                    }
                }

                logger.info("");
            } catch (IllegalArgumentException ex) {
                logger.error("err", ex);
            }
        }

        // updateState(LINKY_DIRECT_MAIN_GROUP, "_ID_D2L", new StringType(payLoad.get("_ID_D2L")));
        // updateState(LINKY_DIRECT_MAIN_GROUP, "SINSTS", new StringType(payLoad.get("SINSTS")));
        // updateState(LINKY_DIRECT_MAIN_GROUP, "DATE", new StringType(payLoad.get("DATE")));
        // updateState(LINKY_DIRECT_MAIN_GROUP, "IRMS1", new StringType(payLoad.get("IRMS1")));
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
