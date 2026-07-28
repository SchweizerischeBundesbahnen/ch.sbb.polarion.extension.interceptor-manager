import { Toaster } from '@grigoriev/react-sbb-polarion';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, render } from 'vitest-browser-react';
import Settings from '../src/pages/Settings';
import { type Route, installFetchMock, jsonResponse } from './mockFetch';

// The Hooks settings page: one tab per installed hook, the selected hook's Enable flag and .properties
// document, and the Save / Cancel / Default / Revisions toolbar. REST is mocked at the fetch boundary,
// so no Polarion is needed. The tab bar itself is react-sbb-polarion's Tabs and is tested there; what
// matters here is that picking a tab loads that hook and that every action addresses the right one.

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

const SETTINGS: Record<string, { enabled: boolean; properties: string; hookVersion: string }> = {
  FirstHook: { enabled: true, properties: 'a=1', hookVersion: '1.0.0' },
  SecondHook: { enabled: false, properties: 'b=2', hookVersion: '2.0.0' },
};

const hookOf = (url: string): string => decodeURIComponent(/\/hook-settings\/([^/]+)\//.exec(url)![1]);

/** The routes the page needs, with per-hook content. `overrides` replace matching routes wholesale. */
function routes(overrides: Route[] = [], hooks = HOOKS): Route[] {
  return [
    ...overrides,
    { method: 'GET', match: /\/hooks\?/, respond: () => jsonResponse(hooks) },
    {
      method: 'GET',
      match: /\/hook-settings\/[^/]+\/content/,
      respond: (url) => jsonResponse(SETTINGS[hookOf(url)]),
    },
    { method: 'PUT', match: /\/hook-settings\/[^/]+\/content/, respond: () => new Response(null, { status: 204 }) },
    {
      method: 'GET',
      match: /\/hook-settings\/[^/]+\/default-content/,
      respond: () => jsonResponse({ enabled: false, properties: 'default=yes' }),
    },
    { method: 'GET', match: /\/hook-settings\/[^/]+\/revisions/, respond: () => jsonResponse([]) },
  ];
}

const tabLabels = () => Array.from(document.querySelectorAll('.tabs .tab')).map((t) => (t.textContent ?? '').trim());
const activeTab = () => (document.querySelector('.tabs .tab.active')?.textContent ?? '').trim();
const enableBox = () => document.querySelector<HTMLInputElement>('#enable-hook')!;
const editor = () => document.querySelector<HTMLTextAreaElement>('#properties-input')!;
const button = (label: string): HTMLButtonElement => {
  const all = Array.from(document.querySelectorAll<HTMLButtonElement>('button'));
  const found = all.find((b) => (b.textContent ?? '').trim() === label);
  if (!found) throw new Error(`button not found: ${label}`);
  return found;
};

async function answerDialog(label: 'OK' | 'Cancel') {
  await vi.waitFor(() => expect(document.querySelector('.rsp-modal')).not.toBeNull());
  const target = Array.from(document.querySelectorAll<HTMLButtonElement>('.rsp-modal-footer .sbb-btn')).find(
    (b) => (b.textContent ?? '').trim() === label,
  );
  if (!target) throw new Error(`dialog button "${label}" not found`);
  target.click();
}

/** Render and wait for the first hook's settings to be on screen. */
async function mount(routeList: Route[] = routes()) {
  const fetchMock = installFetchMock(routeList);
  render(
    <>
      <Toaster />
      <Settings />
    </>,
  );
  await vi.waitFor(() => expect(document.querySelector('#properties-input')).not.toBeNull());
  return fetchMock;
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe('Hooks settings page', () => {
  it('lists every hook as a tab and opens the first one', async () => {
    await mount();
    expect(tabLabels()).toEqual(['FirstHook', 'SecondHook']);
    expect(activeTab()).toBe('FirstHook');
    expect(enableBox().checked).toBe(true);
    expect(editor().value).toBe('a=1');
  });

  it('describes the selected hook: item types, action type and version', async () => {
    await mount();
    const description = document.querySelector('.hook-description')!.textContent ?? '';
    // The backend enum names are shown in their readable form.
    expect(description).toContain('Work Item, Module Comment');
    expect(description).toContain('save');
    expect(description).toContain('1.0.0');
    expect(description).toContain('Checks the first thing.');
  });

  it('shows item and action types it has no name for as they came', async () => {
    // Hooks ship in their own jars, so a newer one can report a type this UI predates.
    const exotic = [{ ...HOOKS[0], actionType: 'ARCHIVE', itemTypes: ['RICH_PAGE'] }];
    SETTINGS.FirstHook = { enabled: true, properties: 'a=1', hookVersion: '1.0.0' };
    await mount(routes([], exotic));

    const description = document.querySelector('.hook-description')!.textContent ?? '';
    expect(description).toContain('RICH_PAGE');
    expect(description).toContain('ARCHIVE');
  });

  it('loads the picked hook when another tab is chosen', async () => {
    await mount();
    document.querySelectorAll<HTMLInputElement>('.tabs input[type="radio"]')[1].click();

    await vi.waitFor(() => expect(editor().value).toBe('b=2'));
    expect(activeTab()).toBe('SecondHook');
    expect(enableBox().checked).toBe(false);
    expect(document.querySelector('.hook-description')!.textContent).toContain('delete');
  });

  it('saves the edited values against the selected hook', async () => {
    let saved: { url: string; body: unknown } | undefined;
    const fetchMock = await mount(
      routes([
        {
          method: 'PUT',
          match: /\/hook-settings\/[^/]+\/content/,
          respond: (url, init) => {
            saved = { url, body: JSON.parse(String(init?.body)) };
            return new Response(null, { status: 204 });
          },
        },
      ]),
    );
    expect(fetchMock).toHaveBeenCalled();

    enableBox().click();
    button('Save').click();

    await vi.waitFor(() => expect(saved).toBeDefined());
    expect(saved!.url).toContain('/hook-settings/FirstHook/content');
    expect(saved!.body).toEqual({ enabled: false, properties: 'a=1' });
    await vi.waitFor(() => expect(document.body.textContent).toContain('successfully saved'));
  });

  it('reports a failed save as a toast carrying the message', async () => {
    await mount(
      routes([
        {
          method: 'PUT',
          match: /\/hook-settings\/[^/]+\/content/,
          respond: () => jsonResponse({ message: 'hook rejected the properties' }, 400),
        },
      ]),
    );

    button('Save').click();

    await vi.waitFor(() => expect(document.body.textContent).toContain('hook rejected the properties'));
  });

  it('re-reads the hook when the cancel is confirmed', async () => {
    await mount();
    enableBox().click();
    expect(enableBox().checked).toBe(false);

    button('Cancel').click();
    await answerDialog('OK');

    await vi.waitFor(() => expect(enableBox().checked).toBe(true));
  });

  it('keeps the edit when the cancel is dismissed', async () => {
    await mount();
    enableBox().click();

    button('Cancel').click();
    await answerDialog('Cancel');

    expect(enableBox().checked).toBe(false);
  });

  it('loads the default values when the revert is confirmed', async () => {
    await mount();

    button('Default').click();
    await answerDialog('OK');

    await vi.waitFor(() => expect(editor().value).toBe('default=yes'));
    expect(document.body.textContent).toContain("Don't forget to save");
  });

  it('keeps the values when the revert is dismissed', async () => {
    await mount();

    button('Default').click();
    await answerDialog('Cancel');

    expect(editor().value).toBe('a=1');
  });

  it('rescans the hooks folder only when the reload button asks for it', async () => {
    const fetchMock = await mount();
    const listCalls = () =>
      fetchMock.mock.calls.map(([input]) => String(input)).filter((url) => url.includes('/hooks?'));

    // The plain page load must not rescan - that is what `reload=false` means.
    expect(listCalls()).toEqual([expect.stringContaining('reload=false')]);

    button('Reload hooks list').click();

    await vi.waitFor(() => expect(listCalls()).toHaveLength(2));
    expect(listCalls()[1]).toContain('reload=true');
    await vi.waitFor(() => expect(document.body.textContent).toContain('Total hooks: 2'));
  });

  it('warns when the stored values came from another version of the hook', async () => {
    await mount(
      routes([
        {
          method: 'GET',
          match: /\/hook-settings\/[^/]+\/content/,
          respond: () => jsonResponse({ enabled: true, properties: 'a=1', hookVersion: '0.9.0' }),
        },
      ]),
    );

    await vi.waitFor(() => expect(document.querySelector('.notifications .alert-warning')).not.toBeNull());
    expect(document.querySelector('.alert-warning')!.textContent).toContain('different version of this hook');
  });

  it('says so, and offers no editor, when no hooks are installed', async () => {
    installFetchMock(routes([], []));
    render(<Settings />);

    await vi.waitFor(() => expect(document.querySelector('.alert-warning')).not.toBeNull());
    expect(document.querySelector('.alert-warning')!.textContent).toContain('No hooks found');
    expect(document.querySelector('#properties-input')).toBeNull();
    expect(document.querySelector('.tabs')).toBeNull();
  });

  it('reports a hooks list it could not load', async () => {
    installFetchMock([{ method: 'GET', match: /\/hooks\?/, json: { message: 'boom' }, status: 500 }]);
    render(<Settings />);

    await vi.waitFor(() => expect(document.querySelector('.alert-error')).not.toBeNull());
  });

  it('reports a hook whose settings could not be read', async () => {
    installFetchMock([
      { method: 'GET', match: /\/hooks\?/, json: HOOKS },
      { method: 'GET', match: /\/hook-settings\/[^/]+\/content/, json: { message: 'boom' }, status: 500 },
    ]);
    render(<Settings />);

    await vi.waitFor(() => expect(document.querySelector('.alert-error')).not.toBeNull());
  });

  it('lists the revisions of the selected hook and applies the one picked', async () => {
    await mount(
      routes([
        {
          method: 'GET',
          match: /\/hook-settings\/[^/]+\/revisions/,
          respond: () => jsonResponse([{ name: '4321', date: '2026-01-01', author: 'jdoe' }]),
        },
        {
          method: 'GET',
          match: /\/hook-settings\/[^/]+\/content\?revision=/,
          respond: () => jsonResponse({ enabled: false, properties: 'old=value' }),
        },
      ]),
    );

    button('Revisions').click();
    await vi.waitFor(() => expect(document.querySelector('.revision-number')).not.toBeNull());
    document.querySelector<HTMLButtonElement>('.revert-to-revision-button')!.click();

    await vi.waitFor(() => expect(editor().value).toBe('old=value'));
    expect(document.body.textContent).toContain('reverted to revision 4321');
  });

  it('reports defaults it could not load, leaving the values alone', async () => {
    await mount(
      routes([
        {
          method: 'GET',
          match: /\/hook-settings\/[^/]+\/default-content/,
          respond: () => jsonResponse({ message: 'boom' }, 500),
        },
      ]),
    );

    button('Default').click();
    await answerDialog('OK');

    await vi.waitFor(() => expect(document.querySelector('.alert-error')).not.toBeNull());
    expect(editor().value).toBe('a=1');
  });

  it('reports a revision it could not load', async () => {
    await mount(
      routes([
        {
          method: 'GET',
          match: /\/hook-settings\/[^/]+\/revisions/,
          respond: () => jsonResponse([{ name: '4321', date: '2026-01-01', author: 'jdoe' }]),
        },
        {
          method: 'GET',
          match: /\/hook-settings\/[^/]+\/content\?revision=/,
          respond: () => jsonResponse({ message: 'gone' }, 500),
        },
      ]),
    );

    button('Revisions').click();
    await vi.waitFor(() => expect(document.querySelector('.revision-number')).not.toBeNull());
    document.querySelector<HTMLButtonElement>('.revert-to-revision-button')!.click();

    await vi.waitFor(() => expect(document.querySelector('.alert-error')).not.toBeNull());
  });

  // The three shapes a failed response can carry, since each drives a different branch of the
  // message extraction: the two JSON field names generic uses, and a body that is not JSON at all.
  it.each([
    ['an errorMessage field', () => jsonResponse({ errorMessage: 'hook refused' }, 400), 'hook refused'],
    ['a plain-text body', () => new Response('gateway blew up', { status: 502 }), 'gateway blew up'],
    ['no body at all', () => new Response(null, { status: 503 }), 'HTTP 503'],
  ])('surfaces a save failure carrying %s', async (_name, respond, expected) => {
    await mount(routes([{ method: 'PUT', match: /\/hook-settings\/[^/]+\/content/, respond }]));

    button('Save').click();

    await vi.waitFor(() => expect(document.body.textContent).toContain(expected));
  });
});
