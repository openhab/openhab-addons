;
(function() {
    'use strict';

    angular.module('PaperUI.control').factory('controlItemService', ControlItemService);

    ControlItemService.$inject = [ '$timeout', 'itemService', 'eventService', 'util' ];

    function ControlItemService($timeout, itemService, eventService, util) {
        var categories = {
            'Alarm' : {},
            'Battery' : {},
            'Blinds' : {},
            'ColorLight' : {
                label : 'Color',
                icon : 'wb_incandescent'
            },
            'Contact' : {},
            'DimmableLight' : {
                label : 'Brightness',
                icon : 'wb_incandescent',
                showSwitch : true
            },
            'CarbonDioxide' : {
                label : 'CO2'
            },
            'Door' : {},
            'Energy' : {},
            'Fan' : {},
            'Fire' : {},
            'Flow' : {},
            'GarageDoor' : {},
            'Gas' : {},
            'Humidity' : {},
            'Light' : {},
            'Motion' : {},
            'MoveControl' : {},
            'Player' : {},
            'PowerOutlet' : {},
            'Pressure' : {},
            'Rain' : {},
            'Recorder' : {},
            'Smoke' : {},
            'SoundVolume' : {
                label : 'Volume',
                icon : 'volume_up'
            },
            'Switch' : {},
            'Temperature' : {
                label : 'Temperature'
            },
            'Water' : {},
            'Wind' : {},
            'Window' : {},
            'Zoom' : {},
        }

        return {
            sendCommand : sendCommand,
            getIcon : getIcon,
            getLabel : getLabel,
            isOptionList : isOptionList,
            showSwitch : showSwitch,
            updateStateText : updateStateText,
            onStateChange : onStateChange
        }

        function onStateChange(itemName, callback) {
            eventService.onEvent('smarthome/items/' + itemName + '/statechanged', function(topic, stateObject) {
                // trigger digest cycle using $timeout(...,0)
                $timeout(function() {
                    callback(stateObject);
                }, 0);
            });
        }

        function showSwitch(itemCategory) {
            if (itemCategory) {
                var category = categories[itemCategory];
                if (category) {
                    return category.showSwitch;
                }
            }
            return false;
        }

        function isOptionList(item) {
            return item && item.stateDescription && item.stateDescription.options.length > 0;
        }

        function getLabel(item, defaultLabel) {
            if (item.label) {
                return item.label;
            }

            if (item.category) {
                var category = categories[item.category];
                if (category) {
                    return category.label ? category.label : item.category;
                }
            }

            return defaultLabel;
        }

        function getIcon(itemCategory, fallbackIcon) {
            var defaultIcon = fallbackIcon ? fallbackIcon : 'radio_button_unchecked';

            if (itemCategory && categories[itemCategory] && categories[itemCategory].icon) {
                return categories[itemCategory].icon
            }

            return defaultIcon;
        }

        function updateStateText(item) {
            item.stateText = util.getItemStateText(item);
        }

        function sendCommand(item, command) {
            itemService.sendCommand({
                itemName : item.name
            }, command);
            updateStateText(item);
        }
    }

})()