<template>
	<view>
		<uni-segmented-control :current="current" :values="items" @clickItem="onClickItem" styleType="text"
			activeColor="#ff9900"></uni-segmented-control>
		<view style="padding: 20rpx;">
			<view class="box" v-for="item in ordersList" :key="item.id" style="margin-bottom: 10rpx;">
				<view style="display: flex; align-items: baseline; margin-bottom: 10rpx;">
					<navigator :url="'/pages/detail/detail?businessId=' + item.businessId"
						style="flex: 1; font-size: 32rpx; ">{{ item.businessName }}
						<uni-icons type="right" size="16" color="#666"
							style="position: relative; top: 2rpx;"></uni-icons>
					</navigator>
					<view style="font-size: 24rpx; color: #666;">{{ item.status }}</view>
				</view>
				<view style="display: flex; align-items: center; grid-gap: 20rpx; margin-bottom: 10rpx;"  @click="goOrdersItem(item.id)">
					<view>
						<image style="display: block; width: 160rpx; height: 160rpx; border-radius: 10rpx;"
							:src="item.cover"></image>
					</view>
					<view style="flex: 1;">{{ item.name }}</view>
					<view>实付￥<text style="font-size: 36rpx; font-weight: bold; color: red;">{{ item.actual }}</text>
					</view>
				</view>
				<view style="display: flex; min-height: 40rpx;">
					<view style="flex: 1;">
						<view v-if="item.status === '已取消' || item.status === '已完成' || item.status === '已退款'"
							@click="del(item.id)">
							<uni-icons type="trash" size="16" color="#666"
								style="position: relative; top: 2rpx;"></uni-icons>
							<text style="font-size: 24rpx; color: #666;">删除</text>
						</view>
					</view>
					<view style="flex: 1; text-align: right;">
						<uni-tag v-if="item.status === '待支付'" text="支付" size="mini" type="primary"
							@click="changeStatus(item, '待发货')"></uni-tag>
						<uni-tag v-if="item.status === '待发货'" text="申请退款" size="mini" type="error"
							@click="changeStatus(item, '已退款')"></uni-tag>
						<uni-tag v-if="item.status === '待收货'" text="确认收货" size="mini" type="warning"
							@click="changeStatus(item, '待评价')"></uni-tag>
						<uni-tag v-if="item.status === '待评价'" text="评价" size="mini" type="royal" @click="goComment(item.id)"></uni-tag>
					</view>
				</view>
			</view>
			    <view v-if="imagePopupVisible" class="image-popup">
			      <image :src="imageUrl" mode="widthFix" class="popup-image"></image>
			      <button class="close-btn" @click="closeImagePopup">关闭</button>
			    </view>

		</view>
	</view>
</template>

<script>
export default {
  data() {
    return {
      current: 0,
      items: ['全部', '进行中', '待评价', '已退款'],
      ordersList: [],
      user: uni.getStorageSync('xm-user'),
      businessQrCodeUrl: '',  // 存储二维码图片URL
	     imagePopupVisible:false,  // 控制弹窗显示与隐藏
	        imageUrl: '',  // 用于存储图片URL
    }
  },
  onShow() {
    this.loadOrders();  // 页面展示时加载订单
  },
  methods: {
    goComment(orderId) {
      uni.navigateTo({
        url: '/pages/comment/comment?orderId=' + orderId
      });
    },
    goOrdersItem(orderId) {
      uni.navigateTo({
        url: '/pages/ordersItem/ordersItem?orderId=' + orderId
      });
    },
    del(orderId) {
      this.$request.del('/orders/delete/' + orderId).then(res => {
        if (res.code === '200') {
          uni.showToast({
            icon: "success",
            title: '操作成功'
          });
          this.loadOrders();
        } else {
          uni.showToast({
            icon: "error",
            title: res.msg
          });
        }
      });
    },
    changeStatus(orders, status) {
      let form = JSON.parse(JSON.stringify(orders));
      form.status = status;
      this.$request.put('/orders/update', form).then(res => {
        if (res.code === '200') {
          uni.showToast({
            icon: "success",
            title: '操作成功'
          });
          this.loadOrders();
          this.loadBusinessInfo(orders.businessId);  // 更新商家信息
        } else {
          uni.showToast({
            icon: "error",
            title: res.msg
          });
        }
      });
    },
    loadOrders() {
      let status = '全部';
      switch (this.current) {
        case 0:
          status = '全部';
          break;
        case 1:
          status = '进行中';
          break;
        case 2:
          status = '待评价';
          break;
        case 3:
          status = '已退款';
          break;
      }
      this.$request.get('/orders/selectAll', {
        userId: this.user.id,
        status: status
      }).then(res => {
        this.ordersList = res.data || [];
      });
    },
    onClickItem(e) {
      if (this.current != e.currentIndex) {
        this.current = e.currentIndex;
        this.loadOrders();
      }
    },
    // 获取商家信息，包括二维码URL
    loadBusinessInfo(businessId) {
		console.log(businessId);
      this.$request.get(`/business/selectById/${businessId}`).then(res => {
        if (res.code === '200') {
          this.businessQrCodeUrl = res.data.paycode;  // 假设返回的二维码图片 URL 是在 res.data.avatar
 console.log(this.businessQrCodeUrl);
          // 弹出二维码图片
          this.showQrCodePopup();
        } else {
          uni.showToast({
            icon: 'error',
            title: '获取商家二维码失败',
          });
        }
      });
    },

    // 弹出二维码图片的自定义弹窗         orders
showQrCodePopup() {
      this.imageUrl = this.businessQrCodeUrl;  // 设置图片URL
	  console.log("s");
      this.imagePopupVisible = true;  // 显示弹窗
    },
    // 关闭图片弹窗
    closeImagePopup() {
      this.imagePopupVisible = false;  // 隐藏弹窗
    }
  }
}

</script>
<style scoped>
.image-popup {
  position: fixed;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);  /* 半透明背景 */
  justify-content: center;
  align-items: center;
  display: flex;
  z-index: 9999;
}

.popup-image {
  max-width: 90%;
  max-height: 80%;
}

.close-btn {
  position: absolute;
  bottom: 30rpx;
  left: 50%;
  transform: translateX(-50%);
  padding: 10rpx;
  background-color: #fff;
  border-radius: 5rpx;
  border: none;
}
</style>
