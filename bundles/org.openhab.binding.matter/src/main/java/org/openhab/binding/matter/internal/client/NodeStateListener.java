/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.matter.internal.client;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.matter.internal.client.dto.ws.NodeStateMessage;

/**
 * A listener for node state changes
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface NodeStateListener {

    public void onEvent(NodeStateMessage messasge);
}
