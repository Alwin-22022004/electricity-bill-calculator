package com.example.electricitybillcalculator;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText etName, etConsumerNumber, etUnits;
    private TextView tvBillAmount;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName = findViewById(R.id.etName);
        etConsumerNumber = findViewById(R.id.etConsumerNumber);
        etUnits = findViewById(R.id.etUnits);

        Button btnCalculate = findViewById(R.id.btnCalculate);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnView = findViewById(R.id.btnView);

        tvBillAmount = findViewById(R.id.tvBillAmount);

        databaseHelper = new DatabaseHelper(this);

        // Calculate button
        btnCalculate.setOnClickListener(v -> {
            String unitsText = etUnits.getText().toString().trim();

            if (unitsText.isEmpty()) {
                etUnits.setError(getString(R.string.error_enter_units));
                return;
            }

            int units;
            try {
                units = Integer.parseInt(unitsText);
            } catch (NumberFormatException e) {
                etUnits.setError(getString(R.string.error_invalid_units));
                return;
            }

            if (units < 0) {
                etUnits.setError(getString(R.string.error_units_negative));
                return;
            }

            double calculatedAmount = calculateBill(units);

            if (calculatedAmount < 0) {
                tvBillAmount.setText(R.string.error_rates_not_configured);
                Toast.makeText(this,
                        R.string.error_enter_units_0_to_100,
                        Toast.LENGTH_LONG).show();
                return;
            }

            tvBillAmount.setText(String.format(Locale.US,
                    "Energy Charges: ₹%.2f", calculatedAmount));
        });

        // Save button
        btnSave.setOnClickListener(v -> saveBill());

        // View all saved bills
        btnView.setOnClickListener(v -> viewAllBills());
    }

    // Calculate energy charges using the supplied rates
    private double calculateBill(int units) {
        if (units < 0 || units > 100) {
            return -1;
        }

        if (units <= 50) {
            return units * 3.35;
        }

        return (50 * 3.35) + ((units - 50) * 4.25);
    }

    // CREATE: Save a bill in SQLite
    private void saveBill() {
        String name = etName.getText().toString().trim();
        String number = etConsumerNumber.getText().toString().trim();
        String unitsText = etUnits.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError(getString(R.string.error_enter_consumer_name));
            return;
        }

        if (number.isEmpty()) {
            etConsumerNumber.setError(getString(R.string.error_enter_consumer_number));
            return;
        }

        if (unitsText.isEmpty()) {
            etUnits.setError(getString(R.string.error_enter_units));
            return;
        }

        int units;
        try {
            units = Integer.parseInt(unitsText);
        } catch (NumberFormatException e) {
            etUnits.setError(getString(R.string.error_invalid_units));
            return;
        }

        double amount = calculateBill(units);

        if (amount < 0) {
            Toast.makeText(this,
                    R.string.error_rates_not_configured,
                    Toast.LENGTH_LONG).show();
            return;
        }

        long result = databaseHelper.insertBill(
                name, number, units, amount
        );

        if (result != -1) {
            Toast.makeText(this,
                    R.string.msg_bill_saved,
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,
                    R.string.msg_failed_to_save,
                    Toast.LENGTH_SHORT).show();
        }
    }

    // READ: Display all saved bills
    private void viewAllBills() {
        Cursor cursor = databaseHelper.getAllBills();
        StringBuilder bills = new StringBuilder();

        try {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                    DatabaseHelper.COL_ID));

                    String name = cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    DatabaseHelper.COL_NAME));

                    String number = cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    DatabaseHelper.COL_NUMBER));

                    int units = cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                    DatabaseHelper.COL_UNITS));

                    double amount = cursor.getDouble(
                            cursor.getColumnIndexOrThrow(
                                    DatabaseHelper.COL_AMOUNT));

                    bills.append("ID: ").append(id)
                            .append("\nName: ").append(name)
                            .append("\nConsumer No: ").append(number)
                            .append("\nUnits: ").append(units)
                            .append("\nBill Amount: ₹")
                            .append(String.format(Locale.US, "%.2f", amount))
                            .append("\n\n");

                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        if (bills.length() == 0) {
            bills.append(getString(R.string.dialog_no_bills));
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title_saved_bills)
                .setMessage(bills.toString())
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
