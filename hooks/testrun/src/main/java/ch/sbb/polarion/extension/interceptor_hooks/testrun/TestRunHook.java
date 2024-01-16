package ch.sbb.polarion.extension.interceptor_hooks.testrun;

import ch.sbb.polarion.extension.interceptor.model.ActionHook;
import ch.sbb.polarion.extension.interceptor.util.PropertiesUtils;
import com.polarion.alm.projects.model.IUniqueObject;
import com.polarion.alm.tracker.ITrackerService;
import com.polarion.alm.tracker.model.ITestRecord;
import com.polarion.alm.tracker.model.ITestRun;
import com.polarion.alm.tracker.model.ITestStepResult;
import com.polarion.core.util.logging.Logger;
import com.polarion.platform.core.PlatformContext;
import com.polarion.platform.persistence.IEnumOption;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

/**
 * WorkItemActionInterceptor hook for handling changes in TestRun:
 * - Does not allow mark test case as passed if any of step not passed
 */
@SuppressWarnings("unused")
public class TestRunHook extends ActionHook {

    private static final String SETTINGS_PROJECTS = "projects";
    private static final String SETTINGS_ERROR_MESSAGE = "errorMessage";
    private static final String TEST_CASE_ID_VARIABLE = "{testCaseId}";
    private static final String STEP_NUMBER_VARIABLE = "{stepNumber}";
    private static final String DEFAULT_ERROR_MESSAGE = "Cannot save execution results for TC " + TEST_CASE_ID_VARIABLE + " as Passed because it does not match to result in step N" + STEP_NUMBER_VARIABLE;
    private static final String VERSION = "1.0.0";
    private static final ITrackerService trackerService = PlatformContext.getPlatform().lookupService(ITrackerService.class);
    private static final Logger logger = Logger.getLogger(TestRunHook.class);

    public TestRunHook() {
        super(ItemType.TESTRUN, ActionType.SAVE, VERSION, "Does not allow mark test case as passed if any of step not passed");
    }

    @Override
    public String processAction(@NotNull IUniqueObject object) {
        String returnMessage = null;
        ITestRun testRun = (ITestRun) object;
        try {
            if (!testRun.isUnresolvable() && !testRun.isTemplate() && isCommaSeparatedSettingsHasItem(testRun.getProjectId(), SETTINGS_PROJECTS)) {
                List<ITestRecord> testRecords = testRun.getAllRecords();
                List<ITestRecord> actualTestRecords = ((ITestRun) trackerService.getDataService().getInstance(testRun.getUri())).getAllRecords();
                if (!testRecords.equals(actualTestRecords)) { //Check if records was changed
                    Iterator<ITestRecord> testRecordsIterator = testRecords.iterator();
                    Iterator<ITestRecord> actualTestRecordsIterator = actualTestRecords.iterator();
                    while (testRecordsIterator.hasNext() && actualTestRecordsIterator.hasNext() && returnMessage == null) {
                        ITestRecord newRecord = testRecordsIterator.next();
                        if (!newRecord.equals(actualTestRecordsIterator.next()) && newRecord.getResult() != null && newRecord.getResult().getId().equals("passed")) {
                            IEnumOption result = newRecord.getResult();
                            java.util.List<ITestStepResult> testStepResults = newRecord.getTestStepResults();
                            int i = 0;
                            for (Iterator<ITestStepResult> iterator = testStepResults.iterator(); iterator.hasNext() && returnMessage == null; i++) {
                                ITestStepResult stepResult = iterator.next();
                                if (stepResult.getResult() != result) {
                                    returnMessage = getSettingsValue(SETTINGS_ERROR_MESSAGE).replace(TEST_CASE_ID_VARIABLE, newRecord.getTestCase().getId()).replace(STEP_NUMBER_VARIABLE, String.valueOf(i + 1));
                                }
                            }
                        }
                    }
                }
            }
        } catch (IllegalStateException e) {
            //ignore - trying to update new TR
        } catch (Exception e) {
            logger.error("Error processing hook", e);
            returnMessage = "Cannot update TestRun due to Exception: " + (e.getMessage() != null ? e.getMessage() : e.getClass());
        }
        return returnMessage;
    }

    @Override
    public String getDefaultSettings() {
        return PropertiesUtils.build(
                SETTINGS_ERROR_MESSAGE, DEFAULT_ERROR_MESSAGE,
                SETTINGS_PROJECTS, ALL_WILDCARD
        );
    }
}
