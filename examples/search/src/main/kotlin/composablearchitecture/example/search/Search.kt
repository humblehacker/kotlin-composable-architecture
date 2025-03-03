package composablearchitecture.example.search

import composablearchitecture.Reducer
import composablearchitecture.Result
import composablearchitecture.cancel
import composablearchitecture.cancellable
import composablearchitecture.debug
import composablearchitecture.withEffect
import composablearchitecture.withNoEffect
import kotlinx.coroutines.delay

data class SearchState(
    val locations: List<Location> = emptyList(),
    val locationWeather: LocationWeather? = null,
    val locationWeatherRequestInFlight: Location? = null,
    val searchQuery: String = ""
) {
    companion object
}

sealed class SearchAction : Comparable<SearchAction> {
    data class LocationsResponse(val result: kotlin.Result<List<Location>>) : SearchAction()
    data class LocationTapped(val location: Location) : SearchAction()
    data class LocationWeatherResponse(val result: kotlin.Result<LocationWeather>) : SearchAction()
    data class SearchQueryChanged(val query: String) : SearchAction()

    override fun compareTo(other: SearchAction): Int = this.compareTo(other)
}

class SearchEnvironment(var weatherClient: WeatherClient = LiveWeatherClient())

val searchReducer = Reducer<SearchState, SearchAction, SearchEnvironment> { state, action, environment ->
    when (action) {
        is SearchAction.LocationsResponse -> action.handle(state)
        is SearchAction.LocationTapped -> action.handle(state, environment)
        is SearchAction.SearchQueryChanged -> action.handle(state, environment)
        is SearchAction.LocationWeatherResponse -> action.handle(state)
    }
}.debug()

private fun SearchAction.LocationsResponse.handle(state: SearchState): Result<SearchState, SearchAction> =
    result.fold(
        { locations -> state.copy(locations = locations).withNoEffect() },
        { error ->
            println(error.message)
            state.copy(locations = emptyList()).withNoEffect()
        }
    )

private fun SearchAction.LocationTapped.handle(
    state: SearchState,
    environment: SearchEnvironment
): Result<SearchState, SearchAction> =
    state
        .copy(locationWeatherRequestInFlight = location)
        .withEffect<SearchState, SearchAction> {
            val result = environment.weatherClient.weather(location.id)
            emit(SearchAction.LocationWeatherResponse(result))
        }
        .cancellable("SearchWeatherId", cancelInFlight = true)

private fun SearchAction.SearchQueryChanged.handle(
    state: SearchState,
    environment: SearchEnvironment
): Result<SearchState, SearchAction> {
    val newState = state.copy(searchQuery = query)
    return if (query.isBlank()) {
        newState
            .copy(locations = emptyList(), locationWeather = null)
            .cancel("SearchLocationId")
    } else {
        // TODO: implement debounce to combine delay and cancellable
        newState
            .withEffect<SearchState, SearchAction> {
                delay(300L)
                val result = environment.weatherClient.searchLocation(newState.searchQuery)
                emit(SearchAction.LocationsResponse(result))
            }
            .cancellable("SearchLocationId", cancelInFlight = true)
    }
}

private fun SearchAction.LocationWeatherResponse.handle(state: SearchState): Result<SearchState, SearchAction> =
    result.fold(
        { locationWeather ->
            state
                .copy(locationWeather = locationWeather, locationWeatherRequestInFlight = null)
                .withNoEffect()
        },
        { error ->
            println(error.message)
            state
                .copy(locationWeather = null, locationWeatherRequestInFlight = null)
                .withNoEffect()
        }
    )
