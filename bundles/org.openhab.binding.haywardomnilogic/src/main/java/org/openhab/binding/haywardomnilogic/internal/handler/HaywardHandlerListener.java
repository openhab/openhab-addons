/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.haywardomnilogic.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link HaywardHandlerListener} is notified when a thing is discovered.
 *
 * @author Matt Myers - Initial Contributionn
 *
 */
@NonNullByDefault
public interface HaywardHandlerListener {

    void onBackyardDiscovered(int systemID, String label);

    void onBOWDiscovered(int systemID, String label);

    void onChlorinatorDiscovered(int systemID, String label, String bowID, String bowName);

    void onColorLogicDiscovered(int systemID, String label, String bowID, String bowName);

    void onFilterDiscovered(int systemID, String label, String bowID, String bowName, String property1,
            String property2, String property3, String property4);

    void onHeaterDiscovered(int systemID, String label, String bowID, String bowName);

    void onPumpDiscovered(int systemID, String label, String bowID, String bowName, String property1, String property2,
            String property3, String property4);

    void onRelayDiscovered(int systemID, String label, String bowID, String bowName);

    void onSensorDiscovered(int systemID, String label, String bowID, String bowName);

    void onVirtualHeaterDiscovered(int systemID, String label, String bowID, String bowName);
}
