package app.selvard.core.domain.ux

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppSectionsTest {

    @Test
    fun fiveSectionsInDisplayOrder() {
        assertEquals(
            listOf(
                AppSection.HOME,
                AppSection.SHIELD,
                AppSection.TIMELINE,
                AppSection.IDENTITY,
                AppSection.SETTINGS,
            ),
            AppSection.entries.toList(),
        )
    }

    @Test
    fun routesAreUniqueAndStable() {
        val routes = AppSection.entries.map { it.route }
        assertEquals(routes.size, routes.toSet().size)
        assertEquals(listOf("home", "shield", "timeline", "identity", "settings"), routes)
    }

    @Test
    fun everySectionHasScreenReaderDescription() {
        AppSection.entries.forEach { section ->
            assertTrue(section.title.isNotBlank(), "${section.route} needs a title")
            assertTrue(
                section.contentDescription.isNotBlank() && section.contentDescription.length >= section.title.length,
                "${section.route} needs a description beyond its title",
            )
        }
    }

    @Test
    fun everySectionHasADistinctKnownIconKey() {
        val known = setOf("home", "shield", "timeline", "person", "settings")
        AppSection.entries.forEach { section ->
            assertTrue(section.iconKey in known, "${section.route} has unknown icon key ${section.iconKey}")
        }
        val keys = AppSection.entries.map { it.iconKey }
        assertEquals(keys.size, keys.toSet().size, "tab icons must be distinct")
    }

    @Test
    fun shieldHoldsTheFourGuardianPanesInOrder() {
        assertEquals(
            listOf(ShieldPane.LINKS, ShieldPane.NETWORK, ShieldPane.APPS, ShieldPane.PRIVACY),
            ShieldPane.entries.toList(),
        )
        assertEquals(ShieldPane.entries.size, ShieldPane.entries.map { it.route }.toSet().size)
        ShieldPane.entries.forEach { assertTrue(it.contentDescription.length > it.title.length) }
    }
}
