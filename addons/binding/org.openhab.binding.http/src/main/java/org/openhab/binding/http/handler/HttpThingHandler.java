/*
 * Copyright (c) 2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.http.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationHelper;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.http.internal.HttpHandlerFactory;
import org.openhab.binding.http.model.HttpHandlerConfig;
import org.openhab.binding.http.model.HttpHandlerConfig.CommandRequest;
import org.openhab.binding.http.model.HttpHandlerConfig.StateRequest;
import org.openhab.binding.http.model.Transform;
import org.openhab.binding.http.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.openhab.binding.http.HttpBindingConstants.CHANNEL_STATE;
import static org.openhab.binding.http.HttpBindingConstants.DEFAULT_CONTENT_TYPE;
import static org.openhab.binding.http.HttpBindingConstants.MAX_IMAGE_RESPONSE_BODY_LEN;
import static org.openhab.binding.http.HttpBindingConstants.MAX_RESPONSE_BODY_LEN;
import static org.openhab.binding.http.HttpBindingConstants.THING_TYPE_IMAGE;

@NonNullByDefault
public class HttpThingHandler extends BaseThingHandler {
    private static State stateFromString(final String stateStr) {
        final String stateStrTrimmed = stateStr.trim();
        switch (stateStrTrimmed) {
            case "ON":
                return OnOffType.ON;
            case "OFF":
                return OnOffType.OFF;
            case "OPEN":
                return OpenClosedType.OPEN;
            case "CLOSED":
                return OpenClosedType.CLOSED;
            case "UP":
                return UpDownType.UP;
            case "DOWN":
                return UpDownType.DOWN;
            default:
                try {
                    return new PointType(stateStrTrimmed);
                } catch (final IllegalArgumentException e) { /* try something else */ }
                try {
                    return new HSBType(stateStrTrimmed);
                } catch (final IllegalArgumentException e) { /* try something else */ }
                try {
                    return new DecimalType(new BigDecimal(stateStrTrimmed));
                } catch (final NumberFormatException e) { /* try something else */ }
                try {
                    return new DateTimeType(stateStrTrimmed);
                } catch (final IllegalArgumentException e) { /* try something else */ }
                return new StringType(stateStr);
        }
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final HttpClient httpClient;
    private final int maxHttpResponseBodyLen;

    private final ItemChannelLinkRegistry itemChannelLinkRegistry;
    private Set<Item> linkedItems = Collections.emptySet();

    private Optional<StateRequest> stateRequest = Optional.empty();
    private volatile boolean fetchingState = false;
    private Optional<ScheduledFuture<?>> stateUpdater = Optional.empty();
    private Optional<String> lastStateEtag = Optional.empty();
    private Optional<CommandRequest> commandRequest = Optional.empty();

    private Duration connectTimeout;
    private Duration requestTimeout;

    public HttpThingHandler(final Thing thing,
                            final HttpClient httpClient,
                            final ItemChannelLinkRegistry itemChannelLinkRegistry,
                            final Duration connectTimeout,
                            final Duration requestTimeout)
    {
        super(thing);
        this.httpClient = httpClient;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
        this.connectTimeout = connectTimeout;
        this.requestTimeout = requestTimeout;
        if (needsRawType()) {
            this.maxHttpResponseBodyLen = MAX_IMAGE_RESPONSE_BODY_LEN;
        } else {
            this.maxHttpResponseBodyLen = MAX_RESPONSE_BODY_LEN;
        }
    }

    private boolean needsRawType() {
        return getThing().getThingTypeUID().equals(THING_TYPE_IMAGE);
    }

    @Override
    public void initialize() {
        try {
            updateConfig(getThing().getConfiguration().getProperties());
            updateStatus(ThingStatus.ONLINE);
        } catch (final IllegalArgumentException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (!CHANNEL_STATE.equals(channelUID.getId())) {
            logger.warn("[{}] unknown channel '{}'", getThing().getUID(), channelUID.getId());
        } else if (command.equals(RefreshType.REFRESH)) {
            this.stateRequest.ifPresent(this::fetchState);
        } else if (!this.commandRequest.isPresent()) {
            logger.warn("[{}] got command on channel '{}', but no command URL set", getThing().getUID(), channelUID.getId());
        } else {
            final CommandRequest commandRequest = this.commandRequest.get();
            final HttpHandlerConfig.Method method = commandRequest.getMethod();
            final String commandStr = command.toFullString();
            try {
                final String transformedCommand = doTransform(commandRequest.getRequestTransform(), commandStr);
                final URL transformedUrl = formatUrl(commandRequest.getUrl(), transformedCommand);
                makeHttpRequest(method, transformedUrl, commandRequest.getContentType(), Optional.empty(), Optional.of(commandStr)).whenComplete((response, t) -> {
                    if (t != null) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connetion to server failed when sending command: " + t.getMessage());
                    } else if (response.getResponse().getStatus() / 100 != 2) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Server returned HTTP status " + response.getResponse().getStatus() + " when sending command");
                    } else {
                        updateStatus(ThingStatus.ONLINE);
                        updateState(stateFromResponse(response, commandRequest.getResponseTransform()));
                    }
                });
            } catch (final IllegalArgumentException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            }
        }
    }

    @Override
    protected void updateStatus(final ThingStatus status, final ThingStatusDetail statusDetail, @Nullable final String description) {
        final ThingStatusInfo curStatusInfo = getThing().getStatusInfo();
        if (!Objects.equals(status, curStatusInfo.getStatus()) || !Objects.equals(statusDetail, curStatusInfo.getStatusDetail()) || !Objects.equals(description, curStatusInfo.getDescription())) {
            super.updateStatus(status, statusDetail, description);
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (channelUID.equals(getThing().getChannel(CHANNEL_STATE).getUID())) {
            this.linkedItems = this.itemChannelLinkRegistry.getLinkedItems(channelUID);
        }
        super.channelLinked(channelUID);
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        super.channelUnlinked(channelUID);
        if (channelUID.equals(getThing().getChannel(CHANNEL_STATE).getUID())) {
            this.linkedItems = this.itemChannelLinkRegistry.getLinkedItems(channelUID);
        }
    }

    @Override
    public void dispose() {
        cancelStateFetch();
    }

    /**
     * Allows {@link HttpHandlerFactory} to update notify this class of changes in binding parameters.
     *
     * @param connectTimeout HTTP connect timeout
     * @param requestTimeout HTTP request timeout
     */
    public void updateBindingConfig(final Duration connectTimeout, final Duration requestTimeout) {
        this.connectTimeout = connectTimeout;
        this.requestTimeout = requestTimeout;
    }

    private void updateConfig(final Map<String, Object> properties) throws IllegalArgumentException {
        final HttpHandlerConfig config = new Configuration(properties).as(HttpHandlerConfig.class);
        this.stateRequest = config.getStateRequest();
        this.commandRequest = config.getCommandRequest();
        stateRequest.ifPresent(this::startStateFetch);
    }

    private URL formatUrl(final URL origUrl, final String command) throws IllegalArgumentException {
        final String origUrlStr = origUrl.toString();
        if (origUrlStr.contains("%s")) {
            try {
                return new URL(String.format(origUrlStr, command));
            } catch (final MalformedURLException e) {
                throw new IllegalArgumentException("Failed to interpolate command into URL: " + e.getMessage(), e);
            }
        } else {
            return origUrl;
        }
    }

    private CompletionStage<HttpUtil.HttpResponse> makeHttpRequest(final HttpHandlerConfig.Method method,
                                                                   final URL url,
                                                                   final String contentType,
                                                                   final Optional<String> lastEtag,
                                                                   final Optional<String> requestBody)
    {
        return HttpUtil.makeRequest(
                this.httpClient,
                method.toString(),
                url,
                contentType,
                lastEtag,
                requestBody,
                this.connectTimeout,
                this.requestTimeout,
                this.maxHttpResponseBodyLen
        );
    }

    private void updateState(final State state) {
        final boolean needsUpdate = this.linkedItems.stream().anyMatch(item -> !state.equals(item.getState()));
        if (needsUpdate) {
            Optional.ofNullable(getCallback()).ifPresent(
                    cb -> cb.stateUpdated(getThing().getChannel(CHANNEL_STATE).getUID(), state)
            );
        }
    }

    private void startStateFetch(final StateRequest stateRequest) {
        cancelStateFetch();

        this.stateUpdater = Optional.of(scheduler.scheduleWithFixedDelay(
                () -> fetchState(stateRequest),
                0,
                stateRequest.getRefreshInterval().toMillis(),
                TimeUnit.MILLISECONDS
        ));
    }

    private void cancelStateFetch() {
        this.stateUpdater = this.stateUpdater.flatMap(su -> {
            su.cancel(false);
            return Optional.empty();
        });
    }

    private void fetchState(final StateRequest stateRequest) {
        if (!this.fetchingState) {
            this.fetchingState = true;
            final URL url = stateRequest.getUrl();
            makeHttpRequest(HttpHandlerConfig.Method.GET, url, DEFAULT_CONTENT_TYPE, this.lastStateEtag, Optional.empty()).whenComplete((response, t) -> {
                this.fetchingState = false;
                if (t != null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connection to server failed when fetching state: " + t.getMessage());
                } else if (response.getResponse().getStatus() == HttpStatus.NOT_MODIFIED_304) {
                    // no need to do anything; last fetched state is still valid
                    updateStatus(ThingStatus.ONLINE);
                } else if (response.getResponse().getStatus() / 100 != 2) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Server returned HTTP status " + response.getResponse().getStatus() + " when fetching state");
                } else {
                    updateStatus(ThingStatus.ONLINE);
                    final State newState = stateFromResponse(response, stateRequest.getResponseTransform());
                    logger.debug("[{}] got new state '{}'", getThing().getUID(), newState.toFullString());
                    updateState(newState);
                    this.lastStateEtag = Optional.ofNullable(response.getResponse().getHeaders().get("etag"));
                }
            });
        }
    }

    private String doTransform(final Optional<Transform> maybeTransform, final String value) throws IllegalArgumentException {
        return maybeTransform.map(transform ->
                Optional.ofNullable(TransformationHelper.getTransformationService(bundleContext, transform.getFunction())).map(service -> {
                    try {
                        return service.transform(transform.getPattern(), value);
                    } catch (final TransformationException e) {
                        throw new IllegalArgumentException(e.getMessage(), e);
                    }
                }).orElseThrow(() -> new IllegalArgumentException("No transformation service available for function " + transform.getFunction()))
        ).orElse(value);
    }

    private State stateFromResponse(final HttpUtil.HttpResponse response, final Optional<Transform> transform) {
        if (needsRawType()) {
            return response.asRawType();
        } else {
            try {
                return stateFromString(doTransform(transform, response.asString()));
            } catch (final IllegalArgumentException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                return UnDefType.UNDEF;
            } catch (final IllegalStateException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "HTTP server returned unparseable state: " + e.getMessage());
                return UnDefType.UNDEF;
            }
        }
    }
}
