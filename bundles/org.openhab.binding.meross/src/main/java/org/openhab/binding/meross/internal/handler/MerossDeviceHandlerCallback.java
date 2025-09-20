/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.meross.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meross.internal.api.MerossEnum.Namespace;
import org.openhab.core.types.State;

/**
 * {@link MerossDeviceHandlerCallback} lists the methods for callback from the
 * {@link org.openhab.binding.meross.internal.api.MerossManager}. This allows the manager, when receiving messages, to
 * update the handler.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public interface MerossDeviceHandlerCallback {

    public @Nullable String getIpAddress();

    public void setIpAddress(String ipAddress);

    public default void setThingStatusFromMerossStatus(int status) {
    };

    public default void updateState(Namespace namespace, int deviceChannel, State state) {
    };
}
