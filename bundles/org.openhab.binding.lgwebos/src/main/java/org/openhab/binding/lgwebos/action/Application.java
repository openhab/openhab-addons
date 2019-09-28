/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.lgwebos.action;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This represents id and name of a WebOS application.
 *
 * @author Sebastian Prehn - Initial contribution
 */
@NonNullByDefault
public class Application {
    private String id;
    private String name;

    public Application(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Application [id=" + id + ", name=" + name + "]";
    }

}
