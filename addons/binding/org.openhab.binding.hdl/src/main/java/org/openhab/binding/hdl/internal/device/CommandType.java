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

package org.openhab.binding.hdl.internal.device;

/**
 * The CommandType class define all allowed commands that can be sent on HDL bus
 * and the value it should have.
 * also defines values recived on HDL bus and what value that has.
 *
 * @author stigla - Initial contribution
 *
 */
public enum CommandType {
    Scene_Control(2),
    Response_Scene_Control(3),
    Read_Status_of_Scene(12),
    Response_Read_Status_of_Scene(13),
    Broadcast_Status_of_Scene(61439),
    Read_Area_Information(4),
    Response_Read_Area_Information(55),
    Read_Scene_Information(0),
    Response_Read_Scene_Information(1),
    Modify_Scene_Information(8),
    Response_Modify_Scene_Information(9),
    Sequence_Control(26),
    Response_Sequence_Control(27),
    Read_Status_of_Sequence(57364),
    Response_Read_Status_of_Sequence(57365),
    Broadcast_Status_of_Sequence(61494),
    Single_Channel_Control(49),
    Response_Single_Channel_Control(50),
    Read_Status_of_Channels(51),
    Response_Read_Status_of_Channels(52),
    Read_Current_Level_of_Channels(56),
    Response_Read_Current_Level_of_Channels(57),
    Logic_Control(61718),
    Response_Logic_Control(61719),
    Read_Status_of_Logic_Control(61714),
    Response_Read_Status_of_Logic_Control(61715),
    Broadcast_Status_of_Status_of_Logic_Control(61743),
    Read_System_Date_and_Time(55808),
    Response_Read_System_Date_and_Time(55809),
    Modify_Read_System_Date_and_Time(55810),
    Response_Modify_Read_System_Date_and_Time(55811),
    Broadcast_System_Date_and_Time_Every_Minute(55876),
    UV_Switch_Control(57372),
    Response_UV_Switch_Control(57373),
    Read_Status_of_UV_Switch(57368),
    Response_Read_Status_of_UV_Switch(57389),
    Broadcast_Status_of_Status_of_UV_Switches(57367),
    Curtain_Switch_Control(58336), // E3E0
    Response_Curtain_Switch_Control(58337),
    Read_Status_of_Curtain_Switch(58338), // E3E2
    Response_Read_Status_of_Curtain_Switch(58339),
    Broadcast_Status_of_Status_of_Curtain_Switches(58340), // E3E4
    GPRS_Control(58324),
    Response_GPRS_Control(58325),
    Panel_Control(58328), // 0xE3D8
    Response_Panel_Control(58329), // 0xE3D9
    Read_Status_of_Panel_Control(58330),
    Response_Read_Status_of_Panel_Control(58331),
    Read_AC_Status(6456),
    Response_Read_AC_Status(6457),
    Control_AC_Status(6458),
    Response_Control_AC_Status(6459),
    Read_Floor_Heating_Status_DLP(6468), // 0x1944
    Response_Read_Floor_Heating_Status_DLP(6469), // 0x1945
    Control_Floor_Heating_Status_DLP(6470), // 0x1946
    Response_Control_Floor_Heating_Status_DLP(6471), // 0x1947
    Read_Floor_Heating_Status(16136),
    Response_Read_Floor_Heating_Status(16137),
    Control_Floor_Heating_Status(7260),
    Response_Control_Floor_Heating_Status(7261),
    Read_Floor_Heating_Settings(6464),
    Response_Read_Floor_Heating_Settings(6465),
    Modify_Floor_Heating_Settings(6466),
    Response_Modify_Floor_Heating_Settings(6467),
    Read_Floor_Heating_Day_Night_Time_Setting(7454),
    Response_Read_Floor_Heating_Day_Night_Time_Setting(7455),
    Modify_Floor_Heating_Day_Night_Time_Setting(7453),
    Response_Modify_Floor_Heating_Day_Night_Time_Setting(7455),
    Read_Sensors_Status_DeviceType315(56064),
    Response_Read_Sensors_Status_DeviceType315(56065),
    // Read_Sensors_Status_DeviceType314(5701),
    // Response_Read_Sensors_Status_DeviceType314(5702),
    Read_Sensors_Status(5701),
    Response_Read_Sensors_Status(5702),
    Broadcast_Sensors_Status_Automatically(5703),
    Read_Sensors_Status_SensorsInOne(5636),
    Response_Read_Sensors_Status_SensorsInOne(5637),
    Broadcast_Sensors_Status_SensorsInOne(5680),
    Read_Temperature(58343),
    Response_Read_Temperature(58344),
    Broadcast_Temperature(58341),
    Read_Temperature_New(6472),
    Response_Temperature(6473),
    Read_Security_Module(286),
    Response_Read_Security_Module(287),
    Arm_Security_Module(260),
    Response_Arm_Security_Module(261),
    Alarm_Security_Module(268),
    Response_Alarm_Security_Module(269),
    Music_Control(536),
    Response_Music_Control(537),
    Read_Read_Music_Control_Status(538),
    Response_Music_Control_2(539),
    Auto_broadcast_Dry_Contact_Status(5584),
    Response_Auto_broadcast_Dry_Contact_Status(5585),
    Read_Dry_Contact_Status(5582),
    Response_Read_Dry_Contact_Status(5583),
    Read_Z_audio_Current_Status(6446),
    Response_Read_Z_audio_Current_Status(6447),
    Read_Play_Lists(4964),
    Response_Read_Play_Lists(4965),
    Read_Voltage(55554),
    Response_Read_Voltage(55555),
    Read_Current(55560),
    Response_Read_Current(55561),
    Read_Power(55562),
    Response_Read_Power(55563),
    Read_Power_Factor(55556),
    Response_Read_Power_Factor(55557),
    Read_Electricity(55578),
    Response_Read_Electricity(55579),
    Read_UV_Control_Setup(5796),
    Response_Read_UV_Control_Setup(5797),
    Universal_control(5798),
    Response_Universal_Cotrol(5799),
    Read_Analog_Value(58432),
    Response_Read_Analog_Value(58433),
    Invalid(99999);

    private int value;

    private CommandType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static CommandType create(int value) {
        switch (value) {
            case 2:
                return Scene_Control;
            case 3:
                return Response_Scene_Control;
            case 12:
                return Read_Status_of_Scene;
            case 13:
                return Response_Read_Status_of_Scene;
            case 61439:
                return Broadcast_Status_of_Scene;
            case 4:
                return Read_Area_Information;
            case 55:
                return Response_Read_Area_Information;
            case 0:
                return Read_Scene_Information;
            case 1:
                return Response_Read_Scene_Information;
            case 8:
                return Modify_Scene_Information;
            case 9:
                return Response_Modify_Scene_Information;
            case 26:
                return Sequence_Control;
            case 27:
                return Response_Sequence_Control;
            case 57364:
                return Read_Status_of_Sequence;
            case 57365:
                return Response_Read_Status_of_Sequence;
            case 61494:
                return Broadcast_Status_of_Sequence;
            case 49:
                return Single_Channel_Control;
            case 50:
                return Response_Single_Channel_Control;
            case 51:
                return Read_Status_of_Channels;
            case 52:
                return Response_Read_Status_of_Channels;
            case 56:
                return Read_Current_Level_of_Channels;
            case 57:
                return Response_Read_Current_Level_of_Channels;
            case 61718:
                return Logic_Control;
            case 61719:
                return Response_Logic_Control;
            case 61714:
                return Read_Status_of_Logic_Control;
            case 61715:
                return Response_Read_Status_of_Logic_Control;
            case 61743:
                return Broadcast_Status_of_Status_of_Logic_Control;
            case 55808:
                return Read_System_Date_and_Time;
            case 55809:
                return Response_Read_System_Date_and_Time;
            case 55810:
                return Modify_Read_System_Date_and_Time;
            case 55811:
                return Response_Modify_Read_System_Date_and_Time;
            case 55876:
                return Broadcast_System_Date_and_Time_Every_Minute;
            case 57372:
                return UV_Switch_Control;
            case 57373:
                return Response_UV_Switch_Control;
            case 57368:
                return Read_Status_of_UV_Switch;
            case 57389:
                return Response_Read_Status_of_UV_Switch;
            case 57367:
                return Broadcast_Status_of_Status_of_UV_Switches;
            case 58336:
                return Curtain_Switch_Control;
            case 58337:
                return Response_Curtain_Switch_Control;
            case 58338:
                return Read_Status_of_Curtain_Switch;
            case 58339:
                return Response_Read_Status_of_Curtain_Switch;
            case 58340:
                return Broadcast_Status_of_Status_of_Curtain_Switches;
            case 58324:
                return GPRS_Control;
            case 58325:
                return Response_GPRS_Control;
            case 58328:
                return Panel_Control;
            case 58329:
                return Response_Panel_Control;
            case 58330:
                return Read_Status_of_Panel_Control;
            case 58331:
                return Response_Read_Status_of_Panel_Control;
            case 6456:
                return Read_AC_Status;
            case 6457:
                return Response_Read_AC_Status;
            case 6458:
                return Control_AC_Status;
            case 6459:
                return Response_Control_AC_Status;
            case 6468:
                return Read_Floor_Heating_Status_DLP;
            case 6469:
                return Response_Read_Floor_Heating_Status_DLP;
            case 6470:
                return Control_Floor_Heating_Status_DLP;
            case 6471:
                return Response_Control_Floor_Heating_Status_DLP;
            case 16136:
                return Read_Floor_Heating_Status;
            case 16137:
                return Response_Read_Floor_Heating_Status;
            case 7260:
                return Control_Floor_Heating_Status;
            case 7261:
                return Response_Control_Floor_Heating_Status;
            case 6464:
                return Read_Floor_Heating_Settings;
            case 6465:
                return Response_Read_Floor_Heating_Settings;
            case 6466:
                return Modify_Floor_Heating_Settings;
            case 6467:
                return Response_Modify_Floor_Heating_Settings;
            case 7454:
                return Read_Floor_Heating_Day_Night_Time_Setting;
            case 7455:
                return Response_Read_Floor_Heating_Day_Night_Time_Setting;
            case 7453:
                return Modify_Floor_Heating_Day_Night_Time_Setting;
            case 56064:
                return Read_Sensors_Status_DeviceType315;
            case 56065:
                return Response_Read_Sensors_Status_DeviceType315;
            case 5701:
                return Read_Sensors_Status;
            case 5702:
                return Response_Read_Sensors_Status;
            case 5703:
                return Broadcast_Sensors_Status_Automatically;
            case 5636:
                return Read_Sensors_Status_SensorsInOne;
            case 5637:
                return Response_Read_Sensors_Status_SensorsInOne;
            case 5680:
                return Broadcast_Sensors_Status_SensorsInOne;
            case 58343:
                return Read_Temperature;
            case 58344:
                return Response_Read_Temperature;
            case 58341:
                return Broadcast_Temperature;
            case 6472:
                return Read_Temperature_New;
            case 6473:
                return Response_Temperature;
            case 286:
                return Read_Security_Module;
            case 287:
                return Response_Read_Security_Module;
            case 260:
                return Arm_Security_Module;
            case 261:
                return Response_Arm_Security_Module;
            case 268:
                return Alarm_Security_Module;
            case 269:
                return Response_Alarm_Security_Module;
            case 536:
                return Music_Control;
            case 537:
                return Response_Music_Control;
            case 538:
                return Read_Read_Music_Control_Status;
            case 539:
                return Response_Music_Control_2;
            case 5584:
                return Auto_broadcast_Dry_Contact_Status;
            case 5585:
                return Response_Auto_broadcast_Dry_Contact_Status;
            case 5582:
                return Read_Dry_Contact_Status;
            case 5583:
                return Response_Read_Dry_Contact_Status;
            case 6446:
                return Read_Z_audio_Current_Status;
            case 6447:
                return Response_Read_Z_audio_Current_Status;
            case 4964:
                return Read_Play_Lists;
            case 4965:
                return Response_Read_Play_Lists;
            case 55554:
                return Read_Voltage;
            case 55555:
                return Response_Read_Voltage;
            case 55560:
                return Read_Current;
            case 55561:
                return Response_Read_Current;
            case 55562:
                return Read_Power;
            case 55563:
                return Response_Read_Power;
            case 55556:
                return Read_Power_Factor;
            case 55557:
                return Response_Read_Power_Factor;
            case 55578:
                return Read_Electricity;
            case 55579:
                return Response_Read_Electricity;
            case 5796:
                return Read_UV_Control_Setup;
            case 5797:
                return Response_Read_UV_Control_Setup;
            case 5798:
                return Universal_control;
            case 5799:
                return Response_Universal_Cotrol;
            case 58432:
                return Read_Analog_Value;
            case 58433:
                return Response_Read_Analog_Value;
            default:
                return Invalid;
        }
    }

    @Override
    public String toString() {
        switch (value) {
            case 2:
                return "Scene_Control";
            case 3:
                return "Response_Scene_Control";
            case 12:
                return "Read_Status_of_Scene";
            case 13:
                return "Response_Read_Status_of_Scene";
            case 61439:
                return "Broadcast_Status_of_Scene";
            case 4:
                return "Read_Area_Information";
            case 55:
                return "Response_Read_Area_Information";
            case 0:
                return "Read_Scene_Information";
            case 1:
                return "Response_Read_Scene_Information";
            case 8:
                return "Modify_Scene_Information";
            case 9:
                return "Response_Modify_Scene_Information";
            case 26:
                return "Sequence_Control";
            case 27:
                return "Response_Sequence_Control";
            case 57364:
                return "Read_Status_of_Sequence";
            case 57365:
                return "Response_Read_Status_of_Sequence";
            case 61494:
                return "Broadcast_Status_of_Sequence";
            case 49:
                return "Single_Channel_Control";
            case 50:
                return "Response_Single_Channel_Control";
            case 51:
                return "Read_Status_of_Channels";
            case 52:
                return "Response_Read_Status_of_Channels";
            case 56:
                return "Read_Current_Level_of_Channels";
            case 57:
                return "Response_Read_Current_Level_of_Channels";
            case 61718:
                return "Logic_Control";
            case 61719:
                return "Response_Logic_Control";
            case 61714:
                return "Read_Status_of_Logic_Control";
            case 61715:
                return "Response_Read_Status_of_Logic_Control";
            case 61743:
                return "Broadcast_Status_of_Status_of_Logic_Control";
            case 55808:
                return "Read_System_Date_and_Time";
            case 55809:
                return "Response_Read_System_Date_and_Time";
            case 55810:
                return "Modify_Read_System_Date_and_Time";
            case 55811:
                return "Response_Modify_Read_System_Date_and_Time";
            case 55876:
                return "Broadcast_System_Date_and_Time_Every_Minute";
            case 57372:
                return "UV_Switch_Control";
            case 57373:
                return "Response_UV_Switch_Control";
            case 57368:
                return "Read_Status_of_UV_Switch";
            case 57389:
                return "Response_Read_Status_of_UV_Switch";
            case 57367:
                return "Broadcast_Status_of_Status_of_UV_Switches";
            case 58336:
                return "Curtain_Switch_Control";
            case 58337:
                return "Response_Curtain_Switch_Control";
            case 58338:
                return "Read_Status_of_Curtain_Switch";
            case 58339:
                return "Response_Read_Status_of_Curtain_Switch";
            case 58340:
                return "Broadcast_Status_of_Status_of_Curtain_Switches";
            case 58324:
                return "GPRS_Control";
            case 58325:
                return "Response_GPRS_Control";
            case 58328:
                return "Panel_Control";
            case 58329:
                return "Response_Panel_Control";
            case 58330:
                return "Read_Status_of_Panel_Control";
            case 58331:
                return "Response_Read_Status_of_Panel_Control";
            case 6456:
                return "Read_AC_Status";
            case 6457:
                return "Response_Read_AC_Status";
            case 6458:
                return "Control_AC_Status";
            case 6459:
                return "Response_Control_AC_Status";
            case 6468:
                return "Read_Floor_Heating_Status_DLP";
            case 6469:
                return "Response_Read_Floor_Heating_Status_DLP";
            case 6470:
                return "Control_Floor_Heating_Status_DLP";
            case 6471:
                return "Response_Control_Floor_Heating_Status_DLP";
            case 16136:
                return "Read_Floor_Heating_Status";
            case 16137:
                return "Response_Read_Floor_Heating_Status";
            case 7260:
                return "Control_Floor_Heating_Status";
            case 7261:
                return "Response_Control_Floor_Heating_Status";
            case 6464:
                return "Read_Floor_Heating_Settings";
            case 6465:
                return "Response_Read_Floor_Heating_Settings";
            case 6466:
                return "Modify_Floor_Heating_Settings";
            case 6467:
                return "Response_Modify_Floor_Heating_Settings";
            case 7454:
                return "Read_Floor_Heating_Day_Night_Time_Setting";
            case 7455:
                return "Response_Read_Floor_Heating_Day_Night_Time_Setting";
            case 7453:
                return "Modify_Floor_Heating_Day_Night_Time_Setting";
            case 56064:
                return "Read_Sensors_Status_DeviceType315";
            case 56065:
                return "Response_Read_Sensors_Status_DeviceType315";
            case 5701:
                return "Read_Sensors_Status";
            case 5702:
                return "Response_Read_Sensors_Status";
            case 5703:
                return "Broadcast_Sensors_Status_Automatically";
            case 5636:
                return "Read_Sensors_Status_SensorsInOne";
            case 5637:
                return "Response_Read_Sensors_Status_SensorsInOne";
            case 5680:
                return "Broadcast_Sensors_Status_SensorsInOne";
            case 58343:
                return "Read_Temperature";
            case 58344:
                return "Response_Read_Temperature";
            case 58341:
                return "Broadcast_Temperature";
            case 6472:
                return "Read_Temperature_New";
            case 6473:
                return "Response_Temperature";
            case 286:
                return "Read_Security_Module";
            case 287:
                return "Response_Read_Security_Module";
            case 260:
                return "Arm_Security_Module";
            case 261:
                return "Response_Arm_Security_Module";
            case 268:
                return "Alarm_Security_Module";
            case 269:
                return "Response_Alarm_Security_Module";
            case 536:
                return "Music_Control";
            case 537:
                return "Response_Music_Control";
            case 538:
                return "Read_Read_Music_Control_Status";
            case 539:
                return "Response_Music_Control_2";
            case 5584:
                return "Auto_broadcast_Dry_Contact_Status";
            case 5585:
                return "Response_Auto_broadcast_Dry_Contact_Status";
            case 5582:
                return "Read_Dry_Contact_Status";
            case 5583:
                return "Response_Read_Dry_Contact_Status";
            case 6446:
                return "Read_Z_audio_Current_Status";
            case 6447:
                return "Response_Read_Z_audio_Current_Status";
            case 4964:
                return "Read_Play_Lists";
            case 4965:
                return "Response_Read_Play_Lists";
            case 55554:
                return "Read_Voltage";
            case 55555:
                return "Response_Read_Voltage";
            case 55560:
                return "Read_Current";
            case 55561:
                return "Response_Read_Current";
            case 55562:
                return "Read_Power";
            case 55563:
                return "Response_Read_Power";
            case 55556:
                return "Read_Power_Factor";
            case 55557:
                return "Response_Read_Power_Factor";
            case 55578:
                return "Read_Electricity";
            case 55579:
                return "Response_Read_Electricity";
            case 5796:
                return "Read_UV_Control_Setup";
            case 5797:
                return "Response_Read_UV_Control_Setup";
            case 5798:
                return "Universal_control";
            case 5799:
                return "Response_Universal_Cotrol";
            case 58432:
                return "Read_Analog_Value";
            case 58433:
                return "Response_Read_Analog_Value";
            default:
                return "Invalid";
        }
    }

}
