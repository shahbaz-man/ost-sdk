import com.asdev.ost.sdk.OSTSdk
import com.asdev.ost.sdk.network.ApacheNetworkProvider
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.util.*

class UsersTest {

    @Before
    fun setup() {
        val apiKey = Files.readAllLines(File(".apikey").toPath()).filterNotNull().first()
        val apiSecret = Files.readAllLines(File(".apisecret").toPath()).filterNotNull().first()

        OSTSdk.init(apiKey, apiSecret, ApacheNetworkProvider)
    }

    @Test
    fun createUser() {
        println(OSTSdk.Users.create("CreateUserTest${Random().nextInt(10000)}"))
    }

    @Test
    fun listUsers() {
        val list = OSTSdk.Users.list(limit = 100)
        println(list.joinToString(separator = "\r\n"))
    }

    @Test
    fun getUser() {
        println(OSTSdk.Users.get(OSTSdk.Users.list()[0].id?: ""))
    }

}