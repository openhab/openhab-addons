;
(function() {
    'use strict';

    angular.module('PaperUI.control').component('itemCommandOptions', {
        bindings : {
            item : '<'
        },
        templateUrl : 'partials/control/component.control.commandOptions.html',
        controller : CommandOptionsController
    });

    CommandOptionsController.$inject = [ 'itemService' ];

    function CommandOptionsController(itemService) {
        var ctrl = this;
        this.commandOptions = [];

        this.sendCommand = sendCommand;

        this.$onInit = activate;

        function activate() {
            if (ctrl.item.commandDescription) {
                ctrl.commandOptions = ctrl.item.commandDescription.commandOptions;
            }
        }

        function sendCommand(command) {
            itemService.sendCommand({
                itemName : ctrl.item.name
            }, command);
        }
    }
})()
