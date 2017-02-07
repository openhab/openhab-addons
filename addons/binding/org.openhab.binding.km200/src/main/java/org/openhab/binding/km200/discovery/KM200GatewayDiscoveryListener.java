/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.km200.discovery;

/**
 * A {@link KM200GatewayDiscoveryListener} listeners for discovered gateways
 *
 * @author Markus Eckhardt
 *
 */
public interface KM200GatewayDiscoveryListener {

    /**
     * The discovery process has finished
     */
    public void gatewayDiscoveryFinished();

    /**
     * The discovery process has discovered a hub
     *
     * @param result
     */
    public void gatewayDiscovered(KM200GatewayDiscoveryResult result);
}