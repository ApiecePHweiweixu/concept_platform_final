import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue')
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue')
    },
    {
      path: '/',
      component: () => import('../layout/MainLayout.vue'),
      children: [
        {
          path: '', // 默认首页跳转逻辑
          redirect: '/my-projects'
        },
        {
          path: 'my-projects',
          name: 'my-projects',
          component: () => import('../views/MyProjects.vue')
        },
        {
          path: 'my-incubation-projects',
          name: 'my-incubation-projects',
          component: () => import('../views/MyIncubationProjects.vue')
        },
        {
          path: 'project/add',
          name: 'project-add',
          component: () => import('../views/ProjectFormView.vue')
        },
        {
          path: 'project/form',
          name: 'project-form',
          component: () => import('../views/ProjectFormView.vue')
        },
        {
          path: 'audit-projects',
          name: 'audit-projects',
          component: () => import('../views/AuditProjects.vue')
        },
        {
          path: 'incubation-manage',
          name: 'incubation-manage',
          component: () => import('../views/IncubationManageView.vue')
        },
        {
          path: 'expert-reviews',
          name: 'expert-reviews',
          component: () => import('../views/ExpertReviewView.vue')
        },
        {
          path: 'success-cases',
          name: 'success-cases',
          component: () => import('../views/SuccessCaseView.vue')
        },
        {
          path: 'mentor-projects',
          name: 'mentor-projects',
          component: () => import('../views/MentorProjectsView.vue')
        }
      ]
    }
  ],
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const publicPages = ['/login', '/register']; // 添加 /register 到白名单
  const authRequired = !publicPages.includes(to.path);
  const loggedIn = localStorage.getItem('user');

  if (authRequired && !loggedIn) {
    next('/login');
  } else {
    next();
  }
});

export default router
