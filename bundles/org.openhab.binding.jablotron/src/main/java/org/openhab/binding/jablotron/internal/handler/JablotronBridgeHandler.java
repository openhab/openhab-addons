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
package org.openhab.binding.jablotron.internal.handler;

import static org.openhab.binding.jablotron.JablotronBindingConstants.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.jablotron.internal.config.JablotronBridgeConfig;
import org.openhab.binding.jablotron.internal.discovery.JablotronDiscoveryService;
import org.openhab.binding.jablotron.internal.model.JablotronAccessTokenResponse;
import org.openhab.binding.jablotron.internal.model.JablotronControlResponse;
import org.openhab.binding.jablotron.internal.model.JablotronDataUpdateResponse;
import org.openhab.binding.jablotron.internal.model.JablotronDiscoveredService;
import org.openhab.binding.jablotron.internal.model.JablotronGetServiceResponse;
import org.openhab.binding.jablotron.internal.model.JablotronHistoryDataEvent;
import org.openhab.binding.jablotron.internal.model.JablotronLoginResponse;
import org.openhab.binding.jablotron.internal.model.ja100f.JablotronGetPGResponse;
import org.openhab.binding.jablotron.internal.model.ja100f.JablotronGetSectionsResponse;
import org.openhab.binding.jablotron.internal.model.ja100f.JablotronGetThermoDevicesResponse;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link JablotronBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class JablotronBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(JablotronBridgeHandler.class);

    private final Gson gson = new Gson();

    final HttpClient httpClient;

    private String accessToken = "";

    private @Nullable ScheduledFuture<?> future = null;

    /**
     * Our configuration
     */
    public JablotronBridgeConfig bridgeConfig = new JablotronBridgeConfig();

    public JablotronBridgeHandler(Bridge thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(JablotronDiscoveryService.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        bridgeConfig = getConfigAs(JablotronBridgeConfig.class);
        scheduler.execute(this::login);
        future = scheduler.scheduleWithFixedDelay(this::updateAlarmThings, 30, bridgeConfig.getRefresh(),
                TimeUnit.SECONDS);
    }

    private void updateAlarmThings() {
        logger.debug("Updating overall alarm's statuses...");
        @Nullable
        List<JablotronDiscoveredService> services = discoverServices();
        if (services != null) {
            Bridge localBridge = getThing();
            if (localBridge != null && ThingStatus.ONLINE != localBridge.getStatus()) {
                updateStatus(ThingStatus.ONLINE);
            }
            for (JablotronDiscoveredService service : services) {
                updateAlarmThing(service);
            }
        }
    }

    private void updateAlarmThing(JablotronDiscoveredService service) {
        for (Thing th : getThing().getThings()) {
            if (ThingStatus.ONLINE != th.getStatus()) {
                logger.debug("Thing {} is not online", th.getUID());
                continue;
            }

            JablotronAlarmHandler handler = (JablotronAlarmHandler) th.getHandler();

            if (handler == null) {
                logger.debug("Thing handler is null");
                continue;
            }

            if (String.valueOf(service.getId()).equals(handler.thingConfig.getServiceId())) {
                if ("ENABLED".equals(service.getStatus())) {
                    if (!service.getWarning().isEmpty()) {
                        logger.debug("Alarm with service id: {} warning: {}", service.getId(), service.getWarning());
                    }
                    handler.setStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, service.getWarning());
                    if ("ALARM".equals(service.getWarning()) || "TAMPER".equals(service.getWarning())) {
                        handler.triggerAlarm(service);
                    }
                    handler.setInService("SERVICE".equals(service.getWarning()));
                } else {
                    logger.debug("Alarm with service id: {} is offline", service.getId());
                    handler.setStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, service.getStatus());
                }
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        ScheduledFuture<?> localFuture = future;
        if (localFuture != null) {
            localFuture.cancel(true);
        }
        logout();
    }

    private @Nullable <T> T sendJsonMessage(String url, String urlParameters, Class<T> classOfT) {
        return sendMessage(url, urlParameters, classOfT, APPLICATION_JSON, true);
    }

    private @Nullable <T> T sendJsonMessage(String url, String urlParameters, Class<T> classOfT, boolean relogin) {
        return sendMessage(url, urlParameters, classOfT, APPLICATION_JSON, relogin);
    }

    private @Nullable <T> T sendUrlEncodedMessage(String url, String urlParameters, Class<T> classOfT) {
        return sendMessage(url, urlParameters, classOfT, WWW_FORM_URLENCODED, true);
    }

    private @Nullable String sendGQLMessage(String url, String urlParameters) {
        String line = "";
        try {
            logger.trace("Request: {} with data: {}", url, urlParameters);
            ContentResponse resp = createGQLRequest(url)
                    .content(new StringContentProvider(urlParameters), APPLICATION_JSON).send();

            line = resp.getContentAsString();
            logger.trace("Response: {}", line);
            return line;
        } catch (TimeoutException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Timeout during calling url: " + url);
        } catch (InterruptedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Interrupt during calling url: " + url);
            Thread.currentThread().interrupt();
        } catch (JsonSyntaxException e) {
            logger.debug("Invalid JSON received: {}", line);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Syntax error during calling url: " + url);
        } catch (ExecutionException e) {
            if (e.getMessage().contains(AUTHENTICATION_CHALLENGE)) {
                relogin();
                return null;
            }
            logger.debug("Error during calling url: {}", url, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error during calling url: " + url);
        }
        return null;
    }

    private @Nullable <T> T sendMessage(String url, String urlParameters, Class<T> classOfT, String encoding,
            boolean relogin) {
        String line = "";
        try {
            logger.trace("Request: {} with data: {}", url, urlParameters);
            ContentResponse resp = createRequest(url).content(new StringContentProvider(urlParameters), encoding)
                    .send();

            line = resp.getContentAsString();
            logger.trace("Response: {}", line);
            return gson.fromJson(line, classOfT);
        } catch (TimeoutException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Timeout during calling url: " + url);
        } catch (InterruptedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Interrupt during calling url: " + url);
            Thread.currentThread().interrupt();
        } catch (JsonSyntaxException e) {
            logger.debug("Invalid JSON received: {}", line);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Syntax error during calling url: " + url);
        } catch (ExecutionException e) {
            if (relogin) {
                if (e.getMessage().contains(AUTHENTICATION_CHALLENGE)) {
                    relogin();
                    return null;
                }
            }
            logger.debug("Error during calling url: {}", url, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error during calling url: " + url);
        }
        return null;
    }

    protected synchronized void login() {
        String url = JABLOTRON_API_URL + "userAuthorize.json";
        String urlParameters = "{\"login\":\"" + bridgeConfig.getLogin() + "\", \"password\":\""
                + bridgeConfig.getPassword() + "\"}";
        JablotronLoginResponse response = sendJsonMessage(url, urlParameters, JablotronLoginResponse.class, false);

        if (response == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Null login response");
            return;
        }

        if (response.getHttpCode() != 200) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Login http error: " + response.getHttpCode());
            return;
        }

        url = JABLOTRON_API_URL + "accessTokenGet.json";
        urlParameters = "{ \"force-renew\": true }";
        JablotronAccessTokenResponse tokenResponse = sendJsonMessage(url, urlParameters,
                JablotronAccessTokenResponse.class, false);

        if (tokenResponse == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Null get access token response");
            return;
        }

        if (tokenResponse.getHttpCode() != 200) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Get access token http error: " + response.getHttpCode());
        } else {
            accessToken = tokenResponse.getData().getAccessToken();
            updateStatus(ThingStatus.ONLINE);
        }
    }

    protected void logout() {
        String url = JABLOTRON_API_URL + "logout.json";
        String urlParameters = "system=" + SYSTEM;

        try {
            ContentResponse resp = createRequest(url)
                    .content(new StringContentProvider(urlParameters), WWW_FORM_URLENCODED).send();

            if (logger.isTraceEnabled()) {
                String line = resp.getContentAsString();
                logger.trace("logout response: {}", line);
            }
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            // Silence
        }
    }

    public @Nullable List<JablotronDiscoveredService> discoverServices() {
        String url = JABLOTRON_API_URL + "serviceListGet.json";
        String urlParameters = "{\"list-type\": \"EXTENDED\",\"visibility\": \"VISIBLE\"}";
        JablotronGetServiceResponse response = sendJsonMessage(url, urlParameters, JablotronGetServiceResponse.class);

        if (response == null) {
            return null;
        }

        if (response.getHttpCode() != 200) {
            logger.debug("Error during service discovery, got http code: {}", response.getHttpCode());
        }

        return response.getData().getServices();
    }

    protected @Nullable JablotronControlResponse sendUserCode(Thing th, String section, String key, String status,
            String code) {
        JablotronAlarmHandler handler = (JablotronAlarmHandler) th.getHandler();

        if (handler == null) {
            logger.debug("Thing handler is null");
            return null;
        }

        if (handler.isInService()) {
            logger.debug("Cannot send command because the alarm is in the service mode");
            return null;
        }

        String url = JABLOTRON_API_URL + "controlSegment.json";
        String urlParameters = "service=" + th.getThingTypeUID().getId() + "&serviceId="
                + handler.thingConfig.getServiceId() + "&segmentId=" + section + "&segmentKey=" + key
                + "&expected_status=" + status + "&control_time=0&control_code=" + code + "&system=" + SYSTEM;

        JablotronControlResponse response = sendUrlEncodedMessage(url, urlParameters, JablotronControlResponse.class);

        if (response == null) {
            return null;
        }

        if (!response.isStatus()) {
            logger.debug("Error during sending user code: {}", response.getErrorMessage());
        }
        return response;
    }

    protected @Nullable JablotronHistoryDataEvent sendGetEventHistory(Thing th, String alarm) {
        JablotronAlarmHandler handler = (JablotronAlarmHandler) th.getHandler();

        if (handler == null) {
            logger.debug("Thing handler is null");
            return null;
        }

        String urlParameters = "{\"operationName\":\"GetEvents\",\"query\":\"query GetEvents($cloudEntityIds: [CloudEntityID!]!, $pagination: Pagination, $lang: String!, $filter: EventsFilter, $eventIds: [ID!]!) { forEndUser { __typename events { __typename events(sources: $cloudEntityIds, pagination: $pagination, filter: $filter) { __typename edges { __typename node { __typename ...EventsFragment } } pageInfo { __typename hasNextPage endCursor startCursor } } batchEvents(sources: $cloudEntityIds, eventIds: $eventIds) { __typename ...EventsFragment } } } }\\nfragment EventsAttachementFragment on EventsAttachment { __typename files { __typename name mimeType downloadUrl } images { __typename name mimeType downloadUrl available widthPx heightPx } videos { __typename name mimeType downloadUrl duration } id type occurredAt }\\nfragment EventsEventFragment on EventsEvent { __typename id name { __typename translation(lang: $lang) } type occurredAt sources { __typename cloudEntityId } invokers { __typename ...EventsInvokerFragment } subjects { __typename ...EventsSubjectFragment } icon }\\nfragment EventsFragment on EventsEvent { __typename ...EventsEventFragment attachments { __typename ...EventsAttachementFragment } childEvents { __typename id name { __typename translation(lang: $lang) } type occurredAt sources { __typename cloudEntityId } invokers { __typename ...EventsInvokerFragment } subjects { __typename ...EventsSubjectFragment } icon attachments { __typename ...EventsAttachementFragment } } }\\nfragment EventsInvokerFragment on EventsInvoker { __typename cloudEntityId defaultName { __typename translation(lang: $lang) } name }\\nfragment EventsSubjectFragment on EventsSubject { __typename name cloudEntityId defaultName { __typename translation(lang: $lang) } }\",\"variables\":{\"cloudEntityIds\":[\"SERVICE_"
                + alarm + ":" + handler.thingConfig.getServiceId() + "\"],\"eventIds\":[],\"lang\":\""
                + bridgeConfig.getLang() + "\",\"pagination\":{\"first\":1}}}";

        String response = sendGQLMessage(JABLOTRON_GQL_URL, urlParameters);
        if (response == null) {
            logger.debug("Null response while getting event history");
            return null;
        }

        return parseEventHistoryResponse(response);
    }

    private @Nullable JablotronHistoryDataEvent parseEventHistoryResponse(String response) {
        JablotronHistoryDataEvent event = new JablotronHistoryDataEvent();

        try {
            JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
            JsonElement node = getJsonElement(jsonObject, "data", "forEndUser", "events", "events", "edges", 0, "node");

            if (node != null && node.isJsonObject()) {
                event.setIconType(getJsonString(node, "icon"));
                event.setEventText(getJsonString(node, "name", "translation"));
                event.setDate(getJsonString(node, "occurredAt"));

                JsonElement subject = getJsonElement(node, "subjects", 0, "defaultName");
                event.setSectionName(getJsonString(subject, "translation"));

                JsonElement invoker = getJsonElement(node, "invokers", 0, "defaultName");
                event.setInvokerName(getJsonString(invoker, "translation"));
            } else {
                logger.debug("No valid node found in the response");
            }
        } catch (JsonSyntaxException ex) {
            logger.debug("Invalid JSON received: {}", response);
            return null;
        }

        return event;
    }

    private @Nullable JsonElement getJsonElement(@Nullable JsonElement jsonElement, Object... keys) {
        for (Object key : keys) {
            if (jsonElement == null || jsonElement.isJsonNull()) {
                return null;
            }
            if (key instanceof String) {
                JsonObject jsonObject = jsonElement.isJsonObject() ? jsonElement.getAsJsonObject() : null;
                jsonElement = (jsonObject != null && jsonObject.has((String) key)) ? jsonObject.get((String) key)
                        : null;
            } else if (key instanceof Integer) {
                JsonArray jsonArray = jsonElement.isJsonArray() ? jsonElement.getAsJsonArray() : null;
                jsonElement = (jsonArray != null && jsonArray.size() > (int) key
                        && !jsonArray.get((int) key).isJsonNull()) ? jsonArray.get((int) key).getAsJsonObject() : null;
            }
        }
        return jsonElement;
    }

    private String getJsonString(@Nullable JsonElement jsonObject, Object... keys) {
        JsonElement targetElement = getJsonElement(jsonObject, keys);
        return (targetElement != null && !targetElement.isJsonNull()) ? targetElement.getAsString() : "";
    }

    protected @Nullable JablotronDataUpdateResponse sendGetStatusRequest(Thing th) {
        String url = JABLOTRON_API_URL + "dataUpdate.json";
        JablotronAlarmHandler handler = (JablotronAlarmHandler) th.getHandler();

        if (handler == null) {
            logger.debug("Thing handler is null");
            return null;
        }

        String urlParameters = "data=[{ \"filter_data\":[{\"data_type\":\"section\"},{\"data_type\":\"pgm\"},{\"data_type\":\"thermometer\"},{\"data_type\":\"thermostat\"}],\"service_type\":\""
                + th.getThingTypeUID().getId() + "\",\"service_id\":" + handler.thingConfig.getServiceId()
                + ",\"data_group\":\"serviceData\"}]&system=" + SYSTEM;

        return sendUrlEncodedMessage(url, urlParameters, JablotronDataUpdateResponse.class);
    }

    protected @Nullable JablotronGetPGResponse sendGetProgrammableGates(Thing th, String alarm) {
        String url = JABLOTRON_API_URL + alarm + "/programmableGatesGet.json";
        JablotronAlarmHandler handler = (JablotronAlarmHandler) th.getHandler();

        if (handler == null) {
            logger.debug("Thing handler is null");
            return null;
        }

        String urlParameters = getCommonUrlParameters(handler.thingConfig.getServiceId());

        return sendJsonMessage(url, urlParameters, JablotronGetPGResponse.class);
    }

    private String getCommonUrlParameters(String serviceId) {
        return "{\"connect-device\":false,\"list-type\":\"FULL\",\"service-id\":" + serviceId
                + ",\"service-states\":true}";
    }

    protected @Nullable JablotronGetThermoDevicesResponse sendGetThermometers(Thing th, String alarm) {
        String url = JABLOTRON_API_URL + alarm + "/thermoDevicesGet.json";
        JablotronAlarmHandler handler = (JablotronAlarmHandler) th.getHandler();

        if (handler == null) {
            logger.debug("Thing handler is null");
            return null;
        }

        String urlParameters = getCommonUrlParameters(handler.thingConfig.getServiceId());

        return sendJsonMessage(url, urlParameters, JablotronGetThermoDevicesResponse.class);
    }

    protected @Nullable JablotronGetSectionsResponse sendGetSections(Thing th, String alarm) {
        String url = JABLOTRON_API_URL + alarm + "/sectionsGet.json";
        JablotronAlarmHandler handler = (JablotronAlarmHandler) th.getHandler();

        if (handler == null) {
            logger.debug("Thing handler is null");
            return null;
        }

        String urlParameters = getCommonUrlParameters(handler.thingConfig.getServiceId());

        return sendJsonMessage(url, urlParameters, JablotronGetSectionsResponse.class);
    }

    protected @Nullable JablotronGetSectionsResponse controlComponent(Thing th, String code, String action,
            String value, String componentId) throws SecurityException {
        JablotronAlarmHandler handler = (JablotronAlarmHandler) th.getHandler();

        if (handler == null) {
            logger.debug("Thing handler is null");
            return null;
        }

        if (handler.isInService()) {
            logger.debug("Cannot control component because the alarm is in the service mode");
            return null;
        }

        String url = JABLOTRON_API_URL + handler.getAlarmName() + "/controlComponent.json";
        String urlParameters = "{\"authorization\":{\"authorization-code\":\"" + code
                + "\"},\"control-components\":[{\"actions\":{\"action\":\"" + action + "\",\"value\":\"" + value
                + "\"},\"component-id\":\"" + componentId + "\"}],\"service-id\":" + handler.thingConfig.getServiceId()
                + "}";

        return sendJsonMessage(url, urlParameters, JablotronGetSectionsResponse.class);
    }

    private Request createRequest(String url) {
        return httpClient.newRequest(url).method(HttpMethod.POST).header(HttpHeader.ACCEPT, APPLICATION_JSON)
                .header(HttpHeader.ACCEPT_LANGUAGE, bridgeConfig.getLang()).header(HttpHeader.ACCEPT_ENCODING, "*")
                .header("x-vendor-id", VENDOR).header("x-client-version", CLIENT_VERSION).agent(AGENT)
                .timeout(TIMEOUT_SEC, TimeUnit.SECONDS);
    }

    private Request createGQLRequest(String url) {
        return httpClient.newRequest(url).method(HttpMethod.POST).accept(MULTIPART_MIXED, APPLICATION_JSON)
                .header(HttpHeader.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeader.ACCEPT_LANGUAGE, bridgeConfig.getLang())
                .header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate, br").agent(AGENT)
                .timeout(TIMEOUT_SEC, TimeUnit.SECONDS);
    }

    private void relogin() {
        logger.debug("doing relogin");
        login();
    }
}
