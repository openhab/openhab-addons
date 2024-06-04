/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.openhab.binding.lifx.internal.LifxProduct.Feature.*;
import static org.openhab.binding.lifx.internal.LifxProduct.TemperatureRange.*;
import static org.openhab.binding.lifx.internal.LifxProduct.Vendor.LIFX;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Enumerates the LIFX products, their IDs and feature set.
 *
 * @see <a href="https://lan.developer.lifx.com/docs/lifx-products">
 *      https://lan.developer.lifx.com/docs/lifx-products</a>
 *
 * @author Wouter Born - Initial contribution
 * @author Wouter Born - Add temperature ranges and simplify feature definitions
 */
@NonNullByDefault
public enum LifxProduct {

    PRODUCT_1(1, "LIFX Original 1000", new Features(TR_2500_9000, COLOR)),
    PRODUCT_3(3, "LIFX Color 650", new Features(TR_2500_9000, COLOR)),
    PRODUCT_10(10, "LIFX White 800 (Low Voltage)", new Features(TR_2700_6500)),
    PRODUCT_11(11, "LIFX White 800 (High Voltage)", new Features(TR_2700_6500)),
    PRODUCT_15(15, "LIFX Color 1000", new Features(TR_2500_9000, COLOR)),
    PRODUCT_18(18, "LIFX White 900 BR30 (Low Voltage)", new Features(TR_2700_6500)),
    PRODUCT_19(19, "LIFX White 900 BR30 (High Voltage)", new Features(TR_2700_6500)),
    PRODUCT_20(20, "LIFX Color 1000 BR30", new Features(TR_2500_9000, COLOR)),
    PRODUCT_22(22, "LIFX Color 1000", new Features(TR_2500_9000, COLOR)),
    PRODUCT_27(27, "LIFX A19", new Features(TR_2500_9000, COLOR), //
            new Upgrade(2, 80, new Features(TR_1500_9000))),
    PRODUCT_28(28, "LIFX BR30", new Features(TR_2500_9000, COLOR), //
            new Upgrade(2, 80, new Features(TR_1500_9000))),
    PRODUCT_29(29, "LIFX A19 Night Vision", new Features(TR_2500_9000, COLOR, INFRARED), //
            new Upgrade(2, 80, new Features(TR_1500_9000))),
    PRODUCT_30(30, "LIFX BR30 Night Vision", new Features(TR_2500_9000, COLOR, INFRARED), //
            new Upgrade(2, 80, new Features(TR_1500_9000))),
    PRODUCT_31(31, "LIFX Z", new Features(TR_2500_9000, COLOR, MULTIZONE)),
    PRODUCT_32(32, "LIFX Z", new Features(TR_2500_9000, COLOR, MULTIZONE), //
            new Upgrade(2, 77, new Features(EXTENDED_MULTIZONE)), //
            new Upgrade(2, 80, new Features(TR_1500_9000))),
    PRODUCT_36(36, "LIFX Downlight", new Features(TR_2500_9000, COLOR), //
            new Upgrade(2, 80, new Features(TR_1500_9000))),
    PRODUCT_37(37, "LIFX Downlight", new Features(TR_2500_9000, COLOR), //
            new Upgrade(2, 80, new Features(TR_1500_9000))),
    PRODUCT_38(38, "LIFX Beam", new Features(TR_2500_9000, COLOR, MULTIZONE), //
            new Upgrade(2, 77, new Features(EXTENDED_MULTIZONE)), //
            new Upgrade(2, 80, new Features(TR_1500_9000))),
    PRODUCT_39(39, "LIFX Downlight White to Warm", new Features(TR_2500_9000), //
            new Upgrade(2, 80, new Features(TR_1500_9000))),
    PRODUCT_40(40, "LIFX Downlight", new Features(TR_2500_9000, COLOR), //
            new Upgrade(2, 80, new Features(TR_1500_9000))),
    PRODUCT_43(43, "LIFX A19", new Features(TR_2500_9000, COLOR), //
            new Upgrade(2, 80, new Features(TR_1500_9000))),
    PRODUCT_44(44, "LIFX BR30", new Features(TR_2500_9000, COLOR), //
            new Upgrade(2, 80, new Features(TR_1500_9000))),
    PRODUCT_45(45, "LIFX A19 Night Vision", new Features(TR_2500_9000, COLOR, INFRARED), //
            new Upgrade(2, 80, new Features(TR_1500_9000))),
    PRODUCT_46(46, "LIFX BR30 Night Vision", new Features(TR_2500_9000, COLOR, INFRARED), //
            new Upgrade(2, 80, new Features(TR_1500_9000))),
    PRODUCT_49(49, "LIFX Mini Color", new Features(TR_1500_9000, COLOR)),
    PRODUCT_50(50, "LIFX Mini White to Warm", new Features(TR_1500_6500), //
            new Upgrade(3, 70, new Features(TR_1500_9000))),
    PRODUCT_51(51, "LIFX Mini White", new Features(TR_2700_2700)),
    PRODUCT_52(52, "LIFX GU10", new Features(TR_1500_9000, COLOR)),
    PRODUCT_53(53, "LIFX GU10", new Features(TR_1500_9000, COLOR)),
    PRODUCT_55(55, "LIFX Tile", new Features(TR_2500_9000, CHAIN, COLOR, MATRIX, TILE_EFFECT)),
    PRODUCT_57(57, "LIFX Candle", new Features(TR_1500_9000, COLOR, MATRIX)),
    PRODUCT_59(59, "LIFX Mini Color", new Features(TR_1500_9000, COLOR)),
    PRODUCT_60(60, "LIFX Mini White to Warm", new Features(TR_1500_6500), //
            new Upgrade(3, 70, new Features(TR_1500_9000))),
    PRODUCT_61(61, "LIFX Mini White", new Features(TR_2700_2700)),
    PRODUCT_62(62, "LIFX A19", new Features(TR_1500_9000, COLOR)),
    PRODUCT_63(63, "LIFX BR30", new Features(TR_1500_9000, COLOR)),
    PRODUCT_64(64, "LIFX A19 Night Vision", new Features(TR_1500_9000, COLOR, INFRARED)),
    PRODUCT_65(65, "LIFX BR30 Night Vision", new Features(TR_1500_9000, COLOR, INFRARED)),
    PRODUCT_66(66, "LIFX Mini White", new Features(TR_2700_2700)),
    PRODUCT_68(68, "LIFX Candle", new Features(TR_1500_9000, COLOR, MATRIX)),
    PRODUCT_70(70, "LIFX Switch", new Features(BUTTONS, RELAYS)),
    PRODUCT_71(71, "LIFX Switch", new Features(BUTTONS, RELAYS)),
    PRODUCT_81(81, "LIFX Candle White to Warm", new Features(TR_2200_6500)),
    PRODUCT_82(82, "LIFX Filament Clear", new Features(TR_2100_2100)),
    PRODUCT_85(85, "LIFX Filament Amber", new Features(TR_2000_2000)),
    PRODUCT_87(87, "LIFX Mini White", new Features(TR_2700_2700)),
    PRODUCT_88(88, "LIFX Mini White", new Features(TR_2700_2700)),
    PRODUCT_89(89, "LIFX Switch", new Features(BUTTONS, RELAYS)),
    PRODUCT_90(90, "LIFX Clean", new Features(TR_1500_9000, COLOR, HEV)),
    PRODUCT_91(91, "LIFX Color", new Features(TR_1500_9000, COLOR)),
    PRODUCT_92(92, "LIFX Color", new Features(TR_1500_9000, COLOR)),
    PRODUCT_93(93, "LIFX A19", new Features(TR_1500_9000, COLOR)),
    PRODUCT_94(94, "LIFX BR30", new Features(TR_1500_9000, COLOR)),
    PRODUCT_96(96, "LIFX Candle White to Warm", new Features(TR_2200_6500)),
    PRODUCT_97(97, "LIFX A19", new Features(TR_1500_9000, COLOR)),
    PRODUCT_98(98, "LIFX BR30", new Features(TR_1500_9000, COLOR)),
    PRODUCT_99(99, "LIFX Clean", new Features(TR_1500_9000, COLOR, HEV)),
    PRODUCT_100(100, "LIFX Filament Clear", new Features(TR_2100_2100)),
    PRODUCT_101(101, "LIFX Filament Amber", new Features(TR_2000_2000)),
    PRODUCT_109(109, "LIFX A19 Night Vision", new Features(TR_1500_9000, COLOR, INFRARED)),
    PRODUCT_110(110, "LIFX BR30 Night Vision", new Features(TR_1500_9000, COLOR, INFRARED)),
    PRODUCT_111(111, "LIFX A19 Night Vision", new Features(TR_1500_9000, COLOR, INFRARED)),
    PRODUCT_112(112, "LIFX BR30 Night Vision", new Features(TR_1500_9000, COLOR, INFRARED)),
    PRODUCT_113(113, "LIFX Mini White to Warm", new Features(TR_1500_9000)),
    PRODUCT_114(114, "LIFX Mini White to Warm", new Features(TR_1500_9000)),
    PRODUCT_115(115, "LIFX Switch", new Features(BUTTONS, RELAYS)),
    PRODUCT_116(116, "LIFX Switch", new Features(BUTTONS, RELAYS)),
    PRODUCT_117(117, "LIFX Z", new Features(TR_1500_9000, COLOR, EXTENDED_MULTIZONE, MULTIZONE)),
    PRODUCT_118(118, "LIFX Z", new Features(TR_1500_9000, COLOR, EXTENDED_MULTIZONE, MULTIZONE)),
    PRODUCT_119(119, "LIFX Beam", new Features(TR_1500_9000, COLOR, EXTENDED_MULTIZONE, MULTIZONE)),
    PRODUCT_120(120, "LIFX Beam", new Features(TR_1500_9000, COLOR, EXTENDED_MULTIZONE, MULTIZONE)),
    PRODUCT_121(121, "LIFX Downlight", new Features(TR_1500_9000, COLOR)),
    PRODUCT_122(122, "LIFX Downlight", new Features(TR_1500_9000, COLOR)),
    PRODUCT_123(123, "LIFX Color", new Features(TR_1500_9000, COLOR)),
    PRODUCT_124(124, "LIFX Color", new Features(TR_1500_9000, COLOR)),
    PRODUCT_125(125, "LIFX White to Warm", new Features(TR_1500_9000)),
    PRODUCT_126(126, "LIFX White to Warm", new Features(TR_1500_9000)),
    PRODUCT_127(127, "LIFX White", new Features(TR_2700_2700)),
    PRODUCT_128(128, "LIFX White", new Features(TR_2700_2700)),
    PRODUCT_129(129, "LIFX Color", new Features(TR_1500_9000, COLOR)),
    PRODUCT_130(130, "LIFX Color", new Features(TR_1500_9000, COLOR)),
    PRODUCT_131(131, "LIFX White to Warm", new Features(TR_1500_9000)),
    PRODUCT_132(132, "LIFX White to Warm", new Features(TR_1500_9000)),
    PRODUCT_133(133, "LIFX White", new Features(TR_2700_2700)),
    PRODUCT_134(134, "LIFX White", new Features(TR_2700_2700)),
    PRODUCT_135(135, "LIFX GU10", new Features(TR_1500_9000, COLOR)),
    PRODUCT_136(136, "LIFX GU10", new Features(TR_1500_9000, COLOR)),
    PRODUCT_137(137, "LIFX Candle", new Features(TR_1500_9000, COLOR, MATRIX)),
    PRODUCT_138(138, "LIFX Candle", new Features(TR_1500_9000, COLOR, MATRIX)),
    PRODUCT_141(141, "LIFX Neon", new Features(TR_1500_9000, COLOR, EXTENDED_MULTIZONE, MULTIZONE)),
    PRODUCT_142(142, "LIFX Neon", new Features(TR_1500_9000, COLOR, EXTENDED_MULTIZONE, MULTIZONE)),
    PRODUCT_143(143, "LIFX String", new Features(TR_1500_9000, COLOR, EXTENDED_MULTIZONE, MULTIZONE)),
    PRODUCT_144(144, "LIFX String", new Features(TR_1500_9000, COLOR, EXTENDED_MULTIZONE, MULTIZONE)),
    PRODUCT_161(161, "LIFX Outdoor Neon", new Features(TR_1500_9000, COLOR, EXTENDED_MULTIZONE, MULTIZONE)),
    PRODUCT_162(162, "LIFX Outdoor Neon", new Features(TR_1500_9000, COLOR, EXTENDED_MULTIZONE, MULTIZONE)),
    PRODUCT_163(163, "LIFX A19", new Features(TR_1500_9000, COLOR)),
    PRODUCT_164(164, "LIFX BR30", new Features(TR_1500_9000, COLOR)),
    PRODUCT_165(165, "LIFX A19", new Features(TR_1500_9000, COLOR)),
    PRODUCT_166(166, "LIFX BR30", new Features(TR_1500_9000, COLOR)),
    PRODUCT_167(167, "LIFX Downlight", new Features(TR_1500_9000, COLOR)),
    PRODUCT_168(168, "LIFX Downlight", new Features(TR_1500_9000, COLOR)),
    PRODUCT_169(169, "LIFX A21 1600lm", new Features(TR_1500_9000, COLOR)),
    PRODUCT_170(170, "LIFX A21 1600lm", new Features(TR_1500_9000, COLOR)),
    PRODUCT_171(171, "LIFX Round Spot", new Features(TR_1500_9000, COLOR, MATRIX)),
    PRODUCT_173(173, "LIFX Round Path", new Features(TR_1500_9000, COLOR, MATRIX)),
    PRODUCT_174(174, "LIFX Square Path", new Features(TR_1500_9000, COLOR, MATRIX)),
    PRODUCT_175(175, "LIFX PAR38", new Features(TR_1500_9000, COLOR)),
    PRODUCT_176(176, "LIFX Ceiling", new Features(TR_1500_9000, COLOR, MATRIX)),
    PRODUCT_177(177, "LIFX Ceiling", new Features(TR_1500_9000, COLOR, MATRIX)),
    PRODUCT_181(181, "LIFX Color", new Features(TR_1500_9000, COLOR)),
    PRODUCT_182(182, "LIFX Color", new Features(TR_1500_9000, COLOR)),
    PRODUCT_185(185, "LIFX Candle", new Features(TR_1500_9000, COLOR, MATRIX)),
    PRODUCT_186(186, "LIFX Candle", new Features(TR_1500_9000, COLOR, MATRIX)),
    PRODUCT_187(187, "LIFX Candle", new Features(TR_1500_9000, COLOR, MATRIX)),
    PRODUCT_188(188, "LIFX Candle", new Features(TR_1500_9000, COLOR, MATRIX)),
    PRODUCT_203(203, "LIFX String", new Features(TR_1500_9000, COLOR, EXTENDED_MULTIZONE, MULTIZONE)),
    PRODUCT_204(204, "LIFX String", new Features(TR_1500_9000, COLOR, EXTENDED_MULTIZONE, MULTIZONE)),
    PRODUCT_205(205, "LIFX Neon", new Features(TR_1500_9000, COLOR, EXTENDED_MULTIZONE, MULTIZONE)),
    PRODUCT_206(206, "LIFX Neon", new Features(TR_1500_9000, COLOR, EXTENDED_MULTIZONE, MULTIZONE));

    /**
     * Enumerates the product features.
     */
    public enum Feature {
        BUTTONS,
        CHAIN,
        COLOR,
        EXTENDED_MULTIZONE,
        HEV,
        INFRARED,
        MATRIX,
        MULTIZONE,
        RELAYS,
        TILE_EFFECT
    }

    /**
     * Enumerates the product vendors.
     */
    public enum Vendor {
        LIFX(1, "LIFX");

        private final int id;
        private final String name;

        Vendor(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getID() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Enumerates the color temperature ranges of lights.
     */
    public enum TemperatureRange {
        /**
         * When the temperature range is not defined for {@link Upgrade}s it is inherited from the most
         * recent firmware version.
         */
        NONE(0, 0),

        /**
         * 1500-4000K
         */
        TR_1500_4000(1500, 4000),

        /**
         * 1500-6500K
         */
        TR_1500_6500(1500, 6500),

        /**
         * 1500-9000K
         */
        TR_1500_9000(1500, 9000),

        /**
         * 2000-2000K
         */
        TR_2000_2000(2000, 2000),

        /**
         * 2100-2100K
         */
        TR_2100_2100(2100, 2100),

        /**
         * 2200-6500K
         */
        TR_2200_6500(2200, 6500),

        /**
         * 2500-9000K
         */
        TR_2500_9000(2500, 9000),

        /**
         * 2700-2700K
         */
        TR_2700_2700(2700, 2700),

        /**
         * 2700-6500K
         */
        TR_2700_6500(2700, 6500);

        private final int minimum;
        private final int maximum;

        TemperatureRange(int minimum, int maximum) {
            this.minimum = minimum;
            this.maximum = maximum;
        }

        /**
         * The minimum color temperature in degrees Kelvin.
         *
         * @return minimum color temperature (K)
         */
        public int getMinimum() {
            return minimum;
        }

        /**
         * The maxiumum color temperature in degrees Kelvin.
         *
         * @return maximum color temperature (K)
         */
        public int getMaximum() {
            return maximum;
        }

        /**
         * The color temperature range in degrees Kelvin.
         *
         * @return difference between maximum and minimum color temperature values
         */
        public int getRange() {
            return maximum - minimum;
        }
    }

    public static class Features {
        private TemperatureRange temperatureRange;
        private Set<Feature> features;

        private Features(Feature... features) {
            this(NONE, features);
        }

        private Features(Features other) {
            this(other.temperatureRange, other.features);
        }

        private Features(TemperatureRange temperatureRange, Feature... features) {
            this.temperatureRange = temperatureRange;
            this.features = Set.of(features);
        }

        private Features(TemperatureRange temperatureRange, Set<Feature> features) {
            this.temperatureRange = temperatureRange;
            this.features = Set.copyOf(features);
        }

        public TemperatureRange getTemperatureRange() {
            return temperatureRange;
        }

        public boolean hasFeature(Feature feature) {
            return features.contains(feature);
        }

        public void update(Features other) {
            temperatureRange = other.temperatureRange;
            features = other.features;
        }
    }

    static class Upgrade {
        final long major;
        final long minor;
        final Features features;

        private Upgrade(long major, long minor, Features features) {
            this.major = major;
            this.minor = minor;
            this.features = features;
        }
    }

    private final Vendor vendor;
    private final long id;
    private final String name;
    private final Features features;
    private final List<Upgrade> upgrades;

    private LifxProduct(long id, String name, Features features, Upgrade... upgrades) {
        this(LIFX, id, name, features, upgrades);
    }

    private LifxProduct(Vendor vendor, long id, String name, Features features, Upgrade... upgrades) {
        this.vendor = vendor;
        this.id = id;
        this.name = name;
        this.features = features;
        this.upgrades = List.of(upgrades);
    }

    @Override
    public String toString() {
        return name;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public long getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    /**
     * Returns the features of the initial product firmware version.
     *
     * @return the initial features
     */
    public Features getFeatures() {
        return new Features(features);
    }

    /**
     * Returns the features for a specific product firmware version.
     *
     * @param version the firmware version
     * @return the composite features of all firmware upgrades for the given major and minor firmware version
     */
    public Features getFeatures(String version) {
        if (upgrades.isEmpty() || !version.contains(".")) {
            return new Features(features);
        }

        String[] majorMinorVersion = version.split("\\.");
        long major = Long.valueOf(majorMinorVersion[0]);
        long minor = Long.valueOf(majorMinorVersion[1]);

        TemperatureRange temperatureRange = features.temperatureRange;
        Set<Feature> features = new HashSet<>(this.features.features);

        for (Upgrade upgrade : upgrades) {
            if (upgrade.major < major || (upgrade.major == major && upgrade.minor <= minor)) {
                Features upgradeFeatures = upgrade.features;
                if (upgradeFeatures.temperatureRange != NONE) {
                    temperatureRange = upgradeFeatures.temperatureRange;
                }
                features.addAll(upgradeFeatures.features);
            } else {
                break;
            }
        }

        return new Features(temperatureRange, features);
    }

    public ThingTypeUID getThingTypeUID() {
        if (hasFeature(COLOR)) {
            if (hasFeature(HEV)) {
                return LifxBindingConstants.THING_TYPE_COLORHEVLIGHT;
            } else if (hasFeature(INFRARED)) {
                return LifxBindingConstants.THING_TYPE_COLORIRLIGHT;
            } else if (hasFeature(MULTIZONE)) {
                return LifxBindingConstants.THING_TYPE_COLORMZLIGHT;
            } else if (hasFeature(TILE_EFFECT)) {
                return LifxBindingConstants.THING_TYPE_TILELIGHT;
            } else {
                return LifxBindingConstants.THING_TYPE_COLORLIGHT;
            }
        } else {
            return LifxBindingConstants.THING_TYPE_WHITELIGHT;
        }
    }

    private boolean hasFeature(Feature feature) {
        return features.hasFeature(feature);
    }

    /**
     * Returns a product that has the given thing type UID.
     *
     * @param uid a thing type UID
     * @return a product that has the given thing type UID
     * @throws IllegalArgumentException when <code>uid</code> is not a valid LIFX thing type UID
     */
    public static LifxProduct getLikelyProduct(ThingTypeUID uid) throws IllegalArgumentException {
        for (LifxProduct product : LifxProduct.values()) {
            if (product.getThingTypeUID().equals(uid)) {
                return product;
            }
        }

        throw new IllegalArgumentException(uid + " is not a valid product thing type UID");
    }

    /**
     * Returns the product that has the given product ID.
     *
     * @param id the product ID
     * @return the product that has the given product ID
     * @throws IllegalArgumentException when <code>id</code> is not a valid LIFX product ID
     */
    public static LifxProduct getProductFromProductID(long id) throws IllegalArgumentException {
        for (LifxProduct product : LifxProduct.values()) {
            if (product.id == id) {
                return product;
            }
        }

        throw new IllegalArgumentException(id + " is not a valid product ID");
    }

    List<Upgrade> getUpgrades() {
        return upgrades;
    }

    public boolean isLight() {
        return !features.hasFeature(Feature.BUTTONS) && !features.hasFeature(Feature.RELAYS);
    }
}
