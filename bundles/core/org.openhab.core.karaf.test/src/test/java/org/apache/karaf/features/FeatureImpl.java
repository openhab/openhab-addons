/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.apache.karaf.features;

public class FeatureImpl implements Feature {

    private String version;
    private String desc;
    private String name;
    private String id;

    public FeatureImpl(String id, String name, String desc, String version) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.version = version;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return desc;
    }

    @Override
    public String getVersion() {
        return version;
    }

}
