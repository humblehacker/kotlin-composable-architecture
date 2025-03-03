package composablearchitecture.example.search

import composablearchitecture.test.TestStore
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.Result as KResult
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class SearchTests {

    @Test
    fun `Search and clear query`() = runTest {
        val dispatcher = TestCoroutineDispatcher()
        val environment = SearchEnvironment(weatherClient = MockWeatherClient())
        val store = TestStore(
            SearchState(),
            searchReducer,
            environment,
            dispatcher
        )

        store.assert {
            send(SearchAction.SearchQueryChanged("S")) {
                it.copy(searchQuery = "S")
            }
            doBlock { dispatcher.advanceTimeBy(300L) }
            receive(SearchAction.LocationsResponse(KResult.success(mockLocations))) {
                it.copy(locations = mockLocations)
            }
            send(SearchAction.SearchQueryChanged("")) {
                it.copy(
                    locations = emptyList(),
                    searchQuery = ""
                )
            }
        }
    }

    @Test
    fun `Search failure`() = runTest {
        val dispatcher = TestCoroutineDispatcher()
        val environment = SearchEnvironment(weatherClient = MockWeatherClient())
        val store = TestStore(
            SearchState(),
            searchReducer,
            environment,
            dispatcher
        )

        val error = KResult.failure<List<Location>>(HttpException(Response.error<Unit>(500, "".toResponseBody())))

        store.assert {
            environment {
                it.weatherClient.searchLocation = { error }
            }
            send(SearchAction.SearchQueryChanged("S")) {
                it.copy(searchQuery = "S")
            }
            doBlock { dispatcher.advanceTimeBy(300L) }
            receive(SearchAction.LocationsResponse( error ))
        }
    }

    @Test
    fun `Clear query cancels in-flight search request`() = runTest {
        val dispatcher = TestCoroutineDispatcher()
        val environment = SearchEnvironment(weatherClient = MockWeatherClient())
        val store = TestStore(
            SearchState(),
            searchReducer,
            environment,
            dispatcher
        )

        store.assert {
            send(SearchAction.SearchQueryChanged("S")) {
                it.copy(searchQuery = "S")
            }
            doBlock { dispatcher.advanceTimeBy(200L) }
            send(SearchAction.SearchQueryChanged("")) {
                it.copy(searchQuery = "")
            }
        }
    }

    @Test
    fun `Tap on location`() = runTest {
        val specialLocation = Location(42, "Special Place")
        val specialLocationWeather = LocationWeather(
            consolidatedWeather = mockWeather,
            id = 42
        )

        val dispatcher = TestCoroutineDispatcher()
        val environment = SearchEnvironment(weatherClient = MockWeatherClient().apply {
            weather = { KResult.success(specialLocationWeather) }
        })
        val store = TestStore(
            SearchState(locations = mockLocations + specialLocation),
            searchReducer,
            environment,
            dispatcher
        )

        store.assert {
            send(SearchAction.LocationTapped(specialLocation)) {
                it.copy(locationWeatherRequestInFlight = specialLocation)
            }
            receive(SearchAction.LocationWeatherResponse(KResult.success(specialLocationWeather))) {
                it.copy(
                    locationWeatherRequestInFlight = null,
                    locationWeather = specialLocationWeather
                )
            }
        }
    }

    @Test
    fun `Tap on location failure`() = runTest {
        val dispatcher = TestCoroutineDispatcher()

        val error = KResult.failure<LocationWeather>(HttpException(Response.error<Unit>(500, "".toResponseBody())))

        val environment = SearchEnvironment(weatherClient = MockWeatherClient().apply {
            weather = { error }
        })

        val store = TestStore(
            SearchState(locations = mockLocations),
            searchReducer,
            environment,
            dispatcher
        )

        store.assert {
            send(SearchAction.LocationTapped(mockLocations.first())) {
                it.copy(locationWeatherRequestInFlight = mockLocations.first())
            }
            receive(SearchAction.LocationWeatherResponse(error)) {
                it.copy(locationWeatherRequestInFlight = null)
            }
        }
    }
}
