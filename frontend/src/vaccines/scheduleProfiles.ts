export type VaccineScheduleProfile = 'GLOBAL' | 'AUSTRIA' | 'ICE_ACIP'

export interface ScheduleProfileInfo {
  key: VaccineScheduleProfile
  label: string
  description: string
  references: { label: string; url: string }[]
}

export const scheduleProfiles: ScheduleProfileInfo[] = [
  {
    key: 'GLOBAL',
    label: 'Global (generic)',
    description: 'Generic reminders. Schedules vary by country and personal risk.',
    references: [
      { label: 'European Vaccination Information Portal (EU)', url: 'https://vaccination-info.europa.eu/en' },
      { label: 'ECDC Vaccine Scheduler (EU/EEA)', url: 'https://vaccine-schedule.ecdc.europa.eu/' },
      { label: 'WHO – Vaccines and immunization', url: 'https://www.who.int/health-topics/vaccines-and-immunization' },
    ],
  },
  {
    key: 'AUSTRIA',
    label: 'Austria (Impfplan)',
    description: 'Uses Austrian schedule hints where implemented (best-effort).',
    references: [
      { label: 'Impfplan Österreich (Sozialministerium)', url: 'https://www.sozialministerium.gv.at/impfplan' },
      { label: 'ECDC Vaccine Scheduler (EU/EEA)', url: 'https://vaccine-schedule.ecdc.europa.eu/' },
      { label: 'Impfservice Wien (example local guidance)', url: 'https://impfservice.wien/' },
    ],
  },
  {
    key: 'ICE_ACIP',
    label: 'Official (ICE / ACIP, US)',
    description: 'Uses the external ICE decision support engine (ACIP-based). Requires Patient DOB and CVX-coded immunizations.',
    references: [
      { label: 'ICE (Immunization Calculation Engine) project', url: 'https://cdsframework.atlassian.net/wiki/spaces/ICE/overview' },
      { label: 'ICE Implementation Guide (DSS SOAP + vMR)', url: 'https://bitbucket-archive.softwareheritage.org/static/56/56340014-2e3f-4974-b8ac-e7ad3eee87e5/attachments/ICE-Implementation-Guide-distrib-trackchanges.pdf' },
      { label: 'CDC/ACIP vaccine recommendations (US)', url: 'https://www.cdc.gov/vaccines/hcp/acip-recs/index.html' },
    ],
  },
]

export function inferProfileFromCountry(country?: string | null): VaccineScheduleProfile {
  const c = (country ?? '').trim().toLowerCase()
  if (!c) return 'GLOBAL'
  if (c === 'at' || c.includes('austria') || c.includes('österreich') || c.includes('oesterreich')) return 'AUSTRIA'
  return 'GLOBAL'
}

const STORAGE_KEY = 'vax_registry_profile'

export function readScheduleProfileFromStorage(): VaccineScheduleProfile | null {
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY)
    return raw === 'AUSTRIA' || raw === 'GLOBAL' || raw === 'ICE_ACIP' ? raw : null
  } catch {
    return null
  }
}

export function writeScheduleProfileToStorage(profile: VaccineScheduleProfile) {
  try {
    window.localStorage.setItem(STORAGE_KEY, profile)
  } catch {
    // ignore
  }
}
