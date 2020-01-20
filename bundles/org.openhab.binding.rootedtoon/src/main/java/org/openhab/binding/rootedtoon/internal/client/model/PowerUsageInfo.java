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

package org.openhab.binding.rootedtoon.internal.client.model;

/**
 *
 * @author daanmeijer - Initial Contribution
 *
 */
public class PowerUsageInfo {
    public AveragedValue powerUsage;

    public AveragedValue powerProduction;

    public AveragedValue gasUsage;

    @Override
    public String toString() {
        return String.format("powerUsage: %s, powerProduction: %s, gasUsage: %s", this.powerUsage, this.powerProduction,
                this.gasUsage);
    }
}
