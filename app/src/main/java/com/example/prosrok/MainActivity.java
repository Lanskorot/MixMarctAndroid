package com.example.prosrok;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText editTextText6, editTextText2, editTextText3, editTextText4, editTextText5;
    private JSONArray jsonArray;
    private Button btn_scan;
    private FirebaseFirestore db;


    private boolean isValidDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        sdf.setLenient(false); // Устанавливаем строгий режим парсинга

        try {
            // Пытаемся распарсить введенную дату
            sdf.parse(date);
            return true; // Если успешно, дата корректна
        } catch (Exception e) {
            return false; // Если произошла ошибка парсинга, дата некорректна
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        editTextText6 = findViewById(R.id.editTextText6);
        editTextText2 = findViewById(R.id.editTextText2);
        editTextText3 = findViewById(R.id.editTextText3);
        editTextText4 = findViewById(R.id.editTextText4);
        editTextText5 = findViewById(R.id.editTextText5);
        Button button = findViewById(R.id.button);
        btn_scan = findViewById(R.id.btn_scan);

        removeExpiredItemsFromFirestore();

        editTextText6.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // Фокус потерян, выполняем поиск в базе данных и устанавливаем результат в editTextText2
                    performSearch();
                }
            }
        });

        editTextText3.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // Фокус потерян, выполняем поиск в базе данных и устанавливаем результат в editTextText2
                    performSearch();
                }
            }
        });
        // Добавляем TextWatcher для форматирования даты
        editTextText4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 2 && editable.charAt(1) != '.') {
                    editable.insert(2, ".");
                } else if (editable.length() == 5 && editable.charAt(4) != '.') {
                    editable.insert(5, ".");
                } else if (editable.length() > 10) {
                    editable.replace(0, 10, editable.subSequence(0, 10));
                }
            }
        });

        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScanActivity();
            }
        });

        jsonArray = loadDataFromPreferences();

        Intent intent = getIntent();
        if (intent != null) {
            String barcode = intent.getStringExtra("barcode");
            String part1 = intent.getStringExtra("part1");
            String part2 = intent.getStringExtra("part2");
            String part3 = intent.getStringExtra("part3");
            String part4 = intent.getStringExtra("part4");
            String part5 = intent.getStringExtra("part5");
            String part6 = intent.getStringExtra("part6");

            if (barcode != null && part1 != null && part2 != null && part3 != null && part4 != null && part5 != null && part6 != null) {
                // Заполните соответствующие поля на главной странице
                editTextText6.setText(part4);
                editTextText2.setText(part6);
            }
        }


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainActivity", "Button clicked");
                String enteredDate = editTextText4.getText().toString().trim();

                // Проверка корректности формата даты
                if (isValidDate(enteredDate)) {
                    Log.d("MainActivity", "Valid date");

                    addDataToArray();
                    saveDataToPreferences(jsonArray.toString());
                    removeExpiredItemsFromFirestore();
                    editTextText6.post(new Runnable() {
                        @Override
                        public void run() {
                            editTextText6.setSelection(0);
                            editTextText6.requestFocus();
                        }
                    });
                } else {
                    // Вывести сообщение об ошибке
                    Toast.makeText(MainActivity.this, "Некорректная дата", Toast.LENGTH_SHORT).show();
                    // Очистить поле ввода
                    editTextText4.setText("");
                }
            }
        });


    }

    private void startScanActivity() {
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }



    private void performSearch() {
        try {
            String barcode = editTextText6.getText().toString().trim();

            if (!barcode.isEmpty()) {
                DataBaseAssetsHelper dbHelper = new DataBaseAssetsHelper(this);
                String resultFromDatabase = dbHelper.getDataFromBarcode(barcode);

                if (resultFromDatabase != null) {
                    editTextText2.setText(resultFromDatabase);
                    // Делайте что-то с данными из базы данных
                    Toast.makeText(this, "Данные из базы данных: " + resultFromDatabase, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Данные для штрих-кода " + barcode + " не найдены", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Обработка случая, когда строка ввода пуста
                editTextText2.setText("Введите номер для поиска");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Обработка других исключений
            Toast.makeText(this, "Произошла ошибка при выполнении поиска", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void removeExpiredItemsFromFirestore() {
        // Получаем текущее время
        long currentTimeMillis = System.currentTimeMillis();

        // Получаем коллекцию "tivat" из Firestore
        db.collection("tivat")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Итерируем по каждому документу
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Получаем дату истечения срока хранения из документа
                            String expirationDate = document.getString("expiration_date");

                            // Парсим дату из строки в формате "dd.MM.yyyy"
                            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                            try {
                                Date expirationDateObj = sdf.parse(expirationDate);
                                if (expirationDateObj != null) {
                                    // Проверяем, истек ли срок хранения (срок истек 3 дня назад)
                                    long threeDaysInMillis = 3 * 24 * 60 * 60 * 1000; // 3 days in milliseconds
                                    long expirationTimeMillis = expirationDateObj.getTime() + threeDaysInMillis;

                                    if (expirationTimeMillis < currentTimeMillis) {
                                        // Срок истек, удаляем документ из Firestore
                                        document.getReference().delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("Firestore", "DocumentSnapshot successfully deleted!");
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.w("Firestore", "Error deleting document", e);
                                                });
                                    }
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                                Log.e("Firestore", "Error parsing expiration date", e);
                            }
                        }
                    } else {
                        Log.w("Firestore", "Error getting documents.", task.getException());
                    }
                });
    }

    private boolean isExpired(Date expirationDate) {
        if (expirationDate != null) {
            Date today = new Date();
            long twoDaysInMillis = 2 * 24 * 60 * 60 * 1000; // 2 days in milliseconds
            Date expirationWithBuffer = new Date(expirationDate.getTime() + twoDaysInMillis);
            return expirationWithBuffer.before(today);
        }
        return false;
    }

    private JSONArray loadDataFromPreferences() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        String jsonArrayString = preferences.getString("jsonArray", "[]");
        try {
            return new JSONArray(jsonArrayString);
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }

    private void saveDataToPreferences(String jsonArrayString) {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("jsonArray", jsonArrayString);
        editor.apply();
    }

    public void show_data(View v) {
        Intent intent = new Intent(this, show_info.class);
        intent.putExtra("jsonArray", jsonArray.toString());
        startActivity(intent);
    }

    public void startEditingAndDeleting(View v) {
        Intent intent = new Intent(this, editingAndDeleting.class);
        intent.putExtra("jsonArray", jsonArray.toString());
        startActivity(intent);
    }

    public void show_scan(View v) {
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }

    private void addDataToFirestore(String documentId, Map<String, Object> data) {
        // Создаем новый документ в коллекции "tivat" с уникальным идентификатором
        db.collection("tivat")
                .document(documentId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "DocumentSnapshot successfully written!");
                    Toast.makeText(MainActivity.this, "Данные успешно сохранены в Firestore", Toast.LENGTH_SHORT).show();

                    // После успешной записи в базу данных очищаем поля ввода
                    editTextText6.setText("");
                    editTextText2.setText("");
                    editTextText3.setText("");
                    editTextText4.setText("");
                    editTextText5.setText("");

                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error writing document", e);
                    Toast.makeText(MainActivity.this, "Ошибка сохранения данных в Firestore", Toast.LENGTH_SHORT).show();
                });
    }

    private void addDataToArray() {
        try {
            // Создаем объект DataModel с помощью данных из полей ввода
            DataModel dataModel = new DataModel(
                    null, // id, который будет присвоен Firestore автоматически
                    editTextText6.getText().toString(),
                    editTextText2.getText().toString(),
                    editTextText3.getText().toString(),
                    editTextText4.getText().toString(),
                    editTextText5.getText().toString()
            );

            // Создаем уникальный идентификатор, состоящий из barcode, символа тире и expiration_date
            String documentId = dataModel.getBarcode() + "-" + dataModel.getExpiration_date();

            // Проверяем, есть ли данные с таким же ID в Firestore
            db.collection("tivat").document(documentId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Если документ с таким ID не существует, добавляем новые данные
                            if (!task.getResult().exists()) {
                                // Конвертируем объект DataModel в Map
                                Map<String, Object> data = new HashMap<>();
                                data.put("barcode", dataModel.getBarcode());
                                data.put("item_name", dataModel.getItem_name());
                                data.put("item_quantity", dataModel.getItem_quantity());
                                data.put("expiration_date", dataModel.getExpiration_date());
                                data.put("comment_text", dataModel.getComment_text());

                                // Добавляем данные в Firestore с уникальным идентификатором
                                addDataToFirestore(documentId, data);
                            } else {
                                // Если данные с таким ID уже существуют, выводим сообщение об ошибке
                                Toast.makeText(MainActivity.this, "Данные с таким ID уже существуют в базе", Toast.LENGTH_SHORT).show();
                                editTextText6.setText("");
                                editTextText2.setText("");
                                editTextText3.setText("");
                                editTextText4.setText("");
                                editTextText5.setText("");
                            }
                        } else {
                            Log.d("MainActivity", "Error checking document existence", task.getException());
                            Toast.makeText(MainActivity.this, "Ошибка при проверке данных в базе", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Произошла ошибка при добавлении данных", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendUpdatedArrayToEditingAndDeleting(JSONArray updatedArray) {
        Intent intent = new Intent(this, editingAndDeleting.class);
        intent.putExtra("jsonArray", updatedArray.toString());
        startActivity(intent);
    }

    private void writeJsonToFile(String fileName, String json) {
        try {
            FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(json.getBytes());
            fos.close();
            Toast.makeText(this, "Данные сохранены в " + fileName, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка сохранения данных", Toast.LENGTH_SHORT).show();
        }
    }
}