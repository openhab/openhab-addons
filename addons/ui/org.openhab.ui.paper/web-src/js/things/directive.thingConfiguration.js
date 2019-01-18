;
(function() {
    'use strict';

    angular.module('PaperUI.things').directive('thingConfiguration', ThingConfiguration);

    function ThingConfiguration() {
        return {
            restrict : 'E',
            scope : {},
            bindToController : {
                thing : '=',
                isEditing : '=?',
                form : '=?'
            },
            controller : ThingConfigurationController,
            controllerAs : '$ctrl',
            templateUrl : 'partials/things/directive.thingConfiguration.html'
        }
    }

    ThingConfigurationController.$inject = [ '$q', '$location', 'thingTypeService', 'thingRepository' ];

    function ThingConfigurationController($q, $location, thingTypeService, thingRepository) {
        var ctrl = this;

        this.bridges = [];
        this.supportedBridgeTypeUIDs = [];

        this.needsBridge = needsBridge;
        this.hasBridge = hasBridge;
        this.createBridge = createBridge;

        this.$onInit = activate;

        function activate() {
            return $q(function() {
                if (ctrl.thing.thingTypeUID) {
                    getThingType(ctrl.thing.thingTypeUID);
                }
            });
        }

        function needsBridge() {
            return ctrl.supportedBridgeTypeUIDs && ctrl.supportedBridgeTypeUIDs.length > 0;
        }

        function hasBridge() {
            return ctrl.bridges && ctrl.bridges.length > 0;
        }

        function createBridge() {
            var bridgeTypeUID = ctrl.supportedBridgeTypeUIDs[0];
            if (ctrl.supportedBridgeTypeUIDs.length > 1) {
                var bindingId = bridgeTypeUID.substring(0, bridgeTypeUID.indexOf(':'));
                $location.path('inbox/setup/thing-types/' + bindingId);
            } else {
                $location.path('inbox/setup/add/' + bridgeTypeUID);
            }
        }

        function refreshBridges(supportedBridgeTypeUIDs) {
            thingRepository.getAll(function(things) {
                ctrl.bridges = things.filter(function(thing) {
                    return supportedBridgeTypeUIDs.includes(thing.thingTypeUID)
                })
            });
        }

        function getThingType(thingTypeUID) {
            thingTypeService.getByUid({
                thingTypeUID : thingTypeUID
            }).$promise.then(function(thingType) {
                if (thingType.supportedBridgeTypeUIDs && thingType.supportedBridgeTypeUIDs.length > 0) {
                    ctrl.supportedBridgeTypeUIDs = thingType.supportedBridgeTypeUIDs;
                    refreshBridges(thingType.supportedBridgeTypeUIDs);
                }
            });
        }
    }

})();
