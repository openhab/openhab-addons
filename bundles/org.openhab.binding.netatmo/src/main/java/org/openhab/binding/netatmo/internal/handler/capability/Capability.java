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
package org.openhab.binding.netatmo.internal.handler.capability;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.RestManager;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeData;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeEvent;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeStatus.HomeStatus;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.handler.NetatmoHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;

/**
 * The {@link Capability} is the base class for handler capabilities
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public abstract class Capability<T extends RestManager> {
    protected final T api;
    protected final Bridge bridge;

    Capability(Bridge bridge, @Nullable T restManager) {
        if (restManager == null) {
            throw new IllegalArgumentException("Should not be null");
        }
        this.api = restManager;
        this.bridge = bridge;
    }

    public void setNewData(NAObject newData) {
        if (newData instanceof NAHomeData) {
            updateHomeData((NAHomeData) newData);
        } else if (newData instanceof HomeStatus) {
            updateHomeStatus((HomeStatus) newData);
        } else if (newData instanceof NAHomeEvent) {
            updateHomeEvent((NAHomeEvent) newData);
        }
    }

    protected void updateHomeEvent(NAHomeEvent newData) {
    }

    protected void updateHomeStatus(HomeStatus newData) {
    }

    protected void updateHomeData(NAHomeData newData) {
    }

    public void internalHandleCommand(String channelName, Command command) {
    }

    public void dispose() {
    }

    protected Optional<NetatmoHandler> getNAHandler() {
        BridgeHandler handler = bridge.getHandler();
        return Optional.ofNullable(handler instanceof NetatmoHandler ? ((NetatmoHandler) handler) : null);
    }

    protected Stream<NetatmoHandler> getActiveChildren() {
        return getNAHandler().map(handler -> handler.getActiveChildren()).orElse(Stream.empty());
    }

    protected void expireData() {
        getNAHandler().ifPresent(handler -> handler.expireData());
    }
}
