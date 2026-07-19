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
package org.openhab.binding.atv.internal.client.dto;

/**
 * All supported features.
 *
 * <p>
 * Numeric values are fixed for storage compatibility.
 *
 * @author Dan Cunningham - Initial contribution
 */
public enum FeatureName {
    /**
     * Up button on remote.
     */
    Up(0),

    /**
     * Down button on remote.
     */
    Down(1),

    /**
     * Left button on remote.
     */
    Left(2),

    /**
     * Right button on remote.
     */
    Right(3),

    /**
     * Start playing media.
     */
    Play(4),

    /**
     * Toggle between play/pause.
     */
    PlayPause(5),

    /**
     * Pause playing media.
     */
    Pause(6),

    /**
     * Stop playing media.
     */
    Stop(7),

    /**
     * Change to next item.
     */
    Next(8),

    /**
     * Change to previous item.
     */
    Previous(9),

    /**
     * Select current option.
     */
    Select(10),

    /**
     * Go back to previous menu.
     */
    Menu(11),

    /**
     * Increase volume.
     */
    VolumeUp(12),

    /**
     * Decrease volume.
     */
    VolumeDown(13),

    /**
     * Home/TV button.
     */
    Home(14),

    /**
     * Long-press home button (deprecated: use RemoteControl.home).
     */
    HomeHold(15),

    /**
     * Go to main menu.
     */
    TopMenu(16),

    /**
     * Suspend device (deprecated; use Power.turn_off).
     */
    Suspend(17),

    /**
     * Wake up device (deprecated; use Power.turn_on).
     */
    WakeUp(18),

    /**
     * Skip forward a time interval.
     */
    SkipForward(36),

    /**
     * Skip backwards a time interval.
     */
    SkipBackward(37),

    /**
     * Seek to position.
     */
    SetPosition(19),

    /**
     * Change shuffle state.
     */
    SetShuffle(20),

    /**
     * Change repeat state.
     */
    SetRepeat(21),

    /**
     * Select next channel.
     */
    ChannelUp(48),

    /**
     * Select previous channel.
     */
    ChannelDown(49),

    /**
     * Title of playing media.
     */
    Title(22),

    /**
     * Artist of playing song.
     */
    Artist(23),

    /**
     * Album from playing artist.
     */
    Album(24),

    /**
     * Genre of playing song.
     */
    Genre(25),

    /**
     * Total length of playing media (seconds).
     */
    TotalTime(26),

    /**
     * Current play time position.
     */
    Position(27),

    /**
     * Shuffle state.
     */
    Shuffle(28),

    /**
     * Repeat state.
     */
    Repeat(29),

    /**
     * Title of TV series.
     */
    SeriesName(40),

    /**
     * Season number of TV series.
     */
    SeasonNumber(41),

    /**
     * Episode number of TV series.
     */
    EpisodeNumber(42),

    /**
     * Identifier for Content
     */
    ContentIdentifier(47),

    /**
     * iTunes Store Identifier for Content
     */
    iTunesStoreIdentifier(50),

    /**
     * List of launchable apps.
     */
    AppList(38),

    /**
     * Launch an app.
     */
    LaunchApp(39),

    /**
     * List of user accounts.
     */
    AccountList(55),

    /**
     * Switch user account.
     */
    SwitchAccount(56),

    /**
     * Playing media artwork.
     */
    Artwork(30),

    /**
     * App playing media.
     */
    App(35),

    /**
     * Push updates are supported.
     */
    PushUpdates(43),

    /**
     * Stream a URL on device.
     */
    PlayUrl(31),

    /**
     * Stream local file to device.
     */
    StreamFile(44),

    /**
     * Current device power state.
     */
    PowerState(32),

    /**
     * Activate screen saver.
     */
    Screensaver(58),

    /**
     * Turn device on.
     */
    TurnOn(33),

    /**
     * Turn off device.
     */
    TurnOff(34),

    /**
     * Current volume level.
     */
    Volume(45),

    /**
     * Set volume level.
     */
    SetVolume(46),

    /**
     * Current output devices.
     */
    OutputDevices(59),

    /**
     * Add output devices.
     */
    AddOutputDevices(60),

    /**
     * Remove output devices.
     */
    RemoveOutputDevices(61),

    /**
     * Set output devices.
     */
    SetOutputDevices(62),

    /**
     * Current virtual keyboard focus state.
     */
    TextFocusState(57),

    /**
     * Get current virtual keyboard text.
     */
    TextGet(51),

    /**
     * Clear virtual keyboard text.
     */
    TextClear(52),

    /**
     * Input text into virtual keyboard.
     */
    TextAppend(53),

    /**
     * Replace text in virtual keyboard.
     */
    TextSet(54),

    /**
     * Touch swipe from given coordinates and duration.
     */
    Swipe(63),

    /**
     * Touch event to given coordinates.
     */
    Action(64),

    /**
     * Touch click command.
     */
    Click(65),

    /**
     * Show EPG.
     */
    Guide(66),

    /**
     * Open the Control Center.
     */
    ControlCenter(68);

    private final int value;

    FeatureName(int value) {
        this.value = value;
    }

    /**
     * Returns the stored numeric value.
     */
    public int value() {
        return value;
    }

    /**
     * Looks up the constant matching a stored numeric value.
     *
     * @throws IllegalArgumentException if no constant has the given value
     */
    public static FeatureName fromValue(int value) {
        for (FeatureName v : values()) {
            if (v.value == value) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown FeatureName value: " + value);
    }
}
