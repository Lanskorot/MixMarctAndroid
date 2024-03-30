package com.example.prosrok;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import android.util.Log;

import androidx.annotation.NonNull;

public class ShowInfo extends Activity {
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<Pair<Date, DataModel>> listData;
    private FirebaseFirestore db;
    private String selectedDatabase;
    private static final String TAG = "ShowInfo";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_info);
        Log.d(TAG, "onCreate()");

        final LinearLayout linearLayout = findViewById(R.id.scrollView);

        init();
        db = FirebaseFirestore.getInstance();

        SharedPreferences preferences = getSharedPreferences("com.example.prosrok", Context.MODE_PRIVATE);
        selectedDatabase = preferences.getString("selectedDatabase", "tivat");
        Log.d(TAG, "Selected database: " + selectedDatabase);

        Button button3 = findViewById(R.id.button3);
        Button button9 = findViewById(R.id.button9);
        Button button6 = findViewById(R.id.button6);
        Button button7 = findViewById(R.id.button7);
        Button button8 = findViewById(R.id.button8);

        listData.clear();

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Создаем намерение для перехода в MainActivity
                Intent intent = new Intent(ShowInfo.this, MainActivity.class);
                // Запускаем активити MainActivity
                startActivity(intent);
            }
        });

        button9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linearLayout.setBackgroundColor(Color.WHITE);
                adapter.clear();
                // Вызываем метод для получения данных и обновления списка
                fetchDataAndUpdateList();
            }
        });

        fetchDataAndUpdateList();



        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int redColorWithTransparency = Color.argb(128, Color.red(getResources().getColor(R.color.red)),
                        Color.green(getResources().getColor(R.color.red)),
                        Color.blue(getResources().getColor(R.color.red)));
                linearLayout.setBackgroundColor(redColorWithTransparency); // Устанавливаем цвет с прозрачностью как фон
                adapter.clear();
                // Получаем сегодняшнюю дату
                Calendar calendar = Calendar.getInstance();
                // Отнимаем один день
                calendar.add(Calendar.DAY_OF_YEAR, -1);
                Date yesterdayDate = calendar.getTime();

                // Фильтруем список данных по датам до вчерашнего дня
                List<Pair<Date, DataModel>> filteredListData = new ArrayList<>();
                for (Pair<Date, DataModel> pair : listData) {
                    Date expirationDate = pair.first;
                    if (expirationDate.before(yesterdayDate)) {
                        filteredListData.add(pair);
                    }
                }

                // Очищаем адаптер и выводим отфильтрованные данные
                adapter.clear();
                for (Pair<Date, DataModel> pair : filteredListData) {
                    DataModel dataModel = pair.second;
                    String item = "Код товара: " + dataModel.getBarcode() + "\n"
                            + "Название: " + dataModel.getItem_name() + "\n"
                            + "Количество: " + dataModel.getItem_quantity() + "\n"
                            + "Дата окончания срока: " + dataModel.getExpiration_date() + "\n"
                            + "Комментарий: " + dataModel.getComment_text()+ "\n"
                            + " ";

                    adapter.add(item);
                }
            }
        });


        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int orangeColorWithTransparency = Color.argb(128,
                        Color.red(getResources().getColor(R.color.orange)),
                        Color.green(getResources().getColor(R.color.orange)),
                        Color.blue(getResources().getColor(R.color.orange)));
                linearLayout.setBackgroundColor(orangeColorWithTransparency);
                adapter.clear();
                // Получаем вчерашнюю дату
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_YEAR, -1);
                Date yesterdayDate = calendar.getTime();

                // Получаем дату через два дня
                calendar.add(Calendar.DAY_OF_YEAR, 3);
                Date twoDaysLaterDate = calendar.getTime();

                // Фильтруем список данных по датам
                List<Pair<Date, DataModel>> filteredListData = new ArrayList<>();
                for (Pair<Date, DataModel> pair : listData) {
                    Date expirationDate = pair.first;
                    if (expirationDate.after(yesterdayDate) && expirationDate.before(twoDaysLaterDate)) {
                        filteredListData.add(pair);
                    }
                }

                // Очищаем адаптер и выводим отфильтрованные данные
                adapter.clear();
                for (Pair<Date, DataModel> pair : filteredListData) {
                    DataModel dataModel = pair.second;
                    String item = "Код товара: " + dataModel.getBarcode() + "\n"
                            + "Название: " + dataModel.getItem_name() + "\n"
                            + "Количество: " + dataModel.getItem_quantity() + "\n"
                            + "Дата окончания срока: " + dataModel.getExpiration_date() + "\n"
                            + "Комментарий: " + dataModel.getComment_text()+ "\n"
                            + " ";

                    adapter.add(item);
                }
            }
        });


        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int greenColorWithTransparency = Color.argb(128,
                        Color.red(getResources().getColor(R.color.green)),
                        Color.green(getResources().getColor(R.color.green)),
                        Color.blue(getResources().getColor(R.color.green)));
                linearLayout.setBackgroundColor(greenColorWithTransparency);
                adapter.clear();
                // Получаем сегодняшнюю дату
                Calendar calendar = Calendar.getInstance();
                // Добавляем три дня к текущей дате
                calendar.add(Calendar.DAY_OF_YEAR, 2);
                Date threeDaysLaterDate = calendar.getTime();

                // Добавляем еще четыре дня для получения даты через 7 дней от исходной даты
                calendar.add(Calendar.DAY_OF_YEAR, 4);
                Date sevenDaysLaterDate = calendar.getTime();

                // Фильтруем список данных по датам от трех дней вперед до семи дней вперед
                List<Pair<Date, DataModel>> filteredListData = new ArrayList<>();
                for (Pair<Date, DataModel> pair : listData) {
                    Date expirationDate = pair.first;
                    if (expirationDate.after(threeDaysLaterDate) && expirationDate.before(sevenDaysLaterDate)) {
                        filteredListData.add(pair);
                    }
                }

                // Очищаем адаптер и выводим отфильтрованные данные
                adapter.clear();
                for (Pair<Date, DataModel> pair : filteredListData) {
                    DataModel dataModel = pair.second;
                    String item = "Код товара: " + dataModel.getBarcode() + "\n"
                            + "Название: " + dataModel.getItem_name() + "\n"
                            + "Количество: " + dataModel.getItem_quantity() + "\n"
                            + "Дата окончания срока: " + dataModel.getExpiration_date() + "\n"
                            + "Комментарий: " + dataModel.getComment_text()+ "\n"
                            + " ";

                    adapter.add(item);
                }
            }
        });

        final String finalSelectedDatabase = selectedDatabase;

        db.collection(selectedDatabase)

                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        listData.clear();
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String barcode = documentSnapshot.getString("barcode");
                            String itemName = documentSnapshot.getString("item_name");
                            String quantity = documentSnapshot.getString("item_quantity");
                            String expirationDate = documentSnapshot.getString("expiration_date");
                            String idNumber = documentSnapshot.getString("id_number");
                            String commentText = documentSnapshot.getString("comment_text");
                            Log.d(TAG, "Data retrieval successful");

                            try {
                                SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
                                Date expirationDateObj = format.parse(expirationDate);

                                DataModel dataModel = new DataModel(idNumber, barcode, itemName, quantity, expirationDate, commentText);

                                listData.add(new Pair<>(expirationDateObj, dataModel));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        Collections.sort(listData, new Comparator<Pair<Date, DataModel>>() {
                            @Override
                            public int compare(Pair<Date, DataModel> o1, Pair<Date, DataModel> o2) {
                                return o1.first.compareTo(o2.first);
                            }
                        });

                        adapter.clear();

                        for (Pair<Date, DataModel> pair : listData) {
                            DataModel dataModel = pair.second;
                            String item = "Код товара: " + dataModel.getBarcode() + "\n"
                                    + "Название: " + dataModel.getItem_name() + "\n"
                                    + "Количество: " + dataModel.getItem_quantity() + "\n"
                                    + "Дата окончания срока: " + dataModel.getExpiration_date() + "\n"
                                    + "Комментарий: " + dataModel.getComment_text()+ "\n"
                                    + " ";

                            adapter.add(item);
                        }
                        Log.d(TAG, "Data retrieval successful");
                    }
                });
    }



    private void fetchDataAndUpdateList() {
        Log.d(TAG, "fetchDataAndUpdateList(): Started fetching data...");
        listData.clear();
        final String finalSelectedDatabase = selectedDatabase;
        db.collection(selectedDatabase)
            .get()
            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    Log.d(TAG, "fetchDataAndUpdateList(): Data fetching successful");
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String barcode = documentSnapshot.getString("barcode");
                        String itemName = documentSnapshot.getString("item_name");
                        String quantity = documentSnapshot.getString("item_quantity");
                        String expirationDate = documentSnapshot.getString("expiration_date");
                        String idNumber = documentSnapshot.getString("id_number");
                        String commentText = documentSnapshot.getString("comment_text");

                        try {
                            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
                            Date expirationDateObj = format.parse(expirationDate);

                            DataModel dataModel = new DataModel(idNumber, barcode, itemName, quantity, expirationDate, commentText);

                            listData.add(new Pair<>(expirationDateObj, dataModel));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    Collections.sort(listData, new Comparator<Pair<Date, DataModel>>() {
                        @Override
                        public int compare(Pair<Date, DataModel> o1, Pair<Date, DataModel> o2) {
                            return o1.first.compareTo(o2.first);
                        }
                    });

                    adapter.clear();

                    for (Pair<Date, DataModel> pair : listData) {
                        DataModel dataModel = pair.second;
                        String item = "Код товара: " + dataModel.getBarcode() + "\n"
                                + "Название: " + dataModel.getItem_name() + "\n"
                                + "Количество: " + dataModel.getItem_quantity() + "\n"
                                + "Дата окончания срока: " + dataModel.getExpiration_date() + "\n"
                                + "Комментарий: " + dataModel.getComment_text()+ "\n"
                                + " ";

                        adapter.add(item);
                    }
                }
            });
    }

    private void deleteSelectedUid(String selectedId){
        db.collection(selectedDatabase).document(selectedId)
            .delete()
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "DocumentSnapshot successfully deleted!");
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error deleting document", e);
                }
            });
    }

    interface OptionSelectedListener {
        void onOptionSelected(int option);
    }

    private void showOptionsDialog(final OptionSelectedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите действие");

        builder.setItems(new CharSequence[]{"Удалить", "Редактировать"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onOptionSelected(which);
                }
            }
        });
        builder.create().show();
    }

    private void init() {
        listView = findViewById(R.id.textView6);
        listData = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);

        // Добавляем слушатель для обработки долгого нажатия на элемент списка
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // Показываем всплывающее окно с опциями при долгом нажатии
                showOptionsDialog(new OptionSelectedListener() {
                    @Override
                    public void onOptionSelected(int option) {
                        DataModel answerModel = listData.get(position).second;
                        // needed fields for processing
                        String editedBarcode = answerModel.getBarcode();
                        String editedItemName = answerModel.getItem_name();
                        String editedExpirationDate = answerModel.getExpiration_date();
                        // made document ID in firebase
                        String selectedUid = editedBarcode + "-" + editedExpirationDate;
                        switch (option){
                            case 0:
                                deleteSelectedUid(selectedUid);
                                fetchDataAndUpdateList();
                                break;
                            case 1:
                                String[] editDataArray = {editedBarcode, editedItemName,
                                                          editedExpirationDate};
                                Intent editIntent = new Intent(ShowInfo.this, MainActivity.class);
                                editIntent.putExtra("selectedToEditUid", editDataArray);
                                deleteSelectedUid(selectedUid);
                                // Запускаем активити MainActivity
                                startActivity(editIntent);
                                break;
                        }
                    }
                });
                return true; // Указываем, что событие было обработано
            }
        });
    }
}