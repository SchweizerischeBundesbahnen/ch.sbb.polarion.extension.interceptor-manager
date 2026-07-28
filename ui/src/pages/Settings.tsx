import { useCallback, useEffect, useState } from 'react';
import { PageLayout, Tabs } from '@grigoriev/react-sbb-polarion';
import { toast } from 'sonner';
import HookSettingsPanel from '../components/HookSettingsPanel';
import useHooks from '../services/hooks';
import type { Hook } from '../services/hooks';

/**
 * Hooks settings admin page (the React equivalent of the legacy settings.jsp + settings.js). One tab
 * per installed hook; the selected hook's values are edited in HookSettingsPanel below the bar.
 *
 * This page owns only the list: which hooks exist, which one is selected, and the reload that rescans
 * the hooks folder. Everything about a hook lives in the panel, mounted per hook.
 */
export default function Settings() {
  const hooksApi = useHooks();

  const [hooks, setHooks] = useState<Hook[]>([]);
  const [selected, setSelected] = useState<Hook | undefined>(undefined);
  const [loaded, setLoaded] = useState(false);
  const [loadingError, setLoadingError] = useState(false);

  const readHooksList = useCallback(
    async (reload: boolean) => {
      setLoadingError(false);
      try {
        const list = await hooksApi.loadHooks(reload);
        setHooks(list);
        // A reload can retire the selected hook, so the selection restarts at the first one - which is
        // also what the very first load does.
        setSelected(list[0]);
        if (reload) {
          // The legacy page reported this in a modal, because vanilla JS had no toast host. It is a
          // transient outcome, so it is a toast here like every other one.
          toast.success(`Hooks list reloaded successfully. Total hooks: ${list.length}`);
        }
      } catch {
        setLoadingError(true);
      } finally {
        setLoaded(true);
      }
    },
    [hooksApi],
  );

  useEffect(() => {
    void readHooksList(false);
  }, [readHooksList]);

  if (!loaded) {
    return (
      <PageLayout title="Interceptor Manager: Hooks settings">
        <p>Loading...</p>
      </PageLayout>
    );
  }

  return (
    <PageLayout title="Interceptor Manager: Hooks settings">
      <div className="hooks-reload">
        <button
          type="button"
          className="toolbar-button"
          title="Reload jar files from the hooks containing folder"
          onClick={() => void readHooksList(true)}
        >
          <span className="button-image sbb-icon-reload" role="img" aria-label="Reload"></span>
          Reload hooks list
        </button>
      </div>

      {/* generic's notifications.jsp block, verbatim: `.notifications .alert-*` (bundled in RSP's
          style.css) is what gives these the yellow/red boxes with the warning triangle. */}
      {(hooks.length === 0 || loadingError) && (
        <div className="notifications">
          {loadingError ? (
            <div className="alert alert-error">Error occurred loading data</div>
          ) : (
            <div className="alert alert-warning">No hooks found. Please refer documentation.</div>
          )}
        </div>
      )}

      {hooks.length > 0 && (
        <div className="hooks-page">
          <Tabs
            items={hooks.map((hook) => ({ id: hook.name, label: hook.name }))}
            activeId={selected?.name}
            onSelect={(name) => setSelected(hooks.find((h) => h.name === name))}
            name="hook-name"
            ariaLabel="Hooks"
          />

          {selected && <HookSettingsPanel key={selected.name} hook={selected} />}
        </div>
      )}
    </PageLayout>
  );
}
