function I18N(locale) {
    let localeObj;
    this.locale = locale;
    let bindings = [];

    this.getString = function (key) {
        return localeObj.getString(key);
    }

    this.setLocale = function (localeString) {
        switch (localeString) {
            case 'it':
                localeObj = new LocaleIT();
                break;
            case 'en':
                localeObj = new LocaleEN();
                break;
        }
        this.locale = localeString;
        bindings.forEach(entry => entry.element.innerText = this.getString(entry.key));
    }

    this.bindElement = function (element, key) {
        bindings.push({
            element: element,
            key: key
        });
        element.innerText = this.getString(key);
    }

    this.setLocale(locale);
}

function Locale(){
    this.strings = {}
}
Locale.prototype.getString = function (key) {
    let property = Object.getOwnPropertyDescriptor(this.strings, key);
    return property ? property.value : 'String not found!';
}


function LocaleIT(){
    Locale.call(this);
    this.strings = {
        logout: 'Esci',
        yourFile: 'I tuoi file',
        orderBy: 'Ordina per:',
        name: 'Nome',
        creationDate: 'Data di creazione',
        creationDate2: 'Data di creazione: ',
        documentType: 'Tipo documento: ',
        path: 'Percorso',
        path2: 'Percorso: ',
        summary: 'Sommario: ',
        trashcan: 'Cestino',
        open: 'Apri',
        close: 'Chiudi',
        emptyFolder: 'Cartella vuota',
        cancelConfirm: 'Conferma cancellazione',
        confirmDeleteText: 'Il documento sarà elimitato definitivamente.',
        cancel: 'Annulla'
    }
}
LocaleIT.prototype = Object.create(Locale.prototype);
LocaleIT.prototype.constructor = LocaleIT;

function LocaleEN(){
    Locale.call(this);
    this.strings = {
        logout: 'Logout',
        yourFile: "Your files",
        orderBy: 'Order by:',
        name: 'Name',
        creationDate: 'Creation date',
        creationDate2: 'Creation date: ',
        documentType: 'Document type: ',
        path: 'Path',
        path2: 'Path: ',
        summary: 'Summary: ',
        trashcan: 'Trashcan',
        open: 'Open',
        close: 'Close',
        emptyFolder: 'Empty folder',
        cancelConfirm: 'Confirm delete',
        confirmDeleteText: 'The document will be permanently deleted.',
        cancel: 'Cancel'
    }
}
LocaleEN.prototype = Object.create(Locale.prototype);
LocaleEN.prototype.constructor = LocaleEN;
