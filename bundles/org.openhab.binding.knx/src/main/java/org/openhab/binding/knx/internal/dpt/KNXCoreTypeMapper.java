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
package org.openhab.binding.knx.internal.dpt;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.openhab.binding.knx.internal.KNXTypeMapper;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.KNXFormatException;
import tuwien.auto.calimero.KNXIllegalArgumentException;
import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.dptxlator.DPT;
import tuwien.auto.calimero.dptxlator.DPTXlator;
import tuwien.auto.calimero.dptxlator.DPTXlator1BitControlled;
import tuwien.auto.calimero.dptxlator.DPTXlator2ByteFloat;
import tuwien.auto.calimero.dptxlator.DPTXlator2ByteUnsigned;
import tuwien.auto.calimero.dptxlator.DPTXlator3BitControlled;
import tuwien.auto.calimero.dptxlator.DPTXlator4ByteFloat;
import tuwien.auto.calimero.dptxlator.DPTXlator4ByteSigned;
import tuwien.auto.calimero.dptxlator.DPTXlator4ByteUnsigned;
import tuwien.auto.calimero.dptxlator.DPTXlator64BitSigned;
import tuwien.auto.calimero.dptxlator.DPTXlator8BitSigned;
import tuwien.auto.calimero.dptxlator.DPTXlator8BitUnsigned;
import tuwien.auto.calimero.dptxlator.DPTXlatorBoolean;
import tuwien.auto.calimero.dptxlator.DPTXlatorDate;
import tuwien.auto.calimero.dptxlator.DPTXlatorDateTime;
import tuwien.auto.calimero.dptxlator.DPTXlatorRGB;
import tuwien.auto.calimero.dptxlator.DPTXlatorSceneControl;
import tuwien.auto.calimero.dptxlator.DPTXlatorSceneNumber;
import tuwien.auto.calimero.dptxlator.DPTXlatorString;
import tuwien.auto.calimero.dptxlator.DPTXlatorTime;
import tuwien.auto.calimero.dptxlator.DPTXlatorUtf8;
import tuwien.auto.calimero.dptxlator.TranslatorTypes;

/**
 * This class provides type mapping between all openHAB core types and KNX data point types.
 *
 * Each 'MainType' delivered from calimero, has a default mapping
 * for all it's children to a openHAB Typeclass.
 * All these 'MainType' mapping's are put into 'dptMainTypeMap'.
 *
 * Default 'MainType' mapping's we can override by a specific mapping.
 * All specific mapping's are put into 'dptTypeMap'.
 *
 * If for a 'MainType' there is currently no specific mapping registered,
 * you can find a commented example line, with it's correct 'DPTXlator' class.
 *
 * @author Kai Kreuzer
 * @author Volker Daube
 * @author Jan N. Klug
 * @author Helmut Lehmeyer - Java8, generic DPT Mapper
 */
@Component
public class KNXCoreTypeMapper implements KNXTypeMapper {

    private final Logger logger = LoggerFactory.getLogger(KNXCoreTypeMapper.class);

    private static final String TIME_DAY_FORMAT = new String("EEE, HH:mm:ss");
    private static final String DATE_FORMAT = new String("yyyy-MM-dd");

    /**
     * stores the openHAB type class for (supported) KNX datapoint types in a generic way.
     * dptTypeMap stores more specific type class and exceptions.
     */
    private final Map<Integer, Class<? extends Type>> dptMainTypeMap;

    /** stores the openHAB type class for all (supported) KNX datapoint types */
    private final Map<String, Class<? extends Type>> dptTypeMap;

    /** stores the default KNX DPT to use for each openHAB type */
    private final Map<Class<? extends Type>, String> defaultDptMap;

    public KNXCoreTypeMapper() {
        @SuppressWarnings("unused")
        final List<Class<?>> xlators = Arrays.<Class<?>> asList(DPTXlator1BitControlled.class,
                DPTXlator2ByteFloat.class, DPTXlator2ByteUnsigned.class, DPTXlator3BitControlled.class,
                DPTXlator4ByteFloat.class, DPTXlator4ByteSigned.class, DPTXlator4ByteUnsigned.class,
                DPTXlator64BitSigned.class, DPTXlator8BitSigned.class, DPTXlator8BitUnsigned.class,
                DPTXlatorBoolean.class, DPTXlatorDate.class, DPTXlatorDateTime.class, DPTXlatorRGB.class,
                DPTXlatorSceneControl.class, DPTXlatorSceneNumber.class, DPTXlatorString.class, DPTXlatorTime.class,
                DPTXlatorUtf8.class);

        dptTypeMap = new HashMap<>();
        dptMainTypeMap = new HashMap<>();

        /**
         * MainType: 1
         * 1.000: General bool
         * 1.001: DPT_Switch values: 0 = off 1 = on
         * 1.002: DPT_Bool values: 0 = false 1 = true
         * 1.003: DPT_Enable values: 0 = disable 1 = enable
         * 1.004: DPT_Ramp values: 0 = no ramp 1 = ramp
         * 1.005: DPT_Alarm values: 0 = no alarm 1 = alarm
         * 1.006: DPT_BinaryValue values: 0 = low 1 = high
         * 1.007: DPT_Step values: 0 = decrease 1 = increase
         * 1.008: DPT_UpDown values: 0 = up 1 = down
         * 1.009: DPT_OpenClose values: 0 = open 1 = close
         * 1.010: DPT_Start values: 0 = stop 1 = start
         * 1.011: DPT_State values: 0 = inactive 1 = active
         * 1.012: DPT_Invert values: 0 = not inverted 1 = inverted
         * 1.013: DPT_DimSendStyle values: 0 = start/stop 1 = cyclic
         * 1.014: DPT_InputSource values: 0 = fixed 1 = calculated
         * 1.015: DPT_Reset values: 0 = no action 1 = reset
         * 1.016: DPT_Ack values: 0 = no action 1 = acknowledge
         * 1.017: DPT_Trigger values: 0 = trigger 1 = trigger
         * 1.018: DPT_Occupancy values: 0 = not occupied 1 = occupied
         * 1.019: DPT_Window_Door values: 0 = closed 1 = open
         * 1.021: DPT_LogicalFunction values: 0 = OR 1 = AND
         * 1.022: DPT_Scene_AB values: 0 = scene A 1 = scene B
         * 1.023: DPT_ShutterBlinds_Mode values: 0 = only move up/down 1 = move up/down + step-stop
         * 1.100: DPT_Heat/Cool values: 0 = cooling 1 = heating
         */
        dptMainTypeMap.put(1, OnOffType.class);
        /** Exceptions Datapoint Types "B1", Main number 1 */
        dptTypeMap.put(DPTXlatorBoolean.DPT_UPDOWN.getID(), UpDownType.class);
        dptTypeMap.put(DPTXlatorBoolean.DPT_OPENCLOSE.getID(), OpenClosedType.class);
        dptTypeMap.put(DPTXlatorBoolean.DPT_START.getID(), StopMoveType.class);
        dptTypeMap.put(DPTXlatorBoolean.DPT_WINDOW_DOOR.getID(), OpenClosedType.class);
        dptTypeMap.put(DPTXlatorBoolean.DPT_SCENE_AB.getID(), DecimalType.class);

        /**
         * MainType: 2
         * 2.001: DPT_Switch_Control values: 0 = off 1 = on
         * 2.002: DPT_Bool_Control values: 0 = false 1 = true
         * 2.003: DPT_Enable_Control values: 0 = disable 1 = enable
         * 2.004: DPT_Ramp_Control values: 0 = no ramp 1 = ramp
         * 2.005: DPT_Alarm_Control values: 0 = no alarm 1 = alarm
         * 2.006: DPT_BinaryValue_Control values: 0 = low 1 = high
         * 2.007: DPT_Step_Control values: 0 = decrease 1 = increase
         * 2.008: DPT_Direction1_Control values: 0 = up 1 = down
         * 2.009: DPT_Direction2_Control values: 0 = open 1 = close
         * 2.010: DPT_Start_Control values: 0 = stop 1 = start
         * 2.011: DPT_State_Control values: 0 = inactive 1 = active
         * 2.012: DPT_Invert_Control values: 0 = not inverted 1 = inverted
         */
        dptMainTypeMap.put(2, DecimalType.class);
        /** Exceptions Datapoint Types "B2", Main number 2 */
        // Example: dptTypeMap.put(DPTXlator1BitControlled.DPT_SWITCH_CONTROL.getID(), DecimalType.class);

        /**
         * MainType: 3
         * 3.007: DPT_Control_Dimming values: 0 = decrease 1 = increase
         * 3.008: DPT_Control_Blinds values: 0 = up 1 = down
         */
        dptMainTypeMap.put(3, IncreaseDecreaseType.class);
        /** Exceptions Datapoint Types "B1U3", Main number 3 */
        dptTypeMap.put(DPTXlator3BitControlled.DPT_CONTROL_BLINDS.getID(), UpDownType.class);

        /**
         * MainType: 4
         * 4.001: DPT_Char_ASCII
         * 4.002: DPT_Char_8859_1
         */
        dptMainTypeMap.put(4, StringType.class);

        /**
         * MainType: 5
         * 5.000: General byte
         * 5.001: DPT_Scaling values: 0...100 %
         * 5.003: DPT_Angle values: 0...360 °
         * 5.004: DPT_Percent_U8 (8 Bit) values: 0...255 %
         * 5.005: DPT_DecimalFactor values: 0...255 ratio
         * 5.006: DPT_Tariff values: 0...254
         * 5.010: DPT_Value_1_Ucount Unsigned count values: 0...255 counter pulses
         */
        dptMainTypeMap.put(5, DecimalType.class);
        /** Exceptions Types "8-Bit Unsigned Value", Main number 5 */
        dptTypeMap.put(DPTXlator8BitUnsigned.DPT_SCALING.getID(), PercentType.class);
        dptTypeMap.put(DPTXlator8BitUnsigned.DPT_PERCENT_U8.getID(), PercentType.class);

        /**
         * MainType: 6
         * 6.001: DPT_Percent_V8 (8 Bit) values: -128...127 %
         * 6.010: DPT_Value_1_Count values: signed -128...127 counter pulses
         * 6.020: DPT_Status_Mode3 with mode values: 0/0/0/0/0 0...1/1/1/1/1 2
         */
        dptMainTypeMap.put(6, DecimalType.class);
        /** Exceptions Datapoint Types "8-Bit Signed Value", Main number 6 */
        dptTypeMap.put(DPTXlator8BitSigned.DPT_PERCENT_V8.getID(), PercentType.class);
        dptTypeMap.put(DPTXlator8BitSigned.DPT_STATUS_MODE3.getID(), StringType.class);

        /**
         * MainType: 7
         * 7.000: General unsigned integer
         * 7.001: DPT_Value_2_Ucount values: 0...65535 pulses
         * 7.002: DPT_TimePeriodMsec values: 0...65535 res 1 ms
         * 7.003: DPT_TimePeriod10MSec values: 0...655350 res 10 ms
         * 7.004: DPT_TimePeriod100MSec values: 0...6553500 res 100 ms
         * 7.005: DPT_TimePeriodSec values: 0...65535 s
         * 7.006: DPT_TimePeriodMin values: 0...65535 min
         * 7.007: DPT_TimePeriodHrs values: 0...65535 h
         * 7.010: DPT_PropDataType values: 0...65535
         * 7.011: DPT_Length_mm values: 0...65535 mm
         * 7.012: DPT_UElCurrentmA values: 0...65535 mA
         * 7.013: DPT_Brightness values: 0...65535 lx
         * Calimero does not map: (map/use to 7.000 until then)
         * 7.600: DPT_Colour_Temperature values: 0...65535 K, 2000K 3000K 5000K 8000K
         */
        dptMainTypeMap.put(7, DecimalType.class);
        /** Exceptions Datapoint Types "2-Octet Unsigned Value", Main number 7 */
        dptTypeMap.put(DPTXlator2ByteFloat.DPT_HUMIDITY.getID(), PercentType.class);

        /**
         * MainType: 8
         * 8.000: General integer
         * 8.001: DPT_Value_2_Count
         * 8.002: DPT_DeltaTimeMsec
         * 8.003: DPT_DeltaTime10MSec
         * 8.004: DPT_DeltaTime100MSec
         * 8.005: DPT_DeltaTimeSec
         * 8.006: DPT_DeltaTimeMin
         * 8.007: DPT_DeltaTimeHrs
         * 8.010: DPT_Percent_V16
         * 8.011: DPT_Rotation_Angle
         */
        dptMainTypeMap.put(8, DecimalType.class);

        /**
         * MainType: 9
         * 9.000: General float
         * 9.001: DPT_Value_Temp values: -273...+670760 °C
         * 9.002: DPT_Value_Tempd values: -670760...+670760 K
         * 9.003: DPT_Value_Tempa values: -670760...+670760 K/h
         * 9.004: DPT_Value_Lux values: 0...+670760 lx
         * 9.005: DPT_Value_Wsp values: 0...+670760 m/s
         * 9.006: DPT_Value_Pres values: 0...+670760 Pa
         * 9.007: DPT_Value_Humidity values: 0...+670760 %
         * 9.008: DPT_Value_AirQuality values: 0...+670760 ppm
         * 9.010: DPT_Value_Time1 values: -670760...+670760 s
         * 9.011: DPT_Value_Time2 values: -670760...+670760 ms
         * 9.020: DPT_Value_Volt values: -670760...+670760 mV
         * 9.021: DPT_Value_Curr values: -670760...+670760 mA
         * 9.022: DPT_PowerDensity values: -670760...+670760 W/m²
         * 9.023: DPT_KelvinPerPercent values: -670760...+670760 K/%
         * 9.024: DPT_Power values: -670760...+670760 kW
         * 9.025: DPT_Value_Volume_Flow values: -670760...+670760 l/h
         * 9.026: DPT_Rain_Amount values: -671088.64...670760.96 l/m²
         * 9.027: DPT_Value_Temp_F values: -459.6...670760.96 °F
         * 9.028: DPT_Value_Wsp_kmh values: 0...670760.96 km/h
         */
        dptMainTypeMap.put(9, DecimalType.class);
        /** Exceptions Datapoint Types "2-Octet Float Value", Main number 9 */
        dptTypeMap.put(DPTXlator2ByteFloat.DPT_HUMIDITY.getID(), PercentType.class);

        /**
         * MainType: 10
         * 10.001: DPT_TimeOfDay values: 1 = Monday...7 = Sunday, 0 = no-day, 00:00:00 Sun, 23:59:59 dow, hh:mm:ss
         */
        dptMainTypeMap.put(10, DateTimeType.class);
        /** Exceptions Datapoint Types "Time", Main number 10 */
        // Example: dptTypeMap.put(DPTXlatorTime.DPT_TIMEOFDAY.getID(), DateTimeType.class);

        /**
         * MainType: 11
         * 11.001: DPT_Date values: 1990-01-01...2089-12-31, yyyy-mm-dd
         */
        dptMainTypeMap.put(11, DateTimeType.class);
        /** Exceptions Datapoint Types “Date”", Main number 11 */
        // Example: dptTypeMap.put(DPTXlatorDate.DPT_DATE.getID(), DateTimeType.class);

        /**
         * MainType: 12
         * 12.000: General unsigned long
         * 12.001: DPT_Value_4_Ucount values: 0...4294967295 counter pulses
         */
        dptMainTypeMap.put(12, DecimalType.class);
        /** Exceptions Datapoint Types "4-Octet Unsigned Value", Main number 12 */
        // Example: dptTypeMap.put(DPTXlator4ByteUnsigned.DPT_VALUE_4_UCOUNT.getID(), DecimalType.class);

        /**
         * MainType: 13
         * 13.000: General long
         * 13.001: DPT_Value_4_Count values: -2147483648...2147483647 counter pulses
         * 13.002: DPT_FlowRate_m3h values: -2147483648...2147483647 m3/h
         * 13.010: DPT_ActiveEnergy values: -2147483648...2147483647 Wh
         * 13.011: DPT_ApparantEnergy values: -2147483648...2147483647 VAh
         * 13.012: DPT_ReactiveEnergy values: -2147483648...2147483647 VARh
         * 13.013: DPT_ActiveEnergy_kWh values: -2147483648...2147483647 kWh
         * 13.014: DPT_ApparantEnergy_kVAh values: -2147483648...2147483647 kVAh
         * 13.015: DPT_ReactiveEnergy_kVARh values: -2147483648...2147483647 kVAR
         * 13.100: DPT_LongDeltaTimeSec values: -2147483648...2147483647 s
         */
        dptMainTypeMap.put(13, DecimalType.class);
        /** Exceptions Datapoint Types "4-Octet Signed Value", Main number 13 */
        // Example: dptTypeMap.put(DPTXlator4ByteSigned.DPT_COUNT.getID(), DecimalType.class);

        /**
         * MainType: 14, Range: [-3.40282347e+38f...3.40282347e+38f]
         * 14.000: Acceleration, values: ms⁻²
         * 14.001: Acceleration, angular, values: rad s⁻²
         * 14.002: Activation energy, values: J/mol
         * 14.003: Activity, values: s⁻¹
         * 14.004: Mol, values: mol
         * 14.005: Amplitude, values:
         * 14.006: Angle, values: rad
         * 14.007: Angle, values: °
         * 14.008: Momentum, values: Js
         * 14.009: Angular velocity, values: rad/s
         * 14.010: Area, values: m²
         * 14.011: Capacitance, values: F
         * 14.012: Charge density (surface), values: C m⁻²
         * 14.013: Charge density (volume), values: C m⁻³
         * 14.014: Compressibility, values: m²/N
         * 14.015: Conductance, values: Ω⁻¹
         * 14.016: Conductivity, electrical, values: Ω⁻¹m⁻¹
         * 14.017: Density, values: kg m⁻³
         * 14.018: Electric charge, values: C
         * 14.019: Electric current, values: A
         * 14.020: Electric current density, values: A m⁻²
         * 14.021: Electric dipole moment, values: Cm
         * 14.022: Electric displacement, values: C m⁻²
         * 14.023: Electric field strength, values: V/m
         * 14.024: Electric flux, values: Vm
         * 14.025: Electric flux density, values: C m⁻²
         * 14.026: Electric polarization, values: C m⁻²
         * 14.027: Electric potential, values: V
         * 14.028: Electric potential difference, values: V
         * 14.029: Electromagnetic moment, values: A m²
         * 14.030: Electromotive force, values: V
         * 14.031: Energy, values: J
         * 14.032: Force, values: N
         * 14.033: Frequency, values: Hz
         * 14.034: Frequency, angular, values: rad/s
         * 14.035: Heat capacity, values: J/K
         * 14.036: Heat flow rate, values: W
         * 14.037: Heat quantity, values: J
         * 14.038: Impedance, values: Ω
         * 14.039: Length, values: m
         * 14.040: Quantity of Light, values: J
         * 14.041: Luminance, values: cd m⁻²
         * 14.042: Luminous flux, values: lm
         * 14.043: Luminous intensity, values: cd
         * 14.044: Magnetic field strength, values: A/m
         * 14.045: Magnetic flux, values: Wb
         * 14.046: Magnetic flux density, values: T
         * 14.047: Magnetic moment, values: A m²
         * 14.048: Magnetic polarization, values: T
         * 14.049: Magnetization, values: A/m
         * 14.050: Magneto motive force, values: A
         * 14.051: Mass, values: kg
         * 14.052: Mass flux, values: kg/s
         * 14.053: Momentum, values: N/s
         * 14.054: Phase angle, radiant, values: rad
         * 14.055: Phase angle, degree, values: °
         * 14.056: Power, values: W
         * 14.057: Power factor, values:
         * 14.058: Pressure, values: Pa
         * 14.059: Reactance, values: Ω
         * 14.060: Resistance, values: Ω
         * 14.061: Resistivity, values: Ωm
         * 14.062: Self inductance, values: H
         * 14.063: Solid angle, values: sr
         * 14.064: Sound intensity, values: W m⁻²
         * 14.065: Speed, values: m/s
         * 14.066: Stress, values: Pa
         * 14.067: Surface tension, values: N/m
         * 14.068: Temperature in Celsius Degree, values: °C
         * 14.069: Temperature, absolute, values: K
         * 14.070: Temperature difference, values: K
         * 14.071: Thermal capacity, values: J/K
         * 14.072: Thermal conductivity, values: W/m K⁻¹
         * 14.073: Thermoelectric power, values: V/K
         * 14.074: Time, values: s
         * 14.075: Torque, values: Nm
         * 14.076: Volume, values: m³
         * 14.077: Volume flux, values: m³/s
         * 14.078: Weight, values: N
         * 14.079: Work, values: J
         */
        dptMainTypeMap.put(14, DecimalType.class);
        /** Exceptions Datapoint Types "4-Octet Float Value", Main number 14 */
        // Example: dptTypeMap.put(DPTXlator4ByteFloat.DPT_ACCELERATION_ANGULAR.getID(), DecimalType.class);

        /**
         * MainType: 16
         * 16.000: ASCII string
         * 16.001: ISO-8859-1 string (Latin 1)
         */
        dptMainTypeMap.put(16, StringType.class);
        /** Exceptions Datapoint Types "String", Main number 16 */
        dptTypeMap.put(DPTXlatorString.DPT_STRING_8859_1.getID(), StringType.class);
        dptTypeMap.put(DPTXlatorString.DPT_STRING_ASCII.getID(), StringType.class);

        /**
         * MainType: 17
         * 17.001: Scene Number, values: 0...63
         */
        dptMainTypeMap.put(17, DecimalType.class);
        /** Exceptions Datapoint Types "Scene Number", Main number 17 */
        // Example: dptTypeMap.put(DPTXlatorSceneNumber.DPT_SCENE_NUMBER.getID(), DecimalType.class);

        /**
         * MainType: 18
         * 18.001: Scene Control, values: 0...63, 0 = activate, 1 = learn
         */
        dptMainTypeMap.put(18, DecimalType.class);
        /** Exceptions Datapoint Types "Scene Control", Main number 18 */
        // Example: dptTypeMap.put(DPTXlatorSceneControl.DPT_SCENE_CONTROL.getID(), DecimalType.class);

        /**
         * MainType: 19
         * 19.001: Date with time, values: 0 = 1900, 255 = 2155, 01/01 00:00:00, 12/31 24:00:00 yr/mth/day hr:min:sec
         */
        dptMainTypeMap.put(19, DateTimeType.class);
        /** Exceptions Datapoint Types "DateTime", Main number 19 */
        // Example: dptTypeMap.put(DPTXlatorDateTime.DPT_DATE_TIME.getID(), DateTimeType.class);

        /**
         * MainType: 20
         * 20.001: System Clock Mode, enumeration [0..2]
         * 20.002: Building Mode, enumeration [0..2]
         * 20.003: Occupancy Mode, enumeration [0..2]
         * 20.004: Priority, enumeration [0..3]
         * 20.005: Light Application Mode, enumeration [0..2]
         * 20.006: Application Area, enumeration [0..14]
         * 20.007: Alarm Class Type, enumeration [0..3]
         * 20.008: PSU Mode, enumeration [0..2]
         * 20.011: Error Class System, enumeration [0..18]
         * 20.012: Error Class HVAC, enumeration [0..4]
         * 20.013: Time Delay, enumeration [0..25]
         * 20.014: Beaufort Wind Force Scale, enumeration [0..12]
         * 20.017: Sensor Select, enumeration [0..4]
         * 20.020: Actuator Connect Type, enumeration [1..2]
         * 20.100: Fuel Type, enumeration [0..3]
         * 20.101: Burner Type, enumeration [0..3]
         * 20.102: HVAC Mode, enumeration [0..4]
         * 20.103: DHW Mode, enumeration [0..4]
         * 20.104: Load Priority, enumeration [0..2]
         * 20.105: HVAC Control Mode, enumeration [0..20]
         * 20.106: HVAC Emergency Mode, enumeration [0..5]
         * 20.107: Changeover Mode, enumeration [0..2]
         * 20.108: Valve Mode, enumeration [1..5]
         * 20.109: Damper Mode, enumeration [1..4]
         * 20.110: Heater Mode, enumeration [1..3]
         * 20.111: Fan Mode, enumeration [0..2]
         * 20.112: Master/Slave Mode, enumeration [0..2]
         * 20.113: Status Room Setpoint, enumeration [0..2]
         * 20.114: Metering Device Type, enumeration [0..41/255]
         * 20.120: Air Damper Actuator Type, enumeration [1..2]
         * 20.121: Backup Mode, enumeration [0..1]
         * 20.122: Start Synchronization, enumeration [0..2]
         * 20.600: Behavior Lock/Unlock, enumeration [0..6]
         * 20.601: Behavior Bus Power Up/Down, enumeration [0..4]
         * 20.602: DALI Fade Time, enumeration [0..15]
         * 20.603: Blinking Mode, enumeration [0..2]
         * 20.604: Light Control Mode, enumeration [0..1]
         * 20.605: Switch PB Model, enumeration [1..2]
         * 20.606: PB Action, enumeration [0..3]
         * 20.607: Dimm PB Model, enumeration [1..4]
         * 20.608: Switch On Mode, enumeration [0..2]
         * 20.609: Load Type Set, enumeration [0..2]
         * 20.610: Load Type Detected, enumeration [0..3]
         * 20.801: SAB Except Behavior, enumeration [0..4]
         * 20.802: SAB Behavior Lock/Unlock, enumeration [0..6]
         * 20.803: SSSB Mode, enumeration [1..4]
         * 20.804: Blinds Control Mode, enumeration [0..1]
         * 20.1000: Comm Mode, enumeration [0..255]
         * 20.1001: Additional Info Type, enumeration [0..7]
         * 20.1002: RF Mode Select, enumeration [0..2]
         * 20.1003: RF Filter Select, enumeration [0..3]
         * 20.1200: M-Bus Breaker/Valve State, enumeration [0..255]
         * 20.1202: Gas Measurement Condition, enumeration [0..3]
         *
         */
        dptMainTypeMap.put(20, StringType.class);
        /** Exceptions Datapoint Types, Main number 20 */
        // Example since calimero 2.4: dptTypeMap.put(DPTXlator8BitEnum.DptSystemClockMode.getID(), StringType.class);

        /**
         * MainType: 21
         * 21.001: General Status, values: 0...31
         * 21.002: Device Control, values: 0...7
         * 21.100: Forcing Signal, values: 0...255
         * 21.101: Forcing Signal Cool, values: 0...1
         * 21.102: Room Heating Controller Status, values: 0...255
         * 21.103: Solar Dhw Controller Status, values: 0...7
         * 21.104: Fuel Type Set, values: 0...7
         * 21.105: Room Cooling Controller Status, values: 0...1
         * 21.106: Ventilation Controller Status, values: 0...15
         * 21.601: Light Actuator Error Info, values: 0...127
         * 21.1000: R F Comm Mode Info, values: 0...7
         * 21.1001: R F Filter Modes, values: 0...7
         * 21.1010: Channel Activation State, values: 0...255
         */
        dptMainTypeMap.put(21, StringType.class);
        /** Exceptions Datapoint Types, Main number 21 */
        // Example since calimero 2.4: dptTypeMap.put(DptXlator8BitSet.DptGeneralStatus.getID(), StringType.class);

        /**
         * MainType: 28
         * 28.001: UTF-8
         */
        dptMainTypeMap.put(28, StringType.class);
        /** Exceptions Datapoint Types "String" UTF-8, Main number 28 */
        // Example: dptTypeMap.put(DPTXlatorUtf8.DPT_UTF8.getID(), StringType.class);

        /**
         * MainType: 29
         * 29.010: Active Energy, values: -9223372036854775808...9223372036854775807 Wh
         * 29.011: Apparent energy, values: -9223372036854775808...9223372036854775807 VAh
         * 29.012: Reactive energy, values: -9223372036854775808...9223372036854775807 VARh
         */
        dptMainTypeMap.put(29, DecimalType.class);
        /** Exceptions Datapoint Types "64-Bit Signed Value", Main number 29 */
        // Example: dptTypeMap.put(DPTXlator64BitSigned.DPT_ACTIVE_ENERGY.getID(), DecimalType.class);

        /**
         * MainType: 229
         * 229.001: Metering Value, values: -2147483648...2147483647
         */
        dptMainTypeMap.put(229, DecimalType.class);
        /** Exceptions Datapoint Types "4-Octet Signed Value", Main number 229 */
        // Example: dptTypeMap.put(DptXlatorMeteringValue.DptMeteringValue.getID(), DecimalType.class);

        /**
         * MainType: 232, 3 bytes
         * 232.600: DPT_Colour_RGB, values: 0 0 0...255 255 255, r g b
         */
        dptMainTypeMap.put(232, HSBType.class);
        /** Exceptions Datapoint Types "RGB Color", Main number 232 */
        // Example: dptTypeMap.put(DPTXlatorRGB.DPT_RGB.getID(), HSBType.class);

        defaultDptMap = new HashMap<>();
        defaultDptMap.put(OnOffType.class, DPTXlatorBoolean.DPT_SWITCH.getID());
        defaultDptMap.put(UpDownType.class, DPTXlatorBoolean.DPT_UPDOWN.getID());
        defaultDptMap.put(StopMoveType.class, DPTXlatorBoolean.DPT_START.getID());
        defaultDptMap.put(OpenClosedType.class, DPTXlatorBoolean.DPT_WINDOW_DOOR.getID());
        defaultDptMap.put(IncreaseDecreaseType.class, DPTXlator3BitControlled.DPT_CONTROL_DIMMING.getID());
        defaultDptMap.put(PercentType.class, DPTXlator8BitUnsigned.DPT_SCALING.getID());
        defaultDptMap.put(DecimalType.class, DPTXlator2ByteFloat.DPT_TEMPERATURE.getID());
        defaultDptMap.put(DateTimeType.class, DPTXlatorTime.DPT_TIMEOFDAY.getID());
        defaultDptMap.put(StringType.class, DPTXlatorString.DPT_STRING_8859_1.getID());
        defaultDptMap.put(HSBType.class, DPTXlatorRGB.DPT_RGB.getID());
    }

    @Override
    public String toDPTValue(Type type, String dptID) {
        DPT dpt;
        int mainNumber = getMainNumber(dptID);
        if (mainNumber == -1) {
            logger.error("toDPTValue couldn't identify mainnumber in dptID: {}", dptID);
            return null;
        }
        int subNumber = getSubNumber(dptID);
        if (subNumber == -1) {
            logger.debug("toType: couldn't identify sub number in dptID: {}.", dptID);
            return null;
        }

        try {
            DPTXlator translator = TranslatorTypes.createTranslator(mainNumber, dptID);
            dpt = translator.getType();
        } catch (KNXException e) {
            return null;
        }

        try {
            // check for HSBType first, because it extends PercentType as well
            if (type instanceof HSBType) {
                switch (mainNumber) {
                    case 5:
                        switch (subNumber) {
                            case 3: // * 5.003: Angle, values: 0...360 °
                                return ((HSBType) type).getHue().toString();
                            case 1: // * 5.001: Scaling, values: 0...100 %
                            default:
                                return ((HSBType) type).getBrightness().toString();
                        }
                    case 232:
                        switch (subNumber) {
                            case 600: // 232.600
                                HSBType hc = ((HSBType) type);
                                return "r:" + convertPercentToByte(hc.getRed()) + " g:"
                                        + convertPercentToByte(hc.getGreen()) + " b:"
                                        + convertPercentToByte(hc.getBlue());
                        }
                    default:
                        HSBType hc = ((HSBType) type);
                        return "r:" + hc.getRed().intValue() + " g:" + hc.getGreen().intValue() + " b:"
                                + hc.getBlue().intValue();
                }
            } else if (type instanceof OnOffType) {
                return type.equals(OnOffType.OFF) ? dpt.getLowerValue() : dpt.getUpperValue();
            } else if (type instanceof UpDownType) {
                return type.equals(UpDownType.UP) ? dpt.getLowerValue() : dpt.getUpperValue();
            } else if (type instanceof IncreaseDecreaseType) {
                DPT valueDPT = ((DPTXlator3BitControlled.DPT3BitControlled) dpt).getControlDPT();
                return type.equals(IncreaseDecreaseType.DECREASE) ? valueDPT.getLowerValue() + " 5"
                        : valueDPT.getUpperValue() + " 5";
            } else if (type instanceof OpenClosedType) {
                return type.equals(OpenClosedType.CLOSED) ? dpt.getLowerValue() : dpt.getUpperValue();
            } else if (type instanceof StopMoveType) {
                return type.equals(StopMoveType.STOP) ? dpt.getLowerValue() : dpt.getUpperValue();
            } else if (type instanceof PercentType) {
                return String.valueOf(((DecimalType) type).intValue());
            } else if (type instanceof DecimalType) {
                switch (mainNumber) {
                    case 2:
                        DPT valueDPT = ((DPTXlator1BitControlled.DPT1BitControlled) dpt).getValueDPT();
                        switch (((DecimalType) type).intValue()) {
                            case 0:
                                return "0 " + valueDPT.getLowerValue();
                            case 1:
                                return "0 " + valueDPT.getUpperValue();
                            case 2:
                                return "1 " + valueDPT.getLowerValue();
                            default:
                                return "1 " + valueDPT.getUpperValue();
                        }
                    case 18:
                        int intVal = ((DecimalType) type).intValue();
                        if (intVal > 63) {
                            return "learn " + (intVal - 0x80);
                        } else {
                            return "activate " + intVal;
                        }
                    default:
                        return ((DecimalType) type).toBigDecimal().stripTrailingZeros().toPlainString();
                }
            } else if (type instanceof StringType) {
                return type.toString();
            } else if (type instanceof DateTimeType) {
                return formatDateTime((DateTimeType) type, dptID);
            }
        } catch (Exception e) {
            logger.warn("An exception occurred converting type {} to dpt id {}: error message={}", type, dptID,
                    e.getMessage());
            return null;
        }

        logger.debug("toDPTValue: Couldn't convert type {} to dpt id {} (no mapping).", type, dptID);

        return null;
    }

    @Override
    public Type toType(Datapoint datapoint, byte[] data) {
        try {
            DPTXlator translator = TranslatorTypes.createTranslator(datapoint.getMainNumber(), datapoint.getDPT());
            translator.setData(data);
            String value = translator.getValue();

            String id = translator.getType().getID();
            logger.trace("toType datapoint DPT = {}", datapoint.getDPT());

            int mainNumber = getMainNumber(id);
            if (mainNumber == -1) {
                logger.debug("toType: couldn't identify mainnumber in dptID: {}.", id);
                return null;
            }
            int subNumber = getSubNumber(id);
            if (subNumber == -1) {
                logger.debug("toType: couldn't identify sub number in dptID: {}.", id);
                return null;
            }
            /*
             * Following code section deals with specific mapping of values from KNX to openHAB types were the String
             * received from the DPTXlator is not sufficient to set the openHAB type or has bugs
             */
            switch (mainNumber) {
                case 1:
                    DPTXlatorBoolean translatorBoolean = (DPTXlatorBoolean) translator;
                    switch (subNumber) {
                        case 8:
                            return translatorBoolean.getValueBoolean() ? UpDownType.DOWN : UpDownType.UP;
                        case 9:
                            return translatorBoolean.getValueBoolean() ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                        case 10:
                            return translatorBoolean.getValueBoolean() ? StopMoveType.MOVE : StopMoveType.STOP;
                        case 19:
                            return translatorBoolean.getValueBoolean() ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                        case 22:
                            return DecimalType.valueOf(translatorBoolean.getValueBoolean() ? "1" : "0");
                        default:
                            return translatorBoolean.getValueBoolean() ? OnOffType.ON : OnOffType.OFF;
                    }
                case 2:
                    DPTXlator1BitControlled translator1BitControlled = (DPTXlator1BitControlled) translator;
                    int decValue = (translator1BitControlled.getControlBit() ? 2 : 0)
                            + (translator1BitControlled.getValueBit() ? 1 : 0);
                    return new DecimalType(decValue);
                case 3:
                    DPTXlator3BitControlled translator3BitControlled = (DPTXlator3BitControlled) translator;
                    if (translator3BitControlled.getStepCode() == 0) {
                        logger.debug("toType: KNX DPT_Control_Dimming: break received.");
                        return UnDefType.UNDEF;
                    }
                    switch (subNumber) {
                        case 7:
                            return translator3BitControlled.getControlBit() ? IncreaseDecreaseType.INCREASE
                                    : IncreaseDecreaseType.DECREASE;
                        case 8:
                            return translator3BitControlled.getControlBit() ? UpDownType.DOWN : UpDownType.UP;
                    }
                case 14:
                    /*
                     * FIXME: Workaround for a bug in Calimero / Openhab DPTXlator4ByteFloat.makeString(): is using a
                     * locale when
                     * translating a Float to String. It could happen the a ',' is used as separator, such as
                     * 3,14159E20.
                     * Openhab's DecimalType expects this to be in US format and expects '.': 3.14159E20.
                     * There is no issue with DPTXlator2ByteFloat since calimero is using a non-localized translation
                     * there.
                     */
                    DPTXlator4ByteFloat translator4ByteFloat = (DPTXlator4ByteFloat) translator;
                    Float f = translator4ByteFloat.getValueFloat();
                    if (Math.abs(f) < 100000) {
                        value = String.valueOf(f);
                    } else {
                        NumberFormat dcf = NumberFormat.getInstance(Locale.US);
                        if (dcf instanceof DecimalFormat) {
                            ((DecimalFormat) dcf).applyPattern("0.#####E0");
                        }
                        value = dcf.format(f);
                    }
                    break;
                case 18:
                    DPTXlatorSceneControl translatorSceneControl = (DPTXlatorSceneControl) translator;
                    int decimalValue = translatorSceneControl.getSceneNumber();
                    if (value.startsWith("learn")) {
                        decimalValue += 0x80;
                    }
                    value = String.valueOf(decimalValue);

                    break;
                case 19:
                    DPTXlatorDateTime translatorDateTime = (DPTXlatorDateTime) translator;
                    if (translatorDateTime.isFaultyClock()) {
                        // Not supported: faulty clock
                        logger.debug("toType: KNX clock msg ignored: clock faulty bit set, which is not supported");
                        return null;
                    } else if (!translatorDateTime.isValidField(DPTXlatorDateTime.YEAR)
                            && translatorDateTime.isValidField(DPTXlatorDateTime.DATE)) {
                        // Not supported: "/1/1" (month and day without year)
                        logger.debug(
                                "toType: KNX clock msg ignored: no year, but day and month, which is not supported");
                        return null;
                    } else if (translatorDateTime.isValidField(DPTXlatorDateTime.YEAR)
                            && !translatorDateTime.isValidField(DPTXlatorDateTime.DATE)) {
                        // Not supported: "1900" (year without month and day)
                        logger.debug(
                                "toType: KNX clock msg ignored: no day and month, but year, which is not supported");
                        return null;
                    } else if (!translatorDateTime.isValidField(DPTXlatorDateTime.YEAR)
                            && !translatorDateTime.isValidField(DPTXlatorDateTime.DATE)
                            && !translatorDateTime.isValidField(DPTXlatorDateTime.TIME)) {
                        // Not supported: No year, no date and no time
                        logger.debug("toType: KNX clock msg ignored: no day and month or year, which is not supported");
                        return null;
                    }

                    Calendar cal = Calendar.getInstance();
                    if (translatorDateTime.isValidField(DPTXlatorDateTime.YEAR)
                            && !translatorDateTime.isValidField(DPTXlatorDateTime.TIME)) {
                        // Pure date format, no time information
                        cal.setTimeInMillis(translatorDateTime.getValueMilliseconds());
                        value = new SimpleDateFormat(DateTimeType.DATE_PATTERN).format(cal.getTime());
                        return DateTimeType.valueOf(value);
                    } else if (!translatorDateTime.isValidField(DPTXlatorDateTime.YEAR)
                            && translatorDateTime.isValidField(DPTXlatorDateTime.TIME)) {
                        // Pure time format, no date information
                        cal.clear();
                        cal.set(Calendar.HOUR_OF_DAY, translatorDateTime.getHour());
                        cal.set(Calendar.MINUTE, translatorDateTime.getMinute());
                        cal.set(Calendar.SECOND, translatorDateTime.getSecond());
                        value = new SimpleDateFormat(DateTimeType.DATE_PATTERN).format(cal.getTime());
                        return DateTimeType.valueOf(value);
                    } else if (translatorDateTime.isValidField(DPTXlatorDateTime.YEAR)
                            && translatorDateTime.isValidField(DPTXlatorDateTime.TIME)) {
                        // Date format and time information
                        cal.setTimeInMillis(translatorDateTime.getValueMilliseconds());
                        value = new SimpleDateFormat(DateTimeType.DATE_PATTERN).format(cal.getTime());
                        return DateTimeType.valueOf(value);
                    }
                    break;
            }

            Class<? extends Type> typeClass = toTypeClass(id);
            if (typeClass == null) {
                return null;
            }

            if (typeClass.equals(PercentType.class)) {
                return new PercentType(BigDecimal.valueOf(Math.round(translator.getNumericValue())));
            }
            if (typeClass.equals(DecimalType.class)) {
                return new DecimalType(translator.getNumericValue());
            }
            if (typeClass.equals(StringType.class)) {
                return StringType.valueOf(value);
            }

            if (typeClass.equals(DateTimeType.class)) {
                String date = formatDateTime(value, datapoint.getDPT());
                if ((date == null) || (date.isEmpty())) {
                    logger.debug("toType: KNX clock msg ignored: date object null or empty {}.", date);
                    return null;
                } else {
                    return DateTimeType.valueOf(date);
                }
            }

            if (typeClass.equals(HSBType.class)) {
                // value has format of "r:<red value> g:<green value> b:<blue value>"
                int r = Integer.parseInt(value.split(" ")[0].split(":")[1]);
                int g = Integer.parseInt(value.split(" ")[1].split(":")[1]);
                int b = Integer.parseInt(value.split(" ")[2].split(":")[1]);

                return HSBType.fromRGB(r, g, b);
            }

        } catch (KNXFormatException kfe) {
            logger.info("Translator couldn't parse data for datapoint type '{}' (KNXFormatException).",
                    datapoint.getDPT());
        } catch (KNXIllegalArgumentException kiae) {
            logger.info("Translator couldn't parse data for datapoint type '{}' (KNXIllegalArgumentException).",
                    datapoint.getDPT());
        } catch (KNXException e) {
            logger.warn("Failed creating a translator for datapoint type '{}'.", datapoint.getDPT(), e);
        }

        return null;
    }

    /**
     * Converts a datapoint type id into an openHAB type class
     *
     * @param dptId the datapoint type id
     * @return the openHAB type (command or state) class or {@code null} if the datapoint type id is not supported.
     */
    @Override
    public Class<? extends Type> toTypeClass(String dptId) {
        Class<? extends Type> ohClass = dptTypeMap.get(dptId);
        if (ohClass == null) {
            int mainNumber = getMainNumber(dptId);
            if (mainNumber == -1) {
                logger.debug("Couldn't convert KNX datapoint type id into openHAB type class for dptId: {}.", dptId);
                return null;
            }
            ohClass = dptMainTypeMap.get(mainNumber);
        }
        return ohClass;
    }

    /**
     * Converts an openHAB type class into a datapoint type id.
     *
     * @param typeClass the openHAB type class
     * @return the datapoint type id
     */
    public String toDPTid(Class<? extends Type> typeClass) {
        return defaultDptMap.get(typeClass);
    }

    /**
     * Formats the given <code>value</code> according to the datapoint type
     * <code>dpt</code> to a String which can be processed by {@link DateTimeType}.
     *
     * @param value
     * @param dpt
     *
     * @return a formatted String like </code>yyyy-MM-dd'T'HH:mm:ss</code> which
     *         is target format of the {@link DateTimeType}
     */
    private String formatDateTime(String value, String dpt) {
        Date date = null;

        try {
            if (DPTXlatorDate.DPT_DATE.getID().equals(dpt)) {
                date = new SimpleDateFormat(DATE_FORMAT).parse(value);
            } else if (DPTXlatorTime.DPT_TIMEOFDAY.getID().equals(dpt)) {
                if (value.contains("no-day")) {
                    /*
                     * KNX "no-day" needs special treatment since openHAB's DateTimeType doesn't support "no-day".
                     * Workaround: remove the "no-day" String, parse the remaining time string, which will result in a
                     * date of "1970-01-01".
                     * Replace "no-day" with the current day name
                     */
                    StringBuffer stb = new StringBuffer(value);
                    int start = stb.indexOf("no-day");
                    int end = start + "no-day".length();
                    stb.replace(start, end, String.format(Locale.US, "%1$ta", Calendar.getInstance()));
                    value = stb.toString();
                }
                date = new SimpleDateFormat(TIME_DAY_FORMAT, Locale.US).parse(value);
            }
        } catch (ParseException pe) {
            // do nothing but logging
            logger.warn("Could not parse '{}' to a valid date", value);
        }

        return date != null ? new SimpleDateFormat(DateTimeType.DATE_PATTERN).format(date) : "";
    }

    /**
     * Formats the given internal <code>dateType</code> to a knx readable String
     * according to the target datapoint type <code>dpt</code>.
     *
     * @param dateType
     * @param dpt the target datapoint type
     *
     * @return a String which contains either an ISO8601 formatted date (yyyy-mm-dd),
     *         a formatted 24-hour clock with the day of week prepended (Mon, 12:00:00) or
     *         a formatted 24-hour clock (12:00:00)
     *
     * @throws IllegalArgumentException if none of the datapoint types DPT_DATE or
     *             DPT_TIMEOFDAY has been used.
     */
    private static String formatDateTime(DateTimeType dateType, String dpt) {
        if (DPTXlatorDate.DPT_DATE.getID().equals(dpt)) {
            return dateType.format("%tF");
        } else if (DPTXlatorTime.DPT_TIMEOFDAY.getID().equals(dpt)) {
            return dateType.format(Locale.US, "%1$ta, %1$tT");
        } else if (DPTXlatorDateTime.DPT_DATE_TIME.getID().equals(dpt)) {
            return dateType.format(Locale.US, "%tF %1$tT");
        } else {
            throw new IllegalArgumentException("Could not format date to datapoint type '" + dpt + "'");
        }
    }

    /**
     * Retrieves sub number from a DTP ID such as "14.001"
     *
     * @param dptID String with DPT ID
     * @return sub number or -1
     */
    private int getSubNumber(String dptID) {
        int result = -1;
        if (dptID == null) {
            throw new IllegalArgumentException("Parameter dptID cannot be null");
        }

        int dptSepratorPosition = dptID.indexOf('.');
        if (dptSepratorPosition > 0) {
            try {
                result = Integer.parseInt(dptID.substring(dptSepratorPosition + 1, dptID.length()));
            } catch (NumberFormatException nfe) {
                logger.error("toType couldn't identify main and/or sub number in dptID (NumberFormatException): {}",
                        dptID);
            } catch (IndexOutOfBoundsException ioobe) {
                logger.error("toType couldn't identify main and/or sub number in dptID (IndexOutOfBoundsException): {}",
                        dptID);
            }
        }
        return result;
    }

    /**
     * Retrieves main number from a DTP ID such as "14.001"
     *
     * @param dptID String with DPT ID
     * @return main number or -1
     */
    private int getMainNumber(String dptID) {
        int result = -1;
        if (dptID == null) {
            throw new IllegalArgumentException("Parameter dptID cannot be null");
        }

        int dptSepratorPosition = dptID.indexOf('.');
        if (dptSepratorPosition > 0) {
            try {
                result = Integer.parseInt(dptID.substring(0, dptSepratorPosition));
            } catch (NumberFormatException nfe) {
                logger.error("toType couldn't identify main and/or sub number in dptID (NumberFormatException): {}",
                        dptID);
            } catch (IndexOutOfBoundsException ioobe) {
                logger.error("toType couldn't identify main and/or sub number in dptID (IndexOutOfBoundsException): {}",
                        dptID);
            }
        }
        return result;
    }

    /**
     * convert 0...100% to 1 byte 0..255
     *
     * @param percent
     * @return int 0..255
     */
    private int convertPercentToByte(PercentType percent) {
        return percent.toBigDecimal().multiply(BigDecimal.valueOf(255))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP).intValue();
    }
}
