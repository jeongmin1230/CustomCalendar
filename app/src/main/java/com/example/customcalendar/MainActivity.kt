package com.example.customcalendar

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.customcalendar.ui.theme.CustomCalendarTheme
import java.time.LocalDate

@Composable
fun dayOfWeek() = stringArrayResource(id = R.array.day_of_week)

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CustomCalendarTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val now = LocalDate.now()
                    Whole(now)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Whole(now: LocalDate) {
    val calendarViewModel = CalendarViewModel()
    val year = remember { mutableStateOf(now.year) }
    val month = remember { mutableStateOf(now.month.value) }
    val day = remember { mutableStateOf(now.dayOfMonth) }
    val scheduleList =  remember { calendarViewModel.scheduleList }
    val item = remember { mutableStateOf("") }
    val time = remember { mutableStateOf("") }
    val who = remember { mutableStateOf("") }
    val count = remember { calendarViewModel.countList }
    val register = remember { mutableStateOf(false) }

    Box(Modifier.background(Color.White)) {
        Column(Modifier.padding(horizontal = 8.dp)) {
            /** calendar header */
            Column(Modifier.weight(0.5f)) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_back),
                        contentDescription = null,
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentSize()
                            .clickable {
                                month.value -= 1
                                if (month.value < 1) {
                                    year.value -= 1
                                    month.value = 12
                                }
                            }
                    )
                    Text(
                        text = "${year.value}년 ${month.value}월",
                        style = MaterialTheme.typography.bodyMedium.copy(Color.Black),
                    )
                    Image(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_forward),
                        contentDescription = null,
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentSize()
                            .clickable {
                                month.value += 1
                                if (month.value > 12) {
                                    year.value += 1
                                    month.value = 1
                                }
                            }
                    )
                }
                /** calendar body */
                Column(modifier = Modifier
                    .fillMaxSize()
                    .border(BorderStroke(1.dp, Color.DarkGray))) {
                    DrawBody(
                        startIndex = LocalDate.of(year.value, month.value, 1).dayOfWeek.toString(),
                        endDay = LocalDate.of(year.value, month.value, day.value).lengthOfMonth(),
                        clickDay = day
                    ) {
                        calendarViewModel.totalDate = "${year.value}-${month.value}-${day.value}"
                        calendarViewModel.load(scheduleList)
                        calendarViewModel.countSchedule(LocalDate.of(year.value, month.value, day.value).lengthOfMonth().toString())
                        count.forEach {
                            println("개수 : $it")
                        }
                    }
                }
            }

            /** schedule */
            Column(modifier = Modifier
                .padding(all = 4.dp)
                .weight(0.5f)) {
                Text(
                    text = "${month.value}월 ${day.value}일 일정",
                    style = MaterialTheme.typography.labelLarge.copy(Color.DarkGray),
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                if(scheduleList.value.isNotEmpty()) {
                    LazyColumn(
                        content = {
                            scheduleList.value.forEach {
                                item {
                                    EachSchedule(item = it.item, time = it.time, who = it.who)
                                }
                            }
                        }
                    )
                } else {
                    Text(text = "일정이 없습니다.")
                }
            }
        }
        FloatingActionButton(
            onClick = { register.value = true},
            containerColor = Color.LightGray,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 8.dp, bottom = 8.dp)) {
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.ic_add),
                contentDescription = null)
        }
        if(register.value) {
            AlertDialog(
                onDismissRequest = { register.value = false },
                confirmButton = {
                    Text(
                        text = "등록",
                        style = MaterialTheme.typography.labelLarge.copy(Color.DarkGray),
                        modifier = Modifier.clickable {
                            register.value = false
                            calendarViewModel.register(item, time, who) }
                    )
                },
                containerColor = Color.White,
                properties = DialogProperties(usePlatformDefaultWidth = false),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                text = {
                    calendarViewModel.totalDate = "${year.value}-${month.value}-${day.value}"
                    Column {
                        Text(
                            text = "${year.value}년 ${month.value}월 ${day.value}일 일정 등록",
                            style = MaterialTheme.typography.labelLarge.copy(Color.DarkGray),
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        RegisterColumn(mean = "일정", value = item)
                        RegisterColumn(mean = "시간", value = time)
                        RegisterColumn(mean = "이름", value = who)
                    }
                }
            )
        }
    }
}

@Composable
fun DrawBody(startIndex: String, endDay: Int, clickDay: MutableState<Int>, show: () -> Unit) {
    val dayOfWeekMap = mapOf("SUNDAY" to "일", "MONDAY" to "월", "TUESDAY" to "화", "WEDNESDAY" to "수",
        "THURSDAY" to "목", "FRIDAY" to "금", "SATURDAY" to "토")
    val dayOfWeek = dayOfWeek()
    val dayBelowDayOfWeek = mutableMapOf<String, MutableList<Int>>()
    val adjustedDayOfWeek = dayOfWeek.toMutableList().subList(dayOfWeek.indexOf(dayOfWeekMap[startIndex]), dayOfWeek.size) + dayOfWeek.toMutableList().subList(0, dayOfWeek.indexOf(dayOfWeekMap[startIndex]))
    dayOfWeekMap.forEach { dayBelowDayOfWeek.computeIfAbsent(it.value) { mutableListOf() } }
    for(i in 0 until dayOfWeek.indexOf(dayOfWeekMap[startIndex])) { dayBelowDayOfWeek[dayOfWeek[i]]?.add(0) }
    (1..endDay).forEachIndexed { index, day ->
        val dayOfWeekIndex = index % 7
        val currentDay = adjustedDayOfWeek[dayOfWeekIndex]
        dayBelowDayOfWeek[currentDay]?.add(day)
    }
    dayBelowDayOfWeek.forEach { day -> if(day.value.size != (dayBelowDayOfWeek.values.maxByOrNull { it.size }?.size ?: 0)) { day.value.add(0) } }

    Row(Modifier.padding(top = 8.dp)) {
        dayBelowDayOfWeek.forEach { (dayOfWeek, dayList) ->
            Day(dayOfWeek = dayOfWeek, dayList = dayList, modifier = Modifier.weight(1f), clickDay = clickDay)
        }
    }

    LaunchedEffect(clickDay.value) { show() }
}

@Composable
fun Day(dayOfWeek: String, dayList: MutableList<Int>?, modifier: Modifier, clickDay: MutableState<Int>) {
    val color = MaterialTheme.typography.labelLarge.copy(textAlign = TextAlign.Center, color = if(dayOfWeek == "일") Color.Red else if(dayOfWeek == "토") Color.Blue else Color.DarkGray)
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dayOfWeek,
            style = color,
            modifier = Modifier.padding(bottom = 14.dp)
        )
        dayList?.forEach { day ->
            Text(
                text = if(day == 0) "" else day.toString(),
                style = color,
                modifier = Modifier
                    .weight(1f)
                    .padding(all = 6.dp)
                    .wrapContentSize()
                    .background(
                        if (clickDay.value == day) Color.LightGray else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { clickDay.value = day }
            )
        }
    }
}

@Composable
fun EachSchedule(item: String, time: String, who: String) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = who.substring(0, 2),
                style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center, color = Color.White),
                modifier = Modifier
                    .weight(0.12f)
                    .background(color = Color.Black, shape = CircleShape)
                    .padding(all = 4.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier
                .border(1.dp, Color.Gray)
                .padding(start = 4.dp)
                .weight(1f)) {
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyLarge.copy(Color.Black)
                )
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodyMedium.copy(Color.DarkGray)
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterColumn(mean: String, value: MutableState<String>) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = mean,
            style = MaterialTheme.typography.labelLarge.copy(Color.DarkGray),
            modifier = Modifier.padding(end = 4.dp)
        )
        TextField(
            value = value.value,
            onValueChange = { value.value = it },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.LightGray,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.height(48.dp)
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}