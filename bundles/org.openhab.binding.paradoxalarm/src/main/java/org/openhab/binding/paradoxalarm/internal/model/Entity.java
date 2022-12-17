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
package org.openhab.binding.paradoxalarm.internal.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Entity} Entity - base abstract class for Paradox entities (Partitions, zones, etc).
 * Extend this class and add entity specific data (states, troubles, etc).
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public abstract class Entity {
    private final Logger logger = LoggerFactory.getLogger(Entity.class);

    private ParadoxPanel panel;
    private int id;
    private String label;

    public Entity(ParadoxPanel panel, int id, String label) {
        this.panel = panel;
        this.id = id;
        this.label = label.trim();
        logger.debug("Creating entity with label: {} and ID: {}", label, id);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public ParadoxPanel getPanel() {
        return panel;
    }

    @Override
    public String toString() {
        return "Entity [id=" + id + ", label=" + label + "]";
    }
}
