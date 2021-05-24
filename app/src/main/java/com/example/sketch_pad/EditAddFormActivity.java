package com.example.sketch_pad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.example.sketch_pad.databinding.EditAddFormBinding;

import java.util.Date;

import static com.example.sketch_pad.MainActivity.id_Sketch;

public class EditAddFormActivity extends AppCompatActivity {
    private Dialog dialog;  // для диалогового окна
    private SQLiteDatabase db;
    private EditAddFormBinding editAddFormBinding;  // Файл биндинга для edit_add_form
    private Cursor query;   // Курсор для запросов
    private byte mode;  // режим работы карточки
    private String labelText = "";   // Заголовок
    private String mainText = "";    // Текст карточки
    private Date date = new Date(); // для передачи даты в базу данных
    private byte saveButtonClicked; // фиксирует нажатие кнопки Save в ActionBar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editAddFormBinding = DataBindingUtil.setContentView(this, R.layout.edit_add_form);
        // Устраняет автофокус на первом EditText при запуске. Т.е. клавиатура появляется только при выборе поля ввода, а не автоматически.
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        db = getBaseContext().openOrCreateDatabase("sketch.db", MODE_PRIVATE, null);
        saveButtonClicked = 0;
        // mode == 1 означает, что карточка открыта на редактирование, 0 - на создание записи
        mode = getIntent().getExtras().getByte("mode");
        if(mode == 1) selectDataID();
    }

    // Вызывает диалоговое окно
    private void openSaveSketchDialog(){
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);   // Скрывает заголовок диалогового окна
        dialog.setContentView(R.layout.save_sketch_dialog); // Путь к макету диалогового окна
        dialog.setCancelable(false);    // окно нельзя закрыть кнопкой назад

        Button button_yes = dialog.findViewById(R.id.btn_save_sketch_dialog_yes);
        Button button_no = dialog.findViewById(R.id.btn_save_sketch_dialog_no);
        Button button_return = dialog.findViewById(R.id.btn_save_sketch_dialog_return);

        button_return.setOnClickListener((v) -> dialog.dismiss());  // закрываем диалоговое окно
        button_yes.setOnClickListener((v) -> {
            saveMenuItemSelected();
            dialog.dismiss();
            button_Back_Pressed();  // если будет после обращения к суперклассу, то будет промаргивать белый экран
            super.onBackPressed();
        });
        button_no.setOnClickListener((v) -> {
            if(saveButtonClicked == 0) id_Sketch = -1; // сбрасываем id записи
            dialog.dismiss();
            button_Back_Pressed();  // если будет после обращения к суперклассу, то будет промаргивать белый экран
            super.onBackPressed();
        });
        dialog.show();  // Показать диалоговое окно
    }

    private void saveToDataBase(){
        db.execSQL("INSERT INTO sketches VALUES (null, " + date.getTime() + ",'"
                + labelText.replace("'","''") + "','"
                + mainText.replace("'","''") + "');");
    }

    private void updateToDataBase(){
        db.execSQL("UPDATE sketches SET " +
                "date_of = " + date.getTime() +
                ", label_sketch = '" + labelText.replace("'","''") + "'," +
                "text_sketch = '" + mainText.replace("'","''") + "'" +
                "WHERE id_sketch = " + id_Sketch + ";");
    }

    // Заполняет поля данными из таблицы
    private void selectDataID(){
        query = db.rawQuery("SELECT * FROM sketches WHERE id_sketch = " + id_Sketch + ";", null);
        query.moveToNext();
        editAddFormBinding.formLabel.setText(query.getString(2));
        editAddFormBinding.formText.setText(query.getString(3));
        query.close();
    }

    // Перенастроим системную кнопку "Назад"
    @Override
    public void onBackPressed() {
        getDataFromForm();
        if(labelText.length() > 0 || mainText.length() > 0) openSaveSketchDialog();
        else {
            button_Back_Pressed();  // если будет после обращения к суперклассу, то будет промаргивать белый экран
            super.onBackPressed();
        }
    }

    // Помещает данные из карточки в переменные
    private void getDataFromForm(){
        labelText = editAddFormBinding.formLabel.getText().toString();
        mainText = editAddFormBinding.formText.getText().toString();
    }

    // Возвращает индекс сохранённой новой записи
    private void getLastIndexFromDB(){
        query = db.rawQuery("SELECT MAX(id_sketch) FROM sketches ;", null);
        query.moveToNext();
        id_Sketch = query.getInt(0);
        query.close();
    }

    private void saveMenuItemSelected(){
        saveButtonClicked = 1;
        if(mode == 1) updateToDataBase();
        else {
            saveToDataBase();
            // Получаем id последней сохраненной записи
            getLastIndexFromDB();
            mode = 1;   // Включает режим редактирования, чтобы запись не дублировалась.
        }
    }

    private void shareMenuItemSelected(){
        getDataFromForm();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Заголовок:\n" + labelText + "\nТекст карточки:\n" + mainText); // То, что отправляется в другое приложение
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));  // getResources().getText(R.string.send_to) формирует заголовок плашки шаринга
    }

    // создает меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_add_form_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        getDataFromForm();
        switch (item.getItemId()){
            case R.id.save_form: saveMenuItemSelected(); break;
            case R.id.menu_item_share: shareMenuItemSelected(); break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void button_Back_Pressed(){
        db.close();
        // закрываем текущую активность
        finish();
    }
}