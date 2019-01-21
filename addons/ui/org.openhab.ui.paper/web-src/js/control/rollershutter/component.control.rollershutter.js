;
(function() {
    'use strict';

    angular.module('PaperUI.control').component('rollershutterControl', {
        bindings : {
            item : '<'
        },
        templateUrl : 'partials/control/rollershutter/component.control.rollershutter.html',
        controller : RollershutterItemController
    });

    RollershutterItemController.$inject = [ 'controlItemService' ];

    function RollershutterItemController(controlItemService) {
        var ctrl = this;
        this.editMode = false;

        this.editState = editState;
        this.updateState = updateState;
        this.sendCommand = sendCommand;
        this.getIcon = controlItemService.getIcon;
        this.getLabel = controlItemService.getLabel;
        this.isOptionList = controlItemService.isOptionList;

        this.$onInit = activate;

        function activate() {
            ctrl.item = angular.copy(ctrl.item);
            if (ctrl.item.state === 'UNDEF' || ctrl.item.state === 'NULL') {
                ctrl.item.state = '-';
            } else {
                ctrl.item.state = parseInt(ctrl.item.state);
            }

            controlItemService.updateStateText(ctrl.item);
            controlItemService.onStateChange(ctrl.item.name, function(stateObject) {
                ctrl.item.state = parseInt(stateObject.value);
                controlItemService.updateStateText(ctrl.item);
            });
        }

        function sendCommand(command) {
            controlItemService.sendCommand(ctrl.item, command);
        }

        function editState() {
            ctrl.editMode = true;
        }

        function updateState() {
            controlItemService.sendCommand(ctrl.item, ctrl.item.state);
            ctrl.editMode = false;
        }
    }

})()