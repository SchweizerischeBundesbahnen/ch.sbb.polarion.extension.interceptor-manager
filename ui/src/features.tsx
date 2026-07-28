import type { ComponentType } from 'react';
import About from './pages/About';
import Settings from './pages/Settings';

/**
 * A single navigable page of the app. The `id` is what appears in the URL as `?feature=<id>` and is
 * also what `hivemodule.xml` points its admin extenders at. Keep the ids stable and aligned with the
 * existing extender ids.
 *
 * This extension contributes two pages to the Polarion administration tree (the third admin extender,
 * "REST API", points straight at the Swagger UI and is not a page of this app).
 */
export interface Feature {
  id: string;
  label: string;
  description: string;
  component: ComponentType;
}

export const FEATURES: Feature[] = [
  {
    id: 'about',
    label: 'About',
    description: 'Extension version and general information.',
    component: About,
  },
  {
    id: 'settings',
    label: 'Hooks settings',
    description: 'Enable each installed hook and edit its properties, with revisions.',
    component: Settings,
  },
];

export function findFeature(id: string | null): Feature | undefined {
  return FEATURES.find((f) => f.id === id);
}
