<!-- <template>
	<view style="padding: 20rpx;">
		<view class="box">
			<uni-forms :modelValue="form" :rules="rules" ref="formRef" label-width="160rpx" label-align="right">
			  <uni-forms-item label="地址信息" name="address" required>
			    <uni-easyinput type="textarea" v-model="form.address" placeholder="请输入地址" />
			  </uni-forms-item>
			  <uni-forms-item label="联系人" name="user" required>
			    <uni-easyinput type="text" v-model="form.user" placeholder="请输入联系人" />
			  </uni-forms-item>
			  <uni-forms-item label="联系方式" name="phone" required>
			    <uni-easyinput type="text" v-model="form.phone" placeholder="请输入联系方式" />
			  </uni-forms-item>
			
			  <uni-forms-item>
			    <button type="primary" size="mini" @click="saveAddress">保 存</button>
			  </uni-forms-item>
			</uni-forms>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				user: uni.getStorageSync('xm-user'),
				form: {},
				rules: {
				  address: {
				    rules: [{
				      required: true,
				      errorMessage: '请填写地址',
				    }]
				  },
				  user: {
				    rules: [{
				      required: true,
				      errorMessage: '请填写联系人',
				    }]
				  },
				  phone: {
				    rules: [{
				      required: true,
				      errorMessage: '请填写联系方式',
				    }]
				  }
				}
			}
		},
		onLoad(option) {
			let addressId = option.addressId || 0
			
			if (addressId > 0) {
				this.$request.get('/address/selectById/' + addressId).then(res => {
					this.form = res.data || {}
				})
			}
		},
		methods: {
			saveAddress() {
				// 验证
				this.$refs.formRef.validate().then(res => {
				  this.$request.request({
					  method: this.form.id ? 'PUT' : 'POST',
					  url: this.form.id ? '/address/update' : '/address/add',
					  data: this.form
				  }).then(res => {
				    if (res.code === '200') {
				      uni.showToast({
				        title: '操作成功'
				      })
					  uni.navigateBack()
				    } else {
				      uni.showToast({
				        icon: 'error',
				        title: res.msg
				      })
				    }
				  })
				}).catch(err => {
				  console.log('err', err);
				})  
			}
		}
	}
</script>

<style>

</style -->>
<template>
	<view style="padding: 20rpx;">
		<view class="box">
			<uni-forms :modelValue="form" :rules="rules" ref="formRef" label-width="160rpx" label-align="right">
			  <uni-forms-item label="地址信息" name="address" required>
			    <uni-easyinput type="textarea" v-model="form.address" placeholder="请输入地址" />
			    <button type="default" size="mini" @click="getCurrentLocation">定位</button>
			  </uni-forms-item>
			  <uni-forms-item label="联系人" name="user" required>
			    <uni-easyinput type="text" v-model="form.user" placeholder="请输入联系人" />
			  </uni-forms-item>
			  <uni-forms-item label="联系方式" name="phone" required>
			    <uni-easyinput type="text" v-model="form.phone" placeholder="请输入联系方式" />
			  </uni-forms-item>
			  <uni-forms-item>
			    <button type="primary" size="mini" @click="saveAddress">保 存</button>
			  </uni-forms-item>
			</uni-forms>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				user: uni.getStorageSync('xm-user'),
				form: {},
				rules: {
				  address: {
				    rules: [{ required: true, errorMessage: '请填写地址' }]
				  },
				  user: {
				    rules: [{ required: true, errorMessage: '请填写联系人' }]
				  },
				  phone: {
				    rules: [{ required: true, errorMessage: '请填写联系方式' }]
				  }
				}
			}
		},
		onLoad(option) {
			let addressId = option.addressId || 0;
			if (addressId > 0) {
				this.$request.get('/address/selectById/' + addressId).then(res => {
					this.form = res.data || {}
				});
			}
			  this.form.address = "address";
		},
		methods: {
	getCurrentLocation() {
	  const ak = 'VBrEBMYmKx0XNijqhBaOOIzCcjHvR1Ag'; // 替换为你的百度地图AK
	
	  // 检查并请求用户授权
	  uni.authorize({
	    scope: 'scope.userLocation',
	    success: () => {
	      // 获取用户地理位置
	      uni.getLocation({
	        type: 'gcj02', // 获取高精度位置
	        success: (res) => {
	          const { latitude, longitude } = res; // 解构获取经纬度
	          console.log('获取的地理位置:', { latitude, longitude });
	
	          // 拼接百度地图API的请求URL
	          const url = `https://api.map.baidu.com/reverse_geocoding/v3/?ak=${ak}&output=json&coordtype=wgs84ll&location=${latitude},${longitude}`;
	          console.log('请求百度地图API的URL:', url);
	
	          // 调用百度地图API获取地址信息
	          uni.request({
	            url: url,
	            success: (response) => {
	              console.log('百度地图API响应数据:', response);
	
	              // 检查百度地图API响应是否成功
	              if (response.statusCode === 200 && response.data.status === 0) {
	                const address = response.data.result.formatted_address;
	                console.log('解析出的地址:', address);
	
	                // 更新到表单的地址字段
	                this.form.address = address;
					console.log('更新后的地址:', this.form.address);
this.$forceUpdate(); // 强制刷新视图
	                uni.showToast({ title: '定位成功', icon: 'success' });
	              } else {
	                console.error('百度地图API响应错误:', response.data);
	                uni.showToast({ title: '获取位置失败', icon: 'error' });
	              }
	            },
	            fail: (err) => {
	              console.error('调用百度地图API失败:', err);
	              uni.showToast({ title: '定位失败，请稍后再试', icon: 'error' });
	            }
	          });
	        },
	        fail: (err) => {
	          console.error('获取地理位置失败：', err);
	          uni.showToast({ title: '获取地理位置失败', icon: 'error' });
	        },
	      });
	    },
	    fail: () => {
	      // 用户未授权处理
	      uni.showModal({
	        title: '授权失败',
	        content: '需要获取您的位置，请前往设置打开权限',
	        success: (modalRes) => {
	          if (modalRes.confirm) {
	            uni.openSetting(); // 引导用户到设置界面
	          }
	        }
	      });
	    }
	  });
	},
			saveAddress() {
				this.$refs.formRef.validate().then(res => {
					this.$request.request({
						method: this.form.id ? 'PUT' : 'POST',
						url: this.form.id ? '/address/update' : '/address/add',
						data: this.form
					}).then(res => {
						if (res.code === '200') {
							uni.showToast({ title: '操作成功' });
							uni.navigateBack();
						} else {
							uni.showToast({ icon: 'error', title: res.msg });
						}
					});
				}).catch(err => {
					console.log('err', err);
				});
			}
		}
	}
</script>

<style>
</style>
