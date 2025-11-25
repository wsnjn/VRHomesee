<template>
  <div class="chat-layout">
    <!-- Sidebar -->
    <div class="sidebar">
      <!-- User Profile Summary -->
      <div class="user-profile">
        <div class="avatar">{{ userState.user?.username?.[0] || 'U' }}</div>
        <div class="user-info">
          <div class="username">{{ userState.user?.username || 'User' }}</div>
          <div class="status">
            <span class="status-dot"></span> 在线
          </div>
        </div>
      </div>

      <!-- Tabs -->
      <div class="sidebar-tabs">
        <button :class="{ active: activeTab === 'groups' }" @click="activeTab = 'groups'">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
          群组
        </button>
        <button :class="{ active: activeTab === 'friends' }" @click="activeTab = 'friends'">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
          好友
        </button>
      </div>

      <!-- Group List -->
      <div v-if="activeTab === 'groups'" class="list-container">
        <div class="list-header">
          <span>我的群组</span>
          <button class="icon-btn" @click="showCreateGroupModal = true" title="创建群组">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
          </button>
        </div>
        <div 
          v-for="group in groups" 
          :key="group.id" 
          class="list-item"
          :class="{ active: activeGroup && activeGroup.id === group.id }"
          @click="selectGroup(group)"
        >
          <div class="item-avatar group-avatar" :class="{ 'friend-avatar': group.groupType === 3 }">
            <img v-if="group.displayAvatar" :src="getAvatarSrc(group.displayAvatar)" style="width:100%;height:100%;border-radius:10px;object-fit:cover;" />
            <span v-else-if="group.groupType === 3">{{ group.displayName?.[0] || 'U' }}</span>
            <svg v-else xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
          </div>
          <div class="item-info">
            <div class="item-name">{{ group.displayName || group.groupName }}</div>
            <div class="item-desc">{{ getGroupTypeText(group.groupType) }}</div>
          </div>
        </div>
      </div>

      <!-- Friends List -->
      <div v-if="activeTab === 'friends'" class="list-container">
        <div class="list-header">
          <span>我的好友</span>
          <button class="icon-btn" @click="showAddFriendModal = true" title="添加好友">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="8.5" cy="7" r="4"></circle><line x1="20" y1="8" x2="20" y2="14"></line><line x1="23" y1="11" x2="17" y2="11"></line></svg>
          </button>
        </div>
        
        <!-- Pending Requests -->
        <div v-if="pendingRequests.length > 0" class="pending-section">
          <div class="section-label">好友申请</div>
          <div v-for="req in pendingRequests" :key="req.id" class="pending-item">
            <span>{{ req.requesterName || '用户 ' + req.requesterId }}</span>
            <div class="pending-actions">
              <button class="accept-btn" @click="respondToRequest(req.id, 1)">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>
              </button>
              <button class="reject-btn" @click="respondToRequest(req.id, 2)">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
              </button>
            </div>
          </div>
        </div>

        <div v-for="friend in friends" :key="friend.id" class="list-item">
          <div class="item-avatar friend-avatar">
            <img v-if="friend.avatar" :src="getAvatarSrc(friend.avatar)" style="width:100%;height:100%;border-radius:10px;object-fit:cover;" />
            <span v-else>{{ friend.username?.[0] || 'F' }}</span>
          </div>
          <div class="item-info">
            <div class="item-name">{{ friend.username }}</div>
            <div class="item-desc">在线</div>
          </div>
          <button class="chat-btn" @click="startPrivateChat(friend)">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path></svg>
          </button>
        </div>
      </div>
    </div>

    <!-- Chat Window -->
    <div class="chat-window" v-if="activeGroup">
      <div class="chat-header">
        <div class="header-info">
          <h3>{{ activeGroup.displayName || activeGroup.groupName }}</h3>
          <span class="announcement" v-if="activeGroup.announcement">
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 8v8"></path><path d="M22 8v8"></path><path d="M2 12h20"></path><path d="M6 4h12"></path><path d="M6 20h12"></path></svg>
            {{ activeGroup.announcement }}
          </span>
        </div>
        <div class="header-actions">
          <button class="icon-btn" @click="showInviteModal = true" title="邀请好友">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="8.5" cy="7" r="4"></circle><line x1="20" y1="8" x2="20" y2="14"></line><line x1="23" y1="11" x2="17" y2="11"></line></svg>
          </button>
          <button class="icon-btn delete-btn" v-if="activeGroup.ownerId === currentUserId" @click="deleteGroup" title="删除群组">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path></svg>
          </button>
        </div>
      </div>
      
      <div class="messages-container" ref="msgContainer">
        <div v-for="msg in messages" :key="msg.id" class="message-wrapper" :class="{ self: msg.senderId === currentUserId }">
          <div class="message-avatar-container" v-if="msg.senderId !== currentUserId">
            <img :src="getAvatarUrl(msg.senderId)" class="message-avatar-img" />
          </div>
          
          <div class="message-content-wrapper">
            <div class="sender-name" v-if="msg.senderId !== currentUserId">{{ getSenderName(msg.senderId) }}</div>
            <div class="message-bubble">
              <div class="msg-content">{{ msg.content }}</div>
              <div class="msg-time">{{ formatTime(msg.createdTime) }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="input-area">
        <div class="input-wrapper">
          <input 
            v-model="newMessage" 
            @keyup.enter="sendMessage"
            placeholder="输入消息..."
          />
          <button class="send-btn" @click="sendMessage" :disabled="!newMessage.trim()">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="22" y1="2" x2="11" y2="13"></line><polygon points="22 2 15 22 11 13 2 9 22 2"></polygon></svg>
          </button>
        </div>
      </div>
    </div>
    
    <div class="empty-state" v-else>
      <div class="empty-content">
        <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#e0e0e0" stroke-width="1" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path></svg>
        <p>选择一个群组或好友开始聊天</p>
      </div>
    </div>

    <!-- Create Group Modal -->
    <div v-if="showCreateGroupModal" class="modal-overlay">
      <div class="modal-content">
        <h3>创建新群组</h3>
        <input v-model="newGroupName" placeholder="群组名称" class="modal-input" />
        <textarea v-model="newGroupAnnouncement" placeholder="群公告 (可选)" class="modal-input"></textarea>
        <div class="modal-actions">
          <button @click="showCreateGroupModal = false" class="btn-cancel">取消</button>
          <button @click="createGroup" class="btn-confirm">创建</button>
        </div>
      </div>
    </div>

    <!-- Add Friend Modal -->
    <div v-if="showAddFriendModal" class="modal-overlay">
      <div class="modal-content">
        <h3>添加好友</h3>
        <input v-model="friendIdInput" placeholder="输入手机号" type="text" class="modal-input" />
        <div class="modal-actions">
          <button @click="showAddFriendModal = false" class="btn-cancel">取消</button>
          <button @click="sendFriendRequest" class="btn-confirm">发送请求</button>
        </div>
      </div>
    </div>

    <!-- Invite Friend Modal -->
    <div v-if="showInviteModal" class="modal-overlay">
      <div class="modal-content">
        <h3>邀请好友入群</h3>
        <div class="friend-select-list">
          <div v-for="friend in friends" :key="friend.id" class="friend-select-item" @click="selectFriendToInvite(friend)">
            <div class="item-avatar friend-avatar-small">
              <img v-if="friend.avatar" :src="getAvatarSrc(friend.avatar)" />
              <span v-else>{{ friend.username?.[0] || 'F' }}</span>
            </div>
            <span class="friend-name">{{ friend.username }}</span>
            <div class="checkbox" :class="{ checked: selectedInviteFriends.includes(friend.friendId) }"></div>
          </div>
        </div>
        <div class="modal-actions">
          <button @click="showInviteModal = false" class="btn-cancel">取消</button>
          <button @click="inviteSelectedFriends" class="btn-confirm">邀请</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { userState } from '../../state/user'

const activeTab = ref('groups')
const groups = ref([])
const friends = ref([])
const pendingRequests = ref([])
const activeGroup = ref(null)
const messages = ref([])
const newMessage = ref('')
const msgContainer = ref(null)
const currentUserId = userState.user ? userState.user.id : 0

const showCreateGroupModal = ref(false)
const showAddFriendModal = ref(false)
const newGroupName = ref('')
const newGroupAnnouncement = ref('')
const friendIdInput = ref('')

const fetchGroups = async () => {
  if (!currentUserId) return
  try {
    const res = await fetch(`http://localhost:8080/api/community/groups/user/${currentUserId}`)
    const data = await res.json()
    if (data.success) {
      groups.value = data.data
    }
  } catch (e) {
    console.error(e)
  }
}

const fetchFriends = async () => {
  try {
    const res = await fetch(`http://localhost:8080/api/community/friends/${currentUserId}`)
    const data = await res.json()
    if (data.success) {
      friends.value = data.data
    }
  } catch (e) {
    console.error(e)
  }
}

const fetchPendingRequests = async () => {
  try {
    const res = await fetch(`http://localhost:8080/api/community/friends/pending/${currentUserId}`)
    const data = await res.json()
    if (data.success) {
      pendingRequests.value = data.data
    }
  } catch (e) {
    console.error(e)
  }
}

const selectGroup = async (group) => {
  activeGroup.value = group
  await fetchMessages(group.id)
}

const fetchMessages = async (groupId) => {
  try {
    const res = await fetch(`http://localhost:8080/api/community/messages/group/${groupId}`)
    const data = await res.json()
    if (data.success) {
      messages.value = data.data
      scrollToBottom()
    }
  } catch (e) {
    console.error(e)
  }
}

const sendMessage = async () => {
  if (!newMessage.value.trim() || !activeGroup.value) return

  const payload = {
    senderId: currentUserId,
    groupId: activeGroup.value.id,
    content: newMessage.value,
    msgType: 0
  }

  try {
    const res = await fetch('http://localhost:8080/api/community/messages/send', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
    const data = await res.json()
    if (data.success) {
      messages.value.push(data.data)
      newMessage.value = ''
      scrollToBottom()
    }
  } catch (e) {
    console.error(e)
  }
}

const createGroup = async () => {
  if (!newGroupName.value.trim()) return
  
  const payload = {
    groupName: newGroupName.value,
    announcement: newGroupAnnouncement.value,
    ownerId: currentUserId,
    groupType: 0 // Normal group
  }

  try {
    const res = await fetch('http://localhost:8080/api/community/groups/create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
    const data = await res.json()
    if (data.success) {
      groups.value.push(data.data)
      showCreateGroupModal.value = false
      newGroupName.value = ''
      newGroupAnnouncement.value = ''
    }
  } catch (e) {
    console.error(e)
  }
}

const sendFriendRequest = async () => {
  if (!friendIdInput.value) return
  
  try {
    // 先通过手机号查找用户
    const searchRes = await fetch(`http://localhost:8080/api/user/search/phone?phone=${friendIdInput.value}`)
    const searchData = await searchRes.json()
    
    if (!searchData.success || !searchData.user) {
      alert('未找到该用户')
      return
    }
    
    if (searchData.user.id === currentUserId) {
      alert('不能添加自己为好友')
      return
    }
    
    // 发送好友请求
    const payload = {
      userId: currentUserId,
      friendId: searchData.user.id
    }

    const res = await fetch('http://localhost:8080/api/community/friends/request', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
    const data = await res.json()
    if (data.success) {
      alert('好友请求已发送')
      showAddFriendModal.value = false
      friendIdInput.value = ''
    } else {
      alert(data.message || '发送失败')
    }
  } catch (e) {
    console.error(e)
    alert('请求失败，请重试')
  }
}

const respondToRequest = async (requestId, status) => {
  const payload = {
    requestId: requestId,
    status: status
  }

  try {
    const res = await fetch('http://localhost:8080/api/community/friends/respond', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
    const data = await res.json()
    if (data.success) {
      fetchPendingRequests()
      if (status === 1) fetchFriends()
    }
  } catch (e) {
    console.error(e)
  }
}

const startPrivateChat = async (friend) => {
  try {
    const res = await fetch('http://localhost:8080/api/community/groups/private', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ userId: currentUserId, friendId: friend.friendId })
    })
    const data = await res.json()
    if (data.success) {
      // Add to groups list if not exists
      const exists = groups.value.find(g => g.id === data.data.id)
      if (!exists) {
        groups.value.push(data.data)
      }
      // Select it
      selectGroup(data.data)
      activeTab.value = 'groups'
    }
  } catch (e) {
    console.error(e)
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (msgContainer.value) {
      msgContainer.value.scrollTop = msgContainer.value.scrollHeight
    }
  })
}

const getGroupTypeText = (type) => {
  return type === 1 ? '全局群' : (type === 2 ? '租客群' : (type === 3 ? '好友' : '普通群'))
}

const userInfoCache = ref({})

const getUserInfo = async (userId) => {
  if (userInfoCache.value[userId]) return userInfoCache.value[userId]
  
  // Placeholder to prevent multiple fetches
  userInfoCache.value[userId] = { username: `用户 ${userId}`, avatar: '' }
  
  try {
    const res = await fetch(`http://localhost:8080/api/user/${userId}`)
    const data = await res.json()
    if (data.success) {
      userInfoCache.value[userId] = data.user
    }
  } catch (e) {
    console.error(e)
  }
  return userInfoCache.value[userId]
}

const getSenderName = (senderId) => {
  if (senderId === currentUserId) return '我'
  if (!userInfoCache.value[senderId]) {
    getUserInfo(senderId)
    return `用户 ${senderId}`
  }
  return userInfoCache.value[senderId].username || `用户 ${senderId}`
}

const getAvatarUrl = (userId) => {
  if (!userInfoCache.value[userId]) {
    getUserInfo(userId)
    return '/src/assets/image/default-avatar.png'
  }
  const avatar = userInfoCache.value[userId].avatar
  if (!avatar) return '/src/assets/image/default-avatar.png'
  return `/src/assets/image/${avatar}`
}

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  return new Date(timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

onMounted(() => {
  fetchGroups()
  fetchFriends()
  fetchPendingRequests()
})

const showInviteModal = ref(false)
const inviteFriendId = ref('')

const deleteGroup = async () => {
  if (!activeGroup.value || !confirm('确定要删除该群组吗？')) return
  
  try {
    const res = await fetch('http://localhost:8080/api/community/groups/delete', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ groupId: activeGroup.value.id, userId: currentUserId })
    })
    const data = await res.json()
    if (data.success) {
      groups.value = groups.value.filter(g => g.id !== activeGroup.value.id)
      activeGroup.value = null
    } else {
      alert(data.message)
    }
  } catch (e) {
    console.error(e)
  }
}

const selectedInviteFriends = ref([])

const selectFriendToInvite = (friend) => {
  const index = selectedInviteFriends.value.indexOf(friend.friendId)
  if (index === -1) {
    selectedInviteFriends.value.push(friend.friendId)
  } else {
    selectedInviteFriends.value.splice(index, 1)
  }
}

const inviteSelectedFriends = async () => {
  if (selectedInviteFriends.value.length === 0 || !activeGroup.value) return
  
  try {
    // Invite one by one for now as backend supports single invite
    // Or update backend to support batch. Let's do loop for simplicity.
    for (const friendId of selectedInviteFriends.value) {
      await fetch('http://localhost:8080/api/community/groups/invite', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ 
          groupId: activeGroup.value.id, 
          inviterId: currentUserId,
          inviteeId: friendId
        })
      })
    }
    
    alert('邀请已发送')
    showInviteModal.value = false
    selectedInviteFriends.value = []
  } catch (e) {
    console.error(e)
    alert('邀请部分或全部失败')
  }
}

const getAvatarSrc = (avatarName) => {
  if (!avatarName) return '/src/assets/image/default-avatar.png'
  return `/src/assets/image/${avatarName}`
}
</script>

<style scoped>
.chat-layout {
  display: flex;
  height: 100%;
  background: #f3f4f6;
  font-family: 'Inter', sans-serif;
}

.sidebar {
  width: 280px;
  background: white;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
}

.user-profile {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid #f3f4f6;
}

.avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 18px;
}

.user-info .username {
  font-weight: 600;
  color: #1f2937;
}

.user-info .status {
  font-size: 12px;
  color: #10b981;
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-dot {
  width: 6px;
  height: 6px;
  background: #10b981;
  border-radius: 50%;
}

.sidebar-tabs {
  display: flex;
  padding: 10px;
  gap: 5px;
}

.sidebar-tabs button {
  flex: 1;
  padding: 8px;
  border: none;
  background: none;
  border-radius: 8px;
  cursor: pointer;
  color: #6b7280;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 14px;
  transition: all 0.2s;
}

.sidebar-tabs button.active {
  background: #eff6ff;
  color: #3b82f6;
  font-weight: 500;
}

.list-container {
  flex: 1;
  overflow-y: auto;
}

.list-header {
  padding: 15px 20px;
  font-size: 12px;
  font-weight: 600;
  color: #9ca3af;
  text-transform: uppercase;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.icon-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #6b7280;
  padding: 4px;
  border-radius: 4px;
  transition: background 0.2s;
}

.icon-btn:hover {
  background: #f3f4f6;
  color: #3b82f6;
}

.list-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  cursor: pointer;
  transition: background 0.2s;
  gap: 12px;
}

.list-item:hover { background: #f9fafb; }
.list-item.active { background: #eff6ff; border-right: 3px solid #3b82f6; }

.item-avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  flex-shrink: 0;
}

.group-avatar {
  background: #3b82f6;
  color: white;
}

.friend-avatar {
  background: #ec4899;
  color: white;
}

.item-info { flex: 1; min-width: 0; }
.item-name { font-weight: 500; color: #374151; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.item-desc { font-size: 12px; color: #9ca3af; }

.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #ffffff;
}

.chat-header {
  padding: 15px 25px;
  border-bottom: 1px solid #f3f4f6;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-header h3 {
  margin: 0;
  font-size: 18px;
  color: #1f2937;
}

.announcement {
  font-size: 12px;
  color: #6b7280;
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 4px;
}

.messages-container {
  flex: 1;
  padding: 25px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
  background: #f9fafb;
}

.message-wrapper {
  display: flex;
  gap: 10px;
  max-width: 70%;
  align-items: flex-start;
}

.message-wrapper.self {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-avatar-container {
  flex-shrink: 0;
}

.message-avatar-img {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #e5e7eb;
}

.message-content-wrapper {
  display: flex;
  flex-direction: column;
  max-width: 100%;
}

.message-bubble {
  background: white;
  padding: 10px 14px;
  border-radius: 12px;
  border-top-left-radius: 2px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  position: relative;
  word-wrap: break-word;
}

.message-wrapper.self .message-bubble {
  background: #3b82f6;
  color: white;
  border-radius: 12px;
  border-top-right-radius: 2px;
  border-top-left-radius: 12px;
}

.sender-name {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 4px;
  margin-left: 2px;
}

.msg-content {
  font-size: 14px;
  line-height: 1.5;
}

.msg-time {
  font-size: 10px;
  color: #9ca3af;
  margin-top: 4px;
  text-align: right;
}

.message-wrapper.self .msg-time {
  color: rgba(255,255,255,0.7);
}

.input-area {
  padding: 20px;
  border-top: 1px solid #f3f4f6;
}

.input-wrapper {
  display: flex;
  gap: 10px;
  background: #f3f4f6;
  padding: 8px;
  border-radius: 12px;
  align-items: center;
}

.input-wrapper input {
  flex: 1;
  border: none;
  background: transparent;
  padding: 8px 12px;
  font-size: 14px;
}

.input-wrapper input:focus {
  outline: none;
}

.send-btn {
  background: #3b82f6;
  color: white;
  border: none;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.2s;
}

.send-btn:hover {
  background: #2563eb;
}

.send-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
}

.empty-content {
  text-align: center;
  color: #9ca3af;
}

.empty-content p {
  margin-top: 15px;
  font-size: 16px;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 25px;
  border-radius: 16px;
  width: 90%;
  max-width: 400px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
}

.modal-content h3 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #1f2937;
}

.modal-input {
  width: 100%;
  padding: 10px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  margin-bottom: 15px;
  box-sizing: border-box;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn-cancel {
  background: white;
  border: 1px solid #d1d5db;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
}

.btn-confirm {
  background: #3b82f6;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
}

.pending-section {
  padding: 10px 20px;
  border-bottom: 1px solid #f3f4f6;
}

.section-label {
  font-size: 11px;
  color: #9ca3af;
  margin-bottom: 8px;
  text-transform: uppercase;
}

.pending-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  font-size: 14px;
}

.pending-actions {
  display: flex;
  gap: 5px;
}

.accept-btn, .reject-btn {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.accept-btn { background: #d1fae5; color: #059669; }
.reject-btn { background: #fee2e2; color: #dc2626; }

.friend-select-list {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  margin-bottom: 15px;
}

.friend-select-item {
  display: flex;
  align-items: center;
  padding: 10px;
  cursor: pointer;
  border-bottom: 1px solid #f3f4f6;
  gap: 10px;
}

.friend-select-item:hover {
  background: #f9fafb;
}

.friend-avatar-small {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #ec4899;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.friend-avatar-small img {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  object-fit: cover;
}

.friend-name {
  flex: 1;
  font-size: 14px;
  color: #374151;
}

.checkbox {
  width: 18px;
  height: 18px;
  border: 2px solid #d1d5db;
  border-radius: 4px;
  position: relative;
}

.checkbox.checked {
  background: #3b82f6;
  border-color: #3b82f6;
}

.checkbox.checked::after {
  content: '';
  position: absolute;
  left: 5px;
  top: 1px;
  width: 4px;
  height: 10px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}
</style>
