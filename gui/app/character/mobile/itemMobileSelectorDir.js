angular.module('main').directive('itemMobileSelector',['CharacterSvc', function(characterSvc){
    return {
        restrict:'E',
        scope:{
            model:'=',
            elementId: '@',
            setToken: '&?',
            label:'@'
        },
        link: function(scope) {
            scope.itemSelection = {};
            
            scope.reloadTokens = function() {
                characterSvc.getSlotTokens(scope.model.characterId, 'BARBARIAN', scope.model.slot).then(function(result) {
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