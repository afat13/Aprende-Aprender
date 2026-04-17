package com.example.aprendeaprender.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aprendeaprender.R
import com.example.aprendeaprender.ui.components.AppButton
import com.example.aprendeaprender.ui.theme.CyanAccent
import com.example.aprendeaprender.ui.theme.DarkBackground
import com.example.aprendeaprender.ui.theme.DarkSurface
import com.example.aprendeaprender.ui.theme.ErrorRed
import com.example.aprendeaprender.ui.theme.TextGray
import com.example.aprendeaprender.ui.theme.TextWhite
import com.example.aprendeaprender.viewmodel.ProfileUiState

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onNombreChange: (String) -> Unit,
    onApellidoChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit,
    onGuardarClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            TextButton(
                onClick = onBackClick,
                modifier = Modifier.padding(start = 0.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.back_label),
                    color = CyanAccent,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(id = R.string.profile_title),
                color = TextWhite,
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.profile_subtitle),
                color = TextGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            ProfileTextField(
                value = uiState.nombre,
                label = stringResource(id = R.string.first_name_label),
                onValueChange = onNombreChange,
                enabled = !uiState.guardando
            )

            Spacer(modifier = Modifier.height(14.dp))

            ProfileTextField(
                value = uiState.apellido,
                label = stringResource(id = R.string.last_name_label),
                onValueChange = onApellidoChange,
                enabled = !uiState.guardando
            )

            Spacer(modifier = Modifier.height(14.dp))

            ProfileTextField(
                value = uiState.correo,
                label = stringResource(id = R.string.email_label),
                onValueChange = {},
                enabled = false,
                readOnly = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(id = R.string.profile_email_not_editable),
                color = TextGray,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            ProfileTextField(
                value = uiState.telefono,
                label = stringResource(id = R.string.phone_label),
                onValueChange = onTelefonoChange,
                enabled = !uiState.guardando,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(id = R.string.profile_phone_helper),
                color = TextGray,
                fontSize = 12.sp
            )

            uiState.mensajeErrorResId?.let { resId ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(id = resId),
                    color = ErrorRed,
                    fontSize = 13.sp
                )
            }

            uiState.mensajeExitoResId?.let { resId ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(id = resId),
                    color = CyanAccent,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AppButton(
                text = if (uiState.guardando) {
                    stringResource(id = R.string.saving_label)
                } else {
                    stringResource(id = R.string.profile_save_button)
                },
                onClick = onGuardarClick,
                enabled = !uiState.guardando && !uiState.cargando
            )

            if (uiState.cargando) {
                Spacer(modifier = Modifier.height(20.dp))
                CircularProgressIndicator(color = CyanAccent)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileTextField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(
                text = label,
                color = TextGray
            )
        },
        enabled = enabled,
        readOnly = readOnly,
        singleLine = true,
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = DarkSurface,
            unfocusedContainerColor = DarkSurface,
            disabledContainerColor = DarkSurface,
            focusedTextColor = TextWhite,
            unfocusedTextColor = TextWhite,
            disabledTextColor = TextGray,
            focusedBorderColor = CyanAccent,
            unfocusedBorderColor = TextGray,
            disabledBorderColor = TextGray,
            focusedLabelColor = CyanAccent,
            unfocusedLabelColor = TextGray,
            disabledLabelColor = TextGray,
            cursorColor = CyanAccent
        )
    )
}