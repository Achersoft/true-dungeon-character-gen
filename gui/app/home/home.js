'use strict';

angular.module('main.home', ['ngRoute'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider
    .when('/home', {
        templateUrl: 'home/home.html',
        controller: 'HomeCtrl'
    });
}])

.controller('HomeCtrl', ['$scope', '$routeParams', 'ImportSvc', function ($scope, $routeParams, homeSvc) {

}])

.factory('HomeSvc',['$http', function($http){    
    var homeSvc={};

    return homeSvc;
}]);