package com.example.sketch_pad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    protected static int id_Sketch = -1;  // id записи в базе данных
    protected static int Sketch_position = -1;    // Позиция записи в списке
    private SQLiteDatabase db;  // база данных
    private Cursor query;   // Курсор для запросов
    private List<Sketch> sketches = new ArrayList<Sketch>();
    private List<Sketch> sketchesSearch = new ArrayList<Sketch>();  // используется для запросов
    private SketchAdapter adapter;
    private RecyclerView sketch_List;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private byte searchMode = 0;
    private String selectWhere = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = getBaseContext().openOrCreateDatabase("sketch.db", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS sketches (id_sketch INTEGER PRIMARY KEY, date_of BIGINT, label_sketch NVARCHAR, text_sketch TEXT)");

        handleIntent(getIntent());
        addDataFromDataBase(sketches);
        sketch_List = findViewById(R.id.sketchList);
        //инициализирует адаптер и присваивает его списку
        adapter = new SketchAdapter();
        sketch_List.setLayoutManager(new LinearLayoutManager(this));
        sketch_List.setAdapter(adapter);
    }

    private void aboutMenuItemSelected(){
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);   // Скрывает заголовок диалогового окна
        dialog.setContentView(R.layout.about_dialog); // Путь к макету диалогового окна

        TextView aboutDialogText = dialog.findViewById(R.id.text_about_dialog);
        aboutDialogText.setText(getString(R.string.about, BuildConfig.VERSION_NAME));

        dialog.show();  // Показать диалоговое окно
    }

    // создает меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // Подключаем виджет поисковой
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true); // строка поиска изначально свернута в значок
        searchView.setSubmitButtonEnabled(true);    // Включает кнопку подтверждения запроса, т.е. вместо Enter на клавиатуре
        searchView.setOnCloseListener(() -> {
            selectWhere = "";
            searchMode = 0; // Поиск закрыт
            adapter.refreshSketches(sketches);
            return false;
        });

        return super.onCreateOptionsMenu(menu);
    }

    // инициализация обработчиков при выборе пункта меню
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.about: aboutMenuItemSelected(); break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
        super.onNewIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            selectWhere = intent.getStringExtra(SearchManager.QUERY).replace("'","''");
            searchMode = 1; // Инициирован поиск
            addDataFromDataBase(sketchesSearch);
            adapter.refreshSketches(sketchesSearch);
        }
    }

    // Запрашивает данные из базы данных и помещает их в список
    private void addDataFromDataBase(List<Sketch> listAdd){
        if(selectWhere.length() > 0){
            listAdd.clear(); // Очищает список перед выполнением поиска
            query = db.rawQuery("SELECT * FROM sketches WHERE label_sketch LIKE '%"
                + selectWhere + "%' OR text_sketch LIKE '%" + selectWhere + "%' ORDER BY date_of ASC;", null);
        }
        else
            query = db.rawQuery("SELECT * FROM sketches ORDER BY date_of ASC;", null);
        while (query.moveToNext()){
            listAdd.add(new Sketch(query.getInt(0),
                    dateFormat.format(query.getLong(1)),
                    query.getString(2),
                    query.getString(3)));
        }
        query.close();  // Закрываем курсор
    }

    public void openAddForm(View view){
        try{
            Intent intent = new Intent(this, EditAddFormActivity.class);
            intent.putExtra("mode", (byte) 0);  // Передаёт режим работы окна редактирования (добавление записи)
            startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        id_Sketch = -1; // сбрасывает id
        Sketch_position = -1; // сбрасывает позицию
        if(searchMode == 0) adapter.refreshSketches(sketches);
        super.onStart();
    }

    @Override
    protected void onStop() {
        db.close(); // закрываем соединение с базой данных
        super.onStop();
    }

    // Обновляет список после переоткрытия главной активности
    private void addAfterRestart(){
        query = db.rawQuery("SELECT * FROM sketches WHERE id_sketch = " + id_Sketch + ";", null);
        query.moveToNext();
        sketches.add(new Sketch(query.getInt(0),
                dateFormat.format(query.getLong(1)),
                query.getString(2),
                query.getString(3)));
        query.close();  // Закрывает курсор
    }

    @Override
    protected void onRestart() {
        // Открывает соединение с базой данных
        db = getBaseContext().openOrCreateDatabase("sketch.db", MODE_PRIVATE, null);
        if(id_Sketch != -1){
            if(Sketch_position != -1) sketches.remove(Sketch_position);
            addAfterRestart();
        }
        super.onRestart();
    }
}