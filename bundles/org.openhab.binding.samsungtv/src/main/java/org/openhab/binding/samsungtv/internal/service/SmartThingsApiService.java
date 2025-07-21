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
package org.openhab.binding.samsungtv.internal.service;

import static org.openhab.binding.samsungtv.internal.SamsungTvBindingConstants.*;
import static org.openhab.binding.samsungtv.internal.config.SamsungTvConfiguration.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.ResponseInfo;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.samsungtv.internal.handler.SamsungTvHandler;
import org.openhab.binding.samsungtv.internal.service.api.SamsungTvService;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link SmartThingsApiService} is responsible for handling the Smartthings cloud interface
 * 
 *
 * @author Nick Waterton - Initial contribution
 */
@NonNullByDefault
public class SmartThingsApiService implements SamsungTvService {

    public static final String SERVICE_NAME = "SmartthingsApi";
    private static final List<String> SUPPORTED_CHANNELS = Arrays.asList(SOURCE_NAME, SOURCE_ID);
    private static final List<String> REFRESH_CHANNELS = Arrays.asList(CHANNEL, CHANNEL_NAME, SOURCE_NAME, SOURCE_ID);
    // Smarttings URL
    private static final String SMARTTHINGS_URL = "api.smartthings.com";
    // Path for the information endpoint note the final /
    private static final String API_ENDPOINT_V1 = "/v1/";
    // private static final String INPUT_SOURCE = "/components/main/capabilities/mediaInputSource/status";
    // private static final String CURRENT_CHANNEL = "/components/main/capabilities/tvChannel/status";
    private static final String COMPONENTS = "/components/main/status";
    private static final String DEVICES = "devices";
    private static final String COMMAND = "/commands";

    private final Logger logger = LoggerFactory.getLogger(SmartThingsApiService.class);

    private String host = "";
    private String apiKey = "";
    private String deviceId = "";
    private boolean subscriptionEnabled = true;
    private int RATE_LIMIT = 1000;
    private int TIMEOUT = 1000; // connection timeout in ms
    private long prevUpdate = 0;
    private boolean online = false;
    private int errorCount = 0;
    private int MAX_ERRORS = 100;

    private final SamsungTvHandler handler;

    private Optional<TvValues> tvInfo = Optional.empty();
    private boolean subscriptionRunning = false;
    private Optional<BodyHandlerWrapper> handlerWrapper = Optional.empty();
    private Optional<STSubscription> subscription = Optional.empty();

    private Map<String, Object> stateMap = Collections.synchronizedMap(new HashMap<>());

    public SmartThingsApiService(String host, SamsungTvHandler handler) {
        this.handler = handler;
        this.host = host;
        this.apiKey = handler.configuration.getSmartThingsApiKey();
        this.deviceId = handler.configuration.getSmartThingsDeviceId();
        this.subscriptionEnabled = handler.configuration.getSmartThingsSubscription();
        logger.debug("{}: Creating a Samsung TV Smartthings Api service", host);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public List<String> getSupportedChannelNames(boolean refresh) {
        if (refresh) {
            if (subscriptionRunning) {
                return Arrays.asList();
            }
            return REFRESH_CHANNELS;
        }
        logger.trace("{}: getSupportedChannelNames: {}", host, SUPPORTED_CHANNELS);
        return SUPPORTED_CHANNELS;
    }

    // Description of tvValues
    @NonNullByDefault({})
    class TvValues {
        class MediaInputSource {
            ValuesList supportedInputSources;
            ValuesListMap supportedInputSourcesMap;
            Values inputSource;
        }

        class TvChannel {
            Values tvChannel;
            Values tvChannelName;

            public String getChannelNum() {
                return Optional.ofNullable(tvChannel).map(a -> a.value).orElse("");
            }
        }

        class Values {
            String value;
            String timestamp;
        }

        class ValuesList {
            String[] value;
            String timestamp;
        }

        class ValuesListMap {
            InputList[] value;
            String timestamp;

            public String[] getInputList() {
                return Optional.ofNullable(value).map(a -> Arrays.stream(a).map(b -> b.getId()).toArray(String[]::new))
                        .orElse(new String[0]);
            }
        }

        class InputList {
            public String id;
            String name;

            public String getId() {
                return Optional.ofNullable(id).orElse("");
            }
        }

        class Items {
            String deviceId;
            String name;
            String label;

            public String getDeviceId() {
                return Optional.ofNullable(deviceId).orElse("");
            }

            public String getName() {
                return Optional.ofNullable(name).orElse("");
            }

            public String getLabel() {
                return Optional.ofNullable(label).orElse("");
            }
        }

        class Error {
            String code;
            String message;
            Details[] details;
        }

        class Details {
            String code;
            String target;
            String message;
        }

        @SerializedName(value = "samsungvd.mediaInputSource", alternate = { "mediaInputSource" })
        MediaInputSource mediaInputSource;
        TvChannel tvChannel;
        Items[] items;
        Error error;

        public void updateSupportedInputSources(String[] values) {
            mediaInputSource.supportedInputSources.value = values;
        }

        public Items[] getItems() {
            return Optional.ofNullable(items).orElse(new Items[0]);
        }

        public String[] getSources() {
            return Optional.ofNullable(mediaInputSource).map(a -> a.supportedInputSources).map(a -> a.value)
                    .orElseGet(() -> getSourcesFromMap());
        }

        public String[] getSourcesFromMap() {
            return Optional.ofNullable(mediaInputSource).map(a -> a.supportedInputSourcesMap).map(a -> a.getInputList())
                    .orElse(new String[0]);
        }

        public String getSourcesString() {
            return Arrays.asList(getSources()).stream().collect(Collectors.joining(","));
        }

        public String getInputSource() {
            return Optional.ofNullable(mediaInputSource).map(a -> a.inputSource).map(a -> a.value).orElse("");
        }

        public int getInputSourceId() {
            return IntStream.range(0, getSources().length).filter(i -> getSources()[i].equals(getInputSource()))
                    .findFirst().orElse(-1);
        }

        public Number getTvChannel() {
            return Optional.ofNullable(tvChannel).map(a -> a.getChannelNum()).map(j -> parseTVChannel(j)).orElse(-1f);
        }

        public String getTvChannelName() {
            return Optional.ofNullable(tvChannel).map(a -> a.tvChannelName).map(a -> a.value).orElse("");
        }

        public boolean isError() {
            return Optional.ofNullable(error).isPresent();
        }

        public String getError() {
            String code = Optional.ofNullable(error).map(a -> a.code).orElse("");
            String message = Optional.ofNullable(error).map(a -> a.message).orElse("");
            return String.format("%s, %s", code, message);
        }
    }

    @NonNullByDefault({})
    class JSONContent {
        public JSONContent(String capability, String action, String value) {
            Command command = new Command();
            command.capability = capability;
            command.command = action;
            command.arguments = new String[] { value };
            commands = new Command[] { command };
        }

        class Command {
            String component = "main";
            String capability;
            String command;
            String[] arguments;
        }

        Command[] commands;
    }

    @NonNullByDefault({})
    class JSONSubscriptionFilter {
        public JSONSubscriptionFilter(String deviceId) {
            SubscriptionFilter sub = new SubscriptionFilter();
            sub.value = new String[] { deviceId };
            subscriptionFilters = new SubscriptionFilter[] { sub };
        }

        class SubscriptionFilter {
            String type = "DEVICEIDS";
            String[] value;
        }

        SubscriptionFilter[] subscriptionFilters;
        String name = "OpenHAB Subscription";
    }

    @NonNullByDefault({})
    class STSubscription {

        String subscriptionId;
        String registrationUrl;
        String name;
        Integer version;
        SubscriptionFilters[] subscriptionFilters;

        class SubscriptionFilters {
            String type;
            String[] value;
        }

        public String getSubscriptionId() {
            return Optional.ofNullable(subscriptionId).orElse("");
        }

        public String getregistrationUrl() {
            return Optional.ofNullable(registrationUrl).orElse("");
        }
    }

    @NonNullByDefault({})
    class STSSEData {

        long eventTime;
        String eventType;
        DeviceEvent deviceEvent;
        Optional<TvValues> tvInfo = Optional.empty();

        class DeviceEvent {

            String eventId;
            String locationId;
            String ownerId;
            String ownerType;
            String deviceId;
            String componentId;
            String capability; // example "sec.diagnosticsInformation"
            String attribute; // example "dumpType"
            JsonElement value; // example "id" or can be an array
            String valueType;
            boolean stateChange;
            JsonElement data;
            String subscriptionName;

            class ValuesList {
                // Array of supportedInputSourcesMap
                String id;
                String name;

                public String getId() {
                    return Optional.ofNullable(id).orElse("");
                }

                public String getName() {
                    return Optional.ofNullable(name).orElse("");
                }

                @Override
                public String toString() {
                    return Map.of("id", getId(), "name", getName()).toString();
                }
            }

            public String getCapability() {
                return Optional.ofNullable(capability).orElse("");
            }

            public String getAttribute() {
                return Optional.ofNullable(attribute).orElse("");
            }

            public String getValueType() {
                return Optional.ofNullable(valueType).orElse("");
            }

            public List<?> getValuesAsList() throws JsonSyntaxException {
                if ("array".equals(getValueType())) {
                    JsonArray resultArray = Optional.ofNullable((JsonArray) value.getAsJsonArray())
                            .orElse(new JsonArray());
                    try {
                        if (resultArray.get(0) instanceof JsonObject) {
                            // Only for Array of supportedInputSourcesMap
                            ValuesList[] values = new Gson().fromJson(resultArray, ValuesList[].class);
                            List<ValuesList> result = Optional.ofNullable(values).map(a -> Arrays.asList(a))
                                    .orElse(new ArrayList<ValuesList>());
                            return Optional.ofNullable(result).orElse(List.of());
                        } else {
                            List<String> result = new Gson().fromJson(resultArray, ArrayList.class);
                            return Optional.ofNullable(result).orElse(List.of());
                        }
                    } catch (IllegalStateException e) {
                    }
                }
                return List.of();
            }

            public String getValue() {
                if ("string".equals(getValueType())) {
                    return Optional.ofNullable((String) value.getAsString()).orElse("");
                }
                return "";
            }
        }

        public void setTvInfo(Optional<TvValues> tvInfo) {
            this.tvInfo = tvInfo;
        }

        public boolean getCapabilityAttribute(String capability, String attribute) {
            return Optional.ofNullable(deviceEvent).map(a -> a.getCapability()).filter(a -> a.equals(capability))
                    .isPresent()
                    && Optional.ofNullable(deviceEvent).map(a -> a.getAttribute()).filter(a -> a.equals(attribute))
                            .isPresent();
        }

        public String getSwitch() {
            if (getCapabilityAttribute("switch", "switch")) {
                return Optional.ofNullable(deviceEvent).map(a -> a.getValue()).orElse("");
            }
            return "";
        }

        public String getInputSource() {
            if (getCapabilityAttribute("mediaInputSource", "inputSource")
                    || getCapabilityAttribute("samsungvd.mediaInputSource", "inputSource")) {
                return Optional.ofNullable(deviceEvent).map(a -> a.getValue()).orElse("");
            }
            return "";
        }

        public String[] getInputSourceList() {
            if (getCapabilityAttribute("mediaInputSource", "supportedInputSources")) {
                return deviceEvent.getValuesAsList().toArray(String[]::new);
            }
            return new String[0];
        }

        public List<?> getInputSourceMapList() {
            if (getCapabilityAttribute("samsungvd.mediaInputSource", "supportedInputSourcesMap")) {
                return deviceEvent.getValuesAsList();
            }
            return List.of();
        }

        public int getInputSourceId() {
            return this.tvInfo.map(t -> IntStream.range(0, t.getSources().length)
                    .filter(i -> t.getSources()[i].equals(getInputSource())).findFirst().orElse(-1)).orElse(-1);
        }

        public Number getTvChannel() {
            if (getCapabilityAttribute("tvChannel", "tvChannel")) {
                return Optional.ofNullable(deviceEvent).map(a -> a.getValue()).map(j -> parseTVChannel(j)).orElse(-1f);
            }
            return -1f;
        }

        public String getTvChannelName() {
            if (getCapabilityAttribute("tvChannel", "tvChannelName")) {
                return Optional.ofNullable(deviceEvent).map(a -> a.getValue()).orElse("");
            }
            return "";
        }
    }

    public static Number parseTVChannel(@Nullable String channel) {
        try {
            return (channel == null || channel.isBlank()) ? -1f
                    : Float.parseFloat(
                            channel.replaceAll("\\D+", ".").replaceFirst("^\\D*((\\d+\\.\\d+)|(\\d+)).*", "$1"));
        } catch (NumberFormatException ignore) {
        }
        return -1f;
    }

    public void updateTV() {
        if (!tvInfo.isPresent()) {
            fetchdata();
            tvInfo.ifPresent(t -> {
                updateState(CHANNEL_NAME, t.getTvChannelName());
                updateState(CHANNEL, t.getTvChannel());
                updateState(SOURCE_NAME, t.getInputSource());
                updateState(SOURCE_ID, t.getInputSourceId());
            });
        }
    }

    /**
     * Smartthings API HTTP interface
     * Currently rate limited to 350 requests/minute
     *
     * @param method the method "GET" or "POST"
     * @param uri as a URI
     * @param content to POST (or null)
     * @return response
     */
    public Optional<String> sendUrl(HttpMethod method, URI uri, @Nullable InputStream content) throws IOException {
        // need to add header "Authorization":"Bearer " + apiKey;
        Properties headers = new Properties();
        headers.put("Authorization", "Bearer " + this.apiKey);
        logger.trace("{}: Sending {}", host, uri.toURL().toString());
        Optional<String> response = Optional.ofNullable(HttpUtil.executeUrl(method.toString(), uri.toURL().toString(),
                headers, content, "application/json", TIMEOUT));
        if (!response.isPresent()) {
            throw new IOException("No Data");
        }
        response.ifPresent(r -> logger.trace("{}: Got response: {}", host, r));
        response.filter(r -> !r.startsWith("{")).ifPresent(r -> logger.debug("{}: Got response: {}", host, r));
        return response;
    }

    /**
     * Smartthings API HTTP getter
     * Currently rate limited to 350 requests/minute
     *
     * @param value the query to send
     * @return tvValues
     */
    public synchronized Optional<TvValues> fetchTVProperties(String value) {
        if (apiKey.isBlank()) {
            return Optional.empty();
        }
        Optional<TvValues> tvValues = Optional.empty();
        try {
            String api = API_ENDPOINT_V1 + ((deviceId.isBlank()) ? "" : "devices/") + deviceId + value;
            URI uri = new URI("https", null, SMARTTHINGS_URL, 443, api, null, null);
            Optional<String> response = sendUrl(HttpMethod.GET, uri, null);
            tvValues = response.map(r -> new Gson().fromJson(r, TvValues.class));
            if (!tvValues.isPresent()) {
                throw new IOException("No Data - is DeviceID correct?");
            }
            tvValues.filter(t -> t.isError()).ifPresent(t -> logger.debug("{}: Error: {}", host, t.getError()));
            errorCount = 0;
        } catch (JsonSyntaxException | URISyntaxException | IOException e) {
            logger.debug("{}: Cannot connect to Smartthings Cloud: {}", host, e.getMessage());
            if (errorCount++ > MAX_ERRORS) {
                logger.warn("{}: Too many connection errors, disabling SmartThings", host);
                stop();
            }
        }
        return tvValues;
    }

    /**
     * Smartthings API HTTP setter
     * Currently rate limited to 350 requests/minute
     *
     * @param capability eg mediaInputSource
     * @param command eg setInputSource
     * @param value from acceptible list eg HDMI1, digitalTv, AM etc
     * @return boolean true if successful
     */
    public synchronized boolean setTVProperties(String capability, String command, String value) {
        if (apiKey.isBlank() || deviceId.isBlank()) {
            return false;
        }
        Optional<String> response = Optional.empty();
        try {
            String contentString = new Gson().toJson(new JSONContent(capability, command, value));
            logger.trace("{}: content: {}", host, contentString);
            InputStream content = new ByteArrayInputStream(contentString.getBytes());
            String api = API_ENDPOINT_V1 + "devices/" + deviceId + COMMAND;
            URI uri = new URI("https", null, SMARTTHINGS_URL, 443, api, null, null);
            response = sendUrl(HttpMethod.POST, uri, content);
        } catch (JsonSyntaxException | URISyntaxException | IOException e) {
            logger.debug("{}: Send Command to Smartthings Cloud failed: {}", host, e.getMessage());
        }
        return response.map(r -> r.contains("ACCEPTED") || r.contains("COMPLETED")).orElse(false);
    }

    /**
     * Smartthings API Subscription
     * Retrieves the Smartthings API Subscription from a remote service, performing an API call
     *
     * @return stSub
     */
    public synchronized Optional<STSubscription> smartthingsSubscription() {
        if (apiKey.isBlank() || deviceId.isBlank()) {
            return Optional.empty();
        }
        Optional<STSubscription> stSub = Optional.empty();
        try {
            logger.info("{}: SSE Creating Smartthings Subscription", host);
            String contentString = new Gson().toJson(new JSONSubscriptionFilter(deviceId));
            logger.trace("{}: subscription: {}", host, contentString);
            InputStream subscriptionFilter = new ByteArrayInputStream(contentString.getBytes());
            URI uri = new URI("https", null, SMARTTHINGS_URL, 443, "/subscriptions", null, null);
            Optional<String> response = sendUrl(HttpMethod.POST, uri, subscriptionFilter);
            stSub = response.map(r -> new Gson().fromJson(r, STSubscription.class));
            if (!stSub.isPresent()) {
                throw new IOException("No Data - is DeviceID correct?");
            }
        } catch (JsonSyntaxException | URISyntaxException | IOException e) {
            logger.warn("{}: SSE Subscription to Smartthings Cloud failed: {}", host, e.getMessage());
        }
        return stSub;
    }

    public synchronized void startSSE() {
        if (!subscriptionRunning) {
            logger.trace("{}: SSE Starting job", host);
            subscription = smartthingsSubscription();
            logger.trace("{}: SSE got subscription ID: {}", host,
                    subscription.map(a -> a.getSubscriptionId()).orElse("None"));
            if (!subscription.map(a -> a.getSubscriptionId()).orElse("").isBlank()) {
                receiveSSEEvents();
            }
        }
    }

    public void stopSSE() {
        handlerWrapper.ifPresent(a -> {
            a.cancel();
            logger.trace("{}: SSE Stopping job", host);
            handlerWrapper = Optional.empty();
            subscriptionRunning = false;
        });
    }

    /**
     * SubscriberWrapper needed to make async SSE stream cancelable
     *
     */
    @NonNullByDefault({})
    private static class SubscriberWrapper implements BodySubscriber<Void> {
        private final CountDownLatch latch;
        private final BodySubscriber<Void> subscriber;
        private Subscription subscription;

        private SubscriberWrapper(BodySubscriber<Void> subscriber, CountDownLatch latch) {
            this.subscriber = subscriber;
            this.latch = latch;
        }

        @Override
        public CompletionStage<Void> getBody() {
            return subscriber.getBody();
        }

        @Override
        public void onSubscribe(Subscription subscription) {
            subscriber.onSubscribe(subscription);
            this.subscription = subscription;
            latch.countDown();
        }

        @Override
        public void onNext(List<ByteBuffer> item) {
            subscriber.onNext(item);
        }

        @Override
        public void onError(Throwable throwable) {
            subscriber.onError(throwable);
        }

        @Override
        public void onComplete() {
            subscriber.onComplete();
        }

        public void cancel() {
            subscription.cancel();
        }
    }

    @NonNullByDefault({})
    private static class BodyHandlerWrapper implements BodyHandler<Void> {
        private final CountDownLatch latch = new CountDownLatch(1);
        private final BodyHandler<Void> handler;
        private SubscriberWrapper subscriberWrapper;
        private int statusCode = -1;

        private BodyHandlerWrapper(BodyHandler<Void> handler) {
            this.handler = handler;
        }

        @Override
        public BodySubscriber<Void> apply(ResponseInfo responseInfo) {
            subscriberWrapper = new SubscriberWrapper(handler.apply(responseInfo), latch);
            this.statusCode = responseInfo.statusCode();
            return subscriberWrapper;
        }

        public void waitForEvent(boolean cancel) {
            try {
                CompletableFuture.runAsync(() -> {
                    try {
                        latch.await();
                        if (cancel) {
                            subscriberWrapper.cancel();
                        }
                    } catch (InterruptedException ignore) {
                    }
                }).get(2, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException ignore) {
            }
        }

        public int getStatusCode() {
            waitForEvent(false);
            return statusCode;
        }

        public void cancel() {
            waitForEvent(true);
        }
    }

    public void receiveSSEEvents() {
        subscription.ifPresent(sub -> {
            updateTV();
            try {
                URI uri = new URI(sub.getregistrationUrl());
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(2)).GET()
                        .header("Authorization", "Bearer " + this.apiKey).build();
                handlerWrapper = Optional.ofNullable(
                        new BodyHandlerWrapper(HttpResponse.BodyHandlers.ofByteArrayConsumer(b -> processSSEEvent(b))));
                handlerWrapper.ifPresent(h -> {
                    client.sendAsync(request, h);
                });
                logger.debug("{}: SSE job {}", host, checkResponseCode() ? "Started" : "Failed");
            } catch (URISyntaxException e) {
                logger.warn("{}: SSE URI Exception: {}", host, e.getMessage());
            }
        });
    }

    boolean checkResponseCode() {
        int respCode = handlerWrapper.map(a -> a.getStatusCode()).orElse(-1);
        logger.trace("{}: SSE GOT Response Code: {}", host, respCode);
        subscriptionRunning = (respCode == 200);
        return subscriptionRunning;
    }

    Map<String, String> bytesToMap(byte[] bytes) {
        String s = new String(bytes, StandardCharsets.UTF_8);
        // logger.trace("{}: SSE received: {}", host, s);
        Map<String, String> properties = new HashMap<String, String>();
        String[] pairs = s.split("\r?\n");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            properties.put(kv[0].trim(), kv[1].trim());
        }
        logger.trace("{}: SSE received: {}", host, properties);
        updateTV();
        return properties;
    }

    synchronized void processSSEEvent(Optional<byte[]> bytes) {
        bytes.ifPresent(b -> {
            Map<String, String> properties = bytesToMap(b);
            String rawData = properties.getOrDefault("data", "none");
            String event = properties.getOrDefault("event", "none");
            // logger.trace("{}: SSE Decoding event: {}", host, event);
            switch (event) {
                case "CONTROL_EVENT":
                    subscriptionRunning = "welcome".equals(rawData);
                    if (!subscriptionRunning) {
                        logger.trace("{}: SSE Subscription ended", host);
                        startSSE();
                    }
                    break;
                case "DEVICE_EVENT":
                    try {
                        // decode json here
                        Optional<STSSEData> data = Optional.ofNullable(new Gson().fromJson(rawData, STSSEData.class));
                        data.ifPresentOrElse(d -> {
                            d.setTvInfo(tvInfo);
                            String[] inputList = d.getInputSourceList();
                            if (inputList.length > 0) {
                                logger.trace("{}: SSE Got input source list: {}", host, Arrays.asList(inputList));
                                tvInfo.ifPresent(a -> a.updateSupportedInputSources(inputList));
                            }
                            String inputSource = d.getInputSource();
                            if (!inputSource.isBlank()) {
                                updateState(SOURCE_NAME, inputSource);
                                int sourceId = d.getInputSourceId();
                                logger.trace("{}: SSE Got input source: {} ID: {}", host, inputSource, sourceId);
                                updateState(SOURCE_ID, sourceId);
                            }
                            Number tvChannel = d.getTvChannel();
                            if (tvChannel.intValue() != -1) {
                                logger.trace("{}: SSE Got TV Channel: {}", host, tvChannel);
                                updateState(CHANNEL, tvChannel);
                            }
                            String tvChannelName = d.getTvChannelName();
                            if (!tvChannelName.isBlank()) {
                                logger.trace("{}: SSE Got TV Channel Name: {}", host, tvChannelName);
                                updateState(CHANNEL_NAME, tvChannelName);
                            }
                            String power = d.getSwitch();
                            if (!power.isBlank()) {
                                logger.debug("{}: SSE Got TV Power: {}", host, power);
                                if ("on".equals(power)) {
                                    // handler.putOnline(); // ignore on event for now
                                } else {
                                    // handler.setOffline(); // ignore off event for now
                                }
                            }
                        }, () -> logger.warn("{}: SSE Received NULL data", host));
                    } catch (JsonSyntaxException e) {
                        logger.warn("{}: SmartThingsApiService: Error ({}) in message: {}", host, e.getMessage(),
                                rawData);
                    }
                    break;
                default:
                    logger.trace("{}: SSE not handling event: {}", host, event);
                    break;
            }
        });
    }

    private boolean updateDeviceID(TvValues.Items item) {
        this.deviceId = item.getDeviceId();
        logger.debug("{}: found {} device, adding device id {}", host, item.getName(), deviceId);
        handler.putConfig(SMARTTHINGS_DEVICEID, deviceId);
        prevUpdate = 0;
        return true;
    }

    public boolean fetchdata() {
        if (System.currentTimeMillis() >= prevUpdate + RATE_LIMIT) {
            if (deviceId.isBlank()) {
                tvInfo = fetchTVProperties(DEVICES);
                boolean found = false;
                if (tvInfo.isPresent()) {
                    TvValues t = tvInfo.get();
                    switch (t.getItems().length) {
                        case 0:
                        case 1:
                            logger.warn("{}: No devices found - please add your TV to the Smartthings app", host);
                            break;
                        case 2:
                            found = Arrays.asList(t.getItems()).stream().filter(a -> "Samsung TV".equals(a.getName()))
                                    .map(a -> updateDeviceID(a)).findFirst().orElse(false);
                            break;
                        default:
                            logger.warn("{}: No device Id selected, please enter one of the following:", host);
                            Arrays.asList(t.getItems()).stream().forEach(a -> logger.info("{}: '{}' : {}({})", host,
                                    a.getDeviceId(), a.getName(), a.getLabel()));
                    }
                }
                if (found) {
                    return fetchdata();
                } else {
                    stop();
                    return false;
                }
            }
            tvInfo = fetchTVProperties(COMPONENTS);
            prevUpdate = System.currentTimeMillis();
        }
        return (tvInfo.isPresent());
    }

    @Override
    public void start() {
        online = true;
        errorCount = 0;
        if (subscriptionEnabled) {
            startSSE();
        }
    }

    @Override
    public void stop() {
        online = false;
        stopSSE();
    }

    @Override
    public void clearCache() {
        stateMap.clear();
        start();
    }

    @Override
    public boolean isUpnp() {
        return false;
    }

    @Override
    public boolean checkConnection() {
        return online;
    }

    @Override
    public boolean handleCommand(String channel, Command command) {
        logger.trace("{}: Received channel: {}, command: {}", host, channel, command);
        if (!checkConnection()) {
            logger.trace("{}: Smartthings offline", host);
            return false;
        }

        if (fetchdata()) {
            return tvInfo.map(t -> {
                boolean result = false;
                if (command == RefreshType.REFRESH) {
                    switch (channel) {
                        case CHANNEL_NAME:
                            updateState(CHANNEL_NAME, t.getTvChannelName());
                            break;
                        case CHANNEL:
                            updateState(CHANNEL, t.getTvChannel());
                            break;
                        case SOURCE_ID:
                        case SOURCE_NAME:
                            updateState(SOURCE_NAME, t.getInputSource());
                            updateState(SOURCE_ID, t.getInputSourceId());
                            break;
                        default:
                            break;
                    }
                    return true;
                }

                switch (channel) {
                    case SOURCE_ID:
                        if (command instanceof DecimalType commandAsDecimalType) {
                            int val = commandAsDecimalType.intValue();
                            if (val >= 0 && val < t.getSources().length) {
                                result = setSourceName(t.getSources()[val]);
                            } else {
                                logger.warn("{}: Invalid source ID: {}, acceptable: 0..{}", host, command,
                                        t.getSources().length);
                            }
                        }
                        break;
                    case SOURCE_NAME:
                        if (command instanceof StringType) {
                            if (t.getSourcesString().contains(command.toString()) || t.getSourcesString().isBlank()) {
                                result = setSourceName(command.toString());
                            } else {
                                logger.warn("{}: Invalid source Name: {}, acceptable: {}", host, command,
                                        t.getSourcesString());
                            }
                        }
                        break;
                    default:
                        logger.warn("{}: Samsung TV doesn't support transmitting for channel '{}'", host, channel);
                }
                if (!result) {
                    logger.warn("{}: Smartthings: wrong command type {} channel {}", host, command, channel);
                }
                return result;
            }).orElse(false);
        }
        return false;
    }

    private void updateState(String channel, Object value) {
        if (!stateMap.getOrDefault(channel, "None").equals(value)) {
            switch (channel) {
                case CHANNEL:
                case SOURCE_ID:
                    handler.valueReceived(channel, new DecimalType((Number) value));
                    break;
                default:
                    handler.valueReceived(channel, new StringType((String) value));
                    break;
            }
            stateMap.put(channel, value);
        } else {
            logger.trace("{}: Value '{}' for {} hasn't changed, ignoring update", host, value, channel);
        }
    }

    private boolean setSourceName(String value) {
        return setTVProperties("mediaInputSource", "setInputSource", value);
    }
}
