<template>
    <view style="padding: 40rpx;">
        <view style="padding: 20rpx; margin: 80rpx 0; background-color: #fff; box-shadow: 0 2rpx 10rpx rgba(0,0,0,.1); border-radius: 10rpx;">
            <view style="margin: 50rpx 30rpx; font-size: 40rpx;">欢迎登录</view>
            <uni-forms ref="form" :modelValue="form" :rules="rules" validateTrigger='blur'>
                <uni-forms-item name="username" required>
                    <uni-easyinput prefixIcon="person" v-model="form.username" placeholder="请输入账号" />
                </uni-forms-item>
                <uni-forms-item name="password" required>
                    <uni-easyinput type="password" prefixIcon="locked" v-model="form.password" placeholder="请输入密码" />
                </uni-forms-item>
                <uni-forms-item>
                    <button @click="login()" style="background-color: #ffd100; border-color: #ffd100; height: 70rpx; line-height: 70rpx;">登 录</button>
                </uni-forms-item>
                <uni-forms-item>
                    <view style="text-align: right;">还没有账号？去 <navigator style="display: inline-block; color: dodgerblue; 
                        margin-left: 4rpx;" url="/pages/register/register">注册</navigator></view>	
                </uni-forms-item>
            </uni-forms>
            <view style="margin-top: 30rpx; text-align: center;">
                <button @click="handleLogin" style="background-color: #07c160; color: #fff; border: none; height: 70rpx; line-height: 70rpx;">微信一键登录</button>
			</view>
        </view>
    </view>
</template>

<script>
import OSS from 'ali-oss'; // 引入阿里云OSS SDK
export default {
    data() {
        return {
            form: {
                username: '',
                password: '',
                role: 'USER'
            },
            rules: {
                username: {
                    rules: [
                        { required: true, errorMessage: '请输入账号' },
                        { minLength: 3, maxLength: 10, errorMessage: '账号长度在 {minLength} 到 {maxLength} 个字符' }
                    ],
                },
                password: {
                    rules: [
                        { required: true, errorMessage: '请输入密码' },
                        { minLength: 3, maxLength: 10, errorMessage: '密码长度在 {minLength} 到 {maxLength} 个字符' }
                    ],
                }
            },
            loginCode: ''
        };
    },
    methods: {
bindblur(event) {
				this.nameUser = event.target.value;

},
    onChooseAvatar(res) {
        const { detail } = res;
        this.imagUrl = detail.avatarUrl; // 临时头像路径
        this.saveFileLocally(this.imagUrl); // 保存头像
    },

saveFileLocally(filePath) {
    // 检查输入的文件路径是否有效
    if (!filePath) {
        uni.showToast({
            title: '文件路径无效',
            icon: 'error'
        });
        return;
    }

    // 下载微信头像临时 URL
    wx.downloadFile({
        url: filePath, // 微信头像的临时 URL
        success: (res) => {
            const tempFilePath = res.tempFilePath; // 临时文件路径
            if (!tempFilePath) {
                uni.showToast({
                    title: '文件下载失败',
                    icon: 'error'
                });
                return;
            }

            // 使用文件系统管理器将文件保存到本地
            const fs = wx.getFileSystemManager();
            const localFilePath = `${wx.env.USER_DATA_PATH}/${Date.now()}.jpg`; // 本地文件路径

            // 保存文件到本地
            fs.saveFile({
                tempFilePath: tempFilePath, // 临时文件路径
                filePath: localFilePath, // 本地永久文件路径
                success: (saveRes) => {
                    console.log('文件保存成功：', saveRes.savedFilePath);

                    uni.showToast({
                        title: '文件保存成功',
                        duration: 2000
                    });

                    // 提供后续操作的灵活性，例如：读取文件或保存到相册
                    this.handleSavedFile(saveRes.savedFilePath);
                },
                fail: (saveErr) => {
                    console.error('保存文件失败：', saveErr);

                    uni.showToast({
                        title: '保存失败，请重试',
                        icon: 'error'
                    });
                }
            });
        },
        fail: (err) => {
            console.error('文件下载失败：', err);

            uni.showToast({
                title: '下载失败，请重试',
                icon: 'error'
            });
        }
    });
},

// 处理保存后的文件（例如显示或存入相册）
handleSavedFile(filePath) {
    // 可选：将文件保存到相册
    wx.saveImageToPhotosAlbum({
        filePath: filePath,
        success: () => {
            uni.showToast({
                title: '已保存到相册',
                duration: 2000
            });
        },
        fail: (err) => {
            console.error('保存到相册失败：', err);

            uni.showToast({
                title: '保存到相册失败',
                icon: 'error'
            });
        }
    });
},


        // 普通登录方法
        login() {
            console.log('页面加载成功！'); // 确保这里能输出日志
            this.$refs.form.validate().then(res => {
                this.$request.post('/login', this.form).then(res => {
                    if (res.code === '200') {
                        uni.showToast({
                            icon: 'success',
                            title: '登录成功'
                        });
                        uni.setStorageSync('xm-user', res.data);

                        // 跳转主页
                        uni.switchTab({
                            url: '/pages/index/index'
                        });
                    } else {
                        uni.showToast({
                            icon: 'error',
                            title: res.msg
                        });
                    }
                });
            }).catch(err => {
                console.log('表单错误信息：', err);
            });
        },

        // 微信登录方法
        handleLogin() {
			
            wx.login({
                success: (loginRes) => {
                    if (loginRes.code) {
                        console.log('微信登录成功，code:', loginRes.code);
                        this.loginCode = loginRes.code;
                        this.handleGetUserProfile(); // 弹窗授权
                    } else {
                        console.error('微信登录失败：', loginRes.errMsg);
                    }
                },
                fail: (err) => {
                    console.error('微信登录接口调用失败：', err);
                }
            });
        },

        // 弹窗授权
        handleGetUserProfile() {
            uni.showModal({
                title: '用户授权',
                content: '我们需要获取您的昵称和头像信息来完善您的个人资料。',
                success: (res) => {
                    if (res.confirm) {
                        // 用户点击授权
                        this.getUserProfile();
                    } else {
                        // 用户点击取消
                        console.log('用户取消授权');
                    }
                }
            });
        },
getUserProfile() {
    wx.getUserProfile({
        desc: '用于完善用户信息', // 提示信息，必须提供
        success: (profileRes) => {
            const userInfo = profileRes.userInfo;
            const avatarUrl = userInfo.avatarUrl; // 用户头像 URL（临时）
       const nickName = userInfo.nickName;  // 用户昵称
            // 下载头像到本地
            wx.downloadFile({
                url: avatarUrl,
                success: (downloadRes) => {
                    if (downloadRes.statusCode === 200) {
                        // 本地临时文件路径
                        const tempFilePath = downloadRes.tempFilePath;

                        // 上传头像文件到后端
                        wx.uploadFile({
                            url: 'http://localhost:9090/upload', // 替换为后端上传接口地址
                            filePath: tempFilePath,
                            name: 'file', // 表单文件字段名
                            success: (uploadRes) => {
                                const data = uploadRes.data;
                                console.log('头像上传成功:', data);

                                // 将头像 URL 存入后端用户信息
                                this.$request.post('/wx-login', {
                                    code: this.loginCode, // 从 handleLogin 获取的 code
                                    avatarUrl: data.url ,// 返回的头像 URL
									      nickName: nickName    // 用户昵称
                                }).then(response => {
                                    if (response.code === '200') {
                                        uni.showToast({
                                            icon: 'success',
                                            title: '微信登录成功'
                                        });
                                        uni.setStorageSync('xm-user', response.data);
                                        // 跳转主页
                                        uni.switchTab({
                                            url: '/pages/index/index'
                                        });
                                    } else {
                                        uni.showToast({
                                            icon: 'error',
                                            title: response.msg || '登录失败，请重试'
                                        });
                                    }
                                }).catch(error => {
                                    uni.showToast({
                                        icon: 'error',
                                        title: error.message || '登录失败，请重试'
                                    });
                                    console.error('微信登录失败:', error);
                                });
                            },
                            fail: (err) => {
                                console.error('头像上传失败:', err);
                                uni.showToast({
                                    icon: 'error',
                                    title: '头像上传失败'
                                });
                            }
                        });
                    }
                },
                fail: (err) => {
                    console.error('头像下载失败:', err);
                    uni.showToast({
                        icon: 'error',
                        title: '头像下载失败'
                    });
                }
            });
        },
        fail: (err) => {
            console.error('获取用户信息失败：', err);
            uni.showToast({
                icon: 'error',
                title: '获取用户信息失败'
            });
        }
    });
}


    }
};
</script>

<style>
</style>
