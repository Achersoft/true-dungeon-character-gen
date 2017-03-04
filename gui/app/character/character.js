angular.module('main')

.config(['$routeProvider', function($routeProvider) {
  $routeProvider
    .when('/desktop/character/mine', {
        templateUrl: 'character/desktop/myCharacters.html',
        controller: 'MyCharactersCtrl'
    })
    .when('/mobile/character/mine', {
        templateUrl: 'character/mobile/myCharacters.html',
        controller: 'MyCharactersCtrl'
    })
    .when('/desktop/character/create', {
        templateUrl: 'character/desktop/createCharacter.html',
        controller: 'CharacterCreationCtrl'
    })
    .when('/mobile/character/create', {
        templateUrl: 'character/mobile/createCharacter.html',
        controller: 'CharacterCreationCtrl'
    })
    .when('/desktop/character/edit/:characterId', {
        templateUrl: 'character/desktop/editCharacter.html',
        controller: 'CharacterEditCtrl'
    })
    .when('/mobile/character/edit/:characterId', {
        templateUrl: 'character/mobile/editCharacter.html',
        controller: 'CharacterEditCtrl'
    });
}])

.controller('MyCharactersCtrl', ['$scope', 'CharacterSvc', function ($scope, characterSvc) {
    $scope.myCharacterContext = {};
    
    characterSvc.getCharacters().then(function(result) {
        $scope.myCharacterContext = result.data;
    });
    
    $scope.deleteCharacter = function(id){
        characterSvc.deleteCharacter(id).then(function(result) {
            $scope.myCharacterContext = result.data;
        });
    };
}])

.controller('CharacterCreationCtrl', ['$scope', 'CharacterSvc', '$location', '$route', function ($scope, characterSvc, $location, $route) {
    $scope.name = null;
    $scope.characterClass = null;
    $scope.characterClasses = ["BARBARIAN", "BARD", "CLERIC", "DRUID", "DWARF_FIGHTER", "ELF_WIZARD", "FIGHTER", "WIZARD", "MONK", "PALADIN", "RANGER", "ROGUE"];

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
    $scope.ringErrors = null;
    $scope.backErrors = null;
    $scope.charmErrors = null;
    
    characterSvc.getCharacter($routeParams.characterId).then(function(result) {
        characterState.setContext(result.data);
        $scope.characterContext = characterState.get();
        $scope.checkItemStatus();
    });
    
    $scope.setTokenSlot = function(id, soltId, tokenId) {
        characterSvc.setTokenSlot(id, soltId, tokenId).then(function(result) {
            characterState.setContext(result.data);
            $scope.characterContext = characterState.get();
            $scope.checkItemStatus();
        });
    };
    
    $scope.unequipItem = function(id, soltId) {
        characterSvc.unequipItem(id, soltId).then(function(result) {
            characterState.setContext(result.data);
            $scope.characterContext = characterState.get();
            $scope.checkItemStatus();
        });
    };
    
    $scope.exportToPDF = function() {
        characterSvc.exportToPDF($scope.characterContext.id).then(function(response) {
            //console.log(response);
            var blob = new Blob([response.data], {type:"blob"});
            //console.log(blob);
            //change download.pdf to the name of whatever you want your file to be
            saveAs(blob, $scope.characterContext.name + ".pdf");
        });
    };
    
    $scope.checkItemStatus = function() {
        $scope.ringErrors = null;
        $scope.backErrors = null;
        $scope.charmErrors = null;
        if($scope.characterContext.rings.length > 0) {
            $scope.characterContext.rings.forEach(function(item, index) {
                if(item.slotStatus === 'INVALID')
                    $scope.ringErrors = item.statusText;
            });
        }
        if($scope.characterContext.backs.length > 0) {
            $scope.characterContext.backs.forEach(function(item, index) {
                if(item.slotStatus === 'INVALID')
                    $scope.backErrors = item.statusText;
            });
        }
        if($scope.characterContext.charms.length > 0) {
            $scope.characterContext.charms.forEach(function(item, index) {
                if(item.slotStatus === 'INVALID') {
                    if($scope.charmErrors === null)
                        $scope.charmErrors = [];
                    $scope.charmErrors.push(item.statusText);
                }
            });
        }
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
        return $http.get(RESOURCES.REST_BASE_URL + '/character/' + id);
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
    
    tokenAdminSvc.deleteCharacter = function(id) {
        return $http.delete(RESOURCES.REST_BASE_URL + '/character/' + id);
    };
    
    tokenAdminSvc.exportToPDF = function(id) {
        return $http.get(RESOURCES.REST_BASE_URL + '/character/pdf/' + id, {responseType: 'arraybuffer'});
    };
    
    return tokenAdminSvc;
}]);