<script setup>
defineProps({
  title: String,
  items: {
    type: Array,
    default: () => [],
  },
});

// 문장 줄바꿈 처리
const formatToSentences = (text) => {
  return text
    .split(/(?<=\.)\s+/) // .뒤 공백 기준 분리
    .map((s) => s.trim()) // 양쪽 공백 제거
    .filter(Boolean) // 빈 문자열 제거
    .join('<br>'); // 줄바꿈
};

// 데이터 내부의 a링크 처리를 위해 html태그 여부 확인
const isHtml = (text) => /<\/?[a-z][\s\S]*>/i.test(text);
</script>

<template>
  <section class="GuidebookSection checklist-section mb-5">
    <h5 class="fw-bold mb-3">{{ title }}</h5>

    <ul class="list-group">
      <li
        v-for="(item, index) in items"
        :key="index"
        class="list-group-item border-0 ps-0"
      >
        <!-- 메인 항목 -->
        <div class="fw-semibold mb-2">{{ index + 1 }}. {{ item.main }}</div>
        <!-- 피해 사례 -->
        <div
          class="text-secondary small mb-1"
          v-if="item.warning"
          v-html="formatToSentences(item.warning)"
        ></div>
        <!-- 참고 설명 -->
        <!-- note에 html 태그(a태그 등) 있으면 직접 출력 -->
        <div
          class="text-green small"
          v-if="item.note && isHtml(item.note)"
          v-html="item.note"
        ></div>
        <!-- 없으면 줄바꿈 출력 -->
        <div
          class="text-green small"
          v-else-if="item.note"
          v-html="formatToSentences(item.note)"
        ></div>
      </li>
    </ul>
  </section>
</template>

<style scoped>
.text-green {
  color: #007c06;
}

.text-main {
  color: #1a80e5;
}
</style>
