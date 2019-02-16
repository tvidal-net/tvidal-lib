package uk.tvidal.db.mapper

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.jooq.Record
import org.jooq.impl.DSL.field
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.fail

@Suppress("UNUSED_PARAMETER", "UNCHECKED_CAST", "unused")
internal class ReflectionMapperTest {

    val record = mockk<Record>().also {
        every { it["test_name"] } returns "ok"
        every { it["test_id"] } returns 1
    }

    @Test
    fun `check for public constructor`() {
        assertThrows<IllegalArgumentException> {
            ReflectionMapper(NoPublicConstructor::class)
        }
    }

    @Test
    fun `use the largest constructor`() {
        ReflectionMapper(LargestConstructor::class)
    }

    @Test
    fun `reads simple values from the database`() {
        val mapper = ReflectionMapper(TestEntity::class)

        val entity = mapper.fromRecord(record)
        val expected = TestEntity("ok").apply { testId = 1 }
        assertEquals(expected, entity)
        assertEquals(expected.testId, entity.testId)
    }

    @Test
    fun `resolves properties using the provided resolver`() {
        every { record["test_name"] } returns "fail"
        val resolver = mockk<PropertyResolver<String>>().also {
            every { it.fromDatabase("fail") } returns "pass"
        }
        val resolverMap = mapOf(
            String::class.java to resolver
        ) as Map<Class<*>, PropertyResolver<Any>>
        val mapper = ReflectionMapper(TestEntity::class, resolverMap)
        val entity = mapper.fromRecord(record)
        val expected = TestEntity("pass")
        assertEquals(expected, entity)
    }

    @Test
    fun `set values to record`() {
        val testName = field("test_name", String::class.java)
        val testId = field("test_id", Int::class.java)
        val record = mockk<Record>().also {
            every { it.fields() } returns arrayOf(testName, testId)
            every { it.set(testName, any()) } just Runs
            every { it.set(testId, any()) } just Runs
        }
        val mapper = ReflectionMapper(TestEntity::class)
        val entity = TestEntity("name")
        mapper.toRecord(entity, record)
        verify { record.set(testName, "name") }
        verify { record.set(testId, 0) }
    }

    class NoPublicConstructor private constructor()

    class LargestConstructor(test: String, id: Number) {
        constructor(test: String) : this(test, 0) {
            fail()
        }
    }

    data class TestEntity(val testName: String) {
        var testId: Number = 0
    }
}