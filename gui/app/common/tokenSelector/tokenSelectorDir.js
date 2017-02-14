angular.module('main').directive('tokenSelector',['CharacterSvc', function(characterSvc){
    return {
        restrict:'E',
        scope:{
            model:'=',
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
             /*   var rarity = 'ALL';
                console.log(scope.tabIndex);
                if(scope.tabIndex === 0)
                    rarity = 'ALL';
                if(scope.tabIndex === 1)
                    rarity = 'COMMON';
                if(scope.tabIndex === 2)
                    rarity = 'UNCOMMON';
                if(scope.tabIndex === 3)
                    rarity = 'RARE';
                if(scope.tabIndex === 4)
                    rarity = 'ULTRARARE';
                if(scope.tabIndex === 5)
                    rarity = 'RELIC_PLUS';*/
                characterSvc.getSlotTokens(scope.model.id, scope.model.characterId, scope.characterClass, scope.model.slot, rarity).then(function(result) {
                    scope.itemSelection = result.data;
                });
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