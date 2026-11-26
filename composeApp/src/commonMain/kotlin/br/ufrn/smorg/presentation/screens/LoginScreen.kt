package br.ufrn.smorg.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.ufrn.smorg.domain.UserRole
import br.ufrn.smorg.presentation.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Card(
                modifier = Modifier.widthIn(max = 460.dp).fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    ),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(bottom = 12.dp),
                    ) {
                        Text(
                            text = "Laboratórios Acadêmicos • NFC",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }

                    Text(
                        text = "SmOrg",
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(
                        text = "Acesso ao Sistema",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.semantics { heading() },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text =
                            "Consulte o acervo do laboratório, retire kits didáticos " +
                                "e registre devoluções sem filas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Selecione seu Perfil de Acesso:",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    ) {
                        UserRole.entries.forEach { role ->
                            FilterChip(
                                selected = state.selectedRole == role,
                                onClick = { viewModel.onRoleSelect(role) },
                                label = { Text(role.label) },
                            )
                        }
                    }

                    Text(
                        text = state.selectedRole.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                    )

                    OutlinedTextField(
                        value = state.identifier,
                        onValueChange = viewModel::onIdentifierChange,
                        label = { Text("Matrícula ou E-mail") },
                        placeholder = { Text("Ex.: 20240027427 ou nome@ufrn.edu.br") },
                        isError = state.identifierError != null,
                        supportingText = {
                            if (state.identifierError != null) {
                                Text(
                                    text = state.identifierError!!,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            } else {
                                Text("Use sua matrícula SIGAA ou e-mail institucional")
                            }
                        },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = "Ícone de usuário")
                        },
                        trailingIcon = {
                            if (state.identifier.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.onIdentifierChange("") },
                                    modifier =
                                        Modifier.semantics {
                                            contentDescription = "Limpar matrícula ou e-mail"
                                        },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Limpar campo",
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = { Text("Senha") },
                        placeholder = { Text("Mínimo de 6 caracteres") },
                        isError = state.passwordError != null,
                        supportingText = {
                            if (state.passwordError != null) {
                                Text(
                                    text = state.passwordError!!,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            } else {
                                Text("Mínimo de 6 caracteres para validação de acesso")
                            }
                        },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Ícone de cadeado")
                        },
                        trailingIcon = {
                            TextButton(
                                onClick = viewModel::togglePasswordVisibility,
                                modifier =
                                    Modifier.semantics {
                                        contentDescription =
                                            if (state.isPasswordVisible) "Ocultar senha" else "Mostrar senha"
                                    },
                            ) {
                                Text(
                                    text = if (state.isPasswordVisible) "Ocultar" else "Mostrar",
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation =
                            if (state.isPasswordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (viewModel.submitLogin()) {
                                onLoginSuccess()
                            }
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                    ) {
                        Text(
                            text = "Entrar",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }

                    if (state.identifier.isNotEmpty() || state.password.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            TextButton(onClick = viewModel::clearFields) {
                                Text("Limpar formulário")
                            }
                        }
                    }
                }
            }
        }
    }
}
