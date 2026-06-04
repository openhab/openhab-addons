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
// AUTO-GENERATED, DO NOT EDIT!

package org.openhab.binding.matter.internal.client.dto.cluster.gen;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * CommodityTariff
 *
 * @author Dan Cunningham - Initial contribution
 */
public class CommodityTariffCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0700;
    public static final String CLUSTER_NAME = "CommodityTariff";
    public static final String CLUSTER_PREFIX = "commodityTariff";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_TARIFF_INFO = "tariffInfo";
    public static final String ATTRIBUTE_TARIFF_UNIT = "tariffUnit";
    public static final String ATTRIBUTE_START_DATE = "startDate";
    public static final String ATTRIBUTE_DAY_ENTRIES = "dayEntries";
    public static final String ATTRIBUTE_DAY_PATTERNS = "dayPatterns";
    public static final String ATTRIBUTE_CALENDAR_PERIODS = "calendarPeriods";
    public static final String ATTRIBUTE_INDIVIDUAL_DAYS = "individualDays";
    public static final String ATTRIBUTE_CURRENT_DAY = "currentDay";
    public static final String ATTRIBUTE_NEXT_DAY = "nextDay";
    public static final String ATTRIBUTE_CURRENT_DAY_ENTRY = "currentDayEntry";
    public static final String ATTRIBUTE_CURRENT_DAY_ENTRY_DATE = "currentDayEntryDate";
    public static final String ATTRIBUTE_NEXT_DAY_ENTRY = "nextDayEntry";
    public static final String ATTRIBUTE_NEXT_DAY_ENTRY_DATE = "nextDayEntryDate";
    public static final String ATTRIBUTE_TARIFF_COMPONENTS = "tariffComponents";
    public static final String ATTRIBUTE_TARIFF_PERIODS = "tariffPeriods";
    public static final String ATTRIBUTE_CURRENT_TARIFF_COMPONENTS = "currentTariffComponents";
    public static final String ATTRIBUTE_NEXT_TARIFF_COMPONENTS = "nextTariffComponents";
    public static final String ATTRIBUTE_DEFAULT_RANDOMIZATION_OFFSET = "defaultRandomizationOffset";
    public static final String ATTRIBUTE_DEFAULT_RANDOMIZATION_TYPE = "defaultRandomizationType";

    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates basic information about the tariff.
     * If the tariff is unavailable, this attribute shall be null.
     */
    public TariffInformationStruct tariffInfo; // 0 TariffInformationStruct R V
    /**
     * Indicates the unit of the commodity for pricing.
     * If the tariff is unavailable, this attribute shall be null.
     */
    public TariffUnitEnum tariffUnit; // 1 TariffUnitEnum R V
    /**
     * Indicates a timestamp in UTC to denote the time at which the published calendar becomes active. A start date/time
     * of 0x00000000 shall indicate that the calendar should become active immediately.
     * If the tariff is unavailable, this attribute shall be null.
     */
    public Integer startDate; // 2 epoch-s R V
    /**
     * Indicates the list of DayEntryStructs included in this calendar.
     * The maximum constraint of this attribute is intended to allow representation of seven days of tariff information
     * at 15 minute intervals.
     * If the tariff is unavailable, this attribute shall be null.
     */
    public List<DayEntryStruct> dayEntries; // 3 list R V
    /**
     * Indicates the list of DayPatternStructs used in the CalendarPeriodStruct in this calendar.
     * Each day pattern in this list shall define the specific schedule that applies to the days it covers.
     */
    public List<DayPatternStruct> dayPatterns; // 4 list R V
    /**
     * Indicates the list of CalendarPeriodStructs comprising this calendar. The CalendarPeriodStructs in this list
     * shall be arranged in increasing order by the value of StartDate.
     * If and only if the value of the StartDate attribute is null, the value of the StartDate field on the first
     * CalendarPeriodStruct in the CalendarPeriods attribute shall also be null, indicating that the period begins
     * immediately. The value of the StartDate field on any subsequent CalendarPeriodStruct in CalendarPeriods shall NOT
     * be null.
     * The active calendar period shall be in effect until the StartDate of the next calendar period.
     * If the calendar is unavailable, this attribute shall be null.
     */
    public List<CalendarPeriodStruct> calendarPeriods; // 5 list R V
    /**
     * Indicates the list of days to overlay on this calendar.
     * The DayStruct in this list shall be arranged in increasing order by the value of Date. The DayStruct in this list
     * shall not overlap.
     * If the calendar is unavailable, this attribute shall be null.
     */
    public List<DayStruct> individualDays; // 6 list R V
    /**
     * Indicates the current day's day entries.
     * If the tariff is not active or CurrentDay information is not available, this attribute shall be null.
     */
    public DayStruct currentDay; // 7 DayStruct R V
    /**
     * Indicates the next day's day entries.
     * If the tariff is not active or NextDay information is not available, this attribute shall be null.
     */
    public DayStruct nextDay; // 8 DayStruct R V
    /**
     * Indicates the currently active DayEntryStruct.
     * If the tariff is not active or day entry information is not available, this attribute shall be null.
     */
    public DayEntryStruct currentDayEntry; // 9 DayEntryStruct R V
    /**
     * Indicates the UTC date when the CurrentDay, CurrentDayEntry, and CurrentTariffComponents attributes were last
     * updated.
     * If the tariff is not active or day entry information is not available, this attribute shall be null.
     */
    public Integer currentDayEntryDate; // 10 epoch-s R V
    /**
     * Indicates the predicted next active DayEntryStruct.
     * If the tariff is not active or is not available, this attribute shall be null.
     */
    public DayEntryStruct nextDayEntry; // 11 DayEntryStruct R V
    /**
     * Indicates the predicted UTC date when the CurrentDay, CurrentDayEntry, and CurrentTariffComponents attributes
     * will update to the values in the NextDay, NextDayEntry, and NextTariffComponents attributes, respectively.
     * If the tariff is not active or is not available, this attribute shall be null.
     */
    public Integer nextDayEntryDate; // 12 epoch-s R V
    /**
     * Indicates a list of TariffComponentStructs for the tariff.
     * If the tariff is unavailable, this attribute shall be empty.
     */
    public List<TariffComponentStruct> tariffComponents; // 13 list R V
    /**
     * Indicates a list of TariffPeriodStructs for the tariff.
     * If the tariff is unavailable, this attribute shall be empty.
     */
    public List<TariffPeriodStruct> tariffPeriods; // 14 list R V
    /**
     * Indicates a list of the currently active TariffComponentStructs.
     * If the tariff is unavailable, this attribute shall be empty.
     */
    public List<TariffComponentStruct> currentTariffComponents; // 15 list R V
    /**
     * Indicates the predicted next active TariffComponentStructs.
     * If the tariff is unavailable, this attribute shall be null.
     */
    public List<TariffComponentStruct> nextTariffComponents; // 16 list R V
    /**
     * Indicates a default randomization offset for DayEntryStructs in this tariff. See RandomizationOffset for details.
     */
    public Integer defaultRandomizationOffset; // 17 int16 R V
    /**
     * Indicates a default randomization type for DayEntryStruct in this tariff. See RandomizationType for details.
     */
    public DayEntryRandomizationTypeEnum defaultRandomizationType; // 18 DayEntryRandomizationTypeEnum R V

    // Structs
    /**
     * This represents the settings for a given auxiliary load switch in a tariff component.
     */
    public static class AuxiliaryLoadSwitchSettingsStruct {
        public Integer number; // uint8
        public AuxiliaryLoadSettingEnum requiredState; // AuxiliaryLoadSettingEnum

        public AuxiliaryLoadSwitchSettingsStruct(Integer number, AuxiliaryLoadSettingEnum requiredState) {
            this.number = number;
            this.requiredState = requiredState;
        }
    }

    /**
     * This represents the set of auxiliary load settings in a tariff component.
     */
    public static class AuxiliaryLoadSwitchesSettingsStruct {
        public List<AuxiliaryLoadSwitchSettingsStruct> switchStates; // list

        public AuxiliaryLoadSwitchesSettingsStruct(List<AuxiliaryLoadSwitchSettingsStruct> switchStates) {
            this.switchStates = switchStates;
        }
    }

    /**
     * This represents a sub period of a calendar, commencing on its StartDate.
     * > [!NOTE]
     * > NOTE: A 'Calendar Period', while normally considered to be a 3 or 6 month period, could be used for other
     * arbitrary periods e.g. monthly or quarterly. The minimum resolution is 1 day, although a week would normally be
     * the smallest interval.
     */
    public static class CalendarPeriodStruct {
        /**
         * This field shall indicate the timestamp in UTC when the calendar period becomes active.
         * A null value shall indicate the calendar period becomes active immediately. See CalendarPeriods attribute.
         */
        public Integer startDate; // epoch-s
        /**
         * This field shall indicate a list of DayPatternIDs for the DayPatternStructs in use during this calendar
         * period.
         * If any of the DayPatternStructs referenced by this list has no bits set in DaysOfWeek, then none of the
         * DayPatternStructs referenced by this list shall have any bits set, and the list shall be treated as a
         * rotating set of days.
         * If at least one of the DayPatternStructs referenced by this list has a bit set in DaysOfWeek, then:
         * 1. All of the DayPatternStructs referenced by this list shall have at least one bit set in DaysOfWeek
         * 2. No two DayPatternStructs referenced by this list shall have the same bit set in DaysOfWeek.
         * 3. Every bit defined in DayPatternDayOfWeekBitmap referenced by this list shall be set in DaysOfWeek on one
         * of the DayPatternStructs.
         * Meeting these constraints ensures that every day of the week during this week has specified day entries, and
         * no day of the week has more than one set of day entries.
         */
        public List<Integer> dayPatternIDs; // list

        public CalendarPeriodStruct(Integer startDate, List<Integer> dayPatternIDs) {
            this.startDate = startDate;
            this.dayPatternIDs = dayPatternIDs;
        }
    }

    /**
     * This struct represents a day entry at a particular time of day, along with an optional duration and randomization
     * parameters.
     */
    public static class DayEntryStruct {
        /**
         * This field shall indicate an identifier for the day entry which is unique on the server to a set of values:
         * 1. If this identifier is included in the DayEntryIDs associated with a DayPatternStruct, then the identifier
         * shall be unique to the combination of:
         * 1. the StartTime
         * 2. the Duration, if indicated
         * 3. the DaysOfWeek field in the containing DayPatternStruct
         * 2. Otherwise, if this identifier is included in the DayEntryIDs associated with a DayStruct, then the
         * identifier shall be unique to the combination of:
         * 1. the StartTime
         * 2. the Duration, if indicated
         * 3. the Date field in the containing DayStruct
         * Once an identifier has been used for a given combination above, it shall never be used for any other
         * combination of these values.
         */
        public Integer dayEntryId; // uint32
        /**
         * This field shall indicate the start time of the DayEntryStruct, expressed as the number of minutes that have
         * elapsed since midnight on the associated days.
         * For example, 6am will be represented by 360 minutes since midnight and 11:30pm will be represented by 1410
         * minutes since midnight.
         * This will differ on days with a day entry in or out of daylight saving time. For example, if the
         * implementation of daylight saving time in a region means that the hour between 2am and 3am will be skipped on
         * the day entry into daylight saving time, then 2am cannot be represented, and 3am will be represented by 120
         * minutes.
         * Similarly, if the implementation of daylight saving time in a region means that the hour between 2am and 3am
         * will be repeated on the date of day entry out of daylight saving time, then the first 2am will be represented
         * by 120 minutes, while the second 2am will be represented by 180 minutes, and 4am will be represented by 300
         * minutes. As such, the maximum start time on this day is 1499, 60 minutes longer than all other days.
         * DayEntryStruct on daylight saving time days SHOULD be handled by a DayStruct in the IndividualDays attribute
         * whose Date field indicates the date of the day entry.
         */
        public Integer startTime; // uint16
        /**
         * This field shall indicate the duration of the day entry, expressed as the number of minutes since the time
         * indicated by the StartTime field.
         * If this field is omitted, the day entry shall end at the StartTime of the DayEntryStruct identified by the
         * next DayEntryID in the containing list. If the DayEntryStruct is the last item in the containing list, then
         * the day entry shall last until the end of the day.
         */
        public Integer duration; // uint16
        /**
         * This field shall indicate a randomization offset for this particular day entry in seconds. If this field is
         * not indicated, randomization shall use the value in the DefaultRandomizationOffset attribute.
         * 1. If the value of the RandomizationType field is Fixed, then the value of this field may be negative
         * 2. Otherwise if the value of the RandomizationType field is RandomNegative, then the value of this field
         * shall be less than or equal to zero
         * 3. Otherwise the value of this field shall be greater than or equal to zero
         */
        public Integer randomizationOffset; // int16
        /**
         * This field shall indicate a randomization type for this particular day entry. If this field is not indicated,
         * randomization shall use the value in the DefaultRandomizationType attribute.
         * If the calculated value for this field is of type None, no randomization shall be applied to the start of
         * this day entry.
         * If the calculated value for this field is of type Fixed, then the calculated value of the RandomizationOffset
         * field shall be added to the start time of the day entry.
         * If the calculated value for this field is of type Random, then a random number of whole seconds whose
         * absolute value is less than or equal to the calculated value of the RandomizationOffset field shall be added
         * to the start time of the day entry.
         * If the calculated value for this field is of type RandomPositive, then a random non-negative number of whole
         * seconds whose value is less than or equal to the calculated value of the RandomizationOffset field shall be
         * added to the start time of the day entry.
         * If the calculated value for this field is of type RandomNegative, then a random negative number of whole
         * seconds whose value is greater than or equal to the calculated value of the RandomizationOffset field shall
         * be added to the start time of the day entry.
         */
        public DayEntryRandomizationTypeEnum randomizationType; // DayEntryRandomizationTypeEnum

        public DayEntryStruct(Integer dayEntryId, Integer startTime, Integer duration, Integer randomizationOffset,
                DayEntryRandomizationTypeEnum randomizationType) {
            this.dayEntryId = dayEntryId;
            this.startTime = startTime;
            this.duration = duration;
            this.randomizationOffset = randomizationOffset;
            this.randomizationType = randomizationType;
        }
    }

    /**
     * This represents a series of day entries over the course of a day for a specific date.
     */
    public static class DayStruct {
        /**
         * This field shall indicate the date the associated set of DayEntryStructs applies to.
         */
        public Integer date; // epoch-s
        /**
         * This field shall indicate the type of day represented by the struct.
         */
        public DayTypeEnum dayType; // DayTypeEnum
        /**
         * This field shall indicate a list of DayEntryIDs for the DayEntryStructs to apply during the date specified by
         * Date, ordered by the value of the StartTime field.
         * This list shall NOT contain two DayEntryIDs for DayEntryStructs with the same value of the StartTime field.
         * If the Randomization feature is supported, every DayEntryStruct whose DayEntryID is included in this field
         * shall have its StartTime field set to a value less than the following DayEntryStruct's StartTime field minus
         * the calculated value of its RandomizationOffset field. In other words, it should not be possible for a random
         * offset to cause a day entry to begin before the preceding day entry.
         * If the DayType field is not Event:
         * 1. The DayEntryStruct referenced by the first DayEntryID in this list shall have its StartTime field set to
         * zero.
         * 2. Every DayEntryStruct referenced by this list shall have its Duration field omitted.
         * Otherwise, if the DayType field is Event:
         * 1. Every DayEntryStruct referenced by this list shall NOT have a Duration field whose value, added to the
         * value of StartTime field, is greater than the value of StartTime field on any subsequent DayEntryStruct
         * referenced by this list. In other words, day entries with a set duration can not overlap any other day
         * entries.
         */
        public List<Integer> dayEntryIDs; // list

        public DayStruct(Integer date, DayTypeEnum dayType, List<Integer> dayEntryIDs) {
            this.date = date;
            this.dayType = dayType;
            this.dayEntryIDs = dayEntryIDs;
        }
    }

    /**
     * This represents a series of day entries over the course of a day for a given set of days of the week.
     */
    public static class DayPatternStruct {
        /**
         * This field shall indicate an identifier for the day pattern. It shall be a unique identifier for the
         * combination of values of the DaysOfWeek and DayEntryIDs fields.
         * Once an identifier has been used for this combination, it shall NOT be used to represent any other
         * combination of these values.
         */
        public Integer dayPatternId; // uint32
        /**
         * This field shall indicate which days of the week the associated set of DayEntryStructs applies to. If no bits
         * are set, then this shall be a rotating day. See DayPatternIDs.
         */
        public DayPatternDayOfWeekBitmap daysOfWeek; // DayPatternDayOfWeekBitmap
        /**
         * This field shall indicate a list of DayEntryIDs for the DayEntryStructs to apply during the days specified by
         * DaysOfWeek, ordered by StartTime.
         * This list shall NOT contain two DayEntryIDs for the DayEntryStructs with the same value of the StartTime
         * field.
         */
        public List<Integer> dayEntryIDs; // list

        public DayPatternStruct(Integer dayPatternId, DayPatternDayOfWeekBitmap daysOfWeek, List<Integer> dayEntryIDs) {
            this.dayPatternId = dayPatternId;
            this.daysOfWeek = daysOfWeek;
            this.dayEntryIDs = dayEntryIDs;
        }
    }

    public static class PeakPeriodStruct {
        /**
         * This field shall indicate the severity of the peak period.
         */
        public PeakPeriodSeverityEnum severity; // PeakPeriodSeverityEnum
        /**
         * This field shall indicate the PeakPeriod number.
         */
        public Integer peakPeriod; // uint16

        public PeakPeriodStruct(PeakPeriodSeverityEnum severity, Integer peakPeriod) {
            this.severity = severity;
            this.peakPeriod = peakPeriod;
        }
    }

    /**
     * This represents particular Tariff Price information.
     */
    public static class TariffInformationStruct {
        /**
         * This field shall indicate a label for the tariff.
         */
        public String tariffLabel; // string
        /**
         * This field shall indicate the name of the commodity provider for this tariff.
         */
        public String providerName; // string
        /**
         * This field shall indicate the currency for the value of the Price field on all TariffPriceStruct.
         */
        public Currency currency; // currency
        /**
         * This field shall indicate the mode for metering blocks of usage.
         */
        public BlockModeEnum blockMode; // BlockModeEnum

        public TariffInformationStruct(String tariffLabel, String providerName, Currency currency,
                BlockModeEnum blockMode) {
            this.tariffLabel = tariffLabel;
            this.providerName = providerName;
            this.currency = currency;
            this.blockMode = blockMode;
        }
    }

    /**
     * This indicates a price or price level for a given tariff component, as well as what type of pricing it represents
     */
    public static class TariffPriceStruct {
        /**
         * This field shall indicate the type of price for the Price or PriceLevel fields.
         */
        public TariffPriceTypeEnum priceType; // TariffPriceTypeEnum
        /**
         * This field shall indicate the tariff price.
         */
        public BigInteger price; // money
        /**
         * This field shall indicate the tariff price level.
         */
        public Integer priceLevel; // int16

        public TariffPriceStruct(TariffPriceTypeEnum priceType, BigInteger price, Integer priceLevel) {
            this.priceType = priceType;
            this.price = price;
            this.priceLevel = priceLevel;
        }
    }

    /**
     * This represents components of a tariff.
     * Tariff components typically represent a price or price level, though they may also specify other aspects of a
     * tariff. For example, a tariff component may indicate changes in power thresholds, friendly credit status, or the
     * expected state of auxiliary switches.
     */
    public static class TariffComponentStruct {
        /**
         * This field shall indicate an identifier for the tariff component. If the tariff component is not a
         * prediction, this field shall be a unique identifier for the combination of values of the Price, Threshold,
         * FriendlyCredit, AuxiliaryLoad, and PeakPeriod fields with the value of the DayEntryIDs field on the
         * encompassing TariffPeriodStruct.
         * Once an identifier has been used for this combination, it shall NOT be used to represent any other
         * combination of these values.
         */
        public Integer tariffComponentId; // uint32
        /**
         * This field shall indicate the price when the tariff component is active.
         * When the Predicted field is set to TRUE, a null value shall indicate that the price and/or price level is not
         * yet available.
         * When the Predicted field is set to FALSE, a null value shall indicate that the server is unable to provide a
         * specific price or price level.
         */
        public TariffPriceStruct price; // TariffPriceStruct
        /**
         * This field shall indicate whether the calendar is entering a friendly credit period or not.
         * > [!NOTE]
         * > NOTE: When a meter enters into a Friendly Credit Period with a usable positive credit balance, the consumer
         * will be allowed to consume energy for the duration of the Friendly Credit Period, regardless of their credit
         * status while in that period. If, however, the consumer had already run out of credit and supply was
         * interrupted before entering into the Friendly Credit Period, they will not be allowed to reconnect without
         * first adding suitable additional credit.
         * > [!NOTE]
         * > NOTE: At the end of the Friendly Credit Period, the normal delivery rules connected with the accounting
         * functions of the meter will be resumed, and if the meter’s credit balance has dropped below the disablement
         * threshold during the Friendly Credit Period, then the meter will disconnect upon resuming normal delivery
         * rules
         */
        public Boolean friendlyCredit; // bool
        /**
         * This field shall indicate the required state of auxiliary load switches.
         */
        public AuxiliaryLoadSwitchSettingsStruct auxiliaryLoad; // AuxiliaryLoadSwitchSettingsStruct
        /**
         * This field shall indicate whether the tariff component represents a peak period.
         */
        public PeakPeriodStruct peakPeriod; // PeakPeriodStruct
        /**
         * This field shall indicate whether the tariff component represents a power threshold.
         */
        public PowerThresholdStruct powerThreshold; // PowerThresholdStruct
        /**
         * This field shall indicate the maximum level of consumption this tariff component applies to, in units
         * specified by the TariffUnit attribute. If the tariff component applies to any level of consumption, this
         * field shall be null.
         */
        public BigInteger threshold; // int64
        /**
         * A free-form label for the tariff component.
         */
        public String label; // string
        /**
         * This field shall indicate whether the tariff component represents a price prediction.
         */
        public Boolean predicted; // bool

        public TariffComponentStruct(Integer tariffComponentId, TariffPriceStruct price, Boolean friendlyCredit,
                AuxiliaryLoadSwitchSettingsStruct auxiliaryLoad, PeakPeriodStruct peakPeriod,
                PowerThresholdStruct powerThreshold, BigInteger threshold, String label, Boolean predicted) {
            this.tariffComponentId = tariffComponentId;
            this.price = price;
            this.friendlyCredit = friendlyCredit;
            this.auxiliaryLoad = auxiliaryLoad;
            this.peakPeriod = peakPeriod;
            this.powerThreshold = powerThreshold;
            this.threshold = threshold;
            this.label = label;
            this.predicted = predicted;
        }
    }

    /**
     * This represents the tariff components in effect for a set of calendar day entry IDs.
     */
    public static class TariffPeriodStruct {
        /**
         * A free-form label for the tariff period.
         */
        public String label; // string
        /**
         * This field shall indicate a list of DayEntryIDs for the DayEntryStructs during which the tariff components
         * are active.
         * Each DayEntryID shall be included in at most one DayEntryIDs field. In other words, there shall be only one
         * TariffPeriodStruct for each DayEntryID.
         */
        public List<Integer> dayEntryIDs; // list
        /**
         * This field shall indicate a list of TariffComponentIDs for the TariffComponentStructs active during the
         * specified day entries.
         */
        public List<Integer> tariffComponentIDs; // list

        public TariffPeriodStruct(String label, List<Integer> dayEntryIDs, List<Integer> tariffComponentIDs) {
            this.label = label;
            this.dayEntryIDs = dayEntryIDs;
            this.tariffComponentIDs = tariffComponentIDs;
        }
    }

    // Enums
    /**
     * This enumeration shall indicated the required state of an auxiliary switch.
     */
    public enum AuxiliaryLoadSettingEnum implements MatterEnum {
        OFF(0, "Off"),
        ON(1, "On"),
        NONE(2, "None");

        private final Integer value;
        private final String label;

        private AuxiliaryLoadSettingEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum DayTypeEnum implements MatterEnum {
        STANDARD(0, "Standard"),
        HOLIDAY(1, "Holiday"),
        DYNAMIC(2, "Dynamic"),
        EVENT(3, "Event");

        private final Integer value;
        private final String label;

        private DayTypeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum PeakPeriodSeverityEnum implements MatterEnum {
        UNUSED(0, "Unused"),
        LOW(1, "Low"),
        MEDIUM(2, "Medium"),
        HIGH(3, "High");

        private final Integer value;
        private final String label;

        private PeakPeriodSeverityEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum DayEntryRandomizationTypeEnum implements MatterEnum {
        NONE(0, "None"),
        FIXED(1, "Fixed"),
        RANDOM(2, "Random"),
        RANDOM_POSITIVE(3, "Random Positive"),
        RANDOM_NEGATIVE(4, "Random Negative");

        private final Integer value;
        private final String label;

        private DayEntryRandomizationTypeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum BlockModeEnum implements MatterEnum {
        NO_BLOCK(0, "No Block"),
        COMBINED(1, "Combined"),
        INDIVIDUAL(2, "Individual");

        private final Integer value;
        private final String label;

        private BlockModeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    // Bitmaps
    public static class DayPatternDayOfWeekBitmap {
        public boolean sunday;
        public boolean monday;
        public boolean tuesday;
        public boolean wednesday;
        public boolean thursday;
        public boolean friday;
        public boolean saturday;

        public DayPatternDayOfWeekBitmap(boolean sunday, boolean monday, boolean tuesday, boolean wednesday,
                boolean thursday, boolean friday, boolean saturday) {
            this.sunday = sunday;
            this.monday = monday;
            this.tuesday = tuesday;
            this.wednesday = wednesday;
            this.thursday = thursday;
            this.friday = friday;
            this.saturday = saturday;
        }
    }

    public static class FeatureMap {
        /**
         * 
         * Supports information about commodity pricing
         */
        public boolean pricing;
        /**
         * 
         * Supports information about when friendly credit periods begin and end
         */
        public boolean friendlyCredit;
        /**
         * 
         * Supports information about when auxiliary loads should be enabled or disabled
         */
        public boolean auxiliaryLoad;
        /**
         * 
         * Supports information about peak periods
         */
        public boolean peakPeriod;
        /**
         * 
         * Supports information about power threshold
         */
        public boolean powerThreshold;
        /**
         * 
         * Supports information about randomization of calendar day entries
         */
        public boolean randomization;

        public FeatureMap(boolean pricing, boolean friendlyCredit, boolean auxiliaryLoad, boolean peakPeriod,
                boolean powerThreshold, boolean randomization) {
            this.pricing = pricing;
            this.friendlyCredit = friendlyCredit;
            this.auxiliaryLoad = auxiliaryLoad;
            this.peakPeriod = peakPeriod;
            this.powerThreshold = powerThreshold;
            this.randomization = randomization;
        }
    }

    public CommodityTariffCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1792, "CommodityTariff");
    }

    protected CommodityTariffCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * The GetTariffComponent command allows a client to request information for a tariff component identifier that may
     * no longer be available in the TariffPeriods attributes.
     */
    public static ClusterCommand getTariffComponent(Integer tariffComponentId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (tariffComponentId != null) {
            map.put("tariffComponentId", tariffComponentId);
        }
        return new ClusterCommand("getTariffComponent", map);
    }

    /**
     * The GetDayEntry command allows a client to request information for a calendar day entry identifier that may no
     * longer be available in the CalendarPeriods or IndividualDays attributes.
     */
    public static ClusterCommand getDayEntry(Integer dayEntryId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (dayEntryId != null) {
            map.put("dayEntryId", dayEntryId);
        }
        return new ClusterCommand("getDayEntry", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "featureMap : " + featureMap + "\n";
        str += "tariffInfo : " + tariffInfo + "\n";
        str += "tariffUnit : " + tariffUnit + "\n";
        str += "startDate : " + startDate + "\n";
        str += "dayEntries : " + dayEntries + "\n";
        str += "dayPatterns : " + dayPatterns + "\n";
        str += "calendarPeriods : " + calendarPeriods + "\n";
        str += "individualDays : " + individualDays + "\n";
        str += "currentDay : " + currentDay + "\n";
        str += "nextDay : " + nextDay + "\n";
        str += "currentDayEntry : " + currentDayEntry + "\n";
        str += "currentDayEntryDate : " + currentDayEntryDate + "\n";
        str += "nextDayEntry : " + nextDayEntry + "\n";
        str += "nextDayEntryDate : " + nextDayEntryDate + "\n";
        str += "tariffComponents : " + tariffComponents + "\n";
        str += "tariffPeriods : " + tariffPeriods + "\n";
        str += "currentTariffComponents : " + currentTariffComponents + "\n";
        str += "nextTariffComponents : " + nextTariffComponents + "\n";
        str += "defaultRandomizationOffset : " + defaultRandomizationOffset + "\n";
        str += "defaultRandomizationType : " + defaultRandomizationType + "\n";
        return str;
    }
}
