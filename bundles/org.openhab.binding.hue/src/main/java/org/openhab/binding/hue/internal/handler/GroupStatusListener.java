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
package org.openhab.binding.hue.internal.handler;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hue.internal.dto.FullGroup;
import org.openhab.binding.hue.internal.dto.Scene;

/**
 * The {@link GroupStatusListener} is notified when a group status has changed or a group has been removed or added.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public interface GroupStatusListener {

    /**
     * This method returns the group id of listener
     *
     * @return groupId String
     */
    String getGroupId();

    /**
     * This method is called whenever the state of the given group has changed. The new state can be obtained by
     * {@link FullGroup#getState()}.
     *
     * @param group The group which received the state update.
     * @return
     */
    boolean onGroupStateChanged(FullGroup group);

    /**
     * This method is called whenever a group is removed.
     */
    void onGroupRemoved();

    /**
     * This method is called whenever a group is reported as gone.
     */
    void onGroupGone();

    /**
     * This method is called whenever a group is added.
     *
     * @param group The added group
     */
    void onGroupAdded(FullGroup group);

    /**
     * This method is called whenever the list of available scenes is updated.
     *
     * @param updatedScenes available scenes
     */
    void onScenesUpdated(List<Scene> scenes);
}
