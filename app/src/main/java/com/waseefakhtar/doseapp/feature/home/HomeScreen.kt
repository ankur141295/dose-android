package com.waseefakhtar.doseapp.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.waseefakhtar.doseapp.R
import com.waseefakhtar.doseapp.domain.model.Medication
import com.waseefakhtar.doseapp.feature.home.viewmodel.HomeState
import com.waseefakhtar.doseapp.feature.home.viewmodel.HomeViewModel
import com.waseefakhtar.doseapp.util.getTimeRemaining
import java.util.Calendar

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state = viewModel.state
    HomeScreen(state, viewModel)
}

@Composable
fun HomeScreen(state: HomeState, viewModel: HomeViewModel) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Greeting()
        DailyMedications(state, viewModel)
    }
}

@Composable
fun Greeting() {
    Column {
        // TODO: Add greeting based on time of day e.g. Good Morning, Good Afternoon, Good evening.
        // TODO: Get name from DB and show user's first name.
        Text(
            text = "Good morning,",
            style = MaterialTheme.typography.displaySmall
        )
        Text(
            text = "Kathryn!",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.displaySmall
        )
        Spacer(modifier = Modifier.padding(8.dp))
    }
}

@Composable
fun DailyOverview(medicationsToday: List<Medication>) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(36.dp),
        colors = cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.tertiary
        )
    ) {

        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(24.dp, 24.dp, 0.dp, 16.dp)
                    .fillMaxWidth(.36F),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text = "Your plan for today",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                )

                Text(
                    text = "${medicationsToday.filter { it.medicationTaken }.size} of ${medicationsToday.size} completed",
                    style = MaterialTheme.typography.titleSmall,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Image(
                    painter = painterResource(id = R.drawable.doctor), contentDescription = ""
                )
            }
        }
    }
}

@Composable
fun DailyMedications(state: HomeState, viewModel: HomeViewModel) {

    val medicationList = state.medications.sortedBy { it.date }
    val combinedList: List<MedicationListItem> = mutableListOf<MedicationListItem>().apply {
        val calendar = Calendar.getInstance()
        val medicationsToday = medicationList.filter {
            val medicationDate = it.date
            calendar.time = medicationDate
            val medicationDay = calendar.get(Calendar.DAY_OF_YEAR)

            val todayCalendar = Calendar.getInstance()
            val todayDay = todayCalendar.get(Calendar.DAY_OF_YEAR)

            medicationDay == todayDay
        }

        add(MedicationListItem.DailyOverviewItem(medicationsToday))

        if (medicationsToday.isNotEmpty()) {
            add(MedicationListItem.HeaderItem("Today"))
            addAll(medicationsToday.map { MedicationListItem.MedicationItem(it) })
        }

        // Find medications for this week and add "This Week" header
        val startOfWeekThisWeek = Calendar.getInstance()
        startOfWeekThisWeek.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val endOfWeekThisWeek = startOfWeekThisWeek.clone() as Calendar
        endOfWeekThisWeek.add(Calendar.DAY_OF_WEEK, 6)
        val medicationsThisWeek = medicationList.filter {
            val medicationDate = it.date // Change this to the appropriate attribute
            medicationDate in startOfWeekThisWeek.time..endOfWeekThisWeek.time && !medicationsToday.contains(it)
        }
        if (medicationsThisWeek.isNotEmpty()) {
            add(MedicationListItem.HeaderItem("This Week"))
            addAll(medicationsThisWeek.map { MedicationListItem.MedicationItem(it) })
        }

        // Find medications for next week and add "Next Week" header
        val startOfWeekNextWeek = Calendar.getInstance()
        startOfWeekNextWeek.time = endOfWeekThisWeek.time // Use the end of current week as start of next week
        startOfWeekNextWeek.add(Calendar.DAY_OF_MONTH, 1)
        val endOfWeekNextWeek = startOfWeekNextWeek.clone() as Calendar
        endOfWeekNextWeek.add(Calendar.DAY_OF_MONTH, 6)
        val medicationsNextWeek = medicationList.filter {
            val medicationDate = it.date // Change this to the appropriate attribute
            medicationDate in startOfWeekNextWeek.time..endOfWeekNextWeek.time
        }
        if (medicationsNextWeek.isNotEmpty()) {
            add(MedicationListItem.HeaderItem("Next Week"))
            addAll(medicationsNextWeek.map { MedicationListItem.MedicationItem(it) })
        }
    }

    LazyColumn(
        modifier = Modifier,
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(
            items = combinedList,
            itemContent = {
                when (it) {
                    is MedicationListItem.DailyOverviewItem -> {
                        DailyOverview(it.medicationsToday)
                    }
                    is MedicationListItem.HeaderItem -> {
                        Text(
                            modifier = Modifier
                                .padding(4.dp, 12.dp, 8.dp, 0.dp)
                                .fillMaxWidth(),
                            text = it.headerText.uppercase(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    is MedicationListItem.MedicationItem -> {
                        MedicationCard(medication = it.medication, viewModel)
                    }
                }
            }
        )
    }
}

sealed class MedicationListItem {
    data class DailyOverviewItem(val medicationsToday: List<Medication>) : MedicationListItem()
    data class MedicationItem(val medication: Medication) : MedicationListItem()
    data class HeaderItem(val headerText: String) : MedicationListItem()
}

@Composable
fun MedicationCard(medication: Medication, viewModel: HomeViewModel) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(30.dp),
        colors = cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = medication.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = medication.timesOfDay.joinToString(", ")
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = getTimeRemaining(medication),
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {

                Button(
                    onClick = {
                        viewModel.takeMedication(medication)
                    },
                    enabled = !medication.medicationTaken
                ) {
                    if (medication.medicationTaken) {
                        Text(
                            text = "Taken"
                        )
                    } else {
                        Text(
                            text = "Take now"
                        )
                    }
                }
            }
        }
    }
}
