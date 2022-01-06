/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.lifx.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.openhab.binding.lifx.internal.LifxProduct.Feature.*;
import static org.openhab.binding.lifx.internal.LifxProduct.TemperatureRange.*;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.lifx.internal.LifxProduct.Features;
import org.openhab.binding.lifx.internal.LifxProduct.Upgrade;

/**
 * Tests {@link LifxProduct}.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class LifxProductTest {

    @Test
    public void productIDsAreUnique() {
        Set<Long> productIDs = new HashSet<>();
        for (LifxProduct product : LifxProduct.values()) {
            assertThat(productIDs, not(hasItem(product.getID())));
            productIDs.add(product.getID());
        }
    }

    @Test
    public void productNamesMatchProductIDs() {
        for (LifxProduct product : LifxProduct.values()) {
            assertThat(product.name(), is("PRODUCT_" + product.getID()));
        }
    }

    @Test
    public void lightsHaveDefinedTemperatureRange() {
        for (LifxProduct product : LifxProduct.values()) {
            if (product.isLight()) {
                String reason = String.format("The %s light does not define a temperature range", product.name());
                assertThat(reason, product.getFeatures().getTemperatureRange(), is(not(NONE)));
            }
        }
    }

    @Test
    public void upgradesSortedByMajorMinor() {
        for (LifxProduct product : LifxProduct.values()) {
            long major = 0;
            long minor = 0;
            for (Upgrade upgrade : product.getUpgrades()) {
                String reason = String.format("Upgrades for %s are not sorted by major minor (%s.%s >= %s.%s)",
                        product.name(), major, minor, upgrade.major, upgrade.minor);
                assertThat(reason, major < upgrade.major || (major == upgrade.major && minor < upgrade.minor),
                        is(true));
                major = upgrade.major;
                minor = upgrade.minor;
            }
        }
    }

    @Test
    public void getFeaturesForProductWithoutUpgrades() {
        LifxProduct product = LifxProduct.PRODUCT_1;
        assertThat(product.getUpgrades(), hasSize(0));

        Features features = product.getFeatures();
        assertThat(features.getTemperatureRange(), is(TR_2500_9000));
        assertThat(features.hasFeature(COLOR), is(true));

        features = product.getFeatures("1.23");
        assertThat(features.getTemperatureRange(), is(TR_2500_9000));
        assertThat(features.hasFeature(COLOR), is(true));
    }

    @Test
    public void getFeaturesForProductWithUpgrades() {
        LifxProduct product = LifxProduct.PRODUCT_32;
        assertThat(product.getUpgrades(), hasSize(2));

        Features features = product.getFeatures();
        assertThat(features.getTemperatureRange(), is(TR_2500_9000));
        assertThat(features.hasFeature(COLOR), is(true));
        assertThat(features.hasFeature(EXTENDED_MULTIZONE), is(false));
        assertThat(features.hasFeature(INFRARED), is(false));
        assertThat(features.hasFeature(MULTIZONE), is(true));

        features = product.getFeatures("2.70");
        assertThat(features.getTemperatureRange(), is(TR_2500_9000));
        assertThat(features.hasFeature(COLOR), is(true));
        assertThat(features.hasFeature(EXTENDED_MULTIZONE), is(false));
        assertThat(features.hasFeature(INFRARED), is(false));
        assertThat(features.hasFeature(MULTIZONE), is(true));

        features = product.getFeatures("2.77");
        assertThat(features.getTemperatureRange(), is(TR_2500_9000));
        assertThat(features.hasFeature(COLOR), is(true));
        assertThat(features.hasFeature(EXTENDED_MULTIZONE), is(true));
        assertThat(features.hasFeature(INFRARED), is(false));
        assertThat(features.hasFeature(MULTIZONE), is(true));

        features = product.getFeatures("2.79");
        assertThat(features.getTemperatureRange(), is(TR_2500_9000));
        assertThat(features.hasFeature(COLOR), is(true));
        assertThat(features.hasFeature(EXTENDED_MULTIZONE), is(true));
        assertThat(features.hasFeature(INFRARED), is(false));
        assertThat(features.hasFeature(MULTIZONE), is(true));

        features = product.getFeatures("2.80");
        assertThat(features.getTemperatureRange(), is(TR_1500_9000));
        assertThat(features.hasFeature(COLOR), is(true));
        assertThat(features.hasFeature(EXTENDED_MULTIZONE), is(true));
        assertThat(features.hasFeature(INFRARED), is(false));
        assertThat(features.hasFeature(MULTIZONE), is(true));

        features = product.getFeatures("2.81");
        assertThat(features.getTemperatureRange(), is(TR_1500_9000));
        assertThat(features.hasFeature(COLOR), is(true));
        assertThat(features.hasFeature(EXTENDED_MULTIZONE), is(true));
        assertThat(features.hasFeature(INFRARED), is(false));
        assertThat(features.hasFeature(MULTIZONE), is(true));
    }
}
