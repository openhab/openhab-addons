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
package org.openhab.binding.velux.internal.bridge.common;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * <B>Common bridge communication message scheme supported by the </B><I>Velux</I><B> bridge.</B>
 * <P>
 * Message semantic will be defined by the implementations according to the different comm paths.
 * <P>
 * In addition to the common methods defined by {@link BridgeCommunicationProtocol}
 * each protocol-specific implementation has to provide the following methods:
 * <UL>
 * <LI>{@link #setActuatorIdAndLimitationType} for defining the intended actuator and the query type.
 * <LI>{@link #getLimitation} for accessing the retrieved information.
 * </UL>
 *
 * @see BridgeCommunicationProtocol
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
public abstract class GetProductLimitation implements BridgeCommunicationProtocol {

    /**
     * Set the intended node identifier to be queried
     *
     * @param nodeId Gateway internal node identifier (zero to 199).
     * @param getLimitationMinimum true, if we query for Minimum.
     */
    public abstract void setActuatorIdAndLimitationType(int nodeId, boolean getLimitationMinimum);

    /**
     * <B>Retrieval of information about the selected product</B>
     *
     * @return <b>limitation</b> as int.
     */
    public abstract int getLimitation();
}
