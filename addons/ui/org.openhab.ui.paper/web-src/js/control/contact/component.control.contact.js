;
(function() {
    'use strict';

    angular.module('PaperUI.control').component('contactControl', {
        bindings : {
            item : '<'
        },
        templateUrl : 'partials/control/contact/component.control.contact.html',
        controller : ContactItemController
    });

    ContactItemController.$inject = [ 'controlItemService' ];

    function ContactItemController(controlItemService) {
        var ctrl = this;

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

            controlItemService.onStateChange(ctrl.item.name, function(stateObject) {
                ctrl.item.state = stateObject.value;
                controlItemService.updateStateText(ctrl.item);
            });
        }

        function updateState() {
            controlItemService.sendCommand(ctrl.item, ctrl.item.state);
        }
    }

})()