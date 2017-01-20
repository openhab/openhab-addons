/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homie.internal.conventionv200;

/**
 * Homie Convention Version 2.0.0
 *
 * @author Michael Kolb
 *
 */
public class HomieConventions {

    /**
     * Pattern to match the ID Format of homie
     */
    protected final static String ID_PATTERN = "(?!-)([a-z0-9\\-]+)(?<!-)";
    /**
     * Topic suffix which contents will be used to name a discovered device
     */
    public final static String NAME_TOPIC_SUFFIX = "$name";
    public final static String INTERNAL_PROPERTY_MARKER = "$";

    public static final String HOMIE_NODE_TYPE_ANNOUNCEMENT_TOPIC_SUFFIX = "$type";
    public static final String HOMIE_NODE_PROPERTYLIST_ANNOUNCEMENT_TOPIC_SUFFIX = "$properties";

    // Additional conventions (not part of Homie convention) below
    public static final String ESH_TYPE_PREFIX = "ESH:";
    public static final String ESH_VALUE_TOPIC = "value";
    public static final String ESH_MIN_TOPIC = "min";
    public static final String ESH_DESC_TOPIC = "desc";
    public static final String ESH_MAX_TOPIC = "max";
    public static final String ESH_STEP_TOPIC = "step";
    public static final String ESH_ITEMTYPE_TOPIC = "itemtype";
    public static final String ESH_UNIT_TOPIC = "unit";
}
