angular.module('main').directive('itemMobileSelector',['CharacterSvc', function(characterSvc){
    return {
        restrict:'E',
        scope:{
            model:'=',
            characterClass: '@',
            elementId: '@',
            setToken: '&?',
            label:'@'
        },
        link: function(scope) {
            scope.itemSelection = {};
            scope.tabIndex = 0;
            
            scope.reloadTokens = function() {
                var rarity = 'ALL';
                if(scope.tabIndex === 0)
                    rarity = 'ALL';
                if(scope.tabIndex === 1)
                    rarity = 'COMMON';
                if(scope.tabIndex === 2)
                    rarity = 'UNCOMMON';
                if(scope.tabIndex === 3)
                    rarity = 'RARE';
                if(scope.tabIndex === 4)
                    rarity = 'ULRARARE';
                if(scope.tabIndex === 5)
                    rarity = 'RELIC_PLUS';
                characterSvc.getSlotTokens(scope.model.id, scope.model.characterId, scope.characterClass, scope.model.slot, rarity).then(function(result) {
                    scope.itemSelection = result.data;
                });
            };       
            
            scope.selectToken = function(item) {
                scope.setToken()(scope.model.characterId, scope.model.id, item.id); 
            };    
        },
        templateUrl:'character/mobile/itemMobileSelectorTemplate.html'
    };
}]);