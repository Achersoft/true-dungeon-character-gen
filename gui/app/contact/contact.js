angular.module('main')

.config(['$routeProvider', function($routeProvider) {
    $routeProvider
        .when('/contact', {
            templateUrl: 'contact/contact.html',
            controller: 'ContactCtrl'
        });
}])

.controller('ContactCtrl', function(){});


