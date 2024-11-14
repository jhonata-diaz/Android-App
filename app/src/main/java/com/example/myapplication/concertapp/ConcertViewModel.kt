package com.example.myapplication.concertapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Definición del enum para géneros musicales
enum class MusicGenre {
    ROCK, POP, JAZZ, CLASSICAL, ELECTRONIC, HIP_HOP, REGGAETON, METAL
}

// Data class para conciertos
data class Concert(
    val id: String,
    val name: String,
    val genre: MusicGenre,
    val price: Double,
    val date: String,
    val venue: String,
    val popularity: Int,
    val discount: Double? = null,
    val imageUrl: String
)

data class TicketConcert(
    val idTicket: String,
    val price: Double,
    var available: Boolean,
    val row: Int,
    val seatNumber: Int,
    val type: String,
    val concertId: String
)


// Interface para el API
interface ConcertApi {
    suspend fun getPopularConcerts(): List<Concert>
    suspend fun getBestOffersConcerts(): List<Concert>
    suspend fun getCalendarConcerts(): List<Concert>
    suspend fun getConcertDetails(id: String): Concert
    suspend fun getTicketsForConcert(concertId: String): List<TicketConcert> // Nuevo método

}

// Implementación mock del API
class MockConcertApi : ConcertApi {
    private val mockConcerts = listOf(
        Concert(
            id = "1",
            name = "Rock Summer Festival",
            genre = MusicGenre.ROCK,
            price = 89.99,
            date = "2024-07-15",
            venue = "Stadium Arena",
            popularity = 95,
            discount = 15.0,
            imageUrl = "https://picsum.photos/300/200"
        ),
        Concert(
            id = "2",
            name = "Jazz Night",
            genre = MusicGenre.JAZZ,
            price = 45.00,
            date = "2024-06-20",
            venue = "Blue Note Club",
            popularity = 85,
            discount = null,
            imageUrl = "https://picsum.photos/300/200"
        ),
        Concert(
            id = "3",
            name = "Electronic Dreams",
            genre = MusicGenre.ELECTRONIC,
            price = 75.00,
            date = "2024-08-01",
            venue = "Tech Arena",
            popularity = 90,
            discount = 20.0,
            imageUrl = "https://picsum.photos/300/200"
        )
        // Añade más conciertos mock aquí...
    )

    private val mockTickets = listOf(
        TicketConcert(idTicket = "T1", price = 89.99, available = true, row = 1, seatNumber = 5, type = "VIP", concertId = "1"),
        TicketConcert(idTicket = "T2", price = 45.00, available = true, row = 2, seatNumber = 10, type = "Regular", concertId = "2"),
        TicketConcert(idTicket = "T3", price = 75.00, available = false, row = 1, seatNumber = 1, type = "VIP", concertId = "3"),
        TicketConcert(idTicket = "T4", price = 55.00, available = true, row = 3, seatNumber = 8, type = "PREFERENTE", concertId = "1"),
        TicketConcert(idTicket = "T5", price = 55.00, available = true, row = 3, seatNumber = 8, type = "INTERMEDIO", concertId = "1"),
        TicketConcert(idTicket = "T6", price = 55.00, available = true, row = 3, seatNumber = 8, type = "ECOMICO", concertId = "1"),

        // Agrega más tickets mock aquí...
    )

    override suspend fun getPopularConcerts(): List<Concert> {
        return mockConcerts.sortedByDescending { it.popularity }
    }

    override suspend fun getBestOffersConcerts(): List<Concert> {
        return mockConcerts.filter { it.discount != null }
            .sortedByDescending { it.discount }
    }

    override suspend fun getCalendarConcerts(): List<Concert> {
        return mockConcerts.sortedBy { it.date }
    }

    override suspend fun getConcertDetails(id: String): Concert {
        return mockConcerts.first { it.id == id }
    }

    override suspend fun getTicketsForConcert(concertId: String): List<TicketConcert> {
        return mockTickets.filter { it.concertId == concertId }
    }


}
class ConcertViewModel : ViewModel() {
    private val api: ConcertApi = MockConcertApi()




    private val _popularConcerts = MutableStateFlow<List<Concert>>(emptyList())
    val popularConcerts: StateFlow<List<Concert>> = _popularConcerts.asStateFlow()

    private val _bestOffersConcerts = MutableStateFlow<List<Concert>>(emptyList())
    val bestOffersConcerts: StateFlow<List<Concert>> = _bestOffersConcerts.asStateFlow()

    private val _calendarConcerts = MutableStateFlow<List<Concert>>(emptyList())
    val calendarConcerts: StateFlow<List<Concert>> = _calendarConcerts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _ticketsForConcert = MutableStateFlow<List<TicketConcert>>(emptyList())
    val ticketsForConcert: StateFlow<List<TicketConcert>> = _ticketsForConcert.asStateFlow()


    private val _concertDetails = MutableStateFlow<Concert?>(null)
    val concertDetails: StateFlow<Concert?> = _concertDetails.asStateFlow()


    init {
        loadAllConcerts()
    }

    private fun loadAllConcerts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                coroutineScope {
                    val popular = async { api.getPopularConcerts() }
                    val offers = async { api.getBestOffersConcerts() }
                    val calendar = async { api.getCalendarConcerts() }

                    _popularConcerts.value = popular.await()
                    _bestOffersConcerts.value = offers.await()
                    _calendarConcerts.value = calendar.await()
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error cargando los conciertos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }





    fun loadTicketsForConcert(concertId: String): StateFlow<List<TicketConcert>> {
        viewModelScope.launch {
            try {
                val tickets = api.getTicketsForConcert(concertId)
                _ticketsForConcert.value = tickets
            } catch (e: Exception) {
                _error.value = "Error cargando los tickets: ${e.message}"
            }
        }
        return ticketsForConcert // Devuelve el StateFlow de tickets
    }

    fun loadConcertDetails(concertId: String): StateFlow<Concert?> {
        viewModelScope.launch {
            try {
                val concert = api.getConcertDetails(concertId)
                _concertDetails.value = concert
            } catch (e: Exception) {
                _error.value = "Error cargando los detalles del concierto: ${e.message}"
            }
        }
        return concertDetails
    }
}
