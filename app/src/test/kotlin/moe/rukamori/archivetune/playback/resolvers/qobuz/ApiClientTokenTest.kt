package moe.rukamori.archivetune.playback.resolvers.qobuz

import kotlinx.coroutines.runBlocking
import moe.rukamori.archivetune.lossless.LosslessTokens
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test

class ApiClientTokenTest {

    private fun createFakeOkHttpClient(onIntercept: (Request) -> Unit): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                onIntercept(chain.request())
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("{}".toResponseBody(null))
                    .build()
            }
            .build()
    }

    @Test
    fun `SquidApiClient uses provider value when not empty`() = runBlocking {
        var capturedCookie: String? = null
        val client = SquidApiClient(
            httpClient = createFakeOkHttpClient { req ->
                capturedCookie = req.header("Cookie")
            },
            captchaCookieProvider = { "user_cookie".takeIf { it.isNotEmpty() } ?: LosslessTokens.SQUID_CAPTCHA_COOKIE }
        )

        client.search("test")
        assertEquals("captcha_verified_at=user_cookie", capturedCookie)
    }

    @Test
    fun `SquidApiClient falls back to default when provider value is empty`() = runBlocking {
        var capturedCookie: String? = null
        val client = SquidApiClient(
            httpClient = createFakeOkHttpClient { req ->
                capturedCookie = req.header("Cookie")
            },
            captchaCookieProvider = { "".takeIf { it.isNotEmpty() } ?: LosslessTokens.SQUID_CAPTCHA_COOKIE }
        )

        client.search("test")
        assertEquals("captcha_verified_at=${LosslessTokens.SQUID_CAPTCHA_COOKIE}", capturedCookie)
    }

    @Test
    fun `ArcodApiClient uses provider values`() = runBlocking {
        var capturedStashKey: String? = null
        var capturedBearerToken: String? = null
        val client = ArcodApiClient(
            httpClient = createFakeOkHttpClient { req ->
                capturedStashKey = req.header("X-Stash-Key")
                capturedBearerToken = req.header("Authorization")
            },
            stashKeyProvider = { "user_stash_key".takeIf { it.isNotEmpty() } ?: LosslessTokens.ARCOD_STASH_KEY },
            bearerTokenProvider = { "user_bearer_token".takeIf { it.isNotEmpty() } ?: LosslessTokens.ARCOD_BEARER_TOKEN }
        )

        client.search("test")
        assertEquals("user_stash_key", capturedStashKey)
        assertEquals("Bearer user_bearer_token", capturedBearerToken)
    }

    @Test
    fun `QobuzApiClient uses provider values`() = runBlocking {
        var capturedAppId: String? = null
        var capturedUserAuthToken: String? = null
        val client = QobuzApiClient(
            httpClient = createFakeOkHttpClient { req ->
                capturedAppId = req.header("X-App-Id")
                capturedUserAuthToken = req.header("X-User-Auth-Token")
            },
            appIdProvider = { "user_app_id".takeIf { it.isNotEmpty() } ?: LosslessTokens.QOBUZ_APP_ID },
            appSecretProvider = { "user_app_secret".takeIf { it.isNotEmpty() } ?: LosslessTokens.QOBUZ_APP_SECRET },
            userAuthTokenProvider = { "user_auth_token".takeIf { it.isNotEmpty() } ?: LosslessTokens.QOBUZ_USER_AUTH_TOKEN }
        )

        client.search("test")
        assertEquals("user_app_id", capturedAppId)
        assertEquals("user_auth_token", capturedUserAuthToken)
    }
}
