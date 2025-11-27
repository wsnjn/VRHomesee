// 漫游看房示例数据 - 每个场景使用对应的全景图
export const houseTourData = [
  {
    scene: {
      photo_key: "1",
      correction: [0, 0, 0],
      rotation: [180, 0, 0],
      title: "客厅",
      sphereSource: {
        thumb: "/models/客厅.jpg",
        slices: []
      },
      cubeSource: {
        thumb: "/models/客厅.jpg",
        slices: []
      },
      is_main: 1
    },
    overlays: [
      {
        title: "卧室",
        x: 4.6720072719141,
        y: -0.52291666726088,
        next_photo_key: "2"
      },
      {
        title: "厨房",
        x: 4.6720072719141,
        y: 0.52291666726088,
        next_photo_key: "3"
      }
    ]
  },
  {
    scene: {
      photo_key: "2",
      correction: [0, 0, 0],
      rotation: [180, 0, 0],
      title: "卧室",
      sphereSource: {
        thumb: "/models/卧室.jpg",
        slices: []
      },
      cubeSource: {
        thumb: "/models/卧室.jpg",
        slices: []
      },
      is_main: 0
    },
    overlays: [
      {
        title: "客厅",
        x: 3.4483749837697,
        y: -0.7169249148447,
        next_photo_key: "1"
      }
    ]
  },
  {
    scene: {
      photo_key: "3",
      correction: [0, 0, 0],
      rotation: [180, 0, 0],
      title: "厨房",
      sphereSource: {
        thumb: "/models/厨房.jpg",
        slices: []
      },
      cubeSource: {
        thumb: "/models/厨房.jpg",
        slices: []
      },
      is_main: 0
    },
    overlays: [
      {
        title: "客厅",
        x: 1.4483749837697,
        y: -0.3169249148447,
        next_photo_key: "1"
      }
    ]
  }
];

// 简化版数据，使用assets文件夹下的对应全景图
export const simpleHouseTourData = [
  {
    scene: {
      photo_key: "1",
      correction: [0, 0, 0],
      rotation: [180, 0, 0],
      title: "客厅",
      sphereSource: {
        thumb: "/models/客厅.jpg",
        slices: []
      },
      cubeSource: {
        thumb: "/models/客厅.jpg",
        slices: []
      },
      is_main: 1
    },
    overlays: [
      {
        title: "卧室",
        x: 4.6720072719141,
        y: -0.52291666726088,
        next_photo_key: "2"
      }
    ]
  },
  {
    scene: {
      photo_key: "2",
      correction: [0, 0, 0],
      rotation: [180, 0, 0],
      title: "卧室",
      sphereSource: {
        thumb: "/models/卧室.jpg",
        slices: []
      },
      cubeSource: {
        thumb: "/models/卧室.jpg",
        slices: []
      },
      is_main: 0
    },
    overlays: [
      {
        title: "客厅",
        x: 3.4483749837697,
        y: -0.7169249148447,
        next_photo_key: "1"
      }
    ]
  }
];
