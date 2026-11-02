# CLAUDE.md

## Gotchas

- **The administration UI is React on [react-sbb-polarion](https://github.com/SchweizerischeBundesbahnen/react-sbb-polarion) (RSP)**, not JSP. It lives in `ui/` (Vite + TypeScript) and is served from its own webapp context `interceptor-manager-app` by `InterceptorManagerAppServlet`; `hivemodule.xml` opens each page as `/polarion/interceptor-manager-app/ui/app/index.html?feature=<id>`, and those ids must match `ui/src/features.tsx`. There is no `interceptor-manager-admin` webapp - the generated `about.html` and the administration-menu icons live under `interceptor-manager-app` too. Details in [`ui/README.md`](ui/README.md).
- **The UI build is inherited, not declared here.** The generic parent's `ui-build-react-app` profile activates on the presence of `ui/package.json` and owns node/npm, `npm ci`, `npm run build`, the copy into `webapp/interceptor-manager-app/`, and the dockerized Vitest run with its coverage gate in the `test` phase. Do not copy a `frontend-maven-plugin` block into this pom; if the extension needs a JS build of its own, give each of its executions an explicit `<workingDirectory>${project.basedir}</workingDirectory>` - the parent's plugin-level `ui` value merges in otherwise.
- **`ch.sbb.polarion.extension.generic`** is the parent project providing reusable infrastructure for all Polarion plugins in this org (settings framework, REST base classes, OSGi helpers, etc.). Before implementing anything cross-cutting, check if it already exists there.
- **Maven Settings**: Builds require `.mvn/settings.xml` (JFrog, GitHub Packages, Sonatype credentials via env vars). CI passes it with `-s .mvn/settings.xml`.
- **Polarion Dependencies**: You must extract dependencies from the Polarion installer using [polarion-artifacts-deployer](https://github.com/SchweizerischeBundesbahnen/polarion-artifacts-deployer) before the Maven build will work.
- **Local Polarion Installation**: Requires `POLARION_HOME` environment variable. Use the `local-install-into-polarion` Maven profile: `mvn clean install -P local-install-into-polarion`
- **After any code change**: Delete `<POLARION_HOME>/data/workspace/.config` before restarting Polarion or changes won't be picked up.
- **Remote Debugging**: Add to Polarion's `config.sh`: `JAVA_OPTS="$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"`
- **Logging**: Polarion logs: `<POLARION_HOME>/polarion/logs/main/*.log`
- **Branch conventions**: Conventional commits enforced by commitizen (pre-commit hook). Feature branches: `feature/<name>`, bug fixes: `fix/<name>`, LTS branches: `release-v*` (e.g., `release-v6`).
- **Pre-commit hooks block internal patterns**: some org-specific identifiers are treated as secrets. Run `pre-commit run -a` after implementation.
