angular.module('main').factory('ConfirmDialogSvc',['$uibModal', function($uibModal) {    
    var confirmDialogSvc={};

    confirmDialogSvc.confirm = function(text, onConfirm, title) {
        $uibModal.open({
            ariaLabelledBy: 'modal-title',
            ariaDescribedBy: 'modal-body',
            controller: 'ModalConfirmCtrl',
            controllerAs: '$ctrl',
            templateUrl: 'common/confirm/confirmModalTemplate-@{TDCC_VERSION}.html',
            resolve: {
              title: function () {
                return (title === undefined || title === null || title.length === 0) ? "Are you sure?" : title;
              },
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
.controller('ModalConfirmCtrl',function ($uibModalStack, title, text, onConfirm) {
    var $ctrl = this;
    $ctrl.text = text;
    $ctrl.title = title;
    
    $ctrl.ok = function () {
        onConfirm();
        $uibModalStack.dismissAll();
    };
    
    $ctrl.cancel = function () {
        $uibModalStack.dismissAll();
    };
});