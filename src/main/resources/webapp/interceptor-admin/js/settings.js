const DEFAULT_SETTING_NAME = 'Default';
SbbCommon.init({
    extension: 'interceptor',
    setting: 'hook',
    scope: SbbCommon.getValueById('scope'),
    initCodeInput: true,
    propertiesHighlighting: true
});

const Hooks = {
    list: [],
    selectedHook: undefined
};

function saveSettings() {
    SbbCommon.hideActionAlerts();

    SbbCommon.callAsync({
        method: 'PUT',
        url: `/polarion/${SbbCommon.extension}/rest/internal/hook-settings/${Hooks.selectedHook.name}/content`,
        contentType: 'application/json',
        body: JSON.stringify({
            'enabled': SbbCommon.getCheckboxValueById('enable-hook'),
            'properties': SbbCommon.getValueById('properties-input')
        }),
        onOk: () => {
            SbbCommon.showSaveSuccessAlert();
            SbbCommon.setNewerVersionNotificationVisible(false);
            readAndFillRevisions();
        },
        onError: () => SbbCommon.showSaveErrorAlert()
    });
}

function getItemTypeName(itemType) {
    switch (itemType) {
        case 'WORKITEM': return 'Work Item';
        case 'TESTRUN': return 'Test Run';
        case 'MODULE': return 'Module';
        case 'PLAN': return 'Plan';
    }
    return itemType;
}

function getInterceptorTypeName(hookType) {
    switch (hookType) {
        case 'SAVE': return 'pre-save';
        case 'DELETE': return 'pre-delete';
    }
    return hookType;
}

function readSelectedHook() {
    SbbCommon.setLoadingErrorNotificationVisible(false);

    SbbCommon.callAsync({
        method: 'GET',
        url: `/polarion/${SbbCommon.extension}/rest/internal/hook-settings/${Hooks.selectedHook.name}/content`,
        contentType: 'application/json',
        onOk: (responseText) => {
            document.getElementById('hook-description-container').innerHTML =
                "Affected item type(s): <b>" + Hooks.selectedHook.itemTypes.map(t => getItemTypeName(t)).join(", ") + "</b><br>" +
                "Interceptor action type: <b>" + getInterceptorTypeName(Hooks.selectedHook.actionType) + "</b><br>" +
                Hooks.selectedHook.description;
            parseAndSetSettings(responseText, true);
            readAndFillRevisions();
        },
        onError: () => SbbCommon.setLoadingErrorNotificationVisible(true)
    });
}

function readHooksList(reload) {
    SbbCommon.setLoadingErrorNotificationVisible(false);

    SbbCommon.callAsync({
        method: 'GET',
        url: `/polarion/${SbbCommon.extension}/rest/internal/hooks?reload=${reload}`,
        contentType: 'application/json',
        onOk: (responseText) => {
            const hooks = JSON.parse(responseText);
            Hooks.list = hooks;

            const container = document.getElementById('hooks-choose-container');

            let noHooks = Hooks.list.length === 0;
            setNoHooksNotificationVisible(noHooks);
            let displayStyle = noHooks ? 'none' : 'block';
            container.style.display = displayStyle;
            document.getElementById('hook-settings-container').style.display = displayStyle;
            document.getElementsByClassName('actions-pane')[0].style.display = displayStyle;
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
            document.getElementById(Hooks.selectedHook.name).checked = true;
            readSelectedHook();
        },
        onError: () => SbbCommon.setLoadingErrorNotificationVisible(true)
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
                SbbCommon.showRevertedToDefaultAlert();
            });
    }
}

function parseAndSetSettings(text, checkNewerVersion) {
    const settings = JSON.parse(text);
    SbbCommon.setCheckboxValueById('enable-hook', settings.enabled);
    SbbCommon.setValueById('properties-input', settings.properties);
    if (checkNewerVersion && settings.hookVersion !== Hooks.selectedHook.version) {
        SbbCommon.setNewerVersionNotificationVisible(true);
    }
}

function readAndFillRevisions() {
    SbbCommon.readAndFillRevisions({
        setting: Hooks.selectedHook.name,
        revertToRevisionCallback: (responseText) => parseAndSetSettings(responseText)
    });
}

function setNoHooksNotificationVisible(visible) {
    document.getElementById('no-hooks-registered').style.display = (visible ? 'block' : 'none')
}

function loadDefaultContent() {
    return new Promise((resolve, reject) => {
        SbbCommon.setLoadingErrorNotificationVisible(false);
        SbbCommon.hideActionAlerts();

        SbbCommon.callAsync({
            method: 'GET',
            url: `/polarion/${SbbCommon.extension}/rest/internal/hook-settings/${Hooks.selectedHook.name}/default-content`,
            contentType: 'application/json',
            onOk: (responseText) => resolve(responseText),
            onError: () => {
                SbbCommon.setLoadingErrorNotificationVisible(true);
                reject();
            }
        });
    });
}

readHooksList(false);
