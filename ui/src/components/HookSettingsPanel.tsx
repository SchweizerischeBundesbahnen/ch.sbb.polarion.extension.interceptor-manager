import { useCallback, useEffect, useState } from 'react';
import { CodeEditor, ConfigurationButtons, RevisionsTable, useConfirm } from '@grigoriev/react-sbb-polarion';
import type { Revision } from '@grigoriev/react-sbb-polarion';
import { toast } from 'sonner';
import useHooks from '../services/hooks';
import type { Hook, HookSettings } from '../services/hooks';

/** Backend enum names, as they read in the description panel. Unknown values are shown as they come. */
const ITEM_TYPE_NAMES: Record<string, string> = {
  WORKITEM: 'Work Item',
  TESTRUN: 'Test Run',
  MODULE: 'Module',
  PLAN: 'Plan',
  MODULE_COMMENT: 'Module Comment',
  WORK_RECORD: 'Work Record',
};

const ACTION_TYPE_NAMES: Record<string, string> = {
  SAVE: 'save',
  DELETE: 'delete',
};

const itemTypeName = (itemType: string): string => ITEM_TYPE_NAMES[itemType] ?? itemType;
const actionTypeName = (actionType: string): string => ACTION_TYPE_NAMES[actionType] ?? actionType;

interface HookSettingsPanelProps {
  hook: Hook;
}

/**
 * The stored values of one hook: what it is, whether it is enabled, and its `.properties` document,
 * with the standard Save / Cancel / Default / Revisions toolbar.
 *
 * The page mounts this per selected hook (`key={hook.name}`), so every piece of state here - the
 * loaded values, the version warning, whether revisions are open - belongs to that one hook and is
 * discarded with it. That is also why the hook arrives as a plain non-optional prop: the panel simply
 * does not exist while nothing is selected, so no handler has to ask whether it is.
 */
export default function HookSettingsPanel({ hook }: HookSettingsPanelProps) {
  const hooksApi = useHooks();
  const { confirm, confirmDialog } = useConfirm();

  const [enabled, setEnabled] = useState(false);
  const [properties, setProperties] = useState('');
  const [loaded, setLoaded] = useState(false);
  const [loadingError, setLoadingError] = useState(false);
  const [otherVersion, setOtherVersion] = useState(false);
  const [showRevisions, setShowRevisions] = useState(false);
  const [revisionsToken, setRevisionsToken] = useState(0);

  const applySettings = useCallback((settings: HookSettings) => {
    setEnabled(Boolean(settings.enabled));
    setProperties(settings.properties ?? '');
  }, []);

  /** Read the stored values. Only the first read judges the version: a revert must not clear the warning. */
  const readSettings = useCallback(
    async (checkVersion: boolean) => {
      setLoadingError(false);
      try {
        const settings = await hooksApi.loadContent(hook.name);
        applySettings(settings);
        if (checkVersion) {
          setOtherVersion(settings.hookVersion !== hook.version);
        }
        setLoaded(true);
      } catch {
        setLoadingError(true);
        setLoaded(true);
      }
    },
    [hooksApi, applySettings, hook.name, hook.version],
  );

  useEffect(() => {
    void readSettings(true);
  }, [readSettings]);

  const handleSave = async () => {
    toast.dismiss();
    try {
      await hooksApi.saveContent(hook.name, { enabled, properties });
      setOtherVersion(false);
      setRevisionsToken((t) => t + 1);
      toast.success('Data successfully saved.');
    } catch (e) {
      toast.error((e as Error).message || 'Error occurred during saving the data.');
    }
  };

  const handleCancel = async () => {
    if (!(await confirm('Are you sure you want to cancel editing and revert all changes made?'))) return;
    toast.dismiss();
    await readSettings(false);
  };

  const handleRevertToDefault = async () => {
    if (!(await confirm('Are you sure you want to return the default values?'))) return;
    toast.dismiss();
    try {
      applySettings(await hooksApi.loadDefaultContent(hook.name));
      toast.success("Default values set. Don't forget to save the data before leaving.");
    } catch {
      setLoadingError(true);
    }
  };

  const handleRevertToRevision = async (revision: Revision) => {
    try {
      applySettings(await hooksApi.loadContent(hook.name, revision.name));
      toast.success(`Data reverted to revision ${revision.name}. Don't forget to save the data before leaving.`);
    } catch {
      setLoadingError(true);
    }
  };

  return (
    <>
      {/* generic's notifications.jsp block, verbatim: `.notifications .alert-*` (bundled in RSP's
          style.css) is what gives these the yellow/red boxes with the warning triangle. */}
      {(otherVersion || loadingError) && (
        <div className="notifications">
          {otherVersion && (
            <div className="alert alert-warning">
              The settings below were persisted by a different version of this hook, which can lead to unexpected
              behaviour. Consider checking if persisted data is still relevant.{' '}
              <span className="alert-note">This message will be hidden after the next save.</span>
            </div>
          )}
          {loadingError && <div className="alert alert-error">Error occurred loading data</div>}
        </div>
      )}

      <div className="hook-description">
        Affected item type(s): <b>{hook.itemTypes.map(itemTypeName).join(', ')}</b>
        <br />
        Interceptor action type: <b>{actionTypeName(hook.actionType)}</b>
        <br />
        <br />
        Hook version: <b>{hook.version}</b>
        <br />
        <br />
        {hook.description}
      </div>

      <div className="enable-hook">
        <label htmlFor="enable-hook">
          <input type="checkbox" id="enable-hook" checked={enabled} onChange={(e) => setEnabled(e.target.checked)} />
          <span>Enable</span>
        </label>
      </div>

      <div className="label-block">
        <label htmlFor="properties-input">Hook properties</label>
      </div>
      {loaded ? (
        <CodeEditor
          language="properties"
          id="properties-input"
          className="hook-properties-editor"
          value={properties}
          onChange={setProperties}
        />
      ) : (
        <p>Loading...</p>
      )}

      <ConfigurationButtons
        onSave={() => void handleSave()}
        onCancel={() => void handleCancel()}
        onRevertToDefault={() => void handleRevertToDefault()}
        onToggleRevisions={() => setShowRevisions((v) => !v)}
        revisionsShown={showRevisions}
      />

      {showRevisions && (
        <RevisionsTable
          name={hook.name}
          scope=""
          reloadToken={revisionsToken}
          loadRevisions={hooksApi.loadRevisions}
          onRevert={(revision) => void handleRevertToRevision(revision)}
        />
      )}

      {confirmDialog}
    </>
  );
}
