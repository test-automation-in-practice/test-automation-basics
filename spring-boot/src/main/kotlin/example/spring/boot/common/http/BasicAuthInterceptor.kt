package example.spring.boot.common.http

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response
import org.springframework.http.HttpHeaders.AUTHORIZATION

class BasicAuthInterceptor(username: String, password: String) : Interceptor {

    private val basicAuthHeaderValue: String = Credentials.basic(username, password)

    override fun intercept(chain: Chain): Response {
        val request = chain.request().newBuilder()
            .removeHeader(AUTHORIZATION)
            .addHeader(AUTHORIZATION, basicAuthHeaderValue)
            .build()
        return chain.proceed(request)
    }

}
