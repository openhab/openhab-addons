/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.phc;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link PHCBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jonas Hohaus - Initial contribution
 */
public class PHCBindingConstants {

  public static final String BINDING_ID = "phc";

  // List of all Thing Type UIDs
  public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

  public final static ThingTypeUID THING_TYPE_AM = new ThingTypeUID(BINDING_ID, "AM");
  public final static ThingTypeUID THING_TYPE_EM = new ThingTypeUID(BINDING_ID, "EM");
  public final static ThingTypeUID THING_TYPE_JRM = new ThingTypeUID(BINDING_ID, "JRM");

  // List of all Channel Group IDs
  public final static String CHANNELS_AM = "am";
  public final static String CHANNELS_EM = "em";
  public final static String CHANNELS_EM_LED = "emLed";
  public final static String CHANNELS_JRM = "jrm";

  // List of all configuration parameters
  public static final String PORT = "port";
  public static final String ADDRESS = "address";
  public static final String UP_DOWN_TIME = "upDownTime";
}
