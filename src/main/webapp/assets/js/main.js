(function ()
{
    let locale = new I18N('it');

    const dateTimeFormat = new Intl.DateTimeFormat('it', {
        year: 'numeric', month: 'short', day: '2-digit',
        hour: 'numeric', minute: 'numeric', second: 'numeric', hour12: false
    });

    let currentDraggedType;
    let currentDragParent;

    let confirmDeleteModal;
    let uploadDocumentModal;
    let errorModal;
    let newDirModal;
    let propertiesModal;

    let rootDirectoriesContextMenu;
    let subDirectoriesContextMenu;
    let documentContextMenu;

    let directoriesView;
    let documentView;
    let documentNameElement;
    let documentDateElement;
    let documentPathElement;
    let documentSummaryElement;
    let documentTypeElement;
    let documentDownloadButton;
    let propertiesName;
    let propertiesDate;
    let propertiesType;
    let propertiesFormat;
    let propertiesPath;

    let orderSelectElement;

    window.onload = function () {
        rootDirectoriesContextMenu = new ContextMenu();
        subDirectoriesContextMenu = new ContextMenu();
        documentContextMenu = new ContextMenu();
        confirmDeleteModal = new Modal(document.getElementById('confirm-delete-modal'));
        errorModal = new Modal(document.getElementById('error-modal'));
        newDirModal = new Modal(document.getElementById('new-folder-dialog'));
        uploadDocumentModal = new Modal(document.getElementById('upload-document-dialog'));
        propertiesModal = new Modal(document.getElementById('properties-dialog'));

        directoriesView = document.getElementById('directories-view');
        documentView = document.getElementById('document-view');
        documentNameElement = document.getElementById('document-name');
        documentDateElement = document.getElementById('document-date');
        documentPathElement = document.getElementById('document-path');
        documentSummaryElement = document.getElementById('document-summary');
        documentTypeElement = document.getElementById('document-type');
        documentDownloadButton = document.getElementById('document-download-button');
        propertiesName = document.getElementById('properties-name');
        propertiesDate = document.getElementById('properties-date');
        propertiesType = document.getElementById('properties-type');
        propertiesFormat = document.getElementById('properties-format');
        propertiesPath = document.getElementById('properties-path');
        orderSelectElement = document.getElementById('order-select');


        initializeDirectories().then(() => console.log('Directories initialized.'));

        document.getElementById('back-button').addEventListener('click', () => showDirectories());

        document.getElementById('logout-btn').addEventListener('click', e => {
            deleteDirectories();
            window.location.href = '/logout';
        });

        Array.from(document.querySelectorAll('[data-locale-key]')).forEach(element =>
        {
           let key = element.getAttribute('data-locale-key');
           locale.bindElement(element, key);
        });

        document.getElementById('i18n-btn').addEventListener('click', function (e) {
            if(locale.locale === 'it'){
                locale.setLocale('en');
                e.currentTarget.querySelector('span').innerText = 'en';
            }
            else{
                locale.setLocale('it');
                e.currentTarget.querySelector('span').innerText = 'it';
            }
        });

        orderSelectElement.addEventListener('change', function (e) {
            let selected = e.target.value;
            sortRecursive(Directory.rootDirectory, selected);
        });

        document.getElementById('invert-order-toggle').addEventListener('change', function(e){
            reverseDirectoryListRecursive(Directory.rootDirectory);
        });

        document.getElementById('sync-btn').addEventListener('click', function (e) {
            let target = e.currentTarget;
            target.disabled = true;
            target.classList.add('loader');
            Directory.rootDirectory.removeAllSubDirectories();
            deleteDirectories();

            initializeDirectories().then(() =>
            {
                target.classList.remove('loader');
                target.disabled = false;
            });
        });

        rootDirectoriesContextMenu.addOption('Apri', function (contextElement) {
            contextElement.querySelector('.open-folder-btn').click();
        });
        rootDirectoriesContextMenu.addOption('Nuova cartella', function (contextElement) {
            contextElement.querySelector('.new-folder-button').click();
        });
        rootDirectoriesContextMenu.addOption('Proprietà', function (contextElement) {
            let dirId = contextElement.closest('.item').getAttribute('data-folder-id');
            showDirectoryProperties(dirId);
        });
        rootDirectoriesContextMenu.addOption('Elimina', function (contextElement) {
            let dirId = contextElement.closest('.item').getAttribute('data-folder-id');
            let dir = Directory.rootDirectory.findSubDirectory(dirId);
            confirmDeleteModal.show({
                type: 'dir',
                id: dir.id,
                parent: dir.parent.id
            });
        });

        subDirectoriesContextMenu.addOption('Apri', function (contextElement) {
            contextElement.querySelector('.open-folder-btn').click();
        });
        subDirectoriesContextMenu.addOption('Carica documento', function (contextElement) {
            contextElement.querySelector('.upload-document-button').click();
        });
        subDirectoriesContextMenu.addOption('Proprietà', function (contextElement) {
            let dirId = contextElement.closest('.item').getAttribute('data-folder-id');
            showDirectoryProperties(dirId);
        });
        subDirectoriesContextMenu.addOption('Elimina', function (contextElement) {
            let dirId = contextElement.closest('.item').getAttribute('data-folder-id');
            let dir = Directory.rootDirectory.findSubDirectory(dirId);
            confirmDeleteModal.show({
                type: 'dir',
                id: dir.id,
                parent: dir.parent.id
            });
        });

        documentContextMenu.addOption('Apri', function (contextElement) {
            contextElement.querySelector('.open-document-button').click();
        });
        documentContextMenu.addOption('Proprietà', function (contextEvent) {
            let documentJson = documentContextMenu.contextData;
            let parentDir = Directory.rootDirectory.findSubDirectory(documentJson.parentId);
            propertiesType.innerText = 'Documento';
            propertiesFormat.innerText = documentJson.type;
            propertiesName.innerText = documentJson.name;
            propertiesPath.innerText = parentDir.getPath();
            propertiesDate.innerText = dateTimeFormat.format(new Date(documentJson.creationDate));
            propertiesModal.setTitle(`Proprietà ${documentJson.name}`);
            propertiesModal.show();
        });
        documentContextMenu.addOption('Elimina', function (contextElement) {
            let documentJson = documentContextMenu.contextData;
            confirmDeleteModal.show({
                type: 'doc',
                id: documentJson.id,
                parent: documentJson.parentId
            });
        });

        confirmDeleteModal.addEventListener('close', function (e) {
            if(e.detail.modalResult === 'OK') {
                let request = new XMLHttpRequest();
                request.open('POST', '/delete');
                request.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');

                request.onreadystatechange = function () {
                    if(request.readyState === XMLHttpRequest.DONE) {
                        let type = confirmDeleteModal.data.type;
                        if(request.status === 200) {
                            if(confirmDeleteModal.data.type === 'dir')Directory.rootDirectory.findSubDirectory(confirmDeleteModal.data.id).remove();
                            else Directory.rootDirectory.findSubDirectory(confirmDeleteModal.data.parent).removeDocument(confirmDeleteModal.data.id);
                        }
                        else{
                            let suffix = type === 'dir' ? 'della cartella' : 'del documento';
                            errorModal.text(`Si è verificato un errore durante l'eliminazione ${suffix}.`);
                            errorModal.show();
                        }
                    }
                }
                request.send(`type=${confirmDeleteModal.data.type}&id=${confirmDeleteModal.data.id}`);
            }
        });

        newDirModal.addEventListener('close', function (e) {
            if(e.detail.modalResult === 'OK') {
                if(e.detail.formResponse.status === 200)
                    addDirectory(new Directory(JSON.parse(e.detail.formResponse.responseText)));
            }
        });

        uploadDocumentModal.addEventListener('close', function (e) {
            if(e.detail.modalResult === 'OK') {
                if(e.detail.formResponse.status === 200)
                {
                    let documentJson = JSON.parse(e.detail.formResponse.responseText);
                    Directory.rootDirectory.findSubDirectory(documentJson.parentId).addDocument(documentJson);
                }
            }
        });

        document.querySelector('.new-folder-button').addEventListener('click', e => newFolderButtonClickHandler(e));

        initTrashcan();
    }

    function showDirectoryProperties(dirId) {
        let dir = Directory.rootDirectory.findSubDirectory(dirId);
        propertiesType.innerText = 'Cartella';
        propertiesFormat.innerText = '-';
        propertiesName.innerText = dir.name;
        propertiesPath.innerText = dir.getParentPath();
        propertiesDate.innerText = dateTimeFormat.format(new Date(dir.json.creationDate));
        propertiesModal.setTitle(`Proprietà ${dir.name}`);
        propertiesModal.show();
    }

    function initializeDirectories() {

        return new Promise((resolve, reject) =>
        {
            let directories = getDirectories();

            if(directories)
            {
                createDirectories(directories);
                resolve();
            }
            else
            {
                let request = new XMLHttpRequest();
                request.open('GET', '/get-directories');
                request.onreadystatechange = function () {
                    if(request.readyState === XMLHttpRequest.DONE)
                    {
                        if(request.status === 200)
                        {
                            let directoriesJson = JSON.parse(request.responseText);
                            createDirectories(directoriesJson);
                            saveDirectories();
                            resolve();
                        }
                        else{
                            reject();
                        }
                    }
                }
                request.send();
            }
        });
    }

    function openFolderOnClickHandler(e)
    {
        let button = e.currentTarget;

        let folder = button.closest('.item');

        if(!folder.hasAttribute('data-id') || folder.getAttribute('data-doc-loaded') === 'true')
        {
            folder.querySelector('.list').classList.remove('hidden');
            button.style.display = "none";
            button.nextElementSibling.removeAttribute('style');
            folder.querySelector('.item-content .item-icon').setAttribute('src', '/assets/images/folder_open-24px.svg')
        }
        else
        {
            button.style.display = "none";
            let loader = button.closest('.right-buttons').querySelector(".loader");
            loader.classList.remove('hidden');

            let folderId = folder.getAttribute('data-id');
            let directory = Directory.rootDirectory.findSubDirectory(folderId);
            fetchDocuments(folderId, function (documents) {
                documents.forEach(documentJson =>
                {
                    directory.addDocument(documentJson);
                });
                directory.json.documentsSaved = true;
                saveDirectories();
                folder.setAttribute('data-doc-loaded', 'true');

                directory.list.classList.remove('hidden');
                loader.classList.add('hidden');
                button.nextElementSibling.removeAttribute('style');
                folder.querySelector('.item-content .item-icon').setAttribute('src', '/assets/images/folder_open-24px.svg')

            }, function () {
                errorModal.text(`Si è verificato un errore durante il download dei documenti.`);
                errorModal.show();
            });
        }
    }

    function closeFolderOnClickHandler(e)
    {
        let button = e.currentTarget;

        let folder = button.closest('.item');
        let list = folder.querySelector('.list');
        list.classList.add('hidden');
        Array.from(list.querySelectorAll('.list')).forEach(subList =>
        {
            let subFolder = subList.closest('.item');
            Array.from(subFolder.querySelectorAll('.close-folder-btn')).forEach(openBtn => openBtn.style.display = 'none');
            Array.from(subFolder.querySelectorAll('.open-folder-btn')).forEach(openBtn => openBtn.style.display = 'block');
            subList.classList.add('hidden');
            subFolder.querySelector('.item-content .item-icon').setAttribute('src', '/assets/images/folder-24px.svg');
        });
        button.style.display = "none";
        button.previousElementSibling.removeAttribute('style');
        folder.querySelector('.item-content .item-icon').setAttribute('src', '/assets/images/folder-24px.svg');
    }

    function directoryDblClickHandler(e)
    {
        e.currentTarget.querySelector('.open-folder-btn').click();
    }

    function newFolderButtonClickHandler(e) {
        let button = e.currentTarget;
        let parentId;
        if(button.classList.contains('root'))parentId = 0;
        else parentId = button.closest('.item').getAttribute('data-folder-id');
        newDirModal.setFormValue('parent-id-input', parentId);
        newDirModal.show();
    }

    function uploadDocumentButtonClickHandler(e) {
        let parentId = e.currentTarget.closest('.item').getAttribute('data-folder-id');
        uploadDocumentModal.setFormValue('doc-parent-id-input', parentId);
        uploadDocumentModal.show();
    }

    function addFolderDragStart(folder) {
        let folderItem = folder.closest(".item");
        let folderId = folderItem.getAttribute("data-folder-id");
        let parentId = folderItem.getAttribute("data-parent-id");

        folder.addEventListener('dragstart', function (e) {
            e.dataTransfer.effectAllowed = 'move';
            e.dataTransfer.dropEffect = 'move';
            currentDraggedType = 'dir';
            currentDragParent = parentId;

            e.dataTransfer.setData('text/plain', JSON.stringify({
                type: 'dir',
                id: folderId,
                parent: parentId
            }));
        });
    }

    function addSubFolderDropEvent(folder) {
        let folderItem = folder.closest(".item");
        let targetFolderId = folderItem.getAttribute('data-folder-id');

        folderItem.addEventListener('dragover', function (e) {
            e.preventDefault();
            if(currentDraggedType === 'doc' && currentDragParent !== targetFolderId)
            {
                folder.classList.add('green-border');
                setOpenFolderTimeout(folderItem);
            }
            else folder.classList.add('red-border');
        });
        folderItem.addEventListener('dragenter', function (e) {
            e.preventDefault();
        });
        folderItem.addEventListener('dragleave', function (e) {
            folder.classList.remove('green-border', 'red-border');
            if(e.currentTarget.timeout)clearTimeout(e.currentTarget.timeout);
            e.currentTarget.timeoutStarted = false;
        });
        folderItem.addEventListener('drop', function (e) {
            folder.classList.remove('green-border', 'red-border');
            let data = JSON.parse(e.dataTransfer.getData('text/plain'));
            if(data.type !== 'doc') return;

            if(targetFolderId === `${data.parent}`) return;
            let request = new XMLHttpRequest();
            request.open("GET", `/move?dir=${targetFolderId}&doc=${data.id}`);
            request.onreadystatechange = function () {
                if(request.readyState === XMLHttpRequest.DONE)
                {
                    if(request.status === 200){
                        Directory.rootDirectory.findSubDirectory(data.parent).moveDocument(data.id, targetFolderId);
                    }
                    else{
                        errorModal.text(`Si è verificato un errore durante lo spostamento del documento.`);
                        errorModal.show();
                    }
                }
            };
            request.send();
        });
    }

    function setOpenFolderTimeout(folderItem) {
        let openBtn = folderItem.querySelector('.open-folder-btn');
        let opened = openBtn.style.display === 'none';

        if(!folderItem.timeoutStarted && !opened)
        {
            folderItem.timeoutStarted = true;
            folderItem.timeout = setTimeout(function () {
                openBtn.click();
            }, 500);
        }
    }

    function addRootFolderDragOverEvent(folderItem)
    {
        folderItem.addEventListener('dragenter', e => setOpenFolderTimeout(folderItem));

        folderItem.addEventListener('dragleave', e => {
            if(e.currentTarget.timeout)clearTimeout(e.currentTarget.timeout);
            e.currentTarget.timeoutStarted = false;
        });
    }

    function showDocument(documentJson) {
        let parentDir = Directory.rootDirectory.findSubDirectory(documentJson.parentId);
        documentNameElement.innerText = documentJson.name;
        documentDateElement.innerText = dateTimeFormat.format(new Date(documentJson.creationDate));
        documentPathElement.innerText = parentDir.getPath();
        documentSummaryElement.innerText = documentJson.summary;
        documentTypeElement.innerText = documentJson.type;
        documentDownloadButton.setAttribute('href', `/download?doc=${documentJson.id}`);

        directoriesView.classList.add('hidden');
        documentView.classList.remove('hidden');
    }

    function showDirectories() {
        documentView.classList.add('hidden');
        directoriesView.classList.remove('hidden');
    }

    function createDocumentElement(documentJson)
    {
        let documentElement = document.createElement('li');
        documentElement.classList.add('item');
        documentElement.setAttribute('id', `document-${documentJson.id}`)
        documentElement.setAttribute('data-parent-id', documentJson.parentId);
        documentElement.setAttribute('draggable', 'true');
        documentElement.setAttribute('data-document-date', documentJson.creationDate);
        documentElement.setAttribute('data-document-name', documentJson.name);

        let documentContent = document.createElement('div');
        documentContent.classList.add('item-content', 'third-level');
        documentContent.addEventListener('dblclick', () => showDocument(documentJson));
        documentContent.addEventListener('contextmenu', e =>
        {
            e.preventDefault();
            documentContextMenu.show(e, documentJson);
        });

        let rightButtons = document.createElement('div');
        rightButtons.classList.add('right-buttons');

        let openDocumentButton = document.createElement('button');
        openDocumentButton.classList.add('button', 'open-document-button');
        openDocumentButton.innerText = 'Apri documento'
        openDocumentButton.addEventListener('click',  () => showDocument(documentJson));

        rightButtons.appendChild(openDocumentButton);

        let icon = document.createElement('img');
        icon.classList.add('item-icon');
        icon.setAttribute('src', '/assets/images/document-24px.svg')

        let itemText = document.createElement('a');
        itemText.classList.add('item-text');
        itemText.innerText = documentJson.name;

        documentContent.appendChild(icon);
        documentContent.appendChild(itemText);
        documentContent.appendChild(rightButtons);
        documentElement.appendChild(documentContent);

        let dragIcon = new Image();
        dragIcon.src = '/assets/images/document-24px.svg';

        documentElement.addEventListener('dragstart', function (e) {
            e.dataTransfer.effectAllowed = 'move';
            e.dataTransfer.dropEffect = 'move';
            let parent = documentElement.getAttribute('data-parent-id');

            currentDraggedType = 'doc';
            currentDragParent = parent;

            e.dataTransfer.setData('text/plain', JSON.stringify({
                type: 'doc',
                id: documentJson.id,
                parent: parent
            }))

            //e.dataTransfer.setDragImage(dragIcon, 0, 0);
        });

        return documentElement;
    }

    function fetchDocuments(folderId, callback, errorCallback) {
        let request = new XMLHttpRequest();
        request.onreadystatechange = function () {
            if(this.readyState === XMLHttpRequest.DONE)
            {
                if(this.status < 400) callback(JSON.parse(this.responseText));
                else errorCallback();
            }
        };
        request.open('GET', `/documents?dir=${folderId}`);
        request.send();
    }

    function initTrashcan()
    {
        let trashcan = document.getElementById('trashcan');
        trashcan.addEventListener('dragover', function (e) {
            e.preventDefault();
            e.target.classList.add('red');
        });

        trashcan.addEventListener('dragleave', function (e) {
            e.target.classList.remove('red');
        });

        trashcan.addEventListener('dragenter', function (e) {
            e.preventDefault();
        });

        trashcan.addEventListener('drop', function (e) {
            e.target.classList.remove('red');
            let data = JSON.parse(e.dataTransfer.getData('text/plain'));
            confirmDeleteModal.show(data);
        });
    }


    function createDirectories(directories) {
        Directory.rootDirectory = new Directory({
            id: 0,
            parentId: 0,
            name: 'root',
            subDirectories: directories
        });
    }

    function Directory(directoryJson)
    {
        this.id = directoryJson.id;
        this.parentId = directoryJson.parentId;
        this.name = directoryJson.name;
        this.parent = null;
        this.json = directoryJson;
        let self = this;

        let subDirectories = [];

        let addSubDirectories = function (directoriesJson) {
            directoriesJson.forEach(dirJson => {
                let dir = new Directory(dirJson);
                dir.parent = self;
                subDirectories.push(dir);
                if(self.list.childElementCount === 1 && self.id !== 0) self.emptyFolderItem.classList.add('hidden-item');
                self.list.appendChild(dir.item);
            });
            sort(self, orderSelectElement.options[orderSelectElement.selectedIndex].value);
        }

        this.getSubDirectories = function () {
            return subDirectories;
        }

        this.addSubDirectory = function (dir) {
            subDirectories.push(dir);
            if(this.json.subDirectories) this.json.subDirectories.push(dir.json);
            else this.json.subDirectories = [dir.json];
            saveDirectories();
            dir.parent = this;
            if(this.list.childElementCount === 1 && this.id !== 0) this.emptyFolderItem.classList.add('hidden-item');
            this.list.appendChild(dir.item);
            sort(self, orderSelectElement.options[orderSelectElement.selectedIndex].value);
        }

        this.addDocument = function (documentJson) {
            if(!this.json.documents)this.json.documents = [documentJson];
            else
            {
                let exists = false;
                for(let doc of this.json.documents)
                {
                    if(doc.id === documentJson.id){
                        exists = true;
                        break;
                    }
                }
                if(exists)return;

                this.json.documents.push(documentJson);
            }
            saveDirectories();

            if(this.list.childElementCount === 1 && this.id !== 0) this.emptyFolderItem.classList.add('hidden-item');
            this.list.append(createDocumentElement(documentJson));
        }

        this.removeDocument = function (documentId) {
            let index;
            let found = false;
            for(index = 0; this.json.documents.length; index++) {
                if(this.json.documents[index].id === documentId) {
                    found = true;
                    break;
                }
            }
            if(found) {
                this.list.querySelector(`#document-${documentId}`).remove();
                this.json.documents.splice(index, 1);
                if(this.list.childElementCount === 1) this.emptyFolderItem.classList.remove('hidden-item');
                saveDirectories();
            }
        }

        this.moveDocument = function (documentId, destDirectoryId) {
            let destDir = Directory.rootDirectory.findSubDirectory(destDirectoryId);
            let index;
            for(index = 0; index < this.json.documents.length; index++)
            {
                if(this.json.documents[index].id === documentId)
                {
                    let removed = this.json.documents.splice(index, 1)[0];
                    removed.parentId = destDirectoryId;
                    if(destDir.json.documents)destDir.json.documents.push(removed);
                    else destDir.json.documents = [removed];
                    saveDirectories();
                    let documentItem = this.list.querySelector(`#document-${documentId}`);
                    documentItem.setAttribute('data-parent-id', destDirectoryId);
                    if(destDir.list.childElementCount === 1) destDir.emptyFolderItem.classList.add('hidden-item');
                    destDir.list.appendChild(documentItem);
                    if(this.list.childElementCount === 1) this.emptyFolderItem.classList.remove('hidden-item');
                    return;
                }
            }
        }

        this.removeSubDirectory = function (subDirId) {
            let index;
            let foundDir = null;
            for(index = 0; subDirectories.length; index++) {
                if(subDirectories[index].id === subDirId) {
                    foundDir = subDirectories[index];
                    break;
                }
            }
            if(foundDir) {
                foundDir.item.remove();
                subDirectories.splice(index, 1);
                this.json.subDirectories.splice(index, 1);
                saveDirectories();
                if(this.list.childElementCount === 1) this.emptyFolderItem.classList.remove('hidden-item');
            }
        }

        this.removeAllSubDirectories = function () {
            subDirectories.forEach(dir => dir.item.remove());
            subDirectories.length = 0;
            this.json.subDirectories = [];
            saveDirectories();
        }

        this.findSubDirectory = function (subDirId) {
            for(let dir of subDirectories) {
                if(dir.id.toString() === subDirId.toString())return dir;
                let d = dir.findSubDirectory(subDirId);
                if(d) return d;
            }
            return null;
        }

        this.remove = function () {
            this.parent.removeSubDirectory(this.id);
        }

        this.getPathName = function () {
            return self.id === 0 ? '' : self.name;
        }

        this.getParentPath = function () {
            if(this.id === 0)return '';
            else {
                let parentPath = this.parent.getParentPath();
                return `${parentPath === '/' ? '' : parentPath}/${this.parent.getPathName()}`;
            }
        }

        this.getPath = function () {
            let parentPath = this.getParentPath();
            return `${parentPath === '/' ? '' : parentPath}/${this.getPathName()}`;
        }

        this.createDirectoryItem = function() {
            let item = document.createElement('li');
            item.classList.add('item');
            item.setAttribute('data-folder-id', this.id);
            item.setAttribute('data-parent-id', this.parentId);
            item.setAttribute('id', `folder-${this.id}`);

            if(this.parentId > 0)
            {
                item.setAttribute('data-id', this.id);
                item.setAttribute('data-doc-loaded', 'false');
            }
            else
            {
                addRootFolderDragOverEvent(item);
            }

            let itemContent = document.createElement('div');
            itemContent.classList.add('item-content');
            if(this.parentId > 0)itemContent.classList.add('second-level');
            itemContent.setAttribute('draggable', 'true');
            itemContent.setAttribute(this.parentId > 0 ? 'data-sub-folder' : 'data-folder', '');
            itemContent.addEventListener('dblclick', e => directoryDblClickHandler(e));
            itemContent.addEventListener('contextmenu', e =>
            {
                e.preventDefault();
                if(self.parentId === 0)rootDirectoriesContextMenu.show(e);
                else subDirectoriesContextMenu.show(e);
            });

            let itemIcon = document.createElement('img');
            itemIcon.classList.add('item-icon');
            itemIcon.setAttribute('src', '/assets/images/folder-24px.svg');

            let itemText = document.createElement('span');
            itemText.classList.add('item-text');
            itemText.innerText = this.name;

            let rightButtons = document.createElement('div');
            rightButtons.classList.add('right-buttons');

            let openFolderButton = document.createElement('button');
            openFolderButton.classList.add('button', 'open-folder-btn');
            openFolderButton.innerText = 'apri';
            openFolderButton.addEventListener('click', e => openFolderOnClickHandler(e));
            locale.bindElement(openFolderButton, 'open');

            let closeFolderButton = document.createElement('button');
            closeFolderButton.classList.add('button', 'close-folder-btn');
            closeFolderButton.innerText = 'chiudi';
            closeFolderButton.style.display = 'none';
            closeFolderButton.addEventListener('click', e => closeFolderOnClickHandler(e));
            locale.bindElement(closeFolderButton, 'close');

            let loader = document.createElement('img');
            loader.classList.add('loader', 'hidden');
            loader.setAttribute('src', '/assets/images/loader-24px.svg');

            let newButton = document.createElement('button');
            newButton.classList.add('icon-button');
            if(this.parentId === 0)newButton.classList.add('new-folder-button');
            else newButton.classList.add('upload-document-button');
            newButton.setAttribute('title', this.parentId > 0 ? 'Carica documento' : 'Crea nuova cartella');

            if(this.parentId > 0)
            {
                newButton.addEventListener('click', e => uploadDocumentButtonClickHandler(e));
            }
            else
            {
                newButton.addEventListener('click', e => newFolderButtonClickHandler(e));
            }

            let newButtonIcon = document.createElement('img');
            newButtonIcon.classList.add('btn-icon');
            newButtonIcon.setAttribute('src', this.parentId > 0 ? '/assets/images/cloud_upload-24px.svg' : '/assets/images/create_new_folder-24px.svg');

            newButton.appendChild(newButtonIcon);
            rightButtons.appendChild(openFolderButton);
            rightButtons.appendChild(closeFolderButton);
            rightButtons.appendChild(loader);
            rightButtons.appendChild(newButton);

            itemContent.appendChild(itemIcon);
            itemContent.appendChild(itemText);
            itemContent.appendChild(rightButtons);

            let listElement = document.createElement('ul');
            listElement.classList.add('list', 'hidden');
            if(this.parentId > 0)listElement.classList.add('document-list');

            let emptyFolderItemElement = document.createElement('li');
            emptyFolderItemElement.classList.add('item', 'empty-folder-item');

            let emptyFolderItemContent = document.createElement('div');
            emptyFolderItemContent.classList.add('item-content', this.parentId > 0 ? 'third-level' : 'second-level');

            let emptyFolderText = document.createElement('a');
            emptyFolderText.classList.add('item-text');
            emptyFolderText.innerText = 'Cartella vuota';
            locale.bindElement(emptyFolderText, 'emptyFolder');

            emptyFolderItemContent.appendChild(emptyFolderText);
            emptyFolderItemElement.appendChild(emptyFolderItemContent);
            listElement.appendChild(emptyFolderItemElement);

            item.appendChild(itemContent);
            item.appendChild(listElement);

            addFolderDragStart(itemContent);
            if(this.parentId > 0)addSubFolderDropEvent(itemContent);

            return item;
        }

        this.directoryItem = this.id === 0 ? null : this.createDirectoryItem();
        this.item = this.directoryItem;
        this.list = this.id === 0 ? document.getElementById('root-list') : this.directoryItem.querySelector('.list');
        this.emptyFolderItem = this.id === 0 ? null : this.list.querySelector('.empty-folder-item');

        if(directoryJson.subDirectories) addSubDirectories(directoryJson.subDirectories);

        if(this.json.documentsSaved && this.id !== 0)
        {
            this.item.setAttribute('data-doc-loaded', 'true');
            if(directoryJson.documents && directoryJson.documents.length > 0)
            {
                if(this.list.childElementCount === 1) this.emptyFolderItem.classList.add('hidden-item');
                for(let doc of directoryJson.documents)
                    this.list.append(createDocumentElement(doc));
            }
        }

    }

    /*
         DIRECTORY SORTING FUNCTIONS
     */

    function compare(a, b){
        if(a < b)return -1;
        if(a > b)return  1;
        return 0;
    }

    function sortRecursive(directory, mode) {
        sort(directory, mode);
        directory.getSubDirectories().forEach(dir => sortRecursive(dir, mode));
    }

    function reverseDirectoryList(directory) {
        let subDirectories = directory.getSubDirectories();
        subDirectories.reverse().forEach(dir => directory.list.appendChild(dir.item));

        let documentElements = Array.from(directory.list.querySelectorAll(":scope > .item[data-document-name]"));
        documentElements.reverse().forEach(doc => directory.list.appendChild(doc));
    }

    function reverseDirectoryListRecursive(directory) {
        reverseDirectoryList(directory);
        directory.getSubDirectories().forEach(dir => reverseDirectoryListRecursive(dir));
    }

    function sort(directory, mode){
        //SORT DIRECTORIES
        let subDirectories = directory.getSubDirectories();
        let list = directory.list;
        subDirectories.sort((dir1, dir2) =>{
            if(mode === 'date')
                return compare(new Date(dir2.json.creationDate), new Date(dir1.json.creationDate));
            else if(mode === 'name')
                return compare(dir1.name, dir2.name);
        });
        if(document.getElementById('invert-order-toggle').checked)subDirectories.reverse();
        subDirectories.forEach(dir => list.appendChild(dir.item));

        //SORT DOCUMENTS
        let documentElements = Array.from(list.querySelectorAll(":scope > .item[data-document-name]"));
        documentElements.sort((doc1, doc2) =>{
            if(mode === 'date')
                return compare(new Date(doc2.getAttribute("data-document-date")), new Date(doc1.getAttribute("data-document-date")));
            else if(mode === 'name')
                return compare(doc1.getAttribute("data-document-name"), doc2.getAttribute("data-document-name"));
        });
        documentElements.forEach(doc => list.appendChild(doc));
    }

    /*
         SESSION STORAGE FUNCTIONS
     */

    function addDirectory(directory)
    {
        console.log(Directory.rootDirectory);
        if(directory.parentId === 0) Directory.rootDirectory.addSubDirectory(directory);
        else Directory.rootDirectory.findSubDirectory(directory.parentId).addSubDirectory(directory);
    }

    function saveDirectories()
    {
        window.sessionStorage.setItem('directories', JSON.stringify(Directory.rootDirectory.json.subDirectories));
    }

    function getDirectories()
    {
        return JSON.parse(window.sessionStorage.getItem('directories'));
    }

    function deleteDirectories() {
        window.sessionStorage.removeItem('directories');
    }
})();