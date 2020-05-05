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
package org.openhab.binding.hue.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.FullGroup;
import org.openhab.binding.hue.internal.HueBridge;

/**
 * The {@link GroupStatusListener} is notified when a group status has changed or a group has been removed or added.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public interface GroupStatusListener {

    /**
     * This method is called whenever the state of the given group has changed. The new state can be obtained by
     * {@link FullGroup#getState()}.
     *
     * @param bridge The bridge the changed group is connected to.
     * @param group The group which received the state update.
     */
    void onGroupStateChanged(@Nullable HueBridge bridge, FullGroup group);

    /**
     * This method is called whenever a group is removed.
     *
     * @param bridge The bridge the removed group was connected to.
     * @param group The removed group
     */
    void onGroupRemoved(@Nullable HueBridge bridge, FullGroup group);

    /**
     * This method is called whenever a group is reported as gone.
     *
     * @param bridge The bridge the reported group was connected to.
     * @param group The group which is reported as gone.
     */
    void onGroupGone(@Nullable HueBridge bridge, FullGroup group);

    /**
     * This method is called whenever a group is added.
     *
     * @param bridge The bridge the added group was connected to.
     * @param group The added group
     */
    void onGroupAdded(@Nullable HueBridge bridge, FullGroup group);
}
