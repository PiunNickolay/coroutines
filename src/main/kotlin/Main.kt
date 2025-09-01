package org.example
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.example.dto.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path


fun main() = runBlocking {
    val posts = api.getPosts()
    val postsWithAuthors = posts.map { post ->
        async {
            val authorDeferred = async { api.getAuthor(post.authorId) }
            val commentsDeferred = async {
                api.getComments(post.id).map { comment ->
                    async {
                        val commentAuthor = api.getAuthor(comment.authorId)
                        CommentWithAuthor(comment, commentAuthor)
                    }
                }.awaitAll()
            }

            PostWithAuthor(post, authorDeferred.await(), commentsDeferred.await())
        }
    }.awaitAll()

    println(postsWithAuthors)
}

interface ApiService {
    @GET("/api/posts")
    suspend fun getPosts(): List<Post>

    @GET("/api/posts/{id}/comments")
    suspend fun getComments(@Path("id") postId: Long): List<Comment>

    @GET("/api/authors/{id}")
    suspend fun getAuthor(@Path("id") authorId: Long): Author
}

val retrofit = Retrofit.Builder()
    .baseUrl("http://localhost:9999/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val api = retrofit.create(ApiService::class.java)