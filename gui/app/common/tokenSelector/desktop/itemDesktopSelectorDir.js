angular.module('main').directive('itemDesktopSelector',['CharacterSvc', '$uibModal', function(characterSvc, $uibModal){
    return {
        restrict:'E',
        scope:{
            model:'=',
            characterClass: '@',
            elementId: '@',
            setToken: '&?',
            unequip: '&?',
            label:'@'
        },
        link: function(scope) {
            scope.itemSelection = {};
            scope.tabIndex = 0;
            scope.modalInstance = null;
            
            scope.openModal = function() {
                characterSvc.getSlotTokens(scope.model.id, scope.model.characterId, scope.characterClass, scope.model.slot, 'ALL').then(function(result) {
                    scope.itemSelection = result.data;
                    
                    scope.modalInstance = $uibModal.open({
                        ariaLabelledBy: 'modal-title',
                        ariaDescribedBy: 'modal-body',
                        bindToController: true,
                        scope: scope,
                        windowClass: 'desktop-item-modal-dialog',
                        openedClass: 'desktop-item-modal-content',
                        templateUrl: 'common/tokenSelector/desktop/itemDesktopSelectorModalTemplate.html'
                    });
                });
            }
            
            scope.reloadTokens = function(rarity) {
                characterSvc.getSlotTokens(scope.model.id, scope.model.characterId, scope.characterClass, scope.model.slot, rarity).then(function(result) {
                    scope.itemSelection = result.data;
                });
            };
            
            scope.selectToken = function(item) {
                scope.setToken()(scope.model.characterId, scope.model.id, item.id); 
                scope.modalInstance.close();
            };    
            
            scope.unequipItemSlot = function() {
                scope.unequip()(scope.model.characterId, scope.model.id); 
                scope.modalInstance.close();
            };
            
            scope.closeModal = function() {
                scope.modalInstance.close();
            };
        },
        templateUrl:'common/tokenSelector/desktop/itemDesktopSelectorTemplate.html'
    };
}]);