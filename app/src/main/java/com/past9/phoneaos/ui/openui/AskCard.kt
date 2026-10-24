package com.past9.phoneaos.ui.openui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.past9.phoneaos.agent.AskOptions
import com.past9.phoneaos.agent.EscalationGate
import com.past9.phoneaos.tools.ConnectRequest
import com.past9.phoneaos.ui.i18n.I18n
import com.past9.phoneaos.ui.i18n.LocalLanguage

/**
 * What an agent asks of the user, drawn as an OpenUI card: who needs you and why, the question, then one full-width
 * button per answer. No is always the last button ([AskOptions.withNo]). Tapping a button answers the question
 * itself; nothing goes into the chat as a message.
 *
 * [display] translates what the user reads; the answer sent back to the agent stays the English key.
 */
object AskProgram {
    /** A string literal in OpenUI Lang. */
    internal fun q(s: String) = "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "").replace("\n", "\\n") + "\""

    fun build(title: String, subtitle: String?, question: String, options: List<String>, display: (String) -> String = { it }): String {
        val all = AskOptions.withNo(options)
        val last = all.lastIndex
        return buildString {
            appendLine("root = Card([head, body, actions])")
            appendLine("head = CardHeader(${q(display(title))}${subtitle?.takeIf { it.isNotBlank() }?.let { ", ${q(display(it))}" } ?: ""})")
            appendLine("body = TextContent(${q(display(question))}, \"large\")")
            appendLine("actions = Buttons([${all.indices.joinToString(", ") { "b$it" }}], \"column\")")
            all.forEachIndexed { i, o -> appendLine("b$i = Button(${q(display(o))}, Action([@ToAssistant(${q(o)})]), ${q(if (i == 0 && last > 0 && o != AskOptions.NO) "primary" else "secondary")})") }
        }
    }

    /** The wire-format question in words a person reads, in their language. English scaffolding becomes Persian; agent-written text is passed through. */
    fun shown(question: String, language: String): String {
        val connect = ConnectRequest.parse(question)
        if (connect != null) return I18n.trArgs("Connect %1\$s?", language, connect.name) +
            (connect.reason.takeIf { it.isNotBlank() }?.let { " (" + it.removePrefix("so I can ").trim() + ")" } ?: "")
        return I18n.tr(EscalationGate.plain(question), language)
    }
}

@Composable
fun AskCard(title: String, subtitle: String?, question: String, options: List<String>, onPick: (String) -> Unit, modifier: Modifier = Modifier) {
    val lang = LocalLanguage.current
    val source = remember(title, subtitle, question, options, lang) {
        AskProgram.build(title, subtitle, AskProgram.shown(question, lang), options) { I18n.tr(it, lang) }
    }
    CompositionLocalProvider(LocalOpenUiPick provides onPick) { OpenUiBlock(source, modifier) }
}
