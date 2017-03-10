angular.module('main').directive('tokenSelector',['CharacterSvc', function(characterSvc){
    return {
        restrict:'E',
        scope:{
            model:'=',
            editable: '=',
            characterClass: '@',
            placement: '@',
            elementId: '@',
            setToken: '&?',
            unequip: '&?',
            label:'@'
        },
        link: function(scope) {
            scope.itemSelection = {};
            scope.tabIndex = 0;
            scope.isOpen = false;  
            
            scope.reloadTokens = function(rarity) {
                if(scope.editable === true) {
                    characterSvc.getSlotTokens(scope.model.id, scope.model.characterId, scope.characterClass, scope.model.slot, rarity).then(function(result) {
                        scope.itemSelection = result.data;
                    });
                }
            };       
            
            scope.selectToken = function(item) {
                scope.isOpen = false; 
                scope.setToken()(scope.model.characterId, scope.model.id, item.id); 
            };    
            
            scope.unequipItemSlot = function() {
                scope.isOpen = false; 
                scope.unequip()(scope.model.characterId, scope.model.id); 
            };
        },
        templateUrl:'common/tokenSelector/tokenSelectorTemplate.html'
    };
}]);