angular.module('PaperUI.directive.searchField', []) //
.directive('searchField', function() {
    return {
        restrict : 'E',
        scope : {
            model : '=',
            placeholder : '=?'
        },
        templateUrl : 'partials/directives/directive.searchField.html',
        link : function(scope, element, attrs, controllers) {
            scope.placeholder = attrs.placeholder ? attrs.placeholder : 'Search'
            scope.clearSearchField = function(event) {
                if (!event || event.keyCode === 27) {
                    scope.model = '';
                }
            }
        }
    }
});