package ch.sbb.polarion.extension.interceptor_manager.model;

import ch.sbb.polarion.extension.interceptor_manager.settings.HookModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IActionHook {
    @NotNull HookExecutor getExecutor();

    boolean isEnabled();
    ActionHook.ActionType getActionType();
    List<ActionHook.ItemType> getItemTypes();

    String getDefaultSettings();
    String getName();
    String getVersion();
    String validateSettings(HookModel model);
    HookModel loadSettings(boolean forceUpdate);
}
