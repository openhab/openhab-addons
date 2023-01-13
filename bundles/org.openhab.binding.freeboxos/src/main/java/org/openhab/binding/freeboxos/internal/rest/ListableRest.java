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
package org.openhab.binding.freeboxos.internal.rest;

import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.Permission;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;

/**
 * The {@link ListableRest} is the Java class used to handle rest answers holding a list of known equipments
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ListableRest<T, Z extends Response<T>, Y extends Response<List<T>>> extends RestManager {
    private final Class<Y> listResponseClass;
    private final Class<Z> deviceResponseClass;

    protected @Nullable String listSubPath = null;

    public ListableRest(FreeboxOsSession session, Class<Z> devRespClass, Class<Y> listRespClass,
            String... pathElements) {
        super(session, pathElements);
        this.listResponseClass = listRespClass;
        this.deviceResponseClass = devRespClass;
    }

    public ListableRest(FreeboxOsSession session, Class<Z> devRespClass, Class<Y> listRespClass, UriBuilder parentUri,
            String... pathElements) {
        super(session, parentUri, pathElements);
        this.listResponseClass = listRespClass;
        this.deviceResponseClass = devRespClass;
    }

    public ListableRest(FreeboxOsSession session, Permission required, Class<Z> devRespClass, Class<Y> listRespClass,
            String... pathElements) throws FreeboxException {
        super(session, required, pathElements);
        this.listResponseClass = listRespClass;
        this.deviceResponseClass = devRespClass;
    }

    public List<T> getDevices() throws FreeboxException {
        if (listSubPath == null) {
            List<T> result = get(listResponseClass);
            return result != null ? result : List.of();
        }
        return getList(listResponseClass, listSubPath);
    }

    public T getDevice(int deviceId) throws FreeboxException {
        @Nullable
        T result = get(deviceResponseClass, deviceSubPath(deviceId));
        if (result != null) {
            return result;
        }
        throw new FreeboxException("Device is null");
    }

    protected String deviceSubPath(int deviceId) {
        return "%d".formatted(deviceId);
    }
}
