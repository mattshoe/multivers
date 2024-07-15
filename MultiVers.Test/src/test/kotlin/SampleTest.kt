import kotlin.test.Test
import kotlin.test.assertTrue

class SampleTest {
    @Test
    fun firstTest() {
        assertTrue { true }
    }

    @Test
    fun longTest() {
        repeat(Int.MAX_VALUE) {
            repeat(2) {
                assertTrue { true }
            }
        }
    }

}