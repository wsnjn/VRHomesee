<template>
  <div class="contract-template-wrapper">
    <!-- PDF 内容区域 -->
    <div ref="contractContent" class="contract-content">
      <div class="contract-header">
        <h1>房屋租赁合同</h1>
        <p class="contract-number">合同编号：{{ contract.contractNumber }}</p>
      </div>

      <div class="contract-parties">
        <div class="party">
          <h3>甲方（出租方）</h3>
          <table>
            <tr><td>姓名：</td><td>{{ landlord.realName || landlord.username }}</td></tr>
            <tr><td>电话：</td><td>{{ landlord.phone }}</td></tr>
            <tr><td>身份证号：</td><td>{{ landlord.idNumber || '' }}</td></tr>
          </table>
        </div>
        <div class="party">
          <h3>乙方（承租方）</h3>
          <table>
            <tr><td>姓名：</td><td>{{ tenant.realName || tenant.username }}</td></tr>
            <tr><td>电话：</td><td>{{ tenant.phone }}</td></tr>
            <tr><td>身份证号：</td><td>{{ tenant.idNumber || '' }}</td></tr>
          </table>
        </div>
      </div>

      <div class="contract-property">
        <h3>一、租赁房屋</h3>
        <p>甲方将位于 <strong>{{ propertyAddress }}</strong> 的房屋出租给乙方使用。</p>
        <table class="property-details">
          <tr>
            <td>房屋面积：</td><td>{{ room.roomArea }} ㎡</td>
            <td>户型：</td><td>{{ room.bedroomCount || 1 }}室{{ room.parlorCount || 1 }}厅</td>
          </tr>
          <tr>
            <td>装修情况：</td><td>{{ getDecorationText(room.decoration) }}</td>
            <td>租赁类型：</td><td>{{ getRentalTypeText(room.rentalType) }}</td>
          </tr>
        </table>
      </div>

      <div class="contract-terms">
        <h3>二、租赁期限</h3>
        <p>租赁期限自 <strong>{{ formatDate(contract.contractStartDate) }}</strong> 起至 <strong>{{ formatDate(contract.contractEndDate) }}</strong> 止。</p>

        <h3>三、租金及支付方式</h3>
        <table class="rent-details">
          <tr>
            <td>月租金：</td><td><strong>¥{{ contract.monthlyRent }}</strong> 元/月</td>
          </tr>
          <tr>
            <td>押金：</td><td>¥{{ contract.depositAmount }} 元</td>
          </tr>
          <tr>
            <td>付款周期：</td><td>{{ getPaymentCycleText(contract.paymentCycle) }}</td>
          </tr>
        </table>

        <h3>四、双方权利与义务</h3>
        <ol>
          <li>甲方应保证房屋设施完好，配合乙方正常使用。</li>
          <li>乙方应按时支付租金，爱护房屋设施。</li>
          <li>乙方不得擅自转租或改变房屋用途。</li>
          <li>租赁期满，乙方应按时交还房屋。</li>
        </ol>

        <h3>五、违约责任</h3>
        <p>任何一方违反本合同约定，应承担违约责任并赔偿对方损失。</p>

        <h3>六、其他约定</h3>
        <p>{{ contract.remarks || '无' }}</p>
      </div>

      <div class="contract-signatures">
        <div class="signature-block">
          <p>甲方签字：</p>
          <div class="signature-line"></div>
          <p>日期：</p>
          <div class="signature-line short"></div>
        </div>
        <div class="signature-block">
          <p>乙方签字：</p>
          <div class="signature-line"></div>
          <p>日期：</p>
          <div class="signature-line short"></div>
        </div>
      </div>

      <div class="contract-footer">
        <p>本合同一式两份，甲乙双方各执一份，具有同等法律效力。</p>
        <p class="generated-time">生成时间：{{ generatedTime }}</p>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="contract-actions">
      <button @click="downloadPDF" class="btn-download" :disabled="isGenerating">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
          <polyline points="7 10 12 15 17 10"/>
          <line x1="12" y1="15" x2="12" y2="3"/>
        </svg>
        {{ isGenerating ? '生成中...' : '下载 PDF' }}
      </button>
      <button @click="printContract" class="btn-print">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="6 9 6 2 18 2 18 9"/>
          <path d="M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2"/>
          <rect x="6" y="14" width="12" height="8"/>
        </svg>
        打印合同
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import html2pdf from 'html2pdf.js'

const props = defineProps({
  contract: {
    type: Object,
    required: true
  },
  room: {
    type: Object,
    default: () => ({})
  },
  landlord: {
    type: Object,
    default: () => ({})
  },
  tenant: {
    type: Object,
    default: () => ({})
  }
})

const contractContent = ref(null)
const isGenerating = ref(false)

const generatedTime = computed(() => {
  return new Date().toLocaleString('zh-CN')
})

const propertyAddress = computed(() => {
  const r = props.room
  return [r.city, r.district, r.street, r.communityName, r.buildingUnit, r.doorNumber].filter(Boolean).join('')
})

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
}

const getDecorationText = (val) => {
  const map = { 1: '毛坯', 2: '简装', 3: '精装', 4: '豪装' }
  return map[val] || '简装'
}

const getRentalTypeText = (val) => {
  const map = { 0: '整租', 1: '合租', 2: '单间' }
  return map[val] || '整租'
}

const getPaymentCycleText = (val) => {
  const map = { 1: '月付', 3: '季付', 6: '半年付', 12: '年付' }
  return map[val] || '月付'
}

const downloadPDF = async () => {
  if (!contractContent.value) return
  
  isGenerating.value = true
  
  try {
    const options = {
      margin: 10,
      filename: `租赁合同_${props.contract.contractNumber}.pdf`,
      image: { type: 'jpeg', quality: 0.98 },
      html2canvas: { scale: 2, useCORS: true },
      jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' }
    }
    
    await html2pdf().set(options).from(contractContent.value).save()
  } catch (error) {
    console.error('PDF生成失败:', error)
    alert('PDF生成失败，请重试')
  } finally {
    isGenerating.value = false
  }
}

const printContract = () => {
  window.print()
}
</script>

<style scoped>
.contract-template-wrapper {
  max-width: 800px;
  margin: 0 auto;
}

.contract-content {
  background: white;
  padding: 40px;
  font-family: 'SimSun', serif;
  font-size: 14px;
  line-height: 1.8;
  color: #333;
}

.contract-header {
  text-align: center;
  margin-bottom: 30px;
  border-bottom: 2px solid #333;
  padding-bottom: 20px;
}

.contract-header h1 {
  font-size: 24px;
  margin: 0 0 10px 0;
  letter-spacing: 4px;
}

.contract-number {
  color: #666;
  font-size: 13px;
}

.contract-parties {
  display: flex;
  gap: 40px;
  margin-bottom: 30px;
}

.party {
  flex: 1;
}

.party h3 {
  font-size: 14px;
  margin: 0 0 10px 0;
  padding-bottom: 5px;
  border-bottom: 1px solid #ddd;
}

.party table {
  width: 100%;
}

.party td {
  padding: 4px 0;
}

.party td:first-child {
  width: 80px;
  color: #666;
}

.contract-property h3,
.contract-terms h3 {
  font-size: 14px;
  margin: 20px 0 10px 0;
}

.property-details,
.rent-details {
  width: 100%;
  border-collapse: collapse;
  margin: 10px 0;
}

.property-details td,
.rent-details td {
  padding: 8px;
  border: 1px solid #ddd;
}

.property-details td:nth-child(odd),
.rent-details td:first-child {
  background: #f9f9f9;
  width: 100px;
}

.contract-terms ol {
  padding-left: 20px;
}

.contract-terms ol li {
  margin: 8px 0;
}

.contract-signatures {
  display: flex;
  justify-content: space-between;
  margin-top: 60px;
  padding-top: 30px;
}

.signature-block {
  width: 45%;
}

.signature-block p {
  margin: 10px 0 5px 0;
}

.signature-line {
  border-bottom: 1px solid #333;
  height: 30px;
}

.signature-line.short {
  width: 150px;
}

.contract-footer {
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px dashed #ccc;
  text-align: center;
  color: #666;
  font-size: 12px;
}

.generated-time {
  margin-top: 10px;
  color: #999;
}

.contract-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 0 0 8px 8px;
}

.btn-download,
.btn-print {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  font-size: 14px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-download {
  background: #3B82F6;
  color: white;
}

.btn-download:hover:not(:disabled) {
  background: #2563EB;
}

.btn-download:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-print {
  background: white;
  color: #333;
  border: 1px solid #ddd;
}

.btn-print:hover {
  background: #f9f9f9;
}

@media print {
  .contract-actions {
    display: none;
  }
  
  .contract-content {
    padding: 20px;
  }
}
</style>
