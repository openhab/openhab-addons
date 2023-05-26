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
package org.openhab.binding.lgthinq.lgservices.model;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.internal.errors.LGThinqUnmarshallException;

/**
 * The {@link SnapshotBuilder}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public interface SnapshotBuilder<S extends SnapshotDefinition> {
    S createFromBinary(String binaryData, List<MonitoringBinaryProtocol> prot, CapabilityDefinition capDef)
            throws LGThinqUnmarshallException, LGThinqApiException;

    S createFromJson(String snapshotDataJson, DeviceTypes deviceType, CapabilityDefinition capDef)
            throws LGThinqUnmarshallException, LGThinqApiException;

    S createFromJson(Map<String, Object> deviceSettings, CapabilityDefinition capDef) throws LGThinqApiException;
}
