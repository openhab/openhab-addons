;
(function() {
    'use strict';

    angular.module('PaperUI.items').config([ '$routeProvider', configure ]);

    function configure($routeProvider) {
        $routeProvider.when('/configuration/items', {
            templateUrl : 'partials/items/configuration.items.html',
            controller : 'ItemSetupController',
            title : 'Configuration'
        }).when('/configuration/item/edit/:itemName', {
            templateUrl : 'partials/items/item.config.html',
            controller : 'ItemConfigController'
        }).when('/configuration/item/create', {
            templateUrl : 'partials/items/item.config.html',
            controller : 'ItemConfigController',
            title : 'Create item'
        })
    }

})();