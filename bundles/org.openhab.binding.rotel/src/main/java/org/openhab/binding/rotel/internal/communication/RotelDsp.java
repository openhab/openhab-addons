/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.rotel.internal.communication;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.rotel.internal.RotelException;
import org.openhab.core.types.StateOption;

/**
 * Represents the different DSP modes available for the Rotel equipments
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public enum RotelDsp {

    CAT1_NONE(1, "NONE", RotelCommand.STEREO, "stereo"),
    CAT1_STEREO3(1, "STEREO3", RotelCommand.STEREO3, "dolby_3_stereo"),
    CAT1_STEREO5(1, "STEREO5", RotelCommand.STEREO5, "5_channel_stereo"),
    CAT1_STEREO7(1, "STEREO7", RotelCommand.STEREO7, "7_channel_stereo"),
    CAT1_MUSIC1(1, "MUSIC1", RotelCommand.DSP1, "dsp1"),
    CAT1_MUSIC2(1, "MUSIC2", RotelCommand.DSP2, "dsp2"),
    CAT1_MUSIC3(1, "MUSIC3", RotelCommand.DSP3, "dsp3"),
    CAT1_MUSIC4(1, "MUSIC4", RotelCommand.DSP4, "dsp4"),
    CAT1_PROLOGIC(1, "PROLOGIC", RotelCommand.PROLOGIC, "dolby_prologic"),
    CAT1_PLII_CINEMA(1, "PLIICINEMA", RotelCommand.PLII_CINEMA, "dolby_plii_movie"),
    CAT1_PLII_MUSIC(1, "PLIIMUSIC", RotelCommand.PLII_MUSIC, "dolby_plii_music"),
    CAT1_NEO6_CINEMA(1, "NEO6CINEMA", RotelCommand.NEO6_CINEMA, "dts_neo:6_cinema"),
    CAT1_NEO6_MUSIC(1, "NEO6MUSIC", RotelCommand.NEO6_MUSIC, "dts_neo:6_music"),

    CAT2_NONE(2, "NONE", RotelCommand.STEREO, "stereo"),
    CAT2_STEREO3(2, "STEREO3", RotelCommand.STEREO3, "dolby_3_stereo"),
    CAT2_STEREO5(2, "STEREO5", RotelCommand.STEREO5, "5_channel_stereo"),
    CAT2_STEREO7(2, "STEREO7", RotelCommand.STEREO7, "7_channel_stereo"),
    CAT2_MUSIC1(2, "MUSIC1", RotelCommand.DSP1, "dsp1"),
    CAT2_MUSIC2(2, "MUSIC2", RotelCommand.DSP2, "dsp2"),
    CAT2_MUSIC3(2, "MUSIC3", RotelCommand.DSP3, "dsp3"),
    CAT2_MUSIC4(2, "MUSIC4", RotelCommand.DSP4, "dsp4"),
    CAT2_PROLOGIC(2, "PROLOGIC", RotelCommand.PROLOGIC, "dolby_prologic"),
    CAT2_PLII_CINEMA(2, "PLIICINEMA", RotelCommand.PLII_CINEMA, "dolby_plii_movie"),
    CAT2_PLII_MUSIC(2, "PLIIMUSIC", RotelCommand.PLII_MUSIC, "dolby_plii_music"),
    CAT2_PLII_GAME(2, "PLIIGAME", RotelCommand.PLII_GAME, "dolby_plii_game"),
    CAT2_NEO6_CINEMA(2, "NEO6CINEMA", RotelCommand.NEO6_CINEMA, "dts_neo:6_cinema"),
    CAT2_NEO6_MUSIC(2, "NEO6MUSIC", RotelCommand.NEO6_MUSIC, "dts_neo:6_music"),

    CAT3_NONE(3, "NONE", RotelCommand.STEREO, "stereo"),
    CAT3_STEREO3(3, "STEREO3", RotelCommand.STEREO3, "dolby_3_stereo"),
    CAT3_STEREO5(3, "STEREO5", RotelCommand.STEREO5, "5_channel_stereo"),
    CAT3_STEREO7(3, "STEREO7", RotelCommand.STEREO7, "7_channel_stereo"),
    CAT3_DSP1(3, "DSP1", RotelCommand.DSP1, "dsp1"),
    CAT3_DSP2(3, "DSP2", RotelCommand.DSP2, "dsp2"),
    CAT3_DSP3(3, "DSP3", RotelCommand.DSP3, "dsp3"),
    CAT3_DSP4(3, "DSP4", RotelCommand.DSP4, "dsp4"),
    CAT3_PROLOGIC(3, "PROLOGIC", RotelCommand.PROLOGIC, "dolby_prologic"),
    CAT3_PLII_CINEMA(3, "PLIIXCINEMA", RotelCommand.PLII_CINEMA, "dolby_plii_movie"),
    CAT3_PLII_MUSIC(3, "PLIIXMUSIC", RotelCommand.PLII_MUSIC, "dolby_plii_music"),
    CAT3_PLII_GAME(3, "PLIIXGAME", RotelCommand.PLII_GAME, "dolby_plii_game"),
    CAT3_NEO6_CINEMA(3, "NEO6CINEMA", RotelCommand.NEO6_CINEMA, "dts_neo:6_cinema"),
    CAT3_NEO6_MUSIC(3, "NEO6MUSIC", RotelCommand.NEO6_MUSIC, "dts_neo:6_music"),

    CAT4_NONE(4, "NONE", RotelCommand.STEREO, "stereo"),
    CAT4_STEREO3(4, "STEREO3", RotelCommand.STEREO3, "dolby_3_stereo"),
    CAT4_STEREO5(4, "STEREO5", RotelCommand.STEREO5, "5_channel_stereo"),
    CAT4_STEREO7(4, "STEREO7", RotelCommand.STEREO7, "7_channel_stereo"),
    CAT4_DSP1(4, "DSP1", RotelCommand.DSP1, "dsp1"),
    CAT4_DSP2(4, "DSP2", RotelCommand.DSP2, "dsp2"),
    CAT4_DSP3(4, "DSP3", RotelCommand.DSP3, "dsp3"),
    CAT4_DSP4(4, "DSP4", RotelCommand.DSP4, "dsp4"),
    CAT4_PROLOGIC(4, "PROLOGIC", RotelCommand.PROLOGIC, "dolby_prologic"),
    CAT4_PLII_CINEMA(4, "PLIIXCINEMA", RotelCommand.PLII_CINEMA, "dolby_plii_movie"),
    CAT4_PLII_MUSIC(4, "PLIIXMUSIC", RotelCommand.PLII_MUSIC, "dolby_plii_music"),
    CAT4_PLII_GAME(4, "PLIIXGAME", RotelCommand.PLII_GAME, "dolby_plii_game"),
    CAT4_PLIIZ(4, "PLIIZ", RotelCommand.PLIIZ, "dolby_pliiz"),
    CAT4_NEO6_CINEMA(4, "NEO6CINEMA", RotelCommand.NEO6_CINEMA, "dts_neo:6_cinema"),
    CAT4_NEO6_MUSIC(4, "NEO6MUSIC", RotelCommand.NEO6_MUSIC, "dts_neo:6_music"),

    CAT5_BYPASS(5, "BYPASS", RotelCommand.BYPASS, "analog_bypass"),
    CAT5_NONE(5, "NONE", RotelCommand.STEREO, "stereo"),
    CAT5_STEREO3(5, "STEREO3", RotelCommand.STEREO3, "dolby_3_stereo"),
    CAT5_STEREO5(5, "STEREO5", RotelCommand.STEREO5, "5_channel_stereo"),
    CAT5_STEREO7(5, "STEREO7", RotelCommand.STEREO7, "7_channel_stereo"),
    CAT5_STEREO9(5, "STEREO9", RotelCommand.STEREO9, "9_channel_stereo"),
    CAT5_STEREO11(5, "STEREO11", RotelCommand.STEREO11, "11_channel_stereo"),
    CAT5_ATMOS(5, "ATMOS", RotelCommand.ATMOS, "dolby atmos surround"),
    CAT5_NEURAL_X(5, "NEURALX", RotelCommand.NEURAL_X, "dts neural:x"),

    CAT6_BYPASS(6, "BYPASS", RotelCommand.BYPASS, "analog_bypass"),
    CAT6_NONE(6, "NONE", RotelCommand.STEREO, "stereo"),
    CAT6_STEREO3(6, "STEREO3", RotelCommand.STEREO3, "dolby_3_stereo"),
    CAT6_STEREO5(6, "STEREO5", RotelCommand.STEREO5, "5_channel_stereo"),
    CAT6_STEREO7(6, "STEREO7", RotelCommand.STEREO7, "7_channel_stereo"),
    CAT6_PLII_CINEMA(6, "PLIIXCINEMA", RotelCommand.PLII_CINEMA, "dolby_plii_movie"),
    CAT6_PLII_MUSIC(6, "PLIIXMUSIC", RotelCommand.PLII_MUSIC, "dolby_plii_music"),
    CAT6_PLII_GAME(6, "PLIIXGAME", RotelCommand.PLII_GAME, "dolby_plii_game"),
    CAT6_PLIIZ(6, "PLIIZ", RotelCommand.PLIIZ, "dolby_pliiz"),
    CAT6_NEO6_CINEMA(6, "NEO6CINEMA", RotelCommand.NEO6_CINEMA, "dts_neo:6_cinema"),
    CAT6_NEO6_MUSIC(6, "NEO6MUSIC", RotelCommand.NEO6_MUSIC, "dts_neo:6_music");

    private int category;
    private String name;
    private RotelCommand command;
    private String feedback;

    /**
     * Constructor
     *
     * @param category a category of models for which the DSP mode is available
     * @param name the name of the DSP mode
     * @param command the command to select the DSP mode
     * @param feedback the feedback message identifying the DSP mode
     */
    private RotelDsp(int category, String name, RotelCommand command, String feedback) {
        this.category = category;
        this.name = name;
        this.command = command;
        this.feedback = feedback;
    }

    /**
     * Get the category of models for the current DSP mode
     *
     * @return the category of models
     */
    public int getCategory() {
        return category;
    }

    /**
     * Get the name of the current DSP mode
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the command to select the current DSP mode
     *
     * @return the command
     */
    public RotelCommand getCommand() {
        return command;
    }

    /**
     * Get the feedback message identifying the current DSP mode
     *
     * @return the feedback message
     */
    public String getFeedback() {
        return feedback;
    }

    /**
     * Get the list of {@link StateOption} associated to the available DSP modes for a particular category of models
     *
     * @param category a category of models
     *
     * @return the list of {@link StateOption} associated to the available DSP modes for a provided category of models
     */
    public static List<StateOption> getStateOptions(int category) {
        List<StateOption> options = new ArrayList<>();
        for (RotelDsp value : RotelDsp.values()) {
            if (value.getCategory() == category) {
                options.add(new StateOption(value.getName(), value.getName()));
            }
        }
        return options;
    }

    /**
     * Get the DSP mode associated to a name for a particular category of models
     *
     * @param category a category of models
     * @param name the name used to identify the DSP mode
     *
     * @return the DSP mode associated to the searched name for the provided category of models
     *
     * @throws RotelException - If no DSP mode is associated to the searched name for the provided category
     */
    public static RotelDsp getFromName(int category, String name) throws RotelException {
        for (RotelDsp value : RotelDsp.values()) {
            if (value.getCategory() == category && value.getName().equals(name)) {
                return value;
            }
        }
        throw new RotelException("Invalid name for a DSP mode: " + name);
    }

    /**
     * Get the DSP mode identified by a feedback message for a particular category of models
     *
     * @param category a category of models
     * @param feedback the feedback message used to identify the DSP mode
     *
     * @return the DSP mode associated to the searched feedback message for the provided category of models
     *
     * @throws RotelException - If no DSP mode is associated to the searched feedback message for the provided category
     */
    public static RotelDsp getFromFeedback(int category, String feedback) throws RotelException {
        for (RotelDsp value : RotelDsp.values()) {
            if (value.getCategory() == category && value.getFeedback().equals(feedback)) {
                return value;
            }
        }
        throw new RotelException("Invalid feedback for a DSP mode: " + feedback);
    }

    /**
     * Get the DSP mode associated to a command for a particular category of models
     *
     * @param category a category of models
     * @param command the command used to identify the DSP mode
     *
     * @return the DSP mode associated to the searched command for the provided category of models
     *
     * @throws RotelException - If no DSP mode is associated to the searched command for the provided category
     */
    public static RotelDsp getFromCommand(int category, RotelCommand command) throws RotelException {
        for (RotelDsp value : RotelDsp.values()) {
            if (value.getCategory() == category && value.getCommand() == command) {
                return value;
            }
        }
        throw new RotelException("Invalid command for a DSP mode: " + command.getLabel());
    }
}
