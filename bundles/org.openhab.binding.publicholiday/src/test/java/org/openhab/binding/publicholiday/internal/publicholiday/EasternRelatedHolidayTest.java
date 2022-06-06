package org.openhab.binding.publicholiday.internal.publicholiday;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.publicholiday.internal.publicholiday.definitions.EasternRelatedHolidayDef;

class EasternRelatedHolidayTest {

    private EasternRelatedHoliday easternRelatedInYear(int year, EasternRelatedHolidayDef def) {
        return new EasternRelatedHoliday(def) {
            @Override
            protected int getCurrentYear() {
                return year;
            }
        };
    }

    @ParameterizedTest
    @MethodSource
    void testEasterCalculation(LocalDate eastern) {
        EasternRelatedHoliday testUnit = easternRelatedInYear(eastern.getYear(),
                EasternRelatedHolidayDef.EASTER_SUNDAY);
        assertTrue(testUnit.isHoliday(eastern));
    }

    static Stream<Arguments> testEasterCalculation() {
        return List.of(Arguments.of(LocalDate.of(2022, 4, 17)), Arguments.of(LocalDate.of(2008, 3, 23)),
                Arguments.of(LocalDate.of(2031, 4, 13))).stream();
    }

    @ParameterizedTest
    @MethodSource
    void testHolidayCalculation(EasternRelatedHolidayDef holiday, int year, int month, int day) {
        LocalDate expHoliday = LocalDate.of(year, month, day);
        EasternRelatedHoliday testUnit = easternRelatedInYear(year, holiday);

        assertTrue(testUnit.isHoliday(expHoliday));
        assertFalse(testUnit.isDayBeforeHoliday(expHoliday));

        assertFalse(testUnit.isHoliday(expHoliday.minusDays(1)));
        assertTrue(testUnit.isDayBeforeHoliday(expHoliday.minusDays(1)));

        // Used test values ensure that eastern is not on the same day
        assertFalse(testUnit.isHoliday(expHoliday.minusYears(1)));
        assertFalse(testUnit.isDayBeforeHoliday(expHoliday.minusYears(1)));
    }

    static Stream<Arguments> testHolidayCalculation() {
        return List.of(Arguments.of(EasternRelatedHolidayDef.GOOD_FRIDAY, 2022, 4, 15),
                Arguments.of(EasternRelatedHolidayDef.EASTER_SUNDAY, 2021, 4, 4),
                Arguments.of(EasternRelatedHolidayDef.EASTER_MONDAY, 2008, 3, 24),
                Arguments.of(EasternRelatedHolidayDef.ASCENSION_DAY, 2040, 5, 10),
                Arguments.of(EasternRelatedHolidayDef.WHIT_SUNDAY, 2037, 5, 24),
                Arguments.of(EasternRelatedHolidayDef.WHIT_MONDAY, 2011, 6, 13),
                Arguments.of(EasternRelatedHolidayDef.CORPUS_CHRISTI, 2019, 6, 20)).stream();
    }
}
