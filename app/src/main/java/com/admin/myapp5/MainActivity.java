package com.admin.myapp5;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.ktx.Firebase;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private DataBase dbHelper;
    private ListView all_tasks;
    private ArrayAdapter<String> my_adapter;    //адаптер для слоя row
    private EditText field_text;
    private SharedPreferences prefs;  // через этот объект мы можем ссылаться на те данные, которые у нас сохранены и сохранять данные в разных форматах
    private String name_list; //название списка

    private TextView infoApp;
    private String text_to_delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        dbHelper = new DataBase(this);
        all_tasks = findViewById(R.id.taskslist);
        field_text = findViewById(R.id.list_name);

        //получаем данные (сначала будет просто пустая строка)Тут находятся все сохраненные данные
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //получаем данные для название списка задач(дел листтекст)
        name_list = prefs.getString("list_name", "");
        field_text.setText(name_list);

        //анимация
        infoApp = findViewById(R.id.infoapp);
        infoApp.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_text));

        changeTextAction();
        loadAllTasks();
    }

    private void changeTextAction() {
        //обработчик событий, который срабатывает при изменении информации в текстовом поле
        field_text.addTextChangedListener(new TextWatcher() {

            //срабатывает начале ввода символов, когда еще не введен весь текст до конца
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            //когда уже ввели текст, но не нажали на кнопку(держим кнопку)
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SharedPreferences.Editor editPrefs = prefs.edit(); // позволит записывать новые значения в локальное хранилище
                //функция, которая позволит поместить в определенную переменную некие значения/\. т.е сохраняем в поле эдит текст введенное значение
                editPrefs.putString("list_name", String.valueOf(field_text.getText()));
               // editPrefs.commit(); // позволит сохранить эти данные
                editPrefs.apply(); // позволит сохранить эти данные(более предпочтительный)
            }

            //после нажатия на кнопку(отпустили кнопку)
            @Override
            public void afterTextChanged(Editable s) {}
        });

    }

    private void loadAllTasks() {
        ArrayList<String> taskList = dbHelper.getAllTasks();
        if(my_adapter == null){
            //контекст   шаблон        текстовое поле(с которым хотим рабоать из шаблона!!)
            my_adapter = new ArrayAdapter<String>(this, R.layout.row, R.id.txttask, taskList);
            all_tasks.setAdapter(my_adapter);
        } else {
            my_adapter.clear();
            my_adapter.addAll(taskList);
            my_adapter.notifyDataSetChanged();
            all_tasks.setAdapter(my_adapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);   //получам созданое нами меню из ресурсов
        Drawable icon = menu.getItem(0).getIcon();  //получаем созданую в меню иконку
        icon.mutate();
        icon.setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_IN);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.add_new_task){
            final EditText userTaskGet = new EditText(this);     //создаем текстовое поле
            AlertDialog dialog = new AlertDialog.Builder(this). // создаем всплывающее окно
                    setTitle("Добавление нового задания").
                    setMessage("Чтобы вы хотели добавить").
                    setView(userTaskGet).                       //то мы хотим добавить(текстовое поле)
                    setPositiveButton("Добавить", new DialogInterface.OnClickListener() {  //создаем кнопку для всплывающего окно
                        @Override
                        public void onClick(DialogInterface dialog, int which) {        // он клик листенер на эту кнопку
                            String task = String.valueOf(userTaskGet.getText());       // получаем строку, которую ввле юзер
                            dbHelper.insertData(task);                                  //Это метод в классе ДатаБэйс для добавления таска
                            loadAllTasks();
                        }
                    }).
                    setNegativeButton("Ничего", null).      //негативная кнопка, которая ничего не делает
                    create();
            dialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void deleteTask(View view){
        View viewParent = (View) view.getParent();   //обращаемся к объекту выше по xml в активита, то биш к textView
        //получаем текствюь по объекту родителя вью, который передам аргументом)(то бишь кнопка(предок) на нее мы установили этот метод(на onClick))
        TextView txt_task = viewParent.findViewById(R.id.txttask);
        //берем текст из этой вьюшки и удаляем его через метод класса db
        text_to_delete = String.valueOf(txt_task.getText());

        AlertDialog alertDialog = new AlertDialog.Builder(this).
                setTitle("Ты точно закончил с этим?").
                setPositiveButton("Да, я все сделал!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //добавляем анимацию на удаление
                        //анимация будет идти до 0 (0-это объект будет полностью прозрачным)
                        showText();
                        view.animate().
                                translationX(1000).
                                alpha(0).
                                setDuration(1000).  // время анимации в млс
                                withEndAction(new Runnable() {  // создаем поток после анимации который удаляет
                            @Override
                            public void run() {
                                dbHelper.deleteData(text_to_delete);
                                loadAllTasks();
                            }
                        });
                    }
                }). setNegativeButton("Еще нет", null).
                create();
        alertDialog.show();
    }

    public void showText(){
        Toast.makeText(this, "Ты продуктивен!", Toast.LENGTH_SHORT).show();
    }

}