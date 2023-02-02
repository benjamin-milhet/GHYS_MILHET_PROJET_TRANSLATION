package com.milhet.translationapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


import android.view.View;
import android.widget.ArrayAdapter;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.androidnetworking.AndroidNetworking;

import com.androidnetworking.error.ANError;

import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
 String token ;
 ArrayList<Language> languages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ImageButton btnHistorique = findViewById(R.id.btnHistorique);
        final ImageButton btnParametres = findViewById(R.id.btnParametres);

        btnHistorique.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, HistoriqueActivity.class);
            MainActivity.this.startActivity(intent);

        });

        btnParametres.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ParametreActivity.class);
            MainActivity.this.startActivity(intent);
        });


        token = "0da5a1b3-1637-fd73-e550-79b8954cf379:fx";
        languages.add(new Language("", ""));
        loadLanguage();

    }

    public void loadLanguage() {

         Context that = this;
        AndroidNetworking.get("https://api-free.deepl.com/v2/languages")
                .addHeaders("Authorization", "DeepL-Auth-Key " + token)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            String lName;
                            String lCode;

                            for (int i = 0; i < response.length(); i++) {
                                JSONObject languageI = response.getJSONObject(i);
                               lName = languageI.getString("name");
                                 lCode = languageI.getString("language");
                                languages.add (new Language( lCode,lName));

                            }
                            ArrayAdapter<Language> adapter = new ArrayAdapter<>(that, android.R.layout.simple_spinner_dropdown_item, languages);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            Spinner spinner = findViewById(R.id.spinnerListeLangue);
                            spinner.setAdapter(adapter);
                           // System.out.println(languages.get(1).getLanguage());
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        System.out.println(anError);
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    public void translateButton(View view) {
        System.out.println("translateButton");
        Context that = this;
        Spinner spinner = findViewById(R.id.spinnerListeLangue);
        Language language = (Language) spinner.getSelectedItem();
        EditText textATraduire = findViewById(R.id.editTexteATraduire);

        //paramètres de la requête:
        String textToTranslate = textATraduire.getText().toString();
        String target_lang = language.getLanguage();

        TextView textTraduit = findViewById(R.id.textViewTexteTraduit);
        TextView detected_source_language = findViewById(R.id.affichageLangueDetectee);

        if (!textToTranslate.isEmpty() && !target_lang.isEmpty()) {
            translate(textToTranslate, target_lang, textTraduit, detected_source_language);

// gestion des erreurs
        } else if (textToTranslate.isEmpty()&& target_lang.isEmpty()) {
            textTraduit.setText("Veuillez entrer un texte à traduire et une langue de traduction");
            textTraduit.setTextColor(that.getColor(R.color.red));


        }else if (textToTranslate.isEmpty()) {
            textTraduit.setText("Veuillez entrer un texte à traduire");
            textTraduit.setTextColor(that.getColor(R.color.red));

        }else {
        textTraduit.setText("Veuillez entrer une langue de traduction");
        textTraduit.setTextColor(that.getColor(R.color.red));
        }
    }


    public void translate(String textToTranslate, String target_lang, TextView textTraduit, TextView detected_source_language) {
        AndroidNetworking.post("https://api-free.deepl.com/v2/translate")
                .addHeaders("Authorization", "DeepL-Auth-Key " + token)
                .addBodyParameter("text", textToTranslate)
                .addBodyParameter("target_lang", target_lang)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String traduction = response.getJSONArray("translations")
                                    .getJSONObject(0)
                                    .getString("text");
                            String langueDetectee = response.getJSONArray("translations")
                                    .getJSONObject(0)
                                    .getString("detected_source_language");

                            // On affiche la traduction dans le TextView
                            textTraduit.setText(traduction);
                            textTraduit.setTextColor(getResources().getColor(R.color.black));


                            // On cherche le nom de la langue dans la liste des langues disponibles
                           String detectedLanguageName = "";
                            for (Language language : languages) {
                                if (language.getLanguage().equals(langueDetectee)) {
                                    detectedLanguageName = language.getName();
                                    break;
                                }
                            }
                            // On affiche le nom de la langue dans le TextView
                            detected_source_language.setText(detectedLanguageName);



                        } catch (JSONException e) {

                            System.out.println("exception traduction");
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        System.out.println("erreur traduction");
                    }
                });





    }
}