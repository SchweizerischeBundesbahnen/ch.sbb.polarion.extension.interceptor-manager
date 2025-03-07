import ExtensionContext from '../../ui/generic/js/modules/ExtensionContext.js';

const ctx = new ExtensionContext({
    extension: 'interceptor-manager',
    setting: 'hook',
    scopeFieldId: 'scope',
    initCodeInput: true,
    propertiesHighlighting: true
});

ctx.onClick(
    'hooks-reload-button', () => readHooksList(true),
    'save-toolbar-button', saveSettings,
    'cancel-toolbar-button', cancelEdit,
    'default-toolbar-button', revertToDefault,
    'revisions-toolbar-button', ctx.toggleRevisions,
);

const Hooks = {
    list: [],
    selectedHook: undefined
};

function saveSettings() {
    ctx.hideActionAlerts();

    ctx.callAsync({
        method: 'PUT',
        url: `/polarion/${ctx.extension}/rest/internal/hook-settings/${Hooks.selectedHook.name}/content`,
        contentType: 'application/json',
        body: JSON.stringify({
            'enabled': ctx.getCheckboxValueById('enable-hook'),
            'properties': ctx.getValueById('properties-input')
        }),
        onOk: () => {
            ctx.showSaveSuccessAlert();
            ctx.setNewerVersionNotificationVisible(false);
            readAndFillRevisions();
        },
        onError: (status, errorMessage) => {
            if (errorMessage) {
                ctx.showActionAlert({
                    containerId: 'action-error',
                    message: errorMessage,
                    hideAlertByTimeout: false
                });
            } else {
                ctx.showSaveErrorAlert()
            }
        }
    });
}

function getItemTypeName(itemType) {
    switch (itemType) {
        case 'WORKITEM': return 'Work Item';
        case 'TESTRUN': return 'Test Run';
        case 'MODULE': return 'Module';
        case 'PLAN': return 'Plan';
        case 'MODULE_COMMENT': return 'Module Comment';
        case 'WORK_RECORD': return 'Work Record';
    }
    return itemType;
}

function getInterceptorTypeName(hookType) {
    switch (hookType) {
        case 'SAVE': return 'save';
        case 'DELETE': return 'delete';
    }
    return hookType;
}

function readSelectedHook() {
    ctx.setLoadingErrorNotificationVisible(false);

    ctx.callAsync({
        method: 'GET',
        url: `/polarion/${ctx.extension}/rest/internal/hook-settings/${Hooks.selectedHook.name}/content`,
        contentType: 'application/json',
        onOk: (responseText) => {
            ctx.getElementById('hook-description-container').innerHTML =
                "Affected item type(s): <b>" + Hooks.selectedHook.itemTypes.map(t => getItemTypeName(t)).join(", ") + "</b><br>" +
                "Interceptor action type: <b>" + getInterceptorTypeName(Hooks.selectedHook.actionType) + "</b><br><br>" +
                "Hook version: <b>" + Hooks.selectedHook.version + "</b><br><br>" +
                Hooks.selectedHook.description;
            parseAndSetSettings(responseText, true);
            readAndFillRevisions();
        },
        onError: () => ctx.setLoadingErrorNotificationVisible(true)
    });
}

function readHooksList(reload) {
    ctx.setLoadingErrorNotificationVisible(false);

    ctx.callAsync({
        method: 'GET',
        url: `/polarion/${ctx.extension}/rest/internal/hooks?reload=${reload}`,
        contentType: 'application/json',
        onOk: (responseText) => {
            const hooks = JSON.parse(responseText);
            Hooks.list = hooks;

            const container = ctx.getElementById('hooks-choose-container');

            let noHooks = Hooks.list.length === 0;
            setNoHooksNotificationVisible(noHooks);
            let displayStyle = noHooks ? 'none' : 'block';
            container.style.display = displayStyle;
            ctx.getElementById('hook-settings-container').style.display = displayStyle;
            ctx.querySelector('.actions-pane').style.display = displayStyle;
            if (noHooks) {
                return;
            }

            container.replaceChildren(); //remove div content

            for (const interceptor of hooks) {
                let div = document.createElement("div");
                let label = document.createElement("label");
                label.innerHTML = interceptor.name;

                let radio = document.createElement("input");
                radio.type = "radio";
                radio.name = "hook-name";
                radio.id = interceptor.name;
                radio.addEventListener('change', () => {
                    Hooks.selectedHook = interceptor;
                    readSelectedHook();
                });

                label.appendChild(radio);
                div.appendChild(label);
                container.appendChild(div);
            }

            if (Hooks.selectedHook === undefined || reload) { //currently selected hook may disappear after reload
                Hooks.selectedHook = hooks[0];
            }
            if (reload) {
                alert("Hooks list reloaded successfully. Total hooks: " + hooks.length);
            }
            ctx.getElementById(Hooks.selectedHook.name).checked = true;
            readSelectedHook();
        },
        onError: () => ctx.setLoadingErrorNotificationVisible(true)
    });
}

function cancelEdit() {
    if (confirm("Are you sure you want to cancel editing and revert all changes made?")) {
        readSelectedHook();
    }
}

function revertToDefault() {
    if (confirm("Are you sure you want to return the default values?")) {
        loadDefaultContent()
            .then((responseText) => {
                parseAndSetSettings(responseText);
                ctx.showRevertedToDefaultAlert();
            });
    }
}

function parseAndSetSettings(text, checkNewerVersion) {
    const settings = JSON.parse(text);
    ctx.setCheckboxValueById('enable-hook', settings.enabled);
    ctx.setValueById('properties-input', settings.properties);
    if (checkNewerVersion) {
        ctx.setNewerVersionNotificationVisible(settings.hookVersion !== Hooks.selectedHook.version);
    }
}

function readAndFillRevisions() {
    ctx.readAndFillRevisions({
        setting: Hooks.selectedHook.name,
        revertToRevisionCallback: (responseText) => parseAndSetSettings(responseText)
    });
}

function setNoHooksNotificationVisible(visible) {
    ctx.getElementById('no-hooks-registered').style.display = (visible ? 'block' : 'none')
}

function loadDefaultContent() {
    return new Promise((resolve, reject) => {
        ctx.setLoadingErrorNotificationVisible(false);
        ctx.hideActionAlerts();

        ctx.callAsync({
            method: 'GET',
            url: `/polarion/${ctx.extension}/rest/internal/hook-settings/${Hooks.selectedHook.name}/default-content`,
            contentType: 'application/json',
            onOk: (responseText) => resolve(responseText),
            onError: () => {
                ctx.setLoadingErrorNotificationVisible(true);
                reject();
            }
        });
    });
}

readHooksList(false);
