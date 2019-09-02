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
package org.openhab.binding.yamahareceiver.internal.protocol;

import static java.util.stream.Collectors.toSet;
import static org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Inputs.*;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Stream;

/**
 * The navigation control protocol interface
 *
 * @author David Graeff - Initial contribution
 * @author Tomasz Maruszak - refactoring
 */
public interface InputWithNavigationControl extends IStateUpdatable {
    /**
     * List all inputs that are compatible with this kind of control
     */
    Set<String> SUPPORTED_INPUTS = Stream
            .of(INPUT_NET_RADIO, INPUT_NET_RADIO_LEGACY, INPUT_USB, INPUT_IPOD_USB, INPUT_DOCK, INPUT_PC, INPUT_NAPSTER,
                    INPUT_PANDORA, INPUT_SIRIUS, INPUT_RHAPSODY, INPUT_IPOD, INPUT_HD_RADIO)
            .collect(toSet());

    /**
     * Navigate back
     *
     * @throws ReceivedMessageParseException, IOException
     */
    void goBack() throws ReceivedMessageParseException, IOException;

    /**
     * Navigate up
     *
     * @throws ReceivedMessageParseException, IOException
     */
    void goUp() throws IOException, ReceivedMessageParseException;

    /**
     * Navigate down
     *
     * @throws ReceivedMessageParseException, IOException
     */
    void goDown() throws IOException, ReceivedMessageParseException;

    /**
     * Navigate left. Not for all zones or functions available.
     *
     * @throws ReceivedMessageParseException, IOException
     */
    void goLeft() throws IOException, ReceivedMessageParseException;

    /**
     * Navigate right. Not for all zones or functions available.
     *
     * @throws ReceivedMessageParseException, IOException
     */
    void goRight() throws IOException, ReceivedMessageParseException;

    /**
     * Select current item. Not for all zones or functions available.
     *
     * @throws ReceivedMessageParseException, IOException
     */
    void selectCurrentItem() throws IOException, ReceivedMessageParseException;

    /**
     * Navigate to root menu
     *
     * @throws ReceivedMessageParseException, IOException
     */
    boolean goToRoot() throws IOException, ReceivedMessageParseException;

    /**
     * Navigate to the given page. The Yamaha protocol separates list of items into pages.
     *
     * @param page The page, starting with 1.
     * @throws IOException
     * @throws ReceivedMessageParseException
     */
    void goToPage(int page) throws IOException, ReceivedMessageParseException;

    /**
     * Provide a full path to the menu and menu item
     *
     * @param fullPath
     * @throws IOException
     * @throws ReceivedMessageParseException
     */
    void selectItemFullPath(String fullPath) throws IOException, ReceivedMessageParseException;
}
