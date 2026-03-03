<template>
  <div :class="['members-panel', { open: modelValue }]">
    <div class="panel-header">
      <span class="panel-title">участники_</span>
      <button class="panel-close" @click="emit('update:modelValue', false)">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <path d="M18 6L6 18M6 6l12 12"/>
        </svg>
      </button>
    </div>
    <div class="members-list">
      <div v-if="members.length === 0" class="members-empty">
        никого нет онлайн
      </div>
      <div v-for="member in members" :key="member.userId" class="member-item">
        <div class="member-avatar">{{ initials(member.displayName) }}</div>
        <div class="member-info">
          <span class="member-name">{{ member.displayName }}</span>
          <span class="member-status">
            <span class="status-dot"></span>
            онлайн
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  modelValue: Boolean,
  members: { type: Array, default: () => [] }
})
const emit = defineEmits(['update:modelValue'])

function initials(name) {
  return (name || '?').slice(0, 2).toUpperCase()
}
</script>

<style scoped>
.members-panel {
  width: 0;
  overflow: hidden;
  flex-shrink: 0;
  background: var(--bg-surface);
  border-left: 0px solid var(--border);
  display: flex;
  flex-direction: column;
  transition: width 0.25s ease, border-width 0.25s ease;
}
.members-panel.open {
  width: 220px;
  border-left-width: 1px;
}

.panel-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 16px; height: 60px; flex-shrink: 0;
  border-bottom: 1px solid var(--border);
}
.panel-title {
  font-family: var(--font-mono); font-size: 11px;
  letter-spacing: 2px; color: var(--text-muted); text-transform: uppercase;
}
.panel-close {
  width: 24px; height: 24px; border-radius: 4px;
  display: flex; align-items: center; justify-content: center;
  color: var(--text-muted); transition: all 0.2s; cursor: pointer;
}
.panel-close:hover { background: var(--bg-elevated); color: var(--text-primary); }

.members-list { flex: 1; overflow-y: auto; padding: 8px; }
.members-empty {
  padding: 20px; text-align: center;
  font-family: var(--font-mono); font-size: 12px; color: var(--text-ghost);
}
.member-item {
  display: flex; align-items: center; gap: 10px;
  padding: 8px 10px; border-radius: var(--radius-md);
  transition: background 0.15s;
}
.member-item:hover { background: var(--bg-elevated); }
.member-avatar {
  width: 32px; height: 32px; border-radius: 9px; flex-shrink: 0;
  background: var(--accent-glow); border: 1px solid var(--border-accent);
  color: var(--accent); font-family: var(--font-mono);
  font-size: 11px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}
.member-info { display: flex; flex-direction: column; min-width: 0; }
.member-name {
  font-size: 13px; font-weight: 500; color: var(--text-primary);
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.member-status {
  display: flex; align-items: center; gap: 4px;
  font-family: var(--font-mono); font-size: 10px; color: var(--accent);
  margin-top: 1px;
}
.status-dot {
  width: 5px; height: 5px; border-radius: 50%;
  background: var(--accent); box-shadow: 0 0 4px var(--accent);
}

@media (max-width: 767px) {
  .members-panel.open {
    position: absolute; right: 0; top: 0; bottom: 0;
    width: 200px; z-index: 20;
    box-shadow: -4px 0 20px rgba(0,0,0,0.4);
  }
}
</style>