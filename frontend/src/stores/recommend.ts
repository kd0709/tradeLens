import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getRecommendProducts } from '@/api/product'

export const useRecommendStore = defineStore('recommend', () => {
  const recommendList = ref<any[]>([])
  const lastFetchTime = ref<number>(0)
  const loading = ref(false)

  const fetchRecommendations = async (force: boolean = false) => {
    const now = Date.now()
    
    // 如果不强制刷新，且缓存未超过 5 分钟，直接使用缓存
    if (!force && recommendList.value.length > 0 && (now - lastFetchTime.value < 300000)) {
      console.log('命中个性化推荐缓存')
      return
    }

    loading.value = true
    try {
      const res: any = await getRecommendProducts(10)

      console.log('获取推荐商品成功', res)

      recommendList.value = res || []
      lastFetchTime.value = now
    } catch (error) {
      console.error('获取推荐商品失败', error)
    } finally {
      loading.value = false
    }
  }

  const clearCache = () => {
    recommendList.value = []
    lastFetchTime.value = 0
  }

  return { recommendList, loading, lastFetchTime, fetchRecommendations, clearCache }
}, {
  // 开启持久化
  persist: {
    key: 'tradelens_recommend_cache',
    paths: ['recommendList', 'lastFetchTime']
  }
})