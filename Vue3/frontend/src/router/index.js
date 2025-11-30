import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    // 用户端路由（租客可访问）
    { path: '/', name: 'home', component: () => import('../views/tenant/Home.vue'), meta: { requiresAuth: true, allowedUserTypes: [1, 2, 3] } },
    { path: '/house-selection', name: 'house-selection', component: () => import('../views/tenant/HouseSelection.vue'), meta: { requiresAuth: true, allowedUserTypes: [1, 2, 3] } },
    { path: '/house-tour', name: 'house-tour', component: () => import('../views/tenant/HouseTour.vue'), meta: { requiresAuth: true, allowedUserTypes: [1, 2, 3] } },
    { path: '/user-profile', name: 'user-profile', component: () => import('../views/tenant/UserProfile.vue'), meta: { requiresAuth: true, allowedUserTypes: [1, 2, 3] } },
    { path: '/appointment', name: 'appointment', component: () => import('../views/tenant/Appointment.vue'), meta: { requiresAuth: true, allowedUserTypes: [1, 2, 3] } },
    { path: '/my-appointments', name: 'my-appointments', component: () => import('../views/tenant/MyAppointments.vue'), meta: { requiresAuth: true, allowedUserTypes: [1, 2, 3] } },
    { path: '/smart-matching', name: 'smart-matching', component: () => import('../views/tenant/SmartMatching.vue'), meta: { requiresAuth: true, allowedUserTypes: [1, 2, 3] } },
    { path: '/maintenance', name: 'maintenance', component: () => import('../views/tenant/Maintenance.vue'), meta: { requiresAuth: true, allowedUserTypes: [1, 2, 3] } },
    { path: '/community', name: 'community', component: () => import('../views/community/Community.vue'), meta: { requiresAuth: true, allowedUserTypes: [1, 2, 3] } },
    { path: '/friend-circle', name: 'friend-circle', component: () => import('../views/community/FriendCircle.vue'), meta: { requiresAuth: true, allowedUserTypes: [1, 2, 3] } },
    { path: '/community-page', name: 'community-page', component: () => import('../views/community/CommunityPage.vue'), meta: { requiresAuth: true, allowedUserTypes: [1, 2, 3] } },

    // 登录页面（共享，无需登录）
    { path: '/login', name: 'login', component: () => import('../views/Login.vue'), meta: { requiresAuth: false } },

    // 管理员端路由（仅管理员可访问）
    { path: '/admin', name: 'admin', component: () => import('../views/admin/Admin.vue'), meta: { requiresAuth: true, allowedUserTypes: [3] } },

    // 房东端路由（仅房东和管理员可访问）
    { path: '/landlord-admin', name: 'landlord-admin', component: () => import('../views/landlord/LandlordAdmin.vue'), meta: { requiresAuth: true, allowedUserTypes: [2, 3] } },

    // 交互方块 (Interactive Cube) - 父级入口
    { path: '/interactive-cube', name: 'interactive-cube', component: () => import('../views/InteractiveCube.vue'), meta: { requiresAuth: true, allowedUserTypes: [1, 2, 3] } },

    // 虚拟世界 (全息交互演示) - 子模块
    { path: '/interactive-cube/virtual-world', name: 'virtual-world', component: () => import('../views/VirtualWorld.vue'), meta: { requiresAuth: true, allowedUserTypes: [1, 2, 3] } },

    // 圣诞树 (交互演示) - 子模块
    { path: '/interactive-cube/christmas-tree', name: 'christmas-tree', component: () => import('../views/ChristmasTree.vue'), meta: { requiresAuth: true, allowedUserTypes: [1, 2, 3] } },

    // 语音识别测试页面
    { path: '/speech-test', name: 'speech-test', component: () => import('../views/SpeechTest.vue'), meta: { requiresAuth: true, allowedUserTypes: [1, 2, 3] } }
  ],
})

// 路由守卫 - 权限控制
router.beforeEach((to, from, next) => {
  // 获取用户信息
  const user = JSON.parse(localStorage.getItem('user') || '{}')

  // 如果路由需要认证
  if (to.meta.requiresAuth) {
    // 检查用户是否已登录
    if (!user.id) {
      // 未登录，重定向到登录页
      next('/login')
      return
    }

    // 检查用户类型权限
    const allowedUserTypes = to.meta.allowedUserTypes || []
    if (!allowedUserTypes.includes(user.userType)) {
      // 用户类型无权限，根据用户类型重定向到对应首页
      switch (user.userType) {
        case 1: // 租客
          next('/')
          break
        case 2: // 房东
          next('/landlord-admin')
          break
        case 3: // 管理员
          next('/admin')
          break
        default:
          next('/login')
      }

      // 显示权限不足提示
      alert('您没有权限访问该页面')
      return
    }
  }

  // 如果用户已登录但访问登录页，重定向到对应首页
  if (to.path === '/login' && user.id) {
    switch (user.userType) {
      case 1: // 租客
        next('/')
        break
      case 2: // 房东
        next('/landlord-admin')
        break
      case 3: // 管理员
        next('/admin')
        break
      default:
        next('/')
    }
    return
  }

  next()
})

export default router
