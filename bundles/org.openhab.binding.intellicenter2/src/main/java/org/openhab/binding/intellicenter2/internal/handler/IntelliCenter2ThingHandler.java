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
package org.openhab.binding.intellicenter2.internal.handler;

import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.OBJNAM;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.intellicenter2.internal.model.ResponseModel;
import org.openhab.binding.intellicenter2.internal.protocol.Attribute;
import org.openhab.binding.intellicenter2.internal.protocol.ICProtocol;
import org.openhab.binding.intellicenter2.internal.protocol.NotifyListListener;
import org.openhab.binding.intellicenter2.internal.protocol.ResponseObject;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Handler that provides a consistent way to support other IntelliCenter2 handlers.
 *
 * @author Valdis Rigdon - Initial contribution
 */
@NonNullByDefault
public abstract class IntelliCenter2ThingHandler<T extends ResponseModel> extends BaseThingHandler
        implements NotifyListListener {

    private final Logger logger = LoggerFactory.getLogger(IntelliCenter2ThingHandler.class);

    public IntelliCenter2ThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> {
            try {
                // get an updated model from IC
                final ICProtocol protocol = getProtocol();
                if (protocol == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR);
                    return;
                }
                final T model = queryModel(protocol);
                getProtocol().subscribe(this, model.asRequestObject());
                updateStatus(ThingStatus.ONLINE);
                updateState(model);
            } catch (Exception e) {
                logger.error("Error refreshing model", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        });
    }

    @Override
    public void dispose() {
        super.dispose();
        scheduler.execute(() -> {
            if (getProtocol() != null) {
                getProtocol().unsubscribe(this);
            }
            updateStatus(ThingStatus.OFFLINE);
        });
    }

    @Nullable
    protected ICProtocol getProtocol() {
        if (getBridgeHandler() != null) {
            return getBridgeHandler().getProtocol();
        }
        return null;
    }

    @Nullable
    protected IntelliCenter2BridgeHandler getBridgeHandler() {
        if (getBridge() == null) {
            return null;
        }
        return (IntelliCenter2BridgeHandler) getBridge().getHandler();
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        logger.debug("Handling {} for {}", channelUID, command);
        final ICProtocol protocol = getProtocol();
        if (protocol == null) {
            logger.error("handleCommand had a null protocol for {} {}", channelUID, command);
            return;
        }
        if (command instanceof RefreshType) {
            final T model = queryModel(protocol);
            logger.debug("updateState uid: {}, id: {}, model: {}", channelUID, channelUID.getId(), model);
            updateState(channelUID, model);
        }
    }

    @Override
    public void onNotifyList(ResponseObject response) {
        try {
            if (!isInitialized()) {
                logger.warn("Notified of a NotifyList response but this is not initialized: {}", response);
                return;
            }
            final T model = createFromResponse(response);
            logger.debug("onNotifyList {}", model);
            response.getParams().forEach((k, v) -> {
                var id = toChannelId(k);
                if (id != null) {
                    var channel = getThing().getChannel(id);
                    if (channel != null) {
                        updateState(channel.getUID(), model);
                    } else {
                        logger.warn("Unable to find a channel to update for {}", id);
                    }
                } else {
                    logger.warn("Received notifyList update for attribute {} that doesn't map to a channelId", k);
                }
            });
        } catch (Exception e) {
            logger.error("Error handling onNotifyList", e);
        }
    }

    protected String getObjectName() {
        return Objects.requireNonNull(getThing().getProperties().get(OBJNAM.name()));
    }

    protected abstract T queryModel(ICProtocol protocol);

    protected abstract void updateState(T model);

    protected abstract T createFromResponse(ResponseObject response);

    protected abstract void updateState(ChannelUID channelUID, T model);

    @Nullable
    protected abstract String toChannelId(Attribute a);
}
