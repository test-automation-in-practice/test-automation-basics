package example.spring.boot.domains.books.business

sealed interface BookFailure
object BookNotFound : BookFailure
object BookUpdateFailed : BookFailure
