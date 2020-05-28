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
package org.openhab.binding.smarther.internal.api.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Smarther API Plant DTO class.
 *
 * @author Fabio Possieri - Initial contribution
 */
public class Plant {

    private String id;
    private String name;
    private List<Module> modules = new ArrayList<Module>();

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Module> getModules() {
        return modules;
    }

    @Override
    public String toString() {
        return String.format("id=%s, name=%s", id, name);
    }

}
