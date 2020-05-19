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
package org.openhab.binding.paradoxalarm.internal.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Partition} Paradox partition.
 * ID is always numeric (1-8 for Evo192)
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class Partition extends Entity {

    private final Logger logger = LoggerFactory.getLogger(Partition.class);

    private PartitionState state = new PartitionState();

    public Partition(int id, String label) {
        super(id, label);
    }

    public PartitionState getState() {
        return state;
    }

    public Partition setState(PartitionState state) {
        this.state = state;
        logger.debug("Partition {}:\t{}", getLabel(), getState().getMainState());
        return this;
    }
}
