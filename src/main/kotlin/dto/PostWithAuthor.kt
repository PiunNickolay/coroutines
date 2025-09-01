package org.example.dto

data class PostWithAuthor(
    val post: Post,
    val author: Author,
    val comments: List<CommentWithAuthor> = emptyList()
)
