/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

// AUTO-GENERATED, DO NOT EDIT!

package org.openhab.binding.matter.internal.client.dto.cluster.gen;

/**
 * Matter Semantic-Tag definitions.
 *
 * @author Dan Cunningham - Initial contribution
 */
public class SemanticTags {

    /**
     * Enumeration of all Tag namespaces defined by the Matter specification.
     */
    public enum Namespace {
        AREA(16, AreaTag.values()),
        CLOSURE(1, ClosureTag.values()),
        COMPASS_DIRECTION(2, CompassDirectionTag.values()),
        COMPASS_LOCATION(3, CompassLocationTag.values()),
        DIRECTION(4, DirectionTag.values()),
        ELECTRICAL_MEASUREMENT(10, ElectricalMeasurementTag.values()),
        LANDMARK(17, LandmarkTag.values()),
        LAUNDRY(14, LaundryTag.values()),
        LEVEL(5, LevelTag.values()),
        LOCATION(6, LocationTag.values()),
        NUMBER(7, NumberTag.values()),
        POSITION(8, PositionTag.values()),
        POWER_SOURCE(15, PowerSourceTag.values()),
        REFRIGERATOR(65, RefrigeratorTag.values()),
        RELATIVE_POSITION(18, RelativePositionTag.values()),
        ROOM_AIR_CONDITIONER(66, RoomAirConditionerTag.values()),
        SWITCHES(67, SwitchesTag.values());

        private final int id;
        private final BaseCluster.MatterEnum[] tags;

        Namespace(int id, BaseCluster.MatterEnum[] tags) {
            this.id = id;
            this.tags = tags;
        }

        /**
         * @return numeric namespace identifier as defined by the Matter specification
         */
        public int getId() {
            return id;
        }

        /**
         * @return array with all tags that belong to this namespace
         */
        public BaseCluster.MatterEnum[] getTags() {
            return tags;
        }
    }

    /**
     * Tags for the "Area" namespace.
     */
    public enum AreaTag implements BaseCluster.MatterEnum {
        AISLE(0, "Aisle"),
        ATTIC(1, "Attic"),
        BACK_DOOR(2, "Back Door"),
        BACK_YARD(3, "Back Yard"),
        BALCONY(4, "Balcony"),
        BALLROOM(5, "Ballroom"),
        BATHROOM(6, "Bathroom"),
        BEDROOM(7, "Bedroom"),
        BORDER(8, "Border"),
        BOXROOM(9, "Boxroom"),
        BREAKFAST_ROOM(10, "Breakfast Room"),
        CARPORT(11, "Carport"),
        CELLAR(12, "Cellar"),
        CLOAKROOM(13, "Cloakroom"),
        CLOSET(14, "Closet"),
        CONSERVATORY(15, "Conservatory"),
        CORRIDOR(16, "Corridor"),
        CRAFT_ROOM(17, "Craft Room"),
        CUPBOARD(18, "Cupboard"),
        DECK(19, "Deck"),
        DEN(20, "Den"),
        DINING(21, "Dining"),
        DRAWING_ROOM(22, "Drawing Room"),
        DRESSING_ROOM(23, "Dressing Room"),
        DRIVEWAY(24, "Driveway"),
        ELEVATOR(25, "Elevator"),
        ENSUITE(26, "Ensuite"),
        ENTRANCE(27, "Entrance"),
        ENTRYWAY(28, "Entryway"),
        FAMILY_ROOM(29, "Family Room"),
        FOYER(30, "Foyer"),
        FRONT_DOOR(31, "Front Door"),
        FRONT_YARD(32, "Front Yard"),
        GAME_ROOM(33, "Game Room"),
        GARAGE(34, "Garage"),
        GARAGE_DOOR(35, "Garage Door"),
        GARDEN(36, "Garden"),
        GARDEN_DOOR(37, "Garden Door"),
        GUEST_BATHROOM(38, "Guest Bathroom"),
        GUEST_BEDROOM(39, "Guest Bedroom"),
        GUEST_ROOM(40, "Guest Room"),
        GYM(41, "Gym"),
        HALLWAY(42, "Hallway"),
        HEARTH_ROOM(43, "Hearth Room"),
        KIDS_ROOM(44, "Kids Room"),
        KIDS_BEDROOM(45, "Kids Bedroom"),
        KITCHEN(46, "Kitchen"),
        LAUNDRY_ROOM(47, "Laundry Room"),
        LAWN(48, "Lawn"),
        LIBRARY(49, "Library"),
        LIVING_ROOM(50, "Living Room"),
        LOUNGE(51, "Lounge"),
        MEDIA_TV_ROOM(52, "Media/TV Room"),
        MUD_ROOM(53, "Mud Room"),
        MUSIC_ROOM(54, "Music Room"),
        NURSERY(55, "Nursery"),
        OFFICE(56, "Office"),
        OUTDOOR_KITCHEN(57, "Outdoor Kitchen"),
        OUTSIDE(58, "Outside"),
        PANTRY(59, "Pantry"),
        PARKING_LOT(60, "Parking Lot"),
        PARLOR(61, "Parlor"),
        PATIO(62, "Patio"),
        PLAY_ROOM(63, "Play Room"),
        POOL_ROOM(64, "Pool Room"),
        PORCH(65, "Porch"),
        PRIMARY_BATHROOM(66, "Primary Bathroom"),
        PRIMARY_BEDROOM(67, "Primary Bedroom"),
        RAMP(68, "Ramp"),
        RECEPTION_ROOM(69, "Reception Room"),
        RECREATION_ROOM(70, "Recreation Room"),
        ROOF(71, "Roof"),
        SAUNA(72, "Sauna"),
        SCULLERY(73, "Scullery"),
        SEWING_ROOM(74, "Sewing Room"),
        SHED(75, "Shed"),
        SIDE_DOOR(76, "Side Door"),
        SIDE_YARD(77, "Side Yard"),
        SITTING_ROOM(78, "Sitting Room"),
        SNUG(79, "Snug"),
        SPA(80, "Spa"),
        STAIRCASE(81, "Staircase"),
        STEAM_ROOM(82, "Steam Room"),
        STORAGE_ROOM(83, "Storage Room"),
        STUDIO(84, "Studio"),
        STUDY(85, "Study"),
        SUN_ROOM(86, "Sun Room"),
        SWIMMING_POOL(87, "Swimming Pool"),
        TERRACE(88, "Terrace"),
        TOILET(89, "Toilet"),
        UTILITY_ROOM(90, "Utility Room"),
        WARD(91, "Ward"),
        WORKSHOP(92, "Workshop");

        private final Integer value;
        private final String label;

        AreaTag(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * Tags for the "Closure" namespace.
     */
    public enum ClosureTag implements BaseCluster.MatterEnum {
        OPENING(0, "Opening"),
        CLOSING(1, "Closing"),
        STOP(2, "Stop");

        private final Integer value;
        private final String label;

        ClosureTag(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * Tags for the "CompassDirection" namespace.
     */
    public enum CompassDirectionTag implements BaseCluster.MatterEnum {
        NORTHWARD(0, "Northward"),
        NORTH_EASTWARD(1, "North-Eastward"),
        EASTWARD(2, "Eastward"),
        SOUTH_EASTWARD(3, "South-Eastward"),
        SOUTHWARD(4, "Southward"),
        SOUTH_WESTWARD(5, "South-Westward"),
        WESTWARD(6, "Westward"),
        NORTH_WESTWARD(7, "North-Westward");

        private final Integer value;
        private final String label;

        CompassDirectionTag(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * Tags for the "CompassLocation" namespace.
     */
    public enum CompassLocationTag implements BaseCluster.MatterEnum {
        NORTH(0, "North"),
        NORTH_EAST(1, "North-East"),
        EAST(2, "East"),
        SOUTH_EAST(3, "South-East"),
        SOUTH(4, "South"),
        SOUTH_WEST(5, "South-West"),
        WEST(6, "West"),
        NORTH_WEST(7, "North-West");

        private final Integer value;
        private final String label;

        CompassLocationTag(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * Tags for the "Direction" namespace.
     */
    public enum DirectionTag implements BaseCluster.MatterEnum {
        UPWARD(0, "Upward"),
        DOWNWARD(1, "Downward"),
        LEFTWARD(2, "Leftward"),
        RIGHTWARD(3, "Rightward"),
        FORWARD(4, "Forward"),
        BACKWARD(5, "Backward");

        private final Integer value;
        private final String label;

        DirectionTag(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * Tags for the "ElectricalMeasurement" namespace.
     */
    public enum ElectricalMeasurementTag implements BaseCluster.MatterEnum {
        DC(0, "DC"),
        AC(1, "AC"),
        AC_PHASE1(2, "ACPhase1"),
        AC_PHASE2(3, "ACPhase2"),
        AC_PHASE3(4, "ACPhase3");

        private final Integer value;
        private final String label;

        ElectricalMeasurementTag(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * Tags for the "Landmark" namespace.
     */
    public enum LandmarkTag implements BaseCluster.MatterEnum {
        AIR_CONDITIONER(0, "Air Conditioner"),
        AIR_PURIFIER(1, "Air Purifier"),
        BACK_DOOR(2, "Back Door"),
        BAR_STOOL(3, "Bar Stool"),
        BATH_MAT(4, "Bath Mat"),
        BATHTUB(5, "Bathtub"),
        BED(6, "Bed"),
        BOOKSHELF(7, "Bookshelf"),
        CHAIR(8, "Chair"),
        CHRISTMAS_TREE(9, "Christmas Tree"),
        COAT_RACK(10, "Coat Rack"),
        COFFEE_TABLE(11, "Coffee Table"),
        COOKING_RANGE(12, "Cooking Range"),
        COUCH(13, "Couch"),
        COUNTERTOP(14, "Countertop"),
        CRADLE(15, "Cradle"),
        CRIB(16, "Crib"),
        DESK(17, "Desk"),
        DINING_TABLE(18, "Dining Table"),
        DISHWASHER(19, "Dishwasher"),
        DOOR(20, "Door"),
        DRESSER(21, "Dresser"),
        LAUNDRY_DRYER(22, "Laundry Dryer"),
        FAN(23, "Fan"),
        FIREPLACE(24, "Fireplace"),
        FREEZER(25, "Freezer"),
        FRONT_DOOR(26, "Front Door"),
        HIGH_CHAIR(27, "High Chair"),
        KITCHEN_ISLAND(28, "Kitchen Island"),
        LAMP(29, "Lamp"),
        LITTER_BOX(30, "Litter Box"),
        MIRROR(31, "Mirror"),
        NIGHTSTAND(32, "Nightstand"),
        OVEN(33, "Oven"),
        PET_BED(34, "Pet Bed"),
        PET_BOWL(35, "Pet Bowl"),
        PET_CRATE(36, "Pet Crate"),
        REFRIGERATOR(37, "Refrigerator"),
        SCRATCHING_POST(38, "Scratching Post"),
        SHOE_RACK(39, "Shoe Rack"),
        SHOWER(40, "Shower"),
        SIDE_DOOR(41, "Side Door"),
        SINK(42, "Sink"),
        SOFA(43, "Sofa"),
        STOVE(44, "Stove"),
        TABLE(45, "Table"),
        TOILET(46, "Toilet"),
        TRASH_CAN(47, "Trash Can"),
        LAUNDRY_WASHER(48, "Laundry Washer"),
        WINDOW(49, "Window"),
        WINE_COOLER(50, "Wine Cooler");

        private final Integer value;
        private final String label;

        LandmarkTag(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * Tags for the "Laundry" namespace.
     */
    public enum LaundryTag implements BaseCluster.MatterEnum {
        NORMAL(0, "Normal"),
        LIGHT_DRY(1, "Light Dry"),
        EXTRA_DRY(2, "Extra Dry"),
        NO_DRY(3, "No Dry");

        private final Integer value;
        private final String label;

        LaundryTag(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * Tags for the "Level" namespace.
     */
    public enum LevelTag implements BaseCluster.MatterEnum {
        LOW(0, "Low"),
        MEDIUM(1, "Medium"),
        HIGH(2, "High");

        private final Integer value;
        private final String label;

        LevelTag(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * Tags for the "Location" namespace.
     */
    public enum LocationTag implements BaseCluster.MatterEnum {
        INDOOR(0, "Indoor"),
        OUTDOOR(1, "Outdoor"),
        INSIDE(2, "Inside"),
        OUTSIDE(3, "Outside");

        private final Integer value;
        private final String label;

        LocationTag(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * Tags for the "Number" namespace.
     */
    public enum NumberTag implements BaseCluster.MatterEnum {
        ZERO(0, "Zero"),
        ONE(1, "One"),
        TWO(2, "Two"),
        THREE(3, "Three"),
        FOUR(4, "Four"),
        FIVE(5, "Five"),
        SIX(6, "Six"),
        SEVEN(7, "Seven"),
        EIGHT(8, "Eight"),
        NINE(9, "Nine"),
        TEN(10, "Ten");

        private final Integer value;
        private final String label;

        NumberTag(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * Tags for the "Position" namespace.
     */
    public enum PositionTag implements BaseCluster.MatterEnum {
        LEFT(0, "Left"),
        RIGHT(1, "Right"),
        TOP(2, "Top"),
        BOTTOM(3, "Bottom"),
        MIDDLE(4, "Middle"),
        ROW(5, "Row"),
        COLUMN(6, "Column");

        private final Integer value;
        private final String label;

        PositionTag(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * Tags for the "PowerSource" namespace.
     */
    public enum PowerSourceTag implements BaseCluster.MatterEnum {
        UNKNOWN(0, "Unknown"),
        GRID(1, "Grid"),
        SOLAR(2, "Solar"),
        BATTERY(3, "Battery"),
        EV(4, "Ev");

        private final Integer value;
        private final String label;

        PowerSourceTag(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * Tags for the "Refrigerator" namespace.
     */
    public enum RefrigeratorTag implements BaseCluster.MatterEnum {
        REFRIGERATOR(0, "Refrigerator"),
        FREEZER(1, "Freezer");

        private final Integer value;
        private final String label;

        RefrigeratorTag(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * Tags for the "RelativePosition" namespace.
     */
    public enum RelativePositionTag implements BaseCluster.MatterEnum {
        UNDER(0, "Under"),
        NEXT_TO(1, "Next To"),
        AROUND(2, "Around"),
        ON(3, "On"),
        ABOVE(4, "Above"),
        FRONT_OF(5, "Front Of"),
        BEHIND(6, "Behind");

        private final Integer value;
        private final String label;

        RelativePositionTag(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * Tags for the "RoomAirConditioner" namespace.
     */
    public enum RoomAirConditionerTag implements BaseCluster.MatterEnum {
        EVAPORATOR(0, "Evaporator"),
        CONDENSER(1, "Condenser");

        private final Integer value;
        private final String label;

        RoomAirConditionerTag(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * Tags for the "Switches" namespace.
     */
    public enum SwitchesTag implements BaseCluster.MatterEnum {
        ON(0, "On"),
        OFF(1, "Off"),
        TOGGLE(2, "Toggle"),
        UP(3, "Up"),
        DOWN(4, "Down"),
        NEXT(5, "Next"),
        PREVIOUS(6, "Previous"),
        ENTER_OK_SELECT(7, "Enter/OK/Select"),
        CUSTOM(8, "Custom");

        private final Integer value;
        private final String label;

        SwitchesTag(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }
}
