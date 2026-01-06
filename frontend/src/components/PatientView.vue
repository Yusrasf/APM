<template>
  <div class="max-w-6xl mx-auto space-y-6">
    <Card class="bg-white shadow-lg">
      <CardHeader class="border-b bg-gradient-to-r from-blue-50 to-green-50">
        <div class="flex items-start justify-between">
          <div>
            <CardTitle>
              <span v-if="patientDisplayName">Welcome, {{ patientDisplayName }}</span>
              <span v-else>Welcome</span>
            </CardTitle>
            <CardDescription class="mt-2 space-y-1">
              <div>Date of Birth: {{ patientBirthDate ?? 'Unknown' }}</div>
              <div>FHIR Patient ID: {{ fhirPatientId || 'Unknown' }}</div>
              <div>Identifier: {{ patientIdentifierLabel || 'None' }}</div>
              <div>Country: {{ patientCountry ?? 'Unknown' }}</div>
            </CardDescription>

            <p v-if="loading" class="mt-3 text-sm text-muted-foreground">Loading patient data…</p>
            <p v-else-if="error" class="mt-3 text-sm text-red-600">{{ error }}</p>
          </div>
          <Badge variant="outline" class="bg-green-50 text-green-700 border-green-200">
            <Shield class="w-3 h-3 mr-1" />
            Verified
          </Badge>
        </div>
      </CardHeader>
    </Card>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <Card>
        <CardContent class="pt-6">
          <div class="flex items-center gap-3">
            <div class="p-3 bg-green-100 rounded-lg">
              <CheckCircle2 class="w-6 h-6 text-green-600" />
            </div>
            <div>
              <div class="text-gray-500">Completed</div>
              <div class="text-green-600">{{ completedVaccinations.length }} vaccines</div>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="pt-6">
          <div class="flex items-center gap-3">
            <div class="p-3 bg-blue-100 rounded-lg">
              <Clock class="w-6 h-6 text-blue-600" />
            </div>
            <div>
              <div class="text-gray-500">Upcoming</div>
              <div class="text-blue-600">{{ upcomingVaccinations.length }} appointments</div>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="pt-6">
          <div class="flex items-center gap-3">
            <div class="p-3 bg-purple-100 rounded-lg">
              <Calendar class="w-6 h-6 text-purple-600" />
            </div>
            <div>
              <div class="text-gray-500">Next Visit</div>
              <div class="text-purple-600">
                {{ upcomingVaccinations.length > 0 ? nextVisit : 'None scheduled' }}
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>

    <Card class="bg-white shadow-sm">
      <CardHeader class="border-b">
        <div class="flex items-start justify-between gap-4">
          <div>
            <CardTitle>Reminders & vaccine information</CardTitle>
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

            <Badge variant="outline" class="border-blue-200 bg-blue-50 text-blue-700">
              <Info class="w-3 h-3 mr-1" />
              Educational
            </Badge>
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

    <Tabs defaultValue="all" class="w-full">
      <TabsList class="grid w-full max-w-md grid-cols-3">
        <TabsTrigger value="all">All Vaccines</TabsTrigger>
        <TabsTrigger value="completed">Completed</TabsTrigger>
        <TabsTrigger value="upcoming">Upcoming</TabsTrigger>
      </TabsList>

      <TabsContent value="all" class="mt-6 space-y-4">
        <VaccinationCard
          v-for="v in vaccinations"
          :key="v.id"
          :vaccination="v"
        />
      </TabsContent>

      <TabsContent value="completed" class="mt-6 space-y-4">
        <VaccinationCard
          v-for="v in completedVaccinations"
          :key="v.id"
          :vaccination="v"
        />
      </TabsContent>

      <TabsContent value="upcoming" class="mt-6 space-y-4">
        <template v-if="upcomingVaccinations.length > 0">
          <VaccinationCard
            v-for="v in upcomingVaccinations"
            :key="v.id"
            :vaccination="v"
          />
        </template>
        <Card v-else>
          <CardContent class="pt-6 text-center text-gray-500">
            No upcoming vaccinations scheduled
          </CardContent>
        </Card>
      </TabsContent>
    </Tabs>

    <div class="flex flex-col items-center gap-3">
      <div class="w-full max-w-md flex gap-2">
        <Input v-model="fhirPatientId" placeholder="FHIR Patient id (e.g., 123)" />
        <Button class="gap-2" @click="handleDownload" :disabled="!fhirPatientId">
          <Download class="w-4 h-4" />
          Download Vaccination Certificate
        </Button>
      </div>
      <p class="text-xs text-muted-foreground">Uses backend endpoint <code>/api/patient/{id}/certificate</code>.</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { Calendar, Download, Shield, Clock, CheckCircle2, Info, AlertTriangle } from 'lucide-vue-next'

import type { Vaccination } from '@/mockData'
import { backendFetch, downloadPdf, getIceForecast, type IceForecastResponse, type IceRecommendation } from '@/api/backend'

import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardDescription from '@/components/ui/CardDescription.vue'
import Badge from '@/components/ui/Badge.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'

import Tabs from '@/components/ui/Tabs.vue'
import TabsList from '@/components/ui/TabsList.vue'
import TabsTrigger from '@/components/ui/TabsTrigger.vue'
import TabsContent from '@/components/ui/TabsContent.vue'

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

const props = defineProps<{ patientId?: string }>()

const patientResource = ref<any | null>(null)
const vaccinations = ref<Vaccination[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

const fhirPatientId = ref('')

const scheduleProfile = ref<VaccineScheduleProfile>('GLOBAL')

const iceForecast = ref<IceForecastResponse | null>(null)
const iceLoading = ref(false)
const iceError = ref<string | null>(null)

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

  // If the recorded/occurrence date is in the future, show it as scheduled.
  const d = new Date(date)
  const now = new Date()
  const adjustedStatus = d.getTime() > now.getTime() ? 'scheduled' : status

  return {
    id: String(
      immunization?.id ||
        ((typeof crypto !== 'undefined' && 'randomUUID' in crypto)
          ? (crypto as any).randomUUID()
          : Math.random().toString(36).slice(2)),
    ),
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

async function loadAll(pid: string) {
  const id = pid.trim()
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
  const pid = (fhirPatientId.value || '').trim()
  if (!pid) return
  iceForecast.value = null
  iceError.value = null
  iceLoading.value = true
  try {
    iceForecast.value = await getIceForecast(pid)
  } catch (e) {
    iceError.value = String(e)
  } finally {
    iceLoading.value = false
  }
}

watch(
  () => props.patientId,
  (id) => {
    const pid = (id || '').trim()
    fhirPatientId.value = pid
    if (pid) void loadAll(pid)
  },
  { immediate: true },
)

async function handleDownload() {
  if (!fhirPatientId.value) return
  try {
    const path = "/api/patient/" + encodeURIComponent(fhirPatientId.value) + "/certificate"
    const name = "certificate-" + fhirPatientId.value + ".pdf"
    await downloadPdf(path, name)
  } catch (e) {
    window.alert(String(e))
  }
}

const patientDisplayName = computed(() => pickDisplayName(patientResource.value))
const patientBirthDate = computed(() => pickBirthDate(patientResource.value))
const patientCountry = computed(() => pickCountry(patientResource.value))
const patientIdentifierLabel = computed(() => pickIdentifierLabel(patientResource.value))

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
    if (p === 'ICE_ACIP') void loadIceForecast()
    else {
      iceForecast.value = null
      iceError.value = null
      iceLoading.value = false
    }
  },
)

watch(
  () => fhirPatientId.value,
  () => {
    if (scheduleProfile.value === 'ICE_ACIP') void loadIceForecast()
  },
)

const completedVaccinations = computed(() => vaccinations.value.filter((v) => v.status === 'completed'))
const upcomingVaccinations = computed(() => vaccinations.value.filter((v) => v.status === 'scheduled'))

const nextVisit = computed(() => {
  const first = upcomingVaccinations.value[0]
  return first ? new Date(first.date).toLocaleDateString() : ''
})

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
