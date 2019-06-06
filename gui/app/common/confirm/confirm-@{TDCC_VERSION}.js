angular.module('main').factory('ConfirmDialogSvc',['$uibModal', function($uibModal) {    
    var confirmDialogSvc={};

    confirmDialogSvc.confirm = function(text, onConfirm) {
        $uibModal.open({
            ariaLabelledBy: 'modal-title',
            ariaDescribedBy: 'modal-body',
            controller: 'ModalConfirmCtrl',
            controllerAs: '$ctrl',
            templateUrl: 'common/confirm/confirmModalTemplate-@{TDCC_VERSION}.html',
            resolve: {
              text: function () {
                return text;
              },
              onConfirm: function () {
                return onConfirm;
              }
            }
        });
    };

    return confirmDialogSvc;
}])
.controller('ModalConfirmCtrl',function ($uibModalStack, text, onConfirm) {
    var $ctrl = this;
    $ctrl.text = text;
    
    $ctrl.ok = function () {
        onConfirm();
        $uibModalStack.dismissAll();
    };
    
    $ctrl.cancel = function () {
        $uibModalStack.dismissAll();
    };
});