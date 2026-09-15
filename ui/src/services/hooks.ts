import { useCallback, useMemo } from 'react';
import type { Revision } from '@sbb-polarion/react-sbb-polarion';
import useRemote from './useRemote';

/**
 * A hook as the backend reports it. `name` is its identity everywhere: the tab label, the path
 * segment of every settings call, and the named-settings feature the values are stored under.
 */
export interface Hook {
  name: string;
  version: string;
  description: string;
  actionType: string;
  itemTypes: string[];
}

/**
 * Stored values of one hook. `validationErrors` lists what the installed hook version needs and the stored
 * settings do not have - the backend derives it on every read, so it is present without ever being stored.
 */
export interface HookSettings {
  enabled: boolean;
  properties: string;
  validationErrors?: string[];
}

/**
 * A rejected save. `validationErrors` carries the individual problems when the backend reported them one by
 * one; it is empty for every other failure, which leaves only the message.
 */
export class SettingsError extends Error {
  readonly validationErrors: string[];

  constructor(message: string, validationErrors: string[] = []) {
    super(message);
    this.name = 'SettingsError';
    this.validationErrors = validationErrors;
  }
}

/** Build the error for a failed Response (mirrors ExtensionContext.callAsync, plus the validation list). */
async function responseError(response: Response): Promise<SettingsError> {
  const text = await response.text().catch(() => '');
  if (text) {
    try {
      const parsed = JSON.parse(text) as {
        message?: string;
        errorMessage?: string;
        validationErrors?: string[];
      };
      const message = parsed?.message ?? parsed?.errorMessage;
      if (message) return new SettingsError(message, parsed.validationErrors ?? []);
    } catch {
      return new SettingsError(text);
    }
  }
  return new SettingsError(`HTTP ${response.status}`);
}

async function jsonOrThrow<T>(response: Response): Promise<T> {
  if (!response.ok) {
    throw await responseError(response);
  }
  return (await response.json()) as T;
}

async function okOrThrow(response: Response): Promise<void> {
  if (!response.ok) {
    throw await responseError(response);
  }
}

/**
 * REST helpers for the Settings page: the hooks list, and the values of one hook.
 *
 * Every hook is its own named-settings feature holding a single `Default` setting, which the backend
 * hides behind `/hook-settings/{hook}/…` - so nothing here has to know that. The pages are registered
 * for the repository scope only, so no scope is passed: the backend stores against the default scope.
 *
 * Built on `useRemote`, so it uses the session `/internal` endpoints in Polarion and the token `/api`
 * endpoints in `vite dev`.
 */
export default function useHooks() {
  const { sendRequest } = useRemote();

  const hookPath = useCallback((hook: string, suffix: string): string => {
    return `/hook-settings/${encodeURIComponent(hook)}${suffix}`;
  }, []);

  /**
   * The installed hooks. `reload` rescans the hooks folder for jars first - what the page's "Reload
   * hooks list" button asks for; the plain load must not, or every page open would rescan.
   */
  const loadHooks = useCallback(
    (reload: boolean): Promise<Hook[]> =>
      sendRequest({ method: 'GET', url: `/hooks?reload=${reload}` }).then((r) => jsonOrThrow<Hook[]>(r)),
    [sendRequest],
  );

  const loadContent = useCallback(
    (hook: string, revision?: string): Promise<HookSettings> => {
      const url = hookPath(hook, revision ? `/content?revision=${encodeURIComponent(revision)}` : '/content');
      return sendRequest({ method: 'GET', url }).then((r) => jsonOrThrow<HookSettings>(r));
    },
    [sendRequest, hookPath],
  );

  const saveContent = useCallback(
    (hook: string, settings: { enabled: boolean; properties: string }): Promise<void> =>
      sendRequest({
        method: 'PUT',
        url: hookPath(hook, '/content'),
        contentType: 'application/json',
        body: JSON.stringify(settings),
      }).then(okOrThrow),
    [sendRequest, hookPath],
  );

  const loadDefaultContent = useCallback(
    (hook: string): Promise<HookSettings> =>
      sendRequest({
        method: 'GET',
        url: hookPath(hook, '/default-content'),
      }).then((r) => jsonOrThrow<HookSettings>(r)),
    [sendRequest, hookPath],
  );

  /**
   * Signature matches what RevisionsTable injects (`(name, scope)`); the hook name arrives as `name`
   * and there is no scope to pass on.
   */
  const loadRevisions = useCallback(
    (hook: string): Promise<Revision[]> =>
      sendRequest({ method: 'GET', url: hookPath(hook, '/revisions') }).then((r) => jsonOrThrow<Revision[]>(r)),
    [sendRequest, hookPath],
  );

  return useMemo(
    () => ({
      loadHooks,
      loadContent,
      saveContent,
      loadDefaultContent,
      loadRevisions,
    }),
    [loadHooks, loadContent, saveContent, loadDefaultContent, loadRevisions],
  );
}
