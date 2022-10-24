/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal.dto;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.types.StateOption;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

/**
 * Basic scene information.
 *
 * @author Hengrui Jiang - Initial contribution
 */
@NonNullByDefault
public class Scene {
    public static final Type GSON_TYPE = new TypeToken<Map<String, Scene>>() {
    }.getType();

    private @NonNullByDefault({}) String id;
    private @NonNullByDefault({}) String name;
    @SerializedName("lights")
    private @NonNullByDefault({}) List<String> lightIds;
    @SerializedName("group")
    private @Nullable String groupId;
    private boolean recycle;

    /**
     * Default constructor for GSon.
     */
    public Scene() {
        super();
    }

    /**
     * Test constructor
     */
    public Scene(String id, String name, @Nullable String groupId, List<String> lightIds, boolean recycle) {
        this.id = id;
        this.name = name;
        this.groupId = groupId;
        this.lightIds = lightIds;
        this.recycle = recycle;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the human readable name of the scene. If the name is omitted upon creation, this
     * defaults to the ID.
     *
     * @return human readable name of the scene
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the list of lights that the scene applies to. For group scenes, this list should be identical to the list
     * of all lights that are in the group.
     *
     * @return list of lights that the scene applies to
     */
    public List<String> getLightIds() {
        return lightIds;
    }

    /**
     * Returns the group that the scene belongs to. This field is optional for scenes that applies to a specific list of
     * lights instead of a group.
     *
     * @return the group that the scene belongs to
     */
    public @Nullable String getGroupId() {
        return groupId;
    }

    /**
     * Indicates if the scene can be recycled by the bridge. A recyclable scene is not able to be activated.
     *
     * @return whether the scene can be recycled
     */
    public boolean isRecycle() {
        return recycle;
    }

    /**
     * Creates a {@link StateOption} to display this scene, including the group that it belongs to.
     * <p>
     * The display name is built with the following pattern:
     * <ol>
     * <li>Human readable name of the scene if set. Otherwise, the ID is displayed</li>
     * <li>Group for which the scene is defined</li>
     * </ol>
     */
    public StateOption toStateOption(Map<String, String> groupNames) {
        StringBuilder stateOptionLabel = new StringBuilder(name);
        if (groupId != null && groupNames.containsKey(groupId)) {
            stateOptionLabel.append(" (").append(groupNames.get(groupId)).append(")");
        }

        return new StateOption(id, stateOptionLabel.toString());
    }

    /**
     * Creates a {@link StateOption} to display this scene.
     */
    public StateOption toStateOption() {
        return new StateOption(id, name);
    }

    /**
     * Returns whether the scene is applicable to the given group.
     * <p>
     * According to the hue API, a scene is applicable to a group if either
     * <ol>
     * <li>The scene is defined for the group</li>
     * <li>All lights of the scene also belong to the group</li>
     * </ol>
     */
    public boolean isApplicableTo(FullGroup group) {
        if (getGroupId() == null) {
            return getLightIds().stream().allMatch(id -> group.getLightIds().contains(id));
        } else {
            String groupId = getGroupId();
            return groupId != null ? group.getId().contentEquals(groupId) : false;
        }
    }

    public String extractKeyForComparator() {
        return (groupId != null ? groupId : "") + "#" + name;
    }

    @Override
    public String toString() {
        return String.format("{Scene name: %s; id: %s; lightIds: %s; groupId: %s; recycle: %s}", name, id, lightIds,
                groupId, recycle);
    }
}
