package com.itl.commonres.firebaseUtils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object FirebaseDbConfig {

    lateinit var db: FirebaseDatabase
    lateinit var dbReference: DatabaseReference
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var configRef: DatabaseReference
    var isInitialized: Boolean = false

    fun initialize() {
        if (!isInitialized) {
            db = FirebaseDatabase.getInstance()
            firebaseAuth = FirebaseAuth.getInstance()
            dbReference = db.reference
            configRef = db.getReference("/config/")
            isInitialized = true
        }
    }

    fun getDatabaseReference(): DatabaseReference {
        checkInitialization()
        return dbReference
    }

    fun getDatabase(): FirebaseDatabase {
        checkInitialization()
        return db
    }

    private fun checkInitialization() {
        if (!isInitialized) {
            throw IllegalStateException("FirebaseDbConfig must be initialized. Call initialize() first.")
        }
    }
}