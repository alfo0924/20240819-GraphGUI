# Graph Algorithm Visualizer

主程式: <a href="
https://github.com/alfo0924/20240819-GraphGUI/blob/7d0510402cafdb77cccd6a02fa4e55d76464edc4/src/main/java/fcu/web/GraphGUI.java
" >GraphGUI </a>


這是一個用於可視化圖論算法的 Java 應用程序。它允許用戶創建隨機圖形，並執行多種圖論算法，包括最小生成樹 (MST)、關鍵節點查找和最短路徑計算。

## 功能
- 隨機圖形生成
- 最小生成樹 (MST) 計算
- 關鍵節點識別
- 最短路徑查找
- 圖形可視化

## 操作方法

### 啟動應用程序
1. 運行 `GraphGUI` 類的 `main` 方法來啟動應用程序。

### 創建圖形
1. 在 "Vertices" 文本框中輸入所需的頂點數。
2. 在 "Edges" 文本框中輸入所需的邊數。
3. 點擊 "Start" 按鈕生成隨機圖形。

### 計算最小生成樹 (MST)
1. 生成圖形後，點擊 "Calculate MST" 按鈕。
2. MST 的邊將以紅色顯示在圖形上。

### 查找關鍵節點
1. 生成圖形後，點擊 "Find Critical Nodes" 按鈕。
2. 關鍵節點將以紅色顯示在圖形上。

### 計算最短路徑
1. 在 "Start" 文本框中輸入起始頂點編號。
2. 在 "End" 文本框中輸入目標頂點編號。
3. 點擊 "Find Shortest Path" 按鈕。
4. 最短路徑將以藍色顯示在圖形上。

### 圖形顯示
- 頂點顯示為黑色方塊，內有白色數字標識。
- 邊顯示為連接頂點的線，線上標有權重。
- MST 的邊以紅色顯示。
- 關鍵節點以紅色顯示。
- 最短路徑以藍色顯示。

## 技術細節
- 使用 Java Swing 構建 GUI。
- 實現了 Kruskal 算法用於 MST 計算。
- 使用改進的 DFS 算法查找關鍵節點。
- 實現了 Dijkstra 算法用於最短路徑計算。
- 使用網格布局來可視化圖形。

## 注意事項
- 確保輸入的邊數不超過完全圖的最大邊數。
- 對於大型圖形，算法的執行可能需要一些時間。
- 圖形是無向的，每條邊都被視為雙向。
