package com.nthcase.sample.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.nthcase.sample.TestIds
import com.nthcase.sample.data.Category
import com.nthcase.sample.data.ClubForm
import com.nthcase.sample.data.FormRules
import com.nthcase.sample.ui.Icons
import com.nthcase.sample.ui.LocalSnack
import com.nthcase.sample.ui.MossgroveTopBar
import com.nthcase.sample.ui.show
import com.nthcase.sample.ui.testIdRoot
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

const val JOINED_MESSAGE = "Welcome to the club"

/** "12 Nov 2026": the delivery date as every framework writes it. */
private val dateFormat = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.US)

fun formatPickedDate(utcMillis: Long): String = Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate().format(dateFormat)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(onBack: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var age by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var category by rememberSaveable { mutableStateOf<Category?>(null) }
    var categoryOpen by rememberSaveable { mutableStateOf(false) }
    var date by rememberSaveable { mutableStateOf<String?>(null) }
    var dateOpen by rememberSaveable { mutableStateOf(false) }
    var newsletter by rememberSaveable { mutableStateOf(false) }
    var budget by rememberSaveable { mutableStateOf(50f) }
    var terms by rememberSaveable { mutableStateOf(false) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var joined by rememberSaveable { mutableStateOf<String?>(null) }
    val snack = LocalSnack.current

    val form = ClubForm(name, email, age, password, category, date, newsletter, budget.roundToInt(), terms)
    // Errors appear after the first Join, then follow every edit.
    val errors = if (submitted) FormRules.errors(form) else emptyMap()

    Scaffold(modifier = Modifier.testTag(TestIds.FORM_SCREEN), topBar = { MossgroveTopBar("Join the club", onBack = onBack) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Members hear about new drawers first. Every field is required except the newsletter.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Field("Full name", name, { name = it }, TestIds.FORM_NAME, errors["name"], TestIds.FORM_NAME_ERROR)
            Field("Email", email, { email = it }, TestIds.FORM_EMAIL, errors["email"], TestIds.FORM_EMAIL_ERROR, KeyboardType.Email)
            Field("Age", age, { age = it.filter(Char::isDigit).take(3) }, TestIds.FORM_AGE, errors["age"], TestIds.FORM_AGE_ERROR, KeyboardType.Number)
            Field(
                "Password", password, { password = it }, TestIds.FORM_PASSWORD, errors["password"], TestIds.FORM_PASSWORD_ERROR,
                KeyboardType.Password,
                hidden = !showPassword,
                trailing = {
                    IconButton(onClick = { showPassword = !showPassword }, modifier = Modifier.testTag(TestIds.FORM_PASSWORD_TOGGLE)) {
                        Icon(if (showPassword) Icons.EyeOff else Icons.Eye, contentDescription = if (showPassword) "Hide password" else "Show password")
                    }
                },
            )

            ExposedDropdownMenuBox(expanded = categoryOpen, onExpandedChange = { categoryOpen = it }) {
                OutlinedTextField(
                    value = category?.label.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Favourite aisle") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryOpen) },
                    isError = errors["category"] != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .testTag(TestIds.FORM_CATEGORY),
                )
                ExposedDropdownMenu(expanded = categoryOpen, onDismissRequest = { categoryOpen = false }, modifier = Modifier.testIdRoot()) {
                    Category.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = { category = option; categoryOpen = false },
                            modifier = Modifier.testTag(TestIds.formCategoryOption(option.key)),
                        )
                    }
                }
            }
            ErrorLine(errors["category"], TestIds.FORM_CATEGORY_ERROR)

            OutlinedTextField(
                value = date.orEmpty(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Delivery date") },
                trailingIcon = {
                    IconButton(onClick = { dateOpen = true }, modifier = Modifier.testTag(TestIds.FORM_DATE)) {
                        Icon(Icons.Calendar, contentDescription = "Choose a delivery date")
                    }
                },
                isError = errors["date"] != null,
                modifier = Modifier.fillMaxWidth(),
            )
            ErrorLine(errors["date"], TestIds.FORM_DATE_ERROR)

            ToggleRow("Email me about new drawers", newsletter, { newsletter = it }, TestIds.FORM_NEWSLETTER, Role.Switch) { checked ->
                Switch(checked = checked, onCheckedChange = null)
            }

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Monthly budget", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Text("$${budget.roundToInt()}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.testTag(TestIds.FORM_BUDGET_VALUE))
                }
                Slider(
                    value = budget,
                    onValueChange = { budget = it },
                    valueRange = 0f..200f,
                    steps = 19, // $10 steps
                    modifier = Modifier
                        .testTag(TestIds.FORM_BUDGET)
                        .semantics { contentDescription = "Monthly budget" },
                )
            }

            ToggleRow("I accept the club terms", terms, { terms = it }, TestIds.FORM_TERMS, Role.Checkbox) { checked ->
                Checkbox(checked = checked, onCheckedChange = null)
            }
            ErrorLine(errors["terms"], TestIds.FORM_TERMS_ERROR)

            Spacer(Modifier.height(4.dp))
            Button(
                onClick = {
                    submitted = true
                    if (FormRules.errors(form).isEmpty()) {
                        joined = "Welcome, ${name.trim()}. We'll write to ${email.trim()} about the ${category!!.label} aisle " +
                            "and deliver on ${date!!}."
                        snack.show(JOINED_MESSAGE)
                    } else {
                        joined = null
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag(TestIds.FORM_SUBMIT),
            ) { Text("Join") }

            joined?.let {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    // one node with the whole message, so a check on form_result reads its text
                    modifier = Modifier.fillMaxWidth().testTag(TestIds.FORM_RESULT).semantics(mergeDescendants = true) {},
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(it, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(start = 12.dp))
                    }
                }
            }
        }
    }

    if (dateOpen) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { dateOpen = false },
            confirmButton = {
                TextButton(
                    onClick = { state.selectedDateMillis?.let { date = formatPickedDate(it) }; dateOpen = false },
                    enabled = state.selectedDateMillis != null,
                    modifier = Modifier.testTag(TestIds.FORM_DATE_CONFIRM),
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { dateOpen = false }, modifier = Modifier.testTag(TestIds.FORM_DATE_CANCEL)) { Text("Cancel") }
            },
            modifier = Modifier.testIdRoot(),
        ) {
            DatePicker(state = state, modifier = Modifier.testTag(TestIds.FORM_DATE_PICKER))
        }
    }
}

@Composable
private fun Field(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    id: String,
    error: String?,
    errorId: String,
    keyboard: KeyboardType = KeyboardType.Text,
    hidden: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            label = { Text(label) },
            singleLine = true,
            isError = error != null,
            trailingIcon = trailing,
            visualTransformation = if (hidden) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboard),
            modifier = Modifier.fillMaxWidth().testTag(id).semantics { if (error != null) error(error) },
        )
        ErrorLine(error, errorId)
    }
}

/**
 * A field's error, as a line of its own under the field. A field's supporting text is folded into the field's
 * accessibility node, which would hide the error's id from Appium; this line keeps it.
 */
@Composable
private fun ErrorLine(error: String?, id: String) {
    if (error == null) return
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp, top = 4.dp)) {
        Icon(Icons.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
        Text(
            error,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 6.dp).testTag(id),
        )
    }
}

/** A whole-row toggle: the label and the control are one 48 dp target, and the id sits on the control. */
@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    id: String,
    role: Role,
    control: @Composable (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag(id)
            .toggleable(value = checked, role = role, onValueChange = onChange),
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        control(checked)
    }
}
