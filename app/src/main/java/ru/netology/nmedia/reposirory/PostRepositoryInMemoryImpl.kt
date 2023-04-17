package ru.netology.nmedia.reposirory

import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post

class PostRepositoryInMemoryImpl : PostRepository {

    private var nextId: Long = 1

    private var posts = listOf(
        Post(
            id = nextId++,
            author = "Нетология. Университет интернет-профессий",
            content = "Знаний зватит на всех: на следующей неделе разбираемся с...",
            published = "18 сентября в 10:12",
            likedByMe = false
        ),
        Post(
            id = nextId++,
            author = "Нетология. Университет интернет-профессий",
            content = "Привет! Это новая нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия - помочь встать на путь роста и начать цепочку перемен",
            published = "21 мая в 18:36",
            likedByMe = false
        )
    )

    private var data = MutableLiveData(posts)

    override fun getAll() = data

    override fun likeById(id: Long) {
        posts = posts.map {
            if (it.id != id) it else {
                if (it.likedByMe) {
                    it.copy(likedByMe = !it.likedByMe, like = it.like - 1)
                } else {
                    it.copy(likedByMe = !it.likedByMe, like = it.like + 1)
                }
            }
        }
        data.value = posts
    }

    override fun shareById(id: Long) {
        posts = posts.map {
            if (it.id != id) it else it.copy(share = it.share + 1)
        }
        data.value = posts
    }

    override fun removeById(id: Long) {
        posts = posts.filter { it.id != id }
        data.value = posts
    }

    override fun save(post: Post) {
        if (post.id == 0L) {
            posts = listOf(
                post.copy(
                    id = nextId++,//todo создать переменную ид
                    author = "Me",
                    likedByMe = false,
                    published = "now"
                )
            ) + posts
            data.value = posts
            return
        }

        posts = posts.map {
            if (it.id != post.id) it else it.copy(content = post.content)
        }
        data.value = posts
    }
}