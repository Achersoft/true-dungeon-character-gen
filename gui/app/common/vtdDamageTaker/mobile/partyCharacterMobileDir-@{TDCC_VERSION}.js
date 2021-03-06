angular.module('main').directive('partyCharacterMobile',['PartySvc', '$uibModal', 'AuthorizationState', 'RESOURCES', function(partySvc, $uibModal, AuthorizationState, RESOURCES){
    return {
        restrict:'E',
        scope:{
            model:'=',
            editable: '=',
            partyId: '=',
            characterClass: '@',
            addCharacter: '&?',
            removeCharacter: '&?'
        },
        link: function(scope) {
            scope.userAccounts = {};
            scope.userCharacters = {};
            scope.user = AuthorizationState.getLoggedInUserId();
            scope.character = null;
            scope.modalInstance = null;
            
            scope.openModal = function() {
                if(scope.editable === true) {
                    partySvc.getSelectableCharacters(scope.user, scope.characterClass).then(function(result) {
                        scope.userAccounts = result.data.userAccounts;
                        scope.userCharacters = result.data.characters;

                        scope.modalInstance = $uibModal.open({
                            ariaLabelledBy: 'modal-title',
                            ariaDescribedBy: 'modal-body',
                            bindToController: true,
                            scope: scope,
                            windowClass: 'mobile-modal-dialog',
                            openedClass: 'mobile-modal-content',
                            templateUrl: 'common/partyCharacter/mobile/partyCharacterMobileModalTemplate-@{TDCC_VERSION}.html'
                        });
                    });
                }
            };
            
            scope.updateCharacters = function(user) {
                partySvc.getSelectableCharacters(user, scope.characterClass).then(function(result) {
                    scope.userAccounts = result.data.userAccounts;
                    scope.userCharacters = result.data.characters;
                    scope.character = null;
                });
            };

            scope.addCharacterToParty = function(character) {
                if(character !== null) {
                    scope.addCharacter()(scope.partyId, character); 
                    scope.modalInstance.close();
                }
            };
            
            scope.removeCharacterFromParty = function(characterClass) {
                if(characterClass !== null) {
                    scope.removeCharacter()(scope.partyId, characterClass); 
                }
            };
            
            scope.closeModal = function() {
                scope.modalInstance.close();
            };
        },
        templateUrl:'common/partyCharacter/mobile/partyCharacterMobileTemplate-@{TDCC_VERSION}.html'
    };
}]);