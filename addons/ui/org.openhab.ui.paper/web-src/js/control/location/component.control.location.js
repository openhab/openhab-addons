;
(function() {
    'use strict';

    angular.module('PaperUI.control').component('locationControl', {
        bindings : {
            item : '<'
        },
        templateUrl : 'partials/control/location/component.control.location.html',
        controller : LocationItemController
    });

    LocationItemController.$inject = [ 'controlItemService' ];

    function LocationItemController(controlItemService) {
        var ctrl = this;
        this.formattedState;
        this.editMode = false;
        this.categories = [];

        this.editState = editState;
        this.updateState = updateState;
        this.getLabel = getLabel;
        this.onMapUpdate = onMapUpdate;

        this.$onChanges = onChanges;
        this.$onInit = activate;

        function onChanges(changes) {
            if (changes.item) {
                ctrl.item = angular.copy(this.item);
                ctrl.formattedState = updateFormattedState();
            }
        }

        function activate() {
            controlItemService.onStateChange(ctrl.item.name, function(stateObject) {
                ctrl.item.state = stateObject.value;
                controlItemService.updateStateText(ctrl.item);
                ctrl.formattedState = updateFormattedState();
            });
        }

        function updateFormattedState() {
            if (ctrl.item.state !== 'UNDEF' && ctrl.item.state !== 'NULL') {
                var location = ctrl.item.state.split(',');
                var latitude = parseFloat(location[0]);
                var longitude = parseFloat(location[1]);
                var height = parseFloat(location[2]);
                return latitude + '°N, ' + longitude + '°E' + (isNaN(height) ? "" : ', ' + height + 'm');
            } else {
                return '- °N, - °E, - m';
            }
        }

        function editState() {
            ctrl.editMode = true;
        }

        function updateState() {
            ctrl.editMode = false;
            controlItemService.sendCommand(ctrl.item, ctrl.item.state);
            ctrl.formattedState = updateFormattedState();
        }

        function getLabel(item, defaultLabel) {
            if (ctrl.item.name) {
                return ctrl.item.label;
            }

            if (ctrl.item.category) {
                var category = categories[ctrl.item.category];
                if (category) {
                    return category.label ? category.label : ctrl.item.category;
                }
            }

            return defaultLabel;
        }

        function onMapUpdate($event) {
            if ($event.location) {
                ctrl.item.state = $event.location;
                ctrl.updateState();
            }
        }
    }
})();