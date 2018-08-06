/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.neeo.internal.models;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.io.neeo.internal.NeeoUtil;

/**
 * Represents a directory action request (action on a leaf value). This class is simply used for deserialization from
 * the brain.
 *
 * @author Tim Roberts - Initial Contribution
 *
 */
@NonNullByDefault
public class NeeoDirectoryRequestAction {
    /** The action identifier from the item */
    private final String actionIdentifier;

    /**
     * Constructs the request action from the action identifier
     *
     * @param actionIdentifier a non-null, non-empty action identifier
     */
    public NeeoDirectoryRequestAction(String actionIdentifier) {
        NeeoUtil.requireNotEmpty(actionIdentifier, "actionIdentifier cannot be empty");
        this.actionIdentifier = actionIdentifier;
    }

    /**
     * Returns the action identifier
     *
     * @return a non-null, non-empty action identifier
     */
    public String getActionIdentifier() {
        return actionIdentifier;
    }

    @Override
    public String toString() {
        return "NeeoDiscoveryRequestAction [actionIdentifier=" + actionIdentifier + "]";
    }
}
