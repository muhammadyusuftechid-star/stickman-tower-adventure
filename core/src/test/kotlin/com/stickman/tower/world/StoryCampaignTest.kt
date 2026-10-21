package com.stickman.tower.world

import com.stickman.tower.enemy.BossEnemy
import com.stickman.tower.enemy.BossState
import com.stickman.tower.enemy.BossType
import com.stickman.tower.endless.FloorMutator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit Test Komprehensif Kampanye Cerita Utama (The 10-Floor Story Odyssey)
 * Menguji integritas struktural ke-10 lantai, 5 tipe Bos Menara, Prasasti Kuno (Lore Tablets),
 * Dialog Tokoh Cerita (Aria & Vulcanus), Zona Aman, serta Anomali Gravitasi Rendah.
 */
class StoryCampaignTest {

    @Test
    fun testAllTenFloorsBuildProperly() {
        for (floorNum in 1..10) {
            val floor = TowerLevelBuilder.buildFloor(floorNum)
            assertEquals(floorNum, floor.floorNumber)
            assertTrue(floor.floorName.isNotEmpty())
            assertTrue(floor.platforms.isNotEmpty(), "Lantai $floorNum harus memiliki platform!")
            assertNotNull(floor.exitPortal, "Lantai $floorNum harus memiliki portal keluar!")
            assertTrue(floor.actTitle.isNotEmpty(), "Lantai $floorNum harus memiliki judul Act!")
            assertTrue(floor.actSubtitle.isNotEmpty(), "Lantai $floorNum harus memiliki subjudul Act!")
        }
    }

    @Test
    fun testActHierarchyAndThematicContinuity() {
        // Act I: Lantai 1 s/d 3
        val floor1 = TowerLevelBuilder.buildFloor(1)
        val floor2 = TowerLevelBuilder.buildFloor(2)
        val floor3 = TowerLevelBuilder.buildFloor(3)
        assertEquals("ACT I: THE INITIATION", floor1.actTitle)
        assertEquals("ACT I: THE INITIATION", floor2.actTitle)
        assertEquals("ACT I: THE INITIATION", floor3.actTitle)
        assertEquals("LOWER BASTION", floor1.actSubtitle)
        assertEquals("SPIRE OF HAZARDS", floor2.actSubtitle)
        assertEquals("APEX BOSS ARENA", floor3.actSubtitle)

        // Act II: Lantai 4 s/d 6
        val floor4 = TowerLevelBuilder.buildFloor(4)
        val floor5 = TowerLevelBuilder.buildFloor(5)
        val floor6 = TowerLevelBuilder.buildFloor(6)
        assertEquals("ACT II: SECRETS OF THE INK", floor4.actTitle)
        assertEquals("ACT II: SECRETS OF THE INK", floor5.actTitle)
        assertEquals("ACT II: SECRETS OF THE INK", floor6.actTitle)
        assertEquals("THE SUNKEN ARCHIVES", floor4.actSubtitle)
        assertEquals("CRUCIBLE OF MUTATIONS", floor5.actSubtitle)
        assertEquals("THE IRON FOUNDRY", floor6.actSubtitle)

        // Act III: Lantai 7 s/d 9
        val floor7 = TowerLevelBuilder.buildFloor(7)
        val floor8 = TowerLevelBuilder.buildFloor(8)
        val floor9 = TowerLevelBuilder.buildFloor(9)
        assertEquals("ACT III: THE ASTRAL CLIMB", floor7.actTitle)
        assertEquals("ACT III: THE ASTRAL CLIMB", floor8.actTitle)
        assertEquals("ACT III: THE ASTRAL CLIMB", floor9.actTitle)
        assertEquals("THE FRACTURED PRECIPICE", floor7.actSubtitle)
        assertEquals("THE HALL OF ECHOES", floor8.actSubtitle)
        assertEquals("THE BLOOD MOON SANCTUM", floor9.actSubtitle)

        // Act IV: Lantai 10
        val floor10 = TowerLevelBuilder.buildFloor(10)
        assertEquals("ACT IV: THE PRIMORDIAL CANVAS", floor10.actTitle)
        assertEquals("SUMMIT OF REALITY", floor10.actSubtitle)
    }

    @Test
    fun testFiveLegendaryBossesInitializationAndAttributes() {
        // 1. Boss Lantai 3: The Tower Guardian (General Oakhaven)
        val floor3 = TowerLevelBuilder.buildFloor(3)
        assertTrue(floor3.isBossFloor)
        val boss3 = floor3.boss
        assertNotNull(boss3)
        assertEquals(BossType.TOWER_GUARDIAN, boss3.type)
        assertEquals("TOWER GUARDIAN", boss3.name)
        assertEquals("General Oakhaven", boss3.subtitle)
        assertEquals(350f, boss3.health.maxHp)

        // 2. Boss Lantai 5: The Amethyst Matron (Sang Ratu Busur Bayang)
        val floor5 = TowerLevelBuilder.buildFloor(5)
        assertTrue(floor5.isBossFloor)
        val boss5 = floor5.boss
        assertNotNull(boss5)
        assertEquals(BossType.AMETHYST_MATRON, boss5.type)
        assertEquals("THE AMETHYST MATRON", boss5.name)
        assertEquals("Queen of the Shadow Bow", boss5.subtitle)
        assertEquals(480f, boss5.health.maxHp)

        // 3. Boss Lantai 8: The Shadow Doppelgänger (Cermin Jiwa Gelap)
        val floor8 = TowerLevelBuilder.buildFloor(8)
        assertTrue(floor8.isBossFloor)
        val boss8 = floor8.boss
        assertNotNull(boss8)
        assertEquals(BossType.SHADOW_DOPPELGANGER, boss8.type)
        assertEquals("SHADOW DOPPELGANGER", boss8.name)
        assertEquals("Dark Reflection", boss8.subtitle)
        assertEquals(620f, boss8.health.maxHp)

        // 4. Boss Lantai 9: The Crimson Sovereign (Pangeran Belati Darah)
        val floor9 = TowerLevelBuilder.buildFloor(9)
        assertTrue(floor9.isBossFloor)
        val boss9 = floor9.boss
        assertNotNull(boss9)
        assertEquals(BossType.CRIMSON_SOVEREIGN, boss9.type)
        assertEquals("CRIMSON SOVEREIGN", boss9.name)
        assertEquals("Prince of Blood Daggers", boss9.subtitle)
        assertEquals(780f, boss9.health.maxHp)

        // 5. Boss Lantai 10: The Grand Calligrapher (Sang Penulis Takdir)
        val floor10 = TowerLevelBuilder.buildFloor(10)
        assertTrue(floor10.isBossFloor)
        val boss10 = floor10.boss
        assertNotNull(boss10)
        assertEquals(BossType.GRAND_CALLIGRAPHER, boss10.type)
        assertEquals("THE GRAND CALLIGRAPHER", boss10.name)
        assertEquals("Architect of Fate", boss10.subtitle)
        assertEquals(1100f, boss10.health.maxHp)
        assertTrue(boss10.health.maxHp > boss9.health.maxHp, "Final Boss harus memiliki HP tertinggi!")
    }

    @Test
    fun testLoreTabletsPlacementAndContent() {
        // Tablet I di Lantai 1
        val floor1 = TowerLevelBuilder.buildFloor(1)
        assertEquals(1, floor1.loreTablets.size)
        val tablet1 = floor1.loreTablets.first()
        assertEquals("TABLET_1", tablet1.tabletId)
        assertTrue(tablet1.title.contains("Tablet I"))
        assertTrue(tablet1.text.contains("Hari ke-47"))

        // Tablet IV di Lantai 4
        val floor4 = TowerLevelBuilder.buildFloor(4)
        assertEquals(1, floor4.loreTablets.size)
        val tablet4 = floor4.loreTablets.first()
        assertEquals("TABLET_4", tablet4.tabletId)
        assertTrue(tablet4.title.contains("Tablet IV"))
        assertTrue(tablet4.text.contains("panah amethyst"))

        // Tablet VIII di Lantai 8
        val floor8 = TowerLevelBuilder.buildFloor(8)
        assertEquals(1, floor8.loreTablets.size)
        val tablet8 = floor8.loreTablets.first()
        assertEquals("TABLET_8", tablet8.tabletId)
        assertTrue(tablet8.title.contains("Tablet VIII"))
        assertTrue(tablet8.text.contains("parry"))
    }

    @Test
    fun testStoryDialoguesAriaAndVulcanus() {
        // Aria di Lantai 1
        val floor1 = TowerLevelBuilder.buildFloor(1)
        assertEquals(1, floor1.storyDialogues.size)
        val ariaDialogue = floor1.storyDialogues.first()
        assertEquals("Aria", ariaDialogue.lines.first().speaker)
        assertTrue(ariaDialogue.lines.first().text.contains("merampas merahnya mawar"))

        // Vulcanus di Lantai 6
        val floor6 = TowerLevelBuilder.buildFloor(6)
        assertEquals(1, floor6.storyDialogues.size)
        val vulcanusDialogue = floor6.storyDialogues.first()
        assertEquals("Vulcanus", vulcanusDialogue.lines.first().speaker)
        assertEquals(2, vulcanusDialogue.lines.size)
        assertTrue(vulcanusDialogue.lines[0].text.contains("Denting paluku"))
        assertTrue(vulcanusDialogue.lines[1].text.contains("Koin emas"))
    }

    @Test
    fun testFloor6SafeZoneSanctuary() {
        val floor6 = TowerLevelBuilder.buildFloor(6)
        assertTrue(floor6.isSafeZone, "Lantai 6 harus merupakan Safe Zone!")
        assertFalse(floor6.isBossFloor, "Lantai 6 bukan boss floor!")
        assertNull(floor6.boss, "Lantai 6 tidak boleh memiliki boss musuh!")
        assertTrue(floor6.enemies.isEmpty(), "Lantai 6 harus bebas dari monster musuh!")
        assertTrue(floor6.spikes.isEmpty(), "Lantai 6 tidak boleh memiliki duri maut!")
        assertTrue(floor6.chests.isNotEmpty(), "Lantai 6 harus memiliki peti relik tempaan!")
    }

    @Test
    fun testFloor7LowGravityAnomaly() {
        val floor7 = TowerLevelBuilder.buildFloor(7)
        assertEquals(0.65f, floor7.gravityScale, "Lantai 7 harus memiliki skala gravitasi rendah 0.65f!")
    }

    @Test
    fun testEndlessFloorTransitionAndScaling() {
        // Lantai 11 (Endless Rank 1)
        val endless11 = TowerLevelBuilder.buildFloor(11, FloorMutator.NONE)
        assertEquals(11, endless11.floorNumber)
        assertEquals("ENDLESS PARADOX", endless11.actTitle)
        assertEquals("FLOOR 11", endless11.actSubtitle)

        // Lantai 15 (Endless Boss)
        val endless15 = TowerLevelBuilder.buildFloor(15, FloorMutator.BERSERK_SURGE)
        assertTrue(endless15.isBossFloor)
        val boss15 = endless15.boss
        assertNotNull(boss15)
        assertTrue(boss15.health.maxHp > 350f, "Boss Endless harus diskalakan HP-nya!")
    }

    @Test
    fun testBossWindSlashProjectilePatterns() {
        val player = com.stickman.tower.character.Player(spawnX = 4f, spawnY = 2f)
        val bossTypes = listOf(
            BossType.TOWER_GUARDIAN,
            BossType.AMETHYST_MATRON,
            BossType.SHADOW_DOPPELGANGER,
            BossType.CRIMSON_SOVEREIGN,
            BossType.GRAND_CALLIGRAPHER
        )

        for (type in bossTypes) {
            val boss = BossEnemy(spawnX = 10f, spawnY = 2f, type = type)
            boss.state = BossState.WIND_SLASH
            val prevProjCount = boss.projectiles.size
            boss.update(delta = 0.30f, player = player, platforms = emptyList())
            assertTrue(boss.projectiles.size > prevProjCount, "Boss $type harus menghasilkan proyektil pada WIND_SLASH!")
        }
    }
}
