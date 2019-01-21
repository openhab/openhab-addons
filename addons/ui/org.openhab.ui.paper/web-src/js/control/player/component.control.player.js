;
(function() {
    'use strict';

    angular.module('PaperUI.control').component('playerControl', {
        bindings : {
            item : '<'
        },
        templateUrl : 'partials/control/player/component.control.player.html',
        controller : PlayerItemController
    });

    PlayerItemController.$inject = [ 'controlItemService', '$timeout' ];

    function PlayerItemController(controlItemService, $timeout) {
        var ctrl = this;

        this.isInterrupted;
        this.time;

        this.sendCommand = sendCommand;
        this.onPrevDown = onPrevDown;
        this.onPrevUp = onPrevUp;
        this.onNextDown = onNextDown;
        this.onNextUp = onNextUp;

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

        function onPrevDown() {
            ctrl.isInterrupted = false;
            ctrl.time = new Date().getTime();
            $timeout(function() {
                if (!ctrl.isInterrupted) {
                    ctrl.sendCommand('REWIND', false);
                }
            }, 300);
        }

        function onPrevUp() {
            var newTime = new Date().getTime();
            if (ctrl.time + 300 > newTime) {
                ctrl.isInterrupted = true;
                ctrl.sendCommand('PREVIOUS', false);
            } else {
                $timeout(function() {
                    ctrl.sendCommand('PLAY', false);
                });
            }
        }

        function onNextDown() {
            ctrl.isInterrupted = false;
            ctrl.time = new Date().getTime();
            $timeout(function() {
                if (!ctrl.isInterrupted) {
                    ctrl.sendCommand('FASTFORWARD', false);
                }
            }, 300);
        }

        function onNextUp() {
            var newTime = new Date().getTime();
            if (ctrl.time + 300 > newTime) {
                ctrl.isInterrupted = true;
                ctrl.sendCommand('NEXT', false);
            } else {
                $timeout(function() {
                    ctrl.sendCommand('PLAY', false);
                });
            }
        }

        function sendCommand(command, updateState) {
            controlItemService.sendCommand(ctrl.item, command);
            if (updateState) {
                ctrl.item.state = command;
            }
        }
    }

})()