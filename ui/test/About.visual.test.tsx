import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, render } from 'vitest-browser-react';
import { page } from 'vitest/browser';
import App from '../src/App';
import { installFetchMock } from './mockFetch';
import { settleBeforeCapture, settleLayout } from './visualHelpers';

// Docker-only full-page snapshot of the About page (the shared RSP About component fed this app's
// endpoints, mocked): the extension-info / properties / status tables, the README article - and this
// extension's own app icon, which is the one thing on the page that no behaviour test can assert.
// App.test.tsx only checks that an `.app-icon` element exists, which stayed true when the scaffold
// brought another extension's icon along.

const origUrl = window.location.pathname + window.location.search;

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
  window.history.replaceState({}, '', origUrl);
});

describe.skipIf(!__PIXEL_REFERENCES__)('About page visual', () => {
  it('loaded (info + properties + status tables, README article)', async () => {
    installFetchMock([
      {
        method: 'GET',
        match: /\/version$/,
        json: {
          bundleName: 'Interceptor Manager',
          bundleVendor: 'SBB',
          supportEmail: 'polarion-opensource@sbb.ch',
          automaticModuleName: 'ch.sbb.polarion.extension.interceptor_manager',
          bundleVersion: '1.0.0',
          bundleBuildTimestamp: '2026-07-01 10:00',
        },
      },
      {
        method: 'GET',
        match: /\/configuration-properties$/,
        json: {
          properties: [
            {
              key: 'ch.sbb.polarion.extension.interceptor-manager.some.property',
              value: 'value',
              defaultValue: 'value',
              description: 'An example configuration property',
            },
          ],
          obsoleteProperties: [],
        },
      },
      {
        method: 'GET',
        match: /\/configuration-status/,
        json: [{ name: 'Interceptor Manager', status: 'OK', details: 'ready' }],
      },
      {
        method: 'GET',
        match: /\/readme$/,
        respond: () =>
          new Response(
            '<h1>Interceptor Manager Extension for Polarion ALM</h1><p>Runs the installed hooks on Polarion save and delete actions.</p>',
            { status: 200 },
          ),
      },
    ]);
    window.history.replaceState({}, '', '?feature=about&embedded=true');
    render(<App />);

    await vi.waitFor(() => expect(document.querySelector('article.markdown-body')).not.toBeNull());
    const app = document.querySelector('.app') as HTMLElement;
    await settleLayout();
    await page.viewport(1280, Math.ceil(app.scrollHeight) + 40);
    await settleBeforeCapture();
    await expect(page.elementLocator(app)).toMatchScreenshot('about-loaded');
  });
});
