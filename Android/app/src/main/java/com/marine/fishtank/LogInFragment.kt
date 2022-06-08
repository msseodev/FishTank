package com.marine.fishtank

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.marine.fishtank.viewmodel.LogInViewModel

sealed class LogInEvent {
    data class SignIn(val userId: String, val password: String) : LogInEvent()
}

private const val TAG = "LogInFragment"
class LogInFragment : Fragment() {
    private val viewModel: LogInViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.connectResult.observe(viewLifecycleOwner) { connectResult ->
            if(connectResult) {
                Log.i(TAG, "Server connect success!")
            } else {
                Log.e(TAG, "FAIL to connect to server!")
                Toast.makeText(context, "FAIL to connect server!", Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.signInResult.observe(viewLifecycleOwner) { signIn ->
            if(signIn.result) {
                navigate(Screen.LogIn, Screen.FishTank)
            }
        }

        viewModel.connectToServer()

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    LogInScreen(viewModel) { event ->
                        when (event) {
                            is LogInEvent.SignIn -> {
                                viewModel.signIn(event.userId, event.password)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogInScreen(viewModel: LogInViewModel, onEvent: (LogInEvent) -> Unit) {
    var userIdText by rememberSaveable { mutableStateOf("") }
    var passwordText by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val enable by viewModel.connectResult.observeAsState(false)

    Surface(
        modifier = Modifier
        .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.loyal_blue))
                .padding(15.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = userIdText,
                maxLines = 1,
                label = { Text(text = "id") },
                placeholder = { Text("id") },
                onValueChange = { userIdText = it },
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = passwordText,
                maxLines = 1,
                onValueChange = { passwordText = it },
                placeholder = { Text("Password") },
                label = { Text(text = "password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    // Please provide localized description for accessibility services
                    val description = if (passwordVisible) "Hide password" else "Show password"

                    IconButton(onClick = {passwordVisible = !passwordVisible}){
                        Icon(imageVector  = image, description)
                    }
                }
            )

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onEvent(LogInEvent.SignIn(userIdText, passwordText)) },
                enabled = enable
            ) {
                Text(text = "SIGN IN")
            }
        }
    }
}