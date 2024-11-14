package com.example.myapplication


import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.myapplication.LoginRegister.LoginUiState
import com.example.myapplication.LoginRegister.UserSessionViewModel
import com.example.myapplication.concertapp.Concert
import com.example.myapplication.concertapp.ConcertViewModel


import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {


    private val userSessionViewModel: UserSessionViewModel by viewModels()

    private val viewModelConcert: ConcertViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ConcertViewModel() as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                MyApp(userSessionViewModel,viewModelConcert)
            }
        }
    }
}

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object ConcertMain : Screen("concert_main")
}

sealed class NavigationItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : NavigationItem("home", "Home", Icons.Default.Home)
    object Search : NavigationItem("search", "Search", Icons.Default.Search)
    object Tickets : NavigationItem("tickets", "Tickets", Icons.Default.Person)
    object Profile : NavigationItem("profile", "Profile", Icons.Default.Person)
}

@Composable
fun BottomNavigation(navController: NavHostController) {
    val items = listOf(
        NavigationItem.Home,
        NavigationItem.Search,
        NavigationItem.Tickets,
        NavigationItem.Profile
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(text = item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}


@Composable
fun ProfileScreen(viewModel: UserSessionViewModel, onLogout: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Perfil",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        when (uiState) {
            is LoginUiState.Success -> {
                val user = (uiState as LoginUiState.Success).user
                Text("Email: ${user.token}")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onLogout) {
                    Text("Cerrar Sesión")
                }
            }
            else -> {
                CircularProgressIndicator()
            }
        }
    }
}


@Composable
fun MyApp(viewModel: UserSessionViewModel, viewModelConcert: ConcertViewModel) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()

    // Observar cambios en el estado de autenticación
    LaunchedEffect(uiState) {
        when (uiState) {
            is LoginUiState.Initial, is LoginUiState.Error -> {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
            is LoginUiState.Success -> {
                if (navController.currentDestination?.route == Screen.Login.route) {
                    navController.navigate(NavigationItem.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }
            else -> {}
        }
    }

    Scaffold(
        bottomBar = {
            // Solo mostrar la barra de navegación si el usuario está autenticado
            if (uiState is LoginUiState.Success) {
                BottomNavigation(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(viewModel)
            }
            composable(NavigationItem.Home.route) {
                // Verificar si el usuario está autenticado
                if (uiState is LoginUiState.Success) {
                    ScrrenNew()
                }
            }

            composable(NavigationItem.Search.route) {


            }
            composable(NavigationItem.Tickets.route) {


            }
            composable(NavigationItem.Home.route) {
                // Verificar si el usuario está autenticado
                if (uiState is LoginUiState.Success) {
                    ConcertApp(viewModelConcert, navController)
                }
            }



            composable(NavigationItem.Profile.route) {
                if (uiState is LoginUiState.Success) {
                    ProfileScreen(
                        viewModel = viewModel,
                        onLogout = {
                            viewModel.logout()
                        }
                    )
                }
            }



            composable("concert_detail/{concertId}") { backStackEntry ->
                val concertId = backStackEntry.arguments?.getString("concertId") ?: return@composable
                ConcertDetailScreen(concertId = concertId, navController = navController, viewModelConcert)
            }

            composable("ticket_options/{concertId}") { backStackEntry ->
                val concertId = backStackEntry.arguments?.getString("concertId") ?: return@composable
                TicketOptionsScreen(concertId = concertId, viewModelConcert,onCheckout = { totalAmount ->
                    // Cuando el usuario finaliza la compra, navega a la pantalla de pago
                    navController.navigate("payment/$totalAmount")
                })
            }


            composable("payment/{totalAmount}") { backStackEntry ->
                val totalAmount = backStackEntry.arguments?.getString("totalAmount")?.toDoubleOrNull() ?: 0.0
                PaymentScreen(totalAmount = totalAmount, onPay = {
                    // Aquí procesarías el pago o mostrarías un mensaje de confirmación
                })
            }




        }
    }
}

@Composable
fun ScrrenNew() {
    Text("hola mundo")
}

@Composable
fun LoginScreen(
    viewModel: UserSessionViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("eve.holt@reqres.in") }
    var password by remember { mutableStateOf("cityslicka") }

    // UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { viewModel.login(email, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
    }

    // Observing the UI State for Login Result

}



//-----------------------------------------------------------------------------------------

@Composable
fun ConcertApp(viewModel: ConcertViewModel, navController: NavHostController) {
    val popularConcerts by viewModel.popularConcerts.collectAsState()
    val bestOffersConcerts by viewModel.bestOffersConcerts.collectAsState()
    val calendarConcerts by viewModel.calendarConcerts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            error?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Sección de Conciertos Populares
            SectionTitle("Conciertos Populares")
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(vertical = 8.dp)
            ) {
                items(popularConcerts) { concert ->
                    ConcertItem(concert) { concertId ->
                        navController.navigate("concert_detail/$concertId")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sección de Mejores Ofertas
            SectionTitle("Mejores Ofertas")
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(vertical = 8.dp)
            ) {
                items(bestOffersConcerts) { concert ->
                    ConcertItem(concert) { concertId ->
                        navController.navigate("concert_detail/$concertId")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sección de Calendario de Conciertos
            SectionTitle("Calendario de Conciertos")
            ExpandableConcertList(
                concerts = calendarConcerts,
                onConcertClick = { concertId ->
                    navController.navigate("concert_detail/$concertId")
                }
            )

            /*calendarConcerts.forEach { concert ->
                ConcertItem(
                    concert = concert,
                    onClick = { concertId ->
                        navController.navigate("concert_detail/$concertId")
                    },

                )
            }*/

            // Espacio adicional al final para evitar que el último elemento
            // quede oculto por la barra de navegación
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}





@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun ConcertsRow(concerts: List<Concert>, navController: NavHostController) {
    if (concerts.isEmpty()) {
        Text(
            text = "No hay conciertos disponibles",
            modifier = Modifier.padding(vertical = 8.dp)
        )
    } else {
        LazyRow(
            modifier = Modifier.height(220.dp)
        ) {
            items(concerts) { concert ->
                ConcertItem(concert) { concertId ->
                    // Navegar a la pantalla de detalles del concierto
                    navController.navigate("concert_detail/$concertId")
                }
            }
        }
    }
}

@Composable
fun ConcertItem(concert: Concert, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .width(250.dp)
            .padding(horizontal = 8.dp)
            .clickable { onClick(concert.id) }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = concert.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Género: ${concert.genre}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Fecha: ${concert.date}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Precio: $${concert.price}",
                style = MaterialTheme.typography.bodyMedium
            )
            concert.discount?.let {
                Text(
                    text = "Descuento: $it%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Imagen con Coil
            AsyncImage(
                model = concert.imageUrl,
                contentDescription = "Imagen de ${concert.name}",
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}


@Composable
fun ConcertDetailScreen(concertId: String, navController: NavHostController, viewModel: ConcertViewModel) {
    val concert by viewModel.loadConcertDetails(concertId).collectAsState(initial = null)

    concert?.let {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = it.name, style = MaterialTheme.typography.titleLarge)
            Text(text = "Fecha: ${it.date}")
            Text(text = "Descripción: ${it.venue}")

            Button(onClick = {
                navController.navigate("ticket_options/$concertId")
            }) {
                Text("Comprar Entrada")
            }
        }
    } ?: run {
        CircularProgressIndicator()
    }
}


@Composable
fun TicketOptionsScreen(concertId: String, viewModelConcert: ConcertViewModel, onCheckout: (Double) -> Unit) {
    val tickets by viewModelConcert.loadTicketsForConcert(concertId).collectAsState(initial = emptyList())

    // Estados para el total y la cantidad de tickets seleccionados
    var totalAmount by remember { mutableStateOf(0.0) }
    var totalSelectedTickets by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Opciones de Tickets para el Concierto ID: $concertId", style = MaterialTheme.typography.titleLarge)

        // Agrupar por tipo y mostrar la cantidad y precio
        val groupedTickets = tickets.groupBy { it.type }

        groupedTickets.forEach { (type, ticketList) ->
            val availableTickets = ticketList.filter { it.available }
            if (availableTickets.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "$type (${availableTickets.size} disponibles)", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(8.dp))

                    // Botón para seleccionar el tipo de ticket
                    Button(onClick = {
                        // Seleccionar un ticket disponible y actualizar el total y la cantidad
                        val selectedTicket = availableTickets.firstOrNull()
                        if (selectedTicket != null) {
                            // Marcar ticket como no disponible y actualizar el estado en el ViewModel
                            selectedTicket.available = false
                            totalAmount += selectedTicket.price
                            totalSelectedTickets += 1
                        }
                    }) {
                        Text(text = "Seleccionar")
                    }
                }

                availableTickets.forEach { ticket ->
                    Text(text = "Asiento ${ticket.seatNumber} - Precio: ${ticket.price}€")
                }
            } else {
                Text(text = "$type (No hay disponibles)")
            }
        }

        // Mostrar el total acumulado y la cantidad de tickets seleccionados al final de la pantalla
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Cantidad de tickets seleccionados: $totalSelectedTickets", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Total acumulado: $totalAmount€", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onCheckout(totalAmount) },
            enabled = totalSelectedTickets > 0
        ) {
            Text("Finalizar Compra")
        }
    }
}

@Composable
fun PaymentScreen(totalAmount: Double, onPay: () -> Unit) {
    var cardNumber by remember { mutableStateOf("") }
    var cardHolderName by remember { mutableStateOf("") }

    // Obtener el contexto actual
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Total: $$totalAmount",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TextField(
            value = cardNumber,
            onValueChange = { cardNumber = it },
            label = { Text("Card Number") },
            modifier = Modifier
                .fillMaxWidth()
                .focusable(true)
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = cardHolderName,
            onValueChange = { cardHolderName = it },
            label = { Text("Cardholder Name") },
            modifier = Modifier
                .fillMaxWidth()
                .focusable(true)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                // Mostrar un toast con el monto total y el mensaje "Success"
                Toast.makeText(context, "Success! Total: $$totalAmount", Toast.LENGTH_LONG).show()
                onPay()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Pay Now")
        }
    }
}


//calendar



@Composable
fun ExpandableConcertList(
    concerts: List<Concert>,
    onConcertClick: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val displayConcerts = if (isExpanded) concerts else concerts.take(2)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Mostrar los conciertos (2 o todos dependiendo del estado)
        displayConcerts.forEach { concert ->
            ConcertItemCalendar(
                concert = concert,
                onClick = onConcertClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }

        // Mostrar el botón "Ver más" solo si hay más de 2 conciertos
        if (concerts.size > 2) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { isExpanded = !isExpanded },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isExpanded) "Ver menos" else "Ver ${concerts.size - 2} conciertos más",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Icon(
                        imageVector = if (isExpanded)
                            Icons.Default.KeyboardArrowUp
                        else
                            Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                        modifier = Modifier.padding(start = 8.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun ConcertItemCalendar(
    concert: Concert,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick(concert.id) }
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del concierto
            AsyncImage(
                model = concert.imageUrl,
                contentDescription = "Imagen de ${concert.name}",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )

            // Información del concierto
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = concert.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Fecha: ${concert.date}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Género: ${concert.genre}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Precio y descuento
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$${concert.price}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                concert.discount?.let {
                    Text(
                        text = "-$it%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}