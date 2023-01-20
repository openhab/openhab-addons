/**
<<<<<<< Upstream, based on origin/main
<<<<<<< Upstream, based on origin/main
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
=======
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
            return List.of();
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
>>>>>>> 46dadb1 SAT warnings handling
=======
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
package org.openhab.binding.freeboxos.internal.api.rest;

import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.rest.LoginManager.Session.Permission;

/**
 * The {@link ListableRest} is the Java class used to handle rest answers holding a list of known equipments
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ListableRest<T, Z extends Response<T>> extends RestManager {
    private final Class<Z> deviceResponseClass;

    protected @Nullable String listSubPath = null;

    public ListableRest(FreeboxOsSession session, Permission required, Class<Z> respClass, UriBuilder uri)
            throws FreeboxException {
        super(session, required, uri);
        this.deviceResponseClass = respClass;
    }

    public List<T> getDevices() throws FreeboxException {
        return listSubPath == null ? get(deviceResponseClass) : get(deviceResponseClass, listSubPath);
    }

    public T getDevice(int deviceId) throws FreeboxException {
        return getSingle(deviceResponseClass, Integer.toString(deviceId));
>>>>>>> e4ef5cc Switching to Java 17 records
    }
}
