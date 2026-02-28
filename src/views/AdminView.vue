<template>
  <div class="admin-container">
    <div class="header">
      <h2>管理员面板</h2>
      <button class="logout-btn" @click="logout">退出登录</button>
    </div>
    
    <div class="tab-container">
      <button 
        :class="['tab-btn', { 'active': activeTab === 'users' }]" 
        @click="activeTab = 'users'">
        用户管理
      </button>
      <button 
        :class="['tab-btn', { 'active': activeTab === 'announcements' }]" 
        @click="activeTab = 'announcements'">
        公告管理
      </button>
    </div>

    <div v-if="activeTab === 'users'" class="users-section">
      <h3>用户管理</h3>
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>邮箱</th>
            <th>角色</th>
            <th>注册时间</th>
            <th>会员到期时间</th>
            <th>剩余天数</th>
            <th>会员状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id">
            <td>{{ user.id }}</td>
            <td>{{ user.username }}</td>
            <td>{{ user.email }}</td>
            <td>{{ user.role === 'ROLE_ADMIN' ? '管理员' : '普通用户' }}</td>
            <td>{{ formatDate(user.startDate) }}</td>
            <td>{{ formatDate(user.endDate) }}</td>
            <td v-if="user.role === 'ROLE_USER'">
              <span :class="{ 'expired': user.daysRemaining <= 0, 'active': user.daysRemaining > 0 }">
                {{ user.daysRemaining }} 天
              </span>
            </td>
            <td v-else>-</td>
            <td v-if="user.role === 'ROLE_USER'">
              <span :class="{ 'expired': !user.isActive, 'active': user.isActive }">
                {{ user.isActive ? '活跃' : '已过期' }}
              </span>
            </td>
            <td v-else>不适用</td>
            <td v-if="user.role === 'ROLE_USER'">
              <button @click="showExtendForm(user)">延长会员期限</button>
              <button @click="showEditForm(user)">修改</button>
            </td>
            <td v-else>-</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="activeTab === 'announcements'" class="announcement-section">
      <h3>公告管理</h3>
      
      <div class="create-announcement">
        <h4>发布新公告</h4>
        <form @submit.prevent="createAnnouncement">
          <div class="form-group">
            <label for="announcementTitle">公告标题</label>
            <input type="text" id="announcementTitle" v-model="newAnnouncement.title" required>
          </div>
          <div class="form-group">
            <label for="announcementContent">公告内容</label>
            <textarea id="announcementContent" v-model="newAnnouncement.content" rows="4" required></textarea>
          </div>
          <button type="submit">发布公告</button>
          <p v-if="announcementError" class="error">{{ announcementError }}</p>
          <p v-if="announcementSuccess" class="success">{{ announcementSuccess }}</p>
        </form>
      </div>

      <div class="announcement-list">
        <h4>公告列表</h4>
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>标题</th>
              <th>内容</th>
              <th>发布时间</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="announcement in announcements" :key="announcement.id">
              <td>{{ announcement.id }}</td>
              <td>{{ announcement.title }}</td>
              <td class="content-cell">{{ announcement.content }}</td>
              <td>{{ formatDate(announcement.createdAt) }}</td>
              <td>
                <span :class="{ 'active': announcement.active, 'expired': !announcement.active }">
                  {{ announcement.active ? '启用' : '停用' }}
                </span>
              </td>
              <td>
                <button v-if="announcement.active" @click="deactivateAnnouncement(announcement.id)">停用</button>
                <button v-else @click="activateAnnouncement(announcement.id)">启用</button>
                <button class="delete-btn" @click="deleteAnnouncement(announcement.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
        <p v-if="announcements.length === 0" class="no-data">暂无公告</p>
      </div>
    </div>

    <div v-if="selectedUser && formType === 'extend'" class="extend-form">
      <h3>为 {{ selectedUser.username }} 延长会员期限</h3>
      <form @submit.prevent="extendMembership">
        <div class="form-group">
          <label for="days">延长天数</label>
          <input type="number" id="days" v-model.number="extendDays" min="1" required>
        </div>
        <button type="submit">延长</button>
        <button type="button" @click="cancelForm">取消</button>
        <p v-if="extendError" class="error">{{ extendError }}</p>
        <p v-if="extendSuccess" class="success">{{ extendSuccess }}</p>
      </form>
    </div>

    <div v-if="selectedUser && formType === 'edit'" class="edit-form">
      <h3>修改 {{ selectedUser.username }} 的信息</h3>
      <form @submit.prevent="updateUser">
        <div class="form-group">
          <label for="email">邮箱</label>
          <input type="email" id="email" v-model="editForm.email" required>
        </div>
        <div class="form-group">
          <label for="startDate">注册时间</label>
          <input type="datetime-local" id="startDate" v-model="editForm.startDate">
        </div>
        <div class="form-group">
          <label for="endDate">会员到期时间</label>
          <input type="datetime-local" id="endDate" v-model="editForm.endDate">
        </div>
        <button type="submit">保存</button>
        <button type="button" @click="cancelForm">取消</button>
        <p v-if="editError" class="error">{{ editError }}</p>
        <p v-if="editSuccess" class="success">{{ editSuccess }}</p>
      </form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AdminView',
  data() {
    return {
      activeTab: 'users',
      users: [],
      selectedUser: null,
      formType: '',
      extendDays: 30,
      extendError: '',
      extendSuccess: '',
      editForm: {
        email: '',
        startDate: '',
        endDate: ''
      },
      editError: '',
      editSuccess: '',
      announcements: [],
      newAnnouncement: {
        title: '',
        content: ''
      },
      announcementError: '',
      announcementSuccess: ''
    }
  },
  mounted() {
    this.fetchUsers()
    this.fetchAnnouncements()
  },
  methods: {
    async fetchUsers() {
      try {
        const response = await fetch('/api/admin/users/detail')
        if (response.ok) {
          this.users = await response.json()
        }
      } catch (error) {
        console.error('获取用户列表失败:', error)
      }
    },
    async fetchAnnouncements() {
      try {
        const response = await fetch('/api/announcements/all')
        if (response.ok) {
          this.announcements = await response.json()
        }
      } catch (error) {
        console.error('获取公告列表失败:', error)
      }
    },
    formatDate(dateString) {
      if (!dateString) return '-'
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    showExtendForm(user) {
      this.selectedUser = user
      this.formType = 'extend'
      this.extendDays = 30
      this.extendError = ''
      this.extendSuccess = ''
    },
    showEditForm(user) {
      this.selectedUser = user
      this.formType = 'edit'
      this.editForm.email = user.email
      this.editForm.startDate = this.formatDateForInput(user.startDate)
      this.editForm.endDate = this.formatDateForInput(user.endDate)
      this.editError = ''
      this.editSuccess = ''
    },
    formatDateForInput(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${year}-${month}-${day}T${hours}:${minutes}`
    },
    cancelForm() {
      this.selectedUser = null
      this.formType = ''
      this.extendError = ''
      this.extendSuccess = ''
      this.editError = ''
      this.editSuccess = ''
    },
    async extendMembership() {
      try {
        const response = await fetch('/api/admin/extend-membership', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            userId: this.selectedUser.id,
            days: this.extendDays
          })
        })
        
        if (response.ok) {
          this.extendSuccess = '会员期限延长成功'
          this.extendError = ''
          setTimeout(() => {
            this.fetchUsers()
            this.cancelForm()
          }, 1000)
        } else {
          const errorText = await response.text()
          this.extendError = errorText
          this.extendSuccess = ''
        }
      } catch (error) {
        this.extendError = '发生错误'
        this.extendSuccess = ''
        console.error(error)
      }
    },
    async updateUser() {
      try {
        const response = await fetch('/api/admin/update-user', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            userId: this.selectedUser.id,
            email: this.editForm.email
          })
        })
        
        if (response.ok) {
          const memberResponse = await fetch('/api/admin/update-member', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify({
              userId: this.selectedUser.id,
              startDate: this.editForm.startDate ? new Date(this.editForm.startDate).toISOString() : null,
              endDate: this.editForm.endDate ? new Date(this.editForm.endDate).toISOString() : null
            })
          })
          
          if (memberResponse.ok) {
            this.editSuccess = '用户信息修改成功'
            this.editError = ''
            setTimeout(() => {
              this.fetchUsers()
              this.cancelForm()
            }, 1000)
          } else {
            const errorText = await memberResponse.text()
            this.editError = errorText
            this.editSuccess = ''
          }
        } else {
          const errorText = await response.text()
          this.editError = errorText
          this.editSuccess = ''
        }
      } catch (error) {
        this.editError = '发生错误'
        this.editSuccess = ''
        console.error(error)
      }
    },
    async createAnnouncement() {
      try {
        const response = await fetch('/api/announcements/create', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(this.newAnnouncement)
        })
        
        if (response.ok) {
          this.announcementSuccess = '公告发布成功'
          this.announcementError = ''
          this.newAnnouncement.title = ''
          this.newAnnouncement.content = ''
          this.fetchAnnouncements()
          setTimeout(() => {
            this.announcementSuccess = ''
          }, 2000)
        } else {
          const errorText = await response.text()
          this.announcementError = errorText
          this.announcementSuccess = ''
        }
      } catch (error) {
        this.announcementError = '发布公告失败'
        this.announcementSuccess = ''
        console.error(error)
      }
    },
    async deactivateAnnouncement(id) {
      try {
        const response = await fetch(`/api/announcements/deactivate/${id}`, {
          method: 'POST'
        })
        if (response.ok) {
          this.fetchAnnouncements()
        }
      } catch (error) {
        console.error('停用公告失败:', error)
      }
    },
    async activateAnnouncement(id) {
      try {
        const response = await fetch(`/api/announcements/activate/${id}`, {
          method: 'POST'
        })
        if (response.ok) {
          this.fetchAnnouncements()
        }
      } catch (error) {
        console.error('启用公告失败:', error)
      }
    },
    async deleteAnnouncement(id) {
      if (!confirm('确定要删除这条公告吗？')) return
      try {
        const response = await fetch(`/api/announcements/${id}`, {
          method: 'DELETE'
        })
        if (response.ok) {
          this.fetchAnnouncements()
        }
      } catch (error) {
        console.error('删除公告失败:', error)
      }
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
.admin-container {
  max-width: 1000px;
  margin: 50px auto;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 5px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
}

.logout-btn {
  background-color: #dc3545;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 3px;
  cursor: pointer;
  font-size: 14px;
}

.logout-btn:hover {
  background-color: #c82333;
}

.tab-container {
  display: flex;
  margin-bottom: 20px;
  border-bottom: 2px solid #ddd;
}

.tab-btn {
  padding: 12px 30px;
  background-color: transparent;
  color: #666;
  border: none;
  border-bottom: 3px solid transparent;
  cursor: pointer;
  font-size: 16px;
  margin-right: 10px;
  margin-bottom: -2px;
  transition: all 0.3s;
}

.tab-btn:hover {
  color: #333;
  background-color: #f5f5f5;
}

.tab-btn.active {
  color: #1976d2;
  border-bottom-color: #1976d2;
  font-weight: bold;
  background-color: #fff;
}

.users-section,
.announcement-section {
  margin-top: 20px;
}

.create-announcement {
  background-color: #f9f9f9;
  padding: 15px;
  border-radius: 5px;
  margin-bottom: 20px;
}

.create-announcement h4,
.announcement-list h4 {
  margin-top: 0;
}

textarea {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 3px;
  width: 100%;
  box-sizing: border-box;
  resize: vertical;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 10px;
}

th, td {
  padding: 10px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

th {
  background-color: #f2f2f2;
}

.content-cell {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

button {
  padding: 5px 10px;
  background-color: #333;
  color: white;
  border: none;
  border-radius: 3px;
  cursor: pointer;
  margin-right: 5px;
}

button:hover {
  background-color: #555;
}

.delete-btn {
  background-color: #dc3545;
}

.delete-btn:hover {
  background-color: #c82333;
}

.extend-form,
.edit-form {
  margin-top: 30px;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 5px;
}

.form-group {
  margin-bottom: 15px;
}

label {
  display: block;
  margin-bottom: 5px;
}

input {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 3px;
  width: 100%;
  box-sizing: border-box;
}

.error {
  color: red;
  margin-top: 10px;
}

.success {
  color: green;
  margin-top: 10px;
}

.active {
  color: green;
  font-weight: bold;
}

.expired {
  color: red;
  font-weight: bold;
}

.no-data {
  color: #999;
  text-align: center;
  padding: 20px;
}
</style>
