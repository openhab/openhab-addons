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
 * The DeviceType class contains all names of Devices that are in HDL definiotns.
 * and the number the device has in HDL bus definitions.
 *
 * @author stigla - Initial contribution
 */
public enum DeviceType {

    MD0610(1), // 6 channels 10A dimmable scene controller
    MD1210A(2), // 12ch 10A Professional Intelligent Dimmer(with load status feedback)
    MD2405A(3), // 24ch 5A Professional Intelligent Dimmer(with load status feedback)
    MD0620(4), // 6ch 20A Professional Intelligent Dimmer
    MD1210(5), // 12ch 10A Professional Intelligent Dimmer
    MD2405(6), // 24ch 5A Professional Intelligent Dimmer
    MDH1210(7), // 12ch 10A Professional Intelligent Dimmer(with load status feedback)
    MD0620A(8), // 6ch 20A Professional Intelligent Dimmerr(with load status feedback)
    MDH0610(9), // 6ch 10A Professional Intelligent Dimmer(with load status feedback)
    MDG512_DMX(10), // 512 channels show controller & DMX gateway (20131028波兰率先使用添加)
    MR1220(11), // 12ch 20A Professional Intelligent relay
    MR2420(12), // 24 channels 20A relay
    // MC48IP(13),//48 channels scene controller bus (up to 8 unit 6 channels power modules in which is
    // extendible,standard t-shaped tunnel installation with Ethernet channels)
    // MC48IP(14),//48 channels scene controller bus (up to 8 unit 6 channel power modules in which is
    // extendible,standard t-shaped tunnel installation)
    MD0602(15), // 6ch 2A Intelligent Dimming Module
    MC48IPDMX(16), // 48 channels scene controller bus (standard t-shaped tunnel installation with Ethernet
                   // channels,with DMX)
    // MRDA06(17),//6 channels,0-10V output,dimmable scene controller of fluorescent lamp
    // MC48IP_DMX_231(18),//48 channels scene controller bus (standard t-shaped tunnel installation with DMX)
    MR1210(19), // 12 channels 10A relay
    MD240_DMX(20), // 240 channels show controller
    // MD512_DMX(21),//512 channels show controller
    MR1205(22), // 12 channels 5A relay
    // MR0810_232(23),//8ch 10A Intelligent relay Module
    MD512_DMX(24), // 512 channels show controller(20111226添加)
    MD0403(25), // 4ch 3A Intelligent Dimming module
    MDH2405(26), // 24ch 5A Professional Intelligent Dimmer
    MDH0620(27), // 6ch 20A Professional Intelligent Dimmer
    MD0304(28), // 3 channels 4A dimmable scene controller
    BN_VRV(29), // Toshiba Bacnet Aircon Gateway
    MC48IP(30), // 48 channels scene controller bus (up to 8 unit 6 channels power modules in which is
                // extendible,standard t-shaped tunnel installation with Ethernet channels)
    MC48IP_231(31), // 48 channels scene controller bus (up to 8 unit 6 channels power modules in which is
                    // extendible,standard t-shaped tunnel installation with Ethernet channels)
    // MC48IPDMX_231(32),//48 channels scene controller bus (standard t-shaped tunnel installation with Ethernet
    // channels,with DMX)
    MGPRS_232(33), // GPRS Module-Only apply to V2.3 and above
    MHC48IP_231(34), // Hotel Room Control Host Module
    MRDA04(35), // 4 channels,0-10V output,dimmable scene controller of fluorescent lamp
    MRDA0610_432(36), // 6 channels,0-10V output,dimmable scene controller of fluorescent lamp
    MRDA02(37), // 2 channels,0-10V output,dimmable scene controller of fluorescent lamp
    MGSM_431(38), // SMS Module II
    MC48IPDMX_231(39), // 48 channels scene controller bus (standard t-shaped tunnel installation with Ethernet
                       // channels,with DMX)
    MC48_DALI(40), // 48 channels DALI scene controller
    // MC48_DALI(41), // 48 channels DALI scene controller
    MC64_DALI(42), // 64 channels DALI scene controller
    // MC64_DALI(43), // 64 channels DALI scene controller
    MD0610_4D(44), // 6ch 10A Professional Intelligent Dimmer
    MD1210_4D(45), // 12ch 10A Professional Intelligent Dimmer
    MRDA06(46), // 6 channels,0-10V output,dimmable scene controller of fluorescent lamp
    MC48IP_DMX_231(47), // 48 channels scene controller bus (standard t-shaped tunnel installation with DMX)
    MPL4_38_FH(48), // DLP Panel with AC Music Clock Floor Heating
    MPL4_28(49), // panel with AC 2
    MP8RM(50), // 8 keys multi-functional panel (with infrared control)
    MP8M(51), // 8 keys multi-functional panel
    MP4RM(52), // 4 keys multi-functional panel (with infrared control)
    MP4M(53), // 4 keys multi-functional panel
    MP6R(54), // 6 keys scene panel (with infrared control, scene intensity temporary adjustable)
    MP6(55), // 6 keys scene panel (without infrared control, with scene intensity temporary adjustable)
    MP2R(56), // 2 keys scene panel (with infrared control, scene intensity temporary adjustable)
    MP2(57), // 2 keys scene panel (without infrared control, with scene intensity temporary adjustable)
    MP2RM_AS(58), // 2 keys multi-functional panel (with infrared control)
    MWH6(59), // Wireless receiver
    // MP8RM(60), // 8 keys multi-functional panel (with infrared control)
    // MP4RM(61), // 4 keys multi-functional panel (with infrared control)
    MWL16(62), // Wireless receiver
    // MP4RM(63), // 4 keys multi-functional panel (with infrared control)
    MP6RM_A(64), // 6 keys multi-functional panel (with infrared control)
    MP6RM_AD(65), // 5 keys multi-functional panel (with infrared control)
    MP4RM_A(66), // 4 keys multi-functional panel (with infrared control)
    MP4RM_AD(67), // 3 keys multi-functional panel (with infrared control)
    MP2RM_A(68), // 2 keys multi-functional panel (with infrared control)
    MP2RM_AD(69), // 1 keys multi-functional panel (with infrared control)
    MSR02_20(70), // Room partition
    MP3RM_A(71), // 3 keys multi-functional panel (with infrared control)
    MP3RM_AD(72), // 2 keys multi-functional panel (with infrared control)
    MP1RM_A(73), // 1 keys multi-functional panel (with infrared control)
    MP4RM_L(74), // 4 keys multi-functional panel (with infrared control)
    MP4RML_A(75), // 4 keys multi-functional panel (with infrared control)
    MP6RM_BD(76), // 5 keys multi-functional panel (with infrared control)
    // MP4RML_A(77),//4 keys multi-functional panel (with infrared control)
    MLAC(78), // panel with AC controller
    // MPL4_28(79),//DLP panel with AC and NUVO 2
    MS01R(80), // Motion sensor
    MS01L(81), // Linear sensor
    // MPL4_28(82),//panel with AC and clock(20090618 clock function)
    MLAC_28(83), // Hotel panel only with AC Voice on/off
    // MPL4_28(84),// panel with AC 2
    // MLAC_28(85),//Hotel panel only with AC Voice on/off
    MPL4_38(86), // DLP panel Audio Clock 3(20090618 clock function)
    // MPL4_38(87),//DLP panel Audio Clock Menu 3(20090618 clock function)
    // MPL4_28(88),// panel with AC No. 2
    // MWH6(89),//Wireless receiver
    // MS01R(90), // Motion sensor
    // MS01L(91),//Linear sensor
    MS12(92), // 12 channels sensor
    MS04(93), // Sensor Input Module
    MS06_232(94), // 6 channels Input and output Module
    MHS01(95), // Roof-Mounting Infrared Dual-Technology Motion Sensor
    MPE_2C(96), // Ambient Intensity Monitor
    MHS02(97), // Wide Field Infrared Dual-Technology Motion Sensor
    // MHS02(98),//Wide Field Infrared Dual-Technology Motion Sensor
    // MHS01(99),//Roof-Mounting Infrared Dual-Technology Motion Sensor
    MT12IP(100), // 12 channels timer (standard t-shaped tunnel installation with Ethernet port)
    // MT12IP(101),//12 channels timer (standard t-shaped tunnel installation with Ethernet port)
    MT01(102), // 1 channels Event timer
    // MT12IP(103),//12 channels timer (standard t-shaped tunnel installation with Ethernet port)
    MT02IP(104), // 2 channels timer (standard t-shaped tunnel installation with Ethernet port)
    // MT02IP(105),//2 channels timer (standard t-shaped tunnel installation with Ethernet port)
    MAC01_331(106), // Air-conditioning controller
    // MAC01_331(107),//Air-conditioning controller
    MT12(108), // 12 channels timer (standard t-shaped tunnel installation with Ethernet port)
    MAC01(109), // Air-conditioning controller
    // MT12IP(110),//12 channels timer (standard t-shaped tunnel installation with Ethernet port)
    // MAC01(111),//Air-conditioning controller
    // MAC01(112),//Air-conditioning controller
    MS32(113), // Sensor Input Module
    // MS04(114),//Sensor Input Module
    // MS04(115),//Sensor Input Module
    // MS06_232(116),//6 channels Input and output Module
    // MAC01(117),//Air-conditioning controller
    // MS04(118),//Sensor Input Module
    // MS04(119),//Sensor Input Module
    // MHS110_2C(120),//Roof-Mounting Infrared Dual-Technology Motion Sensor
    // MS04(121),//Sensor Input Module
    // MHS110_2C(122),//Roof-Mounting Infrared Dual-Technology Motion Sensor
    // MHS110_2C(123),//Roof-Mounting Infrared Dual-Technology Motion Sensor
    MTS_2C(124), // Temperature sensor
    // MHS110_2C(125),//Roof-Mounting Infrared Dual-Technology Motion Sensor
    MHS110_2C(126), // Roof-Mounting Infrared Dual-Technology Motion Sensor
    MHS110W_2W(127), // Roof-Mounting Infrared Dual-Technology Motion Sensor
    MBUS_RS232(128), // HDL-BUS/RS232 converter
    // MHS110W_2W(129),//Roof-Mounting Infrared Dual-Technology Motion Sensor
    // MHS110W_2W(130),//Roof-Mounting Infrared Dual-Technology Motion Sensor
    // MHS110W_2W(131),//Roof-Mounting Infrared Dual-Technology Motion Sensor
    // MHS110W_2W(132),//Roof-Mounting Infrared Dual-Technology Motion Sensor
    // MHS110_2C(133),//Roof-Mounting Infrared Dual-Technology Motion Sensor
    MTS04_20(134), // Temperature sensor
    // MS04(135),//Sensor Input Module
    // MS04(136),//Sensor Input Module
    // MS04(137),//Sensor Input Module
    // MHS110W_2W(138),//Roof-Mounting Infrared Dual-Technology Motion Sensor
    // MHS110_2C(139),//Roof-Mounting Infrared Dual-Technology Motion Sensor
    // MS12(140),//Sensor Input Module
    // MS24(141),//Sensor Input Module
    // MS24(142),//Sensor Input Module
    // MPL4_38_FH(149),//DLP Panel with AC Music Clock Floor Heating(20101115加地热)
    MR1220A(150), // 12 channels 20A relay
    MR2420A(151), // 24 channels 20A relay/ without status feedback
    MR0610(152), // 6ch 10A Intelligent relay module
    MR0416A(153), // 4 channels 16A relay
    // MPL4_38_FH(154),//DLP Panel with AC Music Clock Floor Heating(20101228加地热)
    // MPL4_38_FH(155),//DLP Panel with Floor Heating(20110216地热只有基本信息和地热)
    // MPL4_38_FH(156),//DLP Panel with AC Music Clock Floor Heating(20110811红外新功能替换之前)
    // MPL4_38_FH(157),//DLP Panel with AC Music Clock Floor Heating(20111219添加兆度读取，在156基础上)
    // MPL8_48_FH(158),//DLP Panel with AC Music Clock Floor Heating
    MPL8_38(159), // DLP Panel with Music
    // MPL4_38_FH(160),//DLP Panel with AC Music Clock Floor Heating(20110811红外新功能替换之前)
    MPAC01_48(161), // Hotel panel only with AC
    MPL8_48_FH(162), // DLP Panel with AC Music Clock Floor Heating
    MRSPA05_231(163), // SPA controller module
    MPDL_48(164), // SPA controller panel
    // MPTL14_46(165),//DLP Panel with AC Music Clock Floor Heating
    // MLAC_28(166),//Hotel panel only with AC Voice on/off
    // MPL8_48_FH(167), // DLP Panel with AC Music Clock Floor Heating
    // MPTL14_46(168),//DLP Panel with AC Music Clock Floor Heating
    // MLAC_28(169),//Hotel panel only with AC Voice on/off
    // MPTL14_46(170),//DLP Panel with AC Music Clock Floor Heating
    // MPTLC43_46(171),//Colorized DLP Panel
    // MPTL14_46(172),//DLP Panel with AC Music Clock Floor Heating
    // MPTLC43_46(173),//Colorized DLP Panel
    MPTLC43_46(174), // Colorized DLP Panel
    MPTL14_46(175), // DLP Panel with AC Music Clock Floor Heating
    MSR06_231(200), // Room partition
    LED(201), // LED light
    // MFH06_332(209),//6ch Floor Heating Module
    MFH06_332(210), // 6ch Floor Heating Module
    MFH06_432(211), // 6ch Floor Heating Module
    MP201_38(216), // 1 keys multi-functional panel
    MP202_38(217), // 2 keys multi-functional panel
    MP203_38(218), // 3 keys multi-functional panel
    MP204_38(219), // 4 keys multi-functional panel
    MP2RM_48(220), // 2 keys multi-functional panel
    MP3RM_48(221), // 3 keys multi-functional panel
    MP4RM_48(222), // 4 keys multi-functional panel
    MPT01_48(223), // 1 keys multi-functional panel
    MPT02_48(224), // 2 keys multi-functional panel
    MPT03_48(225), // 3 keys multi-functional panel
    MPT04_48(226), // 4 keys multi-functional panel
    // MP103A_38(227),//3 keys multi-functional panel (with infrared control)
    // MBUS01IP_431(229),//1 port switchboard
    HBLS_GW(230), // Honeywell BUS Lighting System - Gateway
    // MBUS01IP_231(231),//1 port switchboard
    // MBUS01IP_431(232),//1 port switchboard
    // MBUS01IP_231(233),//1 port switchboard
    MBUS01IP_431(234), // 1 port switchboard
    // MBUS01IP_231(235),//1 port switchboard
    MBUS01IP_231(236), // 1 port switchboard
    MBUS04IP(237), // 4 port switchboard
    MBUS08IP(238), // 8 port switchboard
    MPB03_48(239), // HDL Smart-HOTEL Custom Door Bell Unit
    // MP202_2825(240),//1 keys multi-functional panel (with infrared control)
    MPB01_48(241), // HDL Smart-HOTEL Custom Door Bell Unit
    MPH101_48(242), // HDL Smart-HOTEL 1 Button Wall Panel
    MPH102_48(243), // HDL Smart-HOTEL 2 Button Wall Panel
    MPH104_48(244), // HDL Smart-HOTEL 4 Button Wall Panel
    MP1RM_AA(245), // 1 keys multi-functional panel (with infrared control)
    // MP2RM_AA(246),//2 keys multi-functional panel (with infrared control)
    MP2RM_AA(247), // 2 keys multi-functional panel (with infrared control)
    MP4RM_AA(248), // 4 keys multi-functional panel (with infrared control)
    MP8RMA(249), // 8 keys multi-functional panel (with infrared control)
    MP6RM_B(250), // 6 keys multi-functional panel (with infrared control)
    // MP6RM_BD(251),//5 keys multi-functional panel (with infrared control)
    MP4RM_B(252), // 4 keys multi-functional panel (with infrared control)
    MP4RM_BD(253), // 3 keys multi-functional panel (with infrared control)
    MP2RM_B(254), // 2 keys multi-functional panel (with infrared control)
    MP2RM_BD(255), // 1 keys multi-functional panel (with infrared control)
    MP3RM_B(256), // 3 keys multi-functional panel (with infrared control)
    MP3RM_BD(257), // 2 keys multi-functional panel (with infrared control)
    MP1RM_B(258), // 1 keys multi-functional panel (with infrared control)
    MP2RM_BS(259), // 2 keys multi-functional panel (with infrared control)
    MP6RM_AA(260), // 6 keys multi-functional panel (with infrared control)
    // MP101A_38(261),//1 keys multi-functional panel (with infrared control)
    // MP102A_38(262),//2 keys multi-functional panel (with infrared control)
    // MP102_38(263),//2 keys multi-functional panel (with infrared control)
    // MP103A_38(264),//3 keys multi-functional panel (with infrared control)
    // MP104_38(265),//4 keys multi-functional panel (with infrared control)
    // MP5RM_CD(266),//5 keys multi-functional panel (with infrared control)
    // MP106_38(267),//6 keys multi-functional panel (with infrared control)
    MP5RM_AAD(268), // 5 keys multi-functional panel (with infrared control)
    MP3RM_AA(269), // 3 keys multi-functional panel (with infrared control)
    MP201_2825(270), // 1 keys multi-functional panel (with infrared control)
    MP202_2825(271), // 2 keys multi-functional panel (with infrared control)
    // MP203_2825(272),//3 keys multi-functional panel (with infrared control)
    // MP204_2825(273),//4 keys multi-functional panel (with infrared control)
    FM_2825(274), // Touch-type radio
    // MP101A_38(275),//1 keys multi-functional panel (with infrared control)
    // MP102A_38(276),//2 keys multi-functional panel (with infrared control)
    // MP102_38(277),//2 keys multi-functional panel (with infrared control)
    // MP103A_38(278),//3 keys multi-functional panel (with infrared control)
    // MP104_38(279),//4 keys multi-functional panel (with infrared control)
    // MP5RM_CD(280),//5 keys multi-functional panel (with infrared control)
    // MP106_38(281),//6 keys multi-functional panel (with infrared control)
    MP204_2825(282), // 4 keys multi-functional panel (with infrared control)
    MP206_2825(283), // 6 keys multi-functional panel (with infrared control)
    MP212_2825(284), // 12 keys multi-functional panel (with infrared control)
    MPN50(285), // 5*10 keys multi-functional panel
    MS104(286), // 4 keys panel
    MP203_2825(287), // 3 keys multi-functional panel (with infrared control)
    MP101A_38(288), // 1 keys multi-functional panel (with infrared control)
    // MP104_48(289),//4 keys multi-functional panel (with infrared control)
    // MP108_48(290),//8 keys multi-functional panel (with infrared control)
    MPN90(291), // 9*10 keys multi-functional panel
    MP102A_38(292), // 2 keys multi-functional panel (with infrared control)
    MP102_38(293), // 2 keys multi-functional panel (with infrared control)
    MP103A_38(294), // 3 keys multi-functional panel (with infrared control)
    MP104_38(295), // 4 keys multi-functional panel (with infrared control)
    MP5RM_CD(296), // 5 keys multi-functional panel (with infrared control)
    MP106_38(297), // 6 keys multi-functional panel (with infrared control)
    // MP104_48(298),//4 keys multi-functional panel (with infrared control)
    // MP108_48(299),//8 keys multi-functional panel (with infrared control)
    // MIR01(300),//Infrared signal emission,remote receiving module
    // MIR01(301),//Infrared signal emission,remote receiving module
    // MIR01F_20(302),//Infrared signal emission,remote receiving module
    // MIR01(303),//Infrared signal emission,remote receiving module
    MIR01(304), // Infrared signal emission,remote receiving module
    // MS08M_2C(305),//eight sensors in one
    // MIR01F_20(306),//Infrared signal emission,remote receiving module
    MAS09_2C(307), // Six Sensors
    MS12_2C(308), // 12in1 Multi function Sensor
    // MS08M_2C(309),//eight sensors in one
    MASTH_2C(310), // Temp, Humidity & Lux Sensor
    MASLA_2C(311), // Lux & Air Quality Sensor
    MHS110_3C(312), // PIR,Temp & Lux Sensor
    // MIR01F_20(313),//Infrared signal emission,remote receiving module
    MS08Mn_2C(314), // eight sensors in one
    // MS08M_2C(315),//eight sensors in one
    MS08Mn01_2C(316), // eight sensors in one
    MSPU05_48(317), // Wall mount Ultrasonic & PIR Sensor
    MS08M_2C(318), // eight sensors in one
    MIR01F_20(319), // Infrared signal emission,remote receiving module
    MIR04F_20(320), // Infrared signal emission,remote receiving module
    // MS12_2C(321), // 12in1 Multi function Sensor
    // MS08Mn_2C(322), // eight sensors in one
    // MSP02_4C(323),//Ceiling Mount PIR Sensor
    // MSPU03_4C(324),//Ceiling Mount Ultrasonic Sensor
    HDL_MRF16_4C(325), // Wireless receiver
    MSPU03_4C(326), // Ceiling Mount Ultrasonic Sensor
    MSP02_4C(327), // Ceiling Mount PIR Sensor
    MSP07M_4C(328), // PIR,Temp & Lux Sensor
    MSP08M_4C(329), // eight sensors in one
    MSOUT_4W(330), // Outdoor Motion, Lux, Temp & Humidity Sensor
    MSWLR_4W(331), // Weather Sensor
    // MSW01_4C(332),//Ceiling Mount PIR Sensor
    MSW01_4C(333), // Ceiling Mount PIR Sensor
    // MS04(351),//Sensor Input Module
    // MS24(352),//Sensor Input Module
    MS24(353), // Sensor Input Module
    // MS04(354),//Sensor Input Module
    MSD04_40(355), // Sensor Input Module
    // MS04(356),//Sensor Input Module
    MSD08_40(357), // 8 Channels Sensor Input Module
    // MS24(358), // Sensor Input Module
    MS28(359), // Sensor Input Module
    MS38(360), // Sensor Input Module
    MAIR01(400), // Air-Condition controller
    MR0416_C(423), // 4 channels 16A relay
    MR0416_231(424), // 4ch 16A Intelligent relay module
    MR0616_B(425), // 6 channels 16A relay
    MR0616_232(426), // 6ch 16A Intelligent Relay Module
    MR0820C_232(427), // 8 channels 20A Intelligent Relay Module
    MR0816_232(428), // 8ch 16A Intelligent Relay relay module
    MR1220_A(429), // 12 channels 20A relay
    MR1216_233(430), // 12 channels 16A relay
    MR1205_A(431), // 12 channels 5A relay
    MR2420_A(432), // 24 channels 20A relay
    MR0416B(433), // 4 channels 16A relay
    MR0420_A(434), // 4 channels 20A relay
    MR0410_231(435), // 4 channels 10A relay
    MR0810_232(436), // 8 channels 10A relay
    MR0425_231(437), // 4ch 25A Intelligent relay module
    // MR0410_331(438),//4 channels 10A relay
    // MR0810_332(439),//8 channels 10A relay
    // MR1210_333(440),//12 channels 10A relay
    MR0410_331(441), // 4 channels 10A relay III
    MR0810_332(442), // 8 channels 10A relay III
    MR1210_333(443), // 12 channels 10A relay III
    MR0416_431(444), // 4 channels 16A relay IV
    MR0816_432(445), // 8 channels 16A relay IV
    MR1216_433(446), // 12 channels 16A relay IV
    MR0410_431(447), // 4 channels 10A relay IV
    MR0810_432(448), // 8 channels 10A relay IV
    // MR1210_433(449),//12 channels 10A relay IV
    MR1616_434(450), // 16 channels 16A relay IV
    // MR1610_433(451),//16 channels 10A relay IV
    MR1210_433(452), // 12 channels 10A relay IV
    MR1610_433(453), // 16 channels 10A relay IV
    // MRDA0610_432(454),//6 channels,0-10V output,dimmable scene controller of fluorescent lamp
    MTS7000(500), // 7" Ture Color Touch Screen
    MH002R16(596), // Hotel room control module
    HMIX13(597), // Hotel room control module
    MD08DSI_232(598), // 8ch DSI controller
    MH12(599), // Hotel room control module
    // MD0602_232(600),//6ch 2A Intelligent Dimming Module
    // MD0403_232(601),//4ch 3A Intelligent Dimming Module
    // MD0206_232(602),//2ch 6A Intelligent Dimming Module
    MH10(603), // Hotel room control module
    // MP4RML_A(604),// panel for Hotel
    // MP4RML_A(605),// panel for Hotel
    MDT0203_233(606), // 2ch 3A Trailing Edge Dimming module
    MDT0402_233(607), // 4ch 2A Trailing Edge Dimming module
    MDT0601_233(608), // 6ch 1A Trailing Edge Dimming module
    MDT0106_233(609), // 1ch 6A Trailing Edge Dimming module
    MD0602_232(610), // 6ch 2A Intelligent Dimming Module
    MD0403_232(611), // 4ch 3A Intelligent Dimming Module
    MD0206_232(612), // 2ch 6A Intelligent Dimming Module
    // MDLED0605_432(613),//6ch 5A Intelligent LED Dimming Module
    MD0206_432(614), // 2ch 6A Leading Edge Dimming module
    MD0403_432(615), // 4ch 3A Leading Edge Dimming module
    MD0405_433(616), // 4ch 5A Leading Edge Dimming module
    MD0602_432(617), // 6ch 2A Leading Edge Dimming module
    MDT0106_433(618), // 1ch 6A Trailing Edge Dimming module
    MDT0203_433(619), // 2ch 3A Trailing Edge Dimming module
    MDT04015_433(620), // 4ch 1.5A Trailing Edge Dimming module
    MDT0601_433(621), // 6ch 2A Trailing Edge Dimming module
    MDLED0605_432(622), // 6ch 5A Intelligent LED Dimming Module
    MDLED0401_432(623), // 4ch 1A Intelligent LED Dimming Module
    MHD02R18(624), // Hotel room control module
    // MW02(700),//Curtain controller
    // MW02(701),//Curtain controller
    // MW02(702),//Curtain controller
    MW02(703), // Curtain controller
    // MW02_231(704),//2Ch Window Curtain controller
    // MW02_231(705),//2Ch Window Curtain controller
    // MW02_231(706),//2Ch Window Curtain controller
    MW02_231(707), // 2Ch Window Curtain controller
    // MWM70B_12(708),//roller shutters Curtain controller
    MVSM45B_12(709), // Motorised blind motor
    MWM70B_12(710), // 1Ch Window Curtain controller
    // MAC01_331(727),//Air-conditioning controller
    // MAC01_331(728),//Air-conditioning controller
    MIR04T_40(729), // IR Air Conditioner
    // MAC01(730),//Air-conditioning controller
    // MAC01_331(731),//Air-conditioning controller
    // MAC01_331(732),//Air-conditioning controller
    // MAC01_331(733),//Air-conditioning controller
    // MAC01_331(734),//Air-conditioning controller
    // MAC01_331(735),//Air-conditioning controller
    // MAC01_331(736),//Air-conditioning controller
    // MAC01_331(737),//Air-conditioning controller
    // MAC01_331(738),//Air-conditioning controller
    // MAC01_331(739),//Air-conditioning controller
    // MCIP_RF_10(740),//Mesh Gateway
    MCIP_RF_10(741), // Mesh Gateway
    MCIP02_RF_10(742), // Mesh Gateway
    MDMXI08(800), // 8 channels DMX input module
    // MC96IPDMX(850),//96 channels scene controller bus (standard t-shaped tunnelinstallation with DMX)
    // MC96IPDMX(851),//96 channels scene controller bus (standard t-shaped tunnelinstallation with DMX)
    MC96IPDMX(852), // 96 channels scene controller bus (standard t-shaped tunnelinstallation with DMX)
    // MC48IPDMX(853),//48 channels scene controller bus (standard t-shaped tunnelinstallation with DMX)
    // MC48IPDMX_231(854),//48 channels scene controller bus (standard t-shaped tunnel installation with Ethernet
    // channels,with DMX)
    MPM03IP_432(894), // Single-phase Digital electric meter
    // MPM1P03_231(895),//Single-phase Digital electric meter
    // MPM3P01_231(896),//Three-Phase Four-Wire Digital electric meter
    MPM3P01_231(897), // Three-Phase Four-Wire Digital electric meter
    MPM1P03_231(898), // Single-phase Digital electric meter
    // MPM01(899),//Digital electric meter
    MPM01(900), // Digital electric meter
    // Mzbox(901),//standard Music player devices
    // Mzbox(902),//standard Music player devices
    // Mzbox(903),//standard Music player devices()
    // Mzbox(904),//standard Music player devices
    // Mzbox(905),//standard Music player devices
    // MzBox_20(906),//standard Music player devices
    // Mzbox(907),//standard Music player devices
    MMC_01(908), // Media control devices
    // Mzbox(909),//standard Music player devices
    // MzBox_20(910),//standard Music player devices
    MZDN_432(911), // standard Music player devices
    Mzbox(912), // standard Music player devices
    MzBox_20(913), // standard Music player devices
    MFTCL_10(914), // standard Music player devices
    MKTVCL_10(915), // standard Music player devices
    MHAI(950), // HAI<-->HDL Data Transfer
    MCM(951), // CoolMaster<-->HDL Data Transfer
    MDK(952), // DAIKIN<-->HDL Data Transfer
    MRS232_Curtain(960), // RS232<->HDL-BUS Curtain Converter
    MEIB_231(1000), // EIB/HDL-BUS converter
    MCEIB_231(1001), // EIB/HDL-BUS converter
    MBUS_SAMSUNG(1005), // SAMSUNG touch screen convert to HDL-BUS
    C_BUS(1006), // C-BUS converter
    // MRS232_231(1007),//RS232<->HDL-BUS data transfer
    // MRS232_231(1008),//RS232<->HDL-BUS data transfer
    // MRS232_231(1009),//RS232<->HDL-BUS data transfer
    MIR01L(1010), // Infrared learner
    // MEIB(1011),//EIB<-->HDL Data Transfer
    // MEIB(1012),//EIB<-->HDL Data Transfer
    // MRS232IP_231(1013),//RS232<->HDL-BUS data transfer(Professional version)
    // MRS232IP_231(1014),//RS232<->HDL-BUS data transfer(Professional version)
    MDR512(1015), // DMX show control recorder
    // MRS232_231(1016),//RS232<->HDL-BUS data transfer
    // MRS232_MC(1017),//RS232<->HDL-BUS Media Converter
    // MRS232_231(1018),//RS232<->HDL-BUS data transfer
    // MRS232_231(1019),//RS232<->HDL-BUS data transfer
    // MRS232_AC(1020),//RS232<->HDL-BUS AC Converter
    MHC_DA(1021), // HanChang<->HDL-BUS Data Converter
    MJACUZZI(1022), // JACUZZI<->HDL-BUS Data Converter
    Bacnet(1023), // Bacnet<->HDL-BUS Data Converter
    // MRS232_231(1024),//RS232<->HDL-BUS data transfer
    MR232_M1(1025), // ELK-M1<->HDL-BUS data transfer
    MR232_RCU(1026), // RCU<->HDL-BUS data transfer
    MRS232IP_231(1027), // RS232<->HDL-BUS data transfer(Professional version)
    // MRS232_231(1028),//RS232<->HDL-BUS Curtain Controller
    // MRS232_AC(1029),//RS232<->HDL-BUS AC Converter
    // MRS232_MC(1030),//RS232<->HDL-BUS Media Converter
    MRS232_MC(1031), // RS232<->HDL-BUS Media Converter
    // MRS232_AC(1032),//RS232<->HDL-BUS MODBUS Converter
    MRS232_231(1033), // RS232<->HDL-BUS data transfer
    // MRS232_AC(1034),//RS232<->HDL-BUS AC Converter
    // MRS232_AC(1035),//RS232<->HDL-BUS AC Converter
    // MRS232_AC(1036),//RS232<->HDL-BUS AC Converter
    MRS232_AC(1037), // RS232<->HDL-BUS AC Converter
    MOD_DTU(1038), // RS485<->HDL-BUS Environment Detector
    MV_ZIGBEE(1047), // Vingcard zigbee gateway (Zigbee 2015 12 07 杨旭)
    MUPS(1048), // UPS Power
    MUPS_231(1049), // RS232<->HDL-BUS data transfer(Professional version)
    // MEIB(1050),//EIB<-->HDL Data Transfer
    MEIB(1051), // EIB<-->HDL Data Transfer
    MHIOU_332(1052), // Hotel Output and Input module Indonesia
    MHIOU_432(1053), // Hotel Output and Input module Indonesia
    // ML01(1100),//Logic timer
    // ML01(1101),//Logic timer
    // ML01(1102),//Logic timer
    // ML01(1103),//Logic timer
    MCLog_231(1104), // Logic timer
    // ML01(1105),//Logic timer
    // ML01(1106),//Logic timer
    // ML01(1107),//Logic timer
    // ML01(1108),//Logic timer
    ML01(1109), // Logic timer
    // ML01(1110), // Logic timer
    MDCAC(1150), // DAIKIN Central AC Module
    MSPU05_RF_1C(1500), // Wall mount Ultrasonic & PIR Sensor
    MSPU04_48(1501), // Wireless Ultrasonic Sensor Embedded
    // MWH6(2000),//Wireless receiver
    // MP2B_48(2001),//2 keys multi-functional panel
    // MP4B_48(2002),//4 keys multi-functional panel
    // MP6B_48(2003),//6 keys multi-functional panel
    // MP8B_48(2004),//8 keys multi-functional panel
    MP2K_48(2005), // 2 keys multi-functional panel
    MP4K_48(2006), // 4 keys multi-functional panel
    MP6K_48(2007), // 6 keys multi-functional panel
    MP8K_48(2008), // 8 keys multi-functional panel
    // MWH6(2009),//Wireless receiver
    MP2B_46(2010), // 2 keys multi-functional panel
    MP4B_46(2011), // 4 keys multi-functional panel
    MP6B_46(2012), // 6 keys multi-functional panel
    MP8B_46(2013), // 8 keys multi-functional panel
    MP1D_48(2014), // 1 Button Smart switch Digital Wall Panel
    MP2D_48(2015), // 2 Button Smart switch Digital Wall Panel
    MP3D_48(2016), // 3 Button Smart switch Digital Wall Panel
    MP4BD_48(2017), // 4 Button Smart switch Digital Wall Panel
    MP5D_48(2018), // 5 Button Smart switch Digital Wall Panel
    // MP01R_48(2019),//1 Button Smart switch Digital Wall Panel
    // MP02R_48(2020),//2 Button Smart switch Digital Wall Panel
    // MP04R_48(2021),//4 Button Smart switch Digital Wall Panel
    // MPT2_46(2022),//2 keys multi-functional panel
    // MPT4_46(2023),//4 keys multi-functional panel
    // MPT6_46(2024),//6 keys multi-functional panel
    MP4A_48(2025), // 2 keys multi-functional panel
    MP8A_48(2026), // 4 keys multi-functional panel
    MP5R_48(2027), // 5 keys multi-functional panel
    MP5M_48(2028), // 5 keys multi-functional panel
    MPT1_48(2029), // 1 key Touch Panel with Temperature
    MPT2_48(2030), // 2 keys Touch Panel with Temperature
    MPT3_48(2031), // 3 keys Touch Panel with Temperature
    MPT4_48(2032), // 4 keys Touch Panel with Temperature
    MP1C_48(2033), // 1 Button Smart Wall Switch Panel with Temperature
    MP2C_48(2034), // 2 Button Smart Wall Switch Panel with Temperature
    MP3C_48(2035), // 3 Button Smart Wall Switch Panel with Temperature
    MP4BC_48(2036), // 4 Button Smart Wall Switch Panel with Temperature
    MP5C_48(2037), // 5 Button Smart Wall Switch Panel with Temperature
    MP6C_48(2038), // 6 Button Smart Wall Switch Panel with Temperature
    MP13C_48(2039), // 13 Button Smart Wall Switch Panel with Temperature
    MP01R_48(2040), // 1 Button Smart switch Digital Wall Panel with Temperature
    MP02R_48(2041), // 2 Button Smart switch Digital Wall Panel with Temperature
    MP03R_48(2042), // 3 Button Smart switch Digital Wall Panel with Temperature
    MP04R_48(2043), // 4 Button Smart switch Digital Wall Panel with Temperature
    // MP2B_48(2044),//2 keys multi-functional panel
    // MP4B_48(2045),//4 keys multi-functional panel
    // MP6B_48(2046),//6 keys multi-functional panel
    // MP8B_48(2047),//8 keys multi-functional panel
    MP2B_48(2048), // 2 keys multi-functional panel with temperature
    MP4B_48(2049), // 4 keys multi-functional panel with temperature
    MP6B_48(2050), // 6 keys multi-functional panel with temperature
    MP8B_48(2051), // 8 keys multi-functional panel with temperature
    MP6D_48(2053), // 6 Button Smart switch Digital Wall Panel
    MP13D_48(2054), // 13 Button Smart switch Digital Wall Panel
    MP201_3825(2055), // 1 key Touch Panel with Temperature
    MP202_3825(2056), // 2 keys Touch Panel with Temperature
    MP204_3825(2057), // 4 keys Touch Panel with Temperature
    // MPL4_28(2058),// panel with AC 2
    MP104_48(2059), // 2 keys multi-functional panel (with infrared control)
    MP108_48(2060), // 4 keys multi-functional panel (with infrared control)
    MPT6_46(2061), // 6 keys multi-functional panel
    MPT1_58(2062), // 1 key Touch Panel with Temperature
    MPT2_58(2063), // 2 key Touch Panel with Temperature
    MPT3_58(2064), // 3 key Touch Panel with Temperature
    MPT4_58(2065), // 4 key Touch Panel with Temperature
    MPT4_46(2067), // Touch 4 keys multi-functional panel
    MPT2_46(2068), // Touch 2 keys multi-functional panel
    MPT6_D(2069), // 6 Touch keys multi-functional panel
    MPT2_D(2070), // 2 Touch keys multi-functional panel
    // MSM(3047),//Advanced Security Controller
    // MSM(3048),//Advanced Security Controller
    // MSM(3049),//Advanced Security Controller
    MP101B_28013(3050), // HOTEL 1 keys multi-functional panel
    MP102B_28013(3051), // HOTEL 2 keys multi-functional panel
    MP103B_28013(3052), // HOTEL 3 keys multi-functional panel
    MP104B_28013(3053), // HOTEL 4 keys multi-functional panel
    // MHB_1010(3054),//Digital doorbell
    MSM(3055), // Security Module
    // MHBRF_1010(3056),//Hotel Digital doorbell(RF)
    MHIC_1825(3057), // Gold Metal RF Card Reader I
    // MHIC_1811(3058),//Glass IC Card Reader I
    // MHB2_2825(3059),//Digital doorbell
    MHIC_2825_Ⅱ(3060), // Hotel Intelligence Card Reader
    // MHBRF_1010(3061),//Hotel Digital doorbell(RF)
    MHIC_1811(3062), // Glass RF Card Reader I
    MHIC_3811(3063), // Glass Photoelectric Card Reader III
    MHIC_2825(3064), // Gold Metal RF Card Reader II
    MHIC_2811(3065), // Glass RF Card Reader II
    // MHIC_3825(3066),//Gold Metal Photoelectric Card Reader III
    MHB_1010(3067), // Digital doorbell
    MPCH01_48(3068), // RF Card Reader & Master Control
    MHRF_28(3069), // Plastic RF Card Reader(Special Version)
    // MHBRF_1010(3070),//Hotel Digital doorbell(RF)
    MHB2_2825(3071), // Digital doorbell
    MPTB01RF_48(3072), // Hotel Digital doorbell(RF)
    // MHBRF_1010(3073),//Hotel Digital doorbell(RF)
    MHBRF_1010(3074), // Hotel Digital doorbell(RF)
    MHB2_3825(3075), // Hotel Digital doorbell(RF)
    MHIC_3825(3076), // Plastic RF Card Reader
    MHIC_48(3079), // Hotel master card switch
    MDS(3200), // Door Station
    // MHRCU_433(3500),//Hotel Room Control Host Module
    // MHRCU_433(3501),//22 channels mix module
    MHRCU_433(3502), // 22 channels mix module
    // MGSM_431(4000),//SMS Module II
    MBR06_431(4500), // 6 Port Bus Routing module
    // MPL8_RF_18(5000),//DLP Panel with AC Music Clock Floor Heating
    // MPL8_RF_18(5001),//DLP Panel with AC Music Clock Floor Heating
    MPL8_RF_18(5002), // DLP Panel with AC Music Clock Floor Heating
    MPL4FH_RF_18(5003), // Four Buttons RF panel with Floorheating Function
    // MP2B_RF_18(5030),//2 key wireless multifunction panel generation 1
    // MP4B_RF_18(5031),//4 key wireless multifunction panel generation 1
    // MP6B_RF_18(5032),//6 key wireless multifunction panel generation 1
    // MP8B_RF_18(5033),//8 key wireless multifunction panel generation 1
    // MP01R_RF_18(5034),//1 key wireless multifunction round button panel
    // MP02R_RF_18(5035),//2 key wireless multifunction round button panel
    // MP03R_RF_18(5036),//3 key wireless multifunction round button panel
    // MP04R_RF_18(5037),//4 key wireless multifunction round button panel
    // MPT1_RF_18(5038),//1 key wireless multifunction glass touch panel
    // MPT2_RF_18(5039),//2 key wireless multifunction glass touch panel
    // MPT3_RF_18(5040),//3 key wireless multifunction glass touch panel
    // MPT4_RF_18(5041),//4 key wireless multifunction glass touch panel
    MP2B_RF_18(5042), // 2 key wireless multifunction panel generation 1
    MP4B_RF_18(5043), // 4 key wireless multifunction panel generation 1
    MP6B_RF_18(5044), // 6 key wireless multifunction panel generation 1
    MP8B_RF_18(5045), // 8 key wireless multifunction panel generation 1
    MP01R_RF_18(5046), // 1 key wireless multifunction round button panel
    MP02R_RF_18(5047), // 2 key wireless multifunction round button panel
    MP03R_RF_18(5048), // 3 key wireless multifunction round button panel
    // MP04R_RF_18(5049),//4 key wireless multifunction round button panel
    MPT1_RF_18(5050), // 1 key wireless multifunction glass touch panel
    MPT2_RF_18(5051), // 2 key wireless multifunction glass touch panel
    MPT3_RF_18(5052), // 3 key wireless multifunction glass touch panel
    // MPT4_RF_18(5053),//4 key wireless multifunction glass touch panel
    MP04R_RF_18(5054), // 4 key wireless multifunction round button panel
    MPT4_RF_18(5055), // 4 key wireless multifunction glass touch panel
    MPT6_RF_18(5056), // RF 6 key wireless multifunction glass touch panel
    // MPT4_RF_28(5057),//RF 4 key wireless multifunction glass touch panel
    MPT2_RF_28(5058), // RF 2 key wireless multifunction glass touch panel
    MPT4_RF_28(5059), // RF 4 key wireless multifunction glass touch panel
    MWM70_RF_18(5300), // 1Ch Window Curtain controller
    MVSM45B__RF_12(5301), // Wireless Motorised blind motor
    MPC01_RF_18(5302), // 1Ch Window Curtain controller
    MPR01_RF_18(5500), // 1 channels 16A relay driver
    MPR02_RF_18(5501), // 2 channels 8A relay driver
    MPD01_RF_18(5700), // 1ch 5A Dimmer
    // MPS04_RF_18(5900),//Sensor Input Module
    MR_PLUG_RF_1P(6100), // Wireless Relay Socket
    MPS04_RF_18(6101), // Sensor Input Module
    Camera(9000), // Camera
    Server(60000), // Server
    Blooder(65498), // Blooder
    // Blooder(65499),//Blooder
    Modbus(65500), // Modbus
    DiiVA(65501), // DiiVA MXM controller
    TouchLife(65532), // TouchLife
    PC(65534), // PC

    Invalid(65535); // Invalid

    private int value;

    private DeviceType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DeviceType create(int value) {
        switch (value) {
            case 1:
                return MD0610;
            case 2:
                return MD1210A;
            case 3:
                return MD2405A;
            case 4:
                return MD0620;
            case 5:
                return MD1210;
            case 6:
                return MD2405;
            case 7:
                return MDH1210;
            case 8:
                return MD0620A;
            case 9:
                return MDH0610;
            case 10:
                return MDG512_DMX;
            case 11:
                return MR1220;
            case 12:
                return MR2420;
            case 13:
                return MC48IP;
            case 14:
                return MC48IP;
            case 15:
                return MD0602;
            case 16:
                return MC48IPDMX;
            case 17:
                return MRDA06;
            case 18:
                return MC48IP_DMX_231;
            case 19:
                return MR1210;
            case 20:
                return MD240_DMX;
            case 21:
                return MD512_DMX;
            case 22:
                return MR1205;
            case 23:
                return MR0810_232;
            case 24:
                return MD512_DMX;
            case 25:
                return MD0403;
            case 26:
                return MDH2405;
            case 27:
                return MDH0620;
            case 28:
                return MD0304;
            case 29:
                return BN_VRV;
            case 30:
                return MC48IP;
            case 31:
                return MC48IP_231;
            case 32:
                return MC48IPDMX_231;
            case 33:
                return MGPRS_232;
            case 34:
                return MHC48IP_231;
            case 35:
                return MRDA04;
            case 36:
                return MRDA0610_432;
            case 37:
                return MRDA02;
            case 38:
                return MGSM_431;
            case 39:
                return MC48IPDMX_231;
            case 40:
                return MC48_DALI;
            case 41:
                return MC48_DALI;
            case 42:
                return MC64_DALI;
            case 43:
                return MC64_DALI;
            case 44:
                return MD0610_4D;
            case 45:
                return MD1210_4D;
            case 46:
                return MRDA06;
            case 47:
                return MC48IP_DMX_231;
            case 48:
                return MPL4_38_FH;
            case 49:
                return MPL4_28;
            case 50:
                return MP8RM;
            case 51:
                return MP8M;
            case 52:
                return MP4RM;
            case 53:
                return MP4M;
            case 54:
                return MP6R;
            case 55:
                return MP6;
            case 56:
                return MP2R;
            case 57:
                return MP2;
            case 58:
                return MP2RM_AS;
            case 59:
                return MWH6;
            case 60:
                return MP8RM;
            case 61:
                return MP4RM;
            case 62:
                return MWH6;
            case 63:
                return MP4RM;
            case 64:
                return MP6RM_A;
            case 65:
                return MP6RM_AD;
            case 66:
                return MP4RM_A;
            case 67:
                return MP4RM_AD;
            case 68:
                return MP2RM_A;
            case 69:
                return MP2RM_AD;
            case 70:
                return MSR02_20;
            case 71:
                return MP3RM_A;
            case 72:
                return MP3RM_AD;
            case 73:
                return MP1RM_A;
            case 74:
                return MP4RM_L;
            case 75:
                return MP4RML_A;
            case 76:
                return MP6RM_BD;
            case 77:
                return MP4RML_A;
            case 78:
                return MLAC;
            case 79:
                return MPL4_28;
            case 80:
                return MS01R;
            case 81:
                return MS01L;
            case 82:
                return MPL4_28;
            case 83:
                return MLAC_28;
            case 84:
                return MPL4_28;
            case 85:
                return MLAC_28;
            case 86:
                return MPL4_38;
            case 87:
                return MPL4_38;
            case 88:
                return MPL4_28;
            case 89:
                return MWH6;
            case 90:
                return MS01R;
            case 91:
                return MS01L;
            case 92:
                return MS12;
            case 93:
                return MS04;
            case 94:
                return MS06_232;
            case 95:
                return MHS110_2C;
            case 96:
                return MPE_2C;
            case 97:
                return MHS02;
            case 98:
                return MHS02;
            case 99:
                return MHS110_2C;
            case 100:
                return MT12IP;
            case 101:
                return MT12IP;
            case 102:
                return MT01;
            case 103:
                return MT12IP;
            case 104:
                return MT02IP;
            case 105:
                return MT02IP;
            case 106:
                return MAC01_331;
            case 107:
                return MAC01_331;
            case 108:
                return MT12;
            case 109:
                return MAC01;
            case 110:
                return MT12IP;
            case 111:
                return MAC01;
            case 112:
                return MAC01;
            case 113:
                return MS32;
            case 114:
                return MS04;
            case 115:
                return MS04;
            case 116:
                return MS06_232;
            case 117:
                return MAC01;
            case 118:
                return MS04;
            case 119:
                return MS04;
            case 120:
                return MHS110_2C;
            case 121:
                return MS04;
            case 122:
                return MHS110_2C;
            case 123:
                return MHS110_2C;
            case 124:
                return MTS_2C;
            case 125:
                return MHS110_2C;
            case 126:
                return MHS110_2C;
            case 127:
                return MHS110W_2W;
            case 128:
                return MRS232_231;
            case 129:
                return MHS110W_2W;
            case 130:
                return MHS110W_2W;
            case 131:
                return MHS110W_2W;
            case 132:
                return MHS110W_2W;
            case 133:
                return MHS110_2C;
            case 134:
                return MTS04_20;
            case 135:
                return MS04;
            case 136:
                return MS04;
            case 137:
                return MS04;
            case 138:
                return MHS110W_2W;
            case 139:
                return MHS110_2C;
            case 140:
                return MS12;
            case 141:
                return MS24;
            case 142:
                return MS24;
            case 149:
                return MPL4_38_FH;
            case 150:
                return MR1220A;
            case 151:
                return MR2420A;
            case 152:
                return MR0610;
            case 153:
                return MR0416A;
            case 154:
                return MPL4_38_FH;
            case 155:
                return MPL4_38_FH;
            case 156:
                return MPL4_38_FH;
            case 157:
                return MPL4_38_FH;
            case 158:
                return MPL8_48_FH;
            case 159:
                return MPL8_38;
            case 160:
                return MPL4_38_FH;
            case 161:
                return MPAC01_48;
            case 162:
                return MPL8_48_FH;
            case 163:
                return MRSPA05_231;
            case 164:
                return MPDL_48;
            case 165:
                return MPTL14_46;
            case 166:
                return MLAC_28;
            case 167:
                return MPL8_48_FH;
            case 168:
                return MPTL14_46;
            case 169:
                return MLAC_28;
            case 170:
                return MPTL14_46;
            case 171:
                return MPTLC43_46;
            case 172:
                return MPTL14_46;
            case 173:
                return MPTLC43_46;
            case 174:
                return MPTLC43_46;
            case 175:
                return MPTL14_46;
            case 180:
                return MPL8_48_FH;
            case 200:
                return MSR06_231;
            case 201:
                return LED;
            case 209:
                return MFH06_332;
            case 210:
                return MFH06_332;
            case 211:
                return MFH06_432;
            case 216:
                return MP201_38;
            case 217:
                return MP202_38;
            case 218:
                return MP203_38;
            case 219:
                return MP204_38;
            case 220:
                return MP2RM_48;
            case 221:
                return MP3RM_48;
            case 222:
                return MP4RM_48;
            case 223:
                return MPT01_48;
            case 224:
                return MPT02_48;
            case 225:
                return MPT03_48;
            case 226:
                return MPT04_48;
            case 227:
                return MP103A_38;
            case 229:
                return MBUS01IP_431;
            case 230:
                return HBLS_GW;
            case 231:
                return MBUS01IP_231;
            case 232:
                return MBUS01IP_431;
            case 233:
                return MBUS01IP_231;
            case 234:
                return MBUS01IP_431;
            case 235:
                return MBUS01IP_231;
            case 236:
                return MBUS01IP_231;
            case 237:
                return MBUS04IP;
            case 238:
                return MBUS08IP;
            case 239:
                return MPB03_48;
            case 240:
                return MP202_2825;
            case 241:
                return MPB01_48;
            case 242:
                return MPH101_48;
            case 243:
                return MPH102_48;
            case 244:
                return MPH104_48;
            case 245:
                return MP1RM_AA;
            case 246:
                return MP2RM_AA;
            case 247:
                return MP2RM_AA;
            case 248:
                return MP4RM_AA;
            case 249:
                return MP8RMA;
            case 250:
                return MP6RM_B;
            case 251:
                return MP6RM_BD;
            case 252:
                return MP4RM_B;
            case 253:
                return MP4RM_BD;
            case 254:
                return MP2RM_B;
            case 255:
                return MP2RM_BD;
            case 256:
                return MP3RM_B;
            case 257:
                return MP3RM_BD;
            case 258:
                return MP1RM_B;
            case 259:
                return MP2RM_BS;
            case 260:
                return MP6RM_AA;
            case 261:
                return MP101A_38;
            case 262:
                return MP102A_38;
            case 263:
                return MP102_38;
            case 264:
                return MP103A_38;
            case 265:
                return MP104_38;
            case 266:
                return MP5RM_CD;
            case 267:
                return MP106_38;
            case 268:
                return MP5RM_AAD;
            case 269:
                return MP3RM_AA;
            case 270:
                return MP201_2825;
            case 271:
                return MP202_2825;
            case 272:
                return MP203_2825;
            case 273:
                return MP204_2825;
            case 274:
                return FM_2825;
            case 275:
                return MP101A_38;
            case 276:
                return MP102A_38;
            case 277:
                return MP102_38;
            case 278:
                return MP103A_38;
            case 279:
                return MP104_38;
            case 280:
                return MP5RM_CD;
            case 281:
                return MP106_38;
            case 282:
                return MP204_2825;
            case 283:
                return MP206_2825;
            case 284:
                return MP212_2825;
            case 285:
                return MPN50;
            case 286:
                return MS104;
            case 287:
                return MP203_2825;
            case 288:
                return MP101A_38;
            case 289:
                return MP104_48;
            case 290:
                return MP108_48;
            case 291:
                return MPN90;
            case 292:
                return MP102A_38;
            case 293:
                return MP102_38;
            case 294:
                return MP103A_38;
            case 295:
                return MP104_38;
            case 296:
                return MP5RM_CD;
            case 297:
                return MP106_38;
            case 298:
                return MP104_48;
            case 299:
                return MP108_48;
            case 300:
                return MIR01;
            case 301:
                return MIR01;
            case 302:
                return MIR01F_20;
            case 303:
                return MIR01;
            case 304:
                return MIR01;
            case 305:
                return MS08M_2C;
            case 306:
                return MIR01F_20;
            case 307:
                return MAS09_2C;
            case 308:
                return MS12_2C;
            case 309:
                return MS08M_2C;
            case 310:
                return MASTH_2C;
            case 311:
                return MASLA_2C;
            case 312:
                return MHS110_3C;
            case 313:
                return MIR01F_20;
            case 314:
                return MS08Mn_2C;
            case 315:
                return MS08M_2C;
            case 316:
                return MS08Mn01_2C;
            case 317:
                return MSPU05_48;
            case 318:
                return MS08M_2C;
            case 319:
                return MIR01F_20;
            case 320:
                return MIR04F_20;
            case 321:
                return MS12_2C;
            case 322:
                return MS08Mn_2C;
            case 323:
                return MSP02_4C;
            case 324:
                return MSPU03_4C;
            case 325:
                return HDL_MRF16_4C;
            case 326:
                return MSPU03_4C;
            case 327:
                return MSP02_4C;
            case 328:
                return MSP07M_4C;
            case 329:
                return MSP08M_4C;
            case 330:
                return MSOUT_4W;
            case 331:
                return MSWLR_4W;
            case 332:
                return MSW01_4C;
            case 333:
                return MSW01_4C;
            case 351:
                return MS04;
            case 352:
                return MS24;
            case 353:
                return MS24;
            case 354:
                return MS04;
            case 355:
                return MSD04_40;
            case 356:
                return MS04;
            case 357:
                return MSD08_40;
            case 358:
                return MS24;
            case 359:
                return MS28;
            case 360:
                return MS38;
            case 400:
                return MAIR01;
            case 423:
                return MR0416_C;
            case 424:
                return MR0416_231;
            case 425:
                return MR0616_B;
            case 426:
                return MR0616_232;
            case 427:
                return MR0820C_232;
            case 428:
                return MR0816_232;
            case 429:
                return MR1220_A;
            case 430:
                return MR1216_233;
            case 431:
                return MR1205_A;
            case 432:
                return MR2420_A;
            case 433:
                return MR0416B;
            case 434:
                return MR0420_A;
            case 435:
                return MR0410_231;
            case 436:
                return MR0810_232;
            case 437:
                return MR0425_231;
            case 438:
                return MR0410_331;
            case 439:
                return MR0810_332;
            case 440:
                return MR1210_333;
            case 441:
                return MR0410_331;
            case 442:
                return MR0810_332;
            case 443:
                return MR1210_333;
            case 444:
                return MR0416_431;
            case 445:
                return MR0816_432;
            case 446:
                return MR1216_433;
            case 447:
                return MR0410_431;
            case 448:
                return MR0810_432;
            case 449:
                return MR1210_433;
            case 450:
                return MR1616_434;
            case 451:
                return MR1610_433;
            case 452:
                return MR1210_433;
            case 453:
                return MR1610_433;
            case 454:
                return MRDA0610_432;
            case 500:
                return MTS7000;
            case 596:
                return MH002R16;
            case 597:
                return HMIX13;
            case 598:
                return MD08DSI_232;
            case 599:
                return MH12;
            case 600:
                return MD0602_232;
            case 601:
                return MD0403_232;
            case 602:
                return MD0206_232;
            case 603:
                return MH10;
            case 604:
                return MP4RML_A;
            case 605:
                return MP4RML_A;
            case 606:
                return MDT0203_233;
            case 607:
                return MDT0402_233;
            case 608:
                return MDT0601_233;
            case 609:
                return MDT0106_233;
            case 610:
                return MD0602_232;
            case 611:
                return MD0403_232;
            case 612:
                return MD0206_232;
            case 613:
                return MDLED0605_432;
            case 614:
                return MD0206_432;
            case 615:
                return MD0403_432;
            case 616:
                return MD0405_433;
            case 617:
                return MD0602_432;
            case 618:
                return MDT0106_433;
            case 619:
                return MDT0203_433;
            case 620:
                return MDT04015_433;
            case 621:
                return MDT0601_433;
            case 622:
                return MDLED0605_432;
            case 623:
                return MDLED0401_432;
            case 624:
                return MHD02R18;
            case 700:
                return MW02;
            case 701:
                return MW02;
            case 702:
                return MW02;
            case 703:
                return MW02;
            case 704:
                return MW02_231;
            case 705:
                return MW02_231;
            case 706:
                return MW02_231;
            case 707:
                return MW02_231;
            case 708:
                return MWM70B_12;
            case 709:
                return MVSM45B_12;
            case 710:
                return MWM70B_12;
            case 727:
                return MAC01_331;
            case 728:
                return MAC01_331;
            case 729:
                return MIR04T_40;
            case 730:
                return MAC01;
            case 731:
                return MAC01_331;
            case 732:
                return MAC01_331;
            case 733:
                return MAC01_331;
            case 734:
                return MAC01_331;
            case 735:
                return MAC01_331;
            case 736:
                return MAC01_331;
            case 737:
                return MAC01_331;
            case 738:
                return MAC01_331;
            case 739:
                return MAC01_331;
            case 740:
                return MCIP_RF_10;
            case 741:
                return MCIP_RF_10;
            case 742:
                return MCIP02_RF_10;
            case 800:
                return MDMXI08;
            case 850:
                return MC96IPDMX;
            case 851:
                return MC96IPDMX;
            case 852:
                return MC96IPDMX;
            case 853:
                return MC48IPDMX;
            case 854:
                return MC48IPDMX_231;
            case 894:
                return MPM03IP_432;
            case 895:
                return MPM1P03_231;
            case 896:
                return MPM3P01_231;
            case 897:
                return MPM3P01_231;
            case 898:
                return MPM1P03_231;
            case 899:
                return MPM01;
            case 900:
                return MPM01;
            case 901:
                return Mzbox;
            case 902:
                return Mzbox;
            case 903:
                return Mzbox;
            case 904:
                return Mzbox;
            case 905:
                return Mzbox;
            case 906:
                return MzBox_20;
            case 907:
                return Mzbox;
            case 908:
                return MMC_01;
            case 909:
                return Mzbox;
            case 910:
                return MzBox_20;
            case 911:
                return MZDN_432;
            case 912:
                return Mzbox;
            case 913:
                return MzBox_20;
            case 914:
                return MFTCL_10;
            case 915:
                return MKTVCL_10;
            case 950:
                return MHAI;
            case 951:
                return MCM;
            case 952:
                return MDK;
            case 960:
                return MRS232_Curtain;
            case 1000:
                return MEIB_231;
            case 1001:
                return MCEIB_231;
            case 1005:
                return MBUS_SAMSUNG;
            case 1006:
                return C_BUS;
            case 1007:
                return MRS232_231;
            case 1008:
                return MRS232_231;
            case 1009:
                return MRS232_231;
            case 1010:
                return MIR01L;
            case 1011:
                return MEIB;
            case 1012:
                return MEIB;
            case 1013:
                return MRS232IP_231;
            case 1014:
                return MRS232IP_231;
            case 1015:
                return MDR512;
            case 1016:
                return MRS232_231;
            case 1017:
                return MRS232_MC;
            case 1018:
                return MRS232_231;
            case 1019:
                return MRS232_231;
            case 1020:
                return MRS232_AC;
            case 1021:
                return MHC_DA;
            case 1022:
                return MJACUZZI;
            case 1023:
                return Bacnet;
            case 1024:
                return MRS232_231;
            case 1025:
                return MR232_M1;
            case 1026:
                return MR232_RCU;
            case 1027:
                return MRS232IP_231;
            case 1028:
                return MRS232_231;
            case 1029:
                return MRS232_AC;
            case 1030:
                return MRS232_MC;
            case 1031:
                return MRS232_MC;
            case 1032:
                return MRS232_AC;
            case 1033:
                return MRS232_231;
            case 1034:
                return MRS232_AC;
            case 1035:
                return MRS232_AC;
            case 1036:
                return MRS232_AC;
            case 1037:
                return MRS232_AC;
            case 1038:
                return MOD_DTU;
            case 1047:
                return MV_ZIGBEE;
            case 1048:
                return MUPS;
            case 1049:
                return MUPS_231;
            case 1050:
                return MEIB;
            case 1051:
                return MEIB;
            case 1052:
                return MHIOU_332;
            case 1053:
                return MHIOU_432;
            case 1100:
                return ML01;
            case 1101:
                return ML01;
            case 1102:
                return ML01;
            case 1103:
                return ML01;
            case 1104:
                return MCLog_231;
            case 1105:
                return ML01;
            case 1106:
                return ML01;
            case 1107:
                return ML01;
            case 1108:
                return ML01;
            case 1109:
                return ML01;
            case 1110:
                return ML01;
            case 1150:
                return MDCAC;
            case 1500:
                return MSPU05_RF_1C;
            case 1501:
                return MSPU04_48;
            case 2000:
                return MWH6;
            case 2001:
                return MP2B_48;
            case 2002:
                return MP4B_48;
            case 2003:
                return MP6B_48;
            case 2004:
                return MP8B_48;
            case 2005:
                return MP2K_48;
            case 2006:
                return MP4K_48;
            case 2007:
                return MP6K_48;
            case 2008:
                return MP8K_48;
            case 2009:
                return MWH6;
            case 2010:
                return MP2B_46;
            case 2011:
                return MP4B_46;
            case 2012:
                return MP6B_46;
            case 2013:
                return MP8B_46;
            case 2014:
                return MP1D_48;
            case 2015:
                return MP2D_48;
            case 2016:
                return MP3D_48;
            case 2017:
                return MP4BD_48;
            case 2018:
                return MP5D_48;
            case 2019:
                return MP01R_48;
            case 2020:
                return MP02R_48;
            case 2021:
                return MP04R_48;
            case 2022:
                return MPT2_46;
            case 2023:
                return MPT4_46;
            case 2024:
                return MPT6_46;
            case 2025:
                return MP4A_48;
            case 2026:
                return MP8A_48;
            case 2027:
                return MP5R_48;
            case 2028:
                return MP5M_48;
            case 2029:
                return MPT1_48;
            case 2030:
                return MPT2_48;
            case 2031:
                return MPT3_48;
            case 2032:
                return MPT4_48;
            case 2033:
                return MP1C_48;
            case 2034:
                return MP2C_48;
            case 2035:
                return MP3C_48;
            case 2036:
                return MP4BC_48;
            case 2037:
                return MP5C_48;
            case 2038:
                return MP6C_48;
            case 2039:
                return MP13C_48;
            case 2040:
                return MP01R_48;
            case 2041:
                return MP02R_48;
            case 2042:
                return MP03R_48;
            case 2043:
                return MP04R_48;
            case 2044:
                return MP2B_48;
            case 2045:
                return MP4B_48;
            case 2046:
                return MP6B_48;
            case 2047:
                return MP8B_48;
            case 2048:
                return MP2B_48;
            case 2049:
                return MP4B_48;
            case 2050:
                return MP6B_48;
            case 2051:
                return MP8B_48;
            case 2053:
                return MP6D_48;
            case 2054:
                return MP13D_48;
            case 2055:
                return MP201_3825;
            case 2056:
                return MP202_3825;
            case 2057:
                return MP204_3825;
            case 2058:
                return MPL4_28;
            case 2059:
                return MP104_48;
            case 2060:
                return MP108_48;
            case 2061:
                return MPT6_46;
            case 2062:
                return MPT1_58;
            case 2063:
                return MPT2_58;
            case 2064:
                return MPT3_58;
            case 2065:
                return MPT4_58;
            case 2067:
                return MPT4_46;
            case 2068:
                return MPT2_46;
            case 2069:
                return MPT6_D;
            case 2070:
                return MPT2_D;
            case 3047:
                return MSM;
            case 3048:
                return MSM;
            case 3049:
                return MSM;
            case 3050:
                return MP101B_28013;
            case 3051:
                return MP102B_28013;
            case 3052:
                return MP103B_28013;
            case 3053:
                return MP104B_28013;
            case 3054:
                return MHB_1010;
            case 3055:
                return MSM;
            case 3056:
                return MHBRF_1010;
            case 3057:
                return MHIC_1825;
            case 3058:
                return MHIC_1811;
            case 3059:
                return MHB2_2825;
            case 3060:
                return MHIC_2825_Ⅱ;
            case 3061:
                return MHBRF_1010;
            case 3062:
                return MHIC_1811;
            case 3063:
                return MHIC_3811;
            case 3064:
                return MHIC_2825;
            case 3065:
                return MHIC_2811;
            case 3066:
                return MHIC_3825;
            case 3067:
                return MHB_1010;
            case 3068:
                return MPCH01_48;
            case 3069:
                return MHRF_28;
            case 3070:
                return MHBRF_1010;
            case 3071:
                return MHB2_2825;
            case 3072:
                return MPTB01RF_48;
            case 3073:
                return MHBRF_1010;
            case 3074:
                return MHBRF_1010;
            case 3075:
                return MHB2_3825;
            case 3076:
                return MHIC_3825;
            case 3079:
                return MHIC_48;
            case 3200:
                return MDS;
            case 3500:
                return MHRCU_433;
            case 3501:
                return MHRCU_433;
            case 3502:
                return MHRCU_433;
            case 4000:
                return MGSM_431;
            case 4500:
                return MBR06_431;
            case 5000:
                return MPL8_RF_18;
            case 5001:
                return MPL8_RF_18;
            case 5002:
                return MPL8_RF_18;
            case 5003:
                return MPL4FH_RF_18;
            case 5030:
                return MP2B_RF_18;
            case 5031:
                return MP4B_RF_18;
            case 5032:
                return MP6B_RF_18;
            case 5033:
                return MP8B_RF_18;
            case 5034:
                return MP01R_RF_18;
            case 5035:
                return MP02R_RF_18;
            case 5036:
                return MP03R_RF_18;
            case 5037:
                return MP04R_RF_18;
            case 5038:
                return MPT1_RF_18;
            case 5039:
                return MPT2_RF_18;
            case 5040:
                return MPT3_RF_18;
            case 5041:
                return MPT4_RF_18;
            case 5042:
                return MP2B_RF_18;
            case 5043:
                return MP4B_RF_18;
            case 5044:
                return MP6B_RF_18;
            case 5045:
                return MP8B_RF_18;
            case 5046:
                return MP01R_RF_18;
            case 5047:
                return MP02R_RF_18;
            case 5048:
                return MP03R_RF_18;
            case 5049:
                return MP04R_RF_18;
            case 5050:
                return MPT1_RF_18;
            case 5051:
                return MPT2_RF_18;
            case 5052:
                return MPT3_RF_18;
            case 5053:
                return MPT4_RF_18;
            case 5054:
                return MP04R_RF_18;
            case 5055:
                return MPT4_RF_18;
            case 5056:
                return MPT6_RF_18;
            case 5057:
                return MPT4_RF_28;
            case 5058:
                return MPT2_RF_28;
            case 5059:
                return MPT4_RF_28;
            case 5300:
                return MWM70_RF_18;
            case 5301:
                return MVSM45B__RF_12;
            case 5302:
                return MPC01_RF_18;
            case 5500:
                return MPR01_RF_18;
            case 5501:
                return MPR02_RF_18;
            case 5700:
                return MPD01_RF_18;
            case 5900:
                return MPS04_RF_18;
            case 6100:
                return MR_PLUG_RF_1P;
            case 6101:
                return MPS04_RF_18;
            case 9000:
                return Camera;
            case 60000:
                return Server;
            case 65498:
                return Blooder;
            case 65499:
                return Blooder;
            case 65500:
                return Modbus;
            case 65501:
                return DiiVA;
            case 65532:
                return TouchLife;
            case 65534:
                return PC;
            default:
                return Invalid;
        }
    }

    @Override
    public String toString() {
        switch (value) {
            case 1:
                return "MD0610";
            case 2:
                return "MD1210A";
            case 3:
                return "MD2405A";
            case 4:
                return "MD0620";
            case 5:
                return "MD1210";
            case 6:
                return "MD2405";
            case 7:
                return "MDH1210";
            case 8:
                return "MD0620A";
            case 9:
                return "MDH0610";
            case 10:
                return "MDG512_DMX";
            case 11:
                return "MR1220";
            case 12:
                return "MR2420";
            case 13:
                return "MC48IP";
            case 14:
                return "MC48IP";
            case 15:
                return "MD0602";
            case 16:
                return "MC48IPDMX";
            case 17:
                return "MRDA06";
            case 18:
                return "MC48IP_DMX_231";
            case 19:
                return "MR1210";
            case 20:
                return "MD240_DMX";
            case 21:
                return "MD512_DMX";
            case 22:
                return "MR1205";
            case 23:
                return "MR0810_232";
            case 24:
                return "MD512_DMX";
            case 25:
                return "MD0403";
            case 26:
                return "MDH2405";
            case 27:
                return "MDH0620";
            case 28:
                return "MD0304";
            case 29:
                return "BN_VRV";
            case 30:
                return "MC48IP";
            case 31:
                return "MC48IP_231";
            case 32:
                return "MC48IPDMX_231";
            case 33:
                return "MGPRS_232";
            case 34:
                return "MHC48IP_231";
            case 35:
                return "MRDA04";
            case 36:
                return "MRDA0610_432";
            case 37:
                return "MRDA02";
            case 38:
                return "MGSM_431";
            case 39:
                return "MC48IPDMX_231";
            case 40:
                return "MC48_DALI";
            case 41:
                return "MC48_DALI";
            case 42:
                return "MC64_DALI";
            case 43:
                return "MC64_DALI";
            case 44:
                return "MD0610_4D";
            case 45:
                return "MD1210_4D";
            case 46:
                return "MRDA06";
            case 47:
                return "MC48IP_DMX_231";
            case 48:
                return "MPL4_38_FH";
            case 49:
                return "MPL4_28";
            case 50:
                return "MP8RM";
            case 51:
                return "MP8M";
            case 52:
                return "MP4RM";
            case 53:
                return "MP4M";
            case 54:
                return "MP6R";
            case 55:
                return "MP6";
            case 56:
                return "MP2R";
            case 57:
                return "MP2";
            case 58:
                return "MP2RM_AS";
            case 59:
                return "MWH6";
            case 60:
                return "MP8RM";
            case 61:
                return "MP4RM";
            case 62:
                return "MWH6";
            case 63:
                return "MP4RM";
            case 64:
                return "MP6RM_A";
            case 65:
                return "MP6RM_AD";
            case 66:
                return "MP4RM_A";
            case 67:
                return "MP4RM_AD";
            case 68:
                return "MP2RM_A";
            case 69:
                return "MP2RM_AD";
            case 70:
                return "MSR02_20";
            case 71:
                return "MP3RM_A";
            case 72:
                return "MP3RM_AD";
            case 73:
                return "MP1RM_A";
            case 74:
                return "MP4RM_L";
            case 75:
                return "MP4RML_A";
            case 76:
                return "MP6RM_BD";
            case 77:
                return "MP4RML_A";
            case 78:
                return "MLAC";
            case 79:
                return "MPL4_28";
            case 80:
                return "MS01R";
            case 81:
                return "MS01L";
            case 82:
                return "MPL4_28";
            case 83:
                return "MLAC_28";
            case 84:
                return "MPL4_28";
            case 85:
                return "MLAC_28";
            case 86:
                return "MPL4_38";
            case 87:
                return "MPL4_38";
            case 88:
                return "MPL4_28";
            case 89:
                return "MWH6";
            case 90:
                return "MS01R";
            case 91:
                return "MS01L";
            case 92:
                return "MS12";
            case 93:
                return "MS04";
            case 94:
                return "MS06_232";
            case 95:
                return "MHS110_2C";
            case 96:
                return "MPE_2C";
            case 97:
                return "MHS02";
            case 98:
                return "MHS02";
            case 99:
                return "MHS110_2C";
            case 100:
                return "MT12IP";
            case 101:
                return "MT12IP";
            case 102:
                return "MT01";
            case 103:
                return "MT12IP";
            case 104:
                return "MT02IP";
            case 105:
                return "MT02IP";
            case 106:
                return "MAC01_331";
            case 107:
                return "MAC01_331";
            case 108:
                return "MT12";
            case 109:
                return "MAC01";
            case 110:
                return "MT12IP";
            case 111:
                return "MAC01";
            case 112:
                return "MAC01";
            case 113:
                return "MS32";
            case 114:
                return "MS04";
            case 115:
                return "MS04";
            case 116:
                return "MS06_232";
            case 117:
                return "MAC01";
            case 118:
                return "MS04";
            case 119:
                return "MS04";
            case 120:
                return "MHS110_2C";
            case 121:
                return "MS04";
            case 122:
                return "MHS110_2C";
            case 123:
                return "MHS110_2C";
            case 124:
                return "MTS_2C";
            case 125:
                return "MHS110_2C";
            case 126:
                return "MHS110_2C";
            case 127:
                return "MHS110W_2W";
            case 128:
                return "MRS232_231";
            case 129:
                return "MHS110W_2W";
            case 130:
                return "MHS110W_2W";
            case 131:
                return "MHS110W_2W";
            case 132:
                return "MHS110W_2W";
            case 133:
                return "MHS110_2C";
            case 134:
                return "MTS04_20";
            case 135:
                return "MS04";
            case 136:
                return "MS04";
            case 137:
                return "MS04";
            case 138:
                return "MHS110W_2W";
            case 139:
                return "MHS110_2C";
            case 140:
                return "MS12";
            case 141:
                return "MS24";
            case 142:
                return "MS24";
            case 149:
                return "MPL4_38_FH";
            case 150:
                return "MR1220A";
            case 151:
                return "MR2420A";
            case 152:
                return "MR0610";
            case 153:
                return "MR0416A";
            case 154:
                return "MPL4_38_FH";
            case 155:
                return "MPL4_38_FH";
            case 156:
                return "MPL4_38_FH";
            case 157:
                return "MPL4_38_FH";
            case 158:
                return "MPL8_48_FH";
            case 159:
                return "MPL8_38";
            case 160:
                return "MPL4_38_FH";
            case 161:
                return "MPAC01_48";
            case 162:
                return "MPL8_48_FH";
            case 163:
                return "MRSPA05_231";
            case 164:
                return "MPDL_48";
            case 165:
                return "MPTL14_46";
            case 166:
                return "MLAC_28";
            case 167:
                return "MPL8_48_FH";
            case 168:
                return "MPTL14_46";
            case 169:
                return "MLAC_28";
            case 170:
                return "MPTL14_46";
            case 171:
                return "MPTLC43_46";
            case 172:
                return "MPTL14_46";
            case 173:
                return "MPTLC43_46";
            case 174:
                return "MPTLC43_46";
            case 175:
                return "MPTL14_46";
            case 180:
                return "MPL8_48_FH";
            case 200:
                return "MSR06_231";
            case 201:
                return "LED";
            case 209:
                return "MFH06_332";
            case 210:
                return "MFH06_332";
            case 211:
                return "MFH06_432";
            case 216:
                return "MP201_38";
            case 217:
                return "MP202_38";
            case 218:
                return "MP203_38";
            case 219:
                return "MP204_38";
            case 220:
                return "MP2RM_48";
            case 221:
                return "MP3RM_48";
            case 222:
                return "MP4RM_48";
            case 223:
                return "MPT01_48";
            case 224:
                return "MPT02_48";
            case 225:
                return "MPT03_48";
            case 226:
                return "MPT04_48";
            case 227:
                return "MP103A_38";
            case 229:
                return "MBUS01IP_431";
            case 230:
                return "HBLS_GW";
            case 231:
                return "MBUS01IP_231";
            case 232:
                return "MBUS01IP_431";
            case 233:
                return "MBUS01IP_231";
            case 234:
                return "MBUS01IP_431";
            case 235:
                return "MBUS01IP_231";
            case 236:
                return "MBUS01IP_231";
            case 237:
                return "MBUS04IP";
            case 238:
                return "MBUS08IP";
            case 239:
                return "MPB03_48";
            case 240:
                return "MP202_2825";
            case 241:
                return "MPB01_48";
            case 242:
                return "MPH101_48";
            case 243:
                return "MPH102_48";
            case 244:
                return "MPH104_48";
            case 245:
                return "MP1RM_AA";
            case 246:
                return "MP2RM_AA";
            case 247:
                return "MP2RM_AA";
            case 248:
                return "MP4RM_AA";
            case 249:
                return "MP8RMA";
            case 250:
                return "MP6RM_B";
            case 251:
                return "MP6RM_BD";
            case 252:
                return "MP4RM_B";
            case 253:
                return "MP4RM_BD";
            case 254:
                return "MP2RM_B";
            case 255:
                return "MP2RM_BD";
            case 256:
                return "MP3RM_B";
            case 257:
                return "MP3RM_BD";
            case 258:
                return "MP1RM_B";
            case 259:
                return "MP2RM_BS";
            case 260:
                return "MP6RM_AA";
            case 261:
                return "MP101A_38";
            case 262:
                return "MP102A_38";
            case 263:
                return "MP102_38";
            case 264:
                return "MP103A_38";
            case 265:
                return "MP104_38";
            case 266:
                return "MP5RM_CD";
            case 267:
                return "MP106_38";
            case 268:
                return "MP5RM_AAD";
            case 269:
                return "MP3RM_AA";
            case 270:
                return "MP201_2825";
            case 271:
                return "MP202_2825";
            case 272:
                return "MP203_2825";
            case 273:
                return "MP204_2825";
            case 274:
                return "FM_2825";
            case 275:
                return "MP101A_38";
            case 276:
                return "MP102A_38";
            case 277:
                return "MP102_38";
            case 278:
                return "MP103A_38";
            case 279:
                return "MP104_38";
            case 280:
                return "MP5RM_CD";
            case 281:
                return "MP106_38";
            case 282:
                return "MP204_2825";
            case 283:
                return "MP206_2825";
            case 284:
                return "MP212_2825";
            case 285:
                return "MPN50";
            case 286:
                return "MS104";
            case 287:
                return "MP203_2825";
            case 288:
                return "MP101A_38";
            case 289:
                return "MP104_48";
            case 290:
                return "MP108_48";
            case 291:
                return "MPN90";
            case 292:
                return "MP102A_38";
            case 293:
                return "MP102_38";
            case 294:
                return "MP103A_38";
            case 295:
                return "MP104_38";
            case 296:
                return "MP5RM_CD";
            case 297:
                return "MP106_38";
            case 298:
                return "MP104_48";
            case 299:
                return "MP108_48";
            case 300:
                return "MIR01";
            case 301:
                return "MIR01";
            case 302:
                return "MIR01F_20";
            case 303:
                return "MIR01";
            case 304:
                return "MIR01";
            case 305:
                return "MS08M_2C";
            case 306:
                return "MIR01F_20";
            case 307:
                return "MAS09_2C";
            case 308:
                return "MS12_2C";
            case 309:
                return "MS08M_2C";
            case 310:
                return "MASTH_2C";
            case 311:
                return "MASLA_2C";
            case 312:
                return "MHS110_3C";
            case 313:
                return "MIR01F_20";
            case 314:
                return "MS08Mn_2C";
            case 315:
                return "MS08M_2C";
            case 316:
                return "MS08Mn01_2C";
            case 317:
                return "MSPU05_48";
            case 318:
                return "MS08M_2C";
            case 319:
                return "MIR01F_20";
            case 320:
                return "MIR04F_20";
            case 321:
                return "MS12_2C";
            case 322:
                return "MS08Mn_2C";
            case 323:
                return "MSP02_4C";
            case 324:
                return "MSPU03_4C";
            case 325:
                return "HDL_MRF16_4C";
            case 326:
                return "MSPU03_4C";
            case 327:
                return "MSP02_4C";
            case 328:
                return "MSP07M_4C";
            case 329:
                return "MSP08M_4C";
            case 330:
                return "MSOUT_4W";
            case 331:
                return "MSWLR_4W";
            case 332:
                return "MSW01_4C";
            case 333:
                return "MSW01_4C";
            case 351:
                return "MS04";
            case 352:
                return "MS24";
            case 353:
                return "MS24";
            case 354:
                return "MS04";
            case 355:
                return "MSD04_40";
            case 356:
                return "MS04";
            case 357:
                return "MSD08_40";
            case 358:
                return "MS24";
            case 359:
                return "MS28";
            case 360:
                return "MS38";
            case 400:
                return "MAIR01";
            case 423:
                return "MR0416_C";
            case 424:
                return "MR0416_231";
            case 425:
                return "MR0616_B";
            case 426:
                return "MR0616_232";
            case 427:
                return "MR0820C_232";
            case 428:
                return "MR0816_232";
            case 429:
                return "MR1220_A";
            case 430:
                return "MR1216_233";
            case 431:
                return "MR1205_A";
            case 432:
                return "MR2420_A";
            case 433:
                return "MR0416B";
            case 434:
                return "MR0420_A";
            case 435:
                return "MR0410_231";
            case 436:
                return "MR0810_232";
            case 437:
                return "MR0425_231";
            case 438:
                return "MR0410_331";
            case 439:
                return "MR0810_332";
            case 440:
                return "MR1210_333";
            case 441:
                return "MR0410_331";
            case 442:
                return "MR0810_332";
            case 443:
                return "MR1210_333";
            case 444:
                return "MR0416_431";
            case 445:
                return "MR0816_432";
            case 446:
                return "MR1216_433";
            case 447:
                return "MR0410_431";
            case 448:
                return "MR0810_432";
            case 449:
                return "MR1210_433";
            case 450:
                return "MR1616_434";
            case 451:
                return "MR1610_433";
            case 452:
                return "MR1210_433";
            case 453:
                return "MR1610_433";
            case 454:
                return "MRDA0610_432";
            case 500:
                return "MTS7000";
            case 596:
                return "MH002R16";
            case 597:
                return "HMIX13";
            case 598:
                return "MD08DSI_232";
            case 599:
                return "MH12";
            case 600:
                return "MD0602_232";
            case 601:
                return "MD0403_232";
            case 602:
                return "MD0206_232";
            case 603:
                return "MH10";
            case 604:
                return "MP4RML_A";
            case 605:
                return "MP4RML_A";
            case 606:
                return "MDT0203_233";
            case 607:
                return "MDT0402_233";
            case 608:
                return "MDT0601_233";
            case 609:
                return "MDT0106_233";
            case 610:
                return "MD0602_232";
            case 611:
                return "MD0403_232";
            case 612:
                return "MD0206_232";
            case 613:
                return "MDLED0605_432";
            case 614:
                return "MD0206_432";
            case 615:
                return "MD0403_432";
            case 616:
                return "MD0405_433";
            case 617:
                return "MD0602_432";
            case 618:
                return "MDT0106_433";
            case 619:
                return "MDT0203_433";
            case 620:
                return "MDT04015_433";
            case 621:
                return "MDT0601_433";
            case 622:
                return "MDLED0605_432";
            case 623:
                return "MDLED0401_432";
            case 624:
                return "MHD02R18";
            case 700:
                return "MW02";
            case 701:
                return "MW02";
            case 702:
                return "MW02";
            case 703:
                return "MW02";
            case 704:
                return "MW02_231";
            case 705:
                return "MW02_231";
            case 706:
                return "MW02_231";
            case 707:
                return "MW02_231";
            case 708:
                return "MWM70B_12";
            case 709:
                return "MVSM45B_12";
            case 710:
                return "MWM70B_12";
            case 727:
                return "MAC01_331";
            case 728:
                return "MAC01_331";
            case 729:
                return "MIR04T_40";
            case 730:
                return "MAC01";
            case 731:
                return "MAC01_331";
            case 732:
                return "MAC01_331";
            case 733:
                return "MAC01_331";
            case 734:
                return "MAC01_331";
            case 735:
                return "MAC01_331";
            case 736:
                return "MAC01_331";
            case 737:
                return "MAC01_331";
            case 738:
                return "MAC01_331";
            case 739:
                return "MAC01_331";
            case 740:
                return "MCIP_RF_10";
            case 741:
                return "MCIP_RF_10";
            case 742:
                return "MCIP02_RF_10";
            case 800:
                return "MDMXI08";
            case 850:
                return "MC96IPDMX";
            case 851:
                return "MC96IPDMX";
            case 852:
                return "MC96IPDMX";
            case 853:
                return "MC48IPDMX";
            case 854:
                return "MC48IPDMX_231";
            case 894:
                return "MPM03IP_432";
            case 895:
                return "MPM1P03_231";
            case 896:
                return "MPM3P01_231";
            case 897:
                return "MPM3P01_231";
            case 898:
                return "MPM1P03_231";
            case 899:
                return "MPM01";
            case 900:
                return "MPM01";
            case 901:
                return "Mzbox";
            case 902:
                return "Mzbox";
            case 903:
                return "Mzbox";
            case 904:
                return "Mzbox";
            case 905:
                return "Mzbox";
            case 906:
                return "MzBox_20";
            case 907:
                return "Mzbox";
            case 908:
                return "MMC_01";
            case 909:
                return "Mzbox";
            case 910:
                return "MzBox_20";
            case 911:
                return "MZDN_432";
            case 912:
                return "Mzbox";
            case 913:
                return "MzBox_20";
            case 914:
                return "MFTCL_10";
            case 915:
                return "MKTVCL_10";
            case 950:
                return "MHAI";
            case 951:
                return "MCM";
            case 952:
                return "MDK";
            case 960:
                return "MRS232_Curtain";
            case 1000:
                return "MEIB_231";
            case 1001:
                return "MCEIB_231";
            case 1005:
                return "MBUS_SAMSUNG";
            case 1006:
                return "C_BUS";
            case 1007:
                return "MRS232_231";
            case 1008:
                return "MRS232_231";
            case 1009:
                return "MRS232_231";
            case 1010:
                return "MIR01L";
            case 1011:
                return "MEIB";
            case 1012:
                return "MEIB";
            case 1013:
                return "MRS232IP_231";
            case 1014:
                return "MRS232IP_231";
            case 1015:
                return "MDR512";
            case 1016:
                return "MRS232_231";
            case 1017:
                return "MRS232_MC";
            case 1018:
                return "MRS232_231";
            case 1019:
                return "MRS232_231";
            case 1020:
                return "MRS232_AC";
            case 1021:
                return "MHC_DA";
            case 1022:
                return "MJACUZZI";
            case 1023:
                return "Bacnet";
            case 1024:
                return "MRS232_231";
            case 1025:
                return "MR232_M1";
            case 1026:
                return "MR232_RCU";
            case 1027:
                return "MRS232IP_231";
            case 1028:
                return "MRS232_231";
            case 1029:
                return "MRS232_AC";
            case 1030:
                return "MRS232_MC";
            case 1031:
                return "MRS232_MC";
            case 1032:
                return "MRS232_AC";
            case 1033:
                return "MRS232_231";
            case 1034:
                return "MRS232_AC";
            case 1035:
                return "MRS232_AC";
            case 1036:
                return "MRS232_AC";
            case 1037:
                return "MRS232_AC";
            case 1038:
                return "MOD_DTU";
            case 1047:
                return "MV_ZIGBEE";
            case 1048:
                return "MUPS";
            case 1049:
                return "MUPS_231";
            case 1050:
                return "MEIB";
            case 1051:
                return "MEIB";
            case 1052:
                return "MHIOU_332";
            case 1053:
                return "MHIOU_432";
            case 1100:
                return "ML01";
            case 1101:
                return "ML01";
            case 1102:
                return "ML01";
            case 1103:
                return "ML01";
            case 1104:
                return "MCLog_231";
            case 1105:
                return "ML01";
            case 1106:
                return "ML01";
            case 1107:
                return "ML01";
            case 1108:
                return "ML01";
            case 1109:
                return "ML01";
            case 1110:
                return "ML01";
            case 1150:
                return "MDCAC";
            case 1500:
                return "MSPU05_RF_1C";
            case 1501:
                return "MSPU04_48";
            case 2000:
                return "MWH6";
            case 2001:
                return "MP2B_48";
            case 2002:
                return "MP4B_48";
            case 2003:
                return "MP6B_48";
            case 2004:
                return "MP8B_48";
            case 2005:
                return "MP2K_48";
            case 2006:
                return "MP4K_48";
            case 2007:
                return "MP6K_48";
            case 2008:
                return "MP8K_48";
            case 2009:
                return "MWH6";
            case 2010:
                return "MP2B_46";
            case 2011:
                return "MP4B_46";
            case 2012:
                return "MP6B_46";
            case 2013:
                return "MP8B_46";
            case 2014:
                return "MP1D_48";
            case 2015:
                return "MP2D_48";
            case 2016:
                return "MP3D_48";
            case 2017:
                return "MP4BD_48";
            case 2018:
                return "MP5D_48";
            case 2019:
                return "MP01R_48";
            case 2020:
                return "MP02R_48";
            case 2021:
                return "MP04R_48";
            case 2022:
                return "MPT2_46";
            case 2023:
                return "MPT4_46";
            case 2024:
                return "MPT6_46";
            case 2025:
                return "MP4A_48";
            case 2026:
                return "MP8A_48";
            case 2027:
                return "MP5R_48";
            case 2028:
                return "MP5M_48";
            case 2029:
                return "MPT1_48";
            case 2030:
                return "MPT2_48";
            case 2031:
                return "MPT3_48";
            case 2032:
                return "MPT4_48";
            case 2033:
                return "MP1C_48";
            case 2034:
                return "MP2C_48";
            case 2035:
                return "MP3C_48";
            case 2036:
                return "MP4BC_48";
            case 2037:
                return "MP5C_48";
            case 2038:
                return "MP6C_48";
            case 2039:
                return "MP13C_48";
            case 2040:
                return "MP01R_48";
            case 2041:
                return "MP02R_48";
            case 2042:
                return "MP03R_48";
            case 2043:
                return "MP04R_48";
            case 2044:
                return "MP2B_48";
            case 2045:
                return "MP4B_48";
            case 2046:
                return "MP6B_48";
            case 2047:
                return "MP8B_48";
            case 2048:
                return "MP2B_48";
            case 2049:
                return "MP4B_48";
            case 2050:
                return "MP6B_48";
            case 2051:
                return "MP8B_48";
            case 2053:
                return "MP6D_48";
            case 2054:
                return "MP13D_48";
            case 2055:
                return "MP201_3825";
            case 2056:
                return "MP202_3825";
            case 2057:
                return "MP204_3825";
            case 2058:
                return "MPL4_28";
            case 2059:
                return "MP104_48";
            case 2060:
                return "MP108_48";
            case 2061:
                return "MPT6_46";
            case 2062:
                return "MPT1_58";
            case 2063:
                return "MPT2_58";
            case 2064:
                return "MPT3_58";
            case 2065:
                return "MPT4_58";
            case 2067:
                return "MPT4_46";
            case 2068:
                return "MPT2_46";
            case 2069:
                return "MPT6_D";
            case 2070:
                return "MPT2_D";
            case 3047:
                return "MSM";
            case 3048:
                return "MSM";
            case 3049:
                return "MSM";
            case 3050:
                return "MP101B_28013";
            case 3051:
                return "MP102B_28013";
            case 3052:
                return "MP103B_28013";
            case 3053:
                return "MP104B_28013";
            case 3054:
                return "MHB_1010";
            case 3055:
                return "MSM";
            case 3056:
                return "MHBRF_1010";
            case 3057:
                return "MHIC_1825";
            case 3058:
                return "MHIC_1811";
            case 3059:
                return "MHB2_2825";
            case 3060:
                return "MHIC_2825_Ⅱ";
            case 3061:
                return "MHBRF_1010";
            case 3062:
                return "MHIC_1811";
            case 3063:
                return "MHIC_3811";
            case 3064:
                return "MHIC_2825";
            case 3065:
                return "MHIC_2811";
            case 3066:
                return "MHIC_3825";
            case 3067:
                return "MHB_1010";
            case 3068:
                return "MPCH01_48";
            case 3069:
                return "MHRF_28";
            case 3070:
                return "MHBRF_1010";
            case 3071:
                return "MHB2_2825";
            case 3072:
                return "MPTB01RF_48";
            case 3073:
                return "MHBRF_1010";
            case 3074:
                return "MHBRF_1010";
            case 3075:
                return "MHB2_3825";
            case 3076:
                return "MHIC_3825";
            case 3079:
                return "MHIC_48";
            case 3200:
                return "MDS";
            case 3500:
                return "MHRCU_433";
            case 3501:
                return "MHRCU_433";
            case 3502:
                return "MHRCU_433";
            case 4000:
                return "MGSM_431";
            case 4500:
                return "MBR06_431";
            case 5000:
                return "MPL8_RF_18";
            case 5001:
                return "MPL8_RF_18";
            case 5002:
                return "MPL8_RF_18";
            case 5003:
                return "MPL4FH_RF_18";
            case 5030:
                return "MP2B_RF_18";
            case 5031:
                return "MP4B_RF_18";
            case 5032:
                return "MP6B_RF_18";
            case 5033:
                return "MP8B_RF_18";
            case 5034:
                return "MP01R_RF_18";
            case 5035:
                return "MP02R_RF_18";
            case 5036:
                return "MP03R_RF_18";
            case 5037:
                return "MP04R_RF_18";
            case 5038:
                return "MPT1_RF_18";
            case 5039:
                return "MPT2_RF_18";
            case 5040:
                return "MPT3_RF_18";
            case 5041:
                return "MPT4_RF_18";
            case 5042:
                return "MP2B_RF_18";
            case 5043:
                return "MP4B_RF_18";
            case 5044:
                return "MP6B_RF_18";
            case 5045:
                return "MP8B_RF_18";
            case 5046:
                return "MP01R_RF_18";
            case 5047:
                return "MP02R_RF_18";
            case 5048:
                return "MP03R_RF_18";
            case 5049:
                return "MP04R_RF_18";
            case 5050:
                return "MPT1_RF_18";
            case 5051:
                return "MPT2_RF_18";
            case 5052:
                return "MPT3_RF_18";
            case 5053:
                return "MPT4_RF_18";
            case 5054:
                return "MP04R_RF_18";
            case 5055:
                return "MPT4_RF_18";
            case 5056:
                return "MPT6_RF_18";
            case 5057:
                return "MPT4_RF_28";
            case 5058:
                return "MPT2_RF_28";
            case 5059:
                return "MPT4_RF_28";
            case 5300:
                return "MWM70_RF_18";
            case 5301:
                return "MVSM45B__RF_12";
            case 5302:
                return "MPC01_RF_18";
            case 5500:
                return "MPR01_RF_18";
            case 5501:
                return "MPR02_RF_18";
            case 5700:
                return "MPD01_RF_18";
            case 5900:
                return "MPS04_RF_18";
            case 6100:
                return "MR_PLUG_RF_1P";
            case 6101:
                return "MPS04_RF_18";
            case 9000:
                return "Camera";
            case 60000:
                return "Server";
            case 65498:
                return "Blooder";
            case 65499:
                return "Blooder";
            case 65500:
                return "Modbus";
            case 65501:
                return "DiiVA";
            case 65532:
                return "TouchLife";
            case 65534:
                return "PC";
            default:
                return "Invalid";
        }
    }

}
