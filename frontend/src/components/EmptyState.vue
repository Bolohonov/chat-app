<template>
  <div class="empty">
    <div class="empty-glyph">
      <span class="bracket">[</span>
      <span class="inner">
        <span class="dot" v-for="i in 3" :key="i" :style="{ animationDelay: i * 0.3 + 's' }"></span>
      </span>
      <span class="bracket">]</span>
    </div>
    <div class="empty-title">SELECT A ROOM</div>
    <div class="empty-sub">Choose a room from the sidebar<br>or create a new one to start chatting</div>
  </div>
</template>

<style scoped>
.empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  background: var(--bg-deep);
}
.empty-glyph {
  display: flex;
  align-items: center;
  gap: 6px;
  font-family: var(--font-mono);
  font-size: 32px;
}
.bracket { color: var(--accent); opacity: 0.4; }
.inner { display: flex; gap: 8px; align-items: center; }
.dot {
  width: 10px; height: 10px;
  border-radius: 50%;
  background: var(--accent);
  opacity: 0.3;
  animation: pulse-dot 1.8s ease-in-out infinite;
}
.empty-title {
  font-family: var(--font-mono);
  font-size: 12px;
  letter-spacing: 4px;
  color: var(--text-muted);
}
.empty-sub {
  font-size: 13px;
  color: var(--text-ghost);
  text-align: center;
  line-height: 1.7;
}
</style>