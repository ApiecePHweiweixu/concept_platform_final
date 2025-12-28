/**
 * 格式化技术领域显示
 * 将JSON数组字符串转换为逗号分隔的字符串
 * @param {string} techDomain - 技术领域JSON字符串或普通字符串
 * @returns {string} 格式化后的字符串，如 "大数据与云计算,人工智能"
 */
export function formatTechDomain(techDomain) {
    if (!techDomain) return '未分类'

    // 如果已经是逗号分隔的字符串，直接返回
    if (typeof techDomain === 'string' && !techDomain.startsWith('[') && !techDomain.startsWith('"')) {
        return techDomain
    }

    try {
        // 尝试解析JSON数组
        let parsed = techDomain
        // 如果字符串被双引号包裹，先去掉外层引号
        if (techDomain.startsWith('"') && techDomain.endsWith('"')) {
            parsed = JSON.parse(techDomain)
        } else {
            parsed = JSON.parse(techDomain)
        }

        if (Array.isArray(parsed)) {
            return parsed.join(',')
        }
        // 如果是单个字符串，直接返回
        if (typeof parsed === 'string') {
            return parsed
        }
    } catch (e) {
        // 如果不是JSON格式，尝试清理引号和括号
        let cleaned = techDomain
        // 移除开头的 [ 和结尾的 ]
        cleaned = cleaned.replace(/^\[/, '').replace(/\]$/, '')
        // 移除所有引号
        cleaned = cleaned.replace(/"/g, '')
        // 移除多余的空格
        cleaned = cleaned.replace(/\s+/g, ' ').trim()
        // 如果包含逗号，说明是多个值，直接返回清理后的字符串
        if (cleaned.includes(',')) {
            return cleaned
        }
        // 否则返回原字符串（兼容旧数据）
    }

    return techDomain
}

