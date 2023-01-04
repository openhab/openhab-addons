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
package org.openhab.binding.velux.internal.bridge.common;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velux.internal.bridge.slip.FunctionalParameters;
import org.openhab.binding.velux.internal.things.VeluxProductPosition;

/**
 * <B>Common bridge communication message scheme supported by the </B><I>Velux</I><B> bridge.</B>
 * <P>
 * Message semantic will be defined by the implementations according to the different comm paths.
 * <P>
 * In addition to the common methods defined by {@link BridgeCommunicationProtocol}
 * each protocol-specific implementation has to provide the following methods:
 * <UL>
 * <LI>{@link #setNodeIdAndParameters} for defining the intended node and the main parameter value.
 * </UL>
 *
 * @see BridgeCommunicationProtocol
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
public abstract class RunProductCommand implements BridgeCommunicationProtocol {

    /**
     * Modifies the state of an actuator
     *
     * @param nodeId Gateway internal actuator identifier (zero to 199).
     * @param mainParameter target device state.
     * @param functionalParameters the target Functional Parameters.
     * @return true if the method succeeds
     */
    public abstract boolean setNodeIdAndParameters(int nodeId, @Nullable VeluxProductPosition mainParameter,
            @Nullable FunctionalParameters functionalParameters);
}
