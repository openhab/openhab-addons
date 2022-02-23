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
package org.openhab.binding.freeboxos.internal.api.rest;

import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.login.Session.Permission;

/**
 * The {@link ListableRest} is the Java class used to handle portions of the
 * Api that accept to get and set configuration
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ListableRest<T extends FbxDevice, Z extends Response<T>, Y extends Response<List<T>>> extends RestManager {
    private final Class<Y> listRespClass;
    private final Class<Z> devRespClass;
    @Nullable
    protected String listSubPath = null;

    public ListableRest(FreeboxOsSession session, Class<Z> devRespClass, Class<Y> listRespClass,
            String... pathElements) {
        super(session, pathElements);
        this.listRespClass = listRespClass;
        this.devRespClass = devRespClass;
    }

    public ListableRest(FreeboxOsSession session, Class<Z> devRespClass, Class<Y> listRespClass, UriBuilder parentUri,
            String... pathElements) {
        super(session, parentUri, pathElements);
        this.listRespClass = listRespClass;
        this.devRespClass = devRespClass;
    }

    public ListableRest(FreeboxOsSession session, Permission required, Class<Z> devRespClass, Class<Y> listRespClass,
            String... pathElements) throws FreeboxException {
        super(session, required, pathElements);
        this.listRespClass = listRespClass;
        this.devRespClass = devRespClass;
    }

    public List<T> getDevices() throws FreeboxException {
        if (listSubPath == null) {
            List<T> result = get(listRespClass);
            if (result != null) {
                return result;
            }
            throw new FreeboxException("Devicelist is empty");
        } else {
            return getList(listRespClass, listSubPath);
        }
    }

    public T getDevice(int deviceId) throws FreeboxException {
        @Nullable
        T result = get(devRespClass, deviceSubPath(deviceId));
        if (result != null) {
            return result;
        }
        throw new FreeboxException("Device is null");
    }

    protected String deviceSubPath(int deviceId) {
        return String.format("%d", deviceId);
    }
}
