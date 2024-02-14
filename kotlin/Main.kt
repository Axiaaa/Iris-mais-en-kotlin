//https://api.twitch.tv/helix/chat/messages

import dev.minn.jda.ktx.jdabuilder.injectKTX
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.net.HttpURLConnection
import java.net.URI

data class Credentials(
    val TWITCH_CLIENT_ID: String,
    val TWITCH_CLIENT_SECRET: String,
    var SESSION_SECRET: String,
    val CALLBACK_URL: String,
    val DISCORD_TOKEN: String
)

@Serializable
data class SessionToken(
    val access_token: String,
    val expires_in: Int,
    val token_type: String,
    val scope: List<String>,
)

@Serializable
data class Channel(
    val data: List<ChannelData>
)

@Serializable
data class ChannelData(
    val broadcaster_id: String,
    val broadcaster_name: String,
    val broadcaster_language: String,
    val broadcaster_login: String,
    val game_id: String,
    val game_name: String,
    val title: String,
    val delay: Int,
    val is_mature: Boolean = false,
    val tags: List<String> = emptyList(),
    val is_branded_content: Boolean = false,
    val content_classification_labels: List<String> = emptyList()
)

fun get_session_token(creds: Credentials): String {

    val url = URI(
        "https://id.twitch.tv/oauth2/token?client_id=${creds.TWITCH_CLIENT_ID}&client_secret=${creds.TWITCH_CLIENT_SECRET}&grant_type=client_credentials&scope=user%3Awrite%3Achat&expires_in=3600"
    ).toURL()
    val con = url.openConnection() as HttpURLConnection
    con.requestMethod = "POST"
    con.setRequestProperty("Content-Type", "application/json")
    con.setRequestProperty("Accept", "application/json")
    con.doOutput = true
    val response = con.inputStream.bufferedReader().use { it.readText() }
    val sessiontoken = Json.decodeFromString<SessionToken>(response)
    return sessiontoken.access_token
}

fun get_credentials(): Credentials {

    val file = File(".env")
    val reader = BufferedReader(FileReader(file, Charsets.UTF_8))
    val envlist = reader.lines().toList()
    val creds = Credentials(
        TWITCH_CLIENT_ID = envlist[1].removePrefix("LOGIN=").trim('"'),
        TWITCH_CLIENT_SECRET = envlist[0].removePrefix("SECRET=").trim('"'),
        SESSION_SECRET = " ",
        CALLBACK_URL = "http://localhost:3000/auth/twitch/callback]",
        DISCORD_TOKEN = envlist[2].removePrefix("TOKEN=").trim('"')
    )
    creds.SESSION_SECRET = get_session_token(creds)
    return creds
}


fun send_requests(creds: Credentials): Channel {

    val url = URI("https://api.twitch.tv/helix/channels?broadcaster_id=606156919").toURL()
    val con = url.openConnection() as HttpURLConnection
    con.requestMethod = "GET"
    con.setRequestProperty("Authorization", "Bearer ${creds.SESSION_SECRET}")
    con.setRequestProperty("Client-Id", creds.TWITCH_CLIENT_ID)
    con.doOutput = true
    val response = con.inputStream.bufferedReader().use { it.readText() }
    val fresponse = Json.decodeFromString<Channel>(response)
    return fresponse
}

val Creds = get_credentials()
val response = send_requests(Creds)

fun main() {

    JDABuilder.createDefault(Creds.DISCORD_TOKEN)
        .enableIntents(
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.GUILD_PRESENCES,
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_MESSAGES
        )
        .addEventListeners(Dispatcher())
        .injectKTX()
        .setMemberCachePolicy(MemberCachePolicy.OWNER)
        .enableCache(CacheFlag.ACTIVITY)
        .build()
        .awaitReady()
}


