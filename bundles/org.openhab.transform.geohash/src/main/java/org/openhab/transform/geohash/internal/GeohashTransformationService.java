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
package org.openhab.transform.geohash.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;

/**
 *
 * The implementation of {@link GeohashTransformationService} which simply maps coordinates
 * (latitude,longitude) to corresponding Geohash
 *
 * @author Gaël L'hopital - Initial contribution and API
 */
@Component(immediate = true, service = TransformationService.class, property = { "smarthome.transform=GEOHASH" })
@NonNullByDefault
public class GeohashTransformationService implements TransformationService {
    private final Logger logger = LoggerFactory.getLogger(GeohashTransformationService.class);
    private int DEFAULT_PRECISION = 6;

    @Override
    public @Nullable String transform(final String precision, final String coordinates) throws TransformationException {
        try {
            PointType point = new PointType(coordinates);
            try {
                int numcar = precision.isEmpty() ? DEFAULT_PRECISION : Integer.parseInt(precision);
                if (numcar > 0 && numcar <= 12) {
                    return GeoHash.withCharacterPrecision(point.getLatitude().doubleValue(),
                            point.getLongitude().doubleValue(), numcar).toBase32();
                } else {
                    throw new TransformationException(
                            String.format("Valid range for Precision is ]0,12] : '{}'", precision));
                }
            } catch (NumberFormatException e) {
                logger.info("The value '{}' is not valid precision level : {}", precision, e.getMessage());
            }
        } catch (IllegalArgumentException e) {
            try {
                GeoHash hash = GeoHash.fromGeohashString(coordinates);
                WGS84Point centerPoint = hash.getBoundingBox().getCenter();
                PointType point = new PointType(new DecimalType(centerPoint.getLatitude()),
                        new DecimalType(centerPoint.getLongitude()));
                return point.toString();
            } catch (NullPointerException e2) {
                throw new TransformationException(String
                        .format("The value '{}' is not valid geohash nor a valid coordinate expression", coordinates));
            }
        }
        return null;
    }
}
