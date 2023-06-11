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
package org.openhab.voice.actiontemplatehli.internal;

import static org.openhab.voice.actiontemplatehli.internal.ActionTemplateInterpreter.getPlaceholderSymbol;

import java.nio.file.Path;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.OpenHAB;

/**
 * The {@link ActionTemplateInterpreterConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class ActionTemplateInterpreterConstants {
    /**
     * Service name
     */
    public static final String SERVICE_NAME = "Action Template Interpreter";
    /**
     * Service id
     */
    public static final String SERVICE_ID = "actiontemplatehli";

    /**
     * Service category
     */
    public static final String SERVICE_CATEGORY = "voice";
    /**
     * Service pid
     */
    public static final String SERVICE_PID = "org.openhab." + SERVICE_CATEGORY + "." + SERVICE_ID;
    /**
     * Root service folder
     */
    public static final String NLP_FOLDER = Path.of(OpenHAB.getUserDataFolder(), "actiontemplatehli").toString();
    /**
     * NER folder for dictionaries and models
     */
    public static final String NER_FOLDER = Path.of(NLP_FOLDER, "ner").toString();
    /**
     * POS folder for dictionaries and models
     */
    public static final String POS_FOLDER = Path.of(NLP_FOLDER, "pos").toString();
    /**
     * Folder for type action configurations
     */
    public static final String TYPE_ACTION_CONFIGS_FOLDER = Path.of(NLP_FOLDER, "type_actions").toString();
    /**
     * ItemLabel placeholder name
     */
    public static final String ITEM_LABEL_PLACEHOLDER = "itemLabel";
    /**
     * ItemLabel placeholder symbol
     */
    public static final String ITEM_LABEL_PLACEHOLDER_SYMBOL = getPlaceholderSymbol(ITEM_LABEL_PLACEHOLDER);
    /**
     * State placeholder name
     */
    public static final String STATE_PLACEHOLDER = "state";
    /**
     * State placeholder symbol
     */
    public static final String STATE_PLACEHOLDER_SYMBOL = getPlaceholderSymbol(STATE_PLACEHOLDER);
    /**
     * Item option placeholder name
     */
    public static final String ITEM_OPTION_PLACEHOLDER = "itemOption";
    /**
     * State placeholder symbol
     */
    public static final String ITEM_OPTION_PLACEHOLDER_SYMBOL = getPlaceholderSymbol(ITEM_OPTION_PLACEHOLDER);
    /**
     * Dynamic placeholder name
     */
    public static final String DYNAMIC_PLACEHOLDER = "*";
    /**
     * Dynamic placeholder symbol
     */
    public static final String DYNAMIC_PLACEHOLDER_SYMBOL = getPlaceholderSymbol(DYNAMIC_PLACEHOLDER);
    /**
     * GroupLabel placeholder name
     */
    public static final String GROUP_LABEL_PLACEHOLDER = "groupLabel";
    /**
     * GroupLabel placeholder symbol
     */
    public static final String GROUP_LABEL_PLACEHOLDER_SYMBOL = getPlaceholderSymbol(GROUP_LABEL_PLACEHOLDER);
}
