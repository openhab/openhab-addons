import * as _ from 'lodash'
import {floors, objects, OBJECTS_SUFFIX} from './definitions'

export const GROUP_PREFIX = 'g';

export function addFloors(floor, model) {
    return [{
        type: 'Group',
        name: floor.abbr,
        label: floor.name || floor.value,
        category: model.itemsIcons ? floor.icon : '',
        groupNames: ['Home'],
        entryType: 'floor',
        tags: getTags(model, floor)
    }];
}

export function addRooms(floor, model) {
    let items = [];

    if (floor && floor.value && !_.isUndefined(model[floor.value])) {
        model[floor.value].forEach((room) => {
            let roomObjects = floor.value + '_' + room.value + OBJECTS_SUFFIX;
            let floorPrefix = model.floors.length > 1 ? floor.abbr + '_' : '';

            items.push({
                type: 'Group',
                name: floorPrefix + room.value,
                label: room.name || room.value,
                category: model.itemsIcons ? room.icon : '',
                groupNames: _.compact([
                    'Home',
                    model.floors.length > 1 ? floor.abbr : ''
                ]),
                entryType: 'room',
                tags: getTags(model, room)
            });

            items = [
                ...items,
                ...addObjects(room, model, floorPrefix, roomObjects)
            ];
        });
    }

    return items;
}

export function addObjects(room, model, floorPrefix, roomObjects) {
    let objectCollection = model[roomObjects] || [];

    if (!room.value && _.isEmpty(objectCollection)) {
        return [];
    }

    return objectCollection.map(object => ({
        type: _.first(object.type.split(':')),
        name: floorPrefix + room.value + '_' + object.value,
        label: object.name || object.value,
        category: model.itemsIcons ? object.icon : '',
        groupNames: [
            floorPrefix + room.value,
            GROUP_PREFIX + object.value
        ],
        tags: getTags(model, object),
        entryType: 'object'
    }));
}

/**
 * Generates a list of object groups
 *
 * @param {Object} model
 * @return {string}
 */
export function addObjectGroups(model) {
    let items = [];
    let chosenObjects = getChosenObjects(model);

    chosenObjects.forEach(function (dev) {
        let object = _.find(objects, {value: dev});

        if (object) {
            let type = object.type.split(':');
            let groupType = _.first(type);
            let groupFuncName = type[1] ? type[1].split('(')[0] : '';
            let groupFuncArgs = type[1] && type[1].split('(')[1] ? type[1].split('(')[1]
                .split(',')
                .map((arg) => arg.trim().replace(/\W/g, '')) : [];

            let item = {
                type: 'Group',
                name: GROUP_PREFIX + object.value,
                label: object.name || object.value,
                category: model.itemsIcons ? object.icon : '',
                groupNames: ['Home'],
                groupType: groupType,
                entryType: 'objectGroup',
                tags: getTags(model, object)
            };

            if (groupFuncName) {
                item = _.extend({}, item, {
                    function: {
                        name: groupFuncName
                    }
                });

                if (!_.isEmpty(groupFuncArgs)) {
                    item.function.params = groupFuncArgs;
                }
            }

            items.push(item);
        }
    });

    return _.uniq(items);
}

/**
 * Gets list of objects chosen
 * from collection
 *
 * @param {*} model
 * @return {Array}
 */
export function getChosenObjects(model) {
    return _.chain(model)
        .pickBy((value, key) => _.endsWith(key, OBJECTS_SUFFIX))
        .flatMap()
        .map((item) => item.value)
        .uniq()
        .value() || [];
}

function getTags(model, entry) {
    return model.itemsTags  ? entry.tags || [] : [];
}

export function getItems(model) {
    let items = [{
        type: 'Group',
        name: 'Home',
        label: model.homeName,
        category: model.itemsIcons ? 'house' : '',
        entryType: 'home',
        tags: model.itemsTags ? ['Building'] : []
    }];

    model.floors.forEach((floor) => {

        items = [
            ...items,
            ...addFloors(floor, model),
            ...addRooms(floor, model)
        ];
    });

    items = [
        ...items,
        ...addObjectGroups(model)
    ];

    return items;
}

/**
 * Returns array of Items
 * without `entryType` which is not a valid parameter
 * for the request
 *
 * @param {*} model
 */
export function generateItemsJson(model) {
    let items = getItems(model);
    return _.map(items, item => _.omit(item, ['entryType']));
}