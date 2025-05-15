package at.aau.serg.sdlapp.network.viewModels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.*
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.subscribeText
import org.junit.*
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)

class LobbyViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var session: StompSession
    private lateinit var viewModel: LobbyViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        session = mockk()

        mockkStatic("org.hildan.krossbow.stomp.StompSessionKt")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initialize sets current player`() {
        coEvery { session.subscribeText(any()) } returns flow { } // leerer Flow

        viewModel = LobbyViewModel(session)
        viewModel.initialize("lobby123", "Anna")

        val result = viewModel.players.value
        assertEquals(listOf("Anna"), result)
    }

    @Test
    fun `startObserving adds new player from subscription`() = runTest {
        val lobbyId = "lobby123"

        val sampleJson = """{"playerName":"Bob"}"""
        val fakeFlow = flow {
            emit(sampleJson)
        }

        coEvery { session.subscribeText("/topic/$lobbyId") } returns fakeFlow

        viewModel = LobbyViewModel(session)
        viewModel.initialize(lobbyId, "Anna")

        // Warte etwas, bis der Flow verarbeitet wurde
        advanceUntilIdle()

        val result = viewModel.players.value
        assertEquals(listOf("Anna", "Bob"), result)
    }

    @Test
    fun `startObserving does not duplicate existing player`() = runTest {
        val lobbyId = "lobby123"
        val sampleJson = """{"playerName":"Anna"}""" // gleicher Spieler wie Startwert

        val fakeFlow = flow {
            emit(sampleJson)
        }

        coEvery { session.subscribeText("/topic/$lobbyId") } returns fakeFlow

        viewModel = LobbyViewModel(session)
        viewModel.initialize(lobbyId, "Anna")

        advanceUntilIdle()

        val result = viewModel.players.value
        assertEquals(listOf("Anna"), result)
    }
}