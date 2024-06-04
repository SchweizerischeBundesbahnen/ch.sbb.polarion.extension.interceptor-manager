package ch.sbb.polarion.extension.interceptor.model;

import com.polarion.platform.persistence.model.IPObject;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface HookExecutor {

    /**
     * Executed before action. It is possible to prevent action execution by returning non-null string from this method.
     * This string will be displayed as a popup message when running from UI or as a 'detail' field in the
     * Internal Server Error response (500) when calling REST API.
     *
     * @return non-null interrupt reason string in case if action must be prevented
     */
    default String preAction(@NotNull IPObject polarionObject) {
        //do nothing by default
        return null;
    }

    /**
     * Executed after action
     */
    default void postAction(@NotNull IPObject polarionObject) {
        //do nothing by default
    }

}
