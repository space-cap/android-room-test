package com.ezlevup.roomtest.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ezlevup.roomtest.domain.User

@Composable
fun HomeScreen(
        modifier: Modifier = Modifier,
        viewModel: UserViewModel = viewModel(factory = UserViewModel.Factory)
) {
    val users by viewModel.users.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) } // For Edit

    Scaffold(
            modifier = modifier.fillMaxSize(),
            floatingActionButton = {
                FloatingActionButton(
                        onClick = {
                            selectedUser = null // Reset for add
                            showDialog = true
                        }
                ) { Icon(imageVector = Icons.Default.Add, contentDescription = "Add User") }
            }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            Text(
                    text = "User List",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
            )

            if (users.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No users found. Add one!")
                }
            } else {
                LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(users) { user ->
                        UserItem(
                                user = user,
                                onDelete = { viewModel.deleteUser(user) },
                                onEdit = {
                                    selectedUser = user
                                    showDialog = true
                                }
                        )
                    }
                }
            }
        }

        if (showDialog) {
            UserDialog(
                    user = selectedUser,
                    onDismiss = { showDialog = false },
                    onConfirm = { name, age ->
                        if (selectedUser == null) {
                            viewModel.addUser(name, age)
                        } else {
                            viewModel.updateUser(selectedUser!!.copy(name = name, age = age))
                        }
                        showDialog = false
                    }
            )
        }
    }
}

@Composable
fun UserItem(user: User, onDelete: () -> Unit, onEdit: () -> Unit) {
    Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth().clickable { onEdit() }
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = user.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "Age: ${user.age}", fontSize = 14.sp, color = Color.Gray)
            }
            IconButton(onClick = onDelete) {
                Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                )
            }
        }
    }
}

@Composable
fun UserDialog(user: User?, onDismiss: () -> Unit, onConfirm: (String, Int) -> Unit) {
    var name by remember { mutableStateOf(user?.name ?: "") }
    var age by remember { mutableStateOf(user?.age?.toString() ?: "") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = if (user == null) "Add User" else "Edit User") },
            text = {
                Column {
                    OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                            value = age,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() }) {
                                    age = it
                                }
                            },
                            label = { Text("Age") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                    )
                    if (isError) {
                        Text(
                                text = "Please enter valid name and age",
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = {
                            if (name.isNotBlank() && age.isNotBlank()) {
                                onConfirm(name, age.toInt())
                            } else {
                                isError = true
                            }
                        }
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
