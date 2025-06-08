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
package org.openhab.binding.matter.internal.client.dto.ws;

import org.openhab.binding.matter.internal.client.dto.Node;

/**
 * NodeInitializedMessage
 *
 * @author Dan Cunningham - Initial contribution
 */
public class NodeInitializedMessage {

    public Node node;

    public NodeInitializedMessage(Node node) {
        this.node = node;
    }
}
