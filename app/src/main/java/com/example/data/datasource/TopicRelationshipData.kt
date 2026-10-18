package com.example.data.datasource

import com.example.data.model.TopicEntity

/**
 * Curated biblical relationship mappings between topics.
 * Designed to provide meaningful, theological, and devotional connections
 * without duplicate topics or circular clutter.
 */
object TopicRelationshipData {

    private val topicConnections: Map<String, List<String>> = mapOf(
        "faith" to listOf("hope", "prayer", "wisdom", "love", "obedience"),
        "prayer" to listOf("worship", "praise", "fasting", "faith", "holy_spirit"),
        "love" to listOf("grace", "forgiveness", "christian_living", "family"),
        "forgiveness" to listOf("grace", "love", "repentance", "salvation"),
        "salvation" to listOf("grace", "repentance", "eternal_life", "baptism"),
        "grace" to listOf("salvation", "love", "forgiveness", "faith"),
        "holy_spirit" to listOf("spiritual_growth", "prayer", "discipleship", "obedience"),
        "wisdom" to listOf("discipleship", "leadership", "work", "spiritual_growth"),
        "fear" to listOf("peace", "hope", "faith", "anxiety"),
        "anxiety" to listOf("peace", "prayer", "faith", "hope"),
        "peace" to listOf("anxiety", "fear", "hope", "prayer"),
        "hope" to listOf("faith", "eternal_life", "second_coming", "heaven"),
        "healing" to listOf("faith", "prayer", "grace", "hope"),
        "repentance" to listOf("salvation", "forgiveness", "sin", "baptism"),
        "baptism" to listOf("salvation", "obedience", "holy_spirit", "discipleship"),
        "worship" to listOf("praise", "prayer", "holy_spirit", "obedience"),
        "fasting" to listOf("prayer", "spiritual_growth", "repentance"),
        "obedience" to listOf("faith", "christian_living", "discipleship"),
        "temptation" to listOf("sin", "holy_spirit", "spiritual_growth", "prayer"),
        "sin" to listOf("repentance", "forgiveness", "grace", "salvation", "hell"),
        "eternal_life" to listOf("salvation", "heaven", "hope", "second_coming"),
        "second_coming" to listOf("hope", "eternal_life", "heaven", "discipleship"),
        "heaven" to listOf("eternal_life", "hope", "second_coming", "worship"),
        "hell" to listOf("sin", "repentance", "salvation", "eternal_life"),
        "family" to listOf("marriage", "parenting", "love", "christian_living"),
        "marriage" to listOf("family", "love", "parenting", "faith"),
        "parenting" to listOf("family", "marriage", "wisdom", "christian_living"),
        "money" to listOf("work", "faith", "wisdom", "christian_living"),
        "work" to listOf("money", "leadership", "christian_living", "wisdom"),
        "leadership" to listOf("wisdom", "discipleship", "work", "obedience"),
        "spiritual_growth" to listOf("discipleship", "prayer", "holy_spirit", "faith"),
        "discipleship" to listOf("spiritual_growth", "leadership", "obedience", "christian_living"),
        "christian_living" to listOf("discipleship", "love", "obedience", "faith"),
        "praise" to listOf("worship", "prayer", "hope", "peace")
    )

    fun getRelatedTopics(currentTopicId: String, allTopics: List<TopicEntity>): List<TopicEntity> {
        val topicMap = allTopics.associateBy { it.id }
        val mappedIds = topicConnections[currentTopicId] ?: emptyList()

        val results = mutableListOf<TopicEntity>()
        val seen = mutableSetOf<String>()
        seen.add(currentTopicId)

        // 1. Add direct curated relationships first
        for (relId in mappedIds) {
            val found = topicMap[relId]
            if (found != null && !seen.contains(found.id)) {
                results.add(found)
                seen.add(found.id)
            }
        }

        // 2. If fewer than 4, find topics in the same category
        val currentTopic = topicMap[currentTopicId]
        if (currentTopic != null && results.size < 4) {
            val sameCategory = allTopics.filter {
                !seen.contains(it.id) && (it.category == currentTopic.category || it.categoryTa == currentTopic.categoryTa)
            }
            for (t in sameCategory) {
                if (results.size >= 5) break
                results.add(t)
                seen.add(t.id)
            }
        }

        return results
    }
}
