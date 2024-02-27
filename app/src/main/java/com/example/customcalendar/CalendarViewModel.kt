package com.example.customcalendar

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CalendarViewModel: ViewModel() {
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    var scheduleList = mutableStateOf(listOf<Schedule>())
    var countList = mutableListOf<Int>()
    var totalDate = ""

    fun numSchedule() {
        firebaseDatabase.reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(childSnapshot in snapshot.children) {
                    countList.add(childSnapshot.childrenCount.toInt())
                    println("current date : $totalDate")
                    println("num : ${childSnapshot.childrenCount}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    fun load(list: MutableState<List<Schedule>>) {
        firebaseDatabase.reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(childSnapshot in snapshot.children) {
                    val scheduleList = mutableListOf<Schedule>()
                    if(childSnapshot.key.toString() == totalDate) {
                        childSnapshot.children.forEach {
                            val item = it.child("item").getValue(String::class.java) ?: ""
                            val time = it.child("time").getValue(String::class.java) ?: ""
                            val who = it.child("who").getValue(String::class.java) ?: ""

                            val li = Schedule(item, time, who)
                            scheduleList.add(li)
                        }
                        list.value = scheduleList
                        break
                    } else list.value = emptyList()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("error : ${error.message}")
            }

        })
    }

    fun register(item: MutableState<String>, time: MutableState<String>, who: MutableState<String>) {
        firebaseDatabase.getReference(totalDate).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val count = snapshot.childrenCount
                snapshot.child("${count+1}").let {
                    it.child("item").ref.setValue(item.value)
                    it.child("time").ref.setValue(time.value)
                    it.child("who").ref.setValue(who.value)
                }
                item.value = ""
                time.value = ""
                who.value = ""
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}