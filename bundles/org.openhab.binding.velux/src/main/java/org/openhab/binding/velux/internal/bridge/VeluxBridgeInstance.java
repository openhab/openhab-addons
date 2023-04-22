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
package org.openhab.binding.velux.internal.bridge;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velux.internal.config.VeluxBridgeConfiguration;
import org.openhab.binding.velux.internal.things.VeluxExistingProducts;
import org.openhab.binding.velux.internal.things.VeluxExistingScenes;

/**
 * This interface is implemented by classes that deal with a specific Velux bridge and its configuration.
 * <P>
 * <B>Configuration</B>
 * </P>
 * <UL>
 * <LI>{@link org.openhab.binding.velux.internal.config.VeluxBridgeConfiguration VeluxBridgeConfiguration}
 * for specification by a specific Velux bridge,</LI>
 * </UL>
 *
 * <P>
 * <B>Status</B>
 * </P>
 * Two methods for bridge-internal configuration retrieval:
 * <UL>
 * <LI>{@link #existingProducts}
 * for retrieving scene information,</LI>
 * <LI>{@link #existingScenes}
 * for retrieving product information.</LI>
 * </UL>
 *
 * @see VeluxBridgeProvider
 *
 * @author Guenther Schreiner - Initial contribution
 */
@NonNullByDefault
public interface VeluxBridgeInstance {

    /**
     * Bridge configuration
     *
     * @return VeluxBridgeConfiguration containing all bridge configuration settings.
     */
    VeluxBridgeConfiguration veluxBridgeConfiguration();

    /**
     * Information retrieved by {@link org.openhab.binding.velux.internal.bridge.VeluxBridgeActuators#getProducts}
     *
     * @return VeluxExistingProducts containing all registered products, or <B>null</B> in case of any error.
     */
    VeluxExistingProducts existingProducts();

    /**
     * Information retrieved by {@link org.openhab.binding.velux.internal.bridge.VeluxBridgeScenes#getScenes}
     *
     * @return VeluxExistingScenes containing all registered scenes, or <B>null</B> in case of any error.
     */
    VeluxExistingScenes existingScenes();
}
