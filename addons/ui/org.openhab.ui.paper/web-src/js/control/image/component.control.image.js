;
(function() {
    'use strict';

    angular.module('PaperUI.control').component('imageControl', {
        bindings : {
            item : '='
        },
        templateUrl : 'partials/control/image/component.control.image.html',
        controller : ImageItemController
    });

    ImageItemController.$inject = [ 'controlItemService' ];

    function ImageItemController(controlItemService) {
        var ctrl = this;

        this.getLabel = controlItemService.getLabel;
        this.refreshCameraImage = refreshCameraImage;

        this.$onInit = activate;

        function activate() {
            ctrl.item = angular.copy(ctrl.item);

            ctrl.item.imageLoaded = true;

            controlItemService.onStateChange(ctrl.item.name, function(stateObject) {
                ctrl.item.state = stateObject.value;
                controlItemService.updateStateText(ctrl.item);
            });
        }

        function refreshCameraImage() {
            controlItemService.sendCommand(ctrl.item, "REFRESH");
        }
    }

})()
