<template>
  <div class="login-container">
    <h2>登录</h2>
    <form @submit.prevent="login">
      <div class="form-group">
        <input type="text" id="username" v-model="form.username" placeholder="用户名" required>
      </div>
      <div class="form-group">
        <input type="password" id="password" v-model="form.password" placeholder="密码" required>
      </div>
      <div class="button-group">
        <button type="submit">登录</button>
        <button type="button" @click="goToRegister">注册</button>
      </div>
      <p v-if="success" class="success">{{ success }}</p>
      <p v-if="error" class="error">{{ error }}</p>
    </form>
  </div>
</template>

<script>
export default {
  name: 'LoginView',
  data() {
    return {
      form: {
        username: '',
        password: ''
      },
      success: '',
      error: ''
    }
  },
  methods: {
    async login() {
      try {
        console.log('Login form data:', this.form);
        const response = await fetch('/api/auth/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            username: this.form.username,
            password: this.form.password
          })
        })
        
        console.log('Login response status:', response.status);
        console.log('Login response headers:', response.headers);
        
        if (response.ok) {
          // 登录成功
          this.success = '登录成功！'
          this.error = ''
          // 获取用户信息并跳转到对应页面
          setTimeout(() => {
            this.fetchUserProfile()
          }, 1000)
        } else {
          // 登录失败
          const errorText = await response.text();
          console.log('Login error:', errorText);
          this.error = '登录失败：' + errorText
          this.success = ''
          // 3秒后清除错误提示
          setTimeout(() => {
            this.error = ''
          }, 3000)
        }
      } catch (error) {
        // 网络错误
        this.error = '登录失败：网络错误'
        this.success = ''
        // 3秒后清除错误提示
        setTimeout(() => {
          this.error = ''
        }, 3000)
        console.error('Login exception:', error)
      }
    },
    goToRegister() {
      this.$router.push('/register')
    },
    async fetchUserProfile() {
      try {
        const response = await fetch('/api/user/profile', {
          method: 'POST'
        })
        if (response.ok) {
          const user = await response.json()
          console.log('User profile:', user)
          // 根据角色跳转到不同页面
          if (user.role === 'ROLE_ADMIN') {
            this.$router.push('/admin')
          } else {
            this.$router.push('/home')
          }
        } else {
          // 如果获取用户信息失败，默认跳转到首页
          this.$router.push('/home')
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
        // 如果发生错误，默认跳转到首页
        this.$router.push('/home')
      }
    }
  }
}
</script>

<style scoped>
.login-container {
  max-width: 400px;
  margin: 100px auto;
  padding: 40px;
  background-color: #f9f9f9;
  border-radius: 10px;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.login-container h2 {
  margin-bottom: 30px;
  color: #333;
}

.form-group {
  margin-bottom: 20px;
  text-align: left;
}

input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 16px;
  box-sizing: border-box;
}

.button-group {
  display: flex;
  justify-content: space-between;
  margin-top: 30px;
}

button {
  flex: 1;
  padding: 12px;
  background-color: #333;
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

button:hover {
  background-color: #555;
}

button:first-child {
  margin-right: 10px;
}

button:last-child {
  margin-left: 10px;
}

.success {
  color: green;
  text-align: center;
  margin-top: 15px;
  padding: 10px;
  background-color: #d4edda;
  border-radius: 5px;
}

.error {
  color: red;
  text-align: center;
  margin-top: 15px;
  padding: 10px;
  background-color: #f8d7da;
  border-radius: 5px;
}
</style>