<template>
  <div class="max-w-xl mx-auto">
    <Card class="shadow-lg bg-white">
      <CardHeader class="border-b bg-gradient-to-r from-blue-50 to-green-50">
        <CardTitle>Sign in</CardTitle>
        <CardDescription>
          Choose your portal and sign in to continue.
        </CardDescription>
      </CardHeader>

      <CardContent class="pt-6 space-y-6">
        <Tabs v-model="mode" class="w-full">
          <TabsList class="grid w-full grid-cols-2">
            <TabsTrigger value="patient">Patient</TabsTrigger>
            <TabsTrigger value="doctor">Doctor</TabsTrigger>
          </TabsList>

          <TabsContent value="patient" class="mt-6 space-y-4">
            <div class="space-y-2">
              <Label for="patientIdentifier">Patient identifier (preferred)</Label>
              <Input
                id="patientIdentifier"
                v-model="patientIdentifier"
                placeholder="e.g., http://hospital.example/mrn|12345 (or just 12345)"
                autocomplete="username"
              />
            </div>

            <div class="space-y-2">
              <Label for="patientName">Or patient name</Label>
              <Input
                id="patientName"
                v-model="patientName"
                placeholder="e.g., Doe"
                autocomplete="name"
              />
              <p class="text-xs text-muted-foreground">
                If multiple matches are found, you will be asked to select the correct patient.
              </p>
            </div>

            <div class="space-y-2">
              <Label for="patientBirthDate">Birth date (optional filter)</Label>
              <input
                id="patientBirthDate"
                type="date"
                v-model="patientBirthDate"
                class="border-input bg-input-background dark:bg-input/30 flex h-9 w-full rounded-md border px-3 text-sm outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50"
              />
            </div>

            <div v-if="patientCandidates.length" class="space-y-2">
              <p class="text-sm">Multiple patients found. Select one:</p>
              <div class="space-y-2">
                <button
                  v-for="c in patientCandidates"
                  :key="c.id"
                  type="button"
                  class="w-full text-left rounded-md border px-3 py-2 hover:bg-accent"
                  @click="selectPatient(c.id)"
                >
                  <div class="font-medium">{{ c.display }}</div>
                  <div class="text-xs text-muted-foreground">
                    <span>ID: {{ c.id }}</span>
                    <span v-if="c.birthDate"> · DOB: {{ c.birthDate }}</span>
                    <span v-if="c.identifier"> · {{ c.identifier }}</span>
                  </div>
                </button>
              </div>

              <Button variant="outline" class="w-full" @click="clearPatientCandidates" :disabled="loading">
                Search again
              </Button>
            </div>

            <p v-if="error" class="text-sm text-red-600">{{ error }}</p>

            <Button
              class="w-full"
              @click="signInPatient"
              :disabled="(!patientIdentifier.trim() && !patientName.trim()) || loading"
            >
              Continue to Patient Portal
            </Button>
          </TabsContent>

          <TabsContent value="doctor" class="mt-6 space-y-4">
            <div class="space-y-2">
              <Label for="doctorIdentifier">Practitioner identifier (preferred)</Label>
              <Input
                id="doctorIdentifier"
                v-model="doctorIdentifier"
                placeholder="e.g., http://hospital.example/doctor-id|D001 (or just D001)"
                autocomplete="username"
              />
            </div>

            <div class="space-y-2">
              <Label for="doctorName">Or practitioner name</Label>
              <Input
                id="doctorName"
                v-model="doctorName"
                placeholder="e.g., Smith"
                autocomplete="name"
              />
            </div>

            <div v-if="doctorCandidates.length" class="space-y-2">
              <p class="text-sm">Multiple practitioners found. Select one:</p>
              <div class="space-y-2">
                <button
                  v-for="c in doctorCandidates"
                  :key="c.id"
                  type="button"
                  class="w-full text-left rounded-md border px-3 py-2 hover:bg-accent"
                  @click="selectDoctor(c.id)"
                >
                  <div class="font-medium">{{ c.display }}</div>
                  <div class="text-xs text-muted-foreground">
                    <span>ID: {{ c.id }}</span>
                    <span v-if="c.identifier"> · {{ c.identifier }}</span>
                  </div>
                </button>
              </div>

              <Button variant="outline" class="w-full" @click="clearDoctorCandidates" :disabled="loading">
                Search again
              </Button>
            </div>

            <p v-if="error" class="text-sm text-red-600">{{ error }}</p>

            <Button
              class="w-full"
              @click="signInDoctor"
              :disabled="(!doctorIdentifier.trim() && !doctorName.trim()) || loading"
            >
              Continue to Doctor Portal
            </Button>
          </TabsContent>
        </Tabs>
      </CardContent>
    </Card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/auth/auth'

import Card from '@/components/ui/Card.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardContent from '@/components/ui/CardContent.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardDescription from '@/components/ui/CardDescription.vue'
import Input from '@/components/ui/Input.vue'
import Button from '@/components/ui/Button.vue'
import Label from '@/components/ui/Label.vue'

import Tabs from '@/components/ui/Tabs.vue'
import TabsList from '@/components/ui/TabsList.vue'
import TabsTrigger from '@/components/ui/TabsTrigger.vue'
import TabsContent from '@/components/ui/TabsContent.vue'

import { backendFetch } from '@/api/backend'

const router = useRouter()
const { loginAsPatient, loginAsDoctor } = useAuth()

const mode = ref<'patient' | 'doctor'>('patient')

type Candidate = {
  id: string
  display: string
  birthDate?: string | null
  identifier?: string | null
}

const patientIdentifier = ref('')
const patientName = ref('')
const patientBirthDate = ref('')
const doctorIdentifier = ref('')
const doctorName = ref('')

const patientCandidates = ref<Candidate[]>([])
const doctorCandidates = ref<Candidate[]>([])

const error = ref<string | null>(null)
const loading = ref(false)

function clearPatientCandidates() {
  patientCandidates.value = []
}

function clearDoctorCandidates() {
  doctorCandidates.value = []
}

function selectPatient(patientId: string) {
  loginAsPatient(patientId)
  router.push('/patient')
}

function selectDoctor(practitionerId: string) {
  loginAsDoctor(practitionerId)
  router.push('/doctor')
}

async function signInPatient() {
  const identifier = patientIdentifier.value.trim()
  const name = patientName.value.trim()
  if (!identifier && !name) return

  error.value = null
  loading.value = true
  try {
    const res = await backendFetch('/api/auth/patient/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        identifier: identifier || undefined,
        name: name || undefined,
        birthDate: patientBirthDate.value || undefined,
      }),
    })
    const json = await res.json() as { patientId?: string; candidates?: Candidate[] }

    if (json.patientId) {
      selectPatient(json.patientId)
      return
    }
    if (Array.isArray(json.candidates) && json.candidates.length) {
      patientCandidates.value = json.candidates
      return
    }
    throw new Error('Login failed: unexpected response')
  } catch (e) {
    error.value = String(e)
  } finally {
    loading.value = false
  }
}

async function signInDoctor() {
  const identifier = doctorIdentifier.value.trim()
  const name = doctorName.value.trim()
  if (!identifier && !name) return

  error.value = null
  loading.value = true
  try {
    const res = await backendFetch('/api/auth/doctor/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        identifier: identifier || undefined,
        name: name || undefined,
      }),
    })
    const json = await res.json() as { practitionerId?: string; candidates?: Candidate[] }

    if (json.practitionerId) {
      selectDoctor(json.practitionerId)
      return
    }
    if (Array.isArray(json.candidates) && json.candidates.length) {
      doctorCandidates.value = json.candidates
      return
    }
    throw new Error('Login failed: unexpected response')
  } catch (e) {
    error.value = String(e)
  } finally {
    loading.value = false
  }
}
</script>
