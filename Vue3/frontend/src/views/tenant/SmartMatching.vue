<template>
  <div class="smart-matching-container">
    <div class="header">
      <h1>智能匹配</h1>
      <p>根据您的偏好和预算，智能推荐最适合的房源</p>
    </div>
    
    <div class="matching-content">
            <div class="ghost-animation">
                <div id="ghost">
                    <div id="red">
                        <!-- 15x15网格的所有元素 -->
                        <div id="a1"></div><div id="a2"></div><div id="a3"></div><div id="a4"></div><div id="a5"></div><div id="a6"></div><div id="a7"></div><div id="a8"></div><div id="a9"></div><div id="a10"></div><div id="a11"></div><div id="a12"></div><div id="a13"></div><div id="a14"></div><div id="a15"></div>
                        <div id="b1"></div><div id="b2"></div><div id="b3"></div><div id="b4"></div><div id="b5"></div><div id="b6"></div><div id="b7"></div><div id="b8"></div><div id="b9"></div><div id="b10"></div><div id="b11"></div><div id="b12"></div><div id="b13"></div><div id="b14"></div><div id="b15"></div>
                        <div id="c1"></div><div id="c2"></div><div id="c3"></div><div id="c4"></div><div id="c5"></div><div id="c6"></div><div id="c7"></div><div id="c8"></div><div id="c9"></div><div id="c10"></div><div id="c11"></div><div id="c12"></div><div id="c13"></div><div id="c14"></div><div id="c15"></div>
                        <div id="d1"></div><div id="d2"></div><div id="d3"></div><div id="d4"></div><div id="d5"></div><div id="d6"></div><div id="d7"></div><div id="d8"></div><div id="d9"></div><div id="d10"></div><div id="d11"></div><div id="d12"></div><div id="d13"></div><div id="d14"></div><div id="d15"></div>
                        <div id="e1"></div><div id="e2"></div><div id="e3"></div><div id="e4"></div><div id="e5"></div><div id="e6"></div><div id="e7"></div><div id="e8"></div><div id="e9"></div><div id="e10"></div><div id="e11"></div><div id="e12"></div><div id="e13"></div><div id="e14"></div><div id="e15"></div>
                        <div id="f1"></div><div id="f2"></div><div id="f3"></div><div id="f4"></div><div id="f5"></div><div id="f6"></div><div id="f7"></div><div id="f8"></div><div id="f9"></div><div id="f10"></div><div id="f11"></div><div id="f12"></div><div id="f13"></div><div id="f14"></div><div id="f15"></div>
                        <div id="g1"></div><div id="g2"></div><div id="g3"></div><div id="g4"></div><div id="g5"></div><div id="g6"></div><div id="g7"></div><div id="g8"></div><div id="g9"></div><div id="g10"></div><div id="g11"></div><div id="g12"></div><div id="g13"></div><div id="g14"></div><div id="g15"></div>
                        <div id="h1"></div><div id="h2"></div><div id="h3"></div><div id="h4"></div><div id="h5"></div><div id="h6"></div><div id="h7"></div><div id="h8"></div><div id="h9"></div><div id="h10"></div><div id="h11"></div><div id="h12"></div><div id="h13"></div><div id="h14"></div><div id="h15"></div>
                        <div id="i1"></div><div id="i2"></div><div id="i3"></div><div id="i4"></div><div id="i5"></div><div id="i6"></div><div id="i7"></div><div id="i8"></div><div id="i9"></div><div id="i10"></div><div id="i11"></div><div id="i12"></div><div id="i13"></div><div id="i14"></div><div id="i15"></div>
                        <div id="j1"></div><div id="j2"></div><div id="j3"></div><div id="j4"></div><div id="j5"></div><div id="j6"></div><div id="j7"></div><div id="j8"></div><div id="j9"></div><div id="j10"></div><div id="j11"></div><div id="j12"></div><div id="j13"></div><div id="j14"></div><div id="j15"></div>
                        <div id="k1"></div><div id="k2"></div><div id="k3"></div><div id="k4"></div><div id="k5"></div><div id="k6"></div><div id="k7"></div><div id="k8"></div><div id="k9"></div><div id="k10"></div><div id="k11"></div><div id="k12"></div><div id="k13"></div><div id="k14"></div><div id="k15"></div>
                        <div id="l1"></div><div id="l2"></div><div id="l3"></div><div id="l4"></div><div id="l5"></div><div id="l6"></div><div id="l7"></div><div id="l8"></div><div id="l9"></div><div id="l10"></div><div id="l11"></div><div id="l12"></div><div id="l13"></div><div id="l14"></div><div id="l15"></div>
                        <div id="m1"></div><div id="m2"></div><div id="m3"></div><div id="m4"></div><div id="m5"></div><div id="m6"></div><div id="m7"></div><div id="m8"></div><div id="m9"></div><div id="m10"></div><div id="m11"></div><div id="m12"></div><div id="m13"></div><div id="m14"></div><div id="m15"></div>
                        <div id="n1"></div><div id="n2"></div><div id="n3"></div><div id="n4"></div><div id="n5"></div><div id="n6"></div><div id="n7"></div><div id="n8"></div><div id="n9"></div><div id="n10"></div><div id="n11"></div><div id="n12"></div><div id="n13"></div><div id="n14"></div><div id="n15"></div>
                        <div id="o1"></div><div id="o2"></div><div id="o3"></div><div id="o4"></div><div id="o5"></div><div id="o6"></div><div id="o7"></div><div id="o8"></div><div id="o9"></div><div id="o10"></div><div id="o11"></div><div id="o12"></div><div id="o13"></div><div id="o14"></div><div id="o15"></div>
                    </div>
                    <div id="shadow"></div>
                </div>
            </div>
      
      <div class="matching-info">
        <div class="loading-text">
          <p>正在为您智能匹配最佳房源...</p>
          <div class="progress-bar">
            <div class="progress"></div>
          </div>
        </div>
        
        <div class="action-buttons">
          <button class="btn-primary" @click="goBack">返回首页</button>
          <button class="btn-secondary" @click="viewResults">查看匹配结果</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SmartMatching',
  methods: {
    goBack() {
      this.$router.push('/');
    },
    viewResults() {
      // 这里可以跳转到匹配结果页面
      alert('匹配完成！这里可以跳转到匹配结果页面');
    }
  }
}
</script>

<style scoped>
 * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
        }
        
        body {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 20px;
        }
        
        .smart-matching-container {
            width: 100%;
            max-width: 1000px;
            background: rgba(255, 255, 255, 0.1);
            backdrop-filter: blur(10px);
            border-radius: 20px;
            padding: 30px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
        }
        
        .header {
            text-align: center;
            margin-bottom: 40px;
            color: white;
        }
        
        .header h1 {
            font-size: 2.5rem;
            margin-bottom: 10px;
            text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
        }
        
        .header p {
            font-size: 1.2rem;
            opacity: 0.9;
        }
        
        .matching-content {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 40px;
        }
        
        .ghost-animation {
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 20px;
            background: rgba(255, 255, 255, 0.1);
            border-radius: 15px;
        }
        
        .matching-info {
            text-align: center;
            color: white;
            width: 100%;
        }
        
        .loading-text {
            margin-bottom: 30px;
        }
        
        .loading-text p {
            font-size: 1.3rem;
            margin-bottom: 20px;
        }
        
        .progress-bar {
            width: 100%;
            height: 8px;
            background: rgba(255, 255, 255, 0.3);
            border-radius: 4px;
            overflow: hidden;
            margin: 0 auto;
        }
        
        .progress {
            width: 100%;
            height: 100%;
            background: linear-gradient(90deg, #ff6b6b, #feca57, #48dbfb, #ff9ff3);
            border-radius: 4px;
            animation: progressAnimation 2s ease-in-out infinite;
        }
        
        @keyframes progressAnimation {
            0% {
                transform: translateX(-100%);
            }
            100% {
                transform: translateX(100%);
            }
        }
        
        .action-buttons {
            display: flex;
            gap: 15px;
            justify-content: center;
            flex-wrap: wrap;
        }
        
        .btn-primary, .btn-secondary {
            padding: 12px 24px;
            border: none;
            border-radius: 25px;
            font-size: 1rem;
            cursor: pointer;
            transition: all 0.3s ease;
            font-weight: bold;
            min-width: 150px;
        }
        
        .btn-primary {
            background: #ff6b6b;
            color: white;
        }
        
        .btn-primary:hover {
            background: #ff5252;
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(255, 107, 107, 0.4);
        }
        
        .btn-secondary {
            background: rgba(255, 255, 255, 0.2);
            color: white;
            border: 2px solid rgba(255, 255, 255, 0.5);
        }
        
        .btn-secondary:hover {
            background: rgba(255, 255, 255, 0.3);
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(255, 255, 255, 0.2);
        }
        
        /* 幽灵容器 */
        #ghost {
            position: relative;
            scale: 0.8;
        }
        
        /* 幽灵主体 - 15x15网格 */
        #red {
            position: relative;
            width: 150px;
            height: 150px;
            display: grid;
            grid-template-columns: repeat(15, 1fr);
            grid-template-rows: repeat(15, 1fr);
            grid-column-gap: 0px;
            grid-row-gap: 0px;
            grid-template-areas:
                "a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13 a14 a15"
                "b1 b2 b3 b4 b5 b6 b7 b8 b9 b10 b11 b12 b13 b14 b15"
                "c1 c2 c3 c4 c5 c6 c7 c8 c9 c10 c11 c12 c13 c14 c15"
                "d1 d2 d3 d4 d5 d6 d7 d8 d9 d10 d11 d12 d13 d14 d15"
                "e1 e2 e3 e4 e5 e6 e7 e8 e9 e10 e11 e12 e13 e14 e15"
                "f1 f2 f3 f4 f5 f6 f7 f8 f9 f10 f11 f12 f13 f14 f15"
                "g1 g2 g3 g4 g5 g6 g7 g8 g9 g10 g11 g12 g13 g14 g15"
                "h1 h2 h3 h4 h5 h6 h7 h8 h9 h10 h11 h12 h13 h14 h15"
                "i1 i2 i3 i4 i5 i6 i7 i8 i9 i10 i11 i12 i13 i14 i15"
                "j1 j2 j3 j4 j5 j6 j7 j8 j9 j10 j11 j12 j13 j14 j15"
                "k1 k2 k3 k4 k5 k6 k7 k8 k9 k10 k11 k12 k13 k14 k15"
                "l1 l2 l3 l4 l5 l6 l7 l8 l9 l10 l11 l12 l13 l14 l15"
                "m1 m2 m3 m4 m5 m6 m7 m8 m9 m10 m11 m12 m13 m14 m15"
                "n1 n2 n3 n4 n5 n6 n7 n8 n9 n10 n11 n12 n13 n14 n15"
                "o1 o2 o3 o4 o5 o6 o7 o8 o9 o10 o11 o12 o13 o14 o15";
            animation: bodyBounce 0.5s infinite alternate;
        }

        /* 身体上下弹跳动画 */
        @keyframes bodyBounce {
            0% {
                transform: translateY(-20px);
            }
            50% {
                transform: translateY(-10px);
            }
            100% {
                transform: translateY(0px);
            }
        }
        
        /* 为每个网格元素设置grid-area和背景色 */
        #a1 { grid-area: a1; background-color: transparent;}
        #a2 { grid-area: a2; background-color: transparent;}
        #a3 { grid-area: a3; background-color: #205722; }
        #a4 { grid-area: a4; background-color: #205722; }
        #a5 { grid-area: a5; background-color: #205722; }
        #a6 { grid-area: a6; background-color: #205722; }
        #a7 { grid-area: a7; background-color: #205722; }
        #a8 { grid-area: a8; background-color: #205722; }
        #a9 { grid-area: a9; background-color: #205722; }
        #a10 { grid-area: a10; background-color: #205722; }
        #a11 { grid-area: a11; background-color: #205722; }
        #a12 { grid-area: a12; background-color: #205722; }
        #a13 { grid-area: a13; background-color: #205722;  }
        #a14 { grid-area: a14; background-color: #205722;  }
        #a15 { grid-area: a15; background-color: transparent; }
        
        #b1 { grid-area: b1; background-color: transparent; }
        #b2 { grid-area: b2; background-color: #205722; }
        #b3 { grid-area: b3; background-color: #205722;  }
        #b4 { grid-area: b4; background-color: #4b7c4d; }
        #b5 { grid-area: b5; background-color: #4b7c4d; }
        #b6 { grid-area: b6; background-color: #4b7c4d; }
        #b7 { grid-area: b7; background-color: #4b7c4d; }
        #b8 { grid-area: b8; background-color: #4CAF50;  }
        #b9 { grid-area: b9; background-color: #4CAF50;  }
        #b10 { grid-area: b10; background-color: #4CAF50;  }
        #b11 { grid-area: b11; background-color: #4CAF50;  }
        #b12 { grid-area: b12; background-color: #4CAF50;  }
        #b13 { grid-area: b13; background-color: #4CAF50;  }
        #b14 { grid-area: b14; background-color: #4CAF50;  }
        #b15 { grid-area: b15; background-color: #205722;  }
        
        #c1 { grid-area: c1; background-color: #205722; }
        #c2 { grid-area: c2; background-color: #d73131; }
        #c3 { grid-area: c3; background-color: #205722; }
        #c4 { grid-area: c4; background-color: #4b7c4d;  }
        #c5 { grid-area: c5; background-color: #4b7c4d;  }
        #c6 { grid-area: c6; background-color: #4b7c4d;  }
        #c7 { grid-area: c7; background-color: #4b7c4d;  }
        #c8 { grid-area: c8; background-color: #4b7c4d;  }
        #c9 { grid-area: c9; background-color: #4CAF50; }
        #c10 { grid-area: c10; background-color: #4CAF50; }
        #c11 { grid-area: c11; background-color: #4b7c4d;  }
        #c12 { grid-area: c12; background-color: #4b7c4d; }
        #c13 { grid-area: c13; background-color: #4b7c4d;  }
        #c14 { grid-area: c14; background-color: #4CAF50;  }
        #c15 { grid-area: c15; background-color: #205722; }
        
        #d1 { grid-area: d1; background-color: #205722; }
        #d2 { grid-area: d2; background-color: #d73131; }
        #d3 { grid-area: d3; background-color: #205722;}
        #d4 { grid-area: d4; background-color: #4b7c4d; }
        #d5 { grid-area: d5; background-color: transparent;  }
        #d6 { grid-area: d6; background-color: #205722;}
        #d7 { grid-area: d7; background-color: transparent;  }
        #d8 { grid-area: d8; background-color: #4b7c4d; }
        #d9 { grid-area: d9; background-color: #4CAF50; }
        #d10 { grid-area: d10; background-color: #205722; }
        #d11 { grid-area: d11; background-color: #4CAF50; }
        #d12 { grid-area: d12; background-color: #4CAF50; }
        #d13 { grid-area: d13; background-color: #4CAF50; }
        #d14 { grid-area: d14; background-color: #205722; }
        #d15 { grid-area: d15; background-color: #205722; }
        
        #e1 { grid-area: e1; background-color: #205722; }
        #e2 { grid-area: e2; background-color: #205722; }
        #e3 { grid-area: e3; background-color: #205722;}
        #e4 { grid-area: e4; background-color: #4CAF50; }
        #e5 { grid-area: e5; background-color: transparent;  }
        #e6 { grid-area: e6; background-color: #205722; }
        #e7 { grid-area: e7; background-color: transparent;  }
        #e8 { grid-area: e8; background-color: #4b7c4d; }
        #e9 { grid-area: e9; background-color: #4CAF50; }
        #e10 { grid-area: e10; background-color: #4CAF50; }
        #e11 { grid-area: e11; background-color: #4CAF50; }
        #e12 { grid-area: e12; background-color: #4CAF50; }
        #e13 { grid-area: e13; background-color: #4CAF50; }
        #e14 { grid-area: e14; background-color: #4CAF50;}
        #e15 { grid-area: e15; background-color: #205722; }
        
        #f1 { grid-area: f1; background-color: transparent; }
        #f2 { grid-area: f2; background-color: #205722; }
        #f3 { grid-area: f3; background-color: #d73131; }
        #f4 { grid-area: f4; background-color: #205722;}
        #f5 { grid-area: f5; background-color: transparent; }
        #f6 { grid-area: f6; background-color: transparent;}
        #f7 { grid-area: f7; background-color: transparent; }
        #f8 { grid-area: f8; background-color: #4b7c4d;}
        #f9 { grid-area: f9; background-color: #4CAF50; }
        #f10 { grid-area: f10; background-color: #4CAF50; }
        #f11 { grid-area: f11; background-color: #4CAF50; }
        #f12 { grid-area: f12; background-color: #4CAF50; }
        #f13 { grid-area: f13; background-color: #4CAF50; }
        #f14 { grid-area: f14; background-color: #4CAF50; }
        #f15 { grid-area: f15; background-color: #205722; }
        
        #g1 { grid-area: g1; background-color: transparent; }
        #g2 { grid-area: g2; background-color: #205722; }
        #g3 { grid-area: g3; background-color: #d73131; }
        #g4 { grid-area: g4; background-color: #205722;}
        #g5 { grid-area: g5; background-color: #4CAF50; }
        #g6 { grid-area: g6; background-color: #4CAF50; }
        #g7 { grid-area: g7; background-color: #4CAF50; }
        #g8 { grid-area: g8; background-color: #4b7c4d; }
        #g9 { grid-area: g9; background-color: #4CAF50; }
        #g10 { grid-area: g10; background-color: #4CAF50; }
        #g11 { grid-area: g11; background-color: #4CAF50; }
        #g12 { grid-area: g12; background-color: #4CAF50; }
        #g13 { grid-area: g13; background-color: #4CAF50; }
        #g14 { grid-area: g14; background-color: #4CAF50; }
        #g15 { grid-area: g15; background-color: #205722; }
        
        #h1 { grid-area: h1; background-color: transparent;}
        #h2 { grid-area: h2; background-color: #205722;}
        #h3 { grid-area: h3; background-color: #205722; }
        #h4 { grid-area: h4; background-color: #205722;}
        #h5 { grid-area: h5; background-color: #4CAF50; }
        #h6 { grid-area: h6; background-color: #4CAF50; }
        #h7 { grid-area: h7; background-color: #4CAF50; }
        #h8 { grid-area: h8; background-color: #4CAF50; }
        #h9 { grid-area: h9; background-color: #4CAF50; }
        #h10 { grid-area: h10; background-color: #4CAF50; }
        #h11 { grid-area: h11; background-color: #205722;  }
        #h12 { grid-area: h12; background-color: #205722; }
        #h13 { grid-area: h13; background-color: #205722; }
        #h14 { grid-area: h14; background-color: #205722; }
        #h15 { grid-area: h15; background-color: #205722; }
        
        #i1 { grid-area: i1; background-color: transparent; }
        #i2 { grid-area: i2; background-color: transparent; }
        #i3 { grid-area: i3; background-color: transparent; }
        #i4 { grid-area: i4; background-color: #205722; }
        #i5 { grid-area: i5; background-color: #205722; }
        #i6 { grid-area: i6; background-color: #4CAF50; }
        #i7 { grid-area: i7; background-color: #4CAF50; }
        #i8 { grid-area: i8; background-color: #4CAF50; }
        #i9 { grid-area: i9; background-color: #4CAF50; }
        #i10 { grid-area: i10; background-color: #4CAF50; }
        #i11 { grid-area: i11; background-color: #4CAF50; }
        #i12 { grid-area: i12; background-color: #205722;  }
        #i13 { grid-area: i13; background-color: transparent;  }
        #i14 { grid-area: i14; background-color: transparent;  }
        #i15 { grid-area: i15; background-color: transparent;  }
        
        #j1 { grid-area: j1; background-color: transparent; }
        #j2 { grid-area: j2; background-color: transparent; }
        #j3 { grid-area: j3; background-color: transparent; }
        #j4 { grid-area: j4; background-color: transparent; }
        #j5 { grid-area: j5; background-color: #205722; }
        #j6 { grid-area: j6; background-color: #4CAF50; }
        #j7 { grid-area: j7; background-color: #c1d55a; }
        #j8 { grid-area: j8; background-color: #c1d55a; } 
        #j9 { grid-area: j9; background-color: #c1d55a;} 
        #j10 { grid-area: j10; background-color: #c1d55a; } 
        #j11 { grid-area: j11; background-color: #c1d55a; } 
        #j12 { grid-area: j12; background-color: #205722; }
        #j13 { grid-area: j13; background-color: transparent;  }
        #j14 { grid-area: j14; background-color: transparent;  }
        #j15 { grid-area: j15; background-color: transparent;  }
        
        #k1 { grid-area: k1; background-color: transparent; }
        #k2 { grid-area: k2; background-color: transparent; }
        #k3 { grid-area: k3; background-color: #205722; }
        #k4 { grid-area: k4; background-color: #205722; }
        #k5 { grid-area: k5; background-color: #205722; }
        #k6 { grid-area: k6; background-color: #4CAF50; }
        #k7 { grid-area: k7; background-color: #c1d55a; }
        #k8 { grid-area: k8; background-color: #c1d55a; } 
        #k9 { grid-area: k9; background-color: #c1d55a;}
        #k10 { grid-area: k10; background-color: #c1d55a; } 
        #k11 { grid-area: k11; background-color: #c1d55a; } 
        #k12 { grid-area: k12; background-color: #205722;  }
        #k13 { grid-area: k13; background-color: transparent; }
        #k14 { grid-area: k14; background-color: transparent; }
        #k15 { grid-area: k15; background-color: transparent; }
        
        #l1 { grid-area: l1; background-color: transparent; }
        #l2 { grid-area: l2; background-color: transparent; }
        #l3 { grid-area: l3; background-color: #205722; }
        #l4 { grid-area: l4; background-color: #4CAF50; }
        #l5 { grid-area: l5; background-color: #4CAF50; }
        #l6 { grid-area: l6; background-color: #4CAF50; }
        #l7 { grid-area: l7; background-color: #c1d55a; }
        #l8 { grid-area: l8; background-color: #c1d55a; } 
        #l9 { grid-area: l9; background-color: #c1d55a;}
        #l10 { grid-area: l10; background-color: #c1d55a; } 
        #l11 { grid-area: l11; background-color: #c1d55a; } 
        #l12 { grid-area: l12; background-color: #205722;  }
        #l13 { grid-area: l13; background-color: transparent;  }
        #l14 { grid-area: l14; background-color: transparent;  }
        #l15 { grid-area: l15; background-color: transparent;  }
        
        #m1 { grid-area: m1; background-color: transparent; }
        #m2 { grid-area: m2; background-color: transparent; }
        #m3 { grid-area: m3; background-color: #205722; }
        #m4 { grid-area: m4; background-color: #205722;}
        #m5 { grid-area: m5; background-color: #205722; }
        #m6 { grid-area: m6; background-color: #4CAF50; }
        #m7 { grid-area: m7; background-color: #4CAF50;}
        #m8 { grid-area: m8; background-color: #205722;}
        #m9 { grid-area: m9; background-color: #205722;}
        #m10 { grid-area: m10; background-color:  #4CAF50; }
        #m11 { grid-area: m11; background-color: #4CAF50; }
        #m12 { grid-area: m12; background-color: #205722; }
        #m13 { grid-area: m13; background-color:  transparent;}
        #m14 { grid-area: m14; background-color: transparent;   }
        #m15 { grid-area: m15; background-color: transparent;   }
        
        #n1 { grid-area: n1; background-color: transparent; }
        #n2 { grid-area: n2; background-color: transparent; }
        #n3 { grid-area: n3; background-color: transparent; }
        #n4 { grid-area: n4; background-color: transparent; }
        #n5 { grid-area: n5; background-color: #205722; }
        #n6 { grid-area: n6; background-color: #4CAF50; }
        #n7 { grid-area: n7; background-color: #205722; }
        #n8 { grid-area: n8; background-color: transparent;   }
        #n9 { grid-area: n9; background-color: #205722;  }
        #n10 { grid-area: n10; background-color:#4CAF50;   }
        #n11 { grid-area: n11; background-color:#205722;   }
        #n12 { grid-area: n12; background-color:transparent;   }
        #n13 { grid-area: n13; background-color:transparent; }
        #n14 { grid-area: n14; background-color: transparent;   }
        #n15 { grid-area: n15; background-color: transparent;   }
        
        #o1 { grid-area: o1; background-color: transparent; }
        #o2 { grid-area: o2; background-color: transparent; }
        #o3 { grid-area: o3; background-color: transparent; }
        #o4 { grid-area: o4; background-color: transparent; }
        #o5 { grid-area: o5; background-color: #205722; }
        #o6 { grid-area: o6; background-color: #205722; }
        #o7 { grid-area: o7; background-color: #205722; }
        #o8 { grid-area: o8; background-color: transparent;   }
        #o9 { grid-area: o9; background-color: #205722;   }
        #o10 { grid-area: o10; background-color: #205722;  }
        #o11 { grid-area: o11; background-color: #205722;  }
        #o12 { grid-area: o12; background-color: transparent;  }
        #o13 { grid-area: o13; background-color: transparent;  }
        #o14 { grid-area: o14; background-color: transparent;   }
        #o15 { grid-area: o15; background-color: transparent;   }
        
        /* 腿部动画关键帧 - 实现像素移动（保持原有颜色） */
        @keyframes legWalk {
            0% {
                /* 初始状态 */
                transform: translate(0, 0);
            }
            25% {
                /* 开始移动 */
                transform: translate(-1px, -1px);
            }
            50% {
                /* 移动到位 */
                transform: translate(-6.67px, -6.67px);
            }
            75% {
                /* 准备返回 */
                transform: translate(-1px, -1px);
            }
            100% {
                /* 返回原位 */
                transform: translate(0, 0);
            }
        }
        
        /* 为需要移动的像素添加动画（保持原有颜色） */
        #n5, #n6, #n7, #n9, #n10, #n11,
        #o5, #o6, #o7, #o9, #o10, #o11 {
            animation: legWalk 1s infinite;
        }

        /* 为特定像素添加颜色变化动画（不移动） */
        @keyframes colorChangeM5 {
            0%, 25% {
                background-color: #205722; /* 深绿色 */
            }
            50%,75%, 100% {
                background-color: #4CAF50; /* 浅绿色 */
            }
        }

        @keyframes colorChangeM7 {
            0%,25% {
                background-color: #4CAF50; /* 浅绿色 */
            }
            50%,75%, 100% {
                background-color: #205722; /* 深绿色 */
            }
        }

        @keyframes colorChangeM9 {
            0%, 25% {
                background-color: #205722; /* 深绿色 */
            }
            50%,75%, 100% {
                background-color: #4CAF50; /* 浅绿色 */
            }
        }

        @keyframes colorChangeM11 {
            0%, 25% {
                background-color: #4CAF50; /* 浅绿色 */
            }
            50%,75%, 100% {
                background-color: #205722; /* 深绿色 */
            }
        }

        /* 应用颜色变化动画到特定像素（不移动） */
        #m5 {
            animation: colorChangeM5 1s infinite;
        }
        #m7 {
            animation: colorChangeM7 1s infinite;
        }
        #m9 {
            animation: colorChangeM9 1s infinite;
        }
        #m11 {
            animation: colorChangeM11 1s infinite;
        }

        /* 阴影效果 */
        #shadow {
            background-color: rgba(0, 0, 0, 0.3);
            width: 150px;
            height: 20px;
            position: absolute;
            border-radius: 50%;
            filter: blur(10px);
            top: 80%;
            left: 0;
            animation: shadowMovement 0.5s infinite alternate;
        }
        
        @keyframes shadowMovement {
            0%, 100% {
                transform: scale(1);
                opacity: 0.5;
            }
            50% {
                transform: scale(0.9);
                opacity: 0.3;
            }
        }
        
        /* 响应式设计 */
        @media (max-width: 768px) {
            .smart-matching-container {
                padding: 20px;
            }
            
            .header h1 {
                font-size: 2rem;
            }
            
            .header p {
                font-size: 1rem;
            }
            
            .action-buttons {
                flex-direction: column;
                align-items: center;
            }
            
            .btn-primary, .btn-secondary {
                width: 100%;
                max-width: 300px;
            }
        }
    </style>
