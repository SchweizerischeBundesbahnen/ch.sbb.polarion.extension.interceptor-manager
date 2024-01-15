<%@ page import="ch.sbb.polarion.extension.interceptor.rest.model.Version" %>
<%@ page import="ch.sbb.polarion.extension.interceptor.util.ExtensionInfo" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

<%! Version version = ExtensionInfo.getInstance().getVersion();%>

<head>
    <title>Interceptor: Hooks settings</title>
    <link rel="stylesheet" href="../css/prism.css?bundle=<%= version.getBundleBuildTimestampDigitsOnly() %>">
    <script type="text/javascript" src="../js/prism.js?bundle=<%= version.getBundleBuildTimestampDigitsOnly() %>"></script>
    <link rel="stylesheet" href="../css/code-input.min.css?bundle=<%= version.getBundleBuildTimestampDigitsOnly() %>">
    <script type="text/javascript" src="../js/code-input.min.js?bundle=<%= version.getBundleBuildTimestampDigitsOnly() %>"></script>
    <link rel="stylesheet" href="../css/common.css?bundle=<%= version.getBundleBuildTimestampDigitsOnly() %>">
    <script type="text/javascript" src="../js/context.js?bundle=<%= version.getBundleBuildTimestampDigitsOnly() %>"></script>
    <link rel="stylesheet" href="../css/interceptor-admin.css?bundle=<%= version.getBundleBuildTimestampDigitsOnly() %>">
</head>

<body>

<div class="standard-admin-page hooks-admin">
    <h1 id="hooks-settings-header">Interceptor: Hooks settings</h1>

    <div id="hooks-reload-container">
        <button id="hooks-reload-button" type="submit" onclick="readHooksList(true)" title="Reload jar files from the hooks containing folder">Reload hooks list</button>
    </div>

    <div id="hooks-choose-container">
    </div>

    <div class="input-container">
        <div class="input-block wide">
            <div class="notifications">
                <div id="newer-version-warning" class="alert alert-warning" style="display: none">
                    <img src="/polarion/ria/images/icon-indicatorWarning16.png?bundle=<%= version.getBundleBuildTimestampDigitsOnly() %>" class="gwt-Image">
                    <span>A newer plugin version installed since the data below was persisted which can lead to unexpected behaviour.
            Consider checking if persisted data is still compatible and/or relevant to a newer plugin version. <span style="color: #999999">This message will be hidden after the next save.</span>
        </span>
                </div>
                <div id="no-hooks-registered" class="alert alert-warning" style="display: none">
                    <img src="/polarion/ria/images/icon-indicatorWarning16.png?bundle=<%= version.getBundleBuildTimestampDigitsOnly() %>" class="gwt-Image">
                    <span>No hooks found. Please refer documentation.</span>
                </div>
                <div id="data-loading-error" class="alert alert-error" style="display: none">
                    <img src="/polarion/ria/images/warning.gif?bundle=<%= version.getBundleBuildTimestampDigitsOnly() %>" class="gwt-Image">
                    <span>Error occurred loading data</span>
                </div>
                <div id="revisions-loading-error" class="alert alert-error" style="display: none">
                    <img src="/polarion/ria/images/warning.gif?bundle=<%= version.getBundleBuildTimestampDigitsOnly() %>" class="gwt-Image">
                    <span>Error occurred loading list of revisions</span>
                </div>
            </div>

            <div id="hook-settings-container">
                <div id="hook-description-container"></div>

                <div id="enable-hook-container">
                    <input type="checkbox" id="enable-hook">
                    <label id="enable-hook-label" for="enable-hook">Enable</label>
                </div>

                <div class="label-block" style="margin-top: 20px"><label>Interceptor properties</label></div>
                <code-input class="html-input vertical-resizable" id="properties-input" lang="properties" placeholder=""></code-input>
            </div>
        </div>
    </div>

    <input id="scope" type="hidden" value="<%= request.getParameter("scope")%>"/>
</div>

<div id="actions-container" class="actions-pane">
    <div id="revisions-expand-container">
        <table id="revisions-table">
            <thead>
            <tr>
                <th style="width: 10%;">Revision</th>
                <th style="width: 20%;">Date</th>
                <th style="width: 20%;">Author</th>
                <th style="width: 40%;">Comment</th>
                <th style="width: 10%;">Actions</th>
            </tr>
            </thead>
            <tbody>
            </tbody>
        </table>
    </div>

    <div class="action-buttons inline-flex">
        <button class="toolbar-button" type="submit" onclick="saveSettings()">
            <img class="button-image" alt="Save" title="Save data" src="/polarion/ria/images/actions/save.gif?bundle=<%= version.getBundleBuildTimestampDigitsOnly() %>">Save
        </button>
        <button class="toolbar-button" type="submit" onclick="cancelEdit()">
            <img class="button-image" alt="Cancel" title="Cancel editing and revert to last persisted state"
                 src="/polarion/ria/images/actions/cancel.gif?bundle=<%= version.getBundleBuildTimestampDigitsOnly() %>">Cancel
        </button>
        <button class="toolbar-button" type="submit" onclick="revertToDefault()">
            <img class="button-image" alt="Default" title="Load default values" src="/polarion/ria/images/actions/revert.gif?bundle=<%= version.getBundleBuildTimestampDigitsOnly() %>">Default
        </button>
        <button class="toolbar-button" type="submit" onclick="InterceptorContext.toggleRevisions()">
            <img class="button-image" alt="Revisions" title="Toggle list of revisions" src="/polarion/ria/images/actions/select_revision.gif?bundle=<%= version.getBundleBuildTimestampDigitsOnly() %>">Revisions
        </button>
    </div>
    <div class="action-alerts inline-flex">
        <div id="action-error" class="alert alert-error" style="display: none"></div>
        <div id="action-success" class="alert alert-success" style="display: none"></div>
    </div>
</div>

<script type="text/javascript" src="../js/settings.js?bundle=<%= version.getBundleBuildTimestampDigitsOnly() %>"></script>
</body>
</html>