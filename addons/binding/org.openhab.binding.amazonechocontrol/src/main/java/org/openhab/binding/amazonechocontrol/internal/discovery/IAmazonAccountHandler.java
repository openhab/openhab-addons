/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazonechocontrol.internal.discovery;

/**
 * The {@link IAmazonAccountHandler} is responsible connection between account and discovery service
 *
 * @author Michael Geramb - Initial contribution
 */
public interface IAmazonAccountHandler {
    void updateDeviceList(boolean manual);
}
