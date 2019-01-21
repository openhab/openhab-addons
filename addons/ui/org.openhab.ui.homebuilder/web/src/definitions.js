/**
 * i18n definitions
 */
export var languages = [
    {name: 'Czech', id: 'cs-CZ'},
    {name: 'Dutch', id: 'nl-NL'},
    {name: 'English', id: 'en-UK'},
    {name: 'French', id: 'fr-FR'},
    {name: 'German', id: 'de-DE'},
    {name: 'Hungarian', id: 'hu-HU'},
    {name: 'Italian', id: 'it-IT'},
    {name: 'Korean', id: 'ko-KR'},
    {name: 'Polish', id: 'pl-PL'},
    {name: 'Portuguese (BR)', id: 'pt-BR'},
    {name: 'Russian', id: 'ru-RU'},
    {name: 'Spanish', id: 'es-ES'},
    {name: 'Tagalog (Philippines)', id: 'tl-PH'},
    {name: 'Turkish', id: 'tr-TR'}
];

/**
 * Structure definitions
 */
export var floors = [
    {abbr: 'C', value: 'Cellar', icon: 'cellar', tags: ['Basement']},
    {abbr: 'OU', value: 'Outside', icon: 'garden', tags: ['Outdoor']},
    {abbr: 'GF', value: 'GroundFloor', icon: 'groundfloor', tags: ['GroundFloor']},
    {abbr: 'FF', value: 'FirstFloor', icon: 'firstfloor', tags: ['FirstFloor']},
    {abbr: 'F2', value: 'SecondFloor', icon: 'attic', tags: ['Attic']},
    {abbr: 'F3', value: 'ThirdFloor', icon: 'attic', tags: ['Attic']},
    {abbr: 'F4', value: 'FourthFloor', icon: 'attic', tags: ['Attic']},
    {abbr: 'AT', value: 'Attic', icon: 'attic', tags: ['Attic']}
];

export var rooms = [
    {value: 'Balcony', icon: '', tags: ['Outdoor']},
    {value: 'Backyard', icon: 'lawnmower', tags: ['Garden']},
    {value: 'Basement', icon: 'cellar', tags: ['Basement']},
    {value: 'Bathroom', icon: 'bath', tags: ['Bathroom']},
    {value: 'Bedroom', icon: 'bedroom', tags: ['Bedroom']},
    {value: 'Boiler', icon: 'gas', tags: ['Room']},
    {value: 'Wardrobe', icon: 'wardrobe', tags: []},
    {value: 'Cellar', icon: 'cellar', tags: ['Cellar']},
    {value: 'Corridor', icon: 'corridor', tags: ['Corridor']},
    {value: 'Deck', icon: '', tags: ['Terrace']},
    {value: 'Dining', icon: '', tags: ['Room']},
    {value: 'Driveway', icon: '', tags: ['Outdoor']},
    {value: 'Entryway', icon: 'frontdoor', tags: ['Room']},
    {value: 'FamilyRoom', icon: 'parents_2_4', tags: ['Room']},
    {value: 'FrontYard', icon: 'lawnmower', tags: ['Garden']},
    {value: 'Garage', icon: 'garage', tags: ['Garage']},
    {value: 'GuestHouse', icon: 'house', tags: ['House']},
    {value: 'GuestRoom', icon: 'parents_4_3', tags: ['Room']},
    {value: 'Hallway', icon: 'corridor', tags: ['Corridor']},
    {value: 'HomeCinema', icon: 'screen', tags: ['Room']},
    {value: 'KidsRoom', icon: 'girl_3', tags: ['Room']},
    {value: 'Kitchen', icon: 'kitchen', tags: ['Kitchen']},
    {value: 'LaundryRoom', icon: 'washingmachine', tags: ['Room']},
    {value: 'Library', icon: 'office', tags: ['Room']},
    {value: 'LivingRoom', icon: 'sofa', tags: ['LivingRoom']},
    {value: 'LivingDining', icon: 'sofa', tags: ['LivingRoom']},
    {value: 'Loft', icon: 'attic', tags: ['Room']},
    {value: 'Lounge', icon: 'sofa', tags: ['Room']},
    {value: 'MasterBedroom', icon: 'bedroom_red', tags: ['Bedroom']},
    {value: 'NannyRoom', icon: 'woman_1', tags: ['Room']},
    {value: 'Office', icon: 'office', tags: ['Room']},
    {value: 'Outside', icon: 'garden', tags: ['Outside']},
    {value: 'Patio', icon: 'terrace', tags: ['Outside']},
    {value: 'Porch', icon: 'group', tags: ['Outside']},
    {value: 'Stairwell', icon: 'qualityofservice', tags: []},
    {value: 'StorageRoom', icon: 'suitcase', tags: ['Room']},
    {value: 'Studio', icon: 'pantry', tags: ['Room']},
    {value: 'Shed', icon: 'greenhouse', tags: ['Garage']},
    {value: 'Toilet', icon: 'toilet', tags: ['Bathroom']},
    {value: 'Terrace', icon: 'terrace', tags: ['Terrace']}
];

/**
 * Collection of objects (sensors, smart devices etc.) controllable by openHAB.
 */
export var objects = [
    {value: 'Light', icon: 'light', type: 'Switch:OR(ON, OFF)', unit: '[(%d)]', tags: ['Lighting', 'Switchable']},
    {value: 'Window', icon: 'window', type: 'Contact:OR(OPEN, CLOSED)', unit: '[MAP(en.map):%s]', tags: ['Window']},
    {value: 'Door', icon: 'door', type: 'Contact:OR(OPEN, CLOSED)', unit: '[MAP(en.map):%s]', tags: ['Door']},
    {value: 'Motion', icon: 'motion', type: 'Switch:OR(ON, OFF)', unit: '[(%d)]', tags: ['MotionDetector', 'Switchable']},
    {value: 'Power', icon: 'poweroutlet', type: 'Switch:OR(ON, OFF)', unit: '[(%d)]', tags: ['Switch', 'Switchable']},
    {value: 'Shutter', icon: 'rollershutter', type: 'Rollershutter:OR(UP, DOWN)', unit: '[(%d)]', tags: ['Rollershutter']},
    {value: 'Blind', icon: 'blinds', type: 'Dimmer', unit: '[%d %%]', tags: ['Blinds', 'Switchable']},
    {value: 'Fan', icon: 'fan_ceiling', type: 'Switch:OR(ON, OFF)', unit: '[(%d)]', tags: ['Switchable']},
    {value: 'AirCon', icon: 'snow', type: 'Switch:OR(ON, OFF)', unit: '[(%d)]', tags: ['HVAC', 'Switchable']},
    {value: 'Heating', icon: 'heating', type: 'Number:AVG', unit: '[%.1f °C]', tags: ['HVAC']},
    {value: 'Temperature', icon: 'temperature', type: 'Number:AVG', unit: '[%.1f °C]', tags: ['Temperature']},
    {value: 'Humidity', icon: 'humidity', type: 'Number:AVG', unit: '[%d %%]', tags: ['Humidity']}
];

export const OBJECTS_SUFFIX = '_objects';

