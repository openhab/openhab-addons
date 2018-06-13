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
 * The {@link Ihc2DimmerThingConfig} holds the resource id to channel map for a dimmer.
 *
 *
 * @author Niels Peter Enemark - Initial contribution
 */
public class Ihc2DimmerThingConfig {
    private int lightLevelResourceId;
    private int lightIndicationResourceId;

    public int getLightLevelResourceId() {
        return lightLevelResourceId;
    }

    public void setLightLevelResourceId(int lightLevelResourceId) {
        this.lightLevelResourceId = lightLevelResourceId;
    }

    public int getLightIndicationResourceId() {
        return lightIndicationResourceId;
    }

    public void setLightIndicationResourceId(int lightIndicationResourceId) {
        this.lightIndicationResourceId = lightIndicationResourceId;
    }

}
