<template>
  <div class="login-container">
    <h2>注册</h2>
    <form @submit.prevent="register">
      <div class="form-group">
        <input type="text" id="username" v-model="form.username" placeholder="用户名" required>
      </div>
      <div class="form-group">
        <input type="password" id="password" v-model="form.password" placeholder="密码" required>
      </div>
      <div class="form-group">
        <input type="email" id="email" v-model="form.email" placeholder="邮箱" required>
      </div>
      <div class="button-group">
        <button type="button" @click="goToLogin">登录</button>
        <button type="submit">注册</button>
      </div>
      <p v-if="success" class="success">{{ success }}</p>
      <p v-if="error" class="error">{{ error }}</p>
    </form>
  </div>
</template>

<script>
export default {
  name: 'RegisterView',
  data() {
    return {
      form: { username: '', password: '', email: '' },
      success: '',
      error: ''
    }
  },
  methods: {
    async register() {
      try {
        console.log('Register form data:', this.form);
        const response = await fetch('/api/auth/register', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(this.form)
        })
        
        console.log('Register response status:', response.status);
        if (response.ok) {
          // 显示注册成功提示
          this.success = '注册成功！'
          this.error = ''
          // 2秒后跳转到登录页面
          setTimeout(() => {
            this.$router.push('/login')
          }, 2000)
        } else {
          // 显示注册失败提示
          const errorText = await response.text()
          console.log('Register error:', errorText);
          this.error = '注册失败：' + errorText
          this.success = ''
        }
      } catch (error) {
        // 显示网络错误提示
        this.error = '注册失败：网络错误'
        this.success = ''
        console.error('Register exception:', error)
      }
    },
    goToLogin() {
      this.$router.push('/login')
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