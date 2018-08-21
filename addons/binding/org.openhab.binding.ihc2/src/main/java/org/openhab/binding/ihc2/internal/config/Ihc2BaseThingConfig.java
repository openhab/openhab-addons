/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc2.internal.config;

/**
 * The {@link Ihc2BaseThingConfig} holds the resource id from the the LK IHC Controller.
 *
 *
 * @author Niels Peter Enemark - Initial contribution
 */

public class Ihc2BaseThingConfig {
    private String resourceId;

    public int getResourceId() {
        return Integer.decode(resourceId);
    }

    public void setResourceId(String datalineId) {
        this.resourceId = datalineId;
    }
}
