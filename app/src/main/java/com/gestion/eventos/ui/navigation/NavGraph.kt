package com.gestion.eventos.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.CircularProgressIndicator
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gestion.eventos.data.model.Comment
import com.gestion.eventos.data.model.Rating
import com.gestion.eventos.ui.screens.CreateEventScreen
import com.gestion.eventos.ui.screens.EditEventScreen
import com.gestion.eventos.ui.screens.EventDetailScreen
import com.gestion.eventos.ui.screens.EventListScreen
import com.gestion.eventos.ui.screens.LoginScreen
import com.gestion.eventos.ui.screens.ProfileScreen
import com.gestion.eventos.ui.screens.ShareHelper
import com.gestion.eventos.ui.screens.SignUpScreen
import com.gestion.eventos.ui.viewmodel.AuthViewModel
import com.gestion.eventos.ui.viewmodel.EventViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object EventList : Screen("event_list")
    object EventDetail : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
    object CreateEvent : Screen("create_event")
    object EditEvent : Screen("edit_event/{eventId}") {
        fun createRoute(eventId: String) = "edit_event/$eventId"
    }
    object Profile : Screen("profile")
}

@Composable
fun NavGraph(
    authViewModel: AuthViewModel,
    eventViewModel: EventViewModel,
    navController: NavHostController = rememberNavController(),
    onGoogleSignInClick: () -> Unit = {}
) {
    val authState by authViewModel.uiState.collectAsState()
    val eventState by eventViewModel.uiState.collectAsState()

    LaunchedEffect(authState.isAuthenticated) {
        val currentRoute = navController.currentDestination?.route
        if (authState.isAuthenticated) {
            // Si el usuario se autenticó, navegar a la lista de eventos
            if (currentRoute == Screen.Login.route || currentRoute == Screen.SignUp.route) {
                navController.navigate(Screen.EventList.route) {
                    popUpTo(0) { 
                        inclusive = true 
                    }
                }
            }
        } else if (!authState.isAuthenticated && 
                   currentRoute != Screen.Login.route && 
                   currentRoute != Screen.SignUp.route) {
            // Si el usuario no está autenticado y no está en login/signup, volver a login
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { 
                    inclusive = true 
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (authState.isAuthenticated) Screen.EventList.route else Screen.Login.route
    ) {
        composable(route = Screen.Login.route) {
            LoginScreen(
                onLoginClick = { email, password ->
                    authViewModel.signInWithEmail(email, password)
                },
                onSignUpClick = {
                    navController.navigate(Screen.SignUp.route)
                },
                onGoogleSignInClick = onGoogleSignInClick,
                isLoading = authState.isLoading,
                errorMessage = authState.errorMessage,
                onDismissError = { authViewModel.clearError() }
            )
        }

        composable(route = Screen.SignUp.route) {
            SignUpScreen(
                onSignUpClick = { email, password, name ->
                    authViewModel.signUpWithEmail(email, password, name)
                },
                onBackClick = {
                    navController.popBackStack()
                },
                isLoading = authState.isLoading,
                errorMessage = authState.errorMessage,
                onDismissError = { authViewModel.clearError() }
            )
        }

        composable(route = Screen.EventList.route) {
            EventListScreen(
                events = eventState.events,
                upcomingEvents = eventState.upcomingEvents,
                pastEvents = eventState.pastEvents,
                onEventClick = { eventId ->
                    eventViewModel.loadEventDetails(eventId)
                    navController.navigate(Screen.EventDetail.createRoute(eventId))
                },
                onCreateEventClick = {
                    navController.navigate(Screen.CreateEvent.route)
                },
                onSignOutClick = {
                    authViewModel.signOut()
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                isLoading = eventState.isLoading,
                onRefresh = {
                    eventViewModel.loadUpcomingEvents()
                    eventViewModel.loadPastEvents()
                    eventViewModel.loadEvents()
                }
            )
        }

        composable(
            route = Screen.EventDetail.route,
            arguments = listOf(
                navArgument("eventId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            val event = eventState.currentEvent
            val currentUserId = authState.currentUser?.id
            val context = LocalContext.current

            LaunchedEffect(eventId) {
                // Cargar detalles del evento, comentarios y valoración del usuario
                eventViewModel.loadEventDetails(eventId)
                if (currentUserId != null) {
                    eventViewModel.loadUserRating(eventId, currentUserId)
                }
            }

            EventDetailScreen(
                event = event,
                comments = eventState.comments,
                userRating = eventState.userRating,
                currentUserId = currentUserId,
                isAttending = event?.attendees?.contains(currentUserId ?: "") ?: false,
                onBackClick = {
                    navController.popBackStack()
                },
                onJoinClick = {
                    currentUserId?.let { userId ->
                        eventViewModel.joinEvent(eventId, userId)
                    }
                },
                onLeaveClick = {
                    currentUserId?.let { userId ->
                        eventViewModel.leaveEvent(eventId, userId)
                    }
                },
                onAddComment = { text ->
                    currentUserId?.let { userId ->
                        val comment = Comment(
                            eventId = eventId,
                            userId = userId,
                            userName = authState.currentUser?.name ?: "Usuario",
                            userPhotoUrl = authState.currentUser?.photoUrl,
                            text = text
                        )
                        eventViewModel.addComment(comment)
                    }
                },
                onAddRating = { rating ->
                    currentUserId?.let { userId ->
                        val ratingObj = Rating(
                            eventId = eventId,
                            userId = userId,
                            rating = rating
                        )
                        eventViewModel.addRating(ratingObj)
                    }
                },
                onShareClick = {
                    event?.let { eventData ->
                        ShareHelper.shareEvent(context, eventData)
                    }
                },
                isLoading = eventState.isLoading,
                errorMessage = eventState.errorMessage,
                onDismissError = { eventViewModel.clearError() }
            )
        }

        composable(route = Screen.CreateEvent.route) {
            CreateEventScreen(
                organizerName = authState.currentUser?.name ?: "",
                onBackClick = {
                    navController.popBackStack()
                },
                onCreateEvent = { event, imageUri ->
                    authState.currentUser?.id?.let { userId ->
                        eventViewModel.createEvent(event, userId, imageUri)
                        navController.popBackStack()
                    }
                },
                isLoading = eventState.isLoading,
                errorMessage = eventState.errorMessage,
                onDismissError = { eventViewModel.clearError() }
            )
        }

        composable(
            route = Screen.EditEvent.route,
            arguments = listOf(
                navArgument("eventId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            val event = eventState.currentEvent

            LaunchedEffect(eventId) {
                if (event?.id != eventId) {
                    eventViewModel.loadEventDetails(eventId)
                }
            }

            event?.let { eventData ->
                EditEventScreen(
                    event = eventData,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onUpdateEvent = { updatedEvent, imageUri ->
                        eventViewModel.updateEvent(updatedEvent, imageUri)
                        navController.popBackStack()
                    },
                    isLoading = eventState.isLoading,
                    errorMessage = eventState.errorMessage,
                    onDismissError = { eventViewModel.clearError() }
                )
            } ?: run {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        composable(route = Screen.Profile.route) {
            LaunchedEffect(Unit) {
                eventViewModel.loadEvents()
                authState.currentUser?.id?.let { userId ->
                    // Cargar eventos del usuario desde el repositorio
                }
            }

            val currentUser = authState.currentUser
            val myEvents = eventState.events.filter { it.organizerId == currentUser?.id }
            val participatingEvents = eventState.events.filter { currentUser?.id?.let { id -> id in it.attendees } == true }

            ProfileScreen(
                user = currentUser,
                myEvents = myEvents,
                participatingEvents = participatingEvents,
                isLoading = authState.isLoading || eventState.isLoading,
                message = authState.infoMessage,
                errorMessage = authState.errorMessage,
                onBackClick = { navController.popBackStack() },
                onSignOutClick = { authViewModel.signOut() },
                onUpdateProfile = { name, photoUri ->
                    authViewModel.updateProfile(name, photoUri)
                },
                onChangePassword = { newPassword ->
                    authViewModel.updatePassword(newPassword)
                },
                onDismissMessage = { authViewModel.clearInfoMessage() },
                onDismissError = { authViewModel.clearError() },
                onEventClick = { eventId ->
                    eventViewModel.loadEventDetails(eventId)
                    navController.navigate(Screen.EventDetail.createRoute(eventId))
                },
                onEditEventClick = { eventId ->
                    eventViewModel.loadEventDetails(eventId)
                    navController.navigate(Screen.EditEvent.createRoute(eventId))
                }
            )
        }
    }
}
