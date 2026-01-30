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
package org.openhab.transform.math.internal.profiles;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.TimeSeriesProfile;
import org.openhab.core.transform.TransformationService;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Abstract class for {@link TimeSeriesProfile}s which applies simple arithmetic math on the item state.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractArithmeticMathTransformationProfile extends AbstractMathTransformationProfile {

    static final String ITEM_NAME_PARAM = "itemName";

    protected final ItemRegistry itemRegistry;

    protected final @Nullable String itemName;

    public AbstractArithmeticMathTransformationProfile(ProfileCallback callback, ProfileContext context,
            TransformationService service, ItemRegistry itemRegistry, ProfileTypeUID profileTypeUID) {
        super(callback, service, profileTypeUID);
        this.itemRegistry = itemRegistry;

        itemName = getParam(context, ITEM_NAME_PARAM, false);
    }

    /**
     * Returns the Item State as String if the Item exists and its State is not {@link UnDefType} or otherwise the given
     * default value.
     *
     * @param defaultValue the default value
     * @return the Item State as String
     */
    public @Nullable String getItemStateOrElse(@Nullable String defaultValue) {
        String localItemName = itemName;
        if (localItemName != null) {
            try {
                Item item = itemRegistry.getItem(localItemName);
                State state = item.getState();
                if (!(state instanceof UnDefType)) {
                    return state.toString();
                }
            } catch (ItemNotFoundException e) {
                // do nothing
            }
        }
        return defaultValue;
    }
}
