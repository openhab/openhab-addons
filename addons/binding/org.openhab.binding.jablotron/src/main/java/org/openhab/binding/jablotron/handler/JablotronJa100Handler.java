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
import org.eclipse.smarthome.core.library.types.*;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.jablotron.internal.Utils;
import org.openhab.binding.jablotron.internal.model.ja100.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.openhab.binding.jablotron.JablotronBindingConstants.*;

/**
 * The {@link JablotronJa100Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class JablotronJa100Handler extends JablotronAlarmHandler {

    private final Logger logger = LoggerFactory.getLogger(JablotronJa100Handler.class);

    public JablotronJa100Handler(Thing thing) {
        super(thing);
    }

    private int stav_1 = 0;
    private int stav_2 = 0;
    private int stav_3 = 0;
    private int stav_4 = 0;
    private int stav_5 = 0;
    private int stav_6 = 0;
    private int stav_7 = 0;
    private int stav_8 = 0;
    private int stav_9 = 0;
    private int stav_10 = 0;
    private int stav_11 = 0;
    private int stav_12 = 0;
    private int stav_13 = 0;
    private int stav_14 = 0;
    private int stav_15 = 0;

    private int stavPGM_1 = 0;
    private int stavPGM_2 = 0;
    private int stavPGM_3 = 0;
    private int stavPGM_4 = 0;
    private int stavPGM_5 = 0;
    private int stavPGM_6 = 0;
    private int stavPGM_7 = 0;
    private int stavPGM_8 = 0;
    private int stavPGM_9 = 0;
    private int stavPGM_10 = 0;
    private int stavPGM_11 = 0;
    private int stavPGM_12 = 0;
    private int stavPGM_13 = 0;
    private int stavPGM_14 = 0;
    private int stavPGM_15 = 0;
    private int stavPGM_16 = 0;
    private int stavPGM_17 = 0;
    private int stavPGM_18 = 0;
    private int stavPGM_19 = 0;
    private int stavPGM_20 = 0;

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!isAlarmSection(channelUID.getId()) && command instanceof OnOffType) {
            String section = getSectionFromChannel(channelUID.getId());

            if (section != null) {
                scheduler.execute(() -> {
                    controlSection(section, command.equals(OnOffType.ON) ? "1" : "0", thingConfig.getUrl());
                });
            }
        }

        if (isAlarmSection(channelUID.getId()) && command instanceof StringType) {
            String section = getSectionFromChannel(channelUID.getId());
            if (section != null) {
                scheduler.execute(() -> {
                    sendCommand(section, command.toString(), thingConfig.getUrl());
                });
            }
        }
    }

    private String getSectionFromChannel(String channel) {
        switch (channel) {
            /*
            case CHANNEL_STATUS_1:
                return "STATE_1";
            case CHANNEL_STATUS_2:
                return "STATE_2";
            case CHANNEL_STATUS_3:
                return "STATE_3";
            case CHANNEL_STATUS_4:
                return "STATE_4";
            case CHANNEL_STATUS_5:
                return "STATE_5";
            case CHANNEL_STATUS_6:
                return "STATE_6";
            case CHANNEL_STATUS_7:
                return "STATE_7";
            case CHANNEL_STATUS_8:
                return "STATE_8";
            case CHANNEL_STATUS_9:
                return "STATE_9";
            case CHANNEL_STATUS_10:
                return "STATE_10";
            case CHANNEL_STATUS_11:
                return "STATE_11";
            case CHANNEL_STATUS_12:
                return "STATE_12";
            case CHANNEL_STATUS_13:
                return "STATE_13";
            case CHANNEL_STATUS_14:
                return "STATE_14";
            case CHANNEL_STATUS_15:
                return "STATE_15";
            case CHANNEL_STATUS_PGM_1:
                return "PGM_1";
            case CHANNEL_STATUS_PGM_2:
                return "PGM_2";
            case CHANNEL_STATUS_PGM_3:
                return "PGM_3";
            case CHANNEL_STATUS_PGM_4:
                return "PGM_4";
            case CHANNEL_STATUS_PGM_5:
                return "PGM_5";
            case CHANNEL_STATUS_PGM_6:
                return "PGM_6";
            case CHANNEL_STATUS_PGM_7:
                return "PGM_7";
            case CHANNEL_STATUS_PGM_8:
                return "PGM_8";
            case CHANNEL_STATUS_PGM_9:
                return "PGM_9";
            case CHANNEL_STATUS_PGM_10:
                return "PGM_10";
            case CHANNEL_STATUS_PGM_11:
                return "PGM_11";
            case CHANNEL_STATUS_PGM_12:
                return "PGM_12";
            case CHANNEL_STATUS_PGM_13:
                return "PGM_13";
            case CHANNEL_STATUS_PGM_14:
                return "PGM_14";
            case CHANNEL_STATUS_PGM_15:
                return "PGM_15";
            case CHANNEL_STATUS_PGM_16:
                return "PGM_16";
            case CHANNEL_STATUS_PGM_17:
                return "PGM_17";
            case CHANNEL_STATUS_PGM_18:
                return "PGM_18";
            case CHANNEL_STATUS_PGM_19:
                return "PGM_19";
            case CHANNEL_STATUS_PGM_20:
                return "PGM_20";*/
            default:
                return null;
        }
    }

    private boolean isAlarmSection(String channel) {
        return channel.contains("STATE");
    }

    private synchronized Ja100StatusResponse sendGetStatusRequest() {

        String url = JABLOTRON_URL + "app/ja100/ajax/stav.php?" + Utils.getBrowserTimestamp();
        try {
            ContentResponse resp = httpClient.newRequest(url)
                    .method(HttpMethod.GET)
                    .header(HttpHeader.ACCEPT_LANGUAGE, "cs-CZ")
                    .header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate")
                    .header(HttpHeader.REFERER, JABLOTRON_URL + JA100_SERVICE_URL + thingConfig.getServiceId())
                    .header("X-Requested-With", "XMLHttpRequest")
                    .agent(AGENT)
                    .timeout(TIMEOUT, TimeUnit.SECONDS)
                    .send();

            String line = resp.getContentAsString();

            logger.info("getStatus response: {}", line);
            return gson.fromJson(line, Ja100StatusResponse.class);
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

            Ja100StatusResponse response = sendGetStatusRequest();

            if (response == null || response.getStatus() != 200) {
                //controlDisabled = true;
                //inService = false;
                login();
                initializeService();
                response = sendGetStatusRequest();
            }
            if (response == null) {
                logger.error("Null status response received");
                return false;
            }

            if (response.isBusyStatus()) {
                logger.warn("JA100 is busy...giving up");
                logout();
                return false;
            }
            /*
            if (response.hasEvents()) {
                ArrayList<OasisEvent> events = response.getEvents();
                for (OasisEvent event : events) {
                    logger.debug("Found event: {} {} {}", event.getDatum(), event.getCode(), event.getEvent());
                    //updateLastEvent(event);
                }
            } else {
                ArrayList<Ja100Event> history = getServiceHistory();
                logger.debug("History log contains {} events", history.size());
                if (history.size() > 0) {
                    Ja100Event event = history.get(0);
                    updateLastEvent(event);
                    logger.debug("Last event: {} is of class: {} has section: {}", event.getEvent(), event.getEventClass(), event.getSection());
                }
            }*/

            inService = response.inService();

            if (inService) {
                logger.warn("Alarm is in service mode...");
                return false;
            }

            if (!response.isOKStatus()) {
                logger.error("Cannot get alarm status!");
                return false;
            }

            if(response.hasSectionStatus()) {
                processSections(response);
            }
            if(response.hasPGMStatus()) {
                processPGMs(response);
            }

            if (response.hasTemperature()) {
                processTemperatures(response);
            }
            //update last check time
            updateState(CHANNEL_LAST_CHECK_TIME, getCheckTime());

            return true;
        } catch (Exception ex) {
            logger.error("Error during alarm status update", ex);
            return false;
        }
    }

    private void processTemperatures(Ja100StatusResponse response) {
        ArrayList<Ja100Temperature> temps = response.getTemperatures();
        for (Ja100Temperature temp : temps) {
            logger.debug("Found a temperature sensor: {} with value: {}", temp.getStateName(), temp.getValue());
            Channel channel = getThing().getChannel(temp.getStateName());
            if (channel == null) {
                logger.debug("Creating a new channel: {}", temp.getStateName());
                createTemperatureChannel(temp);
            }
            channel = getThing().getChannel(temp.getStateName());
            if (channel != null) {
                logger.debug("Updating channel: {} to value: {}", channel.getUID(), temp.getValue());
                updateState(channel.getUID(), new DecimalType(temp.getValue()));
            } else {
                logger.warn("The channel: {} still doesn't exist!", temp.getStateName());
            }
        }
    }

    private void processSections(Ja100StatusResponse response) {
        ArrayList<Ja100Section> secs = response.getSections();
        for (Ja100Section section : secs) {
            logger.info("Found a section: {} with status: {}", section.getNazev(), section.getStav());
            Channel channel = getThing().getChannel(section.getStateName());
            if (channel == null) {
                logger.info("Creating a new channel: {}", section.getStateName());
                createSectionChannel(section);
            }
            channel = getThing().getChannel(section.getStateName());
            if (channel != null) {
                logger.info("Updating channel: {} to value: {}", channel.getUID(), section.getStav());
                updateState(channel.getUID(), new DecimalType(section.getStav()));
            } else {
                logger.warn("The channel: {} still doesn't exist!", section.getStateName());
            }
        }
    }

    private void processPGMs(Ja100StatusResponse response) {
        ArrayList<Ja100Section> secs = response.getPGMs();
        for (Ja100Section section : secs) {
            logger.info("Found a PGM: {} with status: {}", section.getNazev(), section.getStav());
            Channel channel = getThing().getChannel(section.getStateName());
            if (channel == null) {
                logger.info("Creating a new channel: {}", section.getStateName());
                createPGMChannel(section);
            }
            channel = getThing().getChannel(section.getStateName());
            if (channel != null) {
                logger.info("Updating channel: {} to value: {}", channel.getUID(), section.getStav());
                updateState(channel.getUID(), section.getStav() == 1 ? OnOffType.ON : OnOffType.OFF);
            } else {
                logger.warn("The channel: {} still doesn't exist!", section.getStateName());
            }
        }
    }

    private void createSectionChannel(Ja100Section section) {
        createChannel(section.getStateName(), "Number", section.getNazev());
    }

    private void createPGMChannel(Ja100Section section) {
        createChannel(section.getStateName(), "Switch", section.getNazev());
    }

    private void createTemperatureChannel(Ja100Temperature temp) {
        /*
        ThingBuilder thingBuilder = editThing();
        Channel channel = ChannelBuilder.create(new ChannelUID(thing.getUID(), temp.getStateName()), "Number").withLabel(temp.getStateName()).build();
        thingBuilder.withChannel(channel);
        updateThing(thingBuilder.build());*/
        createChannel(temp.getStateName(), "Number", temp.getStateName());
    }

    private void createChannel(String name, String type, String label) {
        ThingBuilder thingBuilder = editThing();
        Channel channel = ChannelBuilder.create(new ChannelUID(thing.getUID(), name), type).withLabel(label).build();
        thingBuilder.withChannel(channel);
        updateThing(thingBuilder.build());
    }

    private void updateLastEvent(Ja100Event event) {
        //ZonedDateTime time = ZonedDateTime.parse("2018-03-15T18:07:32+01:00", DateTimeFormatter.ISO_DATE_TIME);
        //ZonedDateTime time = ZonedDateTime.parse(event.getDate(), DateTimeFormatter.ISO_DATE_TIME);
        //DateTimeType typ = new DateTimeType(time);
        updateState(CHANNEL_LAST_EVENT_TIME, new DateTimeType(event.getZonedDateTime()));
        updateState(CHANNEL_LAST_EVENT_SECTION, new StringType(event.getSection()));
        updateState(CHANNEL_LAST_EVENT, new StringType(event.getEvent()));
        updateState(CHANNEL_LAST_EVENT_CLASS, new StringType(event.getEventClass()));
    }

    public synchronized void sendCommand(String section, String code, String serviceUrl) {
        int status;
        Integer result;
        try {
            if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
                login();
                initializeService();
            }
            if (!updateAlarmStatus()) {
                logger.error("Cannot send user code due to alarm status!");
                return;
            }

            Ja100ControlResponse response = sendUserCode(section, "", serviceUrl);
            if (response == null) {
                logger.warn("null response received");
                return;
            }

            status = response.getResponseCode();
            result = response.getResult();
            if (result != null) {
                handleHttpRequestStatus(status);
                /*
                if (response != null && response.getResult() != null) {
                    handleHttpRequestStatus(response.getResponseCode());
                } else {
                    logger.warn("null response/status received");
                    logout();
                }*/
            } else {
                logger.warn("null status received");
                logout();
            }
        } catch (Exception e) {
            logger.error("internalReceiveCommand exception", e);
        }
    }


    private synchronized Ja100ControlResponse sendUserCode(String section, String code, String serviceUrl) {
        return sendUserCode("ovladani2.php", section, code.isEmpty() ? "1" : "", code, serviceUrl);
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

            logger.debug("Controlling section: {} with status: {}", section, status);
            Ja100ControlResponse response = sendUserCode("ovladani2.php", section, status, "", serviceUrl);

            if (response != null && response.getResult() != null) {
                handleHttpRequestStatus(response.getResponseCode());
            } else {
                logger.warn("null response/status received");
                logout();
            }

        } catch (Exception e) {
            logger.error("internalReceiveCommand exception", e);
        }
    }

    protected synchronized Ja100ControlResponse sendUserCode(String site, String section, String status, String code, String serviceUrl) {
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
            Ja100ControlResponse response = gson.fromJson(line, Ja100ControlResponse.class);
            logger.debug("sendUserCode result: {}", response.getResult());
            return response;
        } catch (TimeoutException ex) {
            logger.debug("sendUserCode timeout exception", ex);
        } catch (Exception ex) {
            logger.error("sendUserCode exception", ex);
        }
        return null;
    }

    protected void logout(boolean setOffline) {

        String url = JABLOTRON_URL + "logout";
        try {
            ContentResponse resp = httpClient.newRequest(url)
                    .method(HttpMethod.GET)
                    .header(HttpHeader.ACCEPT_LANGUAGE, "cs-CZ")
                    .header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate")
                    .header(HttpHeader.REFERER, JABLOTRON_URL + JA100_SERVICE_URL + thingConfig.getServiceId())
                    .agent(AGENT)
                    .timeout(5, TimeUnit.SECONDS)
                    .send();
            String line = resp.getContentAsString();

            logger.debug("logout... {}", line);
        } catch (Exception e) {
            //Silence
        } finally {
            //controlDisabled = true;
            inService = false;
            if (setOffline) {
                updateStatus(ThingStatus.OFFLINE);
            }
        }
    }

    private ArrayList<Ja100Event> getServiceHistory() {
        String serviceId = thingConfig.getServiceId();
        try {
            String url = "https://www.jablonet.net/app/ja100/ajax/historie.php";
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

            logger.info("History response: {}", line);

            ArrayList<Ja100Event> result = new ArrayList<>();

            JsonParser parser = new JsonParser();
            JsonObject jobject = parser.parse(line).getAsJsonObject();
            if (jobject.has("ResponseCode") && jobject.get("ResponseCode").getAsInt() == 200) {
                logger.info("History successfully retrieved with total of {} events.", jobject.get("EventsCount").getAsInt());
                if (jobject.has("HistoryData")) {
                    jobject = jobject.get("HistoryData").getAsJsonObject();
                    if (jobject.has("Events")) {
                        JsonArray jarray = jobject.get("Events").getAsJsonArray();
                        logger.info("Parsing events...");
                        Ja100Event[] events = gson.fromJson(jarray, Ja100Event[].class);
                        result.addAll(Arrays.asList(events));
                        logger.info("Last event: {}", events[0].toString());
                    }
                }
            }
            return result;
        } catch (Exception ex) {
            logger.error("Cannot get Jablotron service history: {}", serviceId, ex);
        }
        return null;
    }
}
