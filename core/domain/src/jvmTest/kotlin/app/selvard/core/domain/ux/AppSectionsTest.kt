package app.selvard.core.domain.ux

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppSectionsTest {

    @Test
    fun sevenSectionsInRoadmapOrder() {
        assertEquals(
            listOf(
                AppSection.HOME,
                AppSection.SECURITY,
                AppSection.NETWORK,
                AppSection.APPS,
                AppSection.IDENTITY,
                AppSection.TIMELINE,
                AppSection.SETTINGS,
            ),
            AppSection.entries.toList(),
        )
    }

    @Test
    fun routesAreUniqueAndStable() {
        val routes = AppSection.entries.map { it.route }
        assertEquals(routes.size, routes.toSet().size)
        assertEquals(listOf("home", "security", "network", "apps", "identity", "timeline", "settings"), routes)
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
    fun everySectionHasAKnownRoundedIconKey() {
        val known = setOf("home", "shield", "wifi", "apps", "person", "history", "settings")
        AppSection.entries.forEach { section ->
            assertTrue(section.iconKey in known, "${section.route} has unknown icon key ${section.iconKey}")
        }
        val keys = AppSection.entries.map { it.iconKey }
        assertEquals(keys.size, keys.toSet().size, "tab icons must be distinct")
    }
}
