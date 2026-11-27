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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.openhab.binding.lifx.internal.LifxProduct.Feature;
import org.openhab.binding.lifx.internal.LifxProduct.Features;
import org.openhab.binding.lifx.internal.LifxProduct.Upgrade;

/**
 * Tests {@link LifxProduct}.
 *
 * @author Wouter Born - Initial contribution
 * @author Wouter Born - Update product feature regression coverage
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

    @ParameterizedTest
    @EnumSource(value = LifxProduct.class, names = { "PRODUCT_207", "PRODUCT_208", "PRODUCT_219", "PRODUCT_220",
            "PRODUCT_267", "PRODUCT_268" })
    public void lightsWithButtonsAreRecognized(LifxProduct product) {
        assertThat(product.getFeatures().hasFeature(BUTTONS), is(true));
        assertThat(product.isLight(), is(true));
    }

    @ParameterizedTest
    @EnumSource(value = LifxProduct.class, names = { "PRODUCT_70", "PRODUCT_71", "PRODUCT_84", "PRODUCT_89",
            "PRODUCT_115", "PRODUCT_116", "PRODUCT_226" })
    public void switchesAreNotLights(LifxProduct product) {
        assertThat(product.isLight(), is(false));
    }

    @ParameterizedTest(name = "[{index}] {arguments}")
    @CsvSource(delimiter = '|', useHeadersInDisplayName = true, textBlock = """
            productID | previousVersion | upgradeVersion
            117       | 4.147           | 4.148
            176       | 4.147           | 4.148
            219       | 4.147           | 4.148
            265       | 4.147           | 4.148
            267       | 4.199           | 4.200
            268       | 4.147           | 4.148
            """)
    public void getFeaturesForExtendedTemperatureRangeUpgrade(long productID, String previousVersion,
            String upgradeVersion) {
        LifxProduct product = LifxProduct.getProductFromProductID(productID);
        Features initialFeatures = product.getFeatures();
        assertThat(initialFeatures.getTemperatureRange(), is(TR_1500_9000));
        assertThat(product.getFeatures(previousVersion).getTemperatureRange(), is(TR_1500_9000));

        Features upgradedFeatures = product.getFeatures(upgradeVersion);
        assertThat(upgradedFeatures.getTemperatureRange(), is(TR_1000_10000));
        assertThat(product.getFeatures("5.0").getTemperatureRange(), is(TR_1000_10000));
        for (Feature feature : Feature.values()) {
            assertThat(upgradedFeatures.hasFeature(feature), is(initialFeatures.hasFeature(feature)));
        }
    }

    @ParameterizedTest
    @EnumSource(value = LifxProduct.class, names = { "PRODUCT_50", "PRODUCT_60" })
    public void getFeaturesForMiniWhiteToWarmUpgrade(LifxProduct product) {
        assertThat(product.getFeatures().getTemperatureRange(), is(TR_2500_9000));
        assertThat(product.getFeatures("3.69").getTemperatureRange(), is(TR_2500_9000));
        assertThat(product.getFeatures("3.70").getTemperatureRange(), is(TR_1500_9000));
    }

    @Test
    public void extendedMultizoneDoesNotImplyMultizone() {
        LifxProduct product = LifxProduct.getProductFromProductID(72);
        assertThat(product.getFeatures().hasFeature(EXTENDED_MULTIZONE), is(true));
        assertThat(product.getFeatures().hasFeature(MULTIZONE), is(false));
        assertThat(product.getThingTypeUID(), is(LifxBindingConstants.THING_TYPE_COLORLIGHT));
    }

    @Test
    public void multizoneDoesNotRequireExtendedMultizone() {
        LifxProduct product = LifxProduct.getProductFromProductID(300);
        assertThat(product.getFeatures().hasFeature(EXTENDED_MULTIZONE), is(false));
        assertThat(product.getFeatures().hasFeature(MULTIZONE), is(true));
        assertThat(product.getThingTypeUID(), is(LifxBindingConstants.THING_TYPE_COLORMZLIGHT));
    }
}
