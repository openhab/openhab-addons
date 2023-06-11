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
package org.openhab.binding.buienradar.internal.buienradarapi;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.PointType;

/**
 * The {@link PredictionAPI} interface.
 *
 * @author Edwin de Jong - Initial contribution
 */
@NonNullByDefault
public interface PredictionAPI {

    Optional<List<Prediction>> getPredictions(PointType location) throws IOException;
}
