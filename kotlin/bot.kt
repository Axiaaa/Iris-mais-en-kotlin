import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.CoroutineEventListener
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.send
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.events.user.UserActivityStartEvent


class Dispatcher : CoroutineEventListener {

    private fun envoieAnnonce(channel: NewsChannel) {
        val embed = Embed {
            title = response.data[0].title
            color = 0X6441a5
            url = "https://twitch.tv/${response.data[0].broadcaster_login}"
            thumbnail = "https://static-cdn.jtvnw.net/ttv-boxart/${response.data[0].game_id}-272x380.jpg"

            footer {
                name = "Annonce du ${java.time.LocalDate.now()} à ${
                    java.time.LocalTime.now().toString().trim().substring(0, 5)
                }"
                iconUrl =
                    "https://cdn.discordapp.com/attachments/1182428645772509225/1207414072178515979/twitch-icon-1024x1024-rqcv3iwu.png?ex=65df8efb&is=65cd19fb&hm=9632b3becdaa258ff19219a12df781043c09b1321e647b5d1c4b8f29e1624281&"
            }

            author {
                name = response.data[0].broadcaster_name
                iconUrl =
                    "https://cdn.discordapp.com/attachments/1182428645772509225/1207420386367504394/logo.png?ex=65df94dc&is=65cd1fdc&hm=df74811539672e956633297b631e05daabb7df4604aca2bf77db841d5390d75f&"
            }

            field {
                name = "Game"
                value = response.data[0].game_name
                inline = true
            }
        }
        channel.send(
            content = "<@&1180650004839407707>",
            embeds = listOf(embed)
        ).queue()
    }

    override suspend fun onEvent(event: GenericEvent) {

        val bot = event.jda
        when (event) {
            is ReadyEvent -> {
                val user = bot.getGuildById(1179840761781567508)?.retrieveOwner()?.await()
                println(user?.activities)
            }

            is UserActivityStartEvent -> {
                if ((event.guild.idLong != 1179840761781567508) or (event.member.idLong != 240430740158939139)) return
                if (event.newActivity.type == Activity.ActivityType.STREAMING) {
                    val channel = event.guild.getNewsChannelById(1179901367943434433) ?: return
                    envoieAnnonce(channel)
                }
            }

        }
    }
}


