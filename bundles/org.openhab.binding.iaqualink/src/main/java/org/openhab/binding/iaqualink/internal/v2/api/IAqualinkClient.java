/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.iaqualink.internal.v2.api;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.iaqualink.internal.v2.api.dto.AccountInfo;
import org.openhab.binding.iaqualink.internal.v2.api.dto.Device;
import org.openhab.binding.iaqualink.internal.v2.api.dto.SignIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import software.amazon.awssdk.crt.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.crt.mqtt5.Mqtt5Client;
import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions;
import software.amazon.awssdk.crt.mqtt5.OnAttemptingConnectReturn;
import software.amazon.awssdk.crt.mqtt5.OnConnectionFailureReturn;
import software.amazon.awssdk.crt.mqtt5.OnConnectionSuccessReturn;
import software.amazon.awssdk.crt.mqtt5.OnDisconnectionReturn;
import software.amazon.awssdk.crt.mqtt5.OnStoppedReturn;
import software.amazon.awssdk.crt.mqtt5.PublishResult;
import software.amazon.awssdk.crt.mqtt5.PublishReturn;
import software.amazon.awssdk.crt.mqtt5.QOS;
import software.amazon.awssdk.crt.mqtt5.packets.PublishPacket;
import software.amazon.awssdk.crt.mqtt5.packets.SubscribePacket;
import software.amazon.awssdk.iot.AwsIotMqtt5ClientBuilder;

/**
 * IAqualink HTTP Client
 *
 * The {@link org.openhab.binding.iaqualink.internal.v1.api.IAqualinkClient} provides basic HTTP commands to control and
 * monitor an iAquaLink
 * based system.
 *
 * GSON is used to provide custom deserialization on the JSON results. These results
 * unfortunately are not returned as normalized JSON objects and require complex deserialization
 * handlers.
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@NonNullByDefault
public class IAqualinkClient implements Mqtt5ClientOptions.LifecycleEvents, Mqtt5ClientOptions.PublishEvents {
    private final Logger logger = LoggerFactory.getLogger(IAqualinkClient.class);

    private static final String HEADER_AGENT = "iAqualink/98 CFNetwork/978.0.7 Darwin/18.6.0";
    private static final String HEADER_ACCEPT = "*/*";
    private static final String HEADER_ACCEPT_LANGUAGE = "en-us";
    private static final String HEADER_ACCEPT_ENCODING = "br, gzip, deflate";

    /**
     * Default 'Secret' iAqualink key used by existing clients in the marketplace
     */
    private static final String DEFAULT_SECRET_API_KEY = "cj7iYKjiKxOqiLcN65PffA";

    private static final String AUTH_URL = "https://prod.zodiac-io.com/users/v1/login";
    private static final String DEVICES_URL = "https://prm.iaqualink.net/v2/devices.json";
    private static final String AWS_URL = "a1zi08qpbrtjyq-ats.iot.us-east-1.amazonaws.com";
    private static final String AWS_REGION = "us-east-1";

    private static final String TOPIC_THINGS_SHADOW = "$aws/things/%s/shadow";

    private static final String TOPIC_SUFFIX_GET = "/get";
    private static final String TOPIC_SUFFIX_UPDATE = "/update";

    private static final String TOPIC_SUFFIX_ACCEPTED = "/accepted";
    private static final String TOPIC_SUFFIX_REJECTED = "/rejected";

    /**
     * Connection to AWS IOT
     */
    @Nullable
    private Mqtt5Client mqttClient;

    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private final ScheduledExecutorService scheduler;
    private final HttpClient httpClient;
    @Nullable
    protected AccountInfo accountInfo = null;

    boolean connectCredsFailed = false;

    private final Map<String, IAqualinkDeviceListener> listeners = new HashMap<>();

    private final String apiKey;
    private final String username;
    private final String password;
    private final PropertyStorage propertyStorage;

    public CompletableFuture<PublishResult> publishUpdate(String deviceId, String msg) {
        return getMqttClient().publish(mqttMessage(String.format(TOPIC_THINGS_SHADOW + TOPIC_SUFFIX_UPDATE, deviceId),
                msg.getBytes(StandardCharsets.UTF_8)));
    }

    @SuppressWarnings("serial")
    public static class NotAuthorizedException extends Exception {
        public NotAuthorizedException(String message) {
            super(message);
        }
    }

    public IAqualinkClient(HttpClient httpClient, String username, String password, String apiKey,
            ScheduledExecutorService scheduler, PropertyStorage propertyStorage) {
        this.username = username;
        this.password = password;
        this.httpClient = httpClient;
        this.apiKey = apiKey;
        this.scheduler = scheduler;
        this.propertyStorage = propertyStorage;
    }

    /**
     * Initial login to service
     */
    private AccountInfo login() throws IOException, NotAuthorizedException {

        String signIn = gson.toJson(new SignIn(apiKey, username, password));
        try {
            ContentResponse response = httpClient.newRequest(AUTH_URL).method(HttpMethod.POST)
                    .content(new StringContentProvider(signIn), "application/json").send();
            if (response.getStatus() == HttpStatus.UNAUTHORIZED_401) {
                throw new NotAuthorizedException(response.getReason());
            }
            if (response.getStatus() != HttpStatus.OK_200) {
                throw new IOException(response.getReason());
            }

            return Objects.requireNonNull(gson.fromJson(response.getContentAsString(), AccountInfo.class));
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new IOException(e);
        }
    }

    private AccountInfo getAccountInfo() throws IOException, NotAuthorizedException {
        AccountInfo loaded = loadAccountInfo(false);
        if (loaded.getCredentials().getExpiration().before(new Date())) {
            return refreshAccountInfo();
        } else {
            return loaded;
        }
    }

    private AccountInfo refreshAccountInfo() throws IOException, NotAuthorizedException {
        logger.info("Refreshing authentication tokens");
        return loadAccountInfo(true);
    }

    private AccountInfo loadAccountInfo(boolean relogin) throws IOException, NotAuthorizedException {

        AccountInfo accountInfo = null;
        if (!relogin) {
            // first check cache
            accountInfo = this.accountInfo;
            if (accountInfo != null) {
                return accountInfo;
            }

            // then check storage for tokens
            String credString = propertyStorage.getProperty("credentials");

            if (credString != null) {
                accountInfo = gson.fromJson(credString, AccountInfo.class);
            }
        }

        if (accountInfo == null) { // otherwise login
            accountInfo = login();
            propertyStorage.setProperty("credentials", gson.toJson(accountInfo));
        }

        this.accountInfo = accountInfo;

        return accountInfo;
    }

    /**
     * List all devices (pools) registered to an account
     */
    public Device[] getDevices() throws IOException, NotAuthorizedException {
        AccountInfo accountInfo = getAccountInfo();

        int timestamp = Math.round(((float) new Date().getTime()) / 1000.0f);

        //
        URI uri = UriBuilder.fromUri(DEVICES_URL). //
                queryParam("user_id", accountInfo.getId()).queryParam("timestamp", String.valueOf(timestamp)).build();
        return Objects.requireNonNull(gson.fromJson(getRequest(uri, accountInfo.getId().toString() + "," + timestamp),
                (Type) Device[].class));
    }

    @Override
    public void onAttemptingConnect(@Nullable Mqtt5Client client,
            @Nullable OnAttemptingConnectReturn onAttemptingConnectReturn) {
        logger.trace("MQTT connecting...");
    }

    @Override
    public void onConnectionSuccess(@Nullable Mqtt5Client client,
            @Nullable OnConnectionSuccessReturn onConnectionSuccessReturn) {
        logger.info("MQTT connected");
        connectCredsFailed = false;
    }

    @Override
    public void onConnectionFailure(@Nullable Mqtt5Client client,
            @Nullable OnConnectionFailureReturn onConnectionFailureReturn) {
        logger.error("MQTT connect failed: {}", onConnectionFailureReturn == null ? "unknown"
                : software.amazon.awssdk.crt.CRT.awsErrorString(onConnectionFailureReturn.getErrorCode()));

        if (onConnectionFailureReturn.getErrorCode() == 2065) {
            // possibly due to expired credentials, try to re-login and reconnect
            if (!connectCredsFailed) {
                scheduler.schedule(() -> {
                    try {
                        disconnect();
                        refreshAccountInfo();
                        connectCredsFailed = true;
                        connect();
                    } catch (IOException | NotAuthorizedException | ExecutionException | InterruptedException e) {
                        logger.error("Failed to reconnect!", e);
                    }
                }, 0, TimeUnit.SECONDS);
            } else {
                logger.error("Failed to reconnect, likely due to invalid credentials; disconnecting");
                disconnect();
            }
        } else {
            logger.info("Attempting reconnect");
        }
    }

    @Override
    public void onDisconnection(@Nullable Mqtt5Client client, @Nullable OnDisconnectionReturn onDisconnectionReturn) {
        logger.info("MQTT disconnected");
    }

    @Override
    public void onStopped(@Nullable Mqtt5Client client, @Nullable OnStoppedReturn onStoppedReturn) {
        logger.info("MQTT stopped");
    }

    @Override
    public void onMessageReceived(@Nullable final Mqtt5Client client, @Nullable final PublishReturn publishReturn) {
        scheduler.schedule(() -> {

            if (publishReturn == null) {
                logger.error("MQTT message received null publish return");
                return;
            }

            String topic = publishReturn.getPublishPacket().getTopic();

            String payload = new String(publishReturn.getPublishPacket().getPayload(), StandardCharsets.UTF_8);

            logger.debug("MQTT message received on {}: {}", topic,
                    Arrays.toString(publishReturn.getPublishPacket().getPayload()));

            String[] parts = topic.split("/");
            if (parts.length != 6) {
                logger.error("MQTT message received on unknown topic [1]: {}", topic);
                return;
            }

            String[] partsWithPlaceholder = Arrays.copyOf(parts, parts.length);
            partsWithPlaceholder[2] = "%s";
            String topicWithPlaceholder = String.join("/", partsWithPlaceholder);

            if (!topicWithPlaceholder.startsWith(TOPIC_THINGS_SHADOW)) {
                logger.error("MQTT message received on unknown topic [2]: {}", topic);
                return;
            }

            String deviceId = parts[2];
            IAqualinkDeviceListener listener = listeners.get(deviceId);

            if (listener == null) {
                logger.warn("Listener not found for device: {}", deviceId);
            }

            if (parts[4].equals(TOPIC_SUFFIX_GET.substring(1))) {
                logger.debug("Received get accepted message: {}", payload);
                if (listener != null) { // keep compiler happy
                    listener.onGetAccepted(deviceId, payload);
                }

            } else if (parts[4].equals(TOPIC_SUFFIX_UPDATE.substring(1))) {
                if (parts[5].equals(TOPIC_SUFFIX_ACCEPTED.substring(1))) {
                    logger.debug("Received update accepted message: {}", payload);
                    if (listener != null) { // keep compiler happy
                        listener.onUpdateAccepted(deviceId, payload);
                    }

                } else if (parts[5].equals(TOPIC_SUFFIX_REJECTED.substring(1))) {
                    logger.debug("Received update rejected message: {}", payload);
                    if (listener != null) { // keep compiler happy
                        listener.onUpdateRejected(deviceId, payload);
                    }

                } else {
                    logger.error("MQTT message received on unknown topic [3]: {}", topic);

                }
            } else {
                logger.error("MQTT message received on unknown topic [4]: {}", topic);
                return;
            }
        }, 0, TimeUnit.SECONDS);
    }

    public void connect() throws IOException, ExecutionException, InterruptedException, NotAuthorizedException {
        // Create the MQTT client
        disconnect();

        AccountInfo accountInfo = getAccountInfo();

        StaticCredentialsProvider credentialsProvider = new StaticCredentialsProvider.StaticCredentialsProviderBuilder()
                .withSessionToken(accountInfo.getCredentials().getSessionToken().getBytes())
                .withAccessKeyId(accountInfo.getCredentials().getAccessKeyId().getBytes())
                .withSecretAccessKey(accountInfo.getCredentials().getSecretKey().getBytes()).build();

        AwsIotMqtt5ClientBuilder.WebsocketSigv4Config config = new AwsIotMqtt5ClientBuilder.WebsocketSigv4Config();
        config.credentialsProvider = credentialsProvider;
        config.region = AWS_REGION;

        AwsIotMqtt5ClientBuilder builder = AwsIotMqtt5ClientBuilder
                .newWebsocketMqttBuilderWithSigv4Auth(AWS_URL, config).withLifeCycleEvents(this)
                .withPublishEvents(this);

        Mqtt5Client client = builder.build();
        builder.close();
        // Connect the MQTT client
        client.start();
        mqttClient = client;
    }

    public void disconnect() {
        Mqtt5Client mqtt5Client = this.mqttClient;
        if (mqtt5Client != null) {
            mqtt5Client.stop();
            mqttClient = null;
        }
    }

    private Mqtt5Client getMqttClient() {
        Mqtt5Client rv = this.mqttClient;
        if (rv == null) {
            throw new IllegalStateException("MQTT client not initialized");
        }
        return rv;
    }

    public CompletableFuture<Void> subscribe(Device device, IAqualinkDeviceListener listener) {

        listeners.put(device.getSerialNumber(), listener);

        SubscribePacket subscribePacket = new SubscribePacket.SubscribePacketBuilder()
                .withSubscription(String.format(TOPIC_THINGS_SHADOW + TOPIC_SUFFIX_GET + TOPIC_SUFFIX_ACCEPTED,
                        device.getSerialNumber()), QOS.AT_LEAST_ONCE)
                .withSubscription(String.format(TOPIC_THINGS_SHADOW + TOPIC_SUFFIX_UPDATE + TOPIC_SUFFIX_ACCEPTED,
                        device.getSerialNumber()), QOS.AT_LEAST_ONCE)
                .withSubscription(String.format(TOPIC_THINGS_SHADOW + TOPIC_SUFFIX_UPDATE + TOPIC_SUFFIX_REJECTED,
                        device.getSerialNumber()), QOS.AT_LEAST_ONCE)
                .build();

        return getMqttClient().subscribe(subscribePacket).thenAccept((v) -> {
            v.getReasonCodes().stream().filter(r -> r.getValue() > 100).forEach(r -> {
                logger.error("Failed to subscribe to topic: {}", v.getReasonString());
                throw new RuntimeException("Failed to subscribe to topic: " + v.getReasonString());
            });

            logger.debug("Subscribed to device topics ({})", v.getReasonString());
        });
    }

    public void doGetDevice(Device d) {
        getMqttClient()
                .publish(mqttMessage(String.format(TOPIC_THINGS_SHADOW + TOPIC_SUFFIX_GET, d.getSerialNumber()), null))
                .handleAsync((v, e) -> {
                    if (e != null) {
                        logger.error("Failed to send get request to device: {}", e.getMessage());
                        throw new RuntimeException("Failed to send get request to device: " + e.getMessage());
                    } else {
                        logger.debug("Sent get request to device: {}", v);
                    }

                    return null;
                });
    }

    private PublishPacket mqttMessage(String topic, byte @Nullable [] payload) {
        PublishPacket.PublishPacketBuilder builder = new PublishPacket.PublishPacketBuilder();
        if (payload != null && payload.length > 0) {
            builder.withPayload(payload);
        }
        builder.withQOS(QOS.AT_LEAST_ONCE);
        builder.withPayloadFormat(PublishPacket.PayloadFormatIndicator.BYTES);
        builder.withRetain(false);
        builder.withContentType("application/json");
        builder.withTopic(topic);
        return builder.build();
    }

    private String getRequest(URI uri, @Nullable String sigSource) throws IOException, NotAuthorizedException {
        try {
            AccountInfo accountInfo = this.accountInfo;

            // we don't set the signature if we don't have an account info (yet)
            if (accountInfo != null && sigSource != null) {
                String signature = generateSignatureUsingSecretKey(sigSource, DEFAULT_SECRET_API_KEY);

                uri = UriBuilder.fromUri(uri).queryParam("signature", signature).build();
            }
            logger.trace("Trying {}", uri);

            Request request = httpClient.newRequest(uri).method(HttpMethod.GET) //
                    .agent(HEADER_AGENT) //
                    .header(HttpHeader.ACCEPT_LANGUAGE, HEADER_ACCEPT_LANGUAGE) //
                    .header(HttpHeader.ACCEPT_ENCODING, HEADER_ACCEPT_ENCODING) //
                    .header(HttpHeader.ACCEPT, HEADER_ACCEPT); //

            if (accountInfo != null) {
                request = request.header("api_key", apiKey).header(HttpHeader.AUTHORIZATION,
                        accountInfo.getUserPoolOAuth().getIdToken());
            }

            ContentResponse response = request.send();
            logger.trace("Response {}", response);

            if (response.getStatus() == HttpStatus.UNAUTHORIZED_401) {
                throw new NotAuthorizedException(response.getReason());
            }
            if (response.getStatus() != HttpStatus.OK_200) {
                throw new IOException(response.getReason());
            }
            return response.getContentAsString();
        } catch (InterruptedException | TimeoutException | ExecutionException | JsonParseException e) {
            throw new IOException(e);
        }
    }

    private static String generateSignatureUsingSecretKey(String data, String secretKey) {
        try {
            Mac instance = Mac.getInstance("HmacSHA1");
            instance.init(new SecretKeySpec(secretKey.getBytes(), "HmacSHA1"));
            return toHexString(instance.doFinal(data.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String toHexString(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
