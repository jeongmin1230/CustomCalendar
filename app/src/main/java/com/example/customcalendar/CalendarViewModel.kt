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

    fun countSchedule(endDay: String) {
        firebaseDatabase.reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val totalSplit = totalDate.split("-")
                val range = totalSplit[0] + "-" + totalSplit[1] + "-01" .. totalSplit[0] + "-" + totalSplit[1] + "-" + endDay
/*                for(childSnapshot in snapshot.children) {
                    *//*
                                            countList.add(childSnapshot.childrenCount.toInt())
                    *//*
                    println("in countSchedule : ${childSnapshot.key}")
                }*/
                for(i in 0 until endDay.toInt()) {

                }
                for(childSnapshot in snapshot.children) {
                    if(childSnapshot.key.toString() in range) {
                        if(childSnapshot.hasChildren()) {
                            println("has children")
                            countList.add(childSnapshot.childrenCount.toInt())
                        } else {
                            println("haven't children")
                            countList.add(0)
                        }
                        println("in range : ${childSnapshot.key}")
                    } /*else {
                        countList.add(0)
                        println("childSnapshot ${childSnapshot.key}")
                    }*/
                }

/*                for(childSnapshot in snapshot.children) {
                    println("range : $range")
                    println("key : ${childSnapshot.key}")
                    println("hasChildren? : ${childSnapshot.hasChildren()}")
                    if(childSnapshot.key.toString() in range) {
//                        if(childSnapshot.childrenCount.toString() == "") countList.add(0)
                        if(childSnapshot.hasChildren()) countList.add(childSnapshot.childrenCount.toInt())
                        else countList.add(0)
                    } else {
                        countList.add(0)
                    }
                }*/
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