;
(function() {
    'use strict';

    angular.module('PaperUI.control').directive('hueColor', function() {
        return {
            restrict : 'A',
            link : function($scope, $element, attrs, ctrl) {
                attrs.$observe("hueColor", function(hue) {
                    var hsv = tinycolor({
                        h : hue,
                        s : 1,
                        v : 1
                    }).toHsv();
                    $element.find('.md-thumb').css('backgroundColor', tinycolor(hsv).toHexString());
                });
            }
        }
    });

})()
