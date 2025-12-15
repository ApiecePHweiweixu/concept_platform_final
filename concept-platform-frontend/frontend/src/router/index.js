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
      path: '/',
      component: () => import('../layout/MainLayout.vue'),
      children: [
        {
          path: '', // 默认首页跳转逻辑，稍后可以优化，暂时跳到 my-projects
          redirect: '/my-projects' 
        },
        {
          path: 'my-projects',
          name: 'my-projects',
          component: () => import('../views/MyProjects.vue')
        },
        {
          path: 'project/add',
          name: 'project-add',
          component: () => import('../views/ProjectFormView.vue')
        },
        {
          path: 'audit-projects',
          name: 'audit-projects',
          component: () => import('../views/AuditProjects.vue')
        },
        {
          path: 'expert-reviews',
          name: 'expert-reviews',
          component: () => import('../views/ExpertReviewView.vue')
        }
      ]
    }
  ],
})

// 简单的路由守卫，如果未登录则跳转到登录页（可选，用户未明确要求但通常需要）
router.beforeEach((to, from, next) => {
  const publicPages = ['/login'];
  const authRequired = !publicPages.includes(to.path);
  const loggedIn = localStorage.getItem('user');

  if (authRequired && !loggedIn) {
    next('/login');
  } else {
    next();
  }
});

export default router
