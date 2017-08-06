/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jablotron.handler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.jablotron.internal.Utils;
import org.openhab.binding.jablotron.internal.model.oasis.OasisControlResponse;
import org.openhab.binding.jablotron.internal.model.oasis.OasisEvent;
import org.openhab.binding.jablotron.internal.model.oasis.OasisStatusResponse;
import org.openhab.binding.jablotron.internal.model.JablotronTrouble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.openhab.binding.jablotron.JablotronBindingConstants.*;

/**
 * The {@link JablotronOasisHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class JablotronOasisHandler extends JablotronAlarmHandler {

    private final Logger logger = LoggerFactory.getLogger(JablotronOasisHandler.class);

    private int stavA = 0;
    private int stavB = 0;
    private int stavABC = 0;
    private int stavPGX = 0;
    private int stavPGY = 0;
    private boolean controlDisabled = true;

    public JablotronOasisHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_COMMAND) && command instanceof StringType) {
            scheduler.execute(() -> {
                sendCommand(command.toString(), thingConfig.getUrl());
            });
        }

        if (channelUID.getId().equals(CHANNEL_STATUS_PGX) && command instanceof OnOffType) {
            scheduler.execute(() -> {
                controlSection("PGX", command.equals(OnOffType.ON) ? "1" : "0", thingConfig.getUrl());
            });
        }

        if (channelUID.getId().equals(CHANNEL_STATUS_PGY) && command instanceof OnOffType) {
            scheduler.execute(() -> {
                controlSection("PGY", command.equals(OnOffType.ON) ? "1" : "0", thingConfig.getUrl());
            });
        }
    }

    private void readAlarmStatus(OasisStatusResponse response) {
        logger.debug("Reading alarm status...");
        controlDisabled = response.isControlDisabled();

        stavA = response.getSekce().get(0).getStav();
        stavB = response.getSekce().get(1).getStav();
        stavABC = response.getSekce().get(2).getStav();

        stavPGX = response.getPgm().get(0).getStav();
        stavPGY = response.getPgm().get(1).getStav();

        logger.debug("Stav A: {}", stavA);
        logger.debug("Stav B: {}", stavB);
        logger.debug("Stav ABC: {}", stavABC);
        logger.debug("Stav PGX: {}", stavPGX);
        logger.debug("Stav PGY: {}", stavPGY);

        for (Channel channel : getThing().getChannels()) {
            State newState = null;
            String type = channel.getUID().getId();

            switch (type) {
                case CHANNEL_STATUS_A:
                    newState = (stavA == 1) ? OnOffType.ON : OnOffType.OFF;
                    break;
                case CHANNEL_STATUS_B:
                    newState = (stavB == 1) ? OnOffType.ON : OnOffType.OFF;
                    break;
                case CHANNEL_STATUS_ABC:
                    newState = (stavABC == 1) ? OnOffType.ON : OnOffType.OFF;
                    break;
                case CHANNEL_STATUS_PGX:
                    newState = (stavPGX == 1) ? OnOffType.ON : OnOffType.OFF;
                    break;
                case CHANNEL_STATUS_PGY:
                    newState = (stavPGY == 1) ? OnOffType.ON : OnOffType.OFF;
                    break;
                case CHANNEL_ALARM:
                    newState = (response.isAlarm()) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                    break;
                case CHANNEL_LAST_EVENT_TIME:
                    Date lastEvent = response.getLastEventTime();
                    if (lastEvent != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(lastEvent);
                        ZonedDateTime zdt = ZonedDateTime.ofInstant(cal.toInstant(), ZoneId.systemDefault());
                        newState = new DateTimeType(zdt);
                    }
                    break;
                default:
                    break;
            }

            if (newState != null) {
                //eventPublisher.postUpdate(itemName, newState);
                updateState(channel.getUID(), newState);
            }
        }
    }

    private synchronized OasisStatusResponse sendGetStatusRequest() {

        String url = JABLOTRON_URL + "app/oasis/ajax/stav.php?" + Utils.getBrowserTimestamp();
        try {
            ContentResponse resp = httpClient.newRequest(url)
                    .method(HttpMethod.GET)
                    .header(HttpHeader.ACCEPT_LANGUAGE, "cs-CZ")
                    .header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate")
                    .header(HttpHeader.REFERER, JABLOTRON_URL + OASIS_SERVICE_URL + thingConfig.getServiceId())
                    .header("X-Requested-With", "XMLHttpRequest")
                    .agent(AGENT)
                    .timeout(TIMEOUT, TimeUnit.SECONDS)
                    .send();

            String line = resp.getContentAsString();
            logger.trace("get status: {}", line);

            return gson.fromJson(line, OasisStatusResponse.class);
        } catch (TimeoutException ste) {
            logger.debug("Timeout during getting alarm status!");
            return null;
        } catch (Exception e) {
            logger.error("sendGetStatusRequest exception", e);
            return null;
        }
    }

    protected synchronized boolean updateAlarmStatus() {
        logger.debug("updating alarm status...");

        try {
            // relogin every hour
            int hours = Utils.getHoursOfDay();
            if (lastHours >= 0 && lastHours != hours) {
                relogin();
            } else {
                initializeService();
            }
            lastHours = hours;

            OasisStatusResponse response = sendGetStatusRequest();

            if (response == null || response.getStatus() != 200) {
                controlDisabled = true;
                inService = false;
                login();
                initializeService();
                response = sendGetStatusRequest();
                if (response == null) {
                    return false;
                }
            }
            if (response.isBusyStatus()) {
                logger.warn("OASIS is busy...giving up");
                logout();
                return false;
            }
            if (response.hasTroubles()) {
                ArrayList<JablotronTrouble> troubles = response.getTroubles();
                for (JablotronTrouble trouble : troubles) {
                    logger.debug("Found trouble: {} {} {} ({})", trouble.getZekdy(), trouble.getCas(), trouble.getMessage(), trouble.getName());
                }
                updateLastTrouble(troubles.get(0));
            } else {
                clearTrouble();
            }
            if (response.hasEvents()) {
                ArrayList<OasisEvent> events = response.getEvents();
                for (OasisEvent event : events) {
                    logger.debug("Found event: {} {} {}", event.getDatum(), event.getCode(), event.getEvent());
                    updateLastEvent(event);
                }
            } else {
                ArrayList<OasisEvent> history = getServiceHistory();
                logger.debug("History log contains {} events", history.size());
                if (history.size() > 0) {
                    OasisEvent event = history.get(0);
                    updateLastEvent(event);
                    logger.debug("Last event: {} is of class: {} has code: {}", event.getEvent(), event.getEventClass(), event.getCode());
                }
            }

            inService = response.inService();

            if (inService) {
                logger.warn("Alarm is in service mode...");
                return false;
            }

            if (response.isOKStatus() && response.hasSectionStatus()) {
                readAlarmStatus(response);
            } else {
                logger.error("Cannot get alarm status!");
                return false;
            }
            //update last check time
            updateState(CHANNEL_LAST_CHECK_TIME, getCheckTime());

            return true;
        } catch (Exception ex) {
            logger.error("Error during alarm status update", ex);
            return false;
        }
    }

    private void clearTrouble() {
        updateState(CHANNEL_LAST_TROUBLE, new StringType(""));
        updateState(CHANNEL_LAST_TROUBLE_DETAIL, new StringType(""));
    }

    private void updateLastEvent(OasisEvent event) {
        updateState(CHANNEL_LAST_EVENT_CODE, new StringType(event.getCode()));
        updateState(CHANNEL_LAST_EVENT, new StringType(event.getEvent()));
        updateState(CHANNEL_LAST_EVENT_CLASS, new StringType(event.getEventClass()));
    }

    private void updateLastTrouble(JablotronTrouble trouble) {
        updateState(CHANNEL_LAST_TROUBLE, new StringType(trouble.getMessage()));
        updateState(CHANNEL_LAST_TROUBLE_DETAIL, new StringType(trouble.getName()));
    }

    public synchronized void controlSection(String section, String status, String serviceUrl) {
        try {
            if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
                login();
                initializeService();
            }
            if (!updateAlarmStatus()) {
                logger.error("Cannot control section due to alarm status!");
                return;
            }
            int timeout = 30;
            while (controlDisabled && --timeout >= 0) {
                logger.info("Waiting for control enabling...");
                Thread.sleep(1000);
                boolean ok = updateAlarmStatus();
                if (!ok) {
                    return;
                }
            }
            if (timeout < 0) {
                logger.warn("Timeout during waiting for control enabling");
                return;
            }


            logger.debug("Controlling section: {} with status: {}", section, status);
            OasisControlResponse response = sendUserCode("ovladani.php", section, status, "", serviceUrl);

            if (response != null && response.getVysledek() != null) {
                handleHttpRequestStatus(response.getStatus());
            } else {
                logger.warn("null response/status received");
                logout();
            }

        } catch (Exception e) {
            logger.error("internalReceiveCommand exception", e);
        }
    }

    public synchronized void sendCommand(String code, String serviceUrl) {
        int status = 0;
        Integer result = 0;
        try {
            if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
                login();
                initializeService();
            }
            if (!updateAlarmStatus()) {
                logger.error("Cannot send user code due to alarm status!");
                return;
            }
            int timeout = 30;
            while (controlDisabled && --timeout >= 0) {
                logger.info("Waiting for control enabling...");
                Thread.sleep(1000);
                boolean ok = updateAlarmStatus();
                if (!ok) {
                    return;
                }
            }
            if (timeout < 0) {
                logger.warn("Timeout during waiting for control enabling");
                return;
            }

            OasisControlResponse response = sendUserCode("", serviceUrl);
            if (response == null) {
                logger.warn("null response received");
                return;
            }

            status = response.getStatus();
            result = response.getVysledek();
            if (result != null) {
                if (status == 200 && result == 4) {
                    logger.debug("Sending user code: {}", code);
                    response = sendUserCode(code, serviceUrl);
                } else {
                    logger.warn("Received unknown status: {}", status);
                }
                if (response != null && response.getVysledek() != null) {
                    handleHttpRequestStatus(response.getStatus());
                } else {
                    logger.warn("null response/status received");
                    logout();
                }
            } else {
                logger.warn("null status received");
                logout();
            }
        } catch (Exception e) {
            logger.error("internalReceiveCommand exception", e);
        }
    }

    protected synchronized OasisControlResponse sendUserCode(String site, String section, String status, String code, String serviceUrl) {
        String url;

        try {
            url = JABLOTRON_URL + "app/" + thing.getThingTypeUID().getId() + "/ajax/" + site;
            String urlParameters = "section=" + section + "&status=" + status + "&code=" + code;

            logger.info("Sending POST to url address: {} to control section: {}", url, section);

            ContentResponse resp = httpClient.newRequest(url)
                    .method(HttpMethod.POST)
                    .header(HttpHeader.ACCEPT_LANGUAGE, "cs-CZ")
                    .header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate")
                    .header(HttpHeader.REFERER, serviceUrl)
                    .header("X-Requested-With", "XMLHttpRequest")
                    .agent(AGENT)
                    .content(new StringContentProvider(urlParameters), "application/x-www-form-urlencoded; charset=UTF-8")
                    .timeout(TIMEOUT, TimeUnit.SECONDS)
                    .send();

            String line = resp.getContentAsString();


            logger.info("Control response: {}", line);
            OasisControlResponse response = gson.fromJson(line, OasisControlResponse.class);
            logger.debug("sendUserCode result: {}", response.getVysledek());
            return response;
        } catch (TimeoutException ex) {
            logger.debug("sendUserCode timeout exception", ex);
        } catch (Exception ex) {
            logger.error("sendUserCode exception", ex);
        }
        return null;
    }

    private synchronized OasisControlResponse sendUserCode(String code, String serviceUrl) {
        return sendUserCode("ovladani.php", "STATE", code.isEmpty() ? "1" : "", code, serviceUrl);
    }

    protected void logout(boolean setOffline) {

        String url = JABLOTRON_URL + "logout";
        try {
            ContentResponse resp = httpClient.newRequest(url)
                    .method(HttpMethod.GET)
                    .header(HttpHeader.ACCEPT_LANGUAGE, "cs-CZ")
                    .header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate")
                    .header(HttpHeader.REFERER, JABLOTRON_URL + OASIS_SERVICE_URL + thingConfig.getServiceId())
                    .agent(AGENT)
                    .timeout(5, TimeUnit.SECONDS)
                    .send();
            String line = resp.getContentAsString();

            logger.debug("logout... {}", line);
        } catch (Exception e) {
            //Silence
        } finally {
            controlDisabled = true;
            inService = false;
            if (setOffline) {
                updateStatus(ThingStatus.OFFLINE);
            }
        }
    }

    private ArrayList<OasisEvent> getServiceHistory() {
        String serviceId = thingConfig.getServiceId();
        try {
            String url = "https://www.jablonet.net/app/oasis/ajax/historie.php";
            String urlParameters = "from=this_month&to=&gps=0&log=0&header=0";

            ContentResponse resp = httpClient.newRequest(url)
                    .method(HttpMethod.POST)
                    .header(HttpHeader.ACCEPT_LANGUAGE, "cs-CZ")
                    .header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate")
                    .header(HttpHeader.REFERER, JABLOTRON_URL)
                    .header("X-Requested-With", "XMLHttpRequest")
                    .agent(AGENT)
                    .content(new StringContentProvider(urlParameters), "application/x-www-form-urlencoded; charset=UTF-8")
                    .timeout(TIMEOUT, TimeUnit.SECONDS)
                    .send();

            String line = resp.getContentAsString();

            logger.debug("History: {}", line);

            ArrayList<OasisEvent> result = new ArrayList<>();

            JsonParser parser = new JsonParser();
            JsonObject jobject = parser.parse(line).getAsJsonObject();
            if (jobject.has("events")) {
                jobject = jobject.get("events").getAsJsonObject();

                for (Map.Entry<String, JsonElement> entry : jobject.entrySet()) {
                    String key = entry.getKey();
                    if (jobject.get(key) instanceof JsonArray) {
                        OasisEvent[] events = gson.fromJson(jobject.get(key), OasisEvent[].class);
                        result.addAll(Arrays.asList(events));
                    }
                }
            }
            return result;
        } catch (TimeoutException ex) {
            logger.debug("Timeout during getting Jablotron service history: {}", serviceId, ex);
        } catch (Exception ex) {
            logger.error("Cannot get Jablotron service history: {}", serviceId, ex);
        }
        return null;
    }

}
