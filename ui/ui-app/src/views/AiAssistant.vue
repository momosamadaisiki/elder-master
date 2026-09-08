<script setup>
  import {nextTick, onMounted, ref} from 'vue'
  import {showToast} from 'vant'
  import {useRouter} from 'vue-router'
  import aiApi from '@/api/ai.js'

  const router = useRouter()
  const inputText = ref('')
  const sending = ref(false)
  const typing = ref(false)
  const messageListEl = ref()
  const bodyEl = ref()

  //greeting: true 的首条欢迎语不随请求回传（避免重复占用上下文）
  const messages = ref([
    {
      role: 'assistant',
      greeting: true,
      content: '您好，我是康养小智，您的社区健康助手。\n我可以帮您看健康数据、讲解慢病与用药注意事项、给饮食运动建议、做跌倒预防指导等，会结合您的档案与最近护理/体检记录来回答。\n\n请注意：我是健康管理助手，不能替代医生诊断和治疗；遇到紧急情况请立即拨打急救电话。'
    }
  ])

  const suggestions = [
    '帮我看看最近的体检结果',
    '血压有点高要注意什么',
    '最近老人跌倒风险怎么防',
    '血糖偏高时饮食怎么安排'
  ]

  const scrollToBottom = async () => {
    await nextTick()
    if (messageListEl.value) {
      messageListEl.value.scrollTop = messageListEl.value.scrollHeight
    }
  }

  const send = async (text) => {
    const content = (text ?? inputText.value).trim()
    if (!content || sending.value) return
    inputText.value = ''
    messages.value.push({role: 'user', content, time: new Date().toLocaleTimeString('zh-CN', {hour: '2-digit', minute: '2-digit'})})
    await scrollToBottom()

    //把历史（除欢迎语外）回传给后端
    const history = messages.value
        .filter(m => !m.greeting)
        .slice(-20)
        .map(m => ({role: m.role, content: m.content}))

    sending.value = true
    typing.value = true
    try {
      const result = await aiApi.chat(history)
      if (result.code === 1) {
        messages.value.push({
          role: 'assistant',
          content: result.data.reply || '……',
          time: new Date().toLocaleTimeString('zh-CN', {hour: '2-digit', minute: '2-digit'})
        })
      } else {
        showToast(result.msg || 'AI 服务暂时不可用')
      }
    } catch (e) {
      showToast('AI 服务暂时不可用')
    } finally {
      sending.value = false
      typing.value = false
      await scrollToBottom()
    }
  }

  const onQuick = (s) => send(s)

  onMounted(scrollToBottom)
</script>

<template>
  <div class="ai-page">
    <!-- 顶部 -->
    <div class="ai-header">
      <van-icon name="arrow-left" class="back" @click="router.back()"/>
      <div class="header-main">
        <div class="header-title">康养小智</div>
        <div class="header-sub">社区 AI 健康助手 · 非医疗诊断</div>
      </div>
      <van-icon name="service-o" class="header-logo" color="#fff" size="22"/>
    </div>

    <!-- 免责说明 -->
    <div class="disclaimer">遇胸痛、呼吸困难、意识不清等紧急情况，请立即拨打 120 或联系社区，不要等待在线答复。</div>

    <!-- 消息列表 -->
    <div class="chat-body" ref="bodyEl">
      <div class="chat-list" ref="messageListEl">
        <div
            v-for="(msg, index) in messages"
            :key="index"
            class="msg-row"
            :class="msg.role === 'user' ? 'row-user' : 'row-assistant'"
        >
          <div v-if="msg.role === 'assistant'" class="avatar ai">智</div>
          <div class="bubble-wrap">
            <div class="bubble" :class="msg.role === 'user' ? 'bubble-user' : 'bubble-assistant'">
              <span class="content">{{ msg.content }}</span>
            </div>
            <div v-if="msg.time" class="time">{{ msg.time }}</div>
          </div>
        </div>

        <!-- 输入中的动画气泡 -->
        <div v-if="typing" class="msg-row row-assistant">
          <div class="avatar ai">智</div>
          <div class="bubble bubble-assistant bubble-typing">
            <span class="dot"></span><span class="dot"></span><span class="dot"></span>
          </div>
        </div>
      </div>
    </div>

    <!-- 快捷提问 -->
    <div class="suggest-area" v-if="messages.length <= 1 && !typing">
      <div class="suggest-title">你可以这样问我</div>
      <div class="suggest-chips">
        <div v-for="s in suggestions" :key="s" class="chip" @click="onQuick(s)">{{ s }}</div>
      </div>
    </div>

    <!-- 输入区 -->
    <div class="input-bar">
      <textarea
          v-model="inputText"
          class="input-text"
          rows="1"
          placeholder="说下您的身体情况或想问的问题…"
          @keydown.enter.prevent="send()"
      ></textarea>
      <button class="send-btn" :disabled="!inputText.trim() || sending" @click="send()">
        <van-icon name="chat-o" size="18"/> 发送
      </button>
    </div>
  </div>
</template>

<style scoped lang="scss">
  .ai-page {
    display: flex;
    flex-direction: column;
    height: calc(100vh - 50px);
    background: #f5f6f8;
  }

  /* 顶部 */
  .ai-header {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 16px;
    color: #fff;
    background: linear-gradient(120deg, #0d9488 0%, #0891b2 100%);

    .back {
      font-size: 20px;
    }

    .header-main {
      flex: 1;
    }

    .header-title {
      font-size: 17px;
      font-weight: bold;
      letter-spacing: 1px;
    }

    .header-sub {
      margin-top: 2px;
      font-size: 11px;
      opacity: 0.85;
    }
  }

  .disclaimer {
    padding: 6px 12px;
    font-size: 11px;
    color: #b4541d;
    background: #fff7e6;
    border-bottom: 1px solid #ffe7ba;
    line-height: 1.5;
  }

  /* 消息区 */
  .chat-body {
    flex: 1;
    overflow-y: auto;
    padding: 14px 12px 6px;
  }

  .chat-list {
    min-height: 100%;
  }

  .msg-row {
    display: flex;
    margin-bottom: 14px;
    align-items: flex-start;
  }

  .row-user {
    justify-content: flex-end;
  }

  .avatar {
    width: 34px;
    height: 34px;
    margin-right: 8px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 16px;
    font-weight: bold;
    flex-shrink: 0;

    &.ai {
      background: linear-gradient(135deg, #14b8a6, #0891b2);
    }
  }

  .bubble-wrap {
    max-width: 76%;
    display: flex;
    flex-direction: column;
  }

  .row-user .bubble-wrap {
    align-items: flex-end;
  }

  .bubble {
    padding: 10px 12px;
    border-radius: 12px;
    font-size: 14px;
    line-height: 1.6;
    word-break: break-word;

    .content {
      white-space: pre-wrap;
    }

    &.bubble-assistant {
      color: #323233;
      background: #fff;
      border-top-left-radius: 4px;
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
    }

    &.bubble-user {
      color: #fff;
      background: linear-gradient(120deg, #14b8a6, #0891b2);
      border-top-right-radius: 4px;
    }

    &.bubble-typing {
      display: inline-flex;
      gap: 4px;
      align-items: center;
      padding: 14px 16px;
    }
  }

  .dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: #94a3b8;
    animation: blink 1.2s infinite;

    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }

  @keyframes blink {
    0%, 60%, 100% { opacity: 0.3; transform: translateY(0); }
    30% { opacity: 1; transform: translateY(-2px); }
  }

  .time {
    margin-top: 4px;
    font-size: 10px;
    color: #b6b7b9;
  }

  /* 快捷提问 */
  .suggest-area {
    padding: 0 12px 8px;

    .suggest-title {
      margin-bottom: 8px;
      font-size: 12px;
      color: #969799;
    }

    .suggest-chips {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }

    .chip {
      padding: 7px 12px;
      font-size: 13px;
      color: #0d9488;
      background: #e8f8f5;
      border: 1px solid #b5ece3;
      border-radius: 16px;
      cursor: pointer;
      transition: all 0.2s;

      &:active {
        background: #d5f3ec;
      }
    }
  }

  /* 输入区 */
  .input-bar {
    display: flex;
    align-items: flex-end;
    gap: 8px;
    padding: 8px 10px calc(8px + env(safe-area-inset-bottom));
    background: #fff;
    border-top: 1px solid #ebedf0;

    .input-text {
      flex: 1;
      max-height: 88px;
      min-height: 36px;
      padding: 8px 10px;
      font-size: 14px;
      line-height: 1.5;
      resize: none;
      border: none;
      outline: none;
      background: #f2f3f5;
      border-radius: 18px;
      box-sizing: border-box;
      font-family: inherit;
    }

    .send-btn {
      flex-shrink: 0;
      display: flex;
      align-items: center;
      gap: 3px;
      height: 36px;
      padding: 0 14px;
      border: none;
      border-radius: 18px;
      color: #fff;
      font-size: 13px;
      background: linear-gradient(120deg, #14b8a6, #0891b2);

      &:disabled {
        opacity: 0.5;
      }
    }
  }
</style>
