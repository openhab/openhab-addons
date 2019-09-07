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
package org.openhab.binding.pjlinkdevice.internal;

import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link PJLinkDeviceBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Nils Schnabel - Initial contribution
 */
@NonNullByDefault
public class PJLinkDeviceBindingConstants {

  private static final String BINDING_ID = "pjLinkDevice";

  // List of all Thing Type UIDs
  public static final ThingTypeUID THING_TYPE_PJLINK = new ThingTypeUID(BINDING_ID, "pjLinkDevice");

  // List of all Channel ids
  public static final String CHANNEL_POWER = "power";
  public static final String CHANNEL_INPUT = "input";
  public static final String CHANNEL_AUDIO_MUTE = "audioMute";
  public static final String CHANNEL_VIDEO_MUTE = "videoMute";

  public static final int DEFAULT_PORT = 4352;
  public static final int DEFAULT_SCAN_TIMEOUT_SECONDS = 60;

  // configuration
  public static final String PARAMETER_HOSTNAME = "ipAddress";
  public static final String PARAMETER_PORT = "tcpPort";
  public static final long DISCOVERY_RESULT_TTL_SECONDS = TimeUnit.MINUTES.toSeconds(10);

  // information disclosed by device
  public static final String PROPERTY_CLASS = "disclosedPjLinkClass";
  public static final String PROPERTY_NAME = "disclosedName";
  public static final String PROPERTY_ERROR_STATUS = "disclosedErrorStatus";
  public static final String PROPERTY_LAMP_HOURS = "disclosedLampHours";
  public static final String PROPERTY_OTHER_INFORMATION = "disclosedOtherInformation";

  // calculated properties
  public static final String PROPERTY_AUTHENTICATION_REQUIRED = "authenticationRequired";
}
