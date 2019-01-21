import * as _ from 'lodash'
import { floors, objects, OBJECTS_SUFFIX } from './definitions'
import { getChosenObjects } from './restItems'

/**
 * Generates a HABPanel's widget object
 * @param {*} options 
 */
function makeWidget(options) {
    let row = options.row > 0 ? options.row * 2 : 0;
    let widget = {
        item: options.item,
        name: options.name,
        sizeX: 2,
        sizeY: 2,
        type: options.type,
        row: row,
        col: options.col,
        font_size: '24',
        useserverformat: true
    };

    if (options.type === 'switch') {
        widget = _.extend({}, widget, {
            iconset: 'eclipse-smarthome-classic',
            icon: options.icon,
            icon_size: 64
        });
    } else {
        widget = _.extend({}, widget, {
            backdrop_iconset: 'eclipse-smarthome-classic',
            backdrop_icon: options.icon,
            backdrop_center: true
        });
    }

    return widget;
}

function getWidgetType(type) {
    let widget = '';
    switch (type) {
        case 'Dimmer':
            widget = 'slider';
            break;
        case 'Switch':
            widget = 'switch';
            break;
        default:
            widget ='dummy';
            break;
    }

    return widget;
}

/**
 * Generates an array widgets for specific Dashboard
 * @param {*} object 
 * @param {*} model 
 */
function makeWidgets(object, model) {
    let widgets = [];

    model.floors.forEach((floor) => {
        if (!_.isUndefined(model[floor.value])) {
            model[floor.value].forEach(function(room) {
                let roomObjects = floor.value + '_' + room.value + OBJECTS_SUFFIX;
                let objectCollection = model[roomObjects] || [];
                let obj = objectCollection.find(o => o.value === object.value);

                if (obj) {
                    widgets.push(makeWidget({
                        item: (model.floors.length > 1 ? floor.abbr + '_' : '') + room.value + '_' + obj.value,
                        name: room.name,
                        type: getWidgetType(_.first(object.type.split(':'))),
                        row: _.chunk(widgets, 6).length - 1,
                        col: (widgets.length * 2) % 12,
                        icon: object.icon
                    }));
                }
            });
        }

    });

    return widgets;
}

/**
 * Generates a full HABPanel dashboard set
 * @param {*} model 
 */
export function generateDashboard(model) {
    var chosenObjects = getChosenObjects(model);

    return chosenObjects.length ? chosenObjects.map((obj) => {
        let object = _.find(objects, { value: obj });
        return {
            id: object.value,
            name: object.name || object.value,
            row: 0,
            col: 0,
            tile: {
                backdrop_iconset: 'eclipse-smarthome-classic',
                backdrop_icon: object.icon,
                icon_size: 32
            },
            widgets: makeWidgets(object, model)
        }
    }) : '';
}

