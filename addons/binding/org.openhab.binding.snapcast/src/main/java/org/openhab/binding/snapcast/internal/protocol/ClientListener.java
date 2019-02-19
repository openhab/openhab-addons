/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.snapcast.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Steffen Brandemann - Initial contribution
 */
@NonNullByDefault
public interface ClientListener {

    public void updateConnection(String clientId);

    public void updateName(String clientId);

    public void updateVolumn(String clientId);

    public void updateMute(String clientId);

    public void updateLatency(String clientId);

    public void updateGroup(String clientId);
}
