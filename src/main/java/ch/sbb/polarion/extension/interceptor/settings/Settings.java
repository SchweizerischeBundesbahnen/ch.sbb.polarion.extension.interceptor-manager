package ch.sbb.polarion.extension.interceptor.settings;

import ch.sbb.polarion.extension.interceptor.util.VersionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.polarion.subterra.base.location.ILocation;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;

public interface Settings<T extends SettingsModel> {

    String relativeLocation();

    T read(@NotNull String scope, String revisionName);

    T read(@NotNull ILocation contextLocation, String revisionName);

    T save(@NotNull String scope, @NotNull T what);

    T save(@NotNull ILocation contextLocation, @NotNull T what);

    @NotNull T defaultValues();

    default @NotNull String currentBundleTimestamp() {
        return VersionUtils.getVersion().getBundleBuildTimestamp();
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    @NotNull default T fromString(String content) {
        T model = ((Class<T>) ((ParameterizedType) getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0]).getConstructor().newInstance();
        model.deserialize(content);
        return model;
    }

    @SneakyThrows
    @NotNull default String toString(T what) {
        return what.serialize();
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    default T fromJson(String jsonString) {
        return new ObjectMapper().readValue(jsonString, (Class<T>) ((ParameterizedType) getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0]);
    }
}
