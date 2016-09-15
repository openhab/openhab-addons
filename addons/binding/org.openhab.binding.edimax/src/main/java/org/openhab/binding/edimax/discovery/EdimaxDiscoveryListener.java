/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.edimax.discovery;

/**
 * A {@link EdimaxDiscoveryListener} listeners for discovered hubs
 *
 * @author Dan Cunningham - Initial contribution
 *
 */
public interface EdimaxDiscoveryListener {

    /**
     * The discovery process has finished
     */
    // public void discoveryFinished();

    /**
     * The discovery process has discovered a hub
     *
     * @param result
     */
    public void smartplugDiscovered(EdimaxDiscoveryResult result);

    public void discoveryFinished();
}