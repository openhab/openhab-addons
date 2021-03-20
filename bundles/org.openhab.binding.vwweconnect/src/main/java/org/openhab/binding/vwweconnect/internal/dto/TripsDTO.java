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
package org.openhab.binding.vwweconnect.internal.dto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The Trips status representation.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class TripsDTO {

    private String errorCode = "";
    private RtsViewModelDTO rtsViewModel = new RtsViewModelDTO();

    public String getErrorCode() {
        return errorCode;
    }

    public RtsViewModelDTO getRtsViewModel() {
        return rtsViewModel;
    }

    @Override
    public String toString() {
        return "TripsDTO [errorCode=" + errorCode + ", rtsViewModel=" + rtsViewModel + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + errorCode.hashCode();
        result = prime * result + rtsViewModel.hashCode();
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TripsDTO other = (TripsDTO) obj;
        if (!errorCode.equals(other.errorCode)) {
            return false;
        }
        if (!rtsViewModel.equals(other.rtsViewModel)) {
            return false;
        }
        return true;
    }

    public class RtsViewModelDTO {

        private int daysInMonth;
        private int firstWeekday;
        private int month;
        private int year;
        private int firstTripYear;
        private List<TripStatisticDTO> tripStatistics = new ArrayList<>();
        private LongTermDataDTO longTermData = new LongTermDataDTO();
        private @Nullable Object cyclicData = new Object();
        private ServiceConfigurationDTO serviceConfiguration = new ServiceConfigurationDTO();
        private boolean tripFromLastRefuelAvailable;

        public int getDaysInMonth() {
            return daysInMonth;
        }

        public int getFirstWeekday() {
            return firstWeekday;
        }

        public int getMonth() {
            return month;
        }

        public int getYear() {
            return year;
        }

        public int getFirstTripYear() {
            return firstTripYear;
        }

        public List<TripStatisticDTO> getTripStatistics() {
            return tripStatistics;
        }

        public LongTermDataDTO getLongTermData() {
            return longTermData;
        }

        public @Nullable Object getCyclicData() {
            return cyclicData;
        }

        public ServiceConfigurationDTO getServiceConfiguration() {
            return serviceConfiguration;
        }

        public boolean isTripFromLastRefuelAvailable() {
            return tripFromLastRefuelAvailable;
        }

        @Override
        public String toString() {
            return "RtsViewModelDTO [daysInMonth=" + daysInMonth + ", firstWeekday=" + firstWeekday + ", month=" + month
                    + ", year=" + year + ", firstTripYear=" + firstTripYear + ", tripStatistics=" + tripStatistics
                    + ", longTermData=" + longTermData + ", cyclicData=" + cyclicData + ", serviceConfiguration="
                    + serviceConfiguration + ", tripFromLastRefuelAvailable=" + tripFromLastRefuelAvailable + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            Object cyclicData2 = cyclicData;
            result = prime * result + ((cyclicData2 == null) ? 0 : cyclicData2.hashCode());
            result = prime * result + daysInMonth;
            result = prime * result + firstTripYear;
            result = prime * result + firstWeekday;
            result = prime * result + longTermData.hashCode();
            result = prime * result + month;
            result = prime * result + serviceConfiguration.hashCode();
            result = prime * result + (tripFromLastRefuelAvailable ? 1231 : 1237);
            result = prime * result + tripStatistics.hashCode();
            result = prime * result + year;
            return result;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            RtsViewModelDTO other = (RtsViewModelDTO) obj;
            if (cyclicData != null && other.cyclicData != null && !cyclicData.equals(other.cyclicData)) {
                return false;
            }
            if (daysInMonth != other.daysInMonth) {
                return false;
            }
            if (firstTripYear != other.firstTripYear) {
                return false;
            }
            if (firstWeekday != other.firstWeekday) {
                return false;
            }
            if (!longTermData.equals(other.longTermData)) {
                return false;
            }
            if (month != other.month) {
                return false;
            }
            if (!serviceConfiguration.equals(other.serviceConfiguration)) {
                return false;
            }
            if (tripFromLastRefuelAvailable != other.tripFromLastRefuelAvailable) {
                return false;
            }
            if (!tripStatistics.equals(other.tripStatistics)) {
                return false;
            }
            if (year != other.year) {
                return false;
            }
            return true;
        }

        private TripsDTO getEnclosingInstance() {
            return TripsDTO.this;
        }
    }

    public class LongTermDataDTO {

        private int tripId;
        private double averageElectricConsumption = BaseVehicleDTO.UNDEFINED;
        private double averageFuelConsumption = BaseVehicleDTO.UNDEFINED;
        private double averageCngConsumption = BaseVehicleDTO.UNDEFINED;
        private double averageSpeed = BaseVehicleDTO.UNDEFINED;
        private int tripDuration = BaseVehicleDTO.UNDEFINED;
        private double tripLength = BaseVehicleDTO.UNDEFINED;
        private String timestamp = "";
        private String tripDurationFormatted = "";
        private @Nullable Object recuperation = new Object();
        private double averageAuxiliaryConsumption = BaseVehicleDTO.UNDEFINED;
        private double totalElectricConsumption = BaseVehicleDTO.UNDEFINED;
        private String longFormattedTimestamp = "";

        public int getTripId() {
            return tripId;
        }

        public double getAverageElectricConsumption() {
            return averageElectricConsumption;
        }

        public double getAverageFuelConsumption() {
            return averageFuelConsumption;
        }

        public double getAverageCngConsumption() {
            return averageCngConsumption;
        }

        public double getAverageSpeed() {
            return averageSpeed;
        }

        public int getTripDuration() {
            return tripDuration;
        }

        public double getTripLength() {
            return tripLength;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getTripDurationFormatted() {
            return tripDurationFormatted;
        }

        public @Nullable Object getRecuperation() {
            return recuperation;
        }

        public double getAverageAuxiliaryConsumption() {
            return averageAuxiliaryConsumption;
        }

        public double getTotalElectricConsumption() {
            return totalElectricConsumption;
        }

        public String getLongFormattedTimestamp() {
            return longFormattedTimestamp;
        }

        @Override
        public String toString() {
            return "LongTermDataDTO [tripId=" + tripId + ", averageElectricConsumption=" + averageElectricConsumption
                    + ", averageFuelConsumption=" + averageFuelConsumption + ", averageCngConsumption="
                    + averageCngConsumption + ", averageSpeed=" + averageSpeed + ", tripDuration=" + tripDuration
                    + ", tripLength=" + tripLength + ", timestamp=" + timestamp + ", tripDurationFormatted="
                    + tripDurationFormatted + ", recuperation=" + recuperation + ", averageAuxiliaryConsumption="
                    + averageAuxiliaryConsumption + ", totalElectricConsumption=" + totalElectricConsumption
                    + ", longFormattedTimestamp=" + longFormattedTimestamp + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            long temp;
            temp = Double.doubleToLongBits(averageAuxiliaryConsumption);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(averageCngConsumption);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(averageElectricConsumption);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(averageFuelConsumption);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(averageSpeed);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            result = prime * result + longFormattedTimestamp.hashCode();
            Object recuperation2 = recuperation;
            result = prime * result + ((recuperation2 == null) ? 0 : recuperation2.hashCode());
            result = prime * result + timestamp.hashCode();
            temp = Double.doubleToLongBits(totalElectricConsumption);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            result = prime * result + tripDuration;
            result = prime * result + tripDurationFormatted.hashCode();
            result = prime * result + tripId;
            temp = Double.doubleToLongBits(tripLength);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            return result;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            LongTermDataDTO other = (LongTermDataDTO) obj;
            if (Double.doubleToLongBits(averageAuxiliaryConsumption) != Double
                    .doubleToLongBits(other.averageAuxiliaryConsumption)) {
                return false;
            }
            if (Double.doubleToLongBits(averageCngConsumption) != Double
                    .doubleToLongBits(other.averageCngConsumption)) {
                return false;
            }
            if (Double.doubleToLongBits(averageElectricConsumption) != Double
                    .doubleToLongBits(other.averageElectricConsumption)) {
                return false;
            }
            if (Double.doubleToLongBits(averageFuelConsumption) != Double
                    .doubleToLongBits(other.averageFuelConsumption)) {
                return false;
            }
            if (Double.doubleToLongBits(averageSpeed) != Double.doubleToLongBits(other.averageSpeed)) {
                return false;
            }
            if (!longFormattedTimestamp.equals(other.longFormattedTimestamp)) {
                return false;
            }
            if (recuperation != null && other.recuperation != null && !recuperation.equals(other.recuperation)) {
                return false;
            }
            if (!timestamp.equals(other.timestamp)) {
                return false;
            }
            if (Double.doubleToLongBits(totalElectricConsumption) != Double
                    .doubleToLongBits(other.totalElectricConsumption)) {
                return false;
            }
            if (tripDuration != other.tripDuration) {
                return false;
            }
            if (!tripDurationFormatted.equals(other.tripDurationFormatted)) {
                return false;
            }
            if (tripId != other.tripId) {
                return false;
            }
            if (Double.doubleToLongBits(tripLength) != Double.doubleToLongBits(other.tripLength)) {
                return false;
            }
            return true;
        }

        private TripsDTO getEnclosingInstance() {
            return TripsDTO.this;
        }
    }

    public class AggregatedStatisticsDTO {

        private int tripId;
        private double averageElectricConsumption;
        private double averageFuelConsumption;
        private double averageCngConsumption;
        private double averageSpeed;
        private int tripDuration;
        private double tripLength;
        private String timestamp = "";
        private String tripDurationFormatted = "";
        private @Nullable Object recuperation = new Object();
        private double averageAuxiliaryConsumption;
        private double totalElectricConsumption;
        private @Nullable Object longFormattedTimestamp = new Object();

        public int getTripId() {
            return tripId;
        }

        public double getAverageElectricConsumption() {
            return averageElectricConsumption;
        }

        public double getAverageFuelConsumption() {
            return averageFuelConsumption;
        }

        public double getAverageCngConsumption() {
            return averageCngConsumption;
        }

        public double getAverageSpeed() {
            return averageSpeed;
        }

        public int getTripDuration() {
            return tripDuration;
        }

        public double getTripLength() {
            return tripLength;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getTripDurationFormatted() {
            return tripDurationFormatted;
        }

        public @Nullable Object getRecuperation() {
            return recuperation;
        }

        public double getAverageAuxiliaryConsumption() {
            return averageAuxiliaryConsumption;
        }

        public double getTotalElectricConsumption() {
            return totalElectricConsumption;
        }

        public @Nullable Object getLongFormattedTimestamp() {
            return longFormattedTimestamp;
        }

        @Override
        public String toString() {
            return "AggregatedStatisticsDTO [tripId=" + tripId + ", averageElectricConsumption="
                    + averageElectricConsumption + ", averageFuelConsumption=" + averageFuelConsumption
                    + ", averageCngConsumption=" + averageCngConsumption + ", averageSpeed=" + averageSpeed
                    + ", tripDuration=" + tripDuration + ", tripLength=" + tripLength + ", timestamp=" + timestamp
                    + ", tripDurationFormatted=" + tripDurationFormatted + ", recuperation=" + recuperation
                    + ", averageAuxiliaryConsumption=" + averageAuxiliaryConsumption + ", totalElectricConsumption="
                    + totalElectricConsumption + ", longFormattedTimestamp=" + longFormattedTimestamp + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            long temp;
            temp = Double.doubleToLongBits(averageAuxiliaryConsumption);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(averageCngConsumption);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(averageElectricConsumption);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(averageFuelConsumption);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(averageSpeed);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            Object longFormattedTimestamp2 = longFormattedTimestamp;
            result = prime * result + ((longFormattedTimestamp2 == null) ? 0 : longFormattedTimestamp2.hashCode());
            Object recuperation2 = recuperation;
            result = prime * result + ((recuperation2 == null) ? 0 : recuperation2.hashCode());
            result = prime * result + timestamp.hashCode();
            temp = Double.doubleToLongBits(totalElectricConsumption);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            result = prime * result + tripDuration;
            result = prime * result + tripDurationFormatted.hashCode();
            result = prime * result + tripId;
            temp = Double.doubleToLongBits(tripLength);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            return result;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            AggregatedStatisticsDTO other = (AggregatedStatisticsDTO) obj;
            if (Double.doubleToLongBits(averageAuxiliaryConsumption) != Double
                    .doubleToLongBits(other.averageAuxiliaryConsumption)) {
                return false;
            }
            if (Double.doubleToLongBits(averageCngConsumption) != Double
                    .doubleToLongBits(other.averageCngConsumption)) {
                return false;
            }
            if (Double.doubleToLongBits(averageElectricConsumption) != Double
                    .doubleToLongBits(other.averageElectricConsumption)) {
                return false;
            }
            if (Double.doubleToLongBits(averageFuelConsumption) != Double
                    .doubleToLongBits(other.averageFuelConsumption)) {
                return false;
            }
            if (Double.doubleToLongBits(averageSpeed) != Double.doubleToLongBits(other.averageSpeed)) {
                return false;
            }
            if (longFormattedTimestamp != null && other.longFormattedTimestamp != null
                    && !longFormattedTimestamp.equals(other.longFormattedTimestamp)) {
                return false;
            }
            if (recuperation != null && other.recuperation != null && !recuperation.equals(other.recuperation)) {
                return false;
            }
            if (!timestamp.equals(other.timestamp)) {
                return false;
            }
            if (Double.doubleToLongBits(totalElectricConsumption) != Double
                    .doubleToLongBits(other.totalElectricConsumption)) {
                return false;
            }
            if (tripDuration != other.tripDuration) {
                return false;
            }
            if (!tripDurationFormatted.equals(other.tripDurationFormatted)) {
                return false;
            }
            if (tripId != other.tripId) {
                return false;
            }
            if (Double.doubleToLongBits(tripLength) != Double.doubleToLongBits(other.tripLength)) {
                return false;
            }
            return true;
        }

        private TripsDTO getEnclosingInstance() {
            return TripsDTO.this;
        }
    }

    public class ServiceConfigurationDTO {

        private boolean electricConsumption;
        private boolean triptypeShort;
        private boolean auxiliaryConsumption;
        private boolean fuelOverallConsumption;
        private boolean triptypeCyclic;
        private boolean electricOverallConsumption;
        private boolean triptypeLong;
        private boolean cngOverallConsumption;
        private boolean recuperation;

        public boolean isElectricConsumption() {
            return electricConsumption;
        }

        public boolean isTriptypeShort() {
            return triptypeShort;
        }

        public boolean isAuxiliaryConsumption() {
            return auxiliaryConsumption;
        }

        public boolean isFuelOverallConsumption() {
            return fuelOverallConsumption;
        }

        public boolean isTriptypeCyclic() {
            return triptypeCyclic;
        }

        public boolean isElectricOverallConsumption() {
            return electricOverallConsumption;
        }

        public boolean isTriptypeLong() {
            return triptypeLong;
        }

        public boolean isCngOverallConsumption() {
            return cngOverallConsumption;
        }

        public boolean isRecuperation() {
            return recuperation;
        }

        @Override
        public String toString() {
            return "ServiceConfigurationDTO [electricConsumption=" + electricConsumption + ", triptypeShort="
                    + triptypeShort + ", auxiliaryConsumption=" + auxiliaryConsumption + ", fuelOverallConsumption="
                    + fuelOverallConsumption + ", triptypeCyclic=" + triptypeCyclic + ", electricOverallConsumption="
                    + electricOverallConsumption + ", triptypeLong=" + triptypeLong + ", cngOverallConsumption="
                    + cngOverallConsumption + ", recuperation=" + recuperation + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + (auxiliaryConsumption ? 1231 : 1237);
            result = prime * result + (cngOverallConsumption ? 1231 : 1237);
            result = prime * result + (electricConsumption ? 1231 : 1237);
            result = prime * result + (electricOverallConsumption ? 1231 : 1237);
            result = prime * result + (fuelOverallConsumption ? 1231 : 1237);
            result = prime * result + (recuperation ? 1231 : 1237);
            result = prime * result + (triptypeCyclic ? 1231 : 1237);
            result = prime * result + (triptypeLong ? 1231 : 1237);
            result = prime * result + (triptypeShort ? 1231 : 1237);
            return result;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ServiceConfigurationDTO other = (ServiceConfigurationDTO) obj;
            if (auxiliaryConsumption != other.auxiliaryConsumption) {
                return false;
            }
            if (cngOverallConsumption != other.cngOverallConsumption) {
                return false;
            }
            if (electricConsumption != other.electricConsumption) {
                return false;
            }
            if (electricOverallConsumption != other.electricOverallConsumption) {
                return false;
            }
            if (fuelOverallConsumption != other.fuelOverallConsumption) {
                return false;
            }
            if (recuperation != other.recuperation) {
                return false;
            }
            if (triptypeCyclic != other.triptypeCyclic) {
                return false;
            }
            if (triptypeLong != other.triptypeLong) {
                return false;
            }
            if (triptypeShort != other.triptypeShort) {
                return false;
            }
            return true;
        }

        private TripsDTO getEnclosingInstance() {
            return TripsDTO.this;
        }
    }

    public class TripStatisticDTO {

        private AggregatedStatisticsDTO aggregatedStatistics = new AggregatedStatisticsDTO();
        private List<TripStatisticDetailDTO> tripStatistics = new ArrayList<>();

        public AggregatedStatisticsDTO getAggregatedStatistics() {
            return aggregatedStatistics;
        }

        public List<TripStatisticDetailDTO> getTripStatistics() {
            return tripStatistics;
        }

        @Override
        public String toString() {
            return "TripStatisticDTO [aggregatedStatistics=" + aggregatedStatistics + ", tripStatistics="
                    + tripStatistics + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + aggregatedStatistics.hashCode();
            result = prime * result + tripStatistics.hashCode();
            return result;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            TripStatisticDTO other = (TripStatisticDTO) obj;
            if (!aggregatedStatistics.equals(other.aggregatedStatistics)) {
                return false;
            }
            if (!tripStatistics.equals(other.tripStatistics)) {
                return false;
            }
            return true;
        }

        private TripsDTO getEnclosingInstance() {
            return TripsDTO.this;
        }
    }

    public class TripStatisticDetailDTO {

        private int tripId;
        private double averageElectricConsumption = BaseVehicleDTO.UNDEFINED;
        private double averageFuelConsumption = BaseVehicleDTO.UNDEFINED;
        private double averageCngConsumption = BaseVehicleDTO.UNDEFINED;
        private double averageSpeed = BaseVehicleDTO.UNDEFINED;
        private int tripDuration = BaseVehicleDTO.UNDEFINED;
        private double tripLength = BaseVehicleDTO.UNDEFINED;
        private String timestamp = "";
        private String tripDurationFormatted = "";
        private @Nullable Object recuperation = new Object();
        private double averageAuxiliaryConsumption = BaseVehicleDTO.UNDEFINED;
        private double totalElectricConsumption = BaseVehicleDTO.UNDEFINED;
        private String longFormattedTimestamp = "";

        public int getTripId() {
            return tripId;
        }

        public double getAverageElectricConsumption() {
            return averageElectricConsumption;
        }

        public double getAverageFuelConsumption() {
            return averageFuelConsumption;
        }

        public double getAverageCngConsumption() {
            return averageCngConsumption;
        }

        public double getAverageSpeed() {
            return averageSpeed;
        }

        public int getTripDuration() {
            return tripDuration;
        }

        public double getTripLength() {
            return tripLength;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getTripDurationFormatted() {
            return tripDurationFormatted;
        }

        public @Nullable Object getRecuperation() {
            return recuperation;
        }

        public double getAverageAuxiliaryConsumption() {
            return averageAuxiliaryConsumption;
        }

        public double getTotalElectricConsumption() {
            return totalElectricConsumption;
        }

        public String getLongFormattedTimestamp() {
            return longFormattedTimestamp;
        }

        public @Nullable ZonedDateTime getStartTimestamp() {
            String[] splitStrings = longFormattedTimestamp.split(",");
            String dateString = splitStrings[1].trim() + ", " + splitStrings[2].trim();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d.MM.yyyy, HH:mm", Locale.getDefault());
            LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);
            ZonedDateTime zdt = dateTime.atZone(ZoneId.systemDefault());

            return zdt.minusMinutes(tripDuration);
        }

        public @Nullable ZonedDateTime getEndTimestamp() {
            String[] splitStrings = longFormattedTimestamp.split(",");
            String dateString = splitStrings[1].trim() + ", " + splitStrings[2].trim();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d.MM.yyyy, HH:mm", Locale.getDefault());
            LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);
            ZonedDateTime zdt = dateTime.atZone(ZoneId.systemDefault());

            return zdt;
        }

        @Override
        public String toString() {
            return "TripStatisticDetailDTO [tripId=" + tripId + ", averageElectricConsumption="
                    + averageElectricConsumption + ", averageFuelConsumption=" + averageFuelConsumption
                    + ", averageCngConsumption=" + averageCngConsumption + ", averageSpeed=" + averageSpeed
                    + ", tripDuration=" + tripDuration + ", tripLength=" + tripLength + ", timestamp=" + timestamp
                    + ", tripDurationFormatted=" + tripDurationFormatted + ", recuperation=" + recuperation
                    + ", averageAuxiliaryConsumption=" + averageAuxiliaryConsumption + ", totalElectricConsumption="
                    + totalElectricConsumption + ", longFormattedTimestamp=" + longFormattedTimestamp + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            long temp;
            temp = Double.doubleToLongBits(averageAuxiliaryConsumption);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(averageCngConsumption);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(averageElectricConsumption);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(averageFuelConsumption);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(averageSpeed);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            result = prime * result + longFormattedTimestamp.hashCode();
            Object recuperation2 = recuperation;
            result = prime * result + ((recuperation2 == null) ? 0 : recuperation2.hashCode());
            result = prime * result + timestamp.hashCode();
            temp = Double.doubleToLongBits(totalElectricConsumption);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            result = prime * result + tripDuration;
            result = prime * result + tripDurationFormatted.hashCode();
            result = prime * result + tripId;
            temp = Double.doubleToLongBits(tripLength);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            return result;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            TripStatisticDetailDTO other = (TripStatisticDetailDTO) obj;
            if (Double.doubleToLongBits(averageAuxiliaryConsumption) != Double
                    .doubleToLongBits(other.averageAuxiliaryConsumption)) {
                return false;
            }
            if (Double.doubleToLongBits(averageCngConsumption) != Double
                    .doubleToLongBits(other.averageCngConsumption)) {
                return false;
            }
            if (Double.doubleToLongBits(averageElectricConsumption) != Double
                    .doubleToLongBits(other.averageElectricConsumption)) {
                return false;
            }
            if (Double.doubleToLongBits(averageFuelConsumption) != Double
                    .doubleToLongBits(other.averageFuelConsumption)) {
                return false;
            }
            if (Double.doubleToLongBits(averageSpeed) != Double.doubleToLongBits(other.averageSpeed)) {
                return false;
            }
            if (!longFormattedTimestamp.equals(other.longFormattedTimestamp)) {
                return false;
            }
            if (recuperation != null && !recuperation.equals(other.recuperation)) {
                return false;
            }
            if (!timestamp.equals(other.timestamp)) {
                return false;
            }
            if (Double.doubleToLongBits(totalElectricConsumption) != Double
                    .doubleToLongBits(other.totalElectricConsumption)) {
                return false;
            }
            if (tripDuration != other.tripDuration) {
                return false;
            }
            if (!tripDurationFormatted.equals(other.tripDurationFormatted)) {
                return false;
            }
            if (tripId != other.tripId) {
                return false;
            }
            if (Double.doubleToLongBits(tripLength) != Double.doubleToLongBits(other.tripLength)) {
                return false;
            }
            return true;
        }

        private TripsDTO getEnclosingInstance() {
            return TripsDTO.this;
        }
    }
}
