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
package org.openhab.binding.hue.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.hue.internal.api.dto.clip1.FullGroup;
import org.openhab.binding.hue.internal.api.dto.clip1.Scene;
import org.openhab.binding.hue.internal.api.dto.clip1.State;

/**
 * @author HJiang - initial contribution
 */
@NonNullByDefault
public class SceneTest {

    private static final State PLACEHOLDER_STATE = new State();
    private static final String PLACEHOLDER = "placeholder";

    /**
     * If a scene already has a group ID, it should applicable to the group with the given ID.
     */
    @Test
    public void testIsApplicableToHasGroupIdMatchingGroup() {
        String groupId = "groupId";
        List<String> lights = List.of("1", "2");

        Scene scene = new Scene(PLACEHOLDER, PLACEHOLDER, groupId, lights, false);
        FullGroup group = new FullGroup(groupId, PLACEHOLDER, PLACEHOLDER, PLACEHOLDER_STATE, lights,
                PLACEHOLDER_STATE);

        assertThat(scene.isApplicableTo(group), is(true));
    }

    /**
     * If a scene already has a group ID, it should be NOT applicable to a group with different ID even if the lights
     * match.
     */
    @Test
    public void testIsApplicableToHasGroupIdNotMatchingGroup() {
        String groupId = "groupId";
        String otherGroupId = "otherGroupId";
        List<String> lights = List.of("1", "2");
        List<String> otherLights = List.of("1", "2", "3");

        Scene scene = new Scene(PLACEHOLDER, PLACEHOLDER, groupId, lights, false);

        FullGroup nonMatchingGroupWithOtherLights = new FullGroup(otherGroupId, PLACEHOLDER, PLACEHOLDER,
                PLACEHOLDER_STATE, otherLights, PLACEHOLDER_STATE);
        assertThat(scene.isApplicableTo(nonMatchingGroupWithOtherLights), is(false));

        FullGroup nonMatchingGroupWithSameLights = new FullGroup(otherGroupId, PLACEHOLDER, PLACEHOLDER,
                PLACEHOLDER_STATE, lights, PLACEHOLDER_STATE);
        assertThat(scene.isApplicableTo(nonMatchingGroupWithSameLights), is(false));
    }

    /**
     * If a scene does not have a group ID, it should be applicable to a group that contains all lights of the
     * scene.
     */
    @Test
    public void testIsApplicableToNoGroupIdSceneLightsContainedInGroup() {
        List<String> lights = List.of("1", "2");
        List<String> moreLights = List.of("1", "2", "3");

        Scene scene = new Scene(PLACEHOLDER, PLACEHOLDER, null, lights, false);

        FullGroup groupWithAllLights = new FullGroup("groupId", PLACEHOLDER, PLACEHOLDER, PLACEHOLDER_STATE, lights,
                PLACEHOLDER_STATE);
        assertThat(scene.isApplicableTo(groupWithAllLights), is(true));

        FullGroup groupWithMoreLights = new FullGroup("otherGroupId", PLACEHOLDER, PLACEHOLDER, PLACEHOLDER_STATE,
                moreLights, PLACEHOLDER_STATE);
        assertThat(scene.isApplicableTo(groupWithMoreLights), is(true));
    }

    /**
     * If a scene does not have a group ID, it should be NOT applicable to a group that does not contain all lights of
     * the scene.
     */
    @Test
    public void testIsApplicableToNoGroupIdSceneLightsNotContainedInGroup() {
        List<String> lights = List.of("1", "2");
        List<String> lessLights = List.of("1");
        List<String> differentLights = List.of("3");

        Scene scene = new Scene(PLACEHOLDER, PLACEHOLDER, null, lights, false);

        FullGroup groupWithLessLights = new FullGroup("groupId", PLACEHOLDER, PLACEHOLDER, PLACEHOLDER_STATE,
                lessLights, PLACEHOLDER_STATE);
        assertThat(scene.isApplicableTo(groupWithLessLights), is(false));

        FullGroup groupWithDifferentLights = new FullGroup("otherGroupId", PLACEHOLDER, PLACEHOLDER, PLACEHOLDER_STATE,
                differentLights, PLACEHOLDER_STATE);
        assertThat(scene.isApplicableTo(groupWithDifferentLights), is(false));
    }
}
