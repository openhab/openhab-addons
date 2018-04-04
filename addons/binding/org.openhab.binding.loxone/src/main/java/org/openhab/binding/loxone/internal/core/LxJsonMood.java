/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.loxone.internal.core;

import com.google.gson.annotations.SerializedName;

/**
 * A JSON structure of a mood of {@link LxControlLightControllerV2}. This structure is an item of a JSON array received
 * in moodList state update of this controller in the runtime.
 * <p>
 * This structure is used for parsing with Gson library.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
class LxJsonMood {
    /**
     * The userfriendly name for this mood
     */
    String name;

    /**
     * An ID that uniquely identifies this mood (e.g. inside activeMoods)
     */
    Integer id;

    /**
     * Bitmask that tells if the mood is used for a specific purpose in the logic.
     * If it’s not used, it can be removed without affecting the logic on the Miniserver.
     * 0: not used
     * 1: this mood is activated by a movement event
     * 2: a T5 or other inputs activate/deactivate this mood
     */
    @SerializedName("used")
    Integer isUsed;

    /**
     * Whether or not this mood can be controlled with a t5 input
     */
    @SerializedName("t5")
    Boolean isT5Controlled;

    /**
     * If a mood is marked as static it cannot be deleted or modified in any way.
     * But it can be moved within and between favorite and additional lists.
     */
    @SerializedName("static")
    Boolean isStatic;
}
