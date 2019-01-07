/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.handler;

import java.util.EnumSet;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.lutron.internal.KeypadComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating with the virtual buttons on the RadioRA2 main repeater
 *
 * @author Bob Adair - Initial contribution
 */
public class VirtualKeypadHandler extends BaseKeypadHandler {

    private static enum Component implements KeypadComponent {
        BUTTON1(1, "button1", "Virtual button"),
        BUTTON2(2, "button2", "Virtual button"),
        BUTTON3(3, "button3", "Virtual button"),
        BUTTON4(4, "button4", "Virtual button"),
        BUTTON5(5, "button5", "Virtual button"),
        BUTTON6(6, "button6", "Virtual button"),
        BUTTON7(7, "button7", "Virtual button"),
        BUTTON8(8, "button8", "Virtual button"),
        BUTTON9(9, "button9", "Virtual button"),
        BUTTON10(10, "button10", "Virtual button"),
        BUTTON11(11, "button11", "Virtual button"),
        BUTTON12(12, "button12", "Virtual button"),
        BUTTON13(13, "button13", "Virtual button"),
        BUTTON14(14, "button14", "Virtual button"),
        BUTTON15(15, "button15", "Virtual button"),
        BUTTON16(16, "button16", "Virtual button"),
        BUTTON17(17, "button17", "Virtual button"),
        BUTTON18(18, "button18", "Virtual button"),
        BUTTON19(19, "button19", "Virtual button"),
        BUTTON20(20, "button20", "Virtual button"),
        BUTTON21(21, "button21", "Virtual button"),
        BUTTON22(22, "button22", "Virtual button"),
        BUTTON23(23, "button23", "Virtual button"),
        BUTTON24(24, "button24", "Virtual button"),
        BUTTON25(25, "button25", "Virtual button"),
        BUTTON26(26, "button26", "Virtual button"),
        BUTTON27(27, "button27", "Virtual button"),
        BUTTON28(28, "button28", "Virtual button"),
        BUTTON29(29, "button29", "Virtual button"),
        BUTTON30(30, "button30", "Virtual button"),
        BUTTON31(31, "button31", "Virtual button"),
        BUTTON32(32, "button32", "Virtual button"),
        BUTTON33(33, "button33", "Virtual button"),
        BUTTON34(34, "button34", "Virtual button"),
        BUTTON35(35, "button35", "Virtual button"),
        BUTTON36(36, "button36", "Virtual button"),
        BUTTON37(37, "button37", "Virtual button"),
        BUTTON38(38, "button38", "Virtual button"),
        BUTTON39(39, "button39", "Virtual button"),
        BUTTON40(40, "button40", "Virtual button"),
        BUTTON41(41, "button41", "Virtual button"),
        BUTTON42(42, "button42", "Virtual button"),
        BUTTON43(43, "button43", "Virtual button"),
        BUTTON44(44, "button44", "Virtual button"),
        BUTTON45(45, "button45", "Virtual button"),
        BUTTON46(46, "button46", "Virtual button"),
        BUTTON47(47, "button47", "Virtual button"),
        BUTTON48(48, "button48", "Virtual button"),
        BUTTON49(49, "button49", "Virtual button"),
        BUTTON50(50, "button50", "Virtual button"),
        BUTTON51(51, "button51", "Virtual button"),
        BUTTON52(52, "button52", "Virtual button"),
        BUTTON53(53, "button53", "Virtual button"),
        BUTTON54(54, "button54", "Virtual button"),
        BUTTON55(55, "button55", "Virtual button"),
        BUTTON56(56, "button56", "Virtual button"),
        BUTTON57(57, "button57", "Virtual button"),
        BUTTON58(58, "button58", "Virtual button"),
        BUTTON59(59, "button59", "Virtual button"),
        BUTTON60(60, "button60", "Virtual button"),
        BUTTON61(61, "button61", "Virtual button"),
        BUTTON62(62, "button62", "Virtual button"),
        BUTTON63(63, "button63", "Virtual button"),
        BUTTON64(64, "button64", "Virtual button"),
        BUTTON65(65, "button65", "Virtual button"),
        BUTTON66(66, "button66", "Virtual button"),
        BUTTON67(67, "button67", "Virtual button"),
        BUTTON68(68, "button68", "Virtual button"),
        BUTTON69(69, "button69", "Virtual button"),
        BUTTON70(70, "button70", "Virtual button"),
        BUTTON71(71, "button71", "Virtual button"),
        BUTTON72(72, "button72", "Virtual button"),
        BUTTON73(73, "button73", "Virtual button"),
        BUTTON74(74, "button74", "Virtual button"),
        BUTTON75(75, "button75", "Virtual button"),
        BUTTON76(76, "button76", "Virtual button"),
        BUTTON77(77, "button77", "Virtual button"),
        BUTTON78(78, "button78", "Virtual button"),
        BUTTON79(79, "button79", "Virtual button"),
        BUTTON80(80, "button80", "Virtual button"),
        BUTTON81(81, "button81", "Virtual button"),
        BUTTON82(82, "button82", "Virtual button"),
        BUTTON83(83, "button83", "Virtual button"),
        BUTTON84(84, "button84", "Virtual button"),
        BUTTON85(85, "button85", "Virtual button"),
        BUTTON86(86, "button86", "Virtual button"),
        BUTTON87(87, "button87", "Virtual button"),
        BUTTON88(88, "button88", "Virtual button"),
        BUTTON89(89, "button89", "Virtual button"),
        BUTTON90(90, "button90", "Virtual button"),
        BUTTON91(91, "button91", "Virtual button"),
        BUTTON92(92, "button92", "Virtual button"),
        BUTTON93(93, "button93", "Virtual button"),
        BUTTON94(94, "button94", "Virtual button"),
        BUTTON95(95, "button95", "Virtual button"),
        BUTTON96(96, "button96", "Virtual button"),
        BUTTON97(97, "button97", "Virtual button"),
        BUTTON98(98, "button98", "Virtual button"),
        BUTTON99(99, "button99", "Virtual button"),
        BUTTON100(100, "button100", "Virtual button"),

        LED1(101, "led1", "Virtual LED"),
        LED2(102, "led2", "Virtual LED"),
        LED3(103, "led3", "Virtual LED"),
        LED4(104, "led4", "Virtual LED"),
        LED5(105, "led5", "Virtual LED"),
        LED6(106, "led6", "Virtual LED"),
        LED7(107, "led7", "Virtual LED"),
        LED8(108, "led8", "Virtual LED"),
        LED9(109, "led9", "Virtual LED"),
        LED10(110, "led10", "Virtual LED"),
        LED11(111, "led11", "Virtual LED"),
        LED12(112, "led12", "Virtual LED"),
        LED13(113, "led13", "Virtual LED"),
        LED14(114, "led14", "Virtual LED"),
        LED15(115, "led15", "Virtual LED"),
        LED16(116, "led16", "Virtual LED"),
        LED17(117, "led17", "Virtual LED"),
        LED18(118, "led18", "Virtual LED"),
        LED19(119, "led19", "Virtual LED"),
        LED20(120, "led20", "Virtual LED"),
        LED21(121, "led21", "Virtual LED"),
        LED22(122, "led22", "Virtual LED"),
        LED23(123, "led23", "Virtual LED"),
        LED24(124, "led24", "Virtual LED"),
        LED25(125, "led25", "Virtual LED"),
        LED26(126, "led26", "Virtual LED"),
        LED27(127, "led27", "Virtual LED"),
        LED28(128, "led28", "Virtual LED"),
        LED29(129, "led29", "Virtual LED"),
        LED30(130, "led30", "Virtual LED"),
        LED31(131, "led31", "Virtual LED"),
        LED32(132, "led32", "Virtual LED"),
        LED33(133, "led33", "Virtual LED"),
        LED34(134, "led34", "Virtual LED"),
        LED35(135, "led35", "Virtual LED"),
        LED36(136, "led36", "Virtual LED"),
        LED37(137, "led37", "Virtual LED"),
        LED38(138, "led38", "Virtual LED"),
        LED39(139, "led39", "Virtual LED"),
        LED40(140, "led40", "Virtual LED"),
        LED41(141, "led41", "Virtual LED"),
        LED42(142, "led42", "Virtual LED"),
        LED43(143, "led43", "Virtual LED"),
        LED44(144, "led44", "Virtual LED"),
        LED45(145, "led45", "Virtual LED"),
        LED46(146, "led46", "Virtual LED"),
        LED47(147, "led47", "Virtual LED"),
        LED48(148, "led48", "Virtual LED"),
        LED49(149, "led49", "Virtual LED"),
        LED50(150, "led50", "Virtual LED"),
        LED51(151, "led51", "Virtual LED"),
        LED52(152, "led52", "Virtual LED"),
        LED53(153, "led53", "Virtual LED"),
        LED54(154, "led54", "Virtual LED"),
        LED55(155, "led55", "Virtual LED"),
        LED56(156, "led56", "Virtual LED"),
        LED57(157, "led57", "Virtual LED"),
        LED58(158, "led58", "Virtual LED"),
        LED59(159, "led59", "Virtual LED"),
        LED60(160, "led60", "Virtual LED"),
        LED61(161, "led61", "Virtual LED"),
        LED62(162, "led62", "Virtual LED"),
        LED63(163, "led63", "Virtual LED"),
        LED64(164, "led64", "Virtual LED"),
        LED65(165, "led65", "Virtual LED"),
        LED66(166, "led66", "Virtual LED"),
        LED67(167, "led67", "Virtual LED"),
        LED68(168, "led68", "Virtual LED"),
        LED69(169, "led69", "Virtual LED"),
        LED70(170, "led70", "Virtual LED"),
        LED71(171, "led71", "Virtual LED"),
        LED72(172, "led72", "Virtual LED"),
        LED73(173, "led73", "Virtual LED"),
        LED74(174, "led74", "Virtual LED"),
        LED75(175, "led75", "Virtual LED"),
        LED76(176, "led76", "Virtual LED"),
        LED77(177, "led77", "Virtual LED"),
        LED78(178, "led78", "Virtual LED"),
        LED79(179, "led79", "Virtual LED"),
        LED80(180, "led80", "Virtual LED"),
        LED81(181, "led81", "Virtual LED"),
        LED82(182, "led82", "Virtual LED"),
        LED83(183, "led83", "Virtual LED"),
        LED84(184, "led84", "Virtual LED"),
        LED85(185, "led85", "Virtual LED"),
        LED86(186, "led86", "Virtual LED"),
        LED87(187, "led87", "Virtual LED"),
        LED88(188, "led88", "Virtual LED"),
        LED89(189, "led89", "Virtual LED"),
        LED90(190, "led90", "Virtual LED"),
        LED91(191, "led91", "Virtual LED"),
        LED92(192, "led92", "Virtual LED"),
        LED93(193, "led93", "Virtual LED"),
        LED94(194, "led94", "Virtual LED"),
        LED95(195, "led95", "Virtual LED"),
        LED96(196, "led96", "Virtual LED"),
        LED97(197, "led97", "Virtual LED"),
        LED98(198, "led98", "Virtual LED"),
        LED99(199, "led99", "Virtual LED"),
        LED100(200, "led100", "Virtual LED");

        private final int id;
        private final String channel;
        private final String description;

        Component(int id, String channel, String description) {
            this.id = id;
            this.channel = channel;
            this.description = description;
        }

        @Override
        public int id() {
            return id;
        }

        @Override
        public String channel() {
            return channel;
        }

        @Override
        public String description() {
            return description;
        }

    }

    private final Logger logger = LoggerFactory.getLogger(VirtualKeypadHandler.class);

    @Override
    protected boolean isLed(int id) {
        return (id >= 101 && id <= 200);
    }

    @Override
    protected boolean isButton(int id) {
        return (id >= 1 && id <= 100);
    }

    @Override
    protected boolean isCCI(int id) {
        return false;
    }

    @Override
    protected void configureComponents(String model) {
        logger.debug("Configuring components for virtual keypad");

        for (Component x : EnumSet.allOf(Component.class)) {
            if (isLed(x.id)) {
                ledList.add(x);
            }
            if (isButton(x.id)) {
                buttonList.add(x);
            }
        }
    }

    public VirtualKeypadHandler(Thing thing) {
        super(thing);
        // Mark all channels "Advanced" since most are unlikely to be used in any particular config
        advancedChannels = true;
    }

}
