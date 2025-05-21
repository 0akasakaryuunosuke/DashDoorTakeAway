<template>
  <view class="chat-container">
    <scroll-view class="messages" scroll-y :scroll-top="scrollTop">
      <view
        v-for="(msg, i) in messages"
        :key="i"
        :class="msg.role === 'user' ? 'msg user' : 'msg bot'"
      >
        {{ msg.text }}
      </view>
    </scroll-view>

    <view class="input-bar">
      <input v-model="input" placeholder="请输入问题…" class="input"/>
      <button @click="send" :disabled="loading" class="btn-send">发送</button>
    </view>
  </view>
</template>

<script>
import apiConfig from '@/config.js'
export default {
  data() {
    return {
      messages: [],
      input: '',
      loading: false,
      scrollTop: 0
    }
  },
  computed: {
    apiBase() {
      // 如果 Uni-app 环境下拿不到 process.env.NODE_ENV，可改成判断 hostname
      const env = process.env.NODE_ENV === 'production' ? 'prod' : 'dev'
      return apiConfig[env].baseUrl
    }
  },
  methods: {
    send() {
      if (!this.input.trim()) return
      this.messages.push({ role: 'user', text: this.input })
      this.scrollToBottom()

      const question = encodeURIComponent(this.input)
      this.input = ''
      this.loading = true

      const url = `${this.apiBase}/ai/assistant-stream?question=${question}`
      const evt = new EventSource(url)
      let botText = ''

      evt.onmessage = e => {
        botText += e.data
        if (this.loading) {
          this.messages.push({ role: 'bot', text: botText })
          this.loading = false
        } else {
          this.$set(this.messages, this.messages.length - 1, {
            role: 'bot',
            text: botText
          })
        }
        this.scrollToBottom()
      }
      evt.onerror = () => {
        evt.close()
        this.loading = false
      }
    },
    scrollToBottom() {
      // 强制滚到底
      this.scrollTop += 9999
    }
  }
}
</script>

<style>
.chat-container { display: flex; flex-direction: column; height: 100%; padding: 20rpx; }
.messages { flex: 1; }
.msg { margin-bottom: 20rpx; padding: 20rpx; border-radius: 20rpx; max-width: 80%; }
.user { background: #ffd100; align-self: flex-end; }
.bot  { background: #f0f0f0; align-self: flex-start; }
.input-bar { display: flex; padding-top: 20rpx; }
.input { flex: 1; height: 60rpx; border: 1rpx solid #ccc; border-radius: 30rpx; padding: 0 30rpx; }
.btn-send { margin-left: 20rpx; width: 120rpx; height: 60rpx; border-radius: 30rpx; background: #ffd100; }
</style>
