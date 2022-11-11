/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.http.internal;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.client.util.DigestAuthentication;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.http.internal.config.HttpChannelConfig;
import org.openhab.binding.http.internal.config.HttpChannelMode;
import org.openhab.binding.http.internal.config.HttpThingConfig;
import org.openhab.binding.http.internal.converter.AbstractTransformingItemConverter;
import org.openhab.binding.http.internal.converter.ColorItemConverter;
import org.openhab.binding.http.internal.converter.DimmerItemConverter;
import org.openhab.binding.http.internal.converter.FixedValueMappingItemConverter;
import org.openhab.binding.http.internal.converter.GenericItemConverter;
import org.openhab.binding.http.internal.converter.ImageItemConverter;
import org.openhab.binding.http.internal.converter.ItemValueConverter;
import org.openhab.binding.http.internal.converter.NumberItemConverter;
import org.openhab.binding.http.internal.converter.PlayerItemConverter;
import org.openhab.binding.http.internal.converter.RollershutterItemConverter;
import org.openhab.binding.http.internal.http.Content;
import org.openhab.binding.http.internal.http.HttpAuthException;
import org.openhab.binding.http.internal.http.HttpResponseListener;
import org.openhab.binding.http.internal.http.RateLimitedHttpClient;
import org.openhab.binding.http.internal.http.RefreshingUrlCache;
import org.openhab.binding.http.internal.transform.ValueTransformationProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HttpThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class HttpThingHandler extends BaseThingHandler {
    private static final Set<Character> URL_PART_DELIMITER = Set.of('/', '?', '&');

    private final Logger logger = LoggerFactory.getLogger(HttpThingHandler.class);
    private final ValueTransformationProvider valueTransformationProvider;
    private final HttpClientProvider httpClientProvider;
    private HttpClient httpClient;
    private RateLimitedHttpClient rateLimitedHttpClient;
    private final HttpDynamicStateDescriptionProvider httpDynamicStateDescriptionProvider;

    private HttpThingConfig config = new HttpThingConfig();
    private final Map<String, RefreshingUrlCache> urlHandlers = new HashMap<>();
    private final Map<ChannelUID, ItemValueConverter> channels = new HashMap<>();
    private final Map<ChannelUID, String> channelUrls = new HashMap<>();

    public HttpThingHandler(Thing thing, HttpClientProvider httpClientProvider,
            ValueTransformationProvider valueTransformationProvider,
            HttpDynamicStateDescriptionProvider httpDynamicStateDescriptionProvider) {
        super(thing);
        this.httpClientProvider = httpClientProvider;
        this.httpClient = httpClientProvider.getSecureClient();
        this.rateLimitedHttpClient = new RateLimitedHttpClient(httpClient, scheduler);
        this.valueTransformationProvider = valueTransformationProvider;
        this.httpDynamicStateDescriptionProvider = httpDynamicStateDescriptionProvider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        ItemValueConverter itemValueConverter = channels.get(channelUID);
        if (itemValueConverter == null) {
            logger.warn("Cannot find channel implementation for channel {}.", channelUID);
            return;
        }

        if (command instanceof RefreshType) {
            String key = channelUrls.get(channelUID);
            if (key != null) {
                RefreshingUrlCache refreshingUrlCache = urlHandlers.get(key);
                if (refreshingUrlCache != null) {
                    try {
                        refreshingUrlCache.get().ifPresent(itemValueConverter::process);
                    } catch (IllegalArgumentException | IllegalStateException e) {
                        logger.warn("Failed processing REFRESH command for channel {}: {}", channelUID, e.getMessage());
                    }
                }
            }
        } else {
            try {
                itemValueConverter.send(command);
            } catch (IllegalArgumentException e) {
                logger.warn("Failed to convert command '{}' to channel '{}' for sending", command, channelUID);
            } catch (IllegalStateException e) {
                logger.debug("Writing to read-only channel {} not permitted", channelUID);
            }
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(HttpThingConfig.class);

        if (config.baseURL.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Parameter baseURL must not be empty!");
            return;
        }

        // check SSL handling and initialize client
        if (config.ignoreSSLErrors) {
            logger.info("Using the insecure client for thing '{}'.", thing.getUID());
            httpClient = httpClientProvider.getInsecureClient();
        } else {
            logger.info("Using the secure client for thing '{}'.", thing.getUID());
            httpClient = httpClientProvider.getSecureClient();
        }
        rateLimitedHttpClient.setHttpClient(httpClient);
        rateLimitedHttpClient.setDelay(config.delay);

        int channelCount = thing.getChannels().size();
        if (channelCount * config.delay > config.refresh * 1000) {
            // this should prevent the rate limit queue from filling up
            config.refresh = (channelCount * config.delay) / 1000 + 1;
            logger.warn(
                    "{} channels in thing {} with a delay of {} incompatible with the configured refresh time. Refresh-Time increased to the minimum of {}",
                    channelCount, thing.getUID(), config.delay, config.refresh);
        }

        // remove empty headers
        config.headers.removeIf(String::isBlank);

        // configure authentication
        if (!config.username.isEmpty()) {
            try {
                AuthenticationStore authStore = httpClient.getAuthenticationStore();
                URI uri = new URI(config.baseURL);
                switch (config.authMode) {
                    case BASIC_PREEMPTIVE:
                        config.headers.add("Authorization=Basic " + Base64.getEncoder()
                                .encodeToString((config.username + ":" + config.password).getBytes()));
                        logger.debug("Preemptive Basic Authentication configured for thing '{}'", thing.getUID());
                        break;
                    case BASIC:
                        authStore.addAuthentication(new BasicAuthentication(uri, Authentication.ANY_REALM,
                                config.username, config.password));
                        logger.debug("Basic Authentication configured for thing '{}'", thing.getUID());
                        break;
                    case DIGEST:
                        authStore.addAuthentication(new DigestAuthentication(uri, Authentication.ANY_REALM,
                                config.username, config.password));
                        logger.debug("Digest Authentication configured for thing '{}'", thing.getUID());
                        break;
                    default:
                        logger.warn("Unknown authentication method '{}' for thing '{}'", config.authMode,
                                thing.getUID());
                }
            } catch (URISyntaxException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "failed to create authentication: baseUrl is invalid");
            }
        } else {
            logger.debug("No authentication configured for thing '{}'", thing.getUID());
        }

        // create channels
        thing.getChannels().forEach(this::createChannel);

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        // stop update tasks
        urlHandlers.values().forEach(RefreshingUrlCache::stop);
        rateLimitedHttpClient.shutdown();

        // clear lists
        urlHandlers.clear();
        channels.clear();
        channelUrls.clear();

        // remove state descriptions
        httpDynamicStateDescriptionProvider.removeDescriptionsForThing(thing.getUID());

        super.dispose();
    }

    /**
     * create all necessary information to handle every channel
     *
     * @param channel a thing channel
     */
    private void createChannel(Channel channel) {
        ChannelUID channelUID = channel.getUID();
        HttpChannelConfig channelConfig = channel.getConfiguration().as(HttpChannelConfig.class);

        String stateUrl = concatenateUrlParts(config.baseURL, channelConfig.stateExtension);
        String commandUrl = channelConfig.commandExtension == null ? stateUrl
                : concatenateUrlParts(config.baseURL, channelConfig.commandExtension);

        String acceptedItemType = channel.getAcceptedItemType();
        if (acceptedItemType == null) {
            logger.warn("Cannot determine item-type for channel '{}'", channelUID);
            return;
        }

        ItemValueConverter itemValueConverter;
        switch (acceptedItemType) {
            case "Color":
                itemValueConverter = createItemConverter(ColorItemConverter::new, commandUrl, channelUID,
                        channelConfig);
                break;
            case "DateTime":
                itemValueConverter = createGenericItemConverter(commandUrl, channelUID, channelConfig,
                        DateTimeType::new);
                break;
            case "Dimmer":
                itemValueConverter = createItemConverter(DimmerItemConverter::new, commandUrl, channelUID,
                        channelConfig);
                break;
            case "Contact":
            case "Switch":
                itemValueConverter = createItemConverter(FixedValueMappingItemConverter::new, commandUrl, channelUID,
                        channelConfig);
                break;
            case "Image":
                itemValueConverter = new ImageItemConverter(state -> updateState(channelUID, state));
                break;
            case "Location":
                itemValueConverter = createGenericItemConverter(commandUrl, channelUID, channelConfig, PointType::new);
                break;
            case "Number":
                itemValueConverter = createItemConverter(NumberItemConverter::new, commandUrl, channelUID,
                        channelConfig);
                break;
            case "Player":
                itemValueConverter = createItemConverter(PlayerItemConverter::new, commandUrl, channelUID,
                        channelConfig);
                break;
            case "Rollershutter":
                itemValueConverter = createItemConverter(RollershutterItemConverter::new, commandUrl, channelUID,
                        channelConfig);
                break;
            case "String":
                itemValueConverter = createGenericItemConverter(commandUrl, channelUID, channelConfig, StringType::new);
                break;
            default:
                logger.warn("Unsupported item-type '{}'", channel.getAcceptedItemType());
                return;
        }

        channels.put(channelUID, itemValueConverter);
        if (channelConfig.mode != HttpChannelMode.WRITEONLY) {
            // we need a key consisting of stateContent and URL, only if both are equal, we can use the same cache
            String key = channelConfig.stateContent + "$" + stateUrl;
            channelUrls.put(channelUID, key);
            urlHandlers
                    .computeIfAbsent(key,
                            k -> new RefreshingUrlCache(scheduler, rateLimitedHttpClient, stateUrl,
                                    channelConfig.escapedUrl, config, channelConfig.stateContent))
                    .addConsumer(itemValueConverter::process);
        }

        StateDescription stateDescription = StateDescriptionFragmentBuilder.create()
                .withReadOnly(channelConfig.mode == HttpChannelMode.READONLY).build().toStateDescription();
        if (stateDescription != null) {
            // if the state description is not available, we don't need to add it
            httpDynamicStateDescriptionProvider.setDescription(channelUID, stateDescription);
        }
    }

    private void sendHttpValue(String commandUrl, boolean escapedUrl, String command) {
        sendHttpValue(commandUrl, escapedUrl, command, false);
    }

    private void sendHttpValue(String commandUrl, boolean escapedUrl, String command, boolean isRetry) {
        try {
            // format URL
            String url = String.format(commandUrl, new Date(), command);
            URI uri = escapedUrl ? new URI(url) : Util.uriFromString(url);

            // build request
            Request request = httpClient.newRequest(uri).timeout(config.timeout, TimeUnit.MILLISECONDS)
                    .method(config.commandMethod);
            if (config.commandMethod != HttpMethod.GET) {
                final String contentType = config.contentType;
                if (contentType != null) {
                    request.content(new StringContentProvider(command), contentType);
                } else {
                    request.content(new StringContentProvider(command));
                }
            }

            config.headers.forEach(header -> {
                String[] keyValuePair = header.split("=", 2);
                if (keyValuePair.length == 2) {
                    request.header(keyValuePair[0], keyValuePair[1]);
                } else {
                    logger.warn("Splitting header '{}' failed. No '=' was found. Ignoring", header);
                }
            });

            if (logger.isTraceEnabled()) {
                logger.trace("Sending to '{}': {}", uri, Util.requestToLogString(request));
            }

            CompletableFuture<@Nullable Content> f = new CompletableFuture<>();
            f.exceptionally(e -> {
                if (e instanceof HttpAuthException) {
                    if (isRetry) {
                        logger.warn("Retry after authentication failure failed again for '{}', failing here", uri);
                    } else {
                        AuthenticationStore authStore = httpClient.getAuthenticationStore();
                        Authentication.Result authResult = authStore.findAuthenticationResult(uri);
                        if (authResult != null) {
                            authStore.removeAuthenticationResult(authResult);
                            logger.debug("Cleared authentication result for '{}', retrying immediately", uri);
                            sendHttpValue(commandUrl, escapedUrl, command, true);
                        } else {
                            logger.warn("Could not find authentication result for '{}', failing here", uri);
                        }
                    }
                }
                return null;
            });
            request.send(new HttpResponseListener(f, null, config.bufferSize));
        } catch (IllegalArgumentException | URISyntaxException | MalformedURLException e) {
            logger.warn("Creating request for '{}' failed: {}", commandUrl, e.getMessage());
        }
    }

    private String concatenateUrlParts(String baseUrl, @Nullable String extension) {
        if (extension != null && !extension.isEmpty()) {
            if (!URL_PART_DELIMITER.contains(baseUrl.charAt(baseUrl.length() - 1))
                    && !URL_PART_DELIMITER.contains(extension.charAt(0))) {
                return baseUrl + "/" + extension;
            } else {
                return baseUrl + extension;
            }
        } else {
            return baseUrl;
        }
    }

    private ItemValueConverter createItemConverter(AbstractTransformingItemConverter.Factory factory, String commandUrl,
            ChannelUID channelUID, HttpChannelConfig channelConfig) {
        return factory.create(state -> updateState(channelUID, state), command -> postCommand(channelUID, command),
                command -> sendHttpValue(commandUrl, channelConfig.escapedUrl, command),
                valueTransformationProvider.getValueTransformation(channelConfig.stateTransformation),
                valueTransformationProvider.getValueTransformation(channelConfig.commandTransformation), channelConfig);
    }

    private ItemValueConverter createGenericItemConverter(String commandUrl, ChannelUID channelUID,
            HttpChannelConfig channelConfig, Function<String, State> toState) {
        AbstractTransformingItemConverter.Factory factory = (state, command, value, stateTrans, commandTrans,
                config) -> new GenericItemConverter(toState, state, command, value, stateTrans, commandTrans, config);
        return createItemConverter(factory, commandUrl, channelUID, channelConfig);
    }
}
