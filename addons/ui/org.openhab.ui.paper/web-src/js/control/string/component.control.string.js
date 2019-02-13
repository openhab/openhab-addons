;
(function() {
    'use strict';

    angular.module('PaperUI.control').component('stringControl', {
        bindings : {
            item : '<'
        },
        templateUrl : 'partials/control/string/component.control.string.html',
        controller : StringItemController
    });

    StringItemController.$inject = [ 'controlItemService' ];

    function StringItemController(controlItemService) {
        var ctrl = this;
        var longEditMode = false;
        var editMode = false;

        this.getIcon = controlItemService.getIcon;
        this.getLabel = controlItemService.getLabel;
        this.isOptionList = controlItemService.isOptionList;
        this.isCommandOptions = isCommandOptions;

        this.editState = editState;
        this.updateState = updateState;
        this.inputType = inputType;

        this.$onInit = activate;

        function activate() {
            ctrl.item = angular.copy(ctrl.item);
            controlItemService.updateStateText(ctrl.item);
            ctrl.longEditMode = ctrl.item.stateText > 7;

            controlItemService.onStateChange(ctrl.item.name, function(stateObject) {
                ctrl.item.state = stateObject.value;
                controlItemService.updateStateText(ctrl.item);
                ctrl.longEditMode = ctrl.item.stateText > 7;
            });
        }

        function inputType() {
            return ctrl.item.groupType === 'Number' ? 'number' : '';
        }

        function editState() {
            ctrl.editMode = true;
        }

        function updateState() {
            controlItemService.sendCommand(ctrl.item, ctrl.item.state);
            ctrl.editMode = false;
        }

        function isCommandOptions() {
            var commandDescription = ctrl.item.commandDescription;
            return commandDescription && commandDescription.commandOptions && commandDescription.commandOptions.length > 0;
        }
    }

})()
