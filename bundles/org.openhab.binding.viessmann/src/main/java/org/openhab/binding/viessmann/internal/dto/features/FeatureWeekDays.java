/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.viessmann.internal.dto.features;

import java.util.List;

/**
 * The {@link FeatureWeekDays} provides weekdays for feature
 *
 * @author Ronny Grun - Initial contribution
 */
public class FeatureWeekDays {
    public List<FeatureDay> mon = null;
    public List<FeatureDay> tue = null;
    public List<FeatureDay> wed = null;
    public List<FeatureDay> thu = null;
    public List<FeatureDay> fri = null;
    public List<FeatureDay> sat = null;
    public List<FeatureDay> sun = null;
}
