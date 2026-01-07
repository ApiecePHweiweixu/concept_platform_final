/**
 * 格式化技术领域显示
 * 将逗号分隔的字符串格式化显示
 * @param {string} techDomain - 技术领域字符串（逗号分隔）
 * @returns {string} 格式化后的字符串，如 "大数据与云计算,人工智能"
 */
export function formatTechDomain(techDomain) {
    if (!techDomain) return '未分类'

    // 如果是字符串，直接返回（已经是逗号分隔格式）
    if (typeof techDomain === 'string') {
        // 清理多余的空格
        return techDomain.trim()
    }

    return techDomain
}

