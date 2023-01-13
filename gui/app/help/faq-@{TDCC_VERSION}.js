angular.module('main').config(['$routeProvider', function($routeProvider) {
    $routeProvider
        .when('/faq', {
            templateUrl: 'help/faq-@{TDCC_VERSION}.html',
            controller: 'FaqCtrl'
        });
}])

.controller('FaqCtrl', function(){});


