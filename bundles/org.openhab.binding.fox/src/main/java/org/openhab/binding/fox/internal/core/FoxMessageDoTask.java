/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.fox.internal.core;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link FoxMessageBoot} is a message of system task request.
 *
 * @author Kamil Subzda - Initial contribution
 */
@NonNullByDefault
class FoxMessageDoTask extends FoxMessage {

    int taskId;

    public FoxMessageDoTask() {
        super();
        taskId = 0;
    }

    @Override
    protected void prepareMessage() {
        message = String.format("do T%d", taskId);
    }

    void setTaskId(int id) {
        taskId = id;
    }

    @Override
    protected void interpretMessage() {
    }
}
