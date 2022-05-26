import com.marine.fishtank.server.util.Log
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.util.Date

private const val TAG = "SimpleTest"
class SimpleTest {
    @Test
    fun start() {
        val testTimeStamp = System.currentTimeMillis()
        Log.i(TAG, "CurrentMils=$testTimeStamp")

        //1653551798579
        //1653490800000

        val formatter = SimpleDateFormat("HH:mm:ss")
        val formatTimestamp = formatter.format(testTimeStamp)
        val formatDate = formatter.format(Date(testTimeStamp))

        Log.d(TAG, "formatTimeStamp=$formatTimestamp")
        Log.d(TAG, "formatDate=$formatDate")
    }
}