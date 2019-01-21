;
(function() {
    'use strict';

    angular.module('PaperUI.control').component('numberControl', {
        bindings : {
            item : '<'
        },
        templateUrl : 'partials/control/number/component.control.number.html',
        controller : NumberItemController
    });

    NumberItemController.$inject = [ 'controlItemService' ];

    function NumberItemController(controlItemService) {
        var ctrl = this;
        this.editMode = false;

        this.editState = editState;
        this.updateState = updateState;
        this.getIcon = controlItemService.getIcon;
        this.getLabel = controlItemService.getLabel;
        this.isOptionList = controlItemService.isOptionList;

        this.$onInit = activate;

        function activate() {
            ctrl.item = angular.copy(ctrl.item);
            if (ctrl.item.state === 'UNDEF' || ctrl.item.state === 'NULL') {
                ctrl.item.state = '-';
            }

            updateItemState(ctrl.item.state);
            controlItemService.onStateChange(ctrl.item.name, function(stateObject) {
                var state = stateObject.value;
                updateItemState(state);
            });
        }

        function editState() {
            ctrl.editMode = true;
        }

        function updateState() {
            var state = ctrl.item.unit ? ctrl.item.state + ' ' + ctrl.item.unit : ctrl.item.state;
            controlItemService.sendCommand(ctrl.item, state);
            ctrl.editMode = false;
        }

        function updateItemState(state) {
            var strState = '' + state;
            if (strState.indexOf(' ') > 0) {
                ctrl.item.unit = strState.substring(strState.indexOf(' ') + 1);
                state = strState.substring(0, strState.indexOf(' '));
            }
            var parsedValue = Number(state);
            if (!isNaN(parsedValue)) {
                state = parsedValue;
            }
            ctrl.item.state = state;
            controlItemService.updateStateText(ctrl.item);
        }

    }

})()