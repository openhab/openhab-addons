;
(function() {
    'use strict';

    angular.module('PaperUI.bindings').config([ '$routeProvider', configure ]);

    function configure($routeProvider) {
        $routeProvider.when('/configuration/bindings', {
            template : '<bindings-list />'
        }).when('/configuration/bindings/:bindingId', {
            template : '<binding-detail />'
        })
    }

})();