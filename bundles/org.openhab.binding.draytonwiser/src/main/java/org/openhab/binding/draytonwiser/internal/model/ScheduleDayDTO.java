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
package org.openhab.binding.draytonwiser.internal.model;

import java.util.List;

/**
 * @author Andrew Schofield - Initial contribution
 */
public class ScheduleDayDTO {

    private List<SetPointDTO> setPoints;

    public List<SetPointDTO> getSetPoints() {
        return setPoints;
    }

    public void setSetPoints(final List<SetPointDTO> setPoints) {
        this.setPoints = setPoints;
    }

}
