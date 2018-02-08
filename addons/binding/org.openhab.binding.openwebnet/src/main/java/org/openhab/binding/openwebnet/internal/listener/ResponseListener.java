/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet.internal.listener;

import java.util.EventListener;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ResponseListener} defines callback that can be invoked by Parser
 *
 * @author Antoine Laydier
 *
 */
@NonNullByDefault
public interface ResponseListener extends EventListener {

    /**
     * ACK message received
     */
    default void onAck() {
    }

    /**
     * NACK message received
     */
    default void onNack() {
    }

    /**
     * BUSY NACK message received
     */

    default void onBusyNack() {
    }

    /**
     * A light state has changed
     */

    default void onLightStatusChange(int where, int state) {
    }

    /**
     * Network is now open
     */
    default void onNetworkOpen(int where) {
    }

    /**
     * Network is now close
     */
    default void onNetworkClose(int where) {
    }

    /**
     * Network has been joined
     */
    default void onNetworkJoin(int where) {
    }

    /**
     * Network has been left
     */
    default void onNetworkLeave(int where) {
    }

    /**
     * Supervisor mode activated
     */
    default void onSupervisorOn(int where) {
    }

    /**
     * Supervisor mode disabled
     */
    default void onSupervisorOff(int where) {
    }

    /**
     * Product information provided
     */
    default void onProductInformation(int where, int index, int value) {
    }

    /**
     * New thing discovered
     */
    default void onDiscoveredProductsNumber(int number) {
    }

    /**
     * Firmware version
     *
     * @param where can be 0 to indicates local interface
     * @param version
     */
    default void onFirmwareVersion(int where, String version) {
    }

    /**
     * Hardware version
     *
     * @param where can be 0 to indicates local interface
     * @param version
     */
    default void onHardwareVersion(int where, String version) {
    }

    /**
     * Automation feedback
     *
     * @param where
     * @param state
     */
    default void onAutomation(int where, int state) {
    }

    /**
     * Automation detailed feedback
     *
     * @param where
     * @param status Up (11), down (12), stop (10)
     * @param level position of the shutter [0-100] or unknown (255)
     */
    default void onAutomationDetails(int where, int status, int level) {
    }
}
