import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, render } from 'vitest-browser-react';
import { page } from 'vitest/browser';
import { ITEM_TYPE_NAMES } from '../src/components/HookSettingsPanel';
import Settings from '../src/pages/Settings';
import { type Route, installFetchMock, jsonResponse } from './mockFetch';
import { settleBeforeCapture, settleLayout } from './visualHelpers';

// Docker-only snapshot of the Hooks settings page: the tab bar (RSP `Tabs`), the selected hook's
// description, the Enable checkbox, the .properties editor with its syntax highlighting, and the
// Save / Cancel / Default / Revisions toolbar. This is the page a styling change - here or in the
// shared library - would move without any behaviour test noticing.
//
// The selected hook's description is HTML, as real hooks write it: these snapshots are what show that
// the markup renders as a list and not as raw tags, folded to its collapsed height and unfolded by its handle.

const HOOKS = [
  {
    name: 'FirstHook',
    version: '1.0.0',
    description:
      'User can NOT delete workitems IF:<br>' +
      '<ul>' +
      '<li>document is not in <b>Draft</b> status</li>' +
      '<li>there are incoming links for the current workitem</li>' +
      '<li>the "status" field had history of changed status</li>' +
      '</ul>',
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

const routesFor = (hooks: typeof HOOKS): Route[] => [
  { method: 'GET', match: /\/hooks\?/, respond: () => jsonResponse(hooks) },
  { method: 'GET', match: /\/hook-settings\/[^/]+\/content/, respond: () => jsonResponse(CONTENT) },
  { method: 'GET', match: /\/hook-settings\/[^/]+\/revisions/, respond: () => jsonResponse([]) },
];

const ROUTES = routesFor(HOOKS);

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe.skipIf(!__PIXEL_REFERENCES__)('Hooks settings page visual', () => {
  it('a hook selected, enabled, with its properties document', async () => {
    installFetchMock(ROUTES);
    render(
      <div className="app standard-admin-page">
        <Settings />
      </div>,
    );

    await vi.waitFor(() => expect(document.querySelector('#properties-input')).not.toBeNull());
    const app = document.querySelector('.app') as HTMLElement;
    await settleLayout();
    await page.viewport(1280, Math.ceil(app.scrollHeight) + 40);
    await settleBeforeCapture();
    await expect(page.elementLocator(app)).toMatchScreenshot('settings-loaded');
  });

  it('the same hook with its description unfolded', async () => {
    // The snapshot above is the folded box: the clipped height, the fade over the cut and the handle.
    // This is what the handle opens - the other half of the same control.
    installFetchMock(ROUTES);
    render(
      <div className="app standard-admin-page">
        <Settings />
      </div>,
    );

    await vi.waitFor(() => expect(document.querySelector('#properties-input')).not.toBeNull());
    // The handle appears once the box has been measured, which is a render after the editor arrives.
    await vi.waitFor(() => expect(document.querySelector('.hook-description-toggle')).not.toBeNull());
    const toggle = document.querySelector<HTMLButtonElement>('.hook-description-toggle')!;
    toggle.click();
    await vi.waitFor(() => expect(toggle.getAttribute('aria-expanded')).toBe('true'));

    // Back to the default before measuring, as in the test below: the page fills the window, so a
    // viewport left tall by the previous test would measure tall and this capture would inherit it.
    await page.viewport(1280, 720);
    const app = document.querySelector('.app') as HTMLElement;
    await settleLayout();
    await page.viewport(1280, Math.ceil(app.scrollHeight) + 40);
    await settleBeforeCapture();
    await expect(page.elementLocator(app)).toMatchScreenshot('settings-description-unfolded');
  });

  it('a hook that affects every item type there is', async () => {
    // The facts row's edge case: every type the panel knows a name for on one hook, which is the
    // longest that line can get. Built from the panel's own map, so a type added there shows up here
    // instead of being remembered by nobody - and this snapshot is then the thing that has to be redone.
    const everyType = [
      {
        name: 'EveryItemTypeHook',
        // Matches CONTENT.hookVersion: a mismatch adds the yellow version warning, and this snapshot
        // is about the facts row, not about that box.
        version: '1.0.0',
        description: 'Checks something about all of them.',
        actionType: 'SAVE',
        itemTypes: Object.keys(ITEM_TYPE_NAMES),
      },
    ];
    installFetchMock(routesFor(everyType));
    render(
      <div className="app standard-admin-page">
        <Settings />
      </div>,
    );

    await vi.waitFor(() => expect(document.querySelector('#properties-input')).not.toBeNull());
    // Back to the default before measuring: the page fills the window, so a viewport left tall by the
    // previous test would measure tall and this capture would inherit it.
    await page.viewport(1280, 720);
    const app = document.querySelector('.app') as HTMLElement;
    await settleLayout();
    await page.viewport(1280, Math.ceil(app.scrollHeight) + 40);
    await settleBeforeCapture();
    await expect(page.elementLocator(app)).toMatchScreenshot('settings-every-item-type');
  });
});
