import Vue from 'vue';
import Vuex from 'vuex';

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    ws: null, // WebSocket 实例
    newOrderMessage: '', // 新订单消息
  },
  mutations: {
    setWebSocket(state, ws) {
      state.ws = ws;
    },
    setNewOrderMessage(state, message) {
      state.newOrderMessage = message;
    },
  },
  actions: {
    initWebSocket({ commit }) {
      const wsUrl = `ws://localhost:9090/ws/order`;
      const ws = new WebSocket(wsUrl);

      ws.onopen = () => {
        console.log("WebSocket 已连接");
      };

      ws.onmessage = (event) => {
        const orderData = JSON.parse(event.data); // 解析 JSON 数据
        console.log("收到新订单消息:", orderData);

        // 获取当前商家 ID
        const userData = localStorage.getItem('xm-user');
        const userObject = JSON.parse(userData);
        const userId = userObject.id;

        console.log("当前商家ID:", userId);
        console.log(orderData.businessId);

        if (orderData.businessId === userId && userObject.role === "BUSINESS") {
          const message = "你有新的订单，请及时处理"; // 设置新订单消息
          commit('setNewOrderMessage', message);

          // 在这里可以广播消息
          if (this.$notify) {
            this.$notify({
              title: "新订单提醒",
              message: message,
              type: "success",
            });
          }

          // 播放音效
          if (this.playAudio) {
            this.playAudio();
          }

          // 刷新订单列表
          if (this.load) {
            this.load(1);
          }
        }
      };

      ws.onerror = (error) => {
        console.error("WebSocket 错误:", error);
      };

      ws.onclose = () => {
        console.log("WebSocket 已关闭");
      };

      commit('setWebSocket', ws); // 将 WebSocket 实例存储到 Vuex 中
    },
        playAudio() {
      const audio = new Audio("https://oss-jlx.oss-cn-beijing.aliyuncs.com/%E6%96%B0%E7%9A%84%E8%AE%A2%E5%8D%95%E6%9F%A5%E6%94%B6_%E8%80%B3%E8%81%86%E7%BD%91_%5B%E5%A3%B0%E9%9F%B3ID%EF%BC%9A35825%5D.mp3"); // 替换为实际音频文件地址
      audio.play().catch((error) => console.error("音频播放错误:", error));
    },
  },
});
