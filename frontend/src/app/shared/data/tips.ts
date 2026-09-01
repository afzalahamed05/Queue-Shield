/** Rotating tip content shown by <app-tip-banner>, one at a time, advancing on every route
 * change. Two genres, interleaved: real emergency-preparedness guidance, and guidance on how
 * QueueShield's own features actually work - both real content, not filler text. */

export interface Tip {
  readonly kind: 'safety' | 'app';
  readonly text: string;
}

export const TIPS: readonly Tip[] = [
  { kind: 'safety', text: 'In a fire: stay low under smoke, and never use elevators during an evacuation.' },
  { kind: 'app', text: "Priority scores recompute automatically when an incident's details change — no manual refresh needed." },
  { kind: 'safety', text: 'During a flood: move to higher ground immediately and never walk or drive through moving water.' },
  { kind: 'app', text: 'Dispatching a responder locks them to one incident at a time, so the same unit can never be double-booked.' },
  { kind: 'safety', text: 'In an earthquake: Drop, Cover, and Hold On until the shaking fully stops.' },
  { kind: 'app', text: "A resource request can briefly show 'Pending' — it's being reserved asynchronously and usually resolves in a few seconds." },
  { kind: 'safety', text: 'If a structure is reported unstable or collapsed, keep well back and let trained responders assess it first.' },
  { kind: 'app', text: 'The priority tier (Low/Medium/High/Critical) is computed from severity, people affected, and vulnerability — never manually set.' },
  { kind: 'safety', text: 'In a gas or chemical leak, move upwind, avoid open flames, and report the exact location right away.' },
  { kind: 'app', text: 'Shelters show live remaining capacity — check availability there before assigning an incident to one.' },
  { kind: 'safety', text: 'If you evacuate, take medication, water, and ID documents with you if there is time to do so safely.' },
  { kind: 'app', text: 'Completing an assignment automatically frees its responder and resource back to available status.' },
  { kind: 'safety', text: 'Never re-enter a damaged building until officials have confirmed it is safe to do so.' },
  { kind: 'app', text: "The Notifications tab surfaces critical incidents and completed assignments — worth checking after any dispatch." },
  { kind: 'safety', text: 'During severe weather, keep a battery- or hand-powered radio on hand in case power and signal go down.' },
  { kind: 'app', text: "\"Active Incidents\" on the dashboard counts everything not yet resolved or closed — it's a live operational total, not a historical one." },
];
