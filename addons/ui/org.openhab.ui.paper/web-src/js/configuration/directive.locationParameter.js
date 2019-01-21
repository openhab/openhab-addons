;
(function() {
    'use strict';

    angular.module('PaperUI.directive.locationParameter', [ 'PaperUI.control' ]).component('locationParameter', {
        bindings : {
            model : '=',
            parameter : '=',
            form : '<'
        },
        templateUrl : 'partials/configuration/directive.locationParameter.html',
        controller : LocationParameterController

    });

    function LocationParameterController() {
        var ctrl = this;
        this.onMapUpdate = onMapUpdate;

        function onMapUpdate($event) {
            if ($event.location) {
                ctrl.model = $event.location;
            }
        }
    }

})()
