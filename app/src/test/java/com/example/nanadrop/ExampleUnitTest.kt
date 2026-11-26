package com.example.nanadrop

import com.example.nanadrop.data.remote.CheapSharkApiService
import com.example.nanadrop.data.remote.CheapSharkDeal
import com.example.nanadrop.data.remote.RetrofitClient
import com.example.nanadrop.data.repository.RemoteDealsRepository
import com.example.nanadrop.ui.viewmodel.DealsUiState
import com.example.nanadrop.ui.viewmodel.DealsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DealsUnitTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testRetrofitClientGetDeals() = runBlocking {
        val repository = RemoteDealsRepository()
        val result = repository.getDeals(pageSize = 20)
        assertTrue("Repository call failed: ${result.exceptionOrNull()?.message}", result.isSuccess)
        val deals = result.getOrNull()
        assertNotNull(deals)
        assertTrue("Deals list was empty", deals!!.isNotEmpty())
        val firstDeal = deals.first()
        assertNotNull(firstDeal.dealID)
        assertNotNull(firstDeal.title)
    }

    @Test
    fun testDealsViewModelSuccessState() = runBlocking {
        val fakeApiService = object : CheapSharkApiService {
            override suspend fun getDeals(pageSize: Int): List<CheapSharkDeal> {
                return listOf(
                    CheapSharkDeal(
                        dealID = "1",
                        title = "Test Game",
                        salePrice = "9.99",
                        normalPrice = "19.99",
                        savings = "50.0",
                        thumb = "https://example.com/thumb.jpg"
                    )
                )
            }
        }
        val repository = RemoteDealsRepository(apiService = fakeApiService)
        val viewModel = DealsViewModel(repository = repository)

        var state = viewModel.uiState.value
        val start = System.currentTimeMillis()
        while (state is DealsUiState.Loading && System.currentTimeMillis() - start < 2000) {
            kotlinx.coroutines.delay(50)
            state = viewModel.uiState.value
        }

        assertTrue("State was $state", state is DealsUiState.Success)
        val successState = state as DealsUiState.Success
        assertEquals(1, successState.deals.size)
        assertEquals("Test Game", successState.deals.first().title)
    }

    @Test
    fun testDealsViewModelEmptyState() = runBlocking {
        val fakeApiService = object : CheapSharkApiService {
            override suspend fun getDeals(pageSize: Int): List<CheapSharkDeal> {
                return emptyList()
            }
        }
        val repository = RemoteDealsRepository(apiService = fakeApiService)
        val viewModel = DealsViewModel(repository = repository)

        var state = viewModel.uiState.value
        val start = System.currentTimeMillis()
        while (state is DealsUiState.Loading && System.currentTimeMillis() - start < 2000) {
            kotlinx.coroutines.delay(50)
            state = viewModel.uiState.value
        }

        assertTrue("State was $state", state is DealsUiState.Empty)
    }

    @Test
    fun testDealsViewModelErrorState() = runBlocking {
        val fakeApiService = object : CheapSharkApiService {
            override suspend fun getDeals(pageSize: Int): List<CheapSharkDeal> {
                throw RuntimeException("Network error")
            }
        }
        val repository = RemoteDealsRepository(apiService = fakeApiService)
        val viewModel = DealsViewModel(repository = repository)

        var state = viewModel.uiState.value
        val start = System.currentTimeMillis()
        while (state is DealsUiState.Loading && System.currentTimeMillis() - start < 2000) {
            kotlinx.coroutines.delay(50)
            state = viewModel.uiState.value
        }

        assertTrue("State was $state", state is DealsUiState.Error)
        val errorState = state as DealsUiState.Error
        assertEquals("Network error", errorState.message)
    }
}