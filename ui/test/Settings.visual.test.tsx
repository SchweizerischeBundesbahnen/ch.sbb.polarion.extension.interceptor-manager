import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, render } from 'vitest-browser-react';
import { page } from 'vitest/browser';
import Settings from '../src/pages/Settings';
import { installFetchMock, jsonResponse } from './mockFetch';

// Docker-only snapshot of the Hooks settings page: the tab bar (RSP `Tabs`), the selected hook's
// description, the Enable checkbox, the .properties editor with its syntax highlighting, and the
// Save / Cancel / Default / Revisions toolbar. This is the page a styling change - here or in the
// shared library - would move without any behaviour test noticing.

const HOOKS = [
  {
    name: 'FirstHook',
    version: '1.0.0',
    description: 'Checks the first thing.',
    actionType: 'SAVE',
    itemTypes: ['WORKITEM', 'MODULE_COMMENT'],
  },
  {
    name: 'SecondHook',
    version: '2.0.0',
    description: 'Checks the second thing.',
    actionType: 'DELETE',
    itemTypes: ['TESTRUN'],
  },
];

const CONTENT = {
  enabled: true,
  properties: '# the hook reads these\nthreshold=10\nmessage=too many links',
  hookVersion: '1.0.0',
};

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe.skipIf(!__PIXEL_REFERENCES__)('Hooks settings page visual', () => {
  it('a hook selected, enabled, with its properties document', async () => {
    installFetchMock([
      { method: 'GET', match: /\/hooks\?/, respond: () => jsonResponse(HOOKS) },
      { method: 'GET', match: /\/hook-settings\/[^/]+\/content/, respond: () => jsonResponse(CONTENT) },
      { method: 'GET', match: /\/hook-settings\/[^/]+\/revisions/, respond: () => jsonResponse([]) },
    ]);
    render(
      <div className="app standard-admin-page">
        <Settings />
      </div>,
    );

    await vi.waitFor(() => expect(document.querySelector('#properties-input')).not.toBeNull());
    const app = document.querySelector('.app') as HTMLElement;
    await page.viewport(1280, Math.ceil(app.scrollHeight) + 40);
    await expect(page.elementLocator(app)).toMatchScreenshot('settings-loaded');
  });
});
