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
package org.openhab.binding.hyperion.internal.protocol.v1;

import org.openhab.binding.hyperion.internal.protocol.HyperionCommand;

/**
 * The {@link TransformCommand} is a POJO for sending transform commands
 * to the Hyperion server.
 *
 * @author Daniel Walters - Initial contribution
 */
public class TransformCommand extends HyperionCommand {

    private static final String NAME = "transform";
    private Transform transform;

    public TransformCommand(Transform transform) {
        super(NAME);
        setTransform(transform);
    }

    public Transform getTransform() {
        return transform;
    }

    public void setTransform(Transform transform) {
        this.transform = transform;
    }
}
