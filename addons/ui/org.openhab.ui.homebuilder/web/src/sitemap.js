import * as _ from 'lodash'
import * as s from 'underscore.string'
import { floors } from './definitions'
import { getChosenObjects, getItems, GROUP_PREFIX } from './restItems'

export let sitemapName = '';

function getFloorItem(floor, model) {
    let floorFrame = 'Frame ';

    if (model.floors.length > 1) {
        let icon = model.itemsIcons && floor.icon ? '" icon="' + floor.icon : '';
        floorFrame += 'label="' + (floor.name || floor.value) + icon + '" ';
    }

    return floorFrame + '{';
}

function getRoomGroups(floor, model, floorPrefix) {
    if (floor && floor.value && !_.isUndefined(model[floor.value])) {
        return model[floor.value].map(room =>
            s.pad(' ', 8) + 'Group item=' + floorPrefix + room.value
        );
    }

    return [];
}

function addFloorFrames(model) {
    let lines = [];

    model.floors.forEach((floor) => {
        let floorPrefix = '';

        if (model.floors.length > 1) {
            floorPrefix = floor.abbr + '_';
        }

        lines = [
            ...lines,
            s.pad(' ', 4) + getFloorItem(floor, model),
            ...getRoomGroups(floor, model, floorPrefix),
            s.pad(' ', 4) + '}'
        ];

        lines.push('');
    });

    return lines;
}

function getTextGroup(group) {
    let textGroup = `Text label="${group.label}"`;
    textGroup += group.category && group.category !== 'none' ? ` icon="${group.category}" {` : ` {`;
    return textGroup;
}

function getDefaultItems(groupItems, items) {
    return groupItems.map(item => {
        let room = _.find(items, { name: item.groupNames[0] });
        return s.pad(' ', 12) + `Default item=${item.name} label="${room.label}"`;
    });
}

function getObjectItems(model) {
    const items = getItems(model);
    let chosenObjects = getChosenObjects(model);
    let objects = chosenObjects.map((object, index) => {
        let groupName = GROUP_PREFIX + object;
        let group = _.find(items, { name: groupName });
        let groupItems = _.filter(items, item =>
            item.groupNames && item.groupNames.includes(groupName)
        );

        let result = [
            s.pad(' ', 8) + getTextGroup(group),
            ...getDefaultItems(groupItems, items),
            s.pad(' ', 8) + '}'
        ];

        if (index < chosenObjects.length - 1) {
            result.push('');
        }

        return result;
    });

    return _.flatten(objects);
}

function addObjectsFrame(model) {
    let objectItems = getObjectItems(model);

    if (objectItems.length) {
        return [
            s.pad(' ', 4) + 'Frame {',
            ...objectItems,
            s.pad(' ', 4) + '}'
        ];
    }

    return [];
}

export function generateSitemap(model) {
    sitemapName = s(model.homeName)
        .slugify()
        .value()
        .replace(/-/g, '_');

    return [
        'sitemap ' + sitemapName + ' label="' + model.homeName + '" {',
        ...addFloorFrames(model),
        ...addObjectsFrame(model),
        '}'
    ].join('\n');
}
