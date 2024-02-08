package ch.sbb.polarion.extension.interceptor.settings;

import com.polarion.alm.projects.IProjectService;
import com.polarion.alm.projects.model.IUser;
import com.polarion.alm.shared.api.transaction.ReadOnlyTransaction;
import com.polarion.alm.shared.api.transaction.TransactionalExecutor;
import com.polarion.core.util.StringUtils;
import com.polarion.core.util.logging.Logger;
import com.polarion.platform.core.PlatformContext;
import com.polarion.platform.internal.service.repository.ExtendedRevisionMetaData;
import com.polarion.platform.service.repository.IRepositoryConnection;
import com.polarion.platform.service.repository.IRepositoryReadOnlyConnection;
import com.polarion.platform.service.repository.IRepositoryService;
import com.polarion.subterra.base.location.ILocation;
import com.polarion.subterra.base.location.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contains utility methods for load/save settings data and query revision information.
 */
public class SettingsService {

    private static final String REVISION_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
    private static final Logger logger = Logger.getLogger((Object) SettingsService.class);

    private final IRepositoryService repositoryService;
    private final IProjectService projectService;

    public SettingsService() {
        repositoryService = PlatformContext.getPlatform().lookupService(IRepositoryService.class);
        projectService = PlatformContext.getPlatform().lookupService(IProjectService.class);
    }

    public void save(@NotNull ILocation location, @NotNull String content) {
        save(location, content.getBytes(StandardCharsets.UTF_8));
    }

    public void save(@NotNull ILocation location, byte[] content) {
        Runnable runnable = () -> {
            IRepositoryConnection connection = repositoryService.getConnection(location);
            try (InputStream inputStream = new ByteArrayInputStream(content)) {
                if (connection.exists(location)) {
                    connection.setContent(location, inputStream);
                } else {
                    connection.create(location, inputStream);
                }
            } catch (IOException e) {
                logger.error("Cannot save to location: " + location, e);
            }
        };

        if (TransactionalExecutor.currentTransaction() == null) {
            TransactionalExecutor.executeInWriteTransaction(transaction -> {
                runnable.run();
                return null;
            });
        } else {
            runnable.run();
        }
    }

    @Nullable
    public String read(@NotNull ILocation location, String revisionName) {
        return TransactionalExecutor.executeSafelyInReadOnlyTransaction(transaction -> {
            IRepositoryReadOnlyConnection readOnlyConnection = repositoryService.getReadOnlyConnection(location);
            if (!readOnlyConnection.exists(location)) {
                logger.warn("Location does not exist: " + location.getLocationPath());
                return null;
            }

            try (InputStream inputStream = readOnlyConnection.getContent(StringUtils.isEmpty(revisionName) ? location : Location.getLocationWithRevision(location.getLocationPath(), revisionName))) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                logger.error("Error reading content from: " + location.getLocationPath() + ", revision: " + revisionName, e);
                return null;
            }
        });
    }

    public boolean exists(@NotNull ILocation location) {
        return Boolean.TRUE.equals(
                TransactionalExecutor.executeSafelyInReadOnlyTransaction(transaction -> {
                    IRepositoryReadOnlyConnection readOnlyConnection = repositoryService.getReadOnlyConnection(location);
                    return readOnlyConnection.exists(location);
                }));
    }

    @SuppressWarnings("unchecked")
    public List<Revision> listRevisions(@NotNull ILocation location) {
        return TransactionalExecutor.executeSafelyInReadOnlyTransaction(transaction -> {
            IRepositoryReadOnlyConnection readOnlyConnection = repositoryService.getReadOnlyConnection(location);
            if (!readOnlyConnection.exists(location)) {
                logger.warn("Location does not exist: " + location.getLocationPath());
                return new ArrayList<>();
            }

            List<ExtendedRevisionMetaData> metaDataList = readOnlyConnection.getRevisionsMetaData(location, true);
            Map<String, String> userNamesMap = projectService.getUsers().stream()
                    .collect(Collectors.toMap(IUser::getId, user -> user.getName() == null ? user.getId() : user.getName()));

            SimpleDateFormat dateFormat = new SimpleDateFormat(REVISION_DATE_TIME_FORMAT);
            return metaDataList.stream()
                    .filter(metaData -> metaData.getChangeLocationTo() == null || metaData.getChangeLocationTo().getLastComponent().equals(location.getLastComponent()))
                    .map(metaData -> Revision.builder()
                            .name(metaData.getName())
                            .date(dateFormat.format(metaData.getDate()))
                            .author(userNamesMap.getOrDefault(metaData.getAuthor(), metaData.getAuthor()))
                            .description(metaData.getDescription())
                            .build())
                    .sorted()
                    .toList();
        });
    }
}
