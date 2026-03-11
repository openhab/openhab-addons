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
package org.openhab.binding.atmofrance.internal.api.dto;

import java.time.Instant;
import java.util.List;

import javax.measure.Unit;

import org.openhab.core.library.dimension.Density;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import tech.units.indriya.unit.ProductUnit;

/**
 * {@link AtmoFranceDto} class defines DTO used to interact with server api
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
public class AtmoFranceDto {
    public static final Unit<Density> GRAIN_PER_CUBICMETER = new ProductUnit<>(
            ImperialUnits.GRAIN.divide(tech.units.indriya.unit.Units.CUBIC_METRE));

    public record LoginResponse(String token) {
    }

    public record ErrorResponse(int code, String message) {
    }

    public record AtmoResponse(String type, String name, Crs crs, List<Feature<IndexProperties>> features) {
    }

    public record PollensResponse(String type, String name, Crs crs, List<Feature<PollensProperties>> features) {
    }

    public record Crs(String type, Properties properties) {
    }

    public record Properties(String name) {
    }

    public record Feature<T extends BaseProperties> (String type, T properties, Geometry geometry) {
    }

    public record Geometry(String type, List<Double> coordinates) {
    }

    public class BaseProperties {
        public String aasqa;

        public Instant dateMaj;
        public Instant dateDif;
        public Instant dateEch;

        public String codeZone;
        public String libZone;
        public String typeZone;

        public String libQual;
        public String source;
    }

    public class PollensProperties extends BaseProperties {
        public boolean alerte;

        private int codeAmbr;
        private int codeArm;
        private int codeAul;
        private int codeBoul;
        private int codeGram;
        private int codeOliv;

        private int codeQual;

        private double concAmbr;
        private double concArm;
        private double concAul;
        private double concBoul;
        private double concGram;
        private double concOliv;

        public String pollenResp;

        public State getGlobal() {
            return new DecimalType(PollenIndex.valueOf(codeQual).value);
        }

        public State getTaxonIndex(Taxon taxon) {
            int result = switch (taxon) {
                case ALDER -> codeAul;
                case BIRCH -> codeBoul;
                case OLIVE -> codeOliv;
                case GRASSES -> codeGram;
                case WORMWOOD -> codeArm;
                case RAGWEED -> codeAmbr;
                default -> 9;
            };
            return new DecimalType(PollenIndex.valueOf(result).value);
        }

        public State getTaxonConc(Taxon taxon) {
            double result = switch (taxon) {
                case ALDER -> concAul;
                case BIRCH -> concBoul;
                case OLIVE -> concOliv;
                case GRASSES -> concGram;
                case WORMWOOD -> concArm;
                case RAGWEED -> concAmbr;
                default -> -1;
            };

            return result != -1 ? new QuantityType<>(result, GRAIN_PER_CUBICMETER) : UnDefType.NULL;
        }
    }

    public class IndexProperties extends BaseProperties {
        private AtmoIndex codeNo2;
        private AtmoIndex codeO3;
        private AtmoIndex codePm10;
        private AtmoIndex codePm25;
        private AtmoIndex codeSo2;

        public AtmoIndex codeQual;
        public String coulQual;

        public String epsgReg;
        public double xReg;
        public double xWgs84;
        public double yReg;
        public double yWgs84;

        public State getPollutantIndex(Pollutant pollutant) {
            AtmoIndex result = switch (pollutant) {
                case NO2 -> codeNo2;
                case O3 -> codeO3;
                case PM10 -> codePm10;
                case PM25 -> codePm25;
                case SO2 -> codeSo2;
                default -> AtmoIndex.UNKNOWN;
            };
            return new DecimalType(result.value);
        }
    }
}
