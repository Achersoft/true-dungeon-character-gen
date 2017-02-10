angular.module('main')

.config(['$routeProvider', function($routeProvider) {
  $routeProvider
    .when('/character/mine', {
        templateUrl: 'character/myCharacters.html',
        controller: 'MyCharactersCtrl'
    })
    .when('/character/create', {
        templateUrl: 'character/createCharacter.html',
        controller: 'CharacterCreationCtrl'
    })
    .when('/character/edit/:characterId', {
        templateUrl: 'character/editCharacter.html',
        controller: 'CharacterEditCtrl'
    });
}])

.controller('MyCharactersCtrl', ['$scope', 'CharacterSvc', '$location', '$route', function ($scope, characterSvc, $location, $route) {
    $scope.myCharacterContext = {};
    
    characterSvc.getCharacters().then(function(result) {
        $scope.myCharacterContext = result.data;
    });
}])

.controller('CharacterCreationCtrl', ['$scope', 'CharacterSvc', '$location', '$route', function ($scope, characterSvc, $location, $route) {
    $scope.name = null;
    $scope.characterClass = null;
    $scope.characterClasses = ["BARBARIAN", "BARD", "CLERIC", "DRUID", "FIGHTER", "WIZARD", "MONK", "PALADIN", "RANGER", "ROGUE"];

    $scope.characterClassSelected =  function(charClass){
        return charClass === $scope.characterClass;
    };
    
    $scope.toggleCharacterClassSelected =  function(charClass){
        $scope.characterClass = charClass;
    };
    
    $scope.createCharacter = function(){
        characterSvc.createCharacter($scope.characterClass, $scope.name).then(function(result) {
            $location.path("/character/edit/" + result.data.id);
            $route.reload();
        });
    };
}])

.controller('CharacterEditCtrl', ['$scope', 'CharacterSvc', 'CharacterState', '$route', '$routeParams', function ($scope, characterSvc, characterState, $route, $routeParams) {
    $scope.tabIndex = 0;
    $scope.characterContext = characterState.get();
    $scope.characterClass = null;
    $scope.characterClasses = ["BARBARIAN", "BARD", "CLERIC", "DRUID", "FIGHTER", "WIZARD", "MONK", "PALADIN", "RANGER", "ROGUE"];

    characterSvc.getCharacter($routeParams.characterId).then(function(result) {
        characterState.setContext(result.data);
        $scope.characterContext = characterState.get();
    });
    
    $scope.characterClassSelected =  function(charClass){
        return charClass === $scope.characterClass;
    };
    
    $scope.toggleCharacterClassSelected =  function(charClass){
        $scope.characterClass = charClass;
    };
    
    $scope.setTokenSlot = function(id, soltId, tokenId) {
        characterSvc.setTokenSlot(id, soltId, tokenId).then(function(result) {
            characterState.setContext(result.data);
            $scope.characterContext = characterState.get();
        });
    };
    
    $scope.unequipItem = function(id, soltId) {
        characterSvc.unequipItem(id, soltId).then(function(result) {
            characterState.setContext(result.data);
            $scope.characterContext = characterState.get();
        });
    };
}])

.factory('CharacterState', [
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

.factory('CharacterSvc',['$http', 'RESOURCES', 'CharacterState', function($http, RESOURCES, characterState) {    
    var tokenAdminSvc={};

    tokenAdminSvc.createCharacter = function(characterClass, name) {
        return $http.put(RESOURCES.REST_BASE_URL + '/character/create?characterClass=' + characterClass + "&name=" + name);
    };
    
    tokenAdminSvc.getCharacter = function(id) {
        return $http.get(RESOURCES.REST_BASE_URL + '/character?id=' + id);
    };
    
    tokenAdminSvc.getCharacters = function() {
        return $http.get(RESOURCES.REST_BASE_URL + '/character/all');
    };
    
    tokenAdminSvc.getSlotTokens = function(slotId, characterId, characterClass, slot, rarity) {
        return $http.get(RESOURCES.REST_BASE_URL + '/token/character?slotId=' + slotId + "&characterId=" + characterId + "&characterClass=" + characterClass + "&slot=" + slot + "&rarity=" + rarity);
    };
    
    tokenAdminSvc.setTokenSlot = function(id, soltId, tokenId) {
        return $http.post(RESOURCES.REST_BASE_URL + '/character/token?id=' + id + '&soltId=' + soltId + "&tokenId=" + tokenId);
    };
    
    tokenAdminSvc.unequipItem = function(id, soltId) {
        return $http.post(RESOURCES.REST_BASE_URL + '/character/token/unequip?id=' + id + '&soltId=' + soltId);
    };
    
    return tokenAdminSvc;
}]);