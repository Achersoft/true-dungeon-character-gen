angular.module('main')

.config(['$routeProvider', 'RESOURCES', function($routeProvider, RESOURCES) {
  $routeProvider
    .when('/party/mine', {
        templateUrl: (RESOURCES.IS_MOBILE)?'party/mobile/myParties.html':'party/mobile/myParties.html',
        controller: 'MyPartiesCtrl'
    })
    .when('/party/create', {
        templateUrl: (RESOURCES.IS_MOBILE)?'party/mobile/createParty.html':'party/desktop/createParty.html',
        controller: 'CreatePartyCtrl'
    });
}])

.controller('MyPartiesCtrl', ['$scope', 'PartySvc', 'ConfirmDialogSvc', function ($scope, partySvc, confirmDialogSvc) {
    $scope.myPartiesContext = {};
    $scope.name = null;
    $scope.characterClass = null;
    $scope.characterClasses = ["BARBARIAN", "BARD", "CLERIC", "DRUID", "DWARF_FIGHTER", "ELF_WIZARD", "FIGHTER", "WIZARD", "MONK", "PALADIN", "RANGER", "ROGUE"];
    
    partySvc.getParties().then(function(result) {
        $scope.myPartiesContext = result.data;
    });
    
    $scope.deleteCharacter = function(id, name){
        confirmDialogSvc.confirm("Are you sure you wish to delete party " + name +"?", function(){
           partySvc.deleteParty(id).then(function(result) {
                $scope.myPartiesContext = result.data;
            });
        });
    };
}])

.controller('CreatePartyCtrl', ['$scope', 'PartySvc', '$location', '$route', function ($scope, partySvc, $location, $route) {
    $scope.name = null;
    $scope.difficulty = null;
    $scope.difficulties = ["BARBARIAN", "BARD", "CLERIC", "DRUID", "DWARF_FIGHTER", "ELF_WIZARD", "FIGHTER", "WIZARD", "MONK", "PALADIN", "RANGER", "ROGUE"];
    
    $scope.createParty = function(){
        partySvc.createCharacter($scope.characterClass, $scope.name).then(function(result) {
            $location.path("/party/edit/" + result.data.id);
            $route.reload();
        });
    };
}])

.factory('PartyState', [
    function() {                    
        var characterState = {};
        
        function setContext(data) {
            characterState = data;
        }
        
        function get() {
            return characterState;
        }

        return {
            setContext: setContext,
            get: get
        };
    }])

.factory('PartySvc',['$http', 'RESOURCES', 'ErrorDialogSvc', '$q', function($http, RESOURCES, errorDialogSvc, $q) {    
    var partySvc={};
    
    partySvc.getParties = function() {
        return $http.get(RESOURCES.REST_BASE_URL + '/party/all');
    };
    
    return partySvc;
}]);