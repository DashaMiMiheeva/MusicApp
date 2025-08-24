package com.example.musicapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.viewModelScope
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.musicapp.ApiClient.songApi
import com.example.musicapp.ui.theme.MusicAppTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {
    private val darkThemeState = mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicAppTheme(darkTheme = darkThemeState.value) {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = Color.Black) {
                    val navController = rememberNavController()
                    val context = LocalContext.current
                    val dataStoreManager = DataStoreManager(context)
                    val viewModel = MusicViewModel(application, songApi, dataStoreManager)
                    val isAuthorized by viewModel.isAuthorized.collectAsState()

                    viewModel.loadNickname()

                    Scaffold(
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                        bottomBar = {
                            val currentSong = (viewModel.premiereSongs + viewModel.songsByGenre)
                                .find { it.id == viewModel.currentlyPlayingId }

                            if (currentSong != null && isAuthorized) {
                                NowPlayingBar(
                                    song = currentSong,
                                    isPlaying = viewModel.isPlaying,
                                    onPlayPauseClick = { viewModel.onPlayPauseClick(context, currentSong) },
                                    isFavorite = viewModel.isFavorite(currentSong),
                                    onFavoriteClick = { viewModel.toggleFavorite(currentSong)},
                                    navController = navController
                                )

                            }
                        }
                    ) { padding ->
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Welcome.route,
                            modifier = Modifier.padding(padding)
                        ) {
                            composable(Screen.Welcome.route) {
                                WelcomeScreen(darkThemeState, navController)
                            }
                            composable(Screen.SignIn.route) {
                                SignInScreen(navController)
                            }
                            composable(Screen.SignUp.route) {
                                SignUpScreen(darkThemeState, navController)
                            }
                            composable(Screen.Home.route) {
                                HomeScreen(darkThemeState, navController, viewModel)
                            }
                            composable("/by-genre/{genre}") { backStackEntry ->
                                val genre = backStackEntry.arguments?.getString("genre") ?: ""
                                GenreScreen(genre = genre, viewModel = viewModel, navController = navController)
                            }
                            composable("player") {
                                PlayerScreen(viewModel = viewModel, navController = navController)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeScreen(darkThemeState: MutableState<Boolean>, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.musiumlogo),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(300.dp),
            contentScale = ContentScale.Fit
        )
        Text(
            text = "Добро пожаловать!",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 50.dp)
        )
        Button(
            onClick = { navController.navigate(Screen.SignIn.route) },
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .width(298.dp)
                .height(50.dp)
        ) {
            Text(text = "Войти с помощью пароля", color = MaterialTheme.colorScheme.background, fontSize = 16.sp)
        }
        TextButton(onClick = { navController.navigate(Screen.SignUp.route) }) {
            Text(
                text = "Нет аккаунта? Зарегистрируйтесь!",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 5.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(navController: NavController) {
    val context = LocalContext.current
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val passwordVisible = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)

        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back Arrow",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .size(35.dp)
            )
        }
        Image(
            painter = painterResource(id = R.drawable.musiumlogo),
            contentDescription = "App Logo",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(200.dp),
            contentScale = ContentScale.Fit
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 200.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Войдите в свой аккаунт",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 60.dp)
            )

            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text("Почта", color = MaterialTheme.colorScheme.surface) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email Icon"
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 25.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            OutlinedTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = { Text("Пароль", color = MaterialTheme.colorScheme.surface) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password Icon"
                    )
                },
                trailingIcon = {
                    val image = if (passwordVisible.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                        Icon(imageVector = image,
                            contentDescription = if (passwordVisible.value) "Hide Password" else "Show Password")
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 50.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.primary,

                    ),
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Button(
                onClick = {
                    if (email.value.isNotEmpty() && password.value.isNotEmpty()) {
                        loginUser(email.value, password.value, context, navController)
                    } else {
                        Toast.makeText(context, "Заполните все поля", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(text = "Войти", color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp)
            }
        }

        TextButton(
            onClick = { navController.navigate(Screen.SignUp.route) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)) {
            Text(
                text = "Нет аккаунта? Зарегистрируйтесь!",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(top = 16.dp)
            )
        }
    }
}

fun loginUser(email: String, password: String, context: Context, navController: NavController){
    val call = ApiClient.authApi.login(LoginRequest(email, password))

    call.enqueue(object : Callback<AuthResponse> {
        override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
            if (response.isSuccessful) {
                val usernameFromServer = response.body()?.nickname ?: "Гость"
                val dataStoreManager = DataStoreManager(context)

                CoroutineScope(Dispatchers.IO).launch {
                    dataStoreManager.saveUser(email, usernameFromServer)
                }

                Toast.makeText(context, "Успешный вход!", Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.SignIn.route) { inclusive = true }
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Toast.makeText(context, "Ошибка входа: ${errorBody ?: "неизвестная ошибка"}", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onFailure(call: Call<AuthResponse>, t: Throwable){
            Toast.makeText(context,  "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
        }

    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(darkThemeState: MutableState<Boolean>, navController: NavController) {
    val context = LocalContext.current
    val username = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val passwordVisible = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)

        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back Arrow",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .size(35.dp)
            )
        }

        Image(
            painter = painterResource(id = R.drawable.musiumlogo),
            contentDescription = "App Logo",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(200.dp),
            contentScale = ContentScale.Fit
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 200.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Создайте свой аккаунт",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 60.dp)
            )

            OutlinedTextField(
                value = username.value,
                onValueChange = { username.value = it },
                label = { Text("Имя пользователя", color = MaterialTheme.colorScheme.surface) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Nickname Icon"
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 25.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.primary,
                ),
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text("Почта", color = MaterialTheme.colorScheme.surface) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email Icon"
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 25.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.primary,
                ),
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            OutlinedTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = { Text("Пароль", color = MaterialTheme.colorScheme.surface) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password Icon"
                    )
                },
                trailingIcon = {
                    val image = if (passwordVisible.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                        Icon(imageVector = image, contentDescription = if (passwordVisible.value) "Hide Password" else "Show Password")
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 50.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.primary,

                    ),
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Button(
                onClick = {
                    if (email.value.isNotEmpty() && password.value.isNotEmpty() && username.value.isNotEmpty()) {
                        registerUser(email.value, password.value, username.value, context, navController)
                    } else {
                        Toast.makeText(context, "Заполните все поля", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(text = "Войти", color = MaterialTheme.colorScheme.background, fontSize = 16.sp)
            }
        }
    }
}

fun registerUser(email: String, password: String, nickname: String, context: Context, navController: NavController){
    val call = ApiClient.authApi.register(RegisterRequest(email, password, nickname))

    call.enqueue(object : Callback<AuthResponse> {
        override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
            if (response.isSuccessful) {
                val usernameFromServer = response.body()?.nickname ?: "Гость"

                val dataStoreManager = DataStoreManager(context)

                CoroutineScope(Dispatchers.IO).launch {
                    dataStoreManager.saveUser(email, usernameFromServer)
                }

                Toast.makeText(context, "Успешная регистрация!", Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.SignUp.route) { inclusive = true }
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Toast.makeText(context, "Ошибка входа: ${errorBody ?: "неизвестная ошибка"}", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onFailure(call: Call<AuthResponse>, t: Throwable){
            Toast.makeText(context,  "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            Log.d(ContentValues.TAG, "ERROR: ${t.message.toString()}")
        }
    })
}

@SuppressLint("SourceLockedOrientationActivity", "UnrememberedMutableState")
@Composable
fun HomeScreen(
    darkThemeState: MutableState<Boolean>,
    navController: NavController,
    viewModel: MusicViewModel
) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val activity = LocalContext.current as? Activity
    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            BottomNavigationBar(selectedTab) { selectedTab = it }
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp)
                    ) {
                        item { WelcomeSection(darkThemeState, navController, viewModel) }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                        item { SectionTitle("Премьера") }
                        item { PremiereSection(viewModel) }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                        item { SectionTitle("Топ исполнителей") }
                        item { ArtistsSection(viewModel) }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                        item { SectionTitle("Топ жанров") }
                        item { GenresSection(navController) }
                    }
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }

                1 -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp)
                    ) {
                        SearchScreen(context, viewModel)
                    }
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }

                2 -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp)
                    ) {
                        LibraryScreen(viewModel)
                    }
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            }
        }
    }
}

@Composable
fun WelcomeSection(darkThemeState: MutableState<Boolean>, navController: NavController, viewModel: MusicViewModel) {
    var expanded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("С возвращением!", color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(viewModel.nickname.value, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Настройки", tint = MaterialTheme.colorScheme.onBackground)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.secondary)
                ) {

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Темная тема", color = MaterialTheme.colorScheme.onBackground)
                                Spacer(modifier = Modifier.width(8.dp))
                                Switch(
                                    checked = darkThemeState.value,
                                    onCheckedChange = { darkThemeState.value = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.Cyan,
                                        uncheckedThumbColor = Color.Gray
                                    )
                                )
                            }
                        },
                        onClick = {}
                    )

                    DropdownMenuItem(
                        text = { Text("Выйти", color = MaterialTheme.colorScheme.onError) },
                        onClick = {
                            expanded = false
                            viewModel.logout {
                                navController.navigate(Screen.Welcome.route) {
                                    popUpTo(Screen.SignIn.route) { inclusive = true }
                                    popUpTo(Screen.SignUp.route) { inclusive = true }
                                }
                                Toast.makeText(context, "Вы вышли из аккаунта!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(title, color = MaterialTheme.colorScheme.onBackground, fontSize = 21.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
fun PremiereSection(viewModel: MusicViewModel) {
    val songs = viewModel.premiereSongs
    val context = LocalContext.current

    val columns = songs.chunked(3)

    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(columns) { songGroup ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                songGroup.forEach { song ->
                    SongCard(
                        song = song,
                        isPlaying = viewModel.isPlaying && viewModel.currentlyPlayingId == song.id,
                        onPlayPauseClick = { viewModel.onPlayPauseClick(context, song) }
                    )
                }
            }
        }
    }
}

@Composable
fun SongCard(song: Song, isPlaying: Boolean, onPlayPauseClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(250.dp)
            .height(75.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.scrim)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.coverUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.surface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onPlayPauseClick) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun ArtistsSection(viewModel: MusicViewModel) {
    val artists = viewModel.topArtists.filter {
        it.coverArtist.startsWith("http")
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(artists) { artist ->
            ArtistCard(artist)
        }
    }
}

@Composable
fun ArtistCard(artist: ArtistDB) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(120.dp)
    ) {
        AsyncImage(
            model = artist.coverArtist,
            contentDescription = artist.artist,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .background(Color.White)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = artist.artist,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}


@Composable
fun GenresSection(navController: NavController) {
    val genres = listOf(
        GenreItem("Русский реп", colorResource(id = R.color.light_green)),
        GenreItem("Поп", colorResource(id = R.color.pink)),
        GenreItem("Фонк", colorResource(id = R.color.my_purple)),
        GenreItem("Рок", colorResource(id = R.color.my_blue))
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(genres) { genre ->
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(55.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(genre.color)
                        .clickable {
                            navController.navigate("/by-genre/${genre.name}")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = genre.name,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GenreScreen(genre: String, viewModel: MusicViewModel, navController: NavController) {
    val songs = viewModel.songsByGenre
    val context = LocalContext.current

    LaunchedEffect(genre) {
        viewModel.fetchSongsByGenre(genre)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { navController.popBackStack() }
                .padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Назад",
                tint = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Жанр: $genre",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(songs) { song ->
                GenreCard(
                    song = song,
                    isPlaying = viewModel.isPlaying && viewModel.currentlyPlayingId == song.id,
                    onPlayPauseClick = { viewModel.onPlayPauseClick(context, song) }
                )
            }
        }
    }
}

@Composable
fun GenreCard(song: Song, isPlaying: Boolean, onPlayPauseClick: () -> Unit) {
    Card(
        modifier = Modifier
            .height(75.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.scrim)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.coverUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.surface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onPlayPauseClick) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun NowPlayingBar(
    song: Song,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp)
            .clickable {
                navController.navigate("player")
                       },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.scrim),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            AsyncImage(
                model = song.coverUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))

            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = song.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = song.artist,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onPlayPauseClick) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}



@Composable
fun PlayerScreen(
    viewModel: MusicViewModel,
    navController: NavController
) {
    val song = viewModel.currentSong

    if (song == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val isPlaying = viewModel.isPlaying
    val playbackPosition = viewModel.playbackProgress.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.Default.Close, contentDescription = "Закрыть")
        }

        AsyncImage(
            model = song.coverUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(song.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(song.artist, fontSize = 16.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        Slider(
            value = playbackPosition,
            onValueChange = { viewModel.seekToPercent(it) },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = { viewModel.playPreviousSong() }) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Предыдущая")
            }
            IconButton(onClick = { viewModel.togglePlayPause() }) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null
                )
            }
            IconButton(onClick = { viewModel.playNextSong() }) {
                Icon(Icons.Default.SkipNext, contentDescription = "Следующая")
            }
        }
    }
}


@Composable
fun BottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {

    NavigationBar(containerColor = MaterialTheme.colorScheme.onPrimary) {
        NavigationBarItem(
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onBackground,
                unselectedTextColor = MaterialTheme.colorScheme.onBackground
            ),
            icon = { Icon(Icons.Filled.Home, contentDescription = "Для вас") },
            label = { Text("Для вас") },
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) }
        )
        NavigationBarItem(
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onBackground,
                unselectedTextColor = MaterialTheme.colorScheme.onBackground
            ),
            icon = { Icon(Icons.Default.Search, contentDescription = "Поиск") },
            label = { Text("Поиск") },
            selected = selectedTab == 1,
            onClick = {
                onTabSelected(1)
            }
        )
        NavigationBarItem(
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onBackground,
                unselectedTextColor = MaterialTheme.colorScheme.onBackground
            ),
            icon = { Icon(Icons.Default.Folder, contentDescription = "Библиотека") },
            label = { Text("Библиотека") },
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) }
        )
    }
}

class SearchHistoryManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getHistory(): List<String> {
        val json = prefs.getString("search_history", "[]") ?: "[]"
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type)
    }

    fun addToHistory(query: String) {
        val history = getHistory().toMutableList()
        history.remove(query)
        history.add(0, query)
        if (history.size > 10) history.removeAt(history.size - 1)
        prefs.edit().putString("search_history", gson.toJson(history)).apply()
    }

    fun clearHistory() {
        prefs.edit().remove("search_history").apply()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(context: Context, viewModel: MusicViewModel) {
    var searchText = rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    val historyManager = remember { SearchHistoryManager(context) }
    var searchHistory by remember { mutableStateOf(historyManager.getHistory()) }
    var isHistoryVisible by remember { mutableStateOf(false) }
    var isManualSearch by remember { mutableStateOf(false) }

    val searchResults = viewModel.searchResults

    fun performSearch(query: String) {
        coroutineScope.launch {
            isLoading = true
            isError = false
            try {
                println("Выполняется поиск по запросу: $query")
                viewModel.searchSongs(query)
                historyManager.addToHistory(query)
                searchHistory = historyManager.getHistory()
            } catch (e: Exception) {
                isError = true
                println("Ошибка: ${e.message}")
        } finally {
            isLoading = false
            isHistoryVisible = false
            }
        }
    }

    LaunchedEffect(searchText.value) {
        if (searchText.value.isNotEmpty() && !isManualSearch) {
            delay(2000)
            performSearch(searchText.value)
        }
        isManualSearch = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(5.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.musiumlogo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(70.dp),
                contentScale = ContentScale.Fit
            )

            Text("Поиск", color = MaterialTheme.colorScheme.primary, fontSize = 27.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        SearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            query = searchText.value,
            onQueryChange = { text ->
                searchText.value = text
                isHistoryVisible = text.isEmpty()},
            onSearch = {
                keyboardController?.hide()
                performSearch(searchText.value)
                isManualSearch = true}
            ,
            placeholder = {
                Text(
                    text = "Песни, Артисты, Подкасты & другое",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            },
            colors = SearchBarDefaults.colors(
                containerColor = MaterialTheme.colorScheme.onBackground,
                inputFieldColors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.background
                )
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon"
                )
            },
            trailingIcon = {
                if (searchText.value.isNotEmpty()) {
                    IconButton(onClick = {
                        searchText.value = ""
                        isError = false
                        keyboardController?.hide()
                        isHistoryVisible = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear Icon",
                            tint = MaterialTheme.colorScheme.surface
                        )
                    }
                }
            },
            shape = MaterialTheme.shapes.medium,
            active = false,
            onActiveChange = {
                isHistoryVisible = true
                searchHistory = historyManager.getHistory()
            }
        ) {}

        if (isHistoryVisible && searchHistory.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)
            ) {
                searchHistory.forEach { historyItem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                searchText.value = historyItem
                                performSearch(historyItem)
                                isManualSearch = true
                                isHistoryVisible = false
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.History, contentDescription = "History Icon")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(historyItem, fontSize = 14.sp)
                    }
                }
                Text(
                    "Очистить историю",
                    color = MaterialTheme.colorScheme.onError,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            historyManager.clearHistory()
                            searchHistory = emptyList()
                            isHistoryVisible = false
                        }
                        .padding(8.dp)
                )
            }
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            isError && searchText.value.isNotEmpty() -> {
                ErrorPlaceholder(onRetry = { performSearch(searchText.value) })
            }

            searchText.value.isEmpty() -> {}


            searchResults.isEmpty() -> {
                NoResultsPlaceholder()
            }

            else -> {
                Box(modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                ) {
                    LazyColumn (
                        contentPadding = PaddingValues(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ){
                        items(searchResults) { song ->
                            println("Добавляется песня в список: ${song.title}")
                            GenreCard(
                                song = song,
                                isPlaying = viewModel.isPlaying && viewModel.currentlyPlayingId == song.id,
                                onPlayPauseClick = { viewModel.onPlayPauseClick(context, song) }
                            )
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun LibraryScreen(viewModel: MusicViewModel) {
    val context = LocalContext.current
    val favoriteSongs = viewModel.favoriteSongs
    val isLoading = viewModel.isFavoritesLoading
    val error = viewModel.favoritesError

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.musiumlogo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(70.dp),
                contentScale = ContentScale.Fit
            )

            Text("Ваша библиотека", color = MaterialTheme.colorScheme.primary, fontSize = 27.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Ваши любимые песни",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .padding(5.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        when {
            isLoading -> {
                CircularProgressIndicator()
            }
            error != null -> {
                Text(text = error)
            }
            favoriteSongs.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Нет избранных песен")
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favoriteSongs) { song ->
                        GenreCard(
                            song = song,
                            isPlaying = viewModel.isPlaying && viewModel.currentlyPlayingId == song.id,
                            onPlayPauseClick = { viewModel.onPlayPauseClick(context, song) }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun NoResultsPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = "No Results",
            tint = MaterialTheme.colorScheme.surface,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Ничего не найдено",
            color = MaterialTheme.colorScheme.surface,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "Попробуйте изменить запрос",
            color = MaterialTheme.colorScheme.surface,
            fontSize = 14.sp
        )
    }
}

@Composable
fun ErrorPlaceholder(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.onError,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Произошла ошибка",
            color = MaterialTheme.colorScheme.onError,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "Проверьте подключение к интернету",
            color = MaterialTheme.colorScheme.surface,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onBackground
            )
        ) {
            Text("Обновить")
        }
    }
}


