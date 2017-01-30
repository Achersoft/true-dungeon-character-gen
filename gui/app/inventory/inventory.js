'use strict';

angular.module('main.inventory', ['ngRoute'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider
    .when('/bulkAdd/', {
        templateUrl: 'inventory/bulkAdd.html',
        controller: 'InventoryCtrl'
    })
    .when('/adjustInventory/', {
        templateUrl: 'inventory/adjustInventory.html',
        controller: 'InventoryCtrl'
    });;
}])

.controller('InventoryCtrl', ['$scope', '$routeParams', 'NgTableParams', 'InventorySvc', function ($scope, $routeParams, NgTableParams, inventorySvc) {
    $scope.sets;
    $scope.languages;
    $scope.selectedSet;
    $scope.selectedLanguage;
    $scope.data;
    
    inventorySvc.getSets().then(function(response) {
        $scope.sets = response.data;
    });
    
    inventorySvc.getLanguages().then(function(response) {
        $scope.languages = response.data;
    });
    
    $scope.updateSetList = function(clearQty){
        inventorySvc.getCards($scope.selectedSet, $scope.selectedLanguage, clearQty).then(function(response) {
            $scope.data = response.data;
        });
    };
    
    $scope.addToInventory = function(){
        inventorySvc.addToInventory($scope.data).then(function() {
            $scope.data = null;
            $scope.selectedSet = null;
            $scope.selectedLanguage = null;
        });
    };
    
    $scope.adjustInventory = function(){
        inventorySvc.adjustInventory($scope.data).then(function() {
            $scope.data = null;
            $scope.selectedSet = null;
            $scope.selectedLanguage = null;
        });
    };
}])

.factory('InventorySvc',['$http', 'RESOURCES', function($http, RESOURCES){    
    var inventorySvc={};

    inventorySvc.getSets = function(){
        return $http.get(RESOURCES.REST_BASE_URL + '/enums/sets');
    };
    
    inventorySvc.getLanguages = function(){
        return $http.get(RESOURCES.REST_BASE_URL + '/enums/languages');
    };
    
    inventorySvc.getCards = function(setId, lang, clearQty){
        return $http.get(RESOURCES.REST_BASE_URL + '/cards/setinventory/' + setId + '?language=' + lang + "&clearQty=" + clearQty);
    };
    
    inventorySvc.addToInventory = function(data){
        return $http.put(RESOURCES.REST_BASE_URL + '/cards/setinventory', data);
    };
    
    inventorySvc.adjustInventory = function(data){
        return $http.put(RESOURCES.REST_BASE_URL + '/cards/adjustinventory', data);
    };

    return inventorySvc;
}]);