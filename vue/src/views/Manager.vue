<template>
  <div class="manager-container">
    <!-- 头部 -->
    <div class="manager-header">
      <div class="manager-header-left">
<!--        <img src="@/assets/imgs/logo.png" />-->
        <div class="title">门冲外卖后台管理</div>
      </div>

      <div class="manager-header-center">
        <el-breadcrumb separator-class="el-icon-arrow-right">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: $route.path }">{{ $route.meta.name }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <div class="manager-header-right">
        <!-- 店铺状态 -->
 <div class="shop-status-container" v-if="user.role === 'BUSINESS'">
    <!-- 店铺状态切换 -->
    <div class="status-switch">
      <span>店铺状态:</span>
      <el-switch
        v-model="shopStatus"
        active-color="#13ce66"
        inactive-color="#ff4949"
        @change="updateShopStatus"
      ></el-switch>
    </div>
    <!-- 动态图片展示 -->
    <div class="shop-image">
      <img :src="shopImage" alt="店铺状态" />
    </div>
  </div>

        <el-dropdown placement="bottom">
          <div class="avatar">
            <img :src="user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
            <div>{{ user.name || '' }}</div>
          </div>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item @click.native="goToPerson">个人信息</el-dropdown-item>
            <el-dropdown-item @click.native="$router.push('/password')">修改密码</el-dropdown-item>
            <el-dropdown-item @click.native="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </div>

    <!-- 主体 -->
    <div class="manager-main">
      <!-- 侧边栏 -->
      <div class="manager-main-left">
        <el-menu :default-openeds="['info', 'user']" router style="border: none" :default-active="$route.path">
          <el-menu-item index="/home">
            <i class="el-icon-s-home"></i>
            <span slot="title">系统首页</span>
          </el-menu-item>
          <el-submenu index="info">
            <template slot="title">
              <i class="el-icon-menu"></i><span>信息管理</span>
            </template>
            <el-menu-item index="/category">商品分类信息</el-menu-item>
            <el-menu-item index="/goods">商品信息</el-menu-item>
            <el-menu-item index="/orders">订单管理信息</el-menu-item>
            <el-menu-item index="/collect">店铺收藏信息</el-menu-item>
            <el-menu-item index="/comment">店铺评论信息</el-menu-item>
            <el-menu-item index="/banner" v-if="user.role === 'ADMIN'">广告信息</el-menu-item>
            <el-menu-item index="/notice" v-if="user.role === 'ADMIN'">公告信息</el-menu-item>
          </el-submenu>

          <el-submenu index="user" v-if="user.role === 'ADMIN'">
            <template slot="title">
              <i class="el-icon-menu"></i><span>用户管理</span>
            </template>
            <el-menu-item index="/admin">管理员信息</el-menu-item>
            <el-menu-item index="/business">商家信息</el-menu-item>
            <el-menu-item index="/user">用户信息</el-menu-item>
          </el-submenu>
        </el-menu>
      </div>

      <!-- 数据表格 -->
      <div class="manager-main-right">
        <router-view @update:user="updateUser" />
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "Manager",
  data() {
    return {
      user: JSON.parse(localStorage.getItem("xm-user") || "{}"),
      shopStatus: null, // 店铺状态（0/1）
    };
  },
  created() {
    if (!this.user.id) {
      this.$router.push("/login");
    } else {
      this.getShopStatus(); // 获取店铺状态
      this.initWebSocket(); // 初始化 WebSocket
    }
  },
  computed: {
    // 动态计算图片路径
    shopImage() {
      return this.shopStatus
        ? require("D:\\VMware\\koala\\code\\takeaway\\vue\\src\\assets\\imgs\\shopOpen.png") // 店铺开启图片
        : require("D:\\VMware\\koala\\code\\takeaway\\vue\\src\\assets\\imgs\\shopClose.png"); // 店铺关闭图片
    },
  },
  methods: {
    initWebSocket() {
      const wsUrl = `ws://localhost:9090/ws/order`; // 替换为实际 WebSocket 地址
      this.ws = new WebSocket(wsUrl);

      this.ws.onopen = () => {
        console.log("WebSocket 已连接");
      };
      this.ws.onmessage = (event) => {
        const orderData = JSON.parse(event.data); // 解析 JSON 数据
        console.log("收到新订单消息:", orderData)
        // 当前商家 ID
        const userData = localStorage.getItem('xm-user');
        const userObject = JSON.parse(userData);
        const userId = userObject.id;

        console.log("当前商家ID:",userId)
        console.log(orderData.businessId)
        if (orderData.businessId === userId&&userObject.role==="BUSINESS"){
          // 假设消息是通知商家新订单支付完成
          this.newOrder = "你有新的订单，请及时处理"; // 将消息保存到组件数据中显示
          this.$notify({
            title: "新订单提醒",
            message: "你有新的订单，请及时处理",
            type: "success",
          });
          this.playAudio();
        }
      };

      this.ws.onerror = (error) => {
        console.error("WebSocket 错误:", error);
      };

      this.ws.onclose = () => {
        console.log("WebSocket 已关闭");
      };
    },

    playAudio() {
      const audio = new Audio("https://oss-jlx.oss-cn-beijing.aliyuncs.com/%E6%96%B0%E7%9A%84%E8%AE%A2%E5%8D%95%E6%9F%A5%E6%94%B6_%E8%80%B3%E8%81%86%E7%BD%91_%5B%E5%A3%B0%E9%9F%B3ID%EF%BC%9A35825%5D.mp3"); // 替换为实际音频文件地址
      audio.play().catch((error) => console.error("音频播放错误:", error));
    },
    async getShopStatus() {
      try {
        const username = this.user.username; // 从localStorage获取的用户名
        const response = await this.$request.post("/shop/getStatus", { username }); // 传递username到后端
        this.shopStatus = response.data; // 获取店铺状态
      } catch (error) {
        console.error("获取店铺状态失败:", error);
      }
    },
    async updateShopStatus(newStatus) {
      try {
        const username = this.user.username; // 从localStorage获取的用户名
        const status = newStatus ? 1 : 0; // 将布尔值转换为整数
        const response = await this.$request.put("/shop/setStatus", { username, status }); // 传递username和status到后端
        if (response.success) {
          this.shopStatus = status; // 更新状态
        }
      } catch (error) {
        console.error("更新店铺状态失败:", error);
      }
    },
    updateUser() {
      this.user = JSON.parse(localStorage.getItem("xm-user") || "{}"); // 更新用户信息
    },
    goToPerson() {
      if (this.user.role === "ADMIN") {
        this.$router.push("/adminPerson");
      } else if (this.user.role === "BUSINESS") {
        this.$router.push("/businessPerson");
      }
    },
    logout() {
      localStorage.removeItem("xm-user");
      this.$router.push("/login");
    },
  },
};
</script>

<style scoped>
@import "@/assets/css/manager.css";

/* 横向布局样式 */
.shop-status-container {
  display: flex;
  align-items: center; /* 垂直居中 */
}

.status-switch {
  display: flex; /* 使用 Flexbox 实现水平排列 */
  align-items: center; /* 垂直居中 */
  gap: 10px; /* 设置间隔 */
}

.shop-image img {
  width: 40px; /* 调整图片大小 */
  height: 40px;
}

.shop-image img {
  width: 24px;
  height: 24px;
  margin-left: 10px;
  vertical-align: middle;
}
</style>
