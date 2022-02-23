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
package org.openhab.binding.freeboxos.internal.api.airmedia;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.airmedia.AirMediaActionData.MediaType;
import org.openhab.binding.freeboxos.internal.api.rest.FbxDevice;

/**
 * The {@link AirMediaReceiver} is the Java class used to map the "AirMediaReceiver"
 * structure used by the available AirMedia receivers API
 * https://dev.freebox.fr/sdk/os/airmedia/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AirMediaReceiver extends FbxDevice {
    public static class AirMediaReceiversResponse extends Response<List<AirMediaReceiver>> {
    }

    public static class AirMediaReceiverResponse extends Response<AirMediaReceiver> {
    }

    private boolean passwordProtected;
    private Map<MediaType, Boolean> capabilities = Map.of();

    public boolean isPasswordProtected() {
        return passwordProtected;
    }

    public Map<MediaType, Boolean> getCapabilities() {
        return capabilities;
    }
}
