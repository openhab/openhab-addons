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

/**
 * <B>Common bridge communication message scheme supported by the </B><I>Velux</I><B> bridge.</B>
 * <P>
 * Message semantic will be defined by the implementations according to the different comm paths.
 * <P>
 * In addition to the common methods defined by {@link BridgeCommunicationProtocol}
 * each protocol-specific implementation has to provide the following methods:
 * <UL>
 * <LI>{@link #setSceneId} for defining the intended scene.
 * </UL>
 *
 * @see BridgeCommunicationProtocol
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
public abstract class RunScene implements BridgeCommunicationProtocol {

    /**
     * Sets the intended scene identifier to be executed
     *
     * @param id Gateway internal scene identifier
     * @return reference to the class instance.
     */
    public RunScene setSceneId(int id) {
        return this;
    }

    /**
     * Sets the intended scene velocity for later execution
     *
     * @param velocity setting as String.
     * @return reference to the class instance.
     */
    public RunScene setVelocity(int velocity) {
        return this;
    }
}
