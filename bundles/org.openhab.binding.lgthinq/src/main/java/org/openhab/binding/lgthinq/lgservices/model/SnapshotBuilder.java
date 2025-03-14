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
package org.openhab.binding.lgthinq.lgservices.model;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqUnmarshallException;

/**
 * Interface for building snapshots from various data sources, such as binary data or JSON data.
 * This interface provides methods for creating snapshots based on different types of input data,
 * which can be used to define and monitor devices in the LG ThinQ service.
 *
 * <p>
 * The implementation of this interface should be able to handle the deserialization of the
 * binary and JSON data and return a snapshot representation that conforms to the provided
 * {@link SnapshotDefinition}.
 * </p>
 *
 * <p>
 * Usage Example:
 *
 * <pre>
 * SnapshotBuilder&lt;MySnapshot&gt; snapshotBuilder = new MySnapshotBuilder();
 * MySnapshot snapshot = snapshotBuilder.createFromJson(jsonData, deviceType, capDef);
 * </pre>
 * </p>
 *
 * @param <S> the type of snapshot to be created, extending {@link SnapshotDefinition}
 * @author Nemer Daud - Initial contribution
 * @version 1.0
 */
@NonNullByDefault
public interface SnapshotBuilder<S extends SnapshotDefinition> {

    /**
     * Creates a snapshot from binary data.
     *
     * @param binaryData the binary data to be deserialized into a snapshot
     * @param prot a list of monitoring binary protocols used for parsing the binary data
     * @param capDef the capability definition to be applied to the snapshot
     * @return the created snapshot
     * @throws LGThinqUnmarshallException if an error occurs during unmarshalling the binary data
     * @throws LGThinqApiException if a general API error occurs
     */
    S createFromBinary(String binaryData, List<MonitoringBinaryProtocol> prot, CapabilityDefinition capDef)
            throws LGThinqUnmarshallException, LGThinqApiException;

    /**
     * Creates a snapshot from a JSON string representation of the snapshot data.
     *
     * @param snapshotDataJson the JSON string containing the snapshot data
     * @param deviceType the type of the device associated with the snapshot
     * @param capDef the capability definition to be applied to the snapshot
     * @return the created snapshot
     * @throws LGThinqUnmarshallException if an error occurs during unmarshalling the JSON data
     * @throws LGThinqApiException if a general API error occurs
     */
    S createFromJson(String snapshotDataJson, DeviceTypes deviceType, CapabilityDefinition capDef)
            throws LGThinqUnmarshallException, LGThinqApiException;

    /**
     * Creates a snapshot from a map of device settings.
     *
     * @param deviceSettings the map containing device-specific settings
     * @param capDef the capability definition to be applied to the snapshot
     * @return the created snapshot
     * @throws LGThinqApiException if an error occurs during the API processing
     */
    S createFromJson(Map<String, Object> deviceSettings, CapabilityDefinition capDef) throws LGThinqApiException;
}
