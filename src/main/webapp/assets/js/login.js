(function () {

    window.onload = function () {

        let errorModal = Modal.createErrorModal('Errore', 'Nome utente o password non corretti.')

        let loginForm = new Form(document.getElementById('login-form'));

        loginForm.onresponse = function (response){
            window.sessionStorage.removeItem('directories');
            window.location.href = '/';
        }

        loginForm.onerror = function (response) {
            errorModal.show();
        }
    }

})();