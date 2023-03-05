/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.RestManager;
import org.openhab.binding.netatmo.internal.api.dto.Device;
import org.openhab.binding.netatmo.internal.api.dto.Module;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.deserialization.NAObjectMap;
import org.openhab.binding.netatmo.internal.handler.ApiBridgeHandler;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;

/**
 * The {@link RestCapability} is the base class for handler capabilities
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public abstract class RestCapability<T extends RestManager> extends DeviceCapability {
    private Optional<T> api = Optional.empty();
    private Class<T> restManagerClass;

    RestCapability(CommonInterface handler, Class<T> restManagerClazz) {
        super(handler);
        this.restManagerClass = restManagerClazz;
    }

    @Override
    protected void updateNADevice(Device newData) {
        super.updateNADevice(newData);
        NAObjectMap<Module> modules = newData.getModules();
        handler.getActiveChildren().forEach(child -> {
            Module childData = modules.get(child.getId());
            if (childData != null) {
                child.setNewData(childData);
            }
        });
    }

    @Override
    public final List<NAObject> updateReadings() {
        List<NAObject> result = new ArrayList<>();
        getApi().ifPresent(api -> result.addAll(updateReadings(api)));
        return result;
    }

    protected List<NAObject> updateReadings(T api) {
        return List.of();
    }

    protected Optional<T> getApi() {
        if (api.isEmpty()) {
            ApiBridgeHandler bridgeApi = handler.getAccountHandler();
            if (bridgeApi != null) {
                api = Optional.ofNullable(bridgeApi.getRestManager(restManagerClass));
            }
        }
        return api;
    }
}
