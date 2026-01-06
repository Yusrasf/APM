<template>
  <Dialog :open="open" @update:open="(v) => emit('update:open', v)">
    <DialogContent v-if="open" class="max-w-4xl max-h-[90vh] overflow-y-auto">
      <DialogHeader>
        <DialogTitle>Patient Medical Record</DialogTitle>
        <DialogDescription>FHIR patient details and vaccination history</DialogDescription>
      </DialogHeader>

      <div class="space-y-6">
        <div class="bg-gradient-to-r from-blue-50 to-purple-50 p-6 rounded-lg space-y-3">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              <div class="w-16 h-16 bg-gradient-to-br from-blue-500 to-purple-500 rounded-full flex items-center justify-center text-white text-xl">
                {{ initials(patientDisplayName) }}
              </div>
              <div>
                <h3 class="text-gray-900">{{ patientDisplayName || 'Patient' }}</h3>
                <p class="text-gray-600">FHIR Patient ID: {{ patientId }}</p>
              </div>
            </div>
            <Badge variant="outline" class="bg-white">
              <Shield class="w-3 h-3 mr-1" />
              Verified
            </Badge>
          </div>

          <Separator />

          <div class="grid grid-cols-2 md:grid-cols-3 gap-3 text-gray-700">
            <div class="flex items-center gap-2">
              <Calendar class="w-4 h-4 text-gray-500" />
              <span>DOB: {{ patientBirthDate ?? 'Unknown' }}</span>
            </div>
            <div class="flex items-center gap-2">
              <MapPin class="w-4 h-4 text-gray-500" />
              <span>{{ patientCountry ?? 'Unknown' }}</span>
            </div>
            <div class="flex items-center gap-2">
              <User class="w-4 h-4 text-gray-500" />
              <span>Identifier: {{ patientIdentifierLabel || 'None' }}</span>
            </div>
          </div>
          <p v-if="loading" class="text-sm text-muted-foreground">Loading…</p>
          <p v-else-if="error" class="text-sm text-red-600">{{ error }}</p>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div class="bg-green-50 p-4 rounded-lg border border-green-200">
            <div class="text-green-600">{{ completedVaccinations.length }}</div>
            <div class="text-gray-600">Completed Vaccinations</div>
          </div>
          <div class="bg-blue-50 p-4 rounded-lg border border-blue-200">
            <div class="text-blue-600">{{ upcomingVaccinations.length }}</div>
            <div class="text-gray-600">Future-dated Records</div>
          </div>
        </div>

        <Card class="bg-white shadow-sm">
          <CardHeader class="border-b">
            <div class="flex items-start justify-between gap-4">
              <div>
                <CardTitle>Reminders</CardTitle>
                <CardDescription>
                  Suggestions based on what is recorded in this registry. Recommendations vary by country and personal risk.
                </CardDescription>
              </div>
              <div class="flex flex-col items-end gap-2">
                <div class="text-xs text-muted-foreground">Schedule profile</div>
                <select
                  v-model="scheduleProfile"
                  class="border-input bg-input-background dark:bg-input/30 flex h-9 rounded-md border px-3 text-sm outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50"
                  aria-label="Schedule profile"
                >
                  <option v-for="p in scheduleProfiles" :key="p.key" :value="p.key">{{ p.label }}</option>
                </select>
              </div>
            </div>
          </CardHeader>
          <CardContent class="pt-6">
            <div v-if="scheduleProfile === 'ICE_ACIP'">
              <div v-if="iceLoading" class="text-sm text-gray-600">Loading official forecast…</div>
              <div v-else-if="iceError" class="text-sm text-red-600">{{ iceError }}</div>
              <div v-else-if="iceForecast" class="space-y-3">
                <div class="text-xs text-muted-foreground">
                  ICE endpoint: {{ iceForecast.endpointUrl }} • KM: {{ iceForecast.km.businessId }} {{ iceForecast.km.version }} • Included CVX doses: {{ iceForecast.inputDosesIncluded }}
                </div>

                <div v-if="iceForecast.recommendations.length === 0" class="text-sm text-gray-600">
                  No official recommendations returned.
                </div>

                <div v-else class="space-y-3">
                  <div
                    v-for="r in iceForecast.recommendations"
                    :key="(r.focusCode ?? '') + '|' + (r.recommendationCode ?? '')"
                    class="flex flex-col md:flex-row md:items-center justify-between gap-3 rounded-lg border p-4"
                  >
                    <div class="flex items-start gap-3">
                      <div class="mt-0.5">
                        <AlertTriangle v-if="iceStatus(r) === 'due'" class="w-5 h-5 text-red-600" />
                        <Clock v-else-if="iceStatus(r) === 'future'" class="w-5 h-5 text-orange-600" />
                        <CheckCircle2 v-else-if="iceStatus(r) === 'complete'" class="w-5 h-5 text-green-600" />
                        <Info v-else class="w-5 h-5 text-blue-600" />
                      </div>
                      <div>
                        <div class="flex items-center gap-2">
                          <div class="font-medium text-gray-900">{{ r.focusDisplayName || r.focusCode || 'Vaccine group' }}</div>
                          <Badge variant="outline" :class="iceBadgeClass(iceStatus(r))">{{ iceStatusLabel(r) }}</Badge>
                        </div>
                        <div v-if="r.reasonDisplay" class="mt-1 text-sm text-gray-600">{{ r.reasonDisplay }}</div>
                        <div class="mt-1 text-xs text-gray-500 space-x-3">
                          <span v-if="r.earliestDate">Earliest: {{ formatDate(r.earliestDate) }}</span>
                          <span v-if="r.recommendedDate">Recommended: {{ formatDate(r.recommendedDate) }}</span>
                          <span v-if="r.pastDueDate">Past due: {{ formatDate(r.pastDueDate) }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="text-sm text-gray-600">No official forecast loaded.</div>
            </div>

            <template v-else>
              <div v-if="vaccineReminders.length === 0" class="text-sm text-gray-600">
                No reminders available.
              </div>

              <div v-else class="space-y-3">
                <div
                  v-for="r in vaccineReminders"
                  :key="r.key"
                  class="flex flex-col md:flex-row md:items-center justify-between gap-3 rounded-lg border p-4"
                >
                  <div class="flex items-start gap-3">
                    <div class="mt-0.5">
                      <AlertTriangle v-if="r.status === 'due'" class="w-5 h-5 text-red-600" />
                      <Clock v-else-if="r.status === 'due-soon'" class="w-5 h-5 text-orange-600" />
                      <CheckCircle2 v-else-if="r.status === 'up-to-date'" class="w-5 h-5 text-green-600" />
                      <Info v-else class="w-5 h-5 text-blue-600" />
                    </div>
                    <div>
                      <div class="flex items-center gap-2">
                        <div class="font-medium text-gray-900">{{ r.title }}</div>
                        <Badge variant="outline" :class="statusBadgeClass(r.status)">{{ statusLabel(r.status) }}</Badge>
                      </div>
                      <div class="mt-1 text-sm text-gray-600">{{ r.message }}</div>
                      <div v-if="r.lastDoseDate" class="mt-1 text-xs text-gray-500">Last recorded: {{ formatDate(r.lastDoseDate) }}</div>
                    </div>
                  </div>

                  <div class="flex gap-2">
                    <Button variant="outline" size="sm" @click="openVaccineInfo(r)">Details</Button>
                  </div>
                </div>
              </div>
            </template>
          </CardContent>
        </Card>

        <VaccineInfoDialog
          v-if="selectedVaccineKey"
          v-model:open="infoOpen"
          :vaccineKey="selectedVaccineKey"
          :reminder="selectedReminder"
          :scheduleProfile="scheduleProfile"
        />

        <div class="space-y-3">
          <h4 class="text-gray-900">Vaccination History</h4>
          <div class="space-y-3">
            <VaccinationCard v-for="v in vaccinations" :key="v.id" :vaccination="v" />
            <Card v-if="!loading && vaccinations.length === 0">
              <CardContent class="pt-6 text-center text-gray-500">No immunizations found</CardContent>
            </Card>
          </div>
        </div>
      </div>
    </DialogContent>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { User, Calendar, MapPin, Shield, Clock, CheckCircle2, Info, AlertTriangle } from 'lucide-vue-next'

import type { Vaccination } from '@/mockData'
import { backendFetch, getIceForecast, type IceForecastResponse, type IceRecommendation } from '@/api/backend'

import Dialog from '@/components/ui/Dialog.vue'
import DialogContent from '@/components/ui/DialogContent.vue'
import DialogHeader from '@/components/ui/DialogHeader.vue'
import DialogTitle from '@/components/ui/DialogTitle.vue'
import DialogDescription from '@/components/ui/DialogDescription.vue'
import Badge from '@/components/ui/Badge.vue'
import Separator from '@/components/ui/Separator.vue'
import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardDescription from '@/components/ui/CardDescription.vue'
import Button from '@/components/ui/Button.vue'

import VaccinationCard from '@/components/VaccinationCard.vue'
import VaccineInfoDialog from '@/components/VaccineInfoDialog.vue'

import type { VaccineReminder, ReminderStatus } from '@/vaccines/reminders'
import { computeVaccineReminders } from '@/vaccines/reminders'
import type { VaccineKey } from '@/vaccines/vaccineCatalog'
import {
  scheduleProfiles,
  inferProfileFromCountry,
  readScheduleProfileFromStorage,
  writeScheduleProfileToStorage,
  type VaccineScheduleProfile,
} from '@/vaccines/scheduleProfiles'

const props = defineProps<{ patientId: string; open: boolean }>()
const emit = defineEmits<{ (e: 'update:open', v: boolean): void }>()

const patientResource = ref<any | null>(null)
const vaccinations = ref<Vaccination[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

const iceForecast = ref<IceForecastResponse | null>(null)
const iceLoading = ref(false)
const iceError = ref<string | null>(null)

function initials(display: string) {
  const parts = (display || '').trim().split(/\s+/).filter(Boolean)
  if (parts.length === 0) return '?'
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase()
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
}

function pickDisplayName(p: any | null): string {
  const n = p?.name?.[0]
  if (!n) return ''
  if (typeof n.text === 'string' && n.text.trim()) return n.text.trim()
  const given = Array.isArray(n.given) ? n.given.join(' ') : ''
  const family = typeof n.family === 'string' ? n.family : ''
  return `${given} ${family}`.trim()
}

function pickBirthDate(p: any | null): string | null {
  const bd = p?.birthDate
  return typeof bd === 'string' && bd.trim() ? bd : null
}

function pickIdentifierLabel(p: any | null): string {
  const ids = Array.isArray(p?.identifier) ? p.identifier : []
  const first = ids.find((i: any) => typeof i?.value === 'string' && i.value.trim())
  if (first?.system && first?.value) return `${first.system}|${first.value}`
  if (first?.value) return String(first.value)
  return ''
}

function pickCountry(p: any | null): string | null {
  const addrs = Array.isArray(p?.address) ? p.address : []
  const c = addrs.find((a: any) => typeof a?.country === 'string' && a.country.trim())?.country
  return typeof c === 'string' && c.trim() ? c.trim() : null
}

function toVaccination(immunization: any): Vaccination {
  const code = immunization?.vaccineCode
  const codings = Array.isArray(code?.coding) ? code.coding : []
  const cvx = codings.find((c: any) => c?.system === 'http://hl7.org/fhir/sid/cvx') ?? codings[0] ?? null
  const vaccineName = (cvx?.display || code?.text || 'Unknown').toString()
  const vaccineType = (cvx?.system || 'Immunization').toString()
  const vaccineSystem = typeof cvx?.system === 'string' ? cvx.system : undefined
  const vaccineCode = typeof cvx?.code === 'string' ? cvx.code : undefined

  const occurrence = immunization?.occurrenceDateTime || immunization?.occurrenceString || immunization?.recorded
  const date = typeof occurrence === 'string' && occurrence.trim() ? occurrence : new Date().toISOString().slice(0, 10)

  const pa0 = Array.isArray(immunization?.protocolApplied) ? immunization.protocolApplied[0] : null
  const doseNumber = Number(pa0?.doseNumberPositiveInt ?? 1)
  const totalDoses = Number(pa0?.seriesDosesPositiveInt ?? doseNumber)

  const manufacturer = immunization?.manufacturer?.display || immunization?.manufacturer?.reference || 'Unknown'
  const batchNumber = immunization?.lotNumber || 'Unknown'

  const performer0 = Array.isArray(immunization?.performer) ? immunization.performer[0] : null
  const administeredBy = performer0?.actor?.display || performer0?.actor?.reference || 'Unknown'
  const location = immunization?.location?.display || immunization?.location?.reference || 'Unknown'

  const status = String(immunization?.status || '').toLowerCase() === 'completed' ? 'completed' : 'scheduled'

  const d = new Date(date)
  const now = new Date()
  const adjustedStatus = d.getTime() > now.getTime() ? 'scheduled' : status

  return {
    id: String(immunization?.id || Math.random().toString(36).slice(2)),
    vaccineName,
    vaccineType,
    vaccineSystem,
    vaccineCode,
    date: new Date(date).toISOString().slice(0, 10),
    doseNumber: Number.isFinite(doseNumber) ? doseNumber : 1,
    totalDoses: Number.isFinite(totalDoses) && totalDoses > 0 ? totalDoses : 1,
    manufacturer: String(manufacturer),
    batchNumber: String(batchNumber),
    administeredBy: String(administeredBy).replace(/^Practitioner\//, ''),
    location: String(location),
    status: adjustedStatus as 'completed' | 'scheduled',
  }
}

async function loadAll() {
  const id = props.patientId.trim()
  if (!id) return
  error.value = null
  loading.value = true
  try {
    const pRes = await backendFetch(`/Patient/${encodeURIComponent(id)}`)
    patientResource.value = await pRes.json()

    const iRes = await backendFetch(`/api/patient/${encodeURIComponent(id)}/immunizations`)
    const bundle = await iRes.json()
    const entries = Array.isArray(bundle?.entry) ? bundle.entry : []
    vaccinations.value = entries
      .map((e: any) => e?.resource)
      .filter((r: any) => r && r.resourceType === 'Immunization')
      .map(toVaccination)
      .sort((a: Vaccination, b: Vaccination) => (a.date < b.date ? 1 : -1))
  } catch (e) {
    vaccinations.value = []
    patientResource.value = null
    error.value = String(e)
  } finally {
    loading.value = false
  }
}

async function loadIceForecast() {
  const id = props.patientId.trim()
  if (!id) return
  iceForecast.value = null
  iceError.value = null
  iceLoading.value = true
  try {
    iceForecast.value = await getIceForecast(id)
  } catch (e) {
    iceError.value = String(e)
  } finally {
    iceLoading.value = false
  }
}

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      void loadAll()
      if (scheduleProfile.value === 'ICE_ACIP') void loadIceForecast()
    } else {
      iceForecast.value = null
      iceError.value = null
      iceLoading.value = false
    }
  },
)

watch(
  () => props.patientId,
  () => {
    if (props.open) {
      void loadAll()
      if (scheduleProfile.value === 'ICE_ACIP') void loadIceForecast()
    }
  },
)

const patientDisplayName = computed(() => pickDisplayName(patientResource.value))
const patientBirthDate = computed(() => pickBirthDate(patientResource.value))
const patientCountry = computed(() => pickCountry(patientResource.value))
const patientIdentifierLabel = computed(() => pickIdentifierLabel(patientResource.value))

const scheduleProfile = ref<VaccineScheduleProfile>('GLOBAL')

watch(
  () => patientCountry.value,
  (c) => {
    const stored = readScheduleProfileFromStorage()
    scheduleProfile.value = stored ?? inferProfileFromCountry(c)
  },
  { immediate: true },
)

watch(
  () => scheduleProfile.value,
  (p) => {
    writeScheduleProfileToStorage(p)
    if (p === 'ICE_ACIP' && props.open) void loadIceForecast()
    if (p !== 'ICE_ACIP') {
      iceForecast.value = null
      iceError.value = null
      iceLoading.value = false
    }
  },
)

const completedVaccinations = computed(() => vaccinations.value.filter((v) => v.status === 'completed'))
const upcomingVaccinations = computed(() => vaccinations.value.filter((v) => v.status === 'scheduled'))

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('en-GB')
}

const vaccineReminders = computed(() =>
  computeVaccineReminders({
    vaccinations: vaccinations.value,
    patientBirthDate: patientBirthDate.value,
    patientCountry: patientCountry.value,
    scheduleProfile: scheduleProfile.value,
  }),
)

const infoOpen = ref(false)
const selectedVaccineKey = ref<VaccineKey | null>(null)
const selectedReminder = ref<VaccineReminder | null>(null)

function openVaccineInfo(r: VaccineReminder) {
  selectedVaccineKey.value = r.key
  selectedReminder.value = r
  infoOpen.value = true
}

function statusLabel(s: ReminderStatus): string {
  switch (s) {
    case 'due':
      return 'Due'
    case 'due-soon':
      return 'Due soon'
    case 'missing':
      return 'Missing'
    case 'up-to-date':
      return 'Up to date'
    default:
      return 'Check'
  }
}

function statusBadgeClass(s: ReminderStatus): string {
  if (s === 'due') return 'border-red-200 bg-red-50 text-red-700'
  if (s === 'due-soon') return 'border-orange-200 bg-orange-50 text-orange-700'
  if (s === 'missing') return 'border-gray-200 bg-gray-50 text-gray-700'
  if (s === 'up-to-date') return 'border-green-200 bg-green-50 text-green-700'
  return 'border-blue-200 bg-blue-50 text-blue-700'
}

type IceStatus = 'due' | 'future' | 'complete' | 'other'

function iceStatus(rec: IceRecommendation): IceStatus {
  const c = String(rec.recommendationCode || '').toUpperCase()
  if (c === 'OVERDUE' || c === 'DUE_NOW' || c === 'DUE') return 'due'
  if (c === 'FUTURE') return 'future'
  if (c === 'COMPLETE') return 'complete'
  return 'other'
}

function iceStatusLabel(rec: IceRecommendation): string {
  return rec.recommendationDisplay || rec.recommendationCode || 'Status'
}

function iceBadgeClass(s: IceStatus): string {
  if (s === 'due') return 'border-red-200 bg-red-50 text-red-700'
  if (s === 'future') return 'border-orange-200 bg-orange-50 text-orange-700'
  if (s === 'complete') return 'border-green-200 bg-green-50 text-green-700'
  return 'border-blue-200 bg-blue-50 text-blue-700'
}
</script>
