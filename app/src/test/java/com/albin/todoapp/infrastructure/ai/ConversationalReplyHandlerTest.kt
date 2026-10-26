package com.albin.todoapp.infrastructure.ai

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConversationalReplyHandlerTest {

    @Test
    fun `checkConversational handles ok and returns friendly acknowledgment`() {
        val reply = ConversationalReplyHandler.checkConversational("ok")
        assertNotNull(reply)
        assertTrue(reply!!.contains("Con gusto"))
    }

    @Test
    fun `checkConversational handles thanks and variations`() {
        val reply1 = ConversationalReplyHandler.checkConversational("gracias")
        assertNotNull(reply1)

        val reply2 = ConversationalReplyHandler.checkConversational("muchas gracias!")
        assertNotNull(reply2)

        val reply3 = ConversationalReplyHandler.checkConversational("dale")
        assertNotNull(reply3)
    }

    @Test
    fun `checkConversational handles greetings`() {
        val reply = ConversationalReplyHandler.checkConversational("¡Hola!")
        assertNotNull(reply)
        assertTrue(reply!!.contains("Hola"))
    }

    @Test
    fun `checkConversational handles help query`() {
        val reply = ConversationalReplyHandler.checkConversational("ayuda")
        assertNotNull(reply)
        assertTrue(reply!!.contains("Crear tareas"))
    }

    @Test
    fun `checkConversational returns null when prompt contains an actual task action`() {
        val reply1 = ConversationalReplyHandler.checkConversational("ok comprar pan mañana")
        assertNull(reply1)

        val reply2 = ConversationalReplyHandler.checkConversational("hola recuerda comprar leche a las 7pm")
        assertNull(reply2)
    }
}
