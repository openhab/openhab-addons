;
(function() {
    'use strict';

    angular.module('PaperUI.control').component('colorControl', {
        bindings : {
            item : '<'
        },
        templateUrl : 'partials/control/color/component.control.color.html',
        controller : ColorItemController
    });

    ColorItemController.$inject = [ 'controlItemService', '$timeout' ];

    function ColorItemController(controlItemService, $timeout) {
        var commandTimeout;
        var ctrl = this;

        // artificial state for color control
        this.state = {
            hue : '',
            saturation : '',
            brightness : ''
        }

        this.getIcon = controlItemService.getIcon;
        this.getLabel = controlItemService.getLabel;
        this.isOptionList = controlItemService.isOptionList;
        this.setBrightness = setBrightness;
        this.setColor = setColor;

        this.$onInit = activate;

        function activate() {
            ctrl.item = angular.copy(ctrl.item);
            updateColorState(ctrl.item.state);
            if (ctrl.item.state === 'UNDEF' || ctrl.item.state === 'NULL') {
                ctrl.item.state = '-';
            }

            controlItemService.onStateChange(ctrl.item.name, function(stateObject) {
                ctrl.item.state = stateObject.value;
                updateColorState(ctrl.item.state);
            });
        }

        function updateColorState(state) {
            var stateParts = state.split(",");
            if (stateParts.length == 3) {
                ctrl.state.hue = stateParts[0];
                ctrl.state.saturation = stateParts[1];
                ctrl.state.brightness = stateParts[2];
            } else {
                ctrl.state.hue = 0;
                ctrl.state.saturation = 0;
                ctrl.state.brightness = 0;
            }
        }

        function setBrightness() {
            sendCommand(ctrl.state.brightness);
        }

        function setColor() {
            sendCommand(toHSBState());
        }

        function toHSBState() {
            return Math.ceil(ctrl.state.hue) + ',' + Math.ceil(ctrl.state.saturation) + ',' + Math.ceil(ctrl.state.brightness);
        }

        function sendCommand(command) {
            if (commandTimeout) {
                $timeout.cancel(commandTimeout);
            }

            // send updates every 300 ms only
            commandTimeout = $timeout(function() {
                controlItemService.sendCommand(ctrl.item, command);
                commandTimeout = undefined;
            }, 300);
        }
    }

})()