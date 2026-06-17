/**
 * KiriFlow - Neo-Brutalist Mobile UI Logic
 */

// Initial State / Memory Store
let appState = {
  tasks: [
    { id: 1, title: "Configure Vercel Deployments", priority: "High", completed: true },
    { id: 2, title: "Debug Android release keystore signing", priority: "High", completed: false },
    { id: 3, title: "Optimize DoseFlow compositions", priority: "Medium", completed: false },
    { id: 4, title: "Integrate native notification dialogs", priority: "Low", completed: true },
    { id: 5, title: "Review pull requests for KiriCode v1.2", priority: "Medium", completed: false }
  ],
  currentTab: 'dashboard',
  taskFilter: 'all',
  chartRange: 'week',
  notificationsCount: 2,
  notifications: [
    { title: "Database warning", desc: "CPU utilization spike at 08:32 AM" },
    { title: "Build success", desc: "Android bundle KiriCode v1.0.1 generated successfully" }
  ]
};

// DOM Elements & Event Listeners
document.addEventListener("DOMContentLoaded", () => {
  renderTasks();
  updateStats();
  setupFilterCounts();
  updateNotificationBadge();
  initChartData();
});

// Tab Navigation Logic
function goToTab(tabId) {
  // Update Tab States in UI
  document.querySelectorAll('.nav-item').forEach(item => item.classList.remove('active'));
  document.querySelectorAll('.content-section').forEach(section => section.classList.remove('active'));

  // Set active class
  const activeNav = document.getElementById(`nav-${tabId}`);
  const activeSection = document.getElementById(`sec-${tabId}`);
  
  if (activeNav) activeNav.classList.add('active');
  if (activeSection) activeSection.classList.add('active');

  appState.currentTab = tabId;
  
  // Smooth top-scroll reset for device mockup viewport
  const contentEl = document.getElementById('app-content');
  if (contentEl) {
    contentEl.scrollTop = 0;
  }
}

// Generate & Render Tasks List
function renderTasks() {
  const miniList = document.getElementById('mini-task-list');
  const fullList = document.getElementById('full-task-list');
  
  // Clear lists
  if (miniList) miniList.innerHTML = '';
  if (fullList) fullList.innerHTML = '';
  
  // Sort tasks: Active first, then completed. By priority High -> Low
  const priorityWeight = { 'High': 3, 'Medium': 2, 'Low': 1 };
  const sortedTasks = [...appState.tasks].sort((a, b) => {
    if (a.completed !== b.completed) {
      return a.completed ? 1 : -1;
    }
    return priorityWeight[b.priority] - priorityWeight[a.priority];
  });
  
  // 1. Render Mini Checklist on Dashboard (limit to 3 items)
  const dashboardTasks = sortedTasks.filter(t => !t.completed).slice(0, 3);
  if (dashboardTasks.length === 0) {
    if (miniList) {
      miniList.innerHTML = `
        <div class="card yellow-card text-center py-16">
          <p class="font-bold text-sm">All caught up! 🎉</p>
        </div>
      `;
    }
  } else {
    dashboardTasks.forEach(task => {
      const taskEl = createTaskDOM(task, 'mini');
      if (miniList) miniList.appendChild(taskEl);
    });
  }
  
  // 2. Render Full Tasks Tab with Filter options
  const filteredTasks = sortedTasks.filter(task => {
    if (appState.taskFilter === 'active') return !task.completed;
    if (appState.taskFilter === 'completed') return task.completed;
    return true; // 'all'
  });
  
  if (filteredTasks.length === 0) {
    if (fullList) {
      fullList.innerHTML = `
        <div class="card text-center py-24" style="border-style: dashed;">
          <p class="font-bold text-sm" style="color: #666;">No tasks matching filter.</p>
        </div>
      `;
    }
  } else {
    filteredTasks.forEach(task => {
      const taskEl = createTaskDOM(task, 'full');
      if (fullList) fullList.appendChild(taskEl);
    });
  }

  // Update counts
  setupFilterCounts();
}

// Create Task element DOM
function createTaskDOM(task, mode) {
  const item = document.createElement('div');
  item.className = `task-item ${task.completed ? 'completed' : ''}`;
  item.id = `task-el-${mode}-${task.id}`;
  
  // Badge Color Determination
  let priorityBadgeClass = 'badge-yellow';
  if (task.priority === 'High') priorityBadgeClass = 'badge-danger';
  if (task.priority === 'Low') priorityBadgeClass = 'badge-black';
  
  item.innerHTML = `
    <label class="task-checkbox-wrapper">
      <input type="checkbox" ${task.completed ? 'checked' : ''} onchange="toggleTaskStatus(${task.id})">
      <span class="checkmark"></span>
    </label>
    <div class="task-details">
      <span class="task-title-text">${escapeHtml(task.title)}</span>
      <div class="task-meta">
        <span class="badge ${priorityBadgeClass}">${task.priority}</span>
      </div>
    </div>
    ${mode === 'full' ? `
      <button class="task-delete-btn" onclick="deleteTask(${task.id})" aria-label="Delete Task">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="3 6 5 6 21 6"></polyline>
          <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
          <line x1="10" y1="11" x2="10" y2="17"></line>
          <line x1="14" y1="11" x2="14" y2="17"></line>
        </svg>
      </button>
    ` : ''}
  `;
  
  return item;
}

// Toggle Task state
function toggleTaskStatus(taskId) {
  const taskIndex = appState.tasks.findIndex(t => t.id === taskId);
  if (taskIndex !== -1) {
    appState.tasks[taskIndex].completed = !appState.tasks[taskIndex].completed;
    
    // Tactile Feedback Animation
    renderTasks();
    updateStats();
    
    const isDone = appState.tasks[taskIndex].completed;
    simulateAlert('success', `Task marked as ${isDone ? 'completed' : 'active'}!`);
  }
}

// Add New Task
function handleAddTask(e) {
  e.preventDefault();
  const inputEl = document.getElementById('task-title');
  const priorityEl = document.getElementById('task-priority');
  
  if (!inputEl || !inputEl.value.trim()) return;
  
  const newTask = {
    id: Date.now(),
    title: inputEl.value.trim(),
    priority: priorityEl.value,
    completed: false
  };
  
  appState.tasks.push(newTask);
  inputEl.value = '';
  priorityEl.value = 'Medium';
  
  renderTasks();
  updateStats();
  simulateAlert('success', 'New task added successfully!');
}

// Delete Task
function deleteTask(taskId) {
  appState.tasks = appState.tasks.filter(t => t.id !== taskId);
  renderTasks();
  updateStats();
  simulateAlert('warning', 'Task removed from backlog.');
}

// Filter Actions
function filterTasks(filterType) {
  appState.taskFilter = filterType;
  
  // Update UI active buttons
  document.querySelectorAll('.filter-tab').forEach(tab => {
    if (tab.getAttribute('data-filter') === filterType) {
      tab.classList.add('active');
    } else {
      tab.classList.remove('active');
    }
  });
  
  renderTasks();
}

// Calculate and Update stats
function updateStats() {
  const completedCount = appState.tasks.filter(t => t.completed).length;
  const totalCount = appState.tasks.length;
  const efficiency = totalCount > 0 ? Math.round((completedCount / totalCount) * 100) : 0;
  
  const completedValEl = document.getElementById('stat-completed-val');
  const efficiencyValEl = document.getElementById('stat-efficiency-val');
  
  if (completedValEl) completedValEl.textContent = completedCount;
  if (efficiencyValEl) efficiencyValEl.textContent = `${efficiency}%`;
}

// Setup and Update Count badges in Filter tab
function setupFilterCounts() {
  const totalCount = appState.tasks.length;
  const activeCount = appState.tasks.filter(t => !t.completed).length;
  const completedCount = totalCount - activeCount;
  
  const allEl = document.getElementById('count-all');
  const activeEl = document.getElementById('count-active');
  const completedEl = document.getElementById('count-completed');
  
  if (allEl) allEl.textContent = totalCount;
  if (activeEl) activeEl.textContent = activeCount;
  if (completedEl) completedEl.textContent = completedCount;
}

// Toast Alert System
function simulateAlert(type, message) {
  const alertContainer = document.getElementById('app-alerts');
  if (!alertContainer) return;
  
  const alert = document.createElement('div');
  alert.className = `alert-banner alert-${type}`;
  
  let alertIcon = `
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
      <circle cx="12" cy="12" r="10"></circle>
      <line x1="12" y1="8" x2="12" y2="12"></line>
      <line x1="12" y1="16" x2="12.01" y2="16"></line>
    </svg>
  `;
  
  if (type === 'success') {
    alertIcon = `
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
        <polyline points="22 4 12 14.01 9 11.01"></polyline>
      </svg>
    `;
  }
  
  alert.innerHTML = `
    ${alertIcon}
    <span>${escapeHtml(message)}</span>
    <button class="alert-banner-close" onclick="this.parentElement.remove()">&times;</button>
  `;
  
  alertContainer.appendChild(alert);
  
  // Auto dismiss alert after 3.5 seconds
  setTimeout(() => {
    alert.style.opacity = '0';
    alert.style.transform = 'translateX(20px)';
    alert.style.transition = 'opacity 0.2s ease, transform 0.2s ease';
    setTimeout(() => alert.remove(), 200);
  }, 3500);
}

// Notification Drawer Toggle
function toggleNotifications() {
  const badgeEl = document.querySelector('.notification-badge');
  
  // Simulate reading notifications
  if (appState.notificationsCount > 0) {
    appState.notificationsCount = 0;
    updateNotificationBadge();
    simulateAlert('success', 'Clear notifications inbox!');
  } else {
    appState.notificationsCount = 2;
    updateNotificationBadge();
    simulateAlert('success', 'New notifications simulated.');
  }
}

function updateNotificationBadge() {
  const badgeEl = document.querySelector('.notification-badge');
  if (!badgeEl) return;
  
  if (appState.notificationsCount > 0) {
    badgeEl.classList.add('active');
  } else {
    badgeEl.classList.remove('active');
  }
}

// Save Profile configurations
function handleSaveSettings(e) {
  e.preventDefault();
  const nameEl = document.getElementById('profile-name');
  const emailEl = document.getElementById('profile-email');
  
  if (nameEl && emailEl) {
    simulateAlert('success', `Configuration saved for ${nameEl.value}!`);
  }
}

// Trigger Reset confirmation
function triggerResetConfirmation() {
  if (confirm("Are you sure you want to restore default application state?")) {
    appState.tasks = [
      { id: 1, title: "Configure Vercel Deployments", priority: "High", completed: true },
      { id: 2, title: "Debug Android release keystore signing", priority: "High", completed: false }
    ];
    renderTasks();
    updateStats();
    simulateAlert('warning', 'Default workspace configuration restored.');
  }
}

// SVG Interactive Chart Data updates
function toggleChartRange(range) {
  appState.chartRange = range;
  
  const weekBtn = document.getElementById('chart-toggle-week');
  const monthBtn = document.getElementById('chart-toggle-month');
  
  if (range === 'week') {
    if (weekBtn) weekBtn.classList.add('btn-yellow');
    if (monthBtn) monthBtn.classList.remove('btn-yellow');
    updateChartBars([90, 60, 110, 45, 80, 100]);
  } else {
    if (weekBtn) weekBtn.classList.remove('btn-yellow');
    if (monthBtn) monthBtn.classList.add('btn-yellow');
    updateChartBars([50, 95, 30, 115, 60, 85]);
  }
  
  simulateAlert('success', `Analytics switch: ${range.toUpperCase()} data`);
}

function initChartData() {
  updateChartBars([90, 60, 110, 45, 80, 100]);
}

function updateChartBars(heights) {
  const svg = document.getElementById('svg-chart');
  if (!svg) return;
  
  const bars = svg.querySelectorAll('.chart-bar');
  const barFills = svg.querySelectorAll('.chart-bar-fill');
  
  heights.forEach((height, i) => {
    if (bars[i] && barFills[i]) {
      const y = 130 - height;
      
      // Update thick outline rect
      bars[i].setAttribute('y', y);
      bars[i].setAttribute('height', height);
      
      // Update yellow fill inner rect
      barFills[i].setAttribute('y', y + 3);
      barFills[i].setAttribute('height', height - 6);
    }
  });
}

// Utility: Escape HTML inputs
function escapeHtml(str) {
  return str.replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
}
