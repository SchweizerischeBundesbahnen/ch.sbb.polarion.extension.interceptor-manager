package ch.sbb.polarion.extension.interceptor.util;

import com.polarion.alm.projects.IProjectService;
import com.polarion.alm.projects.internal.model.Project;
import com.polarion.alm.projects.model.IProject;
import com.polarion.core.util.StringUtils;
import com.polarion.platform.core.PlatformContext;
import com.polarion.platform.service.repository.IRepositoryService;
import com.polarion.subterra.base.location.ILocation;
import com.polarion.subterra.base.location.Location;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.ws.rs.NotFoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class ScopeUtils {

    public static final String SCOPE_DEFAULT = "";

    @NotNull
    public static ILocation getContextLocation(@NotNull String scope) {
        if (StringUtils.isEmpty(scope)) {
            return getDefaultLocation();
        } else {
            String project = getProjectFromScope(scope);
            if (project == null) {
                throw new IllegalArgumentException(String.format("Wrong scope format: %s. Should be of form 'project/{projectId}/'", scope));
            }
            try {
                return getContextLocationByProject(project);
            } catch (Exception e) {
                throw new NotFoundException("No scope found: " + scope, e);
            }
        }
    }

    @NotNull
    public static ILocation getContextLocationByProject(@NotNull String projectId) {
        final IProjectService projectService = PlatformContext.getPlatform().lookupService(IProjectService.class);
        final IProject project = projectService.getProject(projectId);
        if (((Project) project).exists()) {
            return project.getLocation();
        } else {
            throw new NotFoundException("Project '" + projectId + "' does not exist");
        }
    }

    @Nullable
    public static String getProjectFromScope(@NotNull String scope) {
        if (!StringUtils.isEmptyTrimmed(scope)) {
            Pattern pattern = Pattern.compile("project/(.*)/");
            Matcher matcher = pattern.matcher(scope);
            if (matcher.matches()) {
                return matcher.group(1); // project
            }
        }
        return null;
    }

    @NotNull
    public static ILocation getDefaultLocation() {
        return Location.getLocationWithRepository(IRepositoryService.DEFAULT, "/");
    }
}
