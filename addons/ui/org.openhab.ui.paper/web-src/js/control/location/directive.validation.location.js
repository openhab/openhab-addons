;
(function() {
    'use strict';

    angular.module('PaperUI.control').directive('locationValidation', function() {
        return {
            restrict : 'A',
            require : 'ngModel',
            link : function(scope, elem, attr, ctrl) {

                var required = elem[0].required;

                // process each time the value is parsed into the model:
                ctrl.$parsers.unshift(function(value) {
                    var hasValue = value && value.length > 0;
                    var valid = (!hasValue && !required) || validateLocation(value);
                    ctrl.$setValidity('locationValidation', valid);
                    return valid ? value : undefined;
                });

                // process each time the value is updated on the DOM element.
                ctrl.$formatters.unshift(function(value) {
                    var hasValue = value && value.length > 0;
                    var valid = (!hasValue && !required) || validateLocation(value);
                    ctrl.$setValidity('locationValidation', valid);
                    // return the value or nothing will be written to the DOM.
                    return value;
                });
                
                function validateLocation(value) {
                    if (!value) {
                        return false;
                    }
                    var location = value.split(',');
                    if (location.length < 2 || location.length > 3) {
                        return false;
                    }
                    
                    var lat = location[0];
                    var long = location[1];
                    
                    return lat && long && lat >= -90 && lat <= 90 && long >= -180 && long <= 180; 
                }
            }
        }
    });
})()
