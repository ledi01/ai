<template>
  <div class="home-container">
    <div v-if="user" class="user-info">
      <div class="welcome-section">
        <h2>欢迎，{{ user.username }}！</h2>
      </div>
      
      <div v-if="user.role === 'ROLE_USER'" class="announcement-section">
        <h3>公告栏</h3>
        <div v-if="announcements.length > 0" class="announcements">
          <div v-for="announcement in announcements" :key="announcement.id" class="announcement-item">
            <h4>{{ announcement.title }}</h4>
            <p>{{ announcement.content }}</p>
            <span class="announcement-time">发布时间：{{ formatDate(announcement.createdAt) }}</span>
          </div>
        </div>
        <div v-else class="no-announcement">
          <p>暂无公告</p>
        </div>
      </div>
      
      <div class="user-details">
        <h3>用户信息</h3>
        <p>用户名: {{ user.username }}</p>
        <p>邮箱: {{ user.email }}</p>
        <p>角色: {{ user.role === 'ROLE_ADMIN' ? '管理员' : '普通用户' }}</p>
      </div>
      
      <div v-if="user.role === 'ROLE_USER'" class="member-info">
        <h3>会员状态</h3>
        <p v-if="member">
          开始日期: {{ formatDate(member.startDate) }}<br>
          结束日期: {{ formatDate(member.endDate) }}<br>
          剩余天数: <span :class="{ 'active': member.active, 'expired': !member.active }">{{ getDaysRemaining(member.endDate) }} 天</span><br>
          状态: <span :class="{ 'active': member.active, 'expired': !member.active }">{{ member.active ? '活跃' : '已过期' }}</span>
        </p>
        <p v-else>无会员信息</p>
      </div>
      
      <div class="logout-section">
        <button @click="logout">退出登录</button>
      </div>
    </div>
    <div v-else class="login-prompt">
      <p>请登录查看您的个人资料</p>
      <router-link to="/login">登录</router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HomeView',
  data() {
    return {
      user: null,
      member: null,
      announcements: []
    }
  },
  mounted() {
    this.fetchUserProfile()
  },
  methods: {
    async fetchUserProfile() {
      try {
        const response = await fetch('/api/user/profile', {
          method: 'POST'
        })
        if (response.ok) {
          const contentType = response.headers.get('content-type')
          if (contentType && contentType.includes('application/json')) {
            this.user = await response.json()
            this.fetchMemberStatus()
            this.fetchAnnouncements()
          } else {
            this.user = null
          }
        } else {
          this.user = null
        }
      } catch (error) {
        this.user = null
        console.error('获取用户信息失败:', error)
      }
    },
    async fetchMemberStatus() {
      try {
        const response = await fetch('/api/user/member-status', {
          method: 'POST'
        })
        if (response.ok) {
          this.member = await response.json()
        }
      } catch (error) {
        console.error('获取会员状态失败:', error)
      }
    },
    async fetchAnnouncements() {
      try {
        const response = await fetch('/api/announcements/active')
        if (response.ok) {
          this.announcements = await response.json()
        }
      } catch (error) {
        console.error('获取公告失败:', error)
      }
    },
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    getDaysRemaining(endDate) {
      if (!endDate) return 0
      const end = new Date(endDate)
      const now = new Date()
      const diff = end - now
      const days = Math.ceil(diff / (1000 * 60 * 60 * 24))
      return days > 0 ? days : 0
    },
    async logout() {
      try {
        await fetch('/api/auth/logout', {
          method: 'POST'
        })
        this.$router.push('/login')
      } catch (error) {
        console.error('退出登录失败:', error)
      }
    }
  }
}
</script>

<style scoped>
.home-container {
  max-width: 800px;
  margin: 50px auto;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 5px;
}

.user-info {
  margin-top: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 5px;
}

.welcome-section {
  text-align: center;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #e8f5e9;
  border-radius: 5px;
}

.welcome-section h2 {
  margin: 0;
  color: #2e7d32;
}

.announcement-section {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #e3f2fd;
  border: 1px solid #2196f3;
  border-radius: 5px;
}

.announcement-section h3 {
  margin-top: 0;
  color: #1976d2;
  border-bottom: 1px solid #90caf9;
  padding-bottom: 10px;
}

.announcements {
  margin-top: 15px;
}

.announcement-item {
  padding: 15px;
  margin-bottom: 15px;
  background-color: #fff;
  border-radius: 5px;
  border-left: 4px solid #2196f3;
}

.announcement-item:last-child {
  margin-bottom: 0;
}

.announcement-item h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.announcement-item p {
  margin: 0 0 10px 0;
  color: #555;
  line-height: 1.6;
}

.announcement-time {
  font-size: 12px;
  color: #888;
}

.no-announcement {
  text-align: center;
  padding: 20px;
  color: #999;
}

.user-details {
  margin-top: 20px;
  padding: 15px;
  background-color: #fff;
  border-radius: 5px;
  border: 1px solid #ddd;
}

.user-details h3 {
  margin-top: 0;
  color: #333;
  border-bottom: 1px solid #ddd;
  padding-bottom: 10px;
}

.member-info {
  margin-top: 20px;
  padding: 15px;
  background-color: #fff3e0;
  border-radius: 5px;
  border: 1px solid #ff9800;
}

.member-info h3 {
  margin-top: 0;
  color: #e65100;
}

.active {
  color: green;
  font-weight: bold;
}

.expired {
  color: red;
  font-weight: bold;
}

.logout-section {
  margin-top: 20px;
  text-align: center;
}

.logout-section button {
  padding: 10px 30px;
  background-color: #dc3545;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
}

.logout-section button:hover {
  background-color: #c82333;
}

.login-prompt {
  margin-top: 20px;
  padding: 20px;
  text-align: center;
  background-color: #f9f9f9;
  border-radius: 5px;
}

.login-prompt a {
  display: inline-block;
  margin-top: 10px;
  padding: 10px 20px;
  background-color: #333;
  color: white;
  text-decoration: none;
  border-radius: 3px;
}

.login-prompt a:hover {
  background-color: #555;
}
</style>
