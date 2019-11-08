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
package org.openhab.binding.velux;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VeluxBindingProperties} class defines common constants, which are
 * used within the property definitions.
 *
 * This class contains the property identifications:
 * <UL>
 * <LI>{@link #PROPERTY_BINDING_BUNDLEVERSION} for identification of the binding,
 * </UL>
 * <UL>
 * <LI>{@link #PROPERTY_SCENE_NAME} for defining the name of a scene,
 * </UL>
 * <UL>
 * <LI>{@link #PROPERTY_ACTUATOR_SERIALNUMBER} for defining the serial number of an actuator, a rollershutter and a
 * window,
 * <LI>{@link #PROPERTY_ACTUATOR_NAME} for defining the name of an actuator, a rollershutter and a window,
 * <LI>{@link #PROPERTY_ACTUATOR_INVERTED} for modifying the value of a Channel,
 * </UL>
 * <UL>
 * <LI>{@link #PROPERTY_VSHUTTER_SCENELEVELS} for defining a virtual shutter.
 * <LI>{@link #PROPERTY_VSHUTTER_CURRENTLEVEL} for defining a virtual shutter.
 * </UL>
 *
 * @author Guenther Schreiner - Initial contribution
 */
@NonNullByDefault
public class VeluxBindingProperties {

    public static final String PROPERTY_BINDING_BUNDLEVERSION = "bundleVersion";

    public static final String PROPERTY_SCENE_NAME = "sceneName";
    public static final String PROPERTY_SCENE_VELOCITY = "velocity";

    public static final String PROPERTY_ACTUATOR_SERIALNUMBER = "serial";
    public static final String PROPERTY_ACTUATOR_NAME = "name";
    public static final String PROPERTY_ACTUATOR_INVERTED = "inverted";

    public static final String PROPERTY_VSHUTTER_SCENELEVELS = "sceneLevels";
    public static final String PROPERTY_VSHUTTER_CURRENTLEVEL = "currentLevel";

}
