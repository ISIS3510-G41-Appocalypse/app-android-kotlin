package com.gn41.appandroidkotlin.presentation.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
//import androidx.compose.material3.menuAnchor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gn41.appandroidkotlin.presentation.viewmodels.RegisterViewModel
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBackClick: () -> Unit,
    onRegistrationSuccess: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Crear cuenta",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Regístrate en Happy Ride",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Paso ${viewModel.currentStep} de 4",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            when (viewModel.currentStep) {

                1 -> {

                    CustomOutlinedTextField(
                        value = viewModel.firstName,
                        onValueChange = viewModel::onFirstNameChange,
                        label = "Nombre",
                        isError = viewModel.firstNameInputError.isNotEmpty(),
                        errorMessage = viewModel.firstNameInputError,
                        keyboardType = KeyboardType.Text
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CustomOutlinedTextField(
                        value = viewModel.lastName,
                        onValueChange = viewModel::onLastNameChange,
                        label = "Apellido",
                        isError = viewModel.lastNameInputError.isNotEmpty(),
                        errorMessage = viewModel.lastNameInputError,
                        keyboardType = KeyboardType.Text
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CustomOutlinedTextField(
                        value = viewModel.email,
                        onValueChange = viewModel::onEmailChange,
                        label = "Correo institucional",
                        isError = viewModel.emailInputError.isNotEmpty(),
                        errorMessage = viewModel.emailInputError,
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CustomOutlinedTextField(
                        value = viewModel.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = "Contraseña",
                        isError = viewModel.passwordInputError.isNotEmpty(),
                        errorMessage = viewModel.passwordInputError,
                        keyboardType = KeyboardType.Password,
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CustomOutlinedTextField(
                        value = viewModel.confirmPassword,
                        onValueChange = viewModel::onConfirmPasswordChange,
                        label = "Confirmar contraseña",
                        isError = viewModel.confirmPasswordInputError.isNotEmpty(),
                        errorMessage = viewModel.confirmPasswordInputError,
                        keyboardType = KeyboardType.Password,
                        visualTransformation = PasswordVisualTransformation()
                    )
                }

                2 -> {

                    Text(
                        text = "¿Cómo quieres usar Happy Ride?",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    RoleSelectionCard(
                        title = "Conductor",
                        description = "Ofrece wheels a otros estudiantes",
                        selected = viewModel.selectedRole == "driver",
                        onClick = {
                            viewModel.onRoleSelected("driver")
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    RoleSelectionCard(
                        title = "Pasajero",
                        description = "Encuentra wheels disponibles",
                        selected = viewModel.selectedRole == "rider",
                        onClick = {
                            viewModel.onRoleSelected("rider")
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    RoleSelectionCard(
                        title = "Ambos",
                        description = "Conduce y encuentra wheels",
                        selected = viewModel.selectedRole == "both",
                        onClick = {
                            viewModel.onRoleSelected("both")
                        }
                    )

                    if (viewModel.roleSelectionError.isNotEmpty()) {

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = viewModel.roleSelectionError,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                3 -> {

                    Text(
                        text = "Selecciona tu zona preferida",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    ZoneDropdown(viewModel)

                    if (viewModel.zoneSelectionError.isNotEmpty()) {

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = viewModel.zoneSelectionError,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                4 -> {

                    if (
                        viewModel.selectedRole == "driver" ||
                        viewModel.selectedRole == "both"
                    ) {

                        Text(
                            text = "Información del vehículo",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        CustomOutlinedTextField(
                            value = viewModel.vehicleLicensePlate,
                            onValueChange = viewModel::onVehicleLicensePlateChange,
                            label = "Placa",
                            isError = viewModel.vehicleLicensePlateError.isNotEmpty(),
                            errorMessage = viewModel.vehicleLicensePlateError,
                            keyboardType = KeyboardType.Text
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        CustomOutlinedTextField(
                            value = viewModel.vehicleNumberSlots,
                            onValueChange = viewModel::onVehicleNumberSlotsChange,
                            label = "Número de cupos",
                            isError = viewModel.vehicleNumberSlotsError.isNotEmpty(),
                            errorMessage = viewModel.vehicleNumberSlotsError,
                            keyboardType = KeyboardType.Number
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        CustomOutlinedTextField(
                            value = viewModel.vehicleBrand,
                            onValueChange = viewModel::onVehicleBrandChange,
                            label = "Marca",
                            isError = viewModel.vehicleBrandError.isNotEmpty(),
                            errorMessage = viewModel.vehicleBrandError,
                            keyboardType = KeyboardType.Text
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        CustomOutlinedTextField(
                            value = viewModel.vehicleModel,
                            onValueChange = viewModel::onVehicleModelChange,
                            label = "Modelo",
                            isError = viewModel.vehicleModelError.isNotEmpty(),
                            errorMessage = viewModel.vehicleModelError,
                            keyboardType = KeyboardType.Text
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        CustomOutlinedTextField(
                            value = viewModel.vehicleColor,
                            onValueChange = viewModel::onVehicleColorChange,
                            label = "Color",
                            isError = viewModel.vehicleColorError.isNotEmpty(),
                            errorMessage = viewModel.vehicleColorError,
                            keyboardType = KeyboardType.Text
                        )
                    } else {

                        Text(
                            text = "Todo listo para registrarte",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (viewModel.registrationError.isNotEmpty()) {

                Text(
                    text = viewModel.registrationError,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                if (viewModel.currentStep > 1) {

                    Button(
                        onClick = {
                            viewModel.previousStep()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Atrás")
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                }

                Button(
                    onClick = {

                        if (viewModel.currentStep < 4) {
                            viewModel.nextStep()
                        } else {
                            viewModel.onRegisterClick(
                                onRegistrationSuccess
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !viewModel.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {

                    if (viewModel.isLoading) {

                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary
                        )

                    } else {

                        Text(
                            if (viewModel.currentStep == 4)
                                "Registrarse"
                            else
                                "Continuar"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun RoleSelectionCard(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color =
                    if (selected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                if (selected)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                else
                    MaterialTheme.colorScheme.surface,
                RoundedCornerShape(16.dp)
            )
            .clickable {
                onClick()
            }
            .padding(20.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            if (selected) {

                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoneDropdown(
    viewModel: RegisterViewModel
) {

    var expanded by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    expanded = true
                }
        ) {

            OutlinedTextField(
                value = viewModel.selectedZoneName,
                onValueChange = {},
                enabled = false,
                label = {
                    Text("Zona")
                },
                trailingIcon = {
                    Text("▼")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor =
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    disabledBorderColor =
                        MaterialTheme.colorScheme.primary,
                    disabledTextColor =
                        MaterialTheme.colorScheme.onSurface,
                    disabledLabelColor =
                        MaterialTheme.colorScheme.primary,
                    disabledTrailingIconColor =
                        MaterialTheme.colorScheme.onSurface
                )
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {

            viewModel.zoneOptions.forEach { zone ->

                DropdownMenuItem(
                    text = {
                        Text(zone.second)
                    },
                    onClick = {

                        viewModel.onZoneSelected(
                            zone.first,
                            zone.second
                        )

                        expanded = false
                    }
                )
            }
        }
    }
}





@Composable
fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    errorMessage: String,
    keyboardType: KeyboardType,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation =
        androidx.compose.ui.text.input.VisualTransformation.None
) {

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(label)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = isError,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType
            ),
            visualTransformation = visualTransformation,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor =
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )

        if (isError) {

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}