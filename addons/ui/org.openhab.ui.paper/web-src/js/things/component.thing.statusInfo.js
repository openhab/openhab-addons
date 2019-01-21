;
(function() {
    'use strict';

    angular.module('PaperUI.things').component('thingStatus', {
        bindings : {
            statusInfo : '<'
        },
        templateUrl : 'partials/things/component.thing.statusInfo.html',
        controller : ThingStatusController
    });

    function ThingStatusController() {
        var ctrl = this;
        this.getThingStatus = getThingStatus;

        function getThingStatus() {
            var detail = ctrl.statusInfo.statusDetail;
            var fullStatus = ctrl.statusInfo.status + ((detail && detail !== 'NONE') ? ' - ' + detail : '');
            return fullStatus;
        }

    }
})()
