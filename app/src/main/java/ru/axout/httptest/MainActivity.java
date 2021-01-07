package ru.axout.httptest;

import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView contentView = findViewById(R.id.content);
        final WebView webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        Button btnFetch = findViewById(R.id.downloadBtn);
        final String link = getResources().getString(R.string.link);

        btnFetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentView.setText("Загрузка...");

                // поскольку загрузка может занять долгое время,
                // то метод, загружающий веб-страницу, работает в отдельном потоке
                new Thread(new Runnable() {
                    public void run() {
                        try{
                            // метод getContent() будет загружать веб-страницу
                            final String content = getContent(link);
                            webView.post(new Runnable() {
                                public void run() {
                                    webView.loadDataWithBaseURL(link,content, "text/html", "UTF-8", link);
                                    Toast.makeText(getApplicationContext(), "Данные загружены", Toast.LENGTH_SHORT).show();
                                }
                            });
                            contentView.post(new Runnable() {
                                public void run() {
                                    contentView.setText(content);
                                }
                            });
                        }
                        catch (final IOException ex){
                            contentView.post(new Runnable() {
                                public void run() {
                                    contentView.setText("Ошибка: " + ex.getMessage());
                                    Toast.makeText(getApplicationContext(), "Ошибка", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        });
    }

    // метод, который будет загружать веб-страницу
    private String getContent(String path) throws IOException {
        BufferedReader reader = null;
        InputStream stream = null;
//        HttpsURLConnection connection = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(path);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET"); // установка метода получения данных -GET
            connection.setReadTimeout(10000); // установка таймаута перед выполнением - 10 000 миллисекунд
            connection.connect(); // подключаемся к ресурсу

            // после подключение происходит считывание со входного потока
            stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));

            // используя входной поток, мы можем считать его в строку
            StringBuilder buf = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                buf.append(line).append("\n");
            }
            return buf.toString();
        }
        finally {
            if (reader != null) {
                reader.close();
            }
            if (stream != null) {
                stream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}