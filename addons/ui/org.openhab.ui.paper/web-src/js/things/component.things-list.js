;
(function() {
    'use strict';

    angular.module('PaperUI.things').component('thingsList', {
        templateUrl : 'partials/things/component.things-list.html',
        controller : ThingsListController

    });

    ThingsListController.$inject = [ '$scope', '$timeout', '$location', 'thingRepository', 'bindingRepository', 'titleService', 'eventService' ];

    function ThingsListController($scope, $timeout, $location, thingRepository, bindingRepository, titleService, eventService) {
        var ctrl = this;

        titleService.setTitle('Configuration');
        titleService.setSubtitles([ 'Things' ]);

        this.bindings = []; // used for the things filter
        this.things;

        this.refresh = refresh;
        this.navigateTo = navigateTo;
        this.clearAll = clearAll;

        this.$onInit = activate;

        function navigateTo(path) {
            if (path.startsWith("/")) {
                $location.path(path);
            } else {
                $location.path('configuration/things/' + path);
            }
        }

        function activate() {
            eventService.onEvent('smarthome/things/*/removed', function() {
                refresh();
            });

            refresh();
        }

        function refresh() {
            return thingRepository.getAll(function(things) {
                angular.forEach(things, function(thing) {
                    thing.bindingType = thing.thingTypeUID.split(':')[0];
                })
                ctrl.things = things;
                refreshBindings();
            });
        }

        function clearAll() {
            ctrl.searchText = "";
            $scope.$broadcast("ClearFilters");
        }

        function refreshBindings() {
            bindingRepository.getAll(function(bindings) {
                var filteredBindings = new Set();
                angular.forEach(ctrl.things, function(thing) {
                    var binding = bindings.filter(function(binding) {
                        return binding.id === thing.bindingType
                    })
                    filteredBindings.add(binding[0])
                })
                ctrl.bindings = Array.from(filteredBindings)
            }, true);
        }
    }

})()