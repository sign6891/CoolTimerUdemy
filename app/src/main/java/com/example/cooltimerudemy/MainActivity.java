package com.example.cooltimerudemy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOError;
import java.io.IOException;

public class MainActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private TextView tvTimer;
    private SeekBar seekBar;
    private boolean isTimerOn = false;
    private Button startB;
    private CountDownTimer countDownTimer;
    MediaPlayer mediaPlayer;
    private int defaultInterval;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        startB = findViewById(R.id.btStart);
        tvTimer = findViewById(R.id.tvTimer);
        seekBar = findViewById(R.id.seekBar);
        seekBar.setMax(900);
        setIntervalFromSharedPreferences(sharedPreferences);

        //Работа с seekBar. реакция на движение ползунка
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                long progressInMilles = progress * 1000;
                updateTimer(progressInMilles);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

    }

    //Реакция на кнопку Старт/Стоп
    public void start(View view) {

        if (!isTimerOn) {
            startB.setText("Stop");
            seekBar.setEnabled(false);
            isTimerOn = true;
            // Секундомер с обратным отчетом времени
            countDownTimer = new CountDownTimer(seekBar.getProgress() * 1000, 1000) {

                //переопределенный метод который вызывается каждую секунду(интервал задается выше
                // при инициализации переменной countDownTimer
                @Override
                public void onTick(long millisUntilFinished) {
                    updateTimer(millisUntilFinished);
                }
                //данный метод вызывается когда секундомер равен 0
                @Override
                public void onFinish() {
                    //Сохранение положение настроек в меню Settings
                    //Реакция на положение CheckBox, если true, то звук есть, если false, то звука нет
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    if (sharedPreferences.getBoolean("enable_sound", true)) {

                        // В зависимости какую мелодию выбрал юзер в настройках
                        String melodyName = sharedPreferences.getString("timer_melody", "bell");
                        if (melodyName.equals("bell")) {
                            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.bell_sound);
                        } else if (melodyName.equals("bip")) {
                            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.bip_sound);
                        } else if (melodyName.equals("alarm siren")) {
                            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alarm_siren_sound);
                        }
                        mediaPlayer.start();
                    }
                    resetTimer();
                }
            };
            countDownTimer.start();

        } else {
            resetTimer();
        }

    }

    //Обновление времени по аргументу каждую секунду
    @SuppressLint("SetTextI18n")
    private void updateTimer(long millisUntilFinished) {
        int minutes = (int) millisUntilFinished / 1000 / 60;
        int seconds = (int) millisUntilFinished / 1000 - (minutes * 60);

        String minutesString = "";
        String secondsString = "";

        if (minutes < 10) {
            minutesString = "0" + minutes;
        } else {
            minutesString = String.valueOf(minutes);
        }

        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = String.valueOf(seconds);
        }

        tvTimer.setText(minutesString + ":" + secondsString);
    }

    // Сброс таймера к начальным настройкам и
    // блокировка seekBar
    // очистка таймера countDownTimer
    @SuppressLint("SetTextI18n")
    private void resetTimer() {
        countDownTimer.cancel();
        startB.setText("Start");
        setIntervalFromSharedPreferences(sharedPreferences);
        seekBar.setEnabled(true);
        isTimerOn = false;
    }

    // создание меню в активити справа в верхнем углу в виде трех точек или иконок
    // изначально создается директория в папке res - menu и там xml с item
    //В данном приложении мы используем fragment для отображения полей с настройками
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.timer_menu, menu);
        return true;
    }

    //обработка нажатия на элементы меню и переход на другие Активити
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intentSettings = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intentSettings);
            return true;
        } else if (id == R.id.action_about) {
            Intent intentAbout = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intentAbout);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //устанавливаем секундомер на дефолтное значение в настройках
    //храним это значение в файле sharedPreferences
    private void setIntervalFromSharedPreferences(SharedPreferences sharedPreferences) {
        //парсим стринг в инт
        defaultInterval = Integer.valueOf(sharedPreferences.getString("default_interval", "30"));
        long defaultIntervalMillis = defaultInterval * 1000;
        updateTimer(defaultIntervalMillis);
        seekBar.setProgress(defaultInterval);
    }

    //устанавливаем значение в настройках под выбором дефолтных секунд
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("default_interval")) {
            setIntervalFromSharedPreferences(sharedPreferences);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Имеет отношение к устоновке в настройках значения под именем настройки
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }
}
