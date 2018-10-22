/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.riscocloud.json;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link ServerDatasHandler} is responsible for handling
 * and interpreting json from server
 *
 * @author Sébastien Cantineau - Initial contribution
 */
public class ServerDatasHandler extends ServerDatasObject implements Cloneable {

    private final static Logger logger = LoggerFactory.getLogger(ServerDatasHandler.class);

    public ServerDatasHandler createServerDatasHandler(String json) {
        logger.debug("debug response complete: {}", json);
        Gson gson = new Gson();
        return gson.fromJson(json, ServerDatasHandler.class);
    }

    public boolean isValidObject() {
        return isValidOverview() && isValidDetectors();
    }

    private boolean isValidOverview() {
        return super.getOverview() != null;
    }

    private boolean isValidDetectors() {
        return super.getDetectors() != null;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public OnOffType getIsOnline() {
        // return !getIsOffline() ? OnOffType.ON : OnOffType.OFF;
        return getIsOffline() == null || getIsOffline() ? OnOffType.OFF : OnOffType.ON;
    }

    public OnOffType getIsOngoingAlarm() {
        return getOngoingAlarm() == null || !getOngoingAlarm() ? OnOffType.OFF : OnOffType.ON;
    }

    public DecimalType getArmedPartNb() {
        return DecimalType
                .valueOf(!isValidOverview() ? "0" : super.getOverview().getPartInfo().getArmedStr().split(" ")[0]);
    }

    public DecimalType getDisarmedPartNb() {
        return DecimalType
                .valueOf(!isValidOverview() ? "0" : super.getOverview().getPartInfo().getDisarmedStr().split(" ")[0]);
    }

    public DecimalType getPartiallyArmedPartNb() {
        return DecimalType
                .valueOf(!isValidOverview() ? "0" : super.getOverview().getPartInfo().getPartarmedStr().split(" ")[0]);
    }

    public HashMap<Integer, String> getPartList() {
        HashMap<Integer, String> partList = new HashMap<Integer, String>();
        if (isValidDetectors()) {
            super.getDetectors().getParts().forEach(part -> partList.put(part.getId(), part.getName()));
        }
        return partList;
    }

    public OnOffType getIsPartArmed(int idPart) {
        if (!isValidDetectors()) {
            return OnOffType.OFF;
        }
        return getPartIcon(idPart).equals("armed") ? OnOffType.ON : OnOffType.OFF;
    }

    public OnOffType getIsPartPartiallyArmed(int idPart) {
        if (!isValidDetectors()) {
            return OnOffType.OFF;
        }
        return getPartIcon(idPart).equals("partial") ? OnOffType.ON : OnOffType.OFF;
    }

    public OnOffType getIsPartDisarmed(int idPart) {
        if (!isValidDetectors()) {
            return OnOffType.ON;
        }
        return getPartIcon(idPart).equals("disarmed") ? OnOffType.ON : OnOffType.OFF;
    }

    private String getPartIcon(int idPart) {
        String icon = super.getDetectors().getParts().stream().filter(part -> part.getId() == idPart).findFirst()
                .map(part -> part.getArmIcon()).get();
        Pattern pattern = Pattern.compile(".*\\/ico-(.*)\\.png");
        Matcher matcher = pattern.matcher(icon);
        if (matcher.find()) {
            icon = matcher.group(1);
        }
        return icon;
    }
}
