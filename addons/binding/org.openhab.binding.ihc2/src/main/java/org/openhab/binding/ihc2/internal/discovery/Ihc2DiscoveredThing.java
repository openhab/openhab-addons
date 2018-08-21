/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc2.internal.discovery;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link Ihc2DiscoveredThing} thing info extracted from IHC project file.
 * sent to one of the channels.
 *
 * @author Niels Peter Enemark - Initial contribution
 */
public class Ihc2DiscoveredThing {

    private ThingTypeUID thingTypeUID;
    private String name;
    private String location;
    private String group;
    private String product;
    private String resourceId;
    private boolean isLinked;

    public ThingTypeUID getThingTypeUID() {
        return thingTypeUID;
    }

    public void setThingTypeUID(ThingTypeUID thingTypeUID) {
        this.thingTypeUID = thingTypeUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public boolean isLinked() {
        return isLinked;
    }

    public void setLinked(boolean isLinked) {
        this.isLinked = isLinked;
    }
}
