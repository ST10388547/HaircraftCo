package com.example.haircraftco

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.example.haircraftco.models.Service

class ManageServicesActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var listView: ListView
    private var servicesList: MutableList<Service> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_services)

        val etName = findViewById<EditText>(R.id.et_service_name)
        val etCategory = findViewById<EditText>(R.id.et_category)
        val etPrice = findViewById<EditText>(R.id.et_price)
        val etDuration = findViewById<EditText>(R.id.et_duration)
        val addBtn = findViewById<Button>(R.id.btn_add_service)
        listView = findViewById(R.id.lv_services_admin)

        loadServices()

        addBtn.setOnClickListener {
            val name = etName.text.toString()
            val category = etCategory.text.toString()
            val price = etPrice.text.toString().toDoubleOrNull() ?: 0.0
            val duration = etDuration.text.toString().toIntOrNull() ?: 0

            if (name.isNotEmpty() && category.isNotEmpty()) {
                val serviceId = db.collection("services").document().id
                val service = Service(serviceId, name, category, price, duration)
                db.collection("services").document(serviceId).set(service)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Service added", Toast.LENGTH_SHORT).show()
                        etName.text.clear()
                        etCategory.text.clear()
                        etPrice.text.clear()
                        etDuration.text.clear()
                        loadServices()
                    }
            }
        }
    }

    private fun loadServices() {
        db.collection("services").get()
            .addOnSuccessListener { documents ->
                servicesList.clear()
                servicesList.addAll(documents.toObjects(Service::class.java))
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, servicesList.map { "${it.name} - R${it.price} (${it.category})" })
                listView.adapter = adapter

                listView.setOnItemClickListener { _, _, position, _ ->
                    showEditDialog(servicesList[position])
                }
            }
    }

    private fun showEditDialog(service: Service) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Service")

        val inputName = EditText(this).apply { setText(service.name) }
        val inputCategory = EditText(this).apply { setText(service.category) }
        val inputPrice = EditText(this).apply { setText(service.price.toString()) }
        val inputDuration = EditText(this).apply { setText(service.duration.toString()) }

        builder.setView(inputName)  // Stack views manually or use LinearLayout
        // For simplicity, use multiple setView or custom layout; here mock with sequential inputs

        builder.setPositiveButton("Update") { _, _ ->
            val updatedService = service.copy(
                name = inputName.text.toString(),
                category = inputCategory.text.toString(),
                price = inputPrice.text.toString().toDoubleOrNull() ?: service.price,
                duration = inputDuration.text.toString().toIntOrNull() ?: service.duration
            )
            db.collection("services").document(service.serviceId).set(updatedService)
                .addOnSuccessListener { loadServices() }
        }
        builder.setNegativeButton("Delete") { _, _ ->
            db.collection("services").document(service.serviceId).delete()
                .addOnSuccessListener { loadServices() }
        }
        builder.setNeutralButton("Cancel", null)
        builder.show()
    }
}