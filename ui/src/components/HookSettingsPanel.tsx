import { useCallback, useEffect, useLayoutEffect, useRef, useState } from 'react';
import { CodeEditor, ConfigurationButtons, RevisionsTable, useConfirm } from '@sbb-polarion/react-sbb-polarion';
import type { Revision } from '@sbb-polarion/react-sbb-polarion';
import { toast } from 'sonner';
import useHooks, { SettingsError } from '../services/hooks';
import type { Hook, HookSettings } from '../services/hooks';

/** Backend enum names, as they read in the description panel. Unknown values are shown as they come. */
export const ITEM_TYPE_NAMES: Record<string, string> = {
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

/** The info box is clipped to this height while collapsed, and offers the handle only above it. */
export const COLLAPSED_HEIGHT = 110;

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
 * loaded values, the validation errors, whether revisions are open - belongs to that one hook and is
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
  const [validationErrors, setValidationErrors] = useState<string[]>([]);
  const [showRevisions, setShowRevisions] = useState(false);
  const [revisionsToken, setRevisionsToken] = useState(0);
  const [expanded, setExpanded] = useState(false);
  const [naturalHeight, setNaturalHeight] = useState(0);
  const boxRef = useRef<HTMLDivElement>(null);
  const bodyRef = useRef<HTMLDivElement>(null);

  /**
   * How tall the info box wants to be, kept current by a ResizeObserver: a narrower window rewraps the
   * description and a hook that fits on a wide page overflows on a narrow one, so the handle has to
   * appear and disappear on its own rather than be decided once on mount.
   *
   * Measured on the body, not on the box: the box is the element being clipped, and its own height is
   * therefore whatever the clip allows. The body is a flex item, so it is a formatting context of its
   * own - the description's list margins are inside the height it reports instead of collapsing out.
   */
  useLayoutEffect(() => {
    const box = boxRef.current;
    const body = bodyRef.current;
    if (!box || !body) return;

    const measure = () => {
      const { paddingTop, paddingBottom } = getComputedStyle(box);
      const padding = parseFloat(paddingTop) + parseFloat(paddingBottom);
      setNaturalHeight(Math.ceil(body.getBoundingClientRect().height + padding));
    };
    measure();

    const observer = new ResizeObserver(measure);
    observer.observe(body);
    return () => observer.disconnect();
  }, [hook.description]);

  // Before the first measurement nothing is clipped, so a short description never flashes collapsed.
  const collapsible = naturalHeight > COLLAPSED_HEIGHT;
  const collapsed = collapsible && !expanded;

  const applySettings = useCallback((settings: HookSettings) => {
    setEnabled(Boolean(settings.enabled));
    setProperties(settings.properties ?? '');
  }, []);

  /**
   * Read the stored values. The backend derives the validation errors on every read, so reopening the hook
   * after it was upgraded reports the entries the new version needs before anything is edited.
   */
  const readSettings = useCallback(async () => {
    setLoadingError(false);
    try {
      const settings = await hooksApi.loadContent(hook.name);
      applySettings(settings);
      setValidationErrors(settings.validationErrors ?? []);
      setLoaded(true);
    } catch {
      setLoadingError(true);
      setLoaded(true);
    }
  }, [hooksApi, applySettings, hook.name]);

  useEffect(() => {
    void readSettings();
  }, [readSettings]);

  const handleSave = async () => {
    toast.dismiss();
    try {
      await hooksApi.saveContent(hook.name, { enabled, properties });
      setValidationErrors([]);
      setRevisionsToken((t) => t + 1);
      toast.success('Data successfully saved.');
    } catch (e) {
      // A rejected save lists every problem in the alert above, so the toast only says that the save failed -
      // repeating the whole list in it would say the same thing twice, at the other end of the page.
      //
      // A failure of any other kind - a network error, a 500, an SVN error - changed nothing, so whatever the
      // alert already said about the stored settings is still true and must stay on the page.
      const rejected = e instanceof SettingsError && e.validationErrors.length > 0;
      if (rejected) setValidationErrors((e as SettingsError).validationErrors);
      toast.error(
        rejected
          ? 'Data not saved: the settings are incomplete.'
          : (e as Error).message || 'Error occurred during saving the data.',
      );
    }
  };

  const handleCancel = async () => {
    if (!(await confirm('Are you sure you want to cancel editing and revert all changes made?'))) return;
    toast.dismiss();
    await readSettings();
  };

  const handleRevertToDefault = async () => {
    if (!(await confirm('Are you sure you want to return the default values?'))) return;
    toast.dismiss();
    try {
      applySettings(await hooksApi.loadDefaultContent(hook.name));
      // The hook's own defaults satisfy its own required entries, so nothing is left to report.
      setValidationErrors([]);
      toast.success("Default values set. Don't forget to save the data before leaving.");
    } catch {
      setLoadingError(true);
    }
  };

  const handleRevertToRevision = async (revision: Revision) => {
    try {
      const settings = await hooksApi.loadContent(hook.name, revision.name);
      applySettings(settings);
      // An old revision can miss an entry the installed version needs, and it is about to become the values
      // in the editor - so it is judged exactly like the stored ones.
      setValidationErrors(settings.validationErrors ?? []);
      toast.success(`Data reverted to revision ${revision.name}. Don't forget to save the data before leaving.`);
    } catch {
      setLoadingError(true);
    }
  };

  return (
    <>
      <div className="hook-info">
        {/* An explicit max-height in both states, rather than `none` when open: the CSS transition needs
            a number to animate to, and the observer above keeps the open one honest as the page resizes. */}
        <div
          ref={boxRef}
          id="hook-description"
          className="hook-description"
          style={collapsible ? { maxHeight: collapsed ? COLLAPSED_HEIGHT : naturalHeight } : undefined}
        >
          <div ref={bodyRef} className="hook-description-body">
            {/* The three facts read as one header line and wrap to the next only when the panel is too
                narrow, so a long description starts as high up the box as possible. */}
            <div className="hook-facts">
              <span>
                Affected item type(s): <b>{hook.itemTypes.map(itemTypeName).join(', ')}</b>
              </span>
              <span>
                Interceptor action type: <b>{actionTypeName(hook.actionType)}</b>
              </span>
              <span>
                Hook version: <b>{hook.version}</b>
              </span>
            </div>
            {/* Descriptions carry markup (<br>, <ul>, <b>) and are compiled into the hook jar an
                administrator deploys server-side - the same trust level as the extension itself. The
                legacy page rendered them with innerHTML too, so escaping them here showed raw tags. */}
            <div className="hook-description-text" dangerouslySetInnerHTML={{ __html: hook.description }} />
          </div>
          {collapsed && <div className="hook-description-fade" aria-hidden="true" />}
        </div>
        {collapsible && (
          <button
            type="button"
            className="hook-description-toggle"
            aria-expanded={expanded}
            aria-controls="hook-description"
            onClick={() => setExpanded((v) => !v)}
          >
            {expanded ? 'Show less ↑' : 'Show more ↓'}
          </button>
        )}
      </div>

      <div className="enable-hook">
        <label htmlFor="enable-hook">
          <input type="checkbox" id="enable-hook" checked={enabled} onChange={(e) => setEnabled(e.target.checked)} />
          <span>Enable</span>
        </label>
      </div>

      {/* generic's notifications.jsp block, verbatim: `.notifications .alert-*` (bundled in RSP's
          style.css) is what gives these the yellow/red boxes with the warning triangle. */}
      {(validationErrors.length > 0 || loadingError) && (
        <div className="notifications">
          {validationErrors.length > 0 && (
            <div className="alert alert-error validation-errors">
              The settings below are incomplete for version {hook.version} of this hook. They can not be saved and the
              hook can misbehave until they are repaired:
              <ul>
                {/* Keyed by index: two errors can be byte-identical, and the list is static and never reordered */}
                {validationErrors.map((error, index) => (
                  <li key={index}>{error}</li>
                ))}
              </ul>
            </div>
          )}
          {loadingError && <div className="alert alert-error">Error occurred loading data</div>}
        </div>
      )}

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
