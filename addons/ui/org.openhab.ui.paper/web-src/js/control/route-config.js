;
(function() {
    'use strict';

    angular.module('PaperUI.control').config([ '$routeProvider', configure ]);

    function configure($routeProvider) {
        $routeProvider.when('/control', {
            templateUrl : 'partials/control/control.html',
            controller : 'ControlPageController',
            title : 'Control',
            simpleHeader : true,
            reloadOnSearch : false
        });
    }

})();