/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hdanywhere.handler;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.hdanywhere.HDanywhereBindingConstants.Port;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link Mhub4K431Handler} is responsible for handling commands, which are
 * sent to one of the channels. It supports the MHUB 4K (4×3+1) matrix
 *
 * @author Karel Goderis - Initial contribution
 */
public class Mhub4K431Handler extends BaseThingHandler {

    // List of Configurations constants
    public static final String IP_ADDRESS = "ipAddress";
    // public static final String PORTS = "ports";
    public static final String POLLING_INTERVAL = "interval";

    private Logger logger = LoggerFactory.getLogger(Mhub4K431Handler.class);

    private ScheduledFuture<?> pollingJob;
    protected Gson gson = new Gson();

    private final int timeout = 5000;
    private final int numberOfPorts = 4;

    public Mhub4K431Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing HDanywhere MHUB 4K (4×3+1) matrix handler.");

        if (pollingJob == null || pollingJob.isCancelled()) {
            int polling_interval = ((BigDecimal) getConfig().get(POLLING_INTERVAL)).intValue();
            pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 1, polling_interval, TimeUnit.SECONDS);
        }
        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing HDanywhere matrix handler.");
        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    private Runnable pollingRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                String host = (String) getConfig().get(IP_ADDRESS);

                String httpMethod = "POST";
                String url = "http://" + host + "/cgi-bin/MUH44TP_getsetparams.cgi";
                String content = "{tag:ptn}";
                InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

                if (isNotBlank(httpMethod) && isNotBlank(url)) {
                    String response = HttpUtil.executeUrl(httpMethod, url, null, stream, null, timeout);
                    response = response.trim();
                    response = response.substring(1, response.length() - 1);

                    if (response != null) {
                        updateStatus(ThingStatus.ONLINE);

                        java.lang.reflect.Type type = new TypeToken<Map<String, String>>() {
                        }.getType();
                        Map<String, String> map = gson.fromJson(response, type);

                        String inputChannel = map.get("Inputchannel");

                        for (int i = 0; i < numberOfPorts; i++) {
                            DecimalType decimalType = new DecimalType(String.valueOf(inputChannel.charAt(i)));
                            updateState(new ChannelUID(getThing().getUID(), Port.get(i + 1).channelID()), decimalType);
                        }
                    } else {
                        updateStatus(ThingStatus.OFFLINE);
                    }
                }
            } catch (Exception e) {
                logger.debug("An exception occurred while polling the HDanwywhere matrix: '{}'", e.getMessage());
                updateStatus(ThingStatus.OFFLINE);
            }
        }
    };

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command instanceof RefreshType) {
            // Simply schedule a single run of the polling runnable to refresh all channels
            scheduler.schedule(pollingRunnable, 0, TimeUnit.SECONDS);
        } else {

            String channelID = channelUID.getId();

            String host = (String) getConfig().get(IP_ADDRESS);
            int sourcePort = Integer.valueOf(command.toString());
            int outputPort = Port.get(channelID).toNumber();

            if (sourcePort > numberOfPorts) {
                // nice try - we can switch to a port that does not physically exist
                logger.warn("Source port {} goes beyond the physical number of {} ports available on the matrix {}",
                        new Object[] { sourcePort, numberOfPorts, host });
            } else if (outputPort > numberOfPorts) {
                // nice try - we can switch to a port that does not physically exist
                logger.warn("Output port {} goes beyond the physical number of {} ports available on the matrix {}",
                        new Object[] { outputPort, numberOfPorts, host });
            } else {

                String httpMethod = "POST";
                String url = "http://" + host + "/cgi-bin/MMX32_Keyvalue.cgi";

                String content = "{CMD=";
                content = content + command.toString() + "B";
                content = content + String.valueOf(outputPort) + ".";

                InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

                Properties httpHeaders = new Properties();
                httpHeaders.setProperty("Cookie", "logintype-88=01");

                try {
                    String response = HttpUtil.executeUrl(httpMethod, url, httpHeaders, stream,
                            "application/x-www-form-urlencoded; charset=UTF-8", timeout);
                } catch (IOException e) {
                    logger.debug("Communication with device failed: {}", e);
                    updateStatus(ThingStatus.OFFLINE);
                }
            }
        }
    }

}
