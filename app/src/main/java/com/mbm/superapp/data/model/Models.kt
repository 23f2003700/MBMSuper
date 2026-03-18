package com.mbm.superapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Student(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val branch: String = "",
    val year: Int = 1,
    @SerialName("enrollment_no") val enrollmentNo: String = "",
    @SerialName("avatar_url") val avatarUrl: String = "",
    @SerialName("is_verified") val isVerified: Boolean = false,
    @SerialName("is_admin") val isAdmin: Boolean = false,
    @SerialName("created_at") val createdAt: String = "",
)

@Serializable
data class Issue(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val status: String = "open",
    @SerialName("created_by") val createdBy: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("upvotes") val upvotes: Int = 0,
    @SerialName("image_url") val imageUrl: String = "",
    @SerialName("author_name") val authorName: String = "",
)

@Serializable
data class ExchangePost(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val condition: String = "",
    @SerialName("image_urls") val imageUrls: List<String> = emptyList(),
    @SerialName("created_by") val createdBy: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("is_sold") val isSold: Boolean = false,
    @SerialName("author_name") val authorName: String = "",
)

@Serializable
data class FestEvent(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val time: String = "",
    val venue: String = "",
    val category: String = "",
    @SerialName("image_url") val imageUrl: String = "",
    @SerialName("registration_link") val registrationLink: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("registered_count") val registeredCount: Int = 0,
)

@Serializable
data class LibraryBook(
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val subject: String = "",
    val semester: Int = 0,
    val branch: String = "",
    @SerialName("pdf_url") val pdfUrl: String = "",
    @SerialName("thumbnail_url") val thumbnailUrl: String = "",
    @SerialName("uploaded_by") val uploadedBy: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("download_count") val downloadCount: Int = 0,
)

@Serializable
data class Trip(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val destination: String = "",
    val date: String = "",
    @SerialName("return_date") val returnDate: String = "",
    val budget: Double = 0.0,
    @SerialName("max_members") val maxMembers: Int = 0,
    @SerialName("current_members") val currentMembers: Int = 0,
    @SerialName("created_by") val createdBy: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("image_url") val imageUrl: String = "",
)

@Serializable
data class Alumni(
    val id: String = "",
    val name: String = "",
    val branch: String = "",
    @SerialName("graduation_year") val graduationYear: Int = 0,
    val company: String = "",
    val designation: String = "",
    @SerialName("linkedin_url") val linkedinUrl: String = "",
    @SerialName("avatar_url") val avatarUrl: String = "",
    val bio: String = "",
)

@Serializable
data class ChatRoom(
    val id: String = "",
    val name: String = "",
    @SerialName("is_group") val isGroup: Boolean = false,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("last_message") val lastMessage: String = "",
    @SerialName("last_message_at") val lastMessageAt: String = "",
    val participants: List<String> = emptyList(),
)

@Serializable
data class Message(
    val id: String = "",
    @SerialName("room_id") val roomId: String = "",
    @SerialName("sender_id") val senderId: String = "",
    @SerialName("sender_name") val senderName: String = "",
    val content: String = "",
    val type: String = "text",
    @SerialName("created_at") val createdAt: String = "",
)

@Serializable
data class Broadcast(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    @SerialName("created_by") val createdBy: String = "",
    @SerialName("created_at") val createdAt: String = "",
    val priority: String = "normal",
)
