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
  margin-bottom: 2rem;
  padding: 1.5rem;
  background: linear-gradient(135deg, rgb(179, 208, 253) 0%, rgb(164, 202, 248) 100%);
  border-radius: 16px;
  box-shadow: 0 8px 25px rgba(79, 156, 232, 0.3);
}

.page-header h2 {
  margin: 0;
  color: white;
  font-size: 2rem;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-actions {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.cancel-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(79, 156, 232, 0.3);
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, rgb(255, 107, 107) 0%, rgb(220, 53, 69) 100%);
  color: white;
}

.cancel-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(220, 53, 69, 0.4);
  background: linear-gradient(135deg, rgb(245, 97, 97) 0%, rgb(200, 35, 51) 100%);
}

.cancel-btn:active {
  transform: translateY(0);
}

.form-container {
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 25px rgba(79, 156, 232, 0.2);
  overflow: hidden;
  border: 1px solid rgba(179, 208, 253, 0.3);
}

.house-form {
  padding: 2rem;
}

.form-section {
  margin-bottom: 2.5rem;
  padding-bottom: 2rem;
  border-bottom: 1px solid rgba(179, 208, 253, 0.3);
}

.form-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.form-section h3 {
  margin: 0 0 1.5rem 0;
  color: #2c3e50;
  font-size: 1.5rem;
  font-weight: 700;
  padding-left: 0.5rem;
  border-left: 4px solid rgb(179, 208, 253);
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-group label {
  margin-bottom: 0.5rem;
  color: #2c3e50;
  font-weight: 600;
  font-size: 0.95rem;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 0.75rem 1rem;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s;
  background: white;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: rgb(179, 208, 253);
  box-shadow: 0 0 0 3px rgba(179, 208, 253, 0.2);
}

.form-group input::placeholder,
.form-group textarea::placeholder {
  color: #6c757d;
  opacity: 0.7;
}

.form-group textarea {
  resize: vertical;
  min-height: 100px;
  font-family: inherit;
}

/* 必填字段标记 */
.form-group label:after {
  content: ' *';
  color: #dc3545;
}

/* 表单操作按钮 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 1px solid rgba(179, 208, 253, 0.3);
}

.cancel-btn,
.submit-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.75rem 2rem;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(79, 156, 232, 0.3);
  position: relative;
  overflow: hidden;
  min-width: 120px;
}

.cancel-btn {
  background: linear-gradient(135deg, rgb(255, 107, 107) 0%, rgb(220, 53, 69) 100%);
  color: white;
}

.cancel-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(220, 53, 69, 0.4);
  background: linear-gradient(135deg, rgb(245, 97, 97) 0%, rgb(200, 35, 51) 100%);
}

.submit-btn {
  background: linear-gradient(135deg, rgb(102, 187, 106) 0%, rgb(76, 175, 80) 100%);
  color: white;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(76, 175, 80, 0.4);
  background: linear-gradient(135deg, rgb(92, 177, 96) 0%, rgb(66, 165, 70) 100%);
}

.submit-btn:disabled {
  background: linear-gradient(135deg, rgb(149, 185, 240) 0%, rgb(119, 162, 224) 100%);
  cursor: not-allowed;
  transform: none;
  opacity: 0.7;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .house-form {
    padding: 1.5rem;
  }
  
  .form-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
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
  
  .form-actions {
    flex-direction: column;
  }
  
  .cancel-btn,
  .submit-btn {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .house-form {
    padding: 1rem;
  }
  
  .form-section {
    margin-bottom: 2rem;
    padding-bottom: 1.5rem;
  }
  
  .form-section h3 {
    font-size: 1.3rem;
  }
  
  .page-header h2 {
    font-size: 1.6rem;
  }
}
</style>
