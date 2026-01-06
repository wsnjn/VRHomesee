<!--
  项目名称：融合大模型交互与3D全景预览的智能选房平台设计与实现
  文件名称：HouseStatus.vue
  开发者：牛迦楠
  专业：软件工程（中外合作办学）
  学校：东华理工大学
  功能描述：房屋状态管理组件，提供房屋列表展示、状态切换、编辑和VR场景上传功能
  创建日期：2026-01-06
-->
<template>
  <div class="my-houses">
    <div class="page-header">
      <h2>我的房屋</h2>
      <div class="header-actions">
        <!-- 租赁类型筛选器 -->
        <div class="rental-type-filter">
          <select v-model="filterRentalType">
            <option value="">全部类型</option>
            <option value="0">整租</option>
            <option value="1">合租</option>
            <option value="2">单间</option>
          </select>
        </div>

        <!-- 状态筛选器 -->
        <div class="status-filter-tabs">
          <button 
            class="filter-tab" 
            :class="{ active: selectedStatus === '' }"
            @click="selectedStatus = ''"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <path d="M3 13h2v-2H3v2zm0 4h2v-2H3v2zm0-8h2V7H3v2zm4 4h14v-2H7v2zm0 4h14v-2H7v2zM7 7v2h14V7H7z"/>
            </svg>
            <span>全部</span>
            <span class="count">{{ props.myHouses.length }}</span>
          </button>
          <button 
            class="filter-tab status-0" 
            :class="{ active: selectedStatus === '0' }"
            @click="selectedStatus = '0'"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8h5z"/>
            </svg>
            <span>可租</span>
            <span class="count">{{ statusCounts.available }}</span>
          </button>
          <button 
            class="filter-tab status-1" 
            :class="{ active: selectedStatus === '1' }"
            @click="selectedStatus = '1'"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
            </svg>
            <span>已租</span>
            <span class="count">{{ statusCounts.rented }}</span>
          </button>
          <button 
            class="filter-tab status-2" 
            :class="{ active: selectedStatus === '2' }"
            @click="selectedStatus = '2'"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
            </svg>
            <span>下架</span>
            <span class="count">{{ statusCounts.offline }}</span>
          </button>
          <button 
            class="filter-tab status-3" 
            :class="{ active: selectedStatus === '3' }"
            @click="selectedStatus = '3'"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <path d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z"/>
            </svg>
            <span>预租</span>
            <span class="count">{{ statusCounts.preRent }}</span>
          </button>
        </div>
        <div class="search-wrapper">
          <div class="container">
            <div class="search-container">
              <input class="input" type="text" placeholder="搜索房屋..." v-model="searchQuery">
              <svg viewBox="0 0 24 24" class="search__icon">
                <g>
                  <path d="M21.53 20.47l-3.66-3.66C19.195 15.24 20 13.214 20 11c0-4.97-4.03-9-9-9s-9 4.03-9 9 4.03 9 9 9c2.215 0 4.24-.804 5.808-2.13l3.66 3.66c.147.146.34.22.53.22s.385-.073.53-.22c.295-.293.295-.767.002-1.06zM3.5 11c0-4.135 3.365-7.5 7.5-7.5s7.5 3.365 7.5 7.5-3.365 7.5-7.5 7.5-7.5-3.365-7.5-7.5z">
                  </path>
                </g>
              </svg>
            </div>
          </div>
        </div>
        <button @click="emit('refresh')" class="refresh-btn">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
            <path d="M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/>
          </svg>
          刷新
        </button>
        <button @click="showAddHouse = true" class="add-btn">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
            <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
          </svg>
          添加房屋
        </button>
      </div>
    </div>

    <!-- 编辑房屋模态框 -->
      <!-- 房屋详情模态框 -->
      <div v-if="showHouseDetail" class="modal-overlay">
        <div class="modal-content" style="max-width: 800px;">
          <div class="modal-header">
            <h3>房屋详情</h3>
            <button @click="closeHouseDetail" class="close-btn">×</button>
          </div>
          <div class="modal-body">
            <div v-if="loadingHouseDetail" class="loading">加载中...</div>
            <div v-else-if="houseDetailError" class="error-message">{{ houseDetailError }}</div>
            <div v-else-if="houseDetailData" class="house-detail-content">
              <!-- 房屋基本信息 -->
              <div class="detail-section">
                <h4 class="section-title">基本信息</h4>
                <div class="detail-grid">
                  <div class="detail-item">
                    <label>地址：</label>
                    <span>{{ getHouseFullAddress(houseDetailData.houseInfo) }}</span>
                  </div>
                  <div class="detail-item">
                    <label>面积：</label>
                    <span>{{ houseDetailData.houseInfo.roomArea || '--' }} ㎡</span>
                  </div>
                  <div class="detail-item">
                    <label>楼层：</label>
                    <span>{{ houseDetailData.houseInfo.floorInfo || '--' }}</span>
                  </div>
                  <div class="detail-item">
                    <label>朝向：</label>
                    <span>{{ houseDetailData.houseInfo.orientation || '--' }}</span>
                  </div>
                  <div class="detail-item">
                    <label>装修：</label>
                    <span>{{ getDecorationText(houseDetailData.houseInfo.decoration) }}</span>
                  </div>
                  <div class="detail-item">
                    <label>电梯：</label>
                    <span>{{ houseDetailData.houseInfo.hasElevator === 1 ? '有' : '无' }}</span>
                  </div>
                  <div class="detail-item">
                    <label>租赁类型：</label>
                    <span>{{ getRentalTypeText(houseDetailData.houseInfo.rentalType) }}</span>
                  </div>
                  <div class="detail-item">
                    <label>状态：</label>
                    <span class="house-status" :class="getStatusClass(houseDetailData.houseInfo.status)">
                      {{ getStatusText(houseDetailData.houseInfo.status) }}
                    </span>
                  </div>
                </div>
              </div>

              <!-- 价格信息 -->
              <div class="detail-section">
                <h4 class="section-title">价格信息</h4>
                <div class="detail-grid">
                  <div class="detail-item">
                    <label>月租金：</label>
                    <span class="price-highlight">{{ houseDetailData.houseInfo.rentPrice }} 元/月</span>
                  </div>
                  <div class="detail-item">
                    <label>水费单价：</label>
                    <span>{{ houseDetailData.houseInfo.waterPrice ? houseDetailData.houseInfo.waterPrice + '元/吨' : '包含在租金内' }}</span>
                  </div>
                  <div class="detail-item">
                    <label>电费单价：</label>
                    <span>{{ houseDetailData.houseInfo.electricPrice ? houseDetailData.houseInfo.electricPrice + '元/度' : '包含在租金内' }}</span>
                  </div>
                </div>
              </div>

              <!-- 租客信息 -->
              <div class="detail-section">
                <h4 class="section-title">租客信息</h4>
                <div v-if="!houseDetailData.hasTenant" class="no-tenant">
                  <p>当前没有租客</p>
                </div>
                <div v-else class="tenants-list">
                  <div v-for="(tenant, index) in houseDetailData.tenants" :key="tenant.id" class="tenant-item">
                    <h5>租客 {{ index + 1 }}</h5>
                    <div class="tenant-details">
                      <div class="detail-item">
                        <label>合同编号：</label>
                        <span>{{ tenant.contractNumber }}</span>
                      </div>
                      <div class="detail-item">
                        <label>租约状态：</label>
                        <span>{{ getContractStatusText(tenant.contractStatus) }}</span>
                      </div>
                      <div class="detail-item">
                        <label>合同期限：</label>
                        <span>{{ formatDate(tenant.contractStartDate) }} 至 {{ formatDate(tenant.contractEndDate) }}</span>
                      </div>
                      <div class="detail-item">
                        <label>月租金：</label>
                        <span>{{ tenant.monthlyRent }} 元</span>
                      </div>
                      <div class="detail-item">
                        <label>押金：</label>
                        <span>{{ tenant.depositAmount }} 元</span>
                      </div>
                      <div class="detail-item">
                        <label>付款周期：</label>
                        <span>{{ getPaymentCycleText(tenant.paymentCycle) }}</span>
                      </div>
                      <div class="detail-item">
                        <label>租金状态：</label>
                        <span>{{ getRentStatusText(tenant.rentStatus) }}</span>
                      </div>
                      <div class="detail-item">
                        <label>押金状态：</label>
                        <span>{{ getDepositStatusText(tenant.depositStatus) }}</span>
                      </div>
                      <div v-if="tenant.emergencyContact" class="detail-item">
                        <label>紧急联系人：</label>
                        <span>{{ tenant.emergencyContact }} ({{ tenant.emergencyPhone || '无电话' }})</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 描述信息 -->
              <div v-if="houseDetailData.houseInfo.description" class="detail-section">
                <h4 class="section-title">房屋描述</h4>
                <div class="description-content">
                  {{ houseDetailData.houseInfo.description }}
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button @click="closeHouseDetail" class="cancel-btn">关闭</button>
          </div>
        </div>
      </div>

      <!-- 编辑房屋模态框 -->
      <div v-if="showEditHouse" class="modal-overlay">
      <div class="modal-content">
        <div class="modal-header">
          <h3>编辑房屋信息</h3>
          <button @click="closeEditHouse" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleSubmitEditHouse" class="edit-house-form">
            <div class="form-grid">
              <!-- 价格信息 -->
              <div class="form-group">
                <label for="edit-rentPrice">月租金 (元) *</label>
                <input 
                  type="number" 
                  id="edit-rentPrice" 
                  v-model="editHouseData.rentPrice" 
                  required 
                  step="0.01" 
                  placeholder="如：2500"
                >
              </div>
              <div class="form-group">
                <label for="edit-waterPrice">水费单价 (元/吨)</label>
                <input 
                  type="number" 
                  id="edit-waterPrice" 
                  v-model="editHouseData.waterPrice" 
                  step="0.01" 
                  placeholder="如：3.5"
                >
              </div>
              <div class="form-group">
                <label for="edit-electricPrice">电费单价 (元/度)</label>
                <input 
                  type="number" 
                  id="edit-electricPrice" 
                  v-model="editHouseData.electricPrice" 
                  step="0.01" 
                  placeholder="如：0.8"
                >
              </div>

              <!-- 房屋基本信息 -->
              <div class="form-group">
                <label for="edit-roomArea">房屋面积 (㎡)</label>
                <input 
                  type="number" 
                  id="edit-roomArea" 
                  v-model="editHouseData.roomArea" 
                  step="0.01" 
                  placeholder="如：85.5"
                >
              </div>
              <div class="form-group">
                <label for="edit-floorInfo">楼层信息</label>
                <input 
                  type="text" 
                  id="edit-floorInfo" 
                  v-model="editHouseData.floorInfo" 
                  placeholder="如：5/18"
                >
              </div>
              <div class="form-group">
                <label for="edit-orientation">朝向</label>
                <input 
                  type="text" 
                  id="edit-orientation" 
                  v-model="editHouseData.orientation" 
                  placeholder="如：南"
                >
              </div>
              <div class="form-group">
                <label for="edit-decoration">装修程度</label>
                <select id="edit-decoration" v-model="editHouseData.decoration">
                  <option value="1">毛坯</option>
                  <option value="2">简装</option>
                  <option value="3">精装</option>
                  <option value="4">豪装</option>
                </select>
              </div>
              <div class="form-group">
                <label for="edit-hasElevator">有无电梯</label>
                <select id="edit-hasElevator" v-model="editHouseData.hasElevator">
                  <option value="0">无</option>
                  <option value="1">有</option>
                </select>
              </div>

              <!-- 租赁信息 -->
              <div class="form-group">
                <label for="edit-rentalType">租赁类型 *</label>
                <select id="edit-rentalType" v-model="editHouseData.rentalType" required>
                  <option value="0">整租</option>
                  <option value="1">合租</option>
                  <option value="2">单间</option>
                </select>
              </div>
              <div class="form-group">
                <label for="edit-status">房屋状态 *</label>
                <select id="edit-status" v-model="editHouseData.status" required>
                  <option value="0">可租</option>
                  <option value="1">已租</option>
                  <option value="2">下架</option>
                  <option value="3">预租</option>
                </select>
              </div>

              <!-- 描述信息 -->
              <div class="form-group full-width">
                <label for="edit-description">房屋描述</label>
                <textarea 
                  id="edit-description" 
                  v-model="editHouseData.description" 
                  rows="4" 
                  placeholder="请描述房屋的装修情况、设施配备、周边环境等信息..."
                ></textarea>
              </div>
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button @click="closeEditHouse" class="cancel-btn">取消</button>
          <button @click="handleSubmitEditHouse" class="confirm-btn" :disabled="updatingHouse">
            {{ updatingHouse ? '更新中...' : '确认更新' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 房屋列表 (管理员风格) -->
    <div class="house-list-container">
      <div v-if="loadingHouses" class="loading">加载中...</div>
      
      <div v-else-if="myHouses.length === 0" class="no-houses">
        <div class="empty-state">
          <span class="empty-icon">🏠</span>
          <h3>暂无房屋</h3>
          <p>您还没有添加任何房屋信息</p>
          <button @click="showAddHouse = true" class="add-first-btn">添加第一套房屋</button>
        </div>
      </div>

      <div v-else class="house-list">
        <!-- 搜索结果提示 -->
        <div v-if="searchQuery && filteredHouses.length === 0" class="search-no-results">
          没有找到匹配的房屋
          <button @click="searchQuery = ''" class="clear-search-btn">清空</button>
        </div>
        
        <div class="house-row" v-for="house in filteredHouses" :key="house.id">
          <!-- 左侧：地址盒子 -->
          <div class="row-address">
            <div class="addr-main">{{ getHouseFullAddress(house) }}</div>
            <div class="addr-sub">{{ house.communityName }} · {{ getRentalTypeText(house.rentalType) }} · {{ house.roomNumber || '' }}</div>
          </div>
          <!-- 中间：房屋信息 -->
          <div class="row-info">
            <span class="info-label">面积:</span><span class="info-val">{{ house.roomArea || '-' }}㎡</span>
            <span class="info-label">楼层:</span><span class="info-val">{{ house.floorInfo || '-' }}</span>
            <span class="info-label">朝向:</span><span class="info-val">{{ house.orientation || '-' }}</span>
            <span class="info-label">装修:</span><span class="info-val">{{ getDecorationText(house.decoration) }}</span>
            <span class="info-label">电梯:</span><span class="info-val">{{ house.hasElevator === 1 ? '有' : '无' }}</span>
          </div>
          <!-- 价格 -->
          <div class="row-price">
            <div class="price-main">{{ house.rentPrice }}元/月</div>
            <div class="price-sub">水费:{{ house.waterPrice || '-' }}元/吨</div>
            <div class="price-sub">电费:{{ house.electricPrice || '-' }}元/度</div>
          </div>
          <!-- 状态 -->
          <div class="row-status">
            <span class="status-tag" :class="getStatusClass(house.status)">{{ getStatusText(house.status) }}</span>
          </div>
          <!-- 操作按钮 -->
          <div class="row-actions">
            <button @click="openEditHouse(house)">编辑</button>
            <button 
              @click="toggleHouseStatus(house)" 
              :class="getStatusBtnClass(house.status)"
              style="color: white;" 
            >
              {{ getStatusBtnText(house.status) }}
            </button>
            <button @click="openHouseDetail(house)">详情</button>
            <button @click="openVrDialog(house)">VR</button>
          </div>
        </div>
      </div>
    </div>

    <!-- VR管理对话框 -->
    <div v-if="showVrDialogVisible" class="modal-overlay">
      <div class="modal-content vr-dialog">
        <div class="modal-header">
          <h3>VR全景图管理 - {{ currentVrHouse?.communityName }}</h3>
          <button @click="closeVrDialog" class="close-btn">×</button>
        </div>
        <div class="modal-body">
           <!-- 现有场景列表 -->
          <div class="scene-list-manage">
            <h4>现有场景</h4>
            <div v-if="vrScenes.length === 0" class="no-scenes">暂无VR场景</div>
            <div v-else class="scenes-grid">
              <div v-for="scene in vrScenes" :key="scene.id" class="scene-card">
                <div class="scene-preview">
                  <img :src="scene.imageUrl" :alt="scene.sceneName">
                </div>
                <div class="scene-info">
                  <span>{{ scene.sceneName }}</span>
                  <button @click="deleteVrScene(scene.id)" class="delete-icon">🗑️</button>
                </div>
              </div>
            </div>
          </div>
          <!-- 上传新场景 -->
          <div class="upload-section">
            <h4>上传新场景</h4>
            <div class="upload-form">
              <input v-model="newSceneName" type="text" placeholder="场景名称 (如: 客厅)" class="scene-name-input" />
              <input type="file" @change="handleVrFileSelect" accept="image/*" class="vr-file-input" />
              <button @click="uploadVrScene" :disabled="!newSceneName || !selectedFile || uploadingVr" class="upload-btn">
                {{ uploadingVr ? '上传中...' : '上传' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加房屋模态框 -->
    <div v-if="showAddHouse" class="modal-overlay">
      <div class="modal-content">
        <div class="modal-header">
          <h3>添加房屋</h3>
          <button @click="showAddHouse = false" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="emit('submitAddHouse', newHouse)" class="add-house-form">
            <div class="form-grid">
              <!-- 地址信息 -->
              <div class="form-group">
                <label for="province">省份 *</label>
                <input 
                  type="text" 
                  id="province" 
                  v-model="newHouse.province" 
                  required 
                  placeholder="如：广东省"
                >
              </div>
              <div class="form-group">
                <label for="city">城市 *</label>
                <input 
                  type="text" 
                  id="city" 
                  v-model="newHouse.city" 
                  required 
                  placeholder="如：深圳市"
                >
              </div>
              <div class="form-group">
                <label for="district">区县 *</label>
                <input 
                  type="text" 
                  id="district" 
                  v-model="newHouse.district" 
                  required 
                  placeholder="如：南山区"
                >
              </div>
              <div class="form-group">
                <label for="street">街道 *</label>
                <input 
                  type="text" 
                  id="street" 
                  v-model="newHouse.street" 
                  required 
                  placeholder="如：科技园街道"
                >
              </div>
              <div class="form-group">
                <label for="communityName">小区名称 *</label>
                <input 
                  type="text" 
                  id="communityName" 
                  v-model="newHouse.communityName" 
                  required 
                  placeholder="如：万科城"
                >
              </div>
              <div class="form-group">
                <label for="buildingUnit">单元号</label>
                <input 
                  type="text" 
                  id="buildingUnit" 
                  v-model="newHouse.buildingUnit" 
                  placeholder="如：1单元"
                >
              </div>
              <div class="form-group">
                <label for="doorNumber">门牌号 *</label>
                <input 
                  type="text" 
                  id="doorNumber" 
                  v-model="newHouse.doorNumber" 
                  required 
                  placeholder="如：101号"
                >
              </div>
              <div class="form-group">
                <label for="roomNumber">房间号</label>
                <input 
                  type="text" 
                  id="roomNumber" 
                  v-model="newHouse.roomNumber" 
                  placeholder="如：301室"
                >
              </div>

              <!-- 房屋基本信息 -->
              <div class="form-group">
                <label for="roomArea">房屋面积 (㎡)</label>
                <input 
                  type="number" 
                  id="roomArea" 
                  v-model="newHouse.roomArea" 
                  step="0.01" 
                  placeholder="如：85.5"
                >
              </div>
              <div class="form-group">
                <label for="floorInfo">楼层信息</label>
                <input 
                  type="text" 
                  id="floorInfo" 
                  v-model="newHouse.floorInfo" 
                  placeholder="如：5/18"
                >
              </div>
              <div class="form-group">
                <label for="orientation">朝向</label>
                <input 
                  type="text" 
                  id="orientation" 
                  v-model="newHouse.orientation" 
                  placeholder="如：南"
                >
              </div>
              <div class="form-group">
                <label for="decoration">装修程度</label>
                <select id="decoration" v-model="newHouse.decoration">
                  <option value="1">毛坯</option>
                  <option value="2">简装</option>
                  <option value="3">精装</option>
                  <option value="4">豪装</option>
                </select>
              </div>
              <div class="form-group">
                <label for="hasElevator">有无电梯</label>
                <select id="hasElevator" v-model="newHouse.hasElevator">
                  <option value="0">无</option>
                  <option value="1">有</option>
                </select>
              </div>

              <!-- 价格信息 -->
              <div class="form-group">
                <label for="rentPrice">月租金 (元) *</label>
                <input 
                  type="number" 
                  id="rentPrice" 
                  v-model="newHouse.rentPrice" 
                  required 
                  step="0.01" 
                  placeholder="如：2500"
                >
              </div>
              <div class="form-group">
                <label for="waterPrice">水费单价 (元/吨)</label>
                <input 
                  type="number" 
                  id="waterPrice" 
                  v-model="newHouse.waterPrice" 
                  step="0.01" 
                  placeholder="如：3.5"
                >
              </div>
              <div class="form-group">
                <label for="electricPrice">电费单价 (元/度)</label>
                <input 
                  type="number" 
                  id="electricPrice" 
                  v-model="newHouse.electricPrice" 
                  step="0.01" 
                  placeholder="如：0.8"
                >
              </div>

              <!-- 租赁信息 -->
              <div class="form-group">
                <label for="rentalType">租赁类型 *</label>
                <select id="rentalType" v-model="newHouse.rentalType" required>
                  <option value="0">整租</option>
                  <option value="1">合租</option>
                  <option value="2">单间</option>
                </select>
              </div>
              <div class="form-group">
                <label for="status">房屋状态 *</label>
                <select id="status" v-model="newHouse.status" required>
                  <option value="0">可租</option>
                  <option value="1">已租</option>
                  <option value="2">下架</option>
                  <option value="3">预租</option>
                </select>
              </div>

              <!-- 描述信息 -->
              <div class="form-group full-width">
                <label for="description">房屋描述</label>
                <textarea 
                  id="description" 
                  v-model="newHouse.description" 
                  rows="4" 
                  placeholder="请描述房屋的装修情况、设施配备、周边环境等信息..."
                ></textarea>
              </div>
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button @click="showAddHouse = false" class="cancel-btn">取消</button>
          <button @click="handleSubmitAddHouse" class="confirm-btn" :disabled="addingHouse">
            {{ addingHouse ? '添加中...' : '确认添加' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits, computed } from 'vue'
import axios from 'axios'

const API_BASE_URL = 'https://api.homesee.xyz/api'

const props = defineProps({
  myHouses: {
    type: Array,
    default: () => []
  },
  loadingHouses: {
    type: Boolean,
    default: false
  },
  addingHouse: {
    type: Boolean,
    default: false
  },
  tenantContracts: {
    type: Array,
    default: () => []
  },
  pendingAppointments: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits([
  'refresh',
  'editHouse',
  'viewHouseDetail',
  'submitAddHouse'
])

const showAddHouse = ref(false)
const showEditHouse = ref(false)
const editingHouse = ref(null)
const updatingHouse = ref(false)

// 状态筛选
const selectedStatus = ref('')
const filterRentalType = ref('')

// 状态计数（允许重叠：一个房屋可能同时属于多个状态）
const statusCounts = computed(() => {
  // 已租：有有效合同的房屋 ID（合同状态为已签约/履行中）
  const rentedRoomIds = new Set(
    props.tenantContracts
      .filter(c => c.contractStatus === 1 || c.contractStatus === 2)
      .map(c => c.roomId)
  )
  
  // 预租：有待确认预约的房屋 ID
  const preRentRoomIds = new Set(
    props.pendingAppointments
      .filter(a => a.roomId)
      .map(a => a.roomId)
  )
  
  // 统计各状态房屋数（允许重叠）
  let available = 0 // 状态 === 0 (可租)
  let rented = 0    // 有有效合同
  let offline = 0   // 状态 === 2 (下架)
  let preRent = 0   // 有待确认预约
  
  props.myHouses.forEach(house => {
    // 可租：按 状态 === 0 统计
    if (house.status === 0) {
      available++
    }
    // 下架：按 状态 === 2 统计
    if (house.status === 2) {
      offline++
    }
    // 已租：有有效合同
    if (rentedRoomIds.has(house.id)) {
      rented++
    }
    // 预租：有待确认或已确认预约
    if (preRentRoomIds.has(house.id)) {
      preRent++
    }
  })
  
  return { available, rented, offline, preRent }
})

// 搜索功能
const searchQuery = ref('')

// 检查房屋是否匹配指定状态
const matchesStatus = (house, statusType) => {
  switch (statusType) {
    case 'available':
      return house.status === 0
    case 'offline':
      return house.status === 2
    case 'rented':
      return props.tenantContracts.some(
        c => c.roomId === house.id && (c.contractStatus === 1 || c.contractStatus === 2)
      )
    case 'preRent':
      return props.pendingAppointments.some(a => a.roomId === house.id)
    default:
      return true
  }
}

const filteredHouses = computed(() => {
  let houses = props.myHouses
  
  // 再按搜索关键词筛选
  if (filterRentalType.value !== '') {
    houses = houses.filter(h => h.rentalType === parseInt(filterRentalType.value))
  }
  
  // 先按状态筛选（允许一个房屋匹配多个状态）
  if (selectedStatus.value !== '') {
    const statusMap = { '0': 'available', '1': 'rented', '2': 'offline', '3': 'preRent' }
    const targetStatus = statusMap[selectedStatus.value]
    houses = houses.filter(house => matchesStatus(house, targetStatus))
  }
  
  // 再按搜索关键词筛选
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    houses = houses.filter(house => {
      // 搜索地址信息
      const address = getHouseFullAddress(house).toLowerCase()
      // 搜索小区名称
      const communityName = house.communityName ? house.communityName.toLowerCase() : ''
      // 搜索房屋状态
      const statusText = getStatusText(house.status).toLowerCase()
      // 搜索租赁类型
      const rentalTypeText = getRentalTypeText(house.rentalType).toLowerCase()
    
      return address.includes(query) || 
             communityName.includes(query) || 
             statusText.includes(query) ||
             rentalTypeText.includes(query)
    })
  }
  
  return houses
})

// 房屋详情相关
const showHouseDetail = ref(false)
const loadingHouseDetail = ref(false)
const houseDetailData = ref(null)
const houseDetailError = ref('')

// 编辑房屋数据
const editHouseData = ref({
  rentPrice: null,
  waterPrice: null,
  electricPrice: null,
  status: '0',
  roomArea: null,
  floorInfo: '',
  orientation: '',
  decoration: '2',
  hasElevator: '0',
  rentalType: '0',
  description: ''
})

// 打开编辑房屋模态框
const openEditHouse = (house) => {
  editingHouse.value = house
  editHouseData.value = {
    rentPrice: house.rentPrice,
    waterPrice: house.waterPrice,
    electricPrice: house.electricPrice,
    status: house.status.toString(),
    roomArea: house.roomArea,
    floorInfo: house.floorInfo || '',
    orientation: house.orientation || '',
    decoration: house.decoration ? house.decoration.toString() : '2',
    hasElevator: house.hasElevator ? house.hasElevator.toString() : '0',
    rentalType: house.rentalType ? house.rentalType.toString() : '0',
    description: house.description || ''
  }
  showEditHouse.value = true
}

// 提交编辑房屋
const handleSubmitEditHouse = async () => {
  if (!editingHouse.value) return
  
  updatingHouse.value = true
  try {
    const updateData = {
      rentPrice: parseFloat(editHouseData.value.rentPrice),
      waterPrice: editHouseData.value.waterPrice ? parseFloat(editHouseData.value.waterPrice) : null,
      electricPrice: editHouseData.value.electricPrice ? parseFloat(editHouseData.value.electricPrice) : null,
      status: parseInt(editHouseData.value.status),
      roomArea: editHouseData.value.roomArea ? parseFloat(editHouseData.value.roomArea) : null,
      floorInfo: editHouseData.value.floorInfo,
      orientation: editHouseData.value.orientation,
      decoration: parseInt(editHouseData.value.decoration),
      hasElevator: parseInt(editHouseData.value.hasElevator),
      rentalType: parseInt(editHouseData.value.rentalType),
      description: editHouseData.value.description
    }

    const response = await axios.put(`${API_BASE_URL}/landlord/house/${editingHouse.value.id}`, updateData)
    
    if (response.data.success) {
      alert('房屋信息更新成功！')
      showEditHouse.value = false
      // 重新加载房屋列表
      emit('refresh')
    } else {
      alert(`更新失败: ${response.data.message}`)
    }
  } catch (error) {
    console.error('更新房屋信息失败:', error)
    alert('更新房屋信息失败，请稍后重试')
  } finally {
    updatingHouse.value = false
  }
}

// 关闭编辑模态框
const closeEditHouse = () => {
  showEditHouse.value = false
  editingHouse.value = null
  editHouseData.value = {
    rentPrice: null,
    waterPrice: null,
    electricPrice: null,
    status: '0',
    roomArea: null,
    floorInfo: '',
    orientation: '',
    decoration: '2',
    hasElevator: '0',
    rentalType: '0',
    description: ''
  }
}

// 新房屋数据
const newHouse = ref({
  province: '',
  city: '',
  district: '',
  street: '',
  communityName: '',
  buildingUnit: '',
  doorNumber: '',
  roomNumber: '',
  roomArea: null,
  floorInfo: '',
  orientation: '',
  decoration: '2', // 默认简装
  hasElevator: '0', // 默认无电梯
  rentPrice: null,
  waterPrice: 3.5, // 默认水费
  electricPrice: 0.8, // 默认电费
  rentalType: '0', // 默认整租
  status: '0', // 默认可租
  description: ''
})

// 处理提交添加房屋
const handleSubmitAddHouse = () => {
  emit('submitAddHouse', newHouse.value)
  // 重置表单
  resetNewHouseForm()
  showAddHouse.value = false
}

// 重置新房屋表单
const resetNewHouseForm = () => {
  newHouse.value = {
    province: '',
    city: '',
    district: '',
    street: '',
    communityName: '',
    buildingUnit: '',
    doorNumber: '',
    roomNumber: '',
    roomArea: null,
    floorInfo: '',
    orientation: '',
    decoration: '2',
    hasElevator: '0',
    rentPrice: null,
    waterPrice: 3.5,
    electricPrice: 0.8,
    rentalType: '0',
    status: '0',
    description: ''
  }
}

// 获取房屋完整地址
const getHouseFullAddress = (house) => {
  const parts = [
    house.province,
    house.city,
    house.district,
    house.street,
    house.communityName,
    house.buildingUnit,
    house.doorNumber,
    house.roomNumber
  ].filter(part => part && part.trim() !== '')
  
  return parts.join(' ')
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '可租',
    1: '已租',
    2: '下架',
    3: '预租'
  }
  return statusMap[status] || '未知'
}

// 获取状态样式类
const getStatusClass = (status) => {
  const classMap = {
    0: 'status-available',
    1: 'status-rented',
    2: 'status-offline',
    3: 'status-pre-rent'
  }
  return classMap[status] || 'status-unknown'
}

// 获取房屋状态样式类
const getHouseStatusClass = (status) => {
  const classMap = {
    0: 'house-available',
    1: 'house-rented',
    2: 'house-offline',
    3: 'house-pre-rent'
  }
  return classMap[status] || 'house-unknown'
}

// 获取装修程度文本
const getDecorationText = (decoration) => {
  const decorationMap = {
    1: '毛坯',
    2: '简装',
    3: '精装',
    4: '豪装'
  }
  return decorationMap[decoration] || '--'
}

// 获取租赁类型文本
const getRentalTypeText = (rentalType) => {
  const typeMap = {
    0: '整租',
    1: '合租',
    2: '单间'
  }
  return typeMap[rentalType] || '未知'
}

// 获取状态按钮文本
const getStatusBtnText = (status) => {
  const textMap = {
    0: '设为已租',
    1: '设为可租',
    2: '设为可租',
    3: '设为可租'
  }
  return textMap[status] || '操作'
}

// 获取状态按钮样式类
const getStatusBtnClass = (status) => {
  const classMap = {
    0: 'btn-offline', // 红色用于下架操作
    1: 'btn-available', // 绿色用于上架操作
    2: 'btn-available',
    3: 'btn-available'
  }
  return classMap[status] || 'btn-default'
}

// 切换房屋状态
const toggleHouseStatus = async (house) => {
  if (!house.id) return
  
  const currentStatus = house.status
  let newStatus
  
  // 根据当前状态确定新状态
  switch (currentStatus) {
    case 0: // 可租 -> 已租 (管理员逻辑：设为已租)
      newStatus = 1
      break
    case 1: // 已租 -> 可租
      newStatus = 0
      break
    case 2: // 下架 -> 可租
      newStatus = 0
      break
    case 3: // 预租 -> 可租
      newStatus = 0
      break
    default:
      return
  }
  
  try {
    const response = await axios.put(`${API_BASE_URL}/landlord/house/${house.id}`, {
      status: newStatus
    })
    
    if (response.data.success) {
      alert('房屋状态更新成功！')
      // 重新加载房屋列表
      emit('refresh')
    } else {
      alert(`更新失败: ${response.data.message}`)
    }
  } catch (error) {
    console.error('更新房屋状态失败:', error)
    alert('更新房屋状态失败，请稍后重试')
  }
}

// VR 相关逻辑
const showVrDialogVisible = ref(false)
const vrScenes = ref([])
const currentVrHouse = ref(null)
const newSceneName = ref('')
const selectedFile = ref(null)
const uploadingVr = ref(false)

const openVrDialog = async (house) => {
  currentVrHouse.value = house
  showVrDialogVisible.value = true
  await loadVrScenes(house.id)
}

const closeVrDialog = () => {
  showVrDialogVisible.value = false
  currentVrHouse.value = null
  vrScenes.value = []
  newSceneName.value = ''
  selectedFile.value = null
}

const loadVrScenes = async (roomId) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/vr-scenes/${roomId}`)
    if (response.data.success) {
       vrScenes.value = response.data.data
    }
  } catch (error) {
    console.error('加载场景失败:', error)
  }
}

const handleVrFileSelect = (event) => {
  selectedFile.value = event.target.files[0]
}

const uploadVrScene = async () => {
  if (!currentVrHouse.value || !newSceneName.value || !selectedFile.value) return
  
  uploadingVr.value = true
  const formData = new FormData()
  formData.append('roomId', currentVrHouse.value.id)
  formData.append('sceneName', newSceneName.value)
  formData.append('file', selectedFile.value)
  try {
    const response = await axios.post(`${API_BASE_URL}/vr-scenes/upload`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    if (response.data.success) {
      alert('上传成功')
      newSceneName.value = ''
      selectedFile.value = null
      const fileInput = document.querySelector('.vr-file-input')
      if (fileInput) fileInput.value = ''
      
      await loadVrScenes(currentVrHouse.value.id)
    } else {
       alert('上传失败: ' + response.data.message)
    }
  } catch (error) {
     console.error('上传失败:', error)
    alert('上传失败')
  } finally {
    uploadingVr.value = false
  }
}

const deleteVrScene = async (id) => {
  if (!confirm('确定删除该场景吗？')) return
  
  try {
    const response = await axios.delete(`${API_BASE_URL}/vr-scenes/${id}`)
    if (response.data.success) {
      await loadVrScenes(currentVrHouse.value.id)
    } else {
      alert('删除失败')
    }
  } catch (error) {
    console.error('删除失败:', error)
  }
}

// 打开房屋详情
const openHouseDetail = async (house) => {
  showHouseDetail.value = true
  loadingHouseDetail.value = true
  houseDetailError.value = ''
  houseDetailData.value = null
  
  try {
    const response = await axios.get(`${API_BASE_URL}/landlord/house/${house.id}`)
    
    if (response.data.success) {
      houseDetailData.value = response.data.data
    } else {
      houseDetailError.value = response.data.message || '获取房屋详情失败'
    }
  } catch (error) {
    console.error('获取房屋详情失败:', error)
    houseDetailError.value = '获取房屋详情失败，请稍后重试'
  } finally {
    loadingHouseDetail.value = false
  }
}

// 关闭房屋详情
const closeHouseDetail = () => {
  showHouseDetail.value = false
  houseDetailData.value = null
  houseDetailError.value = ''
}

// 租约状态文本映射
const getContractStatusText = (status) => {
  const statusMap = {
    0: '待签约',
    1: '已签约',
    2: '履行中',
    3: '已到期',
    4: '提前解约',
    5: '已退租'
  }
  return statusMap[status] || '未知'
}

// 付款周期文本映射
const getPaymentCycleText = (cycle) => {
  const cycleMap = {
    1: '月付',
    2: '季付',
    3: '年付'
  }
  return cycleMap[cycle] || '未知'
}

// 租金状态文本映射
const getRentStatusText = (status) => {
  const statusMap = {
    0: '未付款',
    1: '已付款',
    2: '逾期',
    3: '部分付款'
  }
  return statusMap[status] || '未知'
}

// 押金状态文本映射
const getDepositStatusText = (status) => {
  const statusMap = {
    0: '未付',
    1: '已付',
    2: '已退',
    3: '抵扣中'
  }
  return statusMap[status] || '未知'
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '--'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.my-houses {
  width: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding: 1.25rem 1.5rem;
  background-color: #1e3a5f;
  border-radius: 8px;
}

.page-header h2 {
  margin: 0;
  color: white;
  font-size: 1.5rem;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.rental-type-filter select {
  padding: 6px 10px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 4px;
  font-size: 13px;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  cursor: pointer;
  outline: none;
}

.rental-type-filter select option {
  background: white;
  color: #333;
}

/* 状态筛选标签 */
.status-filter-tabs {
  display: flex;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 6px;
  padding: 3px;
  gap: 2px;
}

.filter-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border: none;
  background: transparent;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #5a6c7d;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.filter-tab:hover {
  background-color: #f5f7fa;
  color: #1e3a5f;
}

.filter-tab.active {
  background-color: #1e3a5f;
  color: white;
}

.filter-tab svg {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
}

.filter-tab .count {
  background: rgba(0, 0, 0, 0.08);
  padding: 1px 5px;
  border-radius: 8px;
  font-size: 10px;
  font-weight: 600;
}

.filter-tab.active .count {
  background: rgba(255, 255, 255, 0.2);
}

/* 状态颜色 */
.filter-tab.status-0.active { background-color: #27ae60; }
.filter-tab.status-1.active { background-color: #1e3a5f; }
.filter-tab.status-2.active { background-color: #7f8c8d; }
.filter-tab.status-3.active { background-color: #e67e22; }

.filter-tab.status-0:not(.active):hover { color: #27ae60; }
.filter-tab.status-1:not(.active):hover { color: #1e3a5f; }
.filter-tab.status-2:not(.active):hover { color: #7f8c8d; }
.filter-tab.status-3:not(.active):hover { color: #e67e22; }

.search-wrapper {
  margin-right: 0.75rem;
}

.container {
  position: relative;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 6px;
  padding: 0;
  display: grid;
  place-content: center;
  z-index: 0;
  max-width: 240px;
  width: 240px;
}

.search-container {
  position: relative;
  width: 100%;
  border-radius: 6px;
  background-color: white;
  padding: 0;
  display: flex;
  align-items: center;
  border: 1px solid #e9ecef;
}

.search-container::after, .search-container::before {
  display: none;
}

.input {
  padding: 8px 12px;
  width: calc(100% - 40px);
  background: transparent;
  border: none;
  color: #2c3e50;
  font-size: 13px;
  border-radius: 6px;
}

.input::placeholder {
  color: #95a5a6;
}

.input:focus {
  outline: none;
}

.search__icon {
  width: 16px;
  aspect-ratio: 1;
  border: none;
  padding: 0;
  margin-right: 10px;
}

.search__icon path {
  fill: #7f8c8d;
}

.refresh-btn, .add-btn {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 2px;
  cursor: pointer;
  font-weight: 500;
  font-size: 13px;
  transition: background-color 0.2s ease;
}

.refresh-btn {
  background-color: #1e3a5f;
  color: white;
}

.refresh-btn:hover {
  background-color: #2d5a87;
}

.add-btn {
  background-color: #27ae60;
  color: white;
}

.add-btn:hover {
  background-color: #219a52;
}

/* 房屋列表样式 (管理员风格 - 个人优化) */
.house-list {
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 0; /* 方形圆角 */
}

/* 搜索无结果提示 */
.search-no-results {
  padding: 30px;
  text-align: center;
  color: #666;
  font-size: 14px;
  border-bottom: 1px solid #e5e5e5;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.clear-search-btn {
  color: #1e3a5f;
  background: none;
  border: none;
  cursor: pointer;
  text-decoration: underline;
  padding: 0;
  font-size: 13px;
}

.house-row {
  display: flex;
  align-items: center;
  padding: 8px 16px; /* 紧凑 */
  border-bottom: 1px solid #e5e5e5;
  font-size: 13px;
  transition: background-color 0.2s;
}

.house-row:last-child {
  border-bottom: none;
}

.house-row:hover {
  background: #f9f9f9;
}

.row-address {
  flex: 0 0 280px;
  padding-right: 16px;
  border-right: 1px solid #e5e5e5;
}

.addr-main {
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
  font-size: 14px;
  line-height: 1.4;
}

.addr-sub {
  font-size: 12px;
  color: #888;
}

.row-info {
  flex: 1;
  padding: 0 16px;
  color: #666;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px 16px; /* 统一间距 */
}

.info-label {
  color: #999;
  margin-right: 2px;
}

.info-val {
  color: #333;
  font-weight: 500;
}

.row-price {
  flex: 0 0 140px;
  padding: 0 16px;
  text-align: right;
  border-left: 1px solid #e5e5e5;
}

.price-main {
  font-weight: 600;
  color: #c00;
  font-size: 15px;
  margin-bottom: 4px;
}

.price-sub {
  font-size: 11px;
  color: #999;
  line-height: 1.4;
}

.row-status {
  flex: 0 0 80px;
  padding: 0 16px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
}

.status-tag {
  display: inline-block;
  padding: 2px 6px; /* 紧凑 */
  font-size: 12px;
  border-radius: 0; /* 方形圆角 */
  white-space: nowrap;
}

/* 状态标签颜色 */
.status-available { background: #d4edda; color: #155724; }
.status-rented { background: #f8d7da; color: #721c24; }
.status-offline { background: #e2e3e5; color: #383d41; }
.status-pre-rent { background: #fff3cd; color: #856404; }
.status-unknown { background: #e2e3e5; color: #383d41; }

.row-actions {
  flex: 0 0 auto;
  padding-left: 16px;
  display: flex;
  gap: 8px; /* 统一间距 */
}

.row-actions button {
  padding: 4px 10px;
  border: 1px solid #ddd;
  background: #fff;
  color: #333;
  font-size: 12px;
  border-radius: 0; /* 方形圆角 */
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.row-actions button:hover {
  background: #f5f5f5;
  border-color: #ccc;
  color: #000;
}

/* 特定按钮样式覆盖 */
.row-actions button.btn-available {
  background-color: #27ae60;
  color: white;
  border-color: #27ae60;
}
.row-actions button.btn-available:hover {
  background-color: #219a52;
  border-color: #219a52;
}

.row-actions button.btn-offline {
  background-color: #dc3545;
  color: white;
  border-color: #dc3545;
}
.row-actions button.btn-offline:hover {
  background-color: #c82333;
  border-color: #bd2130;
}

.row-actions button.btn-online {
  background-color: #1e3a5f;
  color: white;
  border-color: #1e3a5f;
}
.row-actions button.btn-online:hover {
  background-color: #2d5a87;
  border-color: #2d5a87;
}

.loading {
  padding: 40px;
  text-align: center;
  color: #999;
}

.no-houses {
  padding: 40px;
  text-align: center;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.empty-icon {
  font-size: 48px;
  opacity: 0.5;
}

.empty-state h3 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.empty-state p {
  margin: 0;
  color: #666;
}

.add-first-btn {
  margin-top: 10px;
  padding: 8px 20px;
  background-color: #27ae60;
  color: white;
  border: none;
  border-radius: 0; /* 方形圆角 */
  cursor: pointer;
  font-weight: 500;
}

.add-first-btn:hover {
  background-color: #219a52;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}

.modal-content {
  background-color: white;
  border-radius: 0; /* 方形圆角 */
  width: 100%;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: none; /* 无阴影 */
  border: 1px solid #333; /* 模态框边框加强 */
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #ddd;
  background-color: #f9f9f9;
  border-radius: 0; /* 方形圆角 */
}

.modal-header h3 {
  margin: 0;
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.close-btn {
  background: transparent;
  border: 1px solid #ddd;
  border-radius: 0; /* 方形圆角 */
  width: 24px;
  height: 24px;
  font-size: 14px;
  cursor: pointer;
  color: #888;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background-color: #f5f5f5;
  color: #333;
}

.modal-body {
  padding: 16px;
}

.modal-footer {
  padding: 16px;
  border-top: 1px solid #ddd;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  background-color: transparent;
}

.cancel-btn, .confirm-btn {
  padding: 6px 16px;
  border: 1px solid #ddd;
  cursor: pointer;
  font-weight: 400;
  font-size: 12px;
  background: transparent;
  border-radius: 0; /* 方形圆角 */
  transition: background-color 0.2s ease;
}

.cancel-btn {
  color: #333;
}

.cancel-btn:hover {
  background-color: #f5f5f5;
}

.confirm-btn {
  background-color: #1e3a5f;
  color: white;
  border-color: #1e3a5f;
}

.confirm-btn:hover {
  background-color: #2d5a87;
  border-color: #2d5a87;
}

.confirm-btn:disabled {
  background-color: #bdc3c7;
  cursor: not-allowed;
  border-color: #bdc3c7;
}

/* 表单样式 */
.add-house-form {
  width: 100%;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-group label {
  margin-bottom: 4px;
  font-weight: 500;
  color: #333;
  font-size: 12px;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 8px;
  border: 1px solid #ddd;
  font-size: 13px;
  background: #fff;
  border-radius: 0; /* 方形圆角 */
  transition: border-color 0.3s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #3A6EA5;
  box-shadow: none;
}

.form-group textarea {
  resize: vertical;
  min-height: 80px;
}

/* VR对话框样式 */
.vr-dialog {
  max-width: 700px;
}

.scene-list-manage {
  margin-bottom: 2rem;
}

.scene-list-manage h4,
.upload-section h4 {
  font-size: 0.9rem;
  color: #333;
  margin-bottom: 1rem;
  font-weight: 600;
  border-left: 3px solid #1e3a5f;
  padding-left: 8px;
}

.scenes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 16px;
}

.scene-card {
  border: 1px solid #eee;
  border-radius: 0; /* 方形圆角 */
  overflow: hidden;
  background: #f9f9f9;
}

.scene-preview {
  height: 100px;
  background: #000;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.scene-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.scene-info {
  padding: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #eee;
}

.scene-info span {
  font-size: 12px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 90px;
}

.delete-icon {
  background: none;
  border: none;
  cursor: pointer;
  padding: 2px 4px;
  font-size: 14px;
  opacity: 0.6;
  transition: opacity 0.2s;
}

.delete-icon:hover {
  opacity: 1;
}

.no-scenes {
  text-align: center;
  padding: 2rem;
  color: #999;
  font-size: 0.9rem;
  background: #fcfcfc;
  border: 1px dashed #ddd;
}

.upload-form {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.scene-name-input {
  padding: 6px 10px;
  border: 1px solid #ddd;
  font-size: 13px;
  flex: 1;
  border-radius: 0; /* 方形圆角 */
}

.vr-file-input {
  font-size: 12px;
  flex: 1;
}

.upload-btn {
  padding: 6px 16px;
  background: #1e3a5f;
  color: white;
  border: none;
  border-radius: 0; /* 方形圆角 */
  cursor: pointer;
  font-size: 13px;
}

.upload-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .houses-table-content {
    display: block;
    overflow-x: auto;
  }
  
  .house-actions {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .modal-content {
    margin: 1rem;
  }
  
  .modal-body {
    padding: 1rem;
  }
  
  .modal-footer {
    flex-direction: column;
  }
}
</style>
