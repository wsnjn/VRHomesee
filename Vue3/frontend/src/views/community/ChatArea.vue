<template>
  <div class="chat-layout">
    <!-- Sidebar -->
    <div class="sidebar">
      <!-- User Profile Summary -->
      <div class="user-profile">
        <div class="avatar">
          <img v-if="userState.user?.avatar" :src="getAvatarSrc(userState.user.avatar)" class="avatar-img" />
          <span v-else>{{ userState.user?.username?.[0] || 'U' }}</span>
        </div>
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
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
          群组
        </button>
        <button :class="{ active: activeTab === 'friends' }" @click="activeTab = 'friends'">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
          好友
        </button>
      </div>

      <!-- Group List -->
      <div v-if="activeTab === 'groups'" class="list-container">
        <div class="list-header">
          <span>我的群组</span>
          <button class="icon-btn" @click="showCreateGroupModal = true" title="创建群组">
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
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
            <img v-if="group.displayAvatar" :src="getAvatarSrc(group.displayAvatar)" class="avatar-img" />
            <span v-else-if="group.groupType === 3">{{ group.displayName?.[0] || 'U' }}</span>
            <svg v-else xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
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
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="8.5" cy="7" r="4"></circle><line x1="20" y1="8" x2="20" y2="14"></line><line x1="23" y1="11" x2="17" y2="11"></line></svg>
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
            <img v-if="friend.avatar" :src="getAvatarSrc(friend.avatar)" class="avatar-img" />
            <span v-else>{{ friend.username?.[0] || 'F' }}</span>
          </div>
          <div class="item-info">
            <div class="item-name">{{ friend.username }}</div>
            <div class="item-desc">在线</div>
          </div>
          <button class="chat-btn" @click="startPrivateChat(friend)">
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path></svg>
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
            <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 8v8"></path><path d="M22 8v8"></path><path d="M2 12h20"></path><path d="M6 4h12"></path><path d="M6 20h12"></path></svg>
            {{ activeGroup.announcement }}
          </span>
        </div>
        <div class="header-actions">
          <button class="icon-btn" @click="showInviteModal = true" title="邀请好友">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="8.5" cy="7" r="4"></circle><line x1="20" y1="8" x2="20" y2="14"></line><line x1="23" y1="11" x2="17" y2="11"></line></svg>
          </button>
          <button class="icon-btn delete-btn" v-if="activeGroup.ownerId === currentUserId" @click="deleteGroup" title="删除群组">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path></svg>
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
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="22" y1="2" x2="11" y2="13"></line><polygon points="22 2 15 22 11 13 2 9 22 2"></polygon></svg>
          </button>
        </div>
      </div>
    </div>
    
    <div class="empty-state" v-else>
      <div class="empty-content">
        <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#E5E7EB" stroke-width="1" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path></svg>
        <p>选择一个群组或好友开始聊天</p>
        <button class="start-chat-btn" @click="showAddFriendModal = true">
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
          添加好友 / 发起聊天
        </button>
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
          <div 
            v-for="friend in friends" 
            :key="friend.id" 
            class="friend-select-item" 
            :class="{ disabled: groupMembers.some(m => m.userId === friend.friendId) }"
            @click="selectFriendToInvite(friend)"
          >
            <div class="item-avatar friend-avatar-small">
              <img v-if="friend.avatar" :src="getAvatarSrc(friend.avatar)" />
              <span v-else>{{ friend.username?.[0] || 'F' }}</span>
            </div>
            <span class="friend-name">
              {{ friend.username }}
              <span v-if="groupMembers.some(m => m.userId === friend.friendId)" class="already-in-badge">(已在群)</span>
            </span>
            <div class="checkbox" :class="{ checked: selectedInviteFriends.includes(friend.friendId) }" v-if="!groupMembers.some(m => m.userId === friend.friendId)"></div>
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
import { ref, onMounted, nextTick, computed, watch, onUnmounted } from 'vue'
import { userState } from '../../state/user'

const activeTab = ref('groups')
const groups = ref([])
const friends = ref([])
const pendingRequests = ref([])
const activeGroup = ref(null)
const groupMembers = ref([])
const messages = ref([])
const newMessage = ref('')
const msgContainer = ref(null)
const currentUserId = computed(() => userState.user ? userState.user.id : 0)

watch(currentUserId, (newId) => {
  if (newId) {
    groups.value = []
    friends.value = []
    pendingRequests.value = []
    activeGroup.value = null
    messages.value = []
    fetchGroups()
    fetchFriends()
    fetchPendingRequests()
  }
})

const showCreateGroupModal = ref(false)
const showAddFriendModal = ref(false)
const newGroupName = ref('')
const newGroupAnnouncement = ref('')
const friendIdInput = ref('')

const fetchGroups = async () => {
  if (!currentUserId.value) return
  try {
    const res = await fetch(`https://api.homesee.xyz/api/community/groups/user/${currentUserId.value}`)
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
    const res = await fetch(`https://api.homesee.xyz/api/community/friends/${currentUserId.value}`)
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
    const res = await fetch(`https://api.homesee.xyz/api/community/friends/pending/${currentUserId.value}`)
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
  await fetchGroupMembers(group.id)
}

const fetchGroupMembers = async (groupId) => {
  try {
    const res = await fetch(`https://api.homesee.xyz/api/community/groups/${groupId}/members`)
    const data = await res.json()
    if (data.success) {
      groupMembers.value = data.data
    }
  } catch (e) {
    console.error(e)
  }
}

const fetchMessages = async (groupId) => {
  try {
    const res = await fetch(`https://api.homesee.xyz/api/community/messages/group/${groupId}`)
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
    senderId: currentUserId.value,
    groupId: activeGroup.value.id,
    content: newMessage.value,
    msgType: 0
  }

  try {
    const res = await fetch('https://api.homesee.xyz/api/community/messages/send', {
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
    ownerId: currentUserId.value,
    groupType: 0 // Normal group
  }

  try {
    const res = await fetch('https://api.homesee.xyz/api/community/groups/create', {
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
    const searchRes = await fetch(`https://api.homesee.xyz/api/user/search/phone?phone=${friendIdInput.value}`)
    const searchData = await searchRes.json()
    
    if (!searchData.success || !searchData.user) {
      alert('未找到该用户')
      return
    }
    
    if (searchData.user.id === currentUserId.value) {
      alert('不能添加自己为好友')
      return
    }
    
    // 发送好友请求
    const payload = {
      userId: currentUserId.value,
      friendId: searchData.user.id
    }

    const res = await fetch('https://api.homesee.xyz/api/community/friends/request', {
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
    const res = await fetch('https://api.homesee.xyz/api/community/friends/respond', {
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
    const res = await fetch('https://api.homesee.xyz/api/community/groups/private', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ userId: currentUserId.value, friendId: friend.friendId })
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
    const res = await fetch(`https://api.homesee.xyz/api/user/${userId}`)
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
  if (senderId === currentUserId.value) return '我'
  if (!userInfoCache.value[senderId]) {
    getUserInfo(senderId)
    return `用户 ${senderId}`
  }
  return userInfoCache.value[senderId].username || `用户 ${senderId}`
}

const getAvatarUrl = (userId) => {
  if (!userInfoCache.value[userId]) {
    getUserInfo(userId)
    return '/models/image/default-avatar.png'
  }
  const avatar = userInfoCache.value[userId].avatar
  if (!avatar) return '/models/image/default-avatar.png'
  
  if (avatar.startsWith('http')) {
    return avatar
  }
  
  const FILE_SERVER_HOST = 'https://files.homesee.xyz'
  return `${FILE_SERVER_HOST}/api/files/download/${avatar}`
}

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  return new Date(timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false })
}

let pollInterval = null

onMounted(() => {
  fetchGroups()
  fetchFriends()
  fetchPendingRequests()
  
  // Poll for updates every 3 seconds
  pollInterval = setInterval(() => {
    fetchGroups()
    fetchFriends()
    fetchPendingRequests()
    if (activeGroup.value) {
      fetchMessages(activeGroup.value.id)
      fetchGroupMembers(activeGroup.value.id)
    }
  }, 3000)
})

onUnmounted(() => {
  if (pollInterval) clearInterval(pollInterval)
})

const showInviteModal = ref(false)
const inviteFriendId = ref('')

const deleteGroup = async () => {
  if (!activeGroup.value || !confirm('确定要删除该群组吗？')) return
  
  try {
    const res = await fetch('https://api.homesee.xyz/api/community/groups/delete', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ groupId: activeGroup.value.id, userId: currentUserId.value })
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
  // Check if already member
  if (groupMembers.value.some(m => m.userId === friend.friendId)) return

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
    for (const friendId of selectedInviteFriends.value) {
      const res = await fetch('https://api.homesee.xyz/api/community/groups/invite', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ 
          groupId: activeGroup.value.id, 
          inviterId: currentUserId.value,
          inviteeId: friendId
        })
      })
      const data = await res.json()
      if (!data.success) {
        throw new Error(data.message)
      }
    }
    
    alert('邀请已发送')
    showInviteModal.value = false
    selectedInviteFriends.value = []
    fetchGroupMembers(activeGroup.value.id)
  } catch (e) {
    console.error(e)
    alert(`邀请失败: ${e.message}`)
  }
}

const getAvatarSrc = (avatarName) => {
  if (!avatarName) return '/models/image/default-avatar.png'
  if (avatarName.startsWith('http')) {
    return avatarName
  }
  const FILE_SERVER_HOST = 'https://files.homesee.xyz'
  return `${FILE_SERVER_HOST}/api/files/download/${avatarName}`
}
</script>

<style scoped>
.chat-layout {
  display: flex;
  height: 100%;
  background: white;
  font-family: 'Inter', sans-serif;
  color: #111827;
}

.sidebar {
  width: 280px;
  background: white;
  border-right: 1px solid #E5E7EB;
  display: flex;
  flex-direction: column;
}

.user-profile {
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  border-bottom: 1px solid #E5E7EB;
}

.avatar {
  width: 48px;
  height: 48px;
  background: #111827; /* Dark Industrial */
  color: white;
  border-radius: 50%; /* Circular */
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 18px;
  font-family: 'JetBrains Mono', monospace;
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-info .username {
  font-weight: 700;
  color: #111827;
  font-size: 0.9rem;
}

.user-info .status {
  font-size: 0.75rem;
  color: #10B981;
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  text-transform: uppercase;
}

.status-dot {
  width: 6px;
  height: 6px;
  background: #10B981;
  border-radius: 50%; /* Circular dot */
}

.sidebar-tabs {
  display: flex;
  padding: 0;
  border-bottom: 1px solid #E5E7EB;
}

.sidebar-tabs button {
  flex: 1;
  padding: 16px;
  border: none;
  background: white;
  border-radius: 0;
  cursor: pointer;
  color: #6B7280;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 0.85rem;
  font-weight: 600;
  text-transform: uppercase;
  transition: all 0.2s;
  border-bottom: 2px solid transparent;
}

.sidebar-tabs button:hover {
  background: #F9FAFB;
  color: #111827;
}

.sidebar-tabs button.active {
  background: white;
  color: #111827;
  border-bottom: 2px solid #111827;
}

.list-container {
  flex: 1;
  overflow-y: auto;
}

.list-header {
  padding: 16px 24px;
  font-size: 0.75rem;
  font-weight: 700;
  color: #9CA3AF;
  text-transform: uppercase;
  display: flex;
  justify-content: space-between;
  align-items: center;
  letter-spacing: 0.5px;
}

.icon-btn {
  background: none;
  border: 1px solid transparent;
  cursor: pointer;
  color: #6B7280;
  padding: 4px;
  border-radius: 0;
  transition: all 0.2s;
}

.icon-btn:hover {
  border-color: #111827;
  color: #111827;
}

.list-item {
  display: flex;
  align-items: center;
  padding: 12px 24px;
  cursor: pointer;
  transition: all 0.1s;
  gap: 12px;
  border-left: 3px solid transparent;
}

.list-item:hover { background: #F9FAFB; }
.list-item.active { 
  background: #F3F4F6; 
  border-left: 3px solid #111827; 
}

.item-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%; /* Circular */
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  flex-shrink: 0;
  background: #E5E7EB;
  color: #111827;
  overflow: hidden;
}

.group-avatar { background: #111827; color: white; }
.friend-avatar { background: #E5E7EB; color: #111827; }

.item-info { flex: 1; min-width: 0; }
.item-name { 
  font-weight: 600; 
  color: #111827; 
  font-size: 0.9rem;
  white-space: nowrap; 
  overflow: hidden; 
  text-overflow: ellipsis; 
}
.item-desc { font-size: 0.75rem; color: #6B7280; margin-top: 2px; }

/* Chat Window */
.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
}

.chat-header {
  padding: 0 24px;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #E5E7EB;
}

.header-info h3 {
  font-size: 1.1rem;
  font-weight: 700;
  color: #111827;
  margin: 0;
}

.announcement {
  font-size: 0.75rem;
  color: #6B7280;
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 4px;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-wrapper {
  display: flex;
  gap: 12px;
  max-width: 80%;
}

.message-wrapper.self {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-avatar-img {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  background: #E5E7EB;
}

.message-content-wrapper {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sender-name {
  font-size: 0.75rem;
  color: #6B7280;
  margin-left: 2px;
}

.message-bubble {
  background: #F3F4F6;
  padding: 12px 16px;
  border-radius: 0;
  position: relative;
  border: 1px solid transparent;
}

.self .message-bubble {
  background: #4B5563; /* Gray instead of Black */
  color: white;
}

.msg-content {
  font-size: 0.95rem;
  line-height: 1.5;
  word-break: break-all;
}

.msg-time {
  font-size: 0.7rem;
  margin-top: 4px;
  color: #9CA3AF;
  text-align: right;
  font-family: 'JetBrains Mono', monospace;
}

.self .msg-time { color: #6B7280; }

.input-area {
  padding: 24px;
  border-top: 1px solid #E5E7EB;
}

.input-wrapper {
  display: flex;
  gap: 12px;
  background: white;
  border: 1px solid #111827;
  padding: 4px;
}

.input-wrapper input {
  flex: 1;
  border: none;
  padding: 12px;
  outline: none;
  font-size: 0.95rem;
  font-family: 'Inter', sans-serif;
}

.send-btn {
  background: #111827;
  color: white;
  border: none;
  padding: 0 16px;
  cursor: pointer;
  transition: all 0.2s;
  border-radius: 0;
}

.send-btn:disabled {
  background: #E5E7EB;
  color: #9CA3AF;
  cursor: not-allowed;
}

.send-btn:hover:not(:disabled) {
  background: #000;
}

/* Empty State */
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #9CA3AF;
  background: #FFFFFF;
}

.empty-content {
  text-align: center;
}

.empty-content p { margin: 16px 0 24px; font-weight: 500; }

.start-chat-btn {
  background: white;
  border: 1px solid #111827;
  color: #111827;
  padding: 10px 24px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
  border-radius: 0;
  text-transform: uppercase;
}

.start-chat-btn:hover {
  background: #111827;
  color: white;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(5px); /* Keep slight blur for focus */
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  width: 400px;
  padding: 32px;
  border: 1px solid #111827;
  box-shadow: none;
}

.modal-content h3 {
  margin: 0 0 24px;
  font-size: 1.25rem;
  font-weight: 700;
  text-transform: uppercase;
}

.modal-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #E5E7EB;
  margin-bottom: 16px;
  font-family: inherit;
  border-radius: 0;
  outline: none;
}

.modal-input:focus { border-color: #111827; }

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn-cancel {
  border: 1px solid #E5E7EB;
  background: white;
  color: #6B7280;
  padding: 8px 24px;
  cursor: pointer;
  border-radius: 0;
  font-weight: 600;
}

.btn-confirm {
  background: #111827;
  color: white;
  border: 1px solid #111827;
  padding: 8px 24px;
  cursor: pointer;
  border-radius: 0;
  font-weight: 600;
}

.btn-cancel:hover { border-color: #111827; color: #111827; }
.btn-confirm:hover { background: white; color: #111827; }

/* Styles for lists in modals etc */
.chat-btn {
  background: none;
  border: 1px solid #E5E7EB;
  padding: 6px;
  cursor: pointer;
  color: #111827;
  border-radius: 0;
}
.chat-btn:hover { border-color: #111827; }

.pending-section {
  padding: 0 16px;
  margin-bottom: 16px;
}

.section-label {
  font-size: 0.75rem;
  color: #9CA3AF;
  font-weight: 700;
  margin-bottom: 8px;
  text-transform: uppercase;
}

.pending-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #F9FAFB;
  border: 1px solid #F3F4F6;
  margin-bottom: 8px;
  font-size: 0.9rem;
}

.pending-actions { display: flex; gap: 8px; }
.accept-btn, .reject-btn {
  border: none;
  background: white;
  width: 28px; height: 28px;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  border: 1px solid #E5E7EB;
  border-radius: 0;
}
.accept-btn:hover { border-color: #10B981; color: #10B981; }
.reject-btn:hover { border-color: #EF4444; color: #EF4444; }

.friend-select-list {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #E5E7EB;
  margin-bottom: 24px;
}

.friend-select-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  cursor: pointer;
}
.friend-select-item:hover { background: #F9FAFB; }
.friend-select-item.disabled { opacity: 0.5; pointer-events: none; }

.friend-avatar-small { width: 32px; height: 32px; margin-right: 12px; font-size: 12px; }
.friend-name { flex: 1; font-weight: 500; font-size: 0.9rem; }
.already-in-badge { color: #9CA3AF; font-size: 0.75rem; margin-left: 4px; }
.checkbox { width: 16px; height: 16px; border: 1px solid #D1D5DB; }
.checkbox.checked { background: #111827; border-color: #111827; }

/* Custom Scrollbar for sidebar list */
.list-container::-webkit-scrollbar { width: 4px; }
.list-container::-webkit-scrollbar-thumb { background: #E5E7EB; }
</style>
