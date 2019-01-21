;
(function() {
    'use strict';

    angular.module('PaperUI.control').component('dimmerControl', {
        bindings : {
            item : '<'
        },
        templateUrl : 'partials/control/dimmer/component.control.dimmer.html',
        controller : DimmerItemController
    });

    DimmerItemController.$inject = [ 'controlItemService', '$timeout' ];

    function DimmerItemController(controlItemService, $timeout) {
        var commandTimeout;
        var ctrl = this;

        // artificial state for the additional switch control
        this.state = {
            switchState : ''
        }

        this.getIcon = controlItemService.getIcon;
        this.getLabel = controlItemService.getLabel;
        this.isOptionList = controlItemService.isOptionList;
        this.showSwitch = controlItemService.showSwitch;
        this.setBrightness = setBrightness;
        this.setSwitch = setSwitch;

        this.$onInit = activate;

        function activate() {
            ctrl.item = angular.copy(ctrl.item);
            updateSwitchState();
            if (ctrl.item.state === 'UNDEF' || ctrl.item.state === 'NULL') {
                ctrl.item.state = '-';
            }

            controlItemService.onStateChange(ctrl.item.name, function(stateObject) {
                if (stateObject.type === 'OnOff') {
                    return;
                }
                ctrl.item.state = stateObject.value;
                updateSwitchState();
                controlItemService.updateStateText(ctrl.item);
            });
        }

        function setSwitch() {
            controlItemService.sendCommand(ctrl.item, ctrl.state.switchState ? 'ON' : 'OFF');
        }

        function setBrightness() {
            if (commandTimeout) {
                $timeout.cancel(commandTimeout);
            }

            // send updates every 300 ms only
            commandTimeout = $timeout(function() {
                controlItemService.sendCommand(ctrl.item, ctrl.item.state);
                updateSwitchState(ctrl.item.state);
                commandTimeout = undefined;
            }, 300);
        }

        function updateSwitchState() {
            ctrl.state.switchState = ctrl.item.state > 0 ? true : false;
        }
    }

})()