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
package org.openhab.binding.velux.internal.bridge;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velux.internal.bridge.common.BridgeAPI;
import org.openhab.binding.velux.internal.bridge.common.BridgeCommunicationProtocol;

/**
 * This interface is implemented by classes that provide general communication with the Velux bridge.
 * <P>
 * Communication
 * </P>
 * <UL>
 * <LI>{@link VeluxBridgeProvider#bridgeCommunicate} for generic communication,</LI>
 * <LI>{@link VeluxBridgeProvider#bridgeAPI} for access to all interaction/communication methods.</LI>
 * </UL>
 *
 * @author Guenther Schreiner - Initial contribution
 */
@NonNullByDefault
public interface VeluxBridgeProvider {

    /**
     * Initializes a client/server communication towards <b>Velux</b> Bridge
     * based on the Basic I/O interface {@link VeluxBridge} and parameters
     * passed as arguments (see below) and provided by
     * {@link org.openhab.binding.velux.internal.config.VeluxBridgeConfiguration}.
     * This method automatically decides to invoke a login communication before the
     * intended request if there has not been an authentication before.
     *
     * @param communication {@link BridgeCommunicationProtocol} describing the intended
     *            communication, that is request and response interactions as well as appropriate URL
     *            definition.
     * @return true if communication was successful, and false otherwise.
     */

    boolean bridgeCommunicate(BridgeCommunicationProtocol communication);

    /**
     * Returns the class {@link BridgeAPI} which summarizes all interfacing methods.
     *
     * @return <b>BridgeAPI</b>
     *         containing all API methods.
     */
    @Nullable
    BridgeAPI bridgeAPI();
}
