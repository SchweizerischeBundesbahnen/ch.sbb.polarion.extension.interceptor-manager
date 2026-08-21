import { Toaster } from '@sbb-polarion/react-sbb-polarion';
import { findFeature } from './features';
import Landing from './pages/Landing';

/**
 * Top-level feature router. There is a single index.html / bundle; the page to show is chosen from
 * the `feature` query parameter, e.g. `?feature=about`. No matching feature (including the bare root
 * `/`) renders the Landing stub, which lists links to every feature so the whole app can be exercised
 * in `vite dev` without a running Polarion.
 *
 * In Polarion, hivemodule.xml points the admin extenders at
 * `/polarion/interceptor-manager-app/ui/app/index.html?feature=about&embedded=true&scope=$scope$` and
 * `...?feature=settings&embedded=true&scope=$scope$`.
 */
export default function App() {
  const feature = new URLSearchParams(window.location.search).get('feature');
  const match = findFeature(feature);
  const Page = match ? match.component : Landing;

  return (
    // The `feature-<id>` class lets a single page opt into a layout the others must not get - here the
    // Settings page's full-height properties editor (App.css). `.app` supplies the page shell (RSP's
    // PageLayout.css) and `standard-admin-page` the --sbb-* control tokens and admin checkbox styling.
    <div className={`app standard-admin-page${match ? ` feature-${match.id}` : ''}`}>
      {/* App-wide toast host: the shared react-sbb-polarion Toaster (top-center + richColors, so
          success toasts are green, errors red). Toasts are fired with `toast()` from sonner. */}
      <Toaster />
      <Page />
    </div>
  );
}
