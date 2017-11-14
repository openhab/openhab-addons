/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solaredge.internal.command;

import static org.openhab.binding.solaredge.SolarEdgeBindingConstants.*;

import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.solaredge.handler.SolarEdgeHandler;
import org.openhab.binding.solaredge.internal.callback.AbstractCommandCallback;
import org.openhab.binding.solaredge.internal.model.LiveDataChannels;

/**
 * command that retrieves values for live data channels - legacy version for old setups
 *
 * @author Alexander Friese - initial contribution
 *
 */
public class LegacyLiveDataUpdate extends AbstractCommandCallback implements SolarEdgeCommand {
    private static final String NCG_NON_GREEDY_TEXT_OR_NUMBER = "(?:.+?)";
    private static final String CG_CURRENT_POWER_AND_UNIT = "(?:\\{currentPower:\"([0-9]+)(.+)\"\\})";
    private static final String UNIT_W = "W";

    private final SolarEdgeHandler handler;
    private int retries = 0;

    public LegacyLiveDataUpdate(SolarEdgeHandler handler) {
        super(handler.getConfiguration());
        this.handler = handler;
    }

    @Override
    protected Request prepareRequest(Request requestToPrepare) {
        requestToPrepare.followRedirects(false);
        requestToPrepare.param(DATA_API_LEGACY_LIVE_DATA_FIELDID_FIELD, config.getSolarId());
        requestToPrepare.method(HttpMethod.GET);

        return requestToPrepare;
    }

    @Override
    protected String getURL() {
        return DATA_API_LEGACY_LIVE_DATA_URL;
    }

    @Override
    public void onComplete(Result result) {
        logger.debug("onComplete()");

        if (!HttpStatus.Code.OK.equals(getCommunicationStatus().getHttpCode()) && retries++ < MAX_RETRIES) {
            if (getListener() != null) {
                getListener().update(getCommunicationStatus());
            }
            handler.getWebInterface().executeCommand(this);

        } else {

            String html = getContentAsString(StandardCharsets.UTF_8);
            if (html != null) {
                handler.updateChannelStatus(parseLegacyData(html));
            }
        }
    }

    /**
     * uses regexp matcher to extract current production data
     *
     * @param data - raw data retrieved from solaredge
     * @return map containing the live data (only one element)
     */
    private Map<String, String> parseLegacyData(String data) {
        Map<String, String> resultMap = new HashMap<>(1);

        data = data.replaceAll("\\s+", "");

        logger.debug("RAW String: {}", data);

        Pattern pattern = compile(NCG_NON_GREEDY_TEXT_OR_NUMBER, CG_CURRENT_POWER_AND_UNIT,
                NCG_NON_GREEDY_TEXT_OR_NUMBER);

        Matcher matcher = pattern.matcher(data);

        if (matcher.matches()) {
            String value = matcher.group(1);
            String unit = matcher.group(2);
            resultMap.put(LiveDataChannels.PRODUCTION.getFQName(), getValueAsKW(value, unit));
        }

        return resultMap;

    }

    /**
     *
     * @param groups
     * @return compiled pattern
     */
    private final Pattern compile(String... groups) {
        StringBuilder sb = new StringBuilder();
        for (String group : groups) {
            sb.append(group);
        }
        return Pattern.compile(sb.toString());
    }

    /**
     * converts the value to KW
     *
     * @param value value retrievd from Solaredge
     * @param unit retrieved from solaredge
     * @return converted value
     */
    private String getValueAsKW(String value, String unit) {
        Double convertedValue = Double.valueOf(value);

        if (unit != null && unit.equals(UNIT_W)) {
            convertedValue = convertedValue / 1000;
        }

        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(convertedValue);
    }

}
