angular.module('main')

.config(['$routeProvider', function($routeProvider) {
    $routeProvider
        .when('/contact', {
            templateUrl: 'contact/contact-@{TDCC_VERSION}.html',
            controller: 'ContactCtrl'
        });
}])

.controller('ContactCtrl', function(){});


