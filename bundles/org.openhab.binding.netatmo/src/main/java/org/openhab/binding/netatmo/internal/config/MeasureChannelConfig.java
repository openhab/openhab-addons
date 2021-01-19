/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.config;

import org.openhab.binding.netatmo.internal.api.doc.NetatmoConstants.MeasureLimit;
import org.openhab.binding.netatmo.internal.api.doc.NetatmoConstants.MeasureScale;
import org.openhab.binding.netatmo.internal.api.doc.NetatmoConstants.MeasureType;

/**
 * The {@link MeasureChannelConfig} holds configuration parameters
 * for extensible channels
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
public class MeasureChannelConfig {
    public MeasureScale period;
    public MeasureType type;
    public MeasureLimit limit = MeasureLimit.NONE;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((limit == null) ? 0 : limit.hashCode());
        result = prime * result + ((period == null) ? 0 : period.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MeasureChannelConfig other = (MeasureChannelConfig) obj;
        if (limit != other.limit) {
            return false;
        }
        if (period != other.period) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        return true;
    }
}
