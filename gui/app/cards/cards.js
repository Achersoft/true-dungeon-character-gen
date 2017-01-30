'use strict';

angular.module('main.cards', ['ngRoute'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider
    .when('/cardDetails/:cardId', {
        templateUrl: 'cards/cardDetails.html',
        controller: 'CardCtrl'
    });
}])

.controller('CardCtrl', ['$scope', '$routeParams', 'CardSvc', 'RESOURCES', function ($scope, $routeParams, cardSvc, RESOURCES) {
    $scope.card;
    $scope.imgBaseURL = RESOURCES.IMG_BASE_URL;
    
    cardSvc.getCardDetails($routeParams.cardId).success(function (data) {
        $scope.card = data;
    });
}])

.factory('CardSvc',['$http', 'RESOURCES', function($http, RESOURCES){    
    var cardSvc={};

    cardSvc.getCardDetails = function(cardId){
        return $http.get(RESOURCES.REST_BASE_URL + '/cards/' + cardId);
    };

    return cardSvc;
}]);