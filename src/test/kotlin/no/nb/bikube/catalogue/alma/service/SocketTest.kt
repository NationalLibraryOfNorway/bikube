package no.nb.bikube.catalogue.alma.service

import okhttp3.internal.concurrent.TaskRunner
import okhttp3.internal.threadFactory
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.junit.jupiter.api.Test
import javax.net.ServerSocketFactory
import org.junit.jupiter.api.Assertions
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.*
import kotlin.concurrent.thread

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class SocketTest {

    @Test
    fun `Try ServerSocket`() {
        val socket = ServerSocketFactory.getDefault().createServerSocket()
        socket.reuseAddress = false
        socket.bind(
            InetSocketAddress(
                InetAddress.getByName("localhost"),
                12345
            ), 50
        )
        socket.soTimeout = 1000
        Assertions.assertThrows(SocketTimeoutException::class.java) { socket.accept() }
        socket.close()
    }

    @Test
    fun `Actually use a socket`() {
        val server = GreetServer()
        thread(start = true, isDaemon = true) { server.start(2345) }

        Thread.sleep(1000)
        // Set up client
        val clientClientSocket = Socket("127.0.0.1", 2345)
        val clientOut = PrintWriter(clientClientSocket.getOutputStream(), true)
        val clientIn = BufferedReader(InputStreamReader(clientClientSocket.getInputStream()))
        clientOut.println("hello server")
        val response = clientIn.readLine()
        Assertions.assertEquals("hello client", response)

        clientIn.close()
        clientOut.close()
        clientClientSocket.close()
        server.stop()
    }

    @Test
    fun `Use TaskRunner`() {
        val server = GreetServer()

        val taskRunnerBackend = TaskRunner.RealBackend(
            threadFactory("MockWebServer TaskRunner", daemon = false)
        )
        val taskRunner = TaskRunner(taskRunnerBackend)
        taskRunner.newQueue().execute("MockWebServer", cancelable = false) {
            server.start(2345)
        }

        thread(start = true, isDaemon = true) { server.start(2345) }

        Thread.sleep(1000)
        // Set up client
        val clientClientSocket = Socket("127.0.0.1", 2345)
        val clientOut = PrintWriter(clientClientSocket.getOutputStream(), true)
        val clientIn = BufferedReader(InputStreamReader(clientClientSocket.getInputStream()))
        clientOut.println("hello server")
        val response = clientIn.readLine()
        Assertions.assertEquals("hello client", response)

        clientIn.close()
        clientOut.close()
        clientClientSocket.close()
        server.stop()
        taskRunnerBackend.shutdown()
    }
}

private class GreetServer {

    var serverSocket: ServerSocket? = null
    var clientSocket: Socket? = null
    var outStream: PrintWriter? = null
    var inStream: BufferedReader? = null

    fun start(port: Int) {
        println("Starting server on $port")
        try {
            serverSocket = ServerSocket(port)
            clientSocket = serverSocket!!.accept()
            outStream = PrintWriter(clientSocket!!.getOutputStream(), true)
            inStream = BufferedReader(InputStreamReader(clientSocket!!.getInputStream()))
            val greeting = inStream!!.readLine()
            if ("hello server" == greeting)
                outStream!!.println("hello client");
            else
                outStream!!.println("unrecognised greeting");
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun stop() {
        inStream!!.close()
        outStream!!.close()
        clientSocket!!.close()
        serverSocket!!.close()
    }
}
