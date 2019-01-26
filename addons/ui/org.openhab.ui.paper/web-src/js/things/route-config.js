;
(function() {
    'use strict';

    angular.module('PaperUI.things').config([ '$routeProvider', configure ]);

    function configure($routeProvider) {
        $routeProvider.when('/configuration/things', {
            template : '<things-list />',
            title : 'Configuration'
        }).when('/configuration/things/view/:thingUID', {
            templateUrl : 'partials/things/configuration.things.view.html',
            controller : 'ViewThingController',
            title : 'Configuration'
        }).when('/configuration/things/edit/:thingUID', {
            templateUrl : 'partials/things/configuration.things.html',
            controller : 'EditThingController',
            title : 'Configuration'
        })
    }

})();