package com.stickman.tower.input

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VirtualControllerViewportTest {

    @Test
    fun testVirtualControllerButtonPlacement() {
        val controller = VirtualController()
        controller.resize(1280, 720)

        // Verifikasi D-Pad (Lingkaran)
        assertEquals(85f, controller.btnLeft.x)
        assertEquals(95f, controller.btnLeft.y)
        assertEquals(45f, controller.btnLeft.radius)

        assertEquals(200f, controller.btnRight.x)
        assertEquals(95f, controller.btnRight.y)
        assertEquals(45f, controller.btnRight.radius)

        // Verifikasi Tombol Aksi Kanan Layar
        assertEquals(1280f - 120f, controller.btnAttack.x)
        assertEquals(110f, controller.btnAttack.y)
        assertEquals(54f, controller.btnAttack.radius)

        assertEquals(1280f - 235f, controller.btnJump.x)
        assertEquals(75f, controller.btnJump.y)
        assertEquals(42f, controller.btnJump.radius)

        assertEquals(1280f - 245f, controller.btnDodge.x)
        assertEquals(175f, controller.btnDodge.y)

        assertEquals(1280f - 120f, controller.btnSkill.x)
        assertEquals(235f, controller.btnSkill.y)

        assertEquals(1280f - 225f, controller.btnUltimate.x)
        assertEquals(275f, controller.btnUltimate.y)
    }

    @Test
    fun testButtonHitTesting() {
        val controller = VirtualController()
        controller.resize(1280, 720)

        // Point tepat di tengah tombol Attack
        assertTrue(controller.btnAttack.contains(1280f - 120f, 110f))
        // Point di luar radius
        assertFalse(controller.btnAttack.contains(0f, 0f))
        assertFalse(controller.btnAttack.contains(1280f - 120f + 60f, 110f))

        // D-Pad hit-testing
        assertTrue(controller.btnLeft.contains(85f, 95f))
        assertTrue(controller.btnRight.contains(200f, 95f))
    }
}
