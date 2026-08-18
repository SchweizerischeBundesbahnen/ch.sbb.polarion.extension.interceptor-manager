# Interceptor Manager UI

A React + Vite single-page app on [react-sbb-polarion](https://github.com/SchweizerischeBundesbahnen/react-sbb-polarion)
(RSP). It replaces the two legacy admin JSPs — `about.jsp` (a two-line wrapper around generic's
server-rendered About) and `configuration.jsp` with its `configuration.js` module.

Both pages are essentially RSP components: About is the library's shared page, and Configuration is
RSP's `PropertiesEditor` + `ConfigurationButtons` + `RevisionsTable`. This app supplies the REST hook,
the settings service and the page wiring.

## Feature routing

There is one `index.html` / bundle. The page to render is chosen from the `feature` query parameter:

- `/` (no param) renders a development landing page listing every feature.
- `/?feature=about` renders the About page.
- `/?feature=configuration` renders the Configuration page (the `.properties` document persisted as
  the generic `Default` setting, with Save / Cancel / Default / Revisions).

Features are declared in [`src/features.tsx`](src/features.tsx). Add a page component under
`src/pages/`, register it there, and it appears on the landing page automatically. The ids must stay
in sync with the `pageUrl`s in `src/main/resources/META-INF/hivemodule.xml` — a mismatch shows up as a
blank page in Polarion and no test catches it.

## Local development

No Polarion restart is needed to develop the UI:

```bash
cd ui
cp .env.local.template .env.local   # optional: VITE_BASE_URL / VITE_BEARER_TOKEN for real REST calls
npm install
npm run dev                          # http://localhost:5173/
```

REST calls are proxied to the Polarion instance in `VITE_BASE_URL`; a personal access token in
`VITE_BEARER_TOKEN` switches `useRemote` from the session `/internal` endpoints to the token `/api`
ones.

## Running the tests

**Locally, one command: `npm run test:coverage:docker`.** It runs the full suite (behavior + visual
regression) plus the 80% istanbul coverage gate inside the pinned Playwright Docker image, which is
what the Maven `test` phase and the pre-commit hook execute. Docker must be running.

```bash
npm run test:coverage:docker   # the canonical local run: full suite + coverage gate, in the pinned image
npm run test:coverage          # fast local loop: behavior only + the gate, no Docker, no pixels
npm run test:update:docker     # regenerate the committed reference PNGs after an intentional UI change
```

**In CI it is `npm run test:coverage:full`**, because the ESTA build agents have no docker-in-docker.
`estaCloudPipeline.json` therefore passes `-DjsTestsNoDocker -DinstallPlaywrightNoDeps`, which switches
the Maven `test` phase to that command and installs Chromium on the agent. It runs the behavior suite
and the coverage gate; the visual suites detect that they are not in the reference environment (the
`PIXEL_REFERENCES` flag the Docker wrapper sets) and **skip themselves** rather than failing on the
agent's font metrics. So the screenshots are verified locally and by review, never by ESTA.

## Formatting & linting

```bash
npm run format          # Prettier: format every file in place
npm run format:check    # Prettier: check only (what pre-commit / CI runs)
npm run lint            # ESLint: report problems
npm run lint:fix        # ESLint: auto-fix what it can
```

The repo's pre-commit hooks run `format:check`, `lint` and the dockerized coverage suite on any change
under `ui/`. They are check-only and never modify your files.

## Production build

`npm run build` emits the bundle to `ui/dist/app` with base path
`/polarion/interceptor-manager-app/ui/app/`. The Maven build (frontend-maven-plugin +
maven-resources-plugin) runs this automatically and copies the bundle into
`src/main/resources/webapp/interceptor-manager-app/app`, where `InterceptorManagerAppServlet` serves it at
`/polarion/interceptor-manager-app/ui/app/index.html`.
