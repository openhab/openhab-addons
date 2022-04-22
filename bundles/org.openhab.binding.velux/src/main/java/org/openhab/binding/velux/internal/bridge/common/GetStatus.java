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
import org.openhab.binding.velux.internal.bridge.slip.FunctionalParameters;

/**
 * @author Andrew Fiddian-Green - Initial contribution.
 */
@NonNullByDefault
public abstract class GetStatus implements BridgeCommunicationProtocol {

    /**
     * Set the intended node identifier to be queried
     *
     * @param nodeId Gateway internal node identifier (zero to 199)
     */
    public abstract void setProductId(int nodeId);

    /**
     * <B>Retrieval of information about the selected product</B>
     *
     * @return <b>veluxProduct</b> as VeluxProduct.
     */
    public abstract FunctionalParameters getFunctionalParameters();

    public abstract int getNodeId();

    public abstract int getCurrentPosition();

    public abstract int getState();
}
