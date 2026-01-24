package com.ndomog.inventory.presentation.dashboard

import androidx.compose.material.icons.filled.Category
import androidx.navigation.NavController
import com.ndomog.inventory.presentation.Routes

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCategories: () -> Unit, // Add this parameter
    viewModelFactory: ViewModelFactory
) {
    val viewModel: DashboardViewModel = viewModel(factory = viewModelFactory)
    val items by viewModel.items.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<Item?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = onNavigateToCategories) { // New button
                        Icon(Icons.Filled.Category, contentDescription = "Categories")
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Filled.Person, contentDescription = "Profile")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                itemToEdit = null
                showAddEditDialog = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Item")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else if (error != null) {
                Text(
                    "Error: $error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                if (items.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No items found. Click '+' to add one!")
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(items) { item ->
                            ItemCard(item = item) {
                                itemToEdit = item
                                showAddEditDialog = true
                            }
                        }
                    }
                }
            }
        }

        AddEditItemDialog(
            showDialog = showAddEditDialog,
            onDismiss = { showAddEditDialog = false },
            onConfirm = { item ->
                if (itemToEdit == null) {
                    viewModel.addItem(item, true) // Assume online for now
                } else {
                    viewModel.updateItem(item, true) // Assume online for now
                }
                showAddEditDialog = false
            },
            existingItem = itemToEdit
        )
    }
}

@Composable
fun ItemCard(item: Item, onEdit: (Item) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onEdit(item) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium)
                Text("Category: ${item.category}", style = MaterialTheme.typography.bodySmall)
                Text("Quantity: ${item.quantity}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { onEdit(item) }) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit Item")
            }
        }
    }
}