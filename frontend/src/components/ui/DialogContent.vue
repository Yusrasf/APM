<template>
  <div v-bind="attrs" :class="classes" @click.stop>
    <slot />
  </div>
</template>

<script setup lang="ts">
import { computed, useAttrs } from 'vue'
import { cn } from './utils'

defineOptions({ inheritAttrs: false })
const props = defineProps<{ class?: string }>()
const attrs = useAttrs()

const classes = computed(() =>
  cn(
    // Constrain dialog height so it never runs off-screen; allow internal scrolling.
    'mx-auto max-w-lg rounded-xl border bg-card text-card-foreground shadow-lg p-6 max-h-[85vh] overflow-y-auto',
    String(attrs.class ?? ''),
    props.class,
  ),
)
</script>
