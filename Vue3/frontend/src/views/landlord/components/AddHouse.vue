<template>
  <div class="add-house-page">
    <div class="page-header">
      <h2>添加房屋</h2>
      <div class="header-actions">
        <button @click="emit('cancel')" class="cancel-btn">取消</button>
      </div>
    </div>

    <!-- 房屋信息表单 -->
    <div class="form-container">
      <form @submit.prevent="submitForm" class="house-form">
        <!-- 地址信息 -->
        <div class="form-section">
          <h3>地址信息</h3>
          <div class="form-grid">
            <div class="form-group">
              <label for="province">省份 *</label>
              <input
                id="province"
                v-model="houseData.province"
                type="text"
                placeholder="请输入省份"
                required
              >
            </div>
            <div class="form-group">
              <label for="city">城市 *</label>
              <input
                id="city"
                v-model="houseData.city"
                type="text"
                placeholder="请输入城市"
                required
              >
            </div>
            <div class="form-group">
              <label for="district">区域 *</label>
              <input
                id="district"
                v-model="houseData.district"
                type="text"
                placeholder="请输入区域"
                required
              >
            </div>
            <div class="form-group">
              <label for="street">街道 *</label>
              <input
                id="street"
                v-model="houseData.street"
                type="text"
                placeholder="请输入街道"
                required
              >
            </div>
            <div class="form-group">
              <label for="communityName">小区名称 *</label>
              <input
                id="communityName"
                v-model="houseData.communityName"
                type="text"
                placeholder="请输入小区名称"
                required
              >
            </div>
            <div class="form-group">
              <label for="buildingUnit">楼栋单元</label>
              <input
                id="buildingUnit"
                v-model="houseData.buildingUnit"
                type="text"
                placeholder="例如：1栋2单元"
              >
            </div>
            <div class="form-group">
              <label for="doorNumber">门牌号</label>
              <input
                id="doorNumber"
                v-model="houseData.doorNumber"
                type="text"
                placeholder="例如：101室"
              >
            </div>
            <div class="form-group">
              <label for="roomNumber">房间号</label>
              <input
                id="roomNumber"
                v-model="houseData.roomNumber"
                type="text"
                placeholder="例如：A01"
              >
            </div>
          </div>
        </div>

        <!-- 房屋基本信息 -->
        <div class="form-section">
          <h3>房屋基本信息</h3>
          <div class="form-grid">
            <div class="form-group">
              <label for="roomArea">房屋面积 (㎡) *</label>
              <input
                id="roomArea"
                v-model.number="houseData.roomArea"
                type="number"
                step="0.1"
                placeholder="请输入房屋面积"
                required
              >
            </div>
            <div class="form-group">
              <label for="floorInfo">楼层信息</label>
              <input
                id="floorInfo"
                v-model="houseData.floorInfo"
                type="text"
                placeholder="例如：3/18层"
              >
            </div>
            <div class="form-group">
              <label for="orientation">朝向</label>
              <select id="orientation" v-model="houseData.orientation">
                <option value="">请选择朝向</option>
                <option value="南">南</option>
                <option value="北">北</option>
                <option value="东">东</option>
                <option value="西">西</option>
                <option value="南北">南北</option>
                <option value="东西">东西</option>
                <option value="东南">东南</option>
                <option value="西南">西南</option>
                <option value="东北">东北</option>
                <option value="西北">西北</option>
              </select>
            </div>
            <div class="form-group">
              <label for="decoration">装修程度 *</label>
              <select id="decoration" v-model="houseData.decoration" required>
                <option value="1">毛坯</option>
                <option value="2">简装</option>
                <option value="3">精装</option>
                <option value="4">豪装</option>
              </select>
            </div>
            <div class="form-group">
              <label for="hasElevator">是否有电梯 *</label>
              <select id="hasElevator" v-model="houseData.hasElevator" required>
                <option value="0">无电梯</option>
                <option value="1">有电梯</option>
              </select>
            </div>
          </div>
        </div>

        <!-- 租赁信息 -->
        <div class="form-section">
          <h3>租赁信息</h3>
          <div class="form-grid">
            <div class="form-group">
              <label for="rentPrice">月租金 (元) *</label>
              <input
                id="rentPrice"
                v-model.number="houseData.rentPrice"
                type="number"
                step="0.01"
                placeholder="请输入月租金"
                required
              >
            </div>
            <div class="form-group">
              <label for="waterPrice">水费 (元/吨)</label>
              <input
                id="waterPrice"
                v-model.number="houseData.waterPrice"
                type="number"
                step="0.01"
                placeholder="默认3.5"
              >
            </div>
            <div class="form-group">
              <label for="electricPrice">电费 (元/度)</label>
              <input
                id="electricPrice"
                v-model.number="houseData.electricPrice"
                type="number"
                step="0.01"
                placeholder="默认0.8"
              >
            </div>
            <div class="form-group">
              <label for="rentalType">租赁类型 *</label>
              <select id="rentalType" v-model="houseData.rentalType" required>
                <option value="0">整租</option>
                <option value="1">合租</option>
                <option value="2">单间</option>
              </select>
            </div>
            <div class="form-group">
              <label for="status">房屋状态 *</label>
              <select id="status" v-model="houseData.status" required>
                <option value="0">可租</option>
                <option value="1">已租</option>
                <option value="2">下架</option>
                <option value="3">预租</option>
              </select>
            </div>
          </div>
        </div>

        <!-- 房屋描述 -->
        <div class="form-section">
          <h3>房屋描述</h3>
          <div class="form-group full-width">
            <label for="description">房屋描述</label>
            <textarea
              id="description"
              v-model="houseData.description"
              placeholder="请输入房屋描述，例如：周边环境、交通便利性、配套设施等"
              rows="4"
            ></textarea>
          </div>
        </div>

        <!-- 表单操作 -->
        <div class="form-actions">
          <button type="button" @click="emit('cancel')" class="cancel-btn">取消</button>
          <button type="submit" :disabled="submitting" class="submit-btn">
            <span v-if="submitting">提交中...</span>
            <span v-else>添加房屋</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const emit = defineEmits(['submit', 'cancel'])

// 表单提交状态
const submitting = ref(false)

// 房屋数据
const houseData = reactive({
  // 地址信息
  province: '',
  city: '',
  district: '',
  street: '',
  communityName: '',
  buildingUnit: '',
  doorNumber: '',
  roomNumber: '',
  
  // 房屋基本信息
  roomArea: null,
  floorInfo: '',
  orientation: '',
  decoration: '2', // 默认简装
  hasElevator: '0', // 默认无电梯
  
  // 租赁信息
  rentPrice: null,
  waterPrice: 3.5, // 默认水费
  electricPrice: 0.8, // 默认电费
  rentalType: '0', // 默认整租
  status: '0', // 默认可租
  
  // 描述
  description: ''
})

// 提交表单
const submitForm = async () => {
  if (submitting.value) return
  
  // 基础验证
  if (!houseData.province || !houseData.city || !houseData.district || !houseData.street || 
      !houseData.communityName || !houseData.roomArea || !houseData.rentPrice) {
    alert('请填写必填字段')
    return
  }

  submitting.value = true
  
  try {
    // 准备提交数据
    const submitData = {
      ...houseData,
      roomArea: parseFloat(houseData.roomArea),
      rentPrice: parseFloat(houseData.rentPrice),
      waterPrice: houseData.waterPrice ? parseFloat(houseData.waterPrice) : null,
      electricPrice: houseData.electricPrice ? parseFloat(houseData.electricPrice) : null,
      decoration: parseInt(houseData.decoration),
      hasElevator: parseInt(houseData.hasElevator),
      rentalType: parseInt(houseData.rentalType),
      status: parseInt(houseData.status)
    }

    // 触发提交事件
    emit('submit', submitData)
    
  } catch (error) {
    console.error('表单提交错误:', error)
    alert('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(houseData, {
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
  })
}

// 暴露重置方法
defineExpose({ resetForm })
</script>

<style scoped>
.add-house-page {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px; /* Compact margin */
  padding: 12px 16px; /* Compact padding */
  background-color: #1e3a5f;
  border-radius: 0; /* Square */
}

.page-header h2 {
  margin: 0;
  color: white;
  font-size: 1.4rem; /* Slightly smaller */
  font-weight: 500;
}

.header-actions {
  display: flex;
  gap: 8px; /* Compact gap */
  align-items: center;
}

/* Common Button Styles */
.cancel-btn,
.submit-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 6px 12px; /* Compact button */
  border: none;
  border-radius: 0; /* Square */
  font-weight: 500;
  font-size: 13px;
  cursor: pointer;
  transition: background-color 0.2s ease;
  min-width: 80px; /* Reduced width */
}

.cancel-btn {
  background-color: #c0392b; /* Slightly darker red */
  color: white;
}

.cancel-btn:hover {
  background-color: #a93226;
}

.submit-btn {
  background-color: #27ae60;
  color: white;
}

.submit-btn:hover:not(:disabled) {
  background-color: #219a52;
}

.submit-btn:disabled {
  background-color: #bdc3c7;
  cursor: not-allowed;
  opacity: 0.7;
}

.form-container {
  background: white;
  border-radius: 0; /* Square */
  box-shadow: none; /* Flat */
  border: 1px solid #ddd;
}

.house-form {
  padding: 16px; /* Compact padding */
}

.form-section {
  margin-bottom: 16px; /* Compact margin */
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}

.form-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.form-section h3 {
  margin: 0 0 12px 0; /* Compact margin */
  color: #333;
  font-size: 1rem; /* Smaller font */
  font-weight: 600;
  padding-left: 8px;
  border-left: 3px solid #1e3a5f; /* Industrial Blue Accent */
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 8px 16px; /* Compact Gap: 8px vertical, 16px horizontal */
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-group label {
  margin-bottom: 4px; /* Compact */
  color: #555;
  font-weight: 500;
  font-size: 12px; /* Smaller label */
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 8px; /* Compact input */
  border: 1px solid #ccc;
  border-radius: 0; /* Square */
  font-size: 13px; /* Smaller text */
  transition: all 0.2s;
  background: white;
  color: #333;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #1e3a5f; /* Sharp Focus */
  box-shadow: none; /* No Glow */
}

.form-group input::placeholder,
.form-group textarea::placeholder {
  color: #999;
  font-size: 12px;
}

.form-group textarea {
  resize: vertical;
  min-height: 60px; /* Reduced min-height */
  font-family: inherit;
}

/* 必填字段标记 */
.form-group label:after {
  content: ' *';
  color: #e74c3c;
}

/* 表单操作按钮 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px; /* Compact margin */
  padding-top: 16px;
  border-top: 1px solid #eee;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .house-form {
    padding: 12px;
  }
  
  .form-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .cancel-btn,
  .submit-btn {
    width: 100%;
  }
}
</style>
