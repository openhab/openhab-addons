/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hive.internal.handler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.hive.internal.handler.strategy.ThingHandlerStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base {@link org.eclipse.smarthome.core.thing.binding.ThingHandler} for
 * Hive devices.
 *
 * <p>
 *     Delegates most functionality to a provided set of
 *     {@link ThingHandlerStrategy}s.
 * </p>
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class DefaultHiveThingHandler extends BaseThingHandler implements HiveThingHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Set<ThingHandlerStrategy> handlerStrategies;

    private @Nullable ThingHandlerCommandCallback commandCallback;

    public DefaultHiveThingHandler(final Thing thing, final Set<ThingHandlerStrategy> handlerStrategies) {
        super(thing);

        this.handlerStrategies = Collections.unmodifiableSet(new HashSet<>(handlerStrategies));
    }

    @Override
    public final Set<ThingHandlerStrategy> getStrategies() {
        return this.handlerStrategies;
    }

    @Override
    public final void setCommandCallback(final ThingHandlerCommandCallback commandCallback) {
        Objects.requireNonNull(commandCallback);

        this.commandCallback = commandCallback;
    }

    @Override
    public final void clearCommandCallback() {
        this.commandCallback = null;
    }

    @Override
    public final @Nullable ThingHandlerCallback getThingHandlerCallback() {
        return this.getCallback();
    }

    @Override
    public void initialize() {
        final @Nullable HiveAccountHandler accountHandler = getAccountHandler();

        if (accountHandler != null) {
            accountHandler.bindHiveThingHandler(this);
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public final void handleCommand(final ChannelUID channelUID, final Command command) {
        final @Nullable HiveAccountHandler accountHandler = getAccountHandler();
        if (accountHandler == null) {
            return;
        }

        final @Nullable ThingHandlerCommandCallback commandCallback = this.commandCallback;
        if (commandCallback == null) {
            throw new IllegalStateException("handleCommand(...) called but no CommandCallback has been set.");
        }

        commandCallback.handleCommand(channelUID, command);
    }

    @Override
    public void dispose() {
        // Clean up the HiveAccountHandler reference to this handler.
        final @Nullable HiveAccountHandler accountHandler = getAccountHandler();
        if (accountHandler != null) {
            accountHandler.unbindHiveThingHandler(this);
        }

        super.dispose();
    }

    /**
     * Get the HiveAccountHandler that is acting as a BridgeHandler for this thing.
     *
     * @return
     *      {@code null} If the bridge has not been set.
     */
    private @Nullable HiveAccountHandler getAccountHandler() {
        final @Nullable Bridge bridge = getBridge();
        if (bridge == null) {
            this.logger.debug("getAccountHandler() called but bridge is null.  Has something gone wrong?  Setting status to offline.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            return null;
        } else {
            return (HiveAccountHandler) bridge.getHandler();
        }
    }
}
