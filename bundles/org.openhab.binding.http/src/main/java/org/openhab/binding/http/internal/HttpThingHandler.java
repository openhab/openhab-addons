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
package org.openhab.binding.http.internal;

import static org.openhab.binding.http.internal.HttpBindingConstants.CHANNEL_LAST_FAILURE;
import static org.openhab.binding.http.internal.HttpBindingConstants.CHANNEL_LAST_SUCCESS;
import static org.openhab.binding.http.internal.HttpBindingConstants.REQUEST_DATE_TIME_CHANNELTYPE_UID;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.client.util.DigestAuthentication;
import org.openhab.binding.http.internal.config.HttpChannelConfig;
import org.openhab.binding.http.internal.config.HttpThingConfig;
import org.openhab.binding.http.internal.http.HttpAuthException;
import org.openhab.binding.http.internal.http.HttpResponseListener;
import org.openhab.binding.http.internal.http.HttpStatusListener;
import org.openhab.binding.http.internal.http.RateLimitedHttpClient;
import org.openhab.binding.http.internal.http.RefreshingUrlCache;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.generic.ChannelHandler;
import org.openhab.core.thing.binding.generic.ChannelHandlerContent;
import org.openhab.core.thing.binding.generic.ChannelMode;
import org.openhab.core.thing.binding.generic.ChannelTransformation;
import org.openhab.core.thing.binding.generic.converter.ColorChannelHandler;
import org.openhab.core.thing.binding.generic.converter.DimmerChannelHandler;
import org.openhab.core.thing.binding.generic.converter.FixedValueMappingChannelHandler;
import org.openhab.core.thing.binding.generic.converter.GenericChannelHandler;
import org.openhab.core.thing.binding.generic.converter.ImageChannelHandler;
import org.openhab.core.thing.binding.generic.converter.NumberChannelHandler;
import org.openhab.core.thing.binding.generic.converter.PlayerChannelHandler;
import org.openhab.core.thing.binding.generic.converter.RollershutterChannelHandler;
import org.openhab.core.thing.internal.binding.generic.converter.AbstractTransformingChannelHandler;
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
public class HttpThingHandler extends BaseThingHandler implements HttpStatusListener {
    private static final Set<Character> URL_PART_DELIMITER = Set.of('/', '?', '&');

    private final Logger logger = LoggerFactory.getLogger(HttpThingHandler.class);
    private final HttpClientProvider httpClientProvider;
    private final RateLimitedHttpClient rateLimitedHttpClient;
    private final HttpDynamicStateDescriptionProvider httpDynamicStateDescriptionProvider;
    private final TimeZoneProvider timeZoneProvider;

    private HttpThingConfig config = new HttpThingConfig();
    private final Map<String, RefreshingUrlCache> urlHandlers = new HashMap<>();
    private final Map<ChannelUID, ChannelHandler> channels = new HashMap<>();
    private final Map<ChannelUID, String> channelUrls = new HashMap<>();

    public HttpThingHandler(Thing thing, HttpClientProvider httpClientProvider,
            HttpDynamicStateDescriptionProvider httpDynamicStateDescriptionProvider,
            TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.httpClientProvider = httpClientProvider;
        this.rateLimitedHttpClient = new RateLimitedHttpClient(httpClientProvider.getSecureClient(), scheduler);
        this.httpDynamicStateDescriptionProvider = httpDynamicStateDescriptionProvider;
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        ChannelHandler itemValueConverter = channels.get(channelUID);
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
                        refreshingUrlCache.get().ifPresentOrElse(itemValueConverter::process, () -> {
                            if (config.strictErrorHandling) {
                                itemValueConverter.process(null);
                            }
                        });
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

        // check protocol is set
        if (!config.baseURL.startsWith("http://") && !config.baseURL.startsWith("https://")) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "baseURL is invalid: protocol not defined.");
            return;
        }

        // check SSL handling and initialize client
        if (config.ignoreSSLErrors) {
            logger.info("Using the insecure client for thing '{}'.", thing.getUID());
            rateLimitedHttpClient.setHttpClient(httpClientProvider.getInsecureClient());
        } else {
            logger.info("Using the secure client for thing '{}'.", thing.getUID());
            rateLimitedHttpClient.setHttpClient(httpClientProvider.getSecureClient());
        }
        rateLimitedHttpClient.setDelay(config.delay);

        // remove empty headers
        config.headers.removeIf(String::isBlank);

        // configure authentication
        try {
            AuthenticationStore authStore = rateLimitedHttpClient.getAuthenticationStore();
            URI uri = new URI(config.baseURL);

            // clear old auths if available
            Authentication.Result authResult = authStore.findAuthenticationResult(uri);
            if (authResult != null) {
                authStore.removeAuthenticationResult(authResult);
            }
            for (String authType : List.of("Basic", "Digest")) {
                Authentication authentication = authStore.findAuthentication(authType, uri, Authentication.ANY_REALM);
                if (authentication != null) {
                    authStore.removeAuthentication(authentication);
                }
            }

            if (!config.username.isEmpty() || !config.password.isEmpty()) {
                switch (config.authMode) {
                    case BASIC_PREEMPTIVE:
                        config.headers.add("Authorization=Basic " + Base64.getEncoder()
                                .encodeToString((config.username + ":" + config.password).getBytes()));
                        logger.debug("Preemptive Basic Authentication configured for thing '{}'", thing.getUID());
                        break;
                    case TOKEN:
                        if (!config.password.isEmpty()) {
                            config.headers.add("Authorization=Bearer " + config.password);
                            logger.debug("Token/Bearer Authentication configured for thing '{}'", thing.getUID());
                        } else {
                            logger.warn("Token/Bearer Authentication configured for thing '{}' but token is empty!",
                                    thing.getUID());
                        }
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
            } else {
                logger.debug("No authentication configured for thing '{}'", thing.getUID());
            }
        } catch (URISyntaxException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Cannot create URI from baseUrl.");
        }
        // create channels
        thing.getChannels().forEach(this::createChannel);

        int urlHandlerCount = urlHandlers.size();
        if (urlHandlerCount * config.delay > config.refresh * 1000) {
            // this should prevent the rate limit queue from filling up
            config.refresh = (urlHandlerCount * config.delay) / 1000 + 1;
            logger.warn(
                    "{} channels in thing {} with a delay of {} incompatible with the configured refresh time. Refresh-Time increased to the minimum of {}",
                    urlHandlerCount, thing.getUID(), config.delay, config.refresh);
        }

        urlHandlers.values().forEach(urlHandler -> urlHandler.start(scheduler, config.refresh));

        updateStatus(ThingStatus.UNKNOWN);
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
        if (REQUEST_DATE_TIME_CHANNELTYPE_UID.equals(channel.getChannelTypeUID())) {
            // do not generate refreshUrls for lastSuccess / lastFailure channels
            return;
        }
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

        ChannelHandler itemValueConverter;
        switch (acceptedItemType) {
            case "Color":
                itemValueConverter = createChannelHandler(ColorChannelHandler::new, commandUrl, channelUID,
                        channelConfig);
                break;
            case "DateTime":
                itemValueConverter = createGenericChannelHandler(commandUrl, channelUID, channelConfig,
                        DateTimeType::new);
                break;
            case "Dimmer":
                itemValueConverter = createChannelHandler(DimmerChannelHandler::new, commandUrl, channelUID,
                        channelConfig);
                break;
            case "Contact":
            case "Switch":
                itemValueConverter = createChannelHandler(FixedValueMappingChannelHandler::new, commandUrl, channelUID,
                        channelConfig);
                break;
            case "Image":
                itemValueConverter = new ImageChannelHandler(state -> updateState(channelUID, state));
                break;
            case "Location":
                itemValueConverter = createGenericChannelHandler(commandUrl, channelUID, channelConfig, PointType::new);
                break;
            case "Number":
                itemValueConverter = createChannelHandler(NumberChannelHandler::new, commandUrl, channelUID,
                        channelConfig);
                break;
            case "Player":
                itemValueConverter = createChannelHandler(PlayerChannelHandler::new, commandUrl, channelUID,
                        channelConfig);
                break;
            case "Rollershutter":
                itemValueConverter = createChannelHandler(RollershutterChannelHandler::new, commandUrl, channelUID,
                        channelConfig);
                break;
            case "String":
                itemValueConverter = createGenericChannelHandler(commandUrl, channelUID, channelConfig,
                        StringType::new);
                break;
            default:
                logger.warn("Unsupported item-type '{}'", channel.getAcceptedItemType());
                return;
        }

        channels.put(channelUID, itemValueConverter);
        if (channelConfig.mode != ChannelMode.WRITEONLY) {
            // we need a key consisting of stateContent and URL, only if both are equal, we can use the same cache
            String key = channelConfig.stateContent + "$" + stateUrl;
            channelUrls.put(channelUID, key);
            Objects.requireNonNull(
                    urlHandlers.computeIfAbsent(key,
                            k -> new RefreshingUrlCache(rateLimitedHttpClient, stateUrl, config,
                                    channelConfig.stateContent, config.contentType, this)))
                    .addConsumer(itemValueConverter::process);
        }

        StateDescription stateDescription = StateDescriptionFragmentBuilder.create()
                .withReadOnly(channelConfig.mode == ChannelMode.READONLY).build().toStateDescription();
        if (stateDescription != null) {
            // if the state description is not available, we don't need to add it
            httpDynamicStateDescriptionProvider.setDescription(channelUID, stateDescription);
        }
    }

    @Override
    public void onHttpError(@Nullable String message) {
        updateState(CHANNEL_LAST_FAILURE, new DateTimeType(Instant.now().atZone(timeZoneProvider.getTimeZone())));
        if (config.strictErrorHandling) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    Objects.requireNonNullElse(message, ""));
        }
    }

    @Override
    public void onHttpSuccess() {
        updateState(CHANNEL_LAST_SUCCESS, new DateTimeType(Instant.now().atZone(timeZoneProvider.getTimeZone())));
        updateStatus(ThingStatus.ONLINE);
    }

    private void sendHttpValue(String commandUrl, String command) {
        sendHttpValue(commandUrl, command, false);
    }

    private void sendHttpValue(String commandUrl, String command, boolean isRetry) {
        try {
            // format URL
            URI uri = Util.uriFromString(Util.wrappedStringFormat(commandUrl, new Date(), command));

            // build request
            rateLimitedHttpClient.newPriorityRequest(uri, config.commandMethod, command, config.contentType)
                    .thenAccept(request -> {
                        request.timeout(config.timeout, TimeUnit.MILLISECONDS);
                        config.getHeaders().forEach(request::header);

                        CompletableFuture<@Nullable ChannelHandlerContent> responseContentFuture = new CompletableFuture<>();
                        responseContentFuture.exceptionally(t -> {
                            if (t instanceof HttpAuthException) {
                                if (isRetry || !rateLimitedHttpClient.reAuth(uri)) {
                                    logger.warn(
                                            "Retry after authentication failure failed again for '{}', failing here",
                                            uri);
                                    onHttpError("Authentication failed");
                                } else {
                                    sendHttpValue(commandUrl, command, true);
                                }
                            }
                            return null;
                        });

                        if (logger.isTraceEnabled()) {
                            logger.trace("Sending to '{}': {}", uri, Util.requestToLogString(request));
                        }

                        request.send(new HttpResponseListener(responseContentFuture, null, config.bufferSize, this));
                    });
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

    private ChannelHandler createChannelHandler(AbstractTransformingChannelHandler.Factory factory, String commandUrl,
            ChannelUID channelUID, HttpChannelConfig channelConfig) {
        return factory.create(state -> updateState(channelUID, state), command -> postCommand(channelUID, command),
                command -> sendHttpValue(commandUrl, command),
                new ChannelTransformation(channelConfig.stateTransformation),
                new ChannelTransformation(channelConfig.commandTransformation), channelConfig);
    }

    private ChannelHandler createGenericChannelHandler(String commandUrl, ChannelUID channelUID,
            HttpChannelConfig channelConfig, Function<String, State> toState) {
        AbstractTransformingChannelHandler.Factory factory = (state, command, value, stateTrans, commandTrans,
                config) -> new GenericChannelHandler(toState, state, command, value, stateTrans, commandTrans, config);
        return createChannelHandler(factory, commandUrl, channelUID, channelConfig);
    }
}
