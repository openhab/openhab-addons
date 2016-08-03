/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.atlona.internal.pro3;

/**
 * The {@link AtlonaPro3Binding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Tim Roberts - Initial contribution
 */
class AtlonaPro3Constants {

    // List of all Channel ids
    final static String CHANNEL_POWER = "power";
    final static String CHANNEL_VERSION = "version";
    final static String CHANNEL_TYPE = "type";
    final static String CHANNEL_PANELLOCK = "panellock";
    final static String CHANNEL_RESETPORTS = "resetports";
    final static String CHANNEL_PORTPOWER = "portpower";
    final static String CHANNEL_PORTALL = "portall";
    final static String CHANNEL_PORTOUTPUT = "portoutput";
    final static String CHANNEL_PORTMIRROR = "portmirror";
    final static String CHANNEL_VOLUME = "volume";
    final static String CHANNEL_VOLUME_MUTE = "volumemute";
    final static String CHANNEL_IRENABLE = "irenable";
    final static String CHANNEL_SAVEIO = "saveio";
    final static String CHANNEL_RECALLIO = "recallio";
    final static String CHANNEL_CLEARIO = "cleario";
    final static String CHANNEL_RESETMATRIX = "resetmatrix";
    // final static String CHANNEL_RS232 = "rs232cmd";

    final static String CONFIG_HOSTNAME = "hostname";
    final static String CONFIG_OUTPUT = "output";
}
