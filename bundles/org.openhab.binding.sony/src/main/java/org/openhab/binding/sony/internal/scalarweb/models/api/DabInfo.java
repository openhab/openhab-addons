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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The digital audio broadcasting (DAB) information class used for deserialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class DabInfo {
    /** The DAB component label */
    private @Nullable String componentLabel;

    /** The DAB dynamic label */
    private @Nullable String dynamicLabel;

    /** The DAB ensemble label */
    private @Nullable String ensembleLabel;

    /** The DAB service label */
    private @Nullable String serviceLabel;

    /**
     * Constructor used for deserialization only
     */
    public DabInfo() {
    }

    /**
     * Returns the DAB component label
     * 
     * @return the component label
     */
    public @Nullable String getComponentLabel() {
        return componentLabel;
    }

    /**
     * Returns the DAB dynamic label
     * 
     * @return the dynamic label
     */
    public @Nullable String getDynamicLabel() {
        return dynamicLabel;
    }

    /**
     * Returns the DAB ensemble label
     * 
     * @return the ensemble label
     */
    public @Nullable String getEnsembleLabel() {
        return ensembleLabel;
    }

    /**
     * Returns the DAB service label
     * 
     * @return the service label
     */
    public @Nullable String getServiceLabel() {
        return serviceLabel;
    }

    @Override
    public String toString() {
        return "DabInfo [componentLabel=" + componentLabel + ", dynamicLabel=" + dynamicLabel + ", ensembleLabel="
                + ensembleLabel + ", serviceLabel=" + serviceLabel + "]";
    }
}
