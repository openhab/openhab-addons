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
        BACK_DOOR(2, "BackDoor"),
        BACK_YARD(3, "BackYard"),
        BALCONY(4, "Balcony"),
        BALLROOM(5, "Ballroom"),
        BATHROOM(6, "Bathroom"),
        BEDROOM(7, "Bedroom"),
        BORDER(8, "Border"),
        BOXROOM(9, "Boxroom"),
        BREAKFAST_ROOM(10, "BreakfastRoom"),
        CARPORT(11, "Carport"),
        CELLAR(12, "Cellar"),
        CLOAKROOM(13, "Cloakroom"),
        CLOSET(14, "Closet"),
        CONSERVATORY(15, "Conservatory"),
        CORRIDOR(16, "Corridor"),
        CRAFT_ROOM(17, "CraftRoom"),
        CUPBOARD(18, "Cupboard"),
        DECK(19, "Deck"),
        DEN(20, "Den"),
        DINING(21, "Dining"),
        DRAWING_ROOM(22, "DrawingRoom"),
        DRESSING_ROOM(23, "DressingRoom"),
        DRIVEWAY(24, "Driveway"),
        ELEVATOR(25, "Elevator"),
        ENSUITE(26, "Ensuite"),
        ENTRANCE(27, "Entrance"),
        ENTRYWAY(28, "Entryway"),
        FAMILY_ROOM(29, "FamilyRoom"),
        FOYER(30, "Foyer"),
        FRONT_DOOR(31, "FrontDoor"),
        FRONT_YARD(32, "FrontYard"),
        GAME_ROOM(33, "GameRoom"),
        GARAGE(34, "Garage"),
        GARAGE_DOOR(35, "GarageDoor"),
        GARDEN(36, "Garden"),
        GARDEN_DOOR(37, "GardenDoor"),
        GUEST_BATHROOM(38, "GuestBathroom"),
        GUEST_BEDROOM(39, "GuestBedroom"),
        RESERVED1(40, "Reserved1"),
        GUEST_ROOM(41, "GuestRoom"),
        GYM(42, "Gym"),
        HALLWAY(43, "Hallway"),
        HEARTH_ROOM(44, "HearthRoom"),
        KIDS_ROOM(45, "KidsRoom"),
        KIDS_BEDROOM(46, "KidsBedroom"),
        KITCHEN(47, "Kitchen"),
        RESERVED2(48, "Reserved2"),
        LAUNDRY_ROOM(49, "LaundryRoom"),
        LAWN(50, "Lawn"),
        LIBRARY(51, "Library"),
        LIVING_ROOM(52, "LivingRoom"),
        LOUNGE(53, "Lounge"),
        MEDIA_TV_ROOM(54, "MediaTvRoom"),
        MUD_ROOM(55, "MudRoom"),
        MUSIC_ROOM(56, "MusicRoom"),
        NURSERY(57, "Nursery"),
        OFFICE(58, "Office"),
        OUTDOOR_KITCHEN(59, "OutdoorKitchen"),
        OUTSIDE(60, "Outside"),
        PANTRY(61, "Pantry"),
        PARKING_LOT(62, "ParkingLot"),
        PARLOR(63, "Parlor"),
        PATIO(64, "Patio"),
        PLAY_ROOM(65, "PlayRoom"),
        POOL_ROOM(66, "PoolRoom"),
        PORCH(67, "Porch"),
        PRIMARY_BATHROOM(68, "PrimaryBathroom"),
        PRIMARY_BEDROOM(69, "PrimaryBedroom"),
        RAMP(70, "Ramp"),
        RECEPTION_ROOM(71, "ReceptionRoom"),
        RECREATION_ROOM(72, "RecreationRoom"),
        RESERVED3(73, "Reserved3"),
        ROOF(74, "Roof"),
        SAUNA(75, "Sauna"),
        SCULLERY(76, "Scullery"),
        SEWING_ROOM(77, "SewingRoom"),
        SHED(78, "Shed"),
        SIDE_DOOR(79, "SideDoor"),
        SIDE_YARD(80, "SideYard"),
        SITTING_ROOM(81, "SittingRoom"),
        SNUG(82, "Snug"),
        SPA(83, "Spa"),
        STAIRCASE(84, "Staircase"),
        STEAM_ROOM(85, "SteamRoom"),
        STORAGE_ROOM(86, "StorageRoom"),
        STUDIO(87, "Studio"),
        STUDY(88, "Study"),
        SUN_ROOM(89, "SunRoom"),
        SWIMMING_POOL(90, "SwimmingPool"),
        TERRACE(91, "Terrace"),
        UTILITY_ROOM(92, "UtilityRoom"),
        WARD(93, "Ward"),
        WORKSHOP(94, "Workshop"),
        TOILET(95, "Toilet");

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
        NORTH_EASTWARD(1, "NorthEastward"),
        EASTWARD(2, "Eastward"),
        SOUTH_EASTWARD(3, "SouthEastward"),
        SOUTHWARD(4, "Southward"),
        SOUTH_WESTWARD(5, "SouthWestward"),
        WESTWARD(6, "Westward"),
        NORTH_WESTWARD(7, "NorthWestward");

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
        NORTH_EAST(1, "NorthEast"),
        EAST(2, "East"),
        SOUTH_EAST(3, "SouthEast"),
        SOUTH(4, "South"),
        SOUTH_WEST(5, "SouthWest"),
        WEST(6, "West"),
        NORTH_WEST(7, "NorthWest");

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
        AIR_CONDITIONER(0, "AirConditioner"),
        AIR_PURIFIER(1, "AirPurifier"),
        BACK_DOOR(2, "BackDoor"),
        BAR_STOOL(3, "BarStool"),
        BATH_MAT(4, "BathMat"),
        BATHTUB(5, "Bathtub"),
        BED(6, "Bed"),
        BOOKSHELF(7, "Bookshelf"),
        CHAIR(8, "Chair"),
        CHRISTMAS_TREE(9, "ChristmasTree"),
        COAT_RACK(10, "CoatRack"),
        COFFEE_TABLE(11, "CoffeeTable"),
        COOKING_RANGE(12, "CookingRange"),
        COUCH(13, "Couch"),
        COUNTERTOP(14, "Countertop"),
        CRADLE(15, "Cradle"),
        CRIB(16, "Crib"),
        DESK(17, "Desk"),
        DINING_TABLE(18, "DiningTable"),
        DISHWASHER(19, "Dishwasher"),
        DOOR(20, "Door"),
        DRESSER(21, "Dresser"),
        LAUNDRY_DRYER(22, "LaundryDryer"),
        FAN(23, "Fan"),
        FIREPLACE(24, "Fireplace"),
        FREEZER(25, "Freezer"),
        FRONT_DOOR(26, "FrontDoor"),
        HIGH_CHAIR(27, "HighChair"),
        KITCHEN_ISLAND(28, "KitchenIsland"),
        LAMP(29, "Lamp"),
        LITTER_BOX(30, "LitterBox"),
        MIRROR(31, "Mirror"),
        NIGHTSTAND(32, "Nightstand"),
        OVEN(33, "Oven"),
        PET_BED(34, "PetBed"),
        PET_BOWL(35, "PetBowl"),
        PET_CRATE(36, "PetCrate"),
        REFRIGERATOR(37, "Refrigerator"),
        SCRATCHING_POST(38, "ScratchingPost"),
        SHOE_RACK(39, "ShoeRack"),
        SHOWER(40, "Shower"),
        SIDE_DOOR(41, "SideDoor"),
        SINK(42, "Sink"),
        SOFA(43, "Sofa"),
        STOVE(44, "Stove"),
        TABLE(45, "Table"),
        TOILET(46, "Toilet"),
        TRASH_CAN(47, "TrashCan"),
        LAUNDRY_WASHER(48, "LaundryWasher"),
        WINDOW(49, "Window"),
        WINE_COOLER(50, "WineCooler");

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
        LIGHT_DRY(1, "LightDry"),
        EXTRA_DRY(2, "ExtraDry"),
        NO_DRY(3, "NoDry");

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
        OUTSIDE(3, "Outside"),
        ZONE(4, "Zone");

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
        TEN(10, "Ten"),
        ELEVEN(11, "Eleven"),
        TWELVE(12, "Twelve"),
        THIRTEEN(13, "Thirteen"),
        FOURTEEN(14, "Fourteen"),
        FIFTEEN(15, "Fifteen"),
        SIXTEEN(16, "Sixteen"),
        SEVENTEEN(17, "Seventeen"),
        EIGHTEEN(18, "Eighteen"),
        NINETEEN(19, "Nineteen"),
        TWENTY(20, "Twenty"),
        TWENTY_ONE(21, "TwentyOne"),
        TWENTY_TWO(22, "TwentyTwo"),
        TWENTY_THREE(23, "TwentyThree"),
        TWENTY_FOUR(24, "TwentyFour"),
        TWENTY_FIVE(25, "TwentyFive"),
        TWENTY_SIX(26, "TwentySix"),
        TWENTY_SEVEN(27, "TwentySeven"),
        TWENTY_EIGHT(28, "TwentyEight"),
        TWENTY_NINE(29, "TwentyNine"),
        THIRTY(30, "Thirty");

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
        NEXT_TO(1, "NextTo"),
        AROUND(2, "Around"),
        ON(3, "On"),
        ABOVE(4, "Above"),
        FRONT_OF(5, "FrontOf"),
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
        ENTER_OK_SELECT(7, "EnterOkSelect"),
        CUSTOM(8, "Custom"),
        OPEN(9, "Open"),
        CLOSE(10, "Close"),
        STOP(11, "Stop");

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
