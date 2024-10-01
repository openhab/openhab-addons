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
package org.openhab.binding.freeboxos.internal.api.rest;

import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;

/**
 * The {@link ListableRest} is the Java class used to handle rest answers holding a list of known equipments
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ListableRest<T, Z extends Response<T>> extends RestManager {
    private final Class<Z> deviceResponseClass;

    protected @Nullable String listSubPath = null;

    public ListableRest(FreeboxOsSession session, LoginManager.Permission required, Class<Z> respClass, UriBuilder uri)
            throws FreeboxException {
        super(session, required, uri);
        this.deviceResponseClass = respClass;
    }

    public List<T> getDevices() throws FreeboxException {
        return listSubPath == null ? get(deviceResponseClass) : get(deviceResponseClass, listSubPath);
    }

    public T getDevice(int deviceId) throws FreeboxException {
        return getSingle(deviceResponseClass, Integer.toString(deviceId));
    }
}
