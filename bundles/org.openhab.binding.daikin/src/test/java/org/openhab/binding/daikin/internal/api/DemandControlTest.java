/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.daikin.internal.api;

import static java.time.DayOfWeek.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.daikin.internal.api.DemandControl.ScheduleEntry;
import org.openhab.binding.daikin.internal.api.Enums.DemandControlMode;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * This class provides tests for the DemandControl class
 *
 * @author Jimmy Tanagra - Initial contribution
 *
 */

@NonNullByDefault
public class DemandControlTest {

    public static Stream<Arguments> parserTest() {
        return Stream.of( //
                Arguments.of("ret=OK,type=1,en_demand=0,mode=0,max_pow=100", DemandControlMode.OFF, 100),
                Arguments.of("ret=OK,type=1,en_demand=1,mode=0,max_pow=100", DemandControlMode.MANUAL, 100),
                Arguments.of("ret=OK,type=1,en_demand=0,mode=1,max_pow=100", DemandControlMode.OFF, 100),
                Arguments.of("ret=OK,type=1,en_demand=1,mode=1,max_pow=100", DemandControlMode.SCHEDULED, 100),
                Arguments.of("ret=OK,type=1,en_demand=0,mode=2,max_pow=100", DemandControlMode.OFF, 100),
                Arguments.of("ret=OK,type=1,en_demand=1,mode=2,max_pow=100", DemandControlMode.AUTO, 100),
                Arguments.of("ret=OK,type=1,en_demand=0,mode=0,max_pow=50", DemandControlMode.OFF, 50),
                Arguments.of("ret=OK,type=1,en_demand=0,mode=0,max_pow=40", DemandControlMode.OFF, 40),

                // Invalid inputs - defaults
                Arguments.of("ret=OK,type=1,en_demand=,mode=,max_pow=", DemandControlMode.OFF, 100)
        //
        );
    }

    @ParameterizedTest
    @MethodSource
    public void parserTest(String input, DemandControlMode expectedMode, int expectedMaxPower) {
        DemandControl info = DemandControl.parse(input);

        // assert
        assertEquals(expectedMode, info.mode);
        assertEquals(expectedMaxPower, info.maxPower);
    }

    public static Stream<Arguments> inputScheduleParserTest() {
        return Stream.of( //
                Arguments.of(
                        "ret=OK,type=1,en_demand=0,mode=0,max_pow=100,scdl_per_day=4,moc=0,tuc=0,wec=0,thc=0,frc=0,sac=0,suc=0",
                        Map.of("monday", List.of(), "tuesday", List.of(), "wednesday", List.of(), "thursday", List.of(),
                                "friday", List.of(), "saturday", List.of(), "sunday", List.of())),
                Arguments.of(
                        "ret=OK,type=1,en_demand=1,mode=1,max_pow=100,scdl_per_day=4,moc=3,mo1_en=1,mo1_t=720,mo1_p=90,mo2_en=1,mo2_t=840,mo2_p=0,mo3_en=1,mo3_t=600,mo3_p=70,tuc=0,wec=0,thc=0,frc=0,sac=0,suc=0",
                        Map.of("monday",
                                List.of(new ScheduleEntry(true, 720, 90), new ScheduleEntry(true, 840, 0),
                                        new ScheduleEntry(true, 600, 70)),
                                "tuesday", List.of(), "wednesday", List.of(), "thursday", List.of(), "friday",
                                List.of(), "saturday", List.of(), "sunday", List.of())),
                // added mo4_xxx but moc=3
                Arguments.of(
                        "ret=OK,type=1,en_demand=1,mode=1,max_pow=100,scdl_per_day=4,moc=3,mo1_en=1,mo1_t=720,mo1_p=90,mo2_en=1,mo2_t=840,mo2_p=0,mo3_en=1,mo3_t=600,mo3_p=70,mo4_en=0,mo4_t=30,mo4_p=0",
                        Map.of("monday",
                                List.of(new ScheduleEntry(true, 720, 90), new ScheduleEntry(true, 840, 0),
                                        new ScheduleEntry(true, 600, 70)),
                                "tuesday", List.of(), "wednesday", List.of(), "thursday", List.of(), "friday",
                                List.of(), "saturday", List.of(), "sunday", List.of())),
                // this time moc=4
                Arguments.of(
                        "ret=OK,type=1,en_demand=1,mode=1,max_pow=100,scdl_per_day=4,moc=4,mo1_en=1,mo1_t=720,mo1_p=90,mo2_en=1,mo2_t=840,mo2_p=0,mo3_en=1,mo3_t=600,mo3_p=70,mo4_en=0,mo4_t=30,mo4_p=0",
                        Map.of("monday",
                                List.of(new ScheduleEntry(true, 720, 90), new ScheduleEntry(true, 840, 0),
                                        new ScheduleEntry(true, 600, 70), new ScheduleEntry(false, 30, 0)),
                                "tuesday", List.of(), "wednesday", List.of(), "thursday", List.of(), "friday",
                                List.of(), "saturday", List.of(), "sunday", List.of())),
                Arguments.of(
                        "ret=OK,type=1,en_demand=1,mode=1,max_pow=100,scdl_per_day=4,tuc=4,tu1_en=1,tu1_t=720,tu1_p=90,tu2_en=1,tu2_t=840,tu2_p=0,tu3_en=1,tu3_t=600,tu3_p=70,tu4_en=0,tu4_t=30,tu4_p=0",
                        Map.of("tuesday",
                                List.of(new ScheduleEntry(true, 720, 90), new ScheduleEntry(true, 840, 0),
                                        new ScheduleEntry(true, 600, 70), new ScheduleEntry(false, 30, 0)),
                                "monday", List.of(), "wednesday", List.of(), "thursday", List.of(), "friday", List.of(),
                                "saturday", List.of(), "sunday", List.of())),

                Arguments.of(
                        "ret=OK,type=1,en_demand=1,mode=1,max_pow=100,scdl_per_day=4,wec=4,we1_en=1,we1_t=720,we1_p=90,we2_en=1,we2_t=840,we2_p=0,we3_en=1,we3_t=600,we3_p=70,we4_en=0,we4_t=30,we4_p=0",
                        Map.of("wednesday",
                                List.of(new ScheduleEntry(true, 720, 90), new ScheduleEntry(true, 840, 0),
                                        new ScheduleEntry(true, 600, 70), new ScheduleEntry(false, 30, 0)),
                                "monday", List.of(), "tuesday", List.of(), "thursday", List.of(), "friday", List.of(),
                                "saturday", List.of(), "sunday", List.of())),
                Arguments.of(
                        "ret=OK,type=1,en_demand=1,mode=1,max_pow=100,scdl_per_day=4,thc=4,th1_en=1,th1_t=720,th1_p=90,th2_en=1,th2_t=840,th2_p=0,th3_en=1,th3_t=600,th3_p=70,th4_en=0,th4_t=30,th4_p=0",
                        Map.of("thursday",
                                List.of(new ScheduleEntry(true, 720, 90), new ScheduleEntry(true, 840, 0),
                                        new ScheduleEntry(true, 600, 70), new ScheduleEntry(false, 30, 0)),
                                "monday", List.of(), "tuesday", List.of(), "wednesday", List.of(), "friday", List.of(),
                                "saturday", List.of(), "sunday", List.of())),
                Arguments.of(
                        "ret=OK,type=1,en_demand=1,mode=1,max_pow=100,scdl_per_day=4,frc=4,fr1_en=1,fr1_t=720,fr1_p=90,fr2_en=1,fr2_t=840,fr2_p=0,fr3_en=1,fr3_t=600,fr3_p=70,fr4_en=0,fr4_t=30,fr4_p=0",
                        Map.of("friday",
                                List.of(new ScheduleEntry(true, 720, 90), new ScheduleEntry(true, 840, 0),
                                        new ScheduleEntry(true, 600, 70), new ScheduleEntry(false, 30, 0)),
                                "monday", List.of(), "tuesday", List.of(), "wednesday", List.of(), "thursday",
                                List.of(), "saturday", List.of(), "sunday", List.of())),
                Arguments.of(
                        "ret=OK,type=1,en_demand=1,mode=1,max_pow=100,scdl_per_day=4,sac=4,sa1_en=1,sa1_t=720,sa1_p=90,sa2_en=1,sa2_t=840,sa2_p=0,sa3_en=1,sa3_t=600,sa3_p=70,sa4_en=0,sa4_t=30,sa4_p=0",
                        Map.of("saturday",
                                List.of(new ScheduleEntry(true, 720, 90), new ScheduleEntry(true, 840, 0),
                                        new ScheduleEntry(true, 600, 70), new ScheduleEntry(false, 30, 0)),
                                "monday", List.of(), "tuesday", List.of(), "wednesday", List.of(), "thursday",
                                List.of(), "friday", List.of(), "sunday", List.of())),
                Arguments.of(
                        "ret=OK,type=1,en_demand=1,mode=1,max_pow=100,scdl_per_day=4,suc=4,su1_en=1,su1_t=720,su1_p=90,su2_en=1,su2_t=840,su2_p=0,su3_en=1,su3_t=600,su3_p=70,su4_en=0,su4_t=30,su4_p=0",
                        Map.of("sunday",
                                List.of(new ScheduleEntry(true, 720, 90), new ScheduleEntry(true, 840, 0),
                                        new ScheduleEntry(true, 600, 70), new ScheduleEntry(false, 30, 0)),
                                "monday", List.of(), "tuesday", List.of(), "wednesday", List.of(), "thursday",
                                List.of(), "friday", List.of(), "saturday", List.of()))

        //
        );
    }

    @ParameterizedTest
    @MethodSource
    public void inputScheduleParserTest(String input, Map<String, List<ScheduleEntry>> expectedSchedule) {
        DemandControl info = DemandControl.parse(input);

        var parsedJsonObject = parseJson(info.getSchedule());

        String expectedJsonString = new Gson().toJson(expectedSchedule);
        var expectedJsonObject = parseJson(expectedJsonString);

        // assert
        assertEquals(expectedJsonObject, parsedJsonObject);
    }

    public static Stream<Arguments> jsonScheduleToParamStringTest() {
        return Stream.of( //
                Arguments.of( //
                        """
                                {
                                    "monday": [
                                        {"enabled":true,"time":720,"power":90},
                                        {"enabled":true,"time":840,"power":0},
                                        {"enabled":false,"time":600,"power":70},
                                        {"enabled":true,"time":300,"power":50}
                                    ],
                                    "tuesday":[],"wednesday":[],"thursday":[],"friday":[],"saturday":[],"sunday":[]
                                }
                                """, //
                        "moc=4," + //
                                "mo1_en=1,mo1_t=720,mo1_p=90," + //
                                "mo2_en=1,mo2_t=840,mo2_p=0," + //
                                "mo3_en=0,mo3_t=600,mo3_p=70," + //
                                "mo4_en=1,mo4_t=300,mo4_p=50," + //
                                "tuc=0,wec=0,thc=0,frc=0,sac=0,suc=0" //
                ), //
                Arguments.of( //
                        """
                                {
                                    "tuesday": [
                                        {"enabled":true,"time":720,"power":90},
                                        {"enabled":true,"time":840,"power":0},
                                        {"enabled":false,"time":600,"power":70},
                                        {"enabled":true,"time":300,"power":50}
                                    ],
                                    "monday":[],"wednesday":[],"thursday":[],"friday":[],"saturday":[],"sunday":[]
                                }
                                """, //
                        "tuc=4," + //
                                "tu1_en=1,tu1_t=720,tu1_p=90," + //
                                "tu2_en=1,tu2_t=840,tu2_p=0," + //
                                "tu3_en=0,tu3_t=600,tu3_p=70," + //
                                "tu4_en=1,tu4_t=300,tu4_p=50," + //
                                "moc=0,wec=0,thc=0,frc=0,sac=0,suc=0" //
                ), //
                Arguments.of( //
                        """
                                {
                                    "wednesday": [
                                        {"enabled":true,"time":720,"power":90},
                                        {"enabled":true,"time":840,"power":0},
                                        {"enabled":false,"time":600,"power":70},
                                        {"enabled":true,"time":300,"power":50}
                                    ],
                                    "monday":[],"tuesday":[],"thursday":[],"friday":[],"saturday":[],"sunday":[]
                                }
                                """, //
                        "wec=4," + //
                                "we1_en=1,we1_t=720,we1_p=90," + //
                                "we2_en=1,we2_t=840,we2_p=0," + //
                                "we3_en=0,we3_t=600,we3_p=70," + //
                                "we4_en=1,we4_t=300,we4_p=50," + //
                                "moc=0,tuc=0,thc=0,frc=0,sac=0,suc=0" //
                ), //
                Arguments.of( //
                        """
                                {
                                    "thursday": [
                                        {"enabled":true,"time":720,"power":90},
                                        {"enabled":true,"time":840,"power":0},
                                        {"enabled":false,"time":600,"power":70},
                                        {"enabled":true,"time":300,"power":50}
                                    ],
                                    "monday":[],"tuesday":[],"wednesday":[],"friday":[],"saturday":[],"sunday":[]
                                }
                                """, //
                        "thc=4," + //
                                "th1_en=1,th1_t=720,th1_p=90," + //
                                "th2_en=1,th2_t=840,th2_p=0," + //
                                "th3_en=0,th3_t=600,th3_p=70," + //
                                "th4_en=1,th4_t=300,th4_p=50," + //
                                "moc=0,tuc=0,wec=0,frc=0,sac=0,suc=0" //
                ), //
                Arguments.of( //
                        """
                                {
                                    "friday": [
                                        {"enabled":true,"time":720,"power":90},
                                        {"enabled":true,"time":840,"power":0},
                                        {"enabled":false,"time":600,"power":70},
                                        {"enabled":true,"time":300,"power":50}
                                    ],
                                    "monday":[],"tuesday":[],"wednesday":[],"thursday":[],"saturday":[],"sunday":[]
                                }
                                """, //
                        "frc=4," + //
                                "fr1_en=1,fr1_t=720,fr1_p=90," + //
                                "fr2_en=1,fr2_t=840,fr2_p=0," + //
                                "fr3_en=0,fr3_t=600,fr3_p=70," + //
                                "fr4_en=1,fr4_t=300,fr4_p=50," + //
                                "moc=0,tuc=0,thc=0,wec=0,sac=0,suc=0" //
                ), //
                Arguments.of( //
                        """
                                {
                                    "saturday": [
                                        {"enabled":true,"time":720,"power":90},
                                        {"enabled":true,"time":840,"power":0},
                                        {"enabled":false,"time":600,"power":70},
                                        {"enabled":true,"time":300,"power":50}
                                    ],
                                    "monday":[],"tuesday":[],"wednesday":[],"thursday":[],"friday":[],"sunday":[]
                                }
                                """, //
                        "sac=4," + //
                                "sa1_en=1,sa1_t=720,sa1_p=90," + //
                                "sa2_en=1,sa2_t=840,sa2_p=0," + //
                                "sa3_en=0,sa3_t=600,sa3_p=70," + //
                                "sa4_en=1,sa4_t=300,sa4_p=50," + //
                                "moc=0,tuc=0,thc=0,frc=0,wec=0,suc=0" //
                ), //
                Arguments.of( //
                        """
                                {
                                    "sunday": [
                                        {"enabled":true,"time":720,"power":90},
                                        {"enabled":true,"time":840,"power":0},
                                        {"enabled":false,"time":600,"power":70},
                                        {"enabled":true,"time":300,"power":50}
                                    ],
                                    "monday":[],"tuesday":[],"thursday":[],"friday":[],"saturday":[],"wednesday":[]
                                }
                                """, //
                        "suc=4," + //
                                "su1_en=1,su1_t=720,su1_p=90," + //
                                "su2_en=1,su2_t=840,su2_p=0," + //
                                "su3_en=0,su3_t=600,su3_p=70," + //
                                "su4_en=1,su4_t=300,su4_p=50," + //
                                "moc=0,tuc=0,thc=0,frc=0,sac=0,wec=0" //
                )//

        );
    }

    @ParameterizedTest
    @MethodSource
    public void jsonScheduleToParamStringTest(String scheduleJson, String expectedParamString) {
        DemandControl info = DemandControl.parse("ret=OK,type=1,en_demand=1,mode=1");
        Map<String, String> expectedParamMap = InfoParser.parse(expectedParamString);

        info.setSchedule(scheduleJson);

        Map<String, String> paramMap = info.getParamString();

        expectedParamMap.entrySet().stream().forEach(expectedParam -> assertThat(paramMap,
                hasEntry(is(expectedParam.getKey()), is(expectedParam.getValue()))));
    }

    private @Nullable Map<String, List<ScheduleEntry>> parseJson(String json) {
        return new Gson().fromJson(json, new TypeToken<Map<String, List<ScheduleEntry>>>() {
        }.getType());
    }

    public static Stream<Arguments> scheduledMaxPowerTest() {
        return Stream.of( //
                // empty schedule
                Arguments.of("moc=0,tuc=0,wec=0,thc=0,frc=0,sac=0,suc=0", MONDAY, "12:00", 100),
                // within the schedule of the day
                Arguments.of(
                        "moc=3,mo1_en=1,mo1_t=720,mo1_p=70,mo2_en=1,mo2_t=840,mo2_p=80,mo3_en=1,mo3_t=600,mo3_p=60,tuc=0,wec=0,thc=0,frc=0,sac=0,suc=0",
                        MONDAY, "10:00", 60),
                Arguments.of(
                        "moc=3,mo1_en=1,mo1_t=720,mo1_p=70,mo2_en=1,mo2_t=840,mo2_p=80,mo3_en=1,mo3_t=600,mo3_p=60,tuc=0,wec=0,thc=0,frc=0,sac=0,suc=0",
                        MONDAY, "10:05", 60),
                Arguments.of(
                        "moc=3,mo1_en=1,mo1_t=720,mo1_p=70,mo2_en=1,mo2_t=840,mo2_p=80,mo3_en=1,mo3_t=600,mo3_p=60,tuc=0,wec=0,thc=0,frc=0,sac=0,suc=0",
                        MONDAY, "12:00", 70),
                Arguments.of(
                        "moc=3,mo1_en=1,mo1_t=720,mo1_p=70,mo2_en=1,mo2_t=840,mo2_p=80,mo3_en=1,mo3_t=600,mo3_p=60,tuc=0,wec=0,thc=0,frc=0,sac=0,suc=0",
                        MONDAY, "15:00", 80),
                // it should ignore disabled schedules
                Arguments.of(
                        "moc=3,mo1_en=1,mo1_t=720,mo1_p=70,mo2_en=0,mo2_t=840,mo2_p=80,mo3_en=1,mo3_t=600,mo3_p=60,tuc=0,wec=0,thc=0,frc=0,sac=0,suc=0",
                        MONDAY, "15:00", 70),
                // earlier than first schedule of the day, must look back and find the last schedule
                Arguments.of(
                        "moc=3,mo1_en=1,mo1_t=720,mo1_p=70,mo2_en=1,mo2_t=840,mo2_p=80,mo3_en=1,mo3_t=600,mo3_p=60,tuc=0,wec=0,thc=0,frc=1,fr1_en=1,fr1_t=10,fr1_p=77,sac=0,suc=0",
                        MONDAY, "08:00", 77),
                // test for boundary conditions (last item on the list, ie. sunday)
                Arguments.of(
                        "moc=3,mo1_en=1,mo1_t=720,mo1_p=70,mo2_en=1,mo2_t=840,mo2_p=80,mo3_en=1,mo3_t=600,mo3_p=60,tuc=0,wec=0,thc=0,frc=0,sac=0,suc=1,su1_en=1,su1_t=10,su1_p=77",
                        MONDAY, "08:00", 77),
                // earlier than first schedule of the day, no other days have schedules,
                // so wrap around and pick the last schedule of the same day
                Arguments.of(
                        "moc=3,mo1_en=1,mo1_t=720,mo1_p=70,mo2_en=1,mo2_t=840,mo2_p=80,mo3_en=1,mo3_t=600,mo3_p=60,tuc=0,wec=0,thc=0,frc=0,sac=0,suc=0",
                        MONDAY, "08:00", 80),
                // but also ignore disabled schedules
                Arguments.of(
                        "moc=3,mo1_en=1,mo1_t=720,mo1_p=70,mo2_en=0,mo2_t=840,mo2_p=80,mo3_en=1,mo3_t=600,mo3_p=60,tuc=0,wec=0,thc=0,frc=0,sac=0,suc=0",
                        MONDAY, "08:00", 70),
                // empty schedule for the day, so look back until we find the last schedule
                Arguments.of(
                        "moc=3,mo1_en=1,mo1_t=720,mo1_p=70,mo2_en=1,mo2_t=840,mo2_p=80,mo3_en=1,mo3_t=600,mo3_p=60,tuc=0,wec=0,thc=0,frc=0,sac=0,suc=0",
                        WEDNESDAY, "15:00", 80),
                // it should also ignore disabled schedules in the previous days
                Arguments.of(
                        "moc=3,mo1_en=1,mo1_t=720,mo1_p=70,mo2_en=0,mo2_t=840,mo2_p=80,mo3_en=1,mo3_t=600,mo3_p=60,tuc=0,wec=0,thc=0,frc=0,sac=0,suc=0",
                        WEDNESDAY, "15:00", 70),
                // it should wrap around and start search from the end of the week
                Arguments.of(
                        "moc=0,tuc=3,tu1_en=1,tu1_t=720,tu1_p=70,tu2_en=1,tu2_t=840,tu2_p=80,tu3_en=1,tu3_t=600,tu3_p=60,wec=0,thc=0,frc=0,sac=0,suc=0",
                        MONDAY, "15:00", 80)
        //
        );
    }

    @ParameterizedTest
    @MethodSource
    public void scheduledMaxPowerTest(String input, DayOfWeek dow, String time, int expectedMaxPower) {
        DemandControl info = DemandControl.parse(input);

        LocalDateTime dateTime = LocalDateTime.now().with(java.time.temporal.TemporalAdjusters.next(dow))
                .with(java.time.LocalTime.parse(time));

        assertEquals(expectedMaxPower, info.getScheduledMaxPower(dateTime));
    }
}
