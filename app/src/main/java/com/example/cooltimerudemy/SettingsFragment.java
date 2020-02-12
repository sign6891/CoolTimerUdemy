package com.example.cooltimerudemy;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

//Класс для меню Settings.
public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceChangeListener {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.timer_preferences);

        //отобразить в настройках тип выбранной мелодии. А они у нас находятся в листе
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();

        //получаем количество настроек(сейчас их 4(1 checkBox + 3 песни из листа ))
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        int count = preferenceScreen.getPreferenceCount();

        //пробегаемся по всем видам настройки и отбераем нужные нам
        for (int i = 0; i < count; i++) {
            Preference preference = preferenceScreen.getPreference(i);

            if (!(preference instanceof CheckBoxPreference)) {
                //по ключу достаем значения
                String value = sharedPreferences.getString(preference.getKey(), "");
                //отправляем данные в метод для выбора значения из листа и отрисовки
                setPreferenceLable(preference, value);
            }
        }
        //дать разрешение на запись инфы в файл sharedpreferences
        Preference preference = findPreference("default_interval");
        preference.setOnPreferenceChangeListener(this);
    }

    //Метод отвечающий за отрисовку в меню настроек выбраных элементов
    private void setPreferenceLable(Preference preference, String value) {
        if (preference instanceof ListPreference) {
            // получаем объект ListPreference и присваеваем ему значение относительно позиции в листе с песнями
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(value);
            if (index >= 0) {
                //устанавливаем запись относительно индекса
                listPreference.setSummary(listPreference.getEntries()[index]);
            }
        } else if (preference instanceof EditTextPreference) {
            preference.setSummary(value);
        }
    }

    // Метод для обновления значения песни после ее смены(иначи в окне песня меняется а значение нет)
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (!(preference instanceof CheckBoxPreference)) {
            String value = sharedPreferences.getString(preference.getKey(), "");
            setPreferenceLable(preference, value);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    //Проверка на корректность введеного значения по дефолту в настройках, где задаем секунды
    //так как могут ввести буквы или символы
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals("default_interval")) {
            String defaultIntervalString = (String) newValue;

            try {
                //Если значение пришедшей строки не парсится в инт выпадает Exception
                int defaultInterval = Integer.parseInt(defaultIntervalString);
            } catch (NumberFormatException nfe) {
                Toast.makeText(getContext(), "Please enter an integer number", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }
}
