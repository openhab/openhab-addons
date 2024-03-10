/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.dmx.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dmx.internal.multiverse.DmxChannel;

/**
 * Resume action. Restores previously suspended value or actions on an item.
 *
 * @author Davy Vanherbergen - Initial contribution
 * @author Jan N. Klug - Refactoring for ESH
 */
@NonNullByDefault
public class ResumeAction extends BaseAction {

    @Override
    public int getNewValue(DmxChannel channel, long currentTime) {
        state = ActionState.COMPLETED;
        channel.resumeAction();
        return channel.getNewHiResValue(currentTime);
    }
}
